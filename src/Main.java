import java.io.IOException;

import common.Constants;
import common.DFileID;

import virtualdisk.DVirtualDisk;
import virtualdisk.VirtualDisk;
import dblockcache.Buffer;
import dblockcache.Cache;
import dblockcache.DBuffer;
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
		
		byte[] test = new byte[Constants.BLOCK_SIZE * 4];
		
		myFileSystem.read(new DFileID(2), test, 0, Constants.BLOCK_SIZE * 4);
		
//		Buffer test = new Buffer(2, Constants.BLOCK_SIZE);
//		
//		byte[] testBuffer = new byte[Constants.BLOCK_SIZE * 4];
//		
//		byte counter = 0;
//		for(int i = 0; i < Constants.BLOCK_SIZE * 4; i++)
//		{
//			testBuffer[i] = counter;
//			
//			counter++;
//			
//			if(counter > 255)
//			{
//				counter = 0;
//			}
//		}
//		
//		myFileSystem.write(new DFileID(2), testBuffer, 0, Constants.BLOCK_SIZE * 4);
//		
//		int testSize = myFileSystem.sizeDFile(new DFileID(2));
//		
//		byte[] testBufferRead = new byte[Constants.BLOCK_SIZE * 4];
//		
//		myFileSystem.read(new DFileID(2), testBufferRead, 0, Constants.BLOCK_SIZE * 4);
//		
//		myCache.sync();
	}
}
