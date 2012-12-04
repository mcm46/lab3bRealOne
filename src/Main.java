import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import virtualdisk.DVirtualDisk;

import common.Constants;
import common.DFileID;
import common.Constants.DiskOperationType;

import dblockcache.Cache;
import dfs.FileSystem;


public class Main 
{
	private static Cache myCache;
	private static DVirtualDisk myDisk;
	private static FileSystem myFileSystem;
	private static ArrayList<Thread> myThreads;
	private static int numThreads=100;
	
	private static void addThread(Thread t)
	{
		myThreads.add(t);
	}
	
	public static void main(String[] args)
	{
		myThreads=new ArrayList<Thread>();
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
		
		for(int i = 0; i < numThreads; i++)
		{
			Thread t = new ThreadTester(i);
			addThread(t);
			t.start();
		}
		
		

//		//Buffer test = new Buffer(2, Constants.BLOCK_SIZE);
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
//		
//
//		
//		byte[] test = new byte[Constants.BLOCK_SIZE * 4];
////		
////		for(int i = 0; i < 512; i++)
////		{
////			myFileSystem.write(new DFileID(i), testBuffer, 0, Constants.BLOCK_SIZE * 4);
////		}
////
////		
////		for(int i = 0; i < 512; i++)
////		{
////			myFileSystem.read(new DFileID(i), test, 0, Constants.BLOCK_SIZE * 4);
////			
////			if(!Arrays.equals(test, testBuffer))
////			{
////				System.out.println("Failed on file: " + i);
////			}
////		}
//
//		
//		myFileSystem.write(new DFileID(2), testBuffer, 0, Constants.BLOCK_SIZE * 4);
//		myFileSystem.write(new DFileID(2), testBuffer, 0, Constants.BLOCK_SIZE * 4);
////		myFileSystem.read(new DFileID(1), test, 0, Constants.BLOCK_SIZE * 4);
////		myFileSystem.read(new DFileID(150), test, 0, Constants.BLOCK_SIZE * 4);

		for (int i=0;i<numThreads;i++)
		{
			try {
				myThreads.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		myCache.sync();
		
			
		
		System.out.println("done");
		

	}
}

