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
	private static int inodeSize= 256;
	
	public static DVirtualDisk getInstance() throws IOException
	{
		if(mySingleton == null)
		{
			mySingleton = new DVirtualDisk();
			
		}
		myBitmap= new HashMap<Integer,Boolean>();
		for(int i=Constants.BLOCK_SIZE;i<Constants.MAX_FILES;i+=inodeSize)
		{
			byte[] fileBytes= new byte[32];
			mySingleton._file.read(fileBytes,0,32);
			int fileId=byteArrayToInt(fileBytes);
			myBitmap.put(fileId, fileId!=0);
		}
		return mySingleton;
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
