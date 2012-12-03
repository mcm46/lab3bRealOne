package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import common.Constants.DiskOperationType;
import common.Constants;

import dblockcache.DBuffer;
import dfs.FileSystem;

public class DVirtualDisk extends VirtualDisk
{
	private static DVirtualDisk mySingleton;
	public static Map<Integer,Boolean> myBitmap;
	private static int iNodeBlocks;
	public static int inodeSize=212;
	public static int iNodesPerBlock;
	
	private ConcurrentLinkedQueue<DBuffer> buffers;
	private ConcurrentLinkedQueue<DiskOperationType> operations; 
	
	/*
	 * If there is not an instance of the virtual disk, create a new one. If there is one, return it.
	 */
	public static synchronized DVirtualDisk getInstance() throws IOException
	{

		if(mySingleton == null)
		{
			mySingleton = new DVirtualDisk(true);
		}
		
		return mySingleton;
	}
	
	/*
	 * Populate the bitmap with what blocks are free or taken. False is free, true is filled
	 */
	private void populateBitmap()
	{
		myBitmap= new TreeMap<Integer,Boolean>();
		for(int i=iNodeBlocks+1;i<Constants.NUM_OF_BLOCKS;i++)
		{
			myBitmap.put(i,false);
		}
		for(int i=Constants.BLOCK_SIZE;i<iNodeBlocks*Constants.BLOCK_SIZE+Constants.BLOCK_SIZE;i+=Constants.BLOCK_SIZE)
		{
			for (int j=0;j<iNodesPerBlock*inodeSize;j+=inodeSize)
			{
				for (int k=0;k<inodeSize;k+=4)
				{
					//Set new empty byte array for reading
					byte[] b = new byte[4];
					try {
						//Get the next 4 bytes from the file
						int seekLen = i+j+k;
						_file.seek(seekLen);
						_file.read(b, 0, 4);
					} catch (IOException e) {
						e.printStackTrace();
					}
					//Set 'id' to the int representation of those bytes
					int id=byteArrayToInt(b);
					switch(k)
					{
						case(0):
						{
							continue;
						}
						case(4):
						{
							if (id!=0)
							{
								FileSystem fs= FileSystem.getInstance();
								fs.availFileId.put(id,true);
							}
							break;
						}
						case(8):
						{
							continue;
						}
						default:
						{
							if(id!=0)
							{
								myBitmap.put(id, true);
							}
							break;
						}
					}
					
				}
			}
			
		}
		System.out.print(myBitmap);
	}
	
	//Helper function
	private static int byteArrayToInt(byte[] b) 
	{
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
	
	private DVirtualDisk() throws FileNotFoundException, IOException
	{
		super();
		buffers = new ConcurrentLinkedQueue<DBuffer>();
		operations = new ConcurrentLinkedQueue<DiskOperationType>();
		iNodesPerBlock= Constants.BLOCK_SIZE/inodeSize;
		iNodeBlocks=Constants.MAX_FILES/iNodesPerBlock;
		populateBitmap();
	}
	
	private DVirtualDisk(String volName, boolean format) throws IOException
	{
		super(volName, format);
		buffers = new ConcurrentLinkedQueue<DBuffer>();
		operations = new ConcurrentLinkedQueue<DiskOperationType>();
		iNodesPerBlock= Constants.BLOCK_SIZE/inodeSize;
		iNodeBlocks=Constants.MAX_FILES/iNodesPerBlock;
		populateBitmap();
	}
	
	private DVirtualDisk(boolean format) throws IOException
	{
		super(format);
		buffers = new ConcurrentLinkedQueue<DBuffer>();
		operations = new ConcurrentLinkedQueue<DiskOperationType>();
		iNodesPerBlock= Constants.BLOCK_SIZE/inodeSize;
		iNodeBlocks=Constants.MAX_FILES/iNodesPerBlock;
		populateBitmap();
	}
	
	public void executeRequests()
	{
		while(true)
		{
			if (buffers.isEmpty())
			{
				continue;
			}
			DBuffer buf=buffers.poll();
			DiskOperationType operation= operations.poll();
			if (operation==DiskOperationType.WRITE)
			{
				try {
					writeBlock(buf);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
			{
				try {
					readBlock(buf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			buf.ioComplete();
		}

	}

	
	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException
	{
		buffers.add(buf);
		operations.add(operation);
	}
	

}
