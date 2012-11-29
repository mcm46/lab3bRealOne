package dblockcache;

import java.io.IOException;

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
	
	//the number of readers waiting
	private int myReadersWaiting;
	
	//the total number of participants, -1 if one of them is a writer
	private int myParticipants;
	
	//a condition variable for writers
	private Object myWriteCV;
	
	//a condition variable for readers
	private Object myReadCV;
	
	
	public Buffer(int blockID, int bufferSize)
	{
		myBlockID = blockID;
		myBuffer = new byte[bufferSize];
		myWriteCV = new Object();
		myReadCV = new Object();
	}
	
	private void acquireWrite()
	{
		synchronized(myWriteCV)
		{
			while(myParticipants != 0)	//if there are writers or readers
			{
				try
				{
					myWriteCV.wait();
				}
				catch (InterruptedException e)
				{
					System.out.println("Error waiting in the acquireWait() method.");
				}

				myBusy = true;
				myParticipants = -1;
			}
		}
	}
	
	private void acquireRead()
	{
		synchronized(myReadCV)
		{
			while(myParticipants < 0)	//if there is a writer
			{
				myReadersWaiting++;
				try
				{
					myReadCV.wait();
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myReadersWaiting--;

				myParticipants++;
			}
		}
	}
	
	private void releaseWrite()
	{
		synchronized(myWriteCV)
		{
			myParticipants = 0;
			myBusy = false;
			
			if(myReadersWaiting > 0)
			{
				myReadCV.notifyAll();
			}
			else
			{
				myWriteCV.notify();
			}
		}
	}
	
	private void releaseRead()
	{
		synchronized(myReadCV)
		{
			myParticipants--;
			
			if(myParticipants == 0)
			{
				myWriteCV.notify();
			}
		}
	}
	
	//MAY ONLY NEED ONE CV?
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

		//is it still busy until the fetch finishes?
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
		acquireRead();
		
		try
		{
			for(int i = 0; i < count; i++)
			{
				buffer[startOffset + i] = myBuffer[i];
			}
		}
		catch(Exception e)
		{
			releaseRead();
			return -1;
		}
		
		releaseRead();
		
		return count;
	}

	@Override
	//update the busy status here, implement a read write lock
	public int write(byte[] buffer, int startOffset, int count)
	{
		acquireWrite();
		
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
			releaseWrite();
			return -1;
		}
		
		releaseWrite();
		
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
