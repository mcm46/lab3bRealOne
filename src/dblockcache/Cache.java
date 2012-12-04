package dblockcache;

import java.util.LinkedList;

import common.Constants;

public class Cache extends DBufferCache
{
	//use the linked list as a queue to implement an LRU eviction policy
	private LinkedList<DBuffer> myCache;
	private LinkedList<Boolean> myHeldBuffers;
	private int myCacheSize = 0;
	private static Cache mySingleton;
	private static final int CACHE_SIZE = 10;
	
	private Cache(int cacheSize)
	{
		myCacheSize = cacheSize;
		myCache = new LinkedList<DBuffer>();
		myHeldBuffers = new LinkedList<Boolean>();
		
		//initialize the lists
		for(int i = 0; i < cacheSize; i++)
		{
			myCache.add(new Buffer(-1, Constants.BLOCK_SIZE));
			myHeldBuffers.add(false);
		}
	}
	
	public synchronized static Cache getInstance()
	{
		if(mySingleton == null)
		{
			mySingleton = new Cache(CACHE_SIZE);
		}
		
		return mySingleton;
	}
	

	@Override
	public DBuffer getBlock(int blockID)
	{
		DBuffer buffer = null;
		synchronized(this)
		{
			boolean foundBlock = false;
			DBuffer searchBuffer;
				//find the block
				for(int i = 0; i < myCacheSize; i++)
				{
					searchBuffer = myCache.get(i);
					if(searchBuffer.getBlockID() == blockID)
					{
						//wait until this block is no longer held
						while(searchBuffer.BLOCK_HELD)
						{
							try
							{
								wait();
							}
							catch (InterruptedException e)
							{
								System.out.println("There was an error waiting in the getBlock() method.");
							}
						}


						//see if the queue has shifted, if the block is not in the queue 
						//anymore then we can't return it so move on
						
						if(myCache.indexOf(searchBuffer) == -1)
						{
							foundBlock = false;
							break;
						}

						if(searchBuffer.getBlockID() != blockID)
						{
							System.out.println("Error in cache: Requested block id " + blockID + " and got block with ID " + buffer.getBlockID());
						}

						myCache.addLast(searchBuffer);
						foundBlock = true;
						buffer = searchBuffer;
						break;
					}

				}

			//if the block is in the cache
			if(foundBlock)
			{
				return buffer;
			}
			else
			{
				//block is not in the cache, we need to evict an old one to make room for this new data
				for(int i = 0; i < myCacheSize; i++)	//iterate this way to make sure we start from the front (I'm not sure how java iterators work)
				{
					//look for a block that is not held and is not busy (may be redundant)
					if(!myCache.get(i).isBusy() && !myCache.get(i).BLOCK_HELD)
					{
						//write back the buffer if it is valid and not clean
						if(!myCache.get(i).checkClean() && myCache.get(i).checkValid())
						{
							myCache.get(i).startPush();
						}


						//evict the buffer and create a new one for this request
						myCache.remove(i);
						myCache.addLast(new Buffer(blockID, Constants.BLOCK_SIZE));
						foundBlock = true;
						buffer = myCache.peekLast();
						if(buffer.getBlockID() != blockID)
						{
							System.out.println("Error in cache2: Requested block id " + blockID + " and got block with ID " + buffer.getBlockID());
						}
						break;
					}
				}

				//if none of the blocks fit the criterion, wait until a block is freed
				if(!foundBlock)
				{					
					while(findFreeBlock() == -1)
					{
						try
						{
							wait();
						} catch (InterruptedException e)
						{
							System.out.println("There was an error waiting in the getBlock() method at the second wait.");
						}
					}

					//now that we know a block is free, find it, write it back and evict it
					DBuffer removeBuffer = myCache.remove(findFreeBlock());
					myCache.addLast(new Buffer(blockID, Constants.BLOCK_SIZE));
					buffer = myCache.peekLast();
					if(buffer.getBlockID() != blockID)
					{
						System.out.println("Error in cache3: Requested block id " + blockID + " and got block with ID " + buffer.getBlockID());
					}
					//write back the contents of the removed buffer if need be
					if(!removeBuffer.checkClean() && removeBuffer.checkValid())
					{
						removeBuffer.startPush();
					}
				}

			}
		}
		//get the data into this block
		buffer.startFetch();
		buffer.waitClean();
		buffer.BLOCK_HELD = true;

		return buffer;
	}
	
	private int findFreeBlock()
	{
		int index = -1;
		
		for(DBuffer buff : myCache)
		{
			if(!buff.BLOCK_HELD)
			{
				index = myCache.indexOf(buff);
			}
		}
		
		return index;
	}

	@Override
	public synchronized void releaseBlock(DBuffer buf)
	{
		//get the index of the buffer to be released
		int index = myCache.indexOf(buf);
		
		//update the lists to reflect that this block is now the most recently used
		DBuffer buffer = myCache.remove(index);
		myCache.addLast(buffer);
		buffer.BLOCK_HELD = false;
		
		notifyAll();
	}

	@Override
	public synchronized void sync()
	{
		for(DBuffer buffer : myCache)
		{
			//write back the buffer if it is valid but not clean, wait if it is held
			if(!buffer.checkClean() && buffer.checkValid())
			{
				while(myHeldBuffers.get(myCache.indexOf(buffer)))
				{
					try 
					{
						wait();
					} 
					catch (InterruptedException e) 
					{
						System.out.println("There was an error waiting in the sync() method.");
					}
				}
				System.out.println("Syncing Block: "+buffer.getBlockID());
				buffer.startPush();
				buffer.waitClean();
			}
		}
		
	}

}
