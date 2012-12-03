import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
<<<<<<< HEAD
import java.util.Map;
=======

>>>>>>> branch 'master' of https://github.com/mcm46/lab3bRealOne.git

import common.Constants;
import common.DFileID;
import common.Constants.DiskOperationType;

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
		
		myFileSystem.write(new DFileID(2), testBuffer, 0, Constants.BLOCK_SIZE * 4);
		
		//int testSize = myFileSystem.sizeDFile(new DFileID(3));
		Buffer test2 = new Buffer(3, Constants.BLOCK_SIZE);
		
		byte[] testBuffer2 = new byte[Constants.BLOCK_SIZE * 4];
		
		byte counter2 = 0;
		for(int i = 0; i < Constants.BLOCK_SIZE * 4; i++)
		{
			testBuffer[i] = counter;
			
			counter2++;
			
			if(counter2 > 255)
			{
				counter2 = 0;
			}
		}
		
		myFileSystem.write(new DFileID(3), testBuffer2, 0, Constants.BLOCK_SIZE * 4);
		
		//int testSize3 = myFileSystem.sizeDFile(new DFileID(3));
		
		//byte[] testBufferRead = new byte[Constants.BLOCK_SIZE * 4];
		
		//myFileSystem.read(new DFileID(2), testBufferRead, 0, Constants.BLOCK_SIZE * 4);
		
		byte[] test = new byte[Constants.BLOCK_SIZE * 4];
//		
//		for(int i = 0; i < test.length; i++)
//		{
//			if(test[i] != 0)
//			{
//				byte b = test[i];
//			}
//		}
//		
		myFileSystem.read(new DFileID(2), test, 0, Constants.BLOCK_SIZE * 4);
		
		byte[] testRead = new byte[Constants.BLOCK_SIZE*4];
		myFileSystem.read(new DFileID(3), testRead, 0, Constants.BLOCK_SIZE * 4);
		
		myCache.sync();
		FileInputStream fis;
		byte[] b=new byte[Constants.BLOCK_SIZE*Constants.NUM_OF_BLOCKS];
		byte[] c = new byte[Constants.BLOCK_SIZE*8];
		myFileSystem.read(new DFileID(2),c,0,Constants.BLOCK_SIZE*4);
		myFileSystem.read(new DFileID(3), c, Constants.BLOCK_SIZE*4, Constants.BLOCK_SIZE * 4);
		try {
			fis= new FileInputStream(new File("DSTORE.dat"));
			try {
				fis.read(b);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i=132096;i<140288;i++)
		{
			if (b[i]!=0)
			{
				System.out.println(i+" "+b[i]);
			}
			
		}
		System.out.println("done");
		

		}
	}

