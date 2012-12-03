import java.io.IOException;

import virtualdisk.DVirtualDisk;

import common.Constants;
import common.DFileID;

import dblockcache.Cache;
import dfs.FileSystem;


public class Main 
{
	private static Cache myCache;
	private static DVirtualDisk myDisk;
	private static FileSystem myFileSystem;
	
	public static void main(String[] args)
	{
		try {
			myDisk = DVirtualDisk.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		myFileSystem = FileSystem.getInstance();
		
		myCache = Cache.getInstance();
		
		DiskThread thread = new DiskThread();
		
		thread.start();
		

		//Buffer test = new Buffer(2, Constants.BLOCK_SIZE);
		
		byte[] testBuffer = new byte[Constants.BLOCK_SIZE * 4];
		
		byte counter = 0;
		for(int i = 0; i < Constants.BLOCK_SIZE * 4; i++)
		{
			testBuffer[i] = counter;
			
			counter++;
			
			if(counter > 255)
			{
				counter = 0;
			}
		}
		
		//myFileSystem.write(new DFileID(2), testBuffer, 0, Constants.BLOCK_SIZE * 4);
		
		
		

		
		byte[] test = new byte[Constants.BLOCK_SIZE * 4];
		
		myFileSystem.read(new DFileID(2), test, 0, Constants.BLOCK_SIZE * 4);

		
		myCache.sync();
		
			
		
		System.out.println("done");
		

		
	}
}

