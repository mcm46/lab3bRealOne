import java.util.Arrays;

import common.Constants;
import common.DFileID;
import dfs.FileSystem;


public class ThreadTester extends Thread
{
	public int myID;
	private FileSystem myFileSystem;
	private int fileSize = Constants.BLOCK_SIZE * 10;
	
	public ThreadTester(int id)
	{
		myID = id;
		myFileSystem = FileSystem.getInstance();
	}
	public void run()
	{
		byte[] testBuffer = new byte[fileSize];
		
		byte counter = 0;
		for(int i = 0; i < fileSize; i++)
		{
			testBuffer[i] = (byte)myID;
//			
//			counter++;
//			
//			if(counter > 255)
//			{
//				counter = 0;
//			}
		}
		
		

		
			byte[] test = new byte[fileSize];
		
		
			myFileSystem.write(new DFileID(myID), testBuffer, 0, fileSize);
		
		
			myFileSystem.read(new DFileID(myID), test, 0, fileSize);
			
			if(!Arrays.equals(test, testBuffer) || myFileSystem.sizeDFile(new DFileID(myID)) != fileSize / Constants.BLOCK_SIZE)
			{
				System.out.println("Failed on file: " + myID + "in thread: " + myID + " with size: " + myFileSystem.sizeDFile(new DFileID(myID)));
			}
			else
			{
				System.out.println("Thread: " + myID + " succeeded.");
			}
		
	}
}
