package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import common.Constants.DiskOperationType;
import common.Constants;

import dblockcache.DBuffer;

public class DVirtualDisk extends VirtualDisk
{
	private static DVirtualDisk mySingleton;
	public static Map<Integer,Boolean> myBitmap;
	private static int inodeBlocks;
	private static int inodeSize=208;
	public static int iNodesPerBlock;
	
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
		for(int i=inodeBlocks+1;i<Constants.NUM_OF_BLOCKS;i++)
		{
			myBitmap.put(i,true);
		}
		
		for(int i=Constants.BLOCK_SIZE;i<inodeBlocks*Constants.BLOCK_SIZE+Constants.BLOCK_SIZE;i+=Constants.BLOCK_SIZE)
		{
			for (int j=0;j<iNodesPerBlock*inodeSize;j+=inodeSize)
			{
				for (int k=0;k<inodeSize;k+=4)
				{
					int block_id;
					int f_id;
					byte[] b = new byte[4];
					try {
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
							f_id=id;
							
							break;
						}
						case(4):
						{
							continue;
							break;
						}
						default:
						{
							block_id=id;
							break;
						}
					}
					
				}
			}
			
			System.out.println(f_id+" : "+myBitmap.get(f_id));
		}
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
		
		populateBitmap();
	}
	
	private DVirtualDisk(String volName, boolean format) throws IOException
	{
		super(volName, format);
	}
	
	private DVirtualDisk(boolean format) throws IOException
	{
		super(format);
	}
	


	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException
	{
		if(operation==DiskOperationType.WRITE)
		{
			mySingleton.writeBlock(buf);
		}
		else
		{
			mySingleton.readBlock(buf);
		}
		buf.ioComplete();
	}

}
