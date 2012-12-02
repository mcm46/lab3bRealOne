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
		
		Buffer test = new Buffer(2, Constants.BLOCK_SIZE);
		
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
		
		myFileSystem.write(new DFileID(2), testBuffer, 0, Constants.BLOCK_SIZE * 4);
		
		int testSize = myFileSystem.sizeDFile(new DFileID(2));
		
		byte[] testBufferRead = new byte[Constants.BLOCK_SIZE * 4];
		
		myFileSystem.read(new DFileID(2), testBufferRead, 0, Constants.BLOCK_SIZE * 4);
//		//construct the disk
//		try {
//			myDisk = DVirtualDisk.getInstance();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		//start the disk execute thread
//		DiskThread thread = new DiskThread();
//		thread.start();
//		
//		Buffer test = new Buffer(2, Constants.BLOCK_SIZE);
//		
//		test.startFetch();
//		test.waitClean();
//		
//		byte[] test2 = new byte[Constants.BLOCK_SIZE];
//		
//		byte[] write = new byte[Constants.BLOCK_SIZE];
//		
//		for(int i = 0; i < write.length; i++)
//		{
//			write[i] = -1;
//		}
//		
//		test.write(write, 0, Constants.BLOCK_SIZE);
//		
//		test.startPush();
//		
//		test.waitClean();
//		
//		Buffer test3 = new Buffer(2, Constants.BLOCK_SIZE);
//		
//		test3.startFetch();
//		test3.waitClean();
//		
//		test3.read(test2, 0, Constants.BLOCK_SIZE);
//		
//		for(int i = 0; i < test2.length; i++)
//		{
//			System.out.print(test2[i]);
//		}
	}
}
