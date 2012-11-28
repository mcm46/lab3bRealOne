package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants.DiskOperationType;

import dblockcache.DBuffer;

public class DVirtualDisk extends VirtualDisk
{
	private static DVirtualDisk mySingleton;
	
	public static DVirtualDisk getInstance() throws IOException
	{
		if(mySingleton == null)
		{
			mySingleton = new DVirtualDisk();
		}
		
		return mySingleton;
	}

	private DVirtualDisk() throws FileNotFoundException, IOException
	{
		super();
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
		
	}

}
