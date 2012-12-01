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
	public synchronized DBuffer getBlock(int blockID)
	{
		DBuffer buffer = null;
		boolean foundBlock = false;


		//find the block
		for(int i = 0; i < myCacheSize; i++)
		{
			if(myCache.get(i).getBlockID() == blockID)
			{
				//wait until this block is no longer held
				while(myHeldBuffers.get(i))
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
				buffer = myCache.remove(i);
				myCache.addLast(buffer);
				myHeldBuffers.remove(i);
				myHeldBuffers.addLast(true);
				foundBlock = true;
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
				if(!myCache.get(i).isBusy() && !myHeldBuffers.get(i))
				{
					//write back the buffer if it is valid and not clean
					if(!myCache.get(i).checkClean() && myCache.get(i).checkValid())
					{
						myCache.get(i).startPush();
					}


					//evict the buffer and create a new one for this request
					myCache.remove(i);
					myHeldBuffers.remove(i);
					myCache.addLast(new Buffer(blockID, Constants.BLOCK_SIZE));
					myHeldBuffers.addLast(true);
					foundBlock = true;
					break;
				}
			}

			//if none of the blocks fit the criterion, wait until a block is freed
			if(!foundBlock)
			{
				while(myHeldBuffers.indexOf(false) != -1)
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
				int index = myHeldBuffers.indexOf(false);
				myHeldBuffers.remove(index);
				buffer = myCache.remove(index);
				myCache.addLast(new Buffer(blockID, Constants.BLOCK_SIZE));
				myHeldBuffers.addLast(true);

				//write back the contents of the removed buffer if need be
				if(!buffer.checkClean() && buffer.checkValid())
				{
					buffer.startPush();
				}
			}

			return myCache.peekLast();
		}
	}

	@Override
	public synchronized void releaseBlock(DBuffer buf)
	{
		//get the index of the buffer to be released
		int index = myCache.indexOf(buf);
		
		//update the lists to reflect that this block is now the most recently used
		myHeldBuffers.remove(index);
		myHeldBuffers.addLast(false);
		DBuffer buffer = myCache.remove(index);
		myCache.addLast(buffer);
		
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
				
				buffer.startPush();
				buffer.waitClean();
			}
		}
		
	}

}
