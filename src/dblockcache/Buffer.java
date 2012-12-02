package dblockcache;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import common.Constants;

import virtualdisk.DVirtualDisk;

public class Buffer extends DBuffer
{
	//is the data in the Buffer valid?
	private boolean myValid = false;
	
	//has the buffer been modified (does it need to be written back?)
	private boolean myClean = false;
	
	//is the buffer busy (is it being used by someone?)
	private boolean myBusy = false;
	
	//byte array containing the data associated with this buffer for retrieval by the disk
	private byte[] myBuffer;
	
	//the ID of the block that this buffer represents
	private int myBlockID;
	
	private final ReentrantReadWriteLock myReentrantLock = new ReentrantReadWriteLock();
	
	//the lock to acquire when reading
	private final Lock readLock = myReentrantLock.readLock();
	
	private final Lock writeLock = myReentrantLock.writeLock();
	
	public Buffer(int blockID, int bufferSize)
	{
		myBlockID = blockID;
		myBuffer = new byte[bufferSize];
	}
	
	
	@Override
	public synchronized void startFetch()
	{		
		//start the read from the disk
		try
		{
			DVirtualDisk.getInstance().startRequest(this,
					Constants.DiskOperationType.READ);
		} 
		catch (IllegalArgumentException e)
		{
			System.out.println("An Illegal Argument was Passed in the startFetch() method.");
		} 
		catch (IOException e)
		{
			System.out.println("I/O Exception: " + e.getLocalizedMessage() + " was caught in the startFetch() method.");
		}
	}

	@Override
	//just call the disk met
	public synchronized void startPush()
	{
		try
		{
			DVirtualDisk.getInstance().startRequest(this, 
					Constants.DiskOperationType.WRITE);
		}
		catch (IllegalArgumentException e)
		{
			System.out.println("An Illegal Argument was Passed in the startPush() method.");
		} 
		catch (IOException e)
		{
			System.out.println("I/O Exception: " + e.getLocalizedMessage() + " was caught in the startPush() method.");
		}
	}

	@Override
	public boolean checkValid()
	{
		return myValid;
	}

	@Override
	//return true if the wait was successful, false otherwise
	public synchronized boolean waitValid()
	{
		while(!myValid)
		{
			try
			{
				wait();
			} 
			catch (InterruptedException e)
			{
				System.out.println("Thread in waitValid() was Interrupted.");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkClean()
	{
		return myClean;
	}

	@Override
	//return true if the wait was successful, false otherwise
	public synchronized boolean waitClean()
	{
		while(!myClean)
		{
			try
			{
				wait();
			} 
			catch (InterruptedException e)
			{
				System.out.println("Thread in waitClean() was Interrupted.");
				return false;
			}
			
		}
		return true;
	}

	@Override
	public boolean isBusy()
	{
		return myBusy;
	}

	@Override
	//really this shouldn't be protected by a mutex unless there is a writer, does this need to be accounted for?
	public int read(byte[] buffer, int startOffset, int count)
	{
		readLock.lock();
		
		try
		{
			for(int i = 0; i < count; i++)
			{
				buffer[startOffset + i] = myBuffer[i];
			}
		}
		catch(Exception e)
		{
			readLock.unlock();
			return -1;
		}
		
		readLock.unlock();
		
		return count;
	}

	@Override
	//update the busy status here, implement a read write lock
	public int write(byte[] buffer, int startOffset, int count)
	{
		writeLock.lock();
		
		try
		{
			for(int i = 0; i < count; i++)
			{
				myBuffer[i] = buffer[startOffset + i];
			}

			myClean = false;
			myValid = true;
		}
		catch(Exception e)
		{
			myClean = false;
			myValid = false;
			writeLock.unlock();
			return -1;
		}
		
		writeLock.unlock();
		
		return count;
	}

	@Override
	public synchronized void ioComplete()
	{
		myValid = true;
		myClean = true;
		notifyAll();
	}

	@Override
	public int getBlockID()
	{
		return myBlockID;
	}

	@Override
	public byte[] getBuffer()
	{
		return myBuffer;
	}

}
