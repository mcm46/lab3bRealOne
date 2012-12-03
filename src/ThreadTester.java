import java.util.Arrays;

import common.Constants;
import common.DFileID;
import dfs.FileSystem;


public class ThreadTester extends Thread
{
	public int myID;
	private FileSystem myFileSystem;
	private int fileSize = Constants.BLOCK_SIZE * 4;
	
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
		
			DFileID fID=myFileSystem.createDFile();
			
			myFileSystem.write(fID, testBuffer, 0, fileSize);
		
		
			myFileSystem.read(fID, test, 0, fileSize);
			
			if(!Arrays.equals(test, testBuffer) || myFileSystem.sizeDFile(fID) != fileSize / Constants.BLOCK_SIZE)
			{
				System.out.println("Failed on file: " + myID + "in thread: " + myID + " with size: " + myFileSystem.sizeDFile(fID));
			}
			else
			{
				System.out.println("Thread: " + myID + " succeeded.");
			}
		
	}
}
