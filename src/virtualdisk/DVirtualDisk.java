package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
	private static int inodeSize=212;
	public static int iNodesPerBlock;
	
	private ConcurrentLinkedQueue<DBuffer> buffers;
	private ConcurrentLinkedQueue<DiskOperationType> operations; 
		
	public static DVirtualDisk getInstance() throws IOException
	{

		if(mySingleton == null)
		{
			mySingleton = new DVirtualDisk();
			
		}
		
		return mySingleton;
	}
	

	private void populateBitmap()
	{
		myBitmap= new HashMap<Integer,Boolean>();
		for(int i=iNodeBlocks+1;i<Constants.NUM_OF_BLOCKS;i++)
		{
			myBitmap.put(i,false);
		}
		for(int i=Constants.BLOCK_SIZE;i<iNodeBlocks*Constants.BLOCK_SIZE+Constants.BLOCK_SIZE;i+=Constants.BLOCK_SIZE)
		{
			for (int j=0;j<iNodesPerBlock*inodeSize;j+=inodeSize)
			{
				boolean setFileUsed=false;
				for (int k=0;k<inodeSize;k+=4)
				{

					byte[] b = new byte[4];
					try {
						System.out.println(i+j+k);
						_file.read(b, i+j+k, 4);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int id=byteArrayToInt(b);
					
					switch(k)
					{
						case(0):
						{
							if(id!=0)
							{
								setFileUsed=true;
							}
							break;
						}
						case(4):
						{
							if(setFileUsed)
							{
								FileSystem fs= FileSystem.getInstance();
								fs.availFileId.put(id,true);
								setFileUsed=false;
							}
							break;
						}
						case(12):
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
		iNodesPerBlock= Constants.BLOCK_SIZE/inodeSize;
		iNodeBlocks=Constants.MAX_FILES/iNodesPerBlock;
		populateBitmap();
		executeRequests();
	}
	
	private DVirtualDisk(String volName, boolean format) throws IOException
	{
		super(volName, format);
	}
	
	private DVirtualDisk(boolean format) throws IOException
	{
		super(format);
	}
	
	private void executeRequests()
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
