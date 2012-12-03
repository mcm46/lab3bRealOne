package dfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import virtualdisk.DVirtualDisk;

import common.Constants;
import common.DFileID;
import dblockcache.Cache;
import dblockcache.DBuffer;

public class FileSystem extends DFS {

    private static FileSystem mySingleton;
    public HashMap<Integer, Boolean> availFileId = new HashMap<Integer, Boolean>();
    private ArrayList<DFileID> allFiles = new ArrayList<DFileID>();

    public FileSystem()
    {
	for (int x=0; x<Constants.MAX_FILES; x++)
	{
	    availFileId.put(x, false);
	}
    }
    
    
    public synchronized static FileSystem getInstance()
    {
	if (mySingleton == null)
	{
	    mySingleton = new FileSystem();
	}

	return mySingleton;
    }


    @Override
    public boolean format() 
    {
    	return DVirtualDisk.format();
    }

    @Override
    public synchronized DFileID createDFile() {
	for (int x=0; x<Constants.MAX_FILES; x++)
	{
	    if (availFileId.get(x) == false)
	    {
		DFileID id = new DFileID(x);
		availFileId.put(x, true);
		allFiles.add(id);
		return id;
	    }
	}
	return new DFileID(-1);
    }

    @Override
    public synchronized void destroyDFile(DFileID dFID) {
	int x = dFID.getIntId();
	availFileId.put(x, false);
	allFiles.remove(dFID);
    }

    @Override
    public int read(DFileID dFID, byte[] buffer, int startOffset, int count) 
    {
    	try
    	{
    		//get the cache instance
    		Cache c = Cache.getInstance();

    		//get the block number of the inode
    		int iNodeBlock = (dFID.getIntId() / DVirtualDisk.iNodesPerBlock) + 1;

    		//get that block
    		DBuffer inodeBuffer = c.getBlock(iNodeBlock);

    		//buffer to hold the inode data
    		byte [] inodeBlockData = new byte [Constants.BLOCK_SIZE];

    		//read the inode data into the buffer
    		inodeBuffer.read(inodeBlockData, 0, Constants.BLOCK_SIZE);

    		//figure out how many blocks we are going to have to read
    		int numBlocksToUse = (int)Math.ceil((double)count / Constants.BLOCK_SIZE);

    		//find the start of the inode within the inode block
    		int actualINodeStart = (dFID.getIntId() - DVirtualDisk.iNodesPerBlock * (iNodeBlock - 1)) * DVirtualDisk.inodeSize;


    		for (int y=0; y<numBlocksToUse; y++)
    		{
    			//create an buffer to hold the int for the next block to be read
    			byte [] pointerArray = new byte[4];

    			//copy data into the buffer
    			pointerArray = Arrays.copyOfRange(inodeBlockData, actualINodeStart + 12 + 4 * y, actualINodeStart + 16 + 4 * y);

    			//convert the data into a pointer for the next block to read
    			int pointer = byteArrayToInt(pointerArray);

    			//get the block with the requested data
    			DBuffer readBuffer = c.getBlock(pointer);

    			//figure out how much to read, read that data into the passed buffer
    			if(buffer.length - (startOffset + Constants.BLOCK_SIZE * y) >= Constants.BLOCK_SIZE)
    			{
    				readBuffer.read(buffer, startOffset + Constants.BLOCK_SIZE * y, Constants.BLOCK_SIZE);
    			}
    			else
    			{
    				readBuffer.read(buffer, startOffset + Constants.BLOCK_SIZE * y, buffer.length - (startOffset + Constants.BLOCK_SIZE * y));
    			}
    			//release the current block we are reading from
    			c.releaseBlock(readBuffer);
    		}
    		
    		//release the inode block, return the number of bytes written, if there is 
    		//an exception catch it and return -1
    		c.releaseBlock(inodeBuffer);
    		return count;
    	}
    	catch(Exception e)
    	{
    		return -1;
    	}
    }

    @Override
    public synchronized int write(DFileID dFID, byte[] buffer, int startOffset, int count) 
    {
    	try
    	{
    		//get the cache instance
    		Cache c = Cache.getInstance();

    		//figure out what block contains the inode for this file
    		int iNodeBlock = (dFID.getIntId() / DVirtualDisk.iNodesPerBlock) + 1;

    		//get that block
    		DBuffer inodeBuffer = c.getBlock(iNodeBlock);

    		//create a buffer to fill up and read in the inode data
    		byte [] inodeBlockData = new byte [Constants.BLOCK_SIZE];
    		inodeBuffer.read(inodeBlockData, 0, Constants.BLOCK_SIZE);

    		//figure out how many blocks are required for the write
    		int numBlocksToUse = (int)Math.ceil((double)count / Constants.BLOCK_SIZE);

    		//find the actual start of the inode data for this particular file
    		int actualINodeStart = (dFID.getIntId() - DVirtualDisk.iNodesPerBlock * (iNodeBlock - 1)) * DVirtualDisk.inodeSize;

    		//buffers for the integer start of the inode, the inode is structured with a header of the form |validFile|fileID|fileSize|
    		byte [] changedHasFile = new byte [4];
    		byte [] changedId = new byte [4];
    		byte [] changedSize = new byte [4];

    		//load the buffers with the integer values
    		changedHasFile = toBytes(1);
    		changedId = toBytes(dFID.getIntId());
    		changedSize = toBytes(numBlocksToUse);
    		
    		
    		//checking if there was a pointer there already, if so convert it to int
    		//and set needToOverwrite to true
    		boolean needToOverwrite = false;
    		byte [] changedTestHasFile = new byte [4];
    		changedTestHasFile = Arrays.copyOfRange(inodeBlockData, actualINodeStart,actualINodeStart + 4);
    		int hasFileInt = byteArrayToInt(changedTestHasFile);
	    	ArrayList<Integer> blocksToFree = new ArrayList<Integer>();
    		if (hasFileInt == 1)
    		{
    		    needToOverwrite = true;
    		}
    		
    		
		if (needToOverwrite)
		{
	   		byte [] changedSizeForFreeing = new byte [4];
	   		changedSizeForFreeing = Arrays.copyOfRange(inodeBlockData, actualINodeStart + 8,actualINodeStart + 12);
		    	int sizeForFreeingInt = byteArrayToInt(changedSizeForFreeing);
		    	for (int ee=0; ee<sizeForFreeingInt; ee++)
		    	{
		    	    byte [] numArray = new byte[4];
		    	    for (int ii=0; ii<4; ii++)
		    	    {
		    		numArray[ii] = inodeBlockData[actualINodeStart + 12 + ee * 4 + ii];
		    	    }
		    	    int pointerToFree = byteArrayToInt(numArray);
		    	    blocksToFree.add(pointerToFree);
		    	}
		}

    		//write the data into the inode
    		for (int y=0; y<4; y++)
    		{
    			inodeBlockData[actualINodeStart + y] = changedHasFile[y];
    			inodeBlockData[actualINodeStart + 4 + y] = changedId[y];
    			inodeBlockData[actualINodeStart + 8 + y] = changedSize[y];
    		}

    		//write the data into the blocks, updating the inode along the way to indicate
    		//what blocks a files data occupies
    		for (int z=0; z < numBlocksToUse; z++)
    		{
    			//find a free block
    			for (Integer freeBlockNumber: DVirtualDisk.myBitmap.keySet())
    			{
    				if (DVirtualDisk.myBitmap.get(freeBlockNumber) == false)
    				{
    					//get an empty block to write to
    					DBuffer writeBlock = c.getBlock(freeBlockNumber);

    					//update the disk to indicate that that block is now used
    					DVirtualDisk.myBitmap.put(freeBlockNumber, true);
    					//System.out.println(z + " " + DVirtualDisk.myBitmap);

    					//write the correct part of the data to this buffer, check to see how much is left
    					if(buffer.length - (startOffset + Constants.BLOCK_SIZE * z) >= Constants.BLOCK_SIZE)
    					{
    						writeBlock.write(buffer, startOffset + Constants.BLOCK_SIZE * z, Constants.BLOCK_SIZE);
    					}
    					else
    					{
    						writeBlock.write(buffer, startOffset + Constants.BLOCK_SIZE * z, buffer.length - (startOffset + Constants.BLOCK_SIZE * z));
    					}

    					//create an integer in byte form to update the inode that this block is now part of the file
    					byte [] changePointer = new byte [4];
    					changePointer = toBytes(freeBlockNumber);
    					
    					//write the inode block data
    					for (int yy=0; yy<4; yy++)
    					{
  			    			inodeBlockData[actualINodeStart + 12 + z * 4 + yy] = changePointer[yy];
  					}

    			    		
    					
    					
    					//we're done writing to this block, release it and break out of the loop
    					c.releaseBlock(writeBlock);
    					break;
    				}
    			}
    		}
    		
    		for (Integer blocksToF: blocksToFree)
    		{
    		    DVirtualDisk.myBitmap.put(blocksToF, false);
    		}
    		
    		//we're done writing, write the inode, release the inode and return how many bits are written, if there is an error 
    		//catch it and return -1
    		inodeBuffer.write(inodeBlockData, 0, Constants.BLOCK_SIZE);
    		c.releaseBlock(inodeBuffer);
    		return count;
    	}
    	catch(Exception e)
    	{
    		System.out.println("There was an error writing the file: " + e.getLocalizedMessage());
    		return -1;
    	}
    }

    @Override
    public int sizeDFile(DFileID dFID) 
    {
    	//get the cache instance
    	Cache c = Cache.getInstance();

    	//figure out which block the inode is actually in
    	int iNodeBlock = (dFID.getIntId() / DVirtualDisk.iNodesPerBlock) + 1;

    	//get the buffer corresponding to that block
    	DBuffer inodeBuffer = c.getBlock(iNodeBlock);

    	//byte array corresponding to the data in the inode block
    	byte [] inodeBlockData = new byte [Constants.BLOCK_SIZE];

    	//read the data into the buffer
    	inodeBuffer.read(inodeBlockData, 0, Constants.BLOCK_SIZE);

    	//find the start of the inode
    	int actualINodeStart = (dFID.getIntId() - DVirtualDisk.iNodesPerBlock * (iNodeBlock - 1)) * DVirtualDisk.inodeSize;

    	//get the data for the size integer
    	byte[] sizeArray = Arrays.copyOfRange(inodeBlockData, actualINodeStart + 8, actualINodeStart + 12);

    	//convert that data into an integer
    	int size = byteArrayToInt(sizeArray);	

    	//release the inode
    	c.releaseBlock(inodeBuffer);
    	
    	return size;
    }

    @Override
    public List<DFileID> listAllDFiles() {

	return allFiles;
    }

    private byte[] toBytes(int i)
    {
	byte[] result = new byte[4];

	result[0] = (byte) (i >> 24);
	result[1] = (byte) (i >> 16);
	result[2] = (byte) (i >> 8);
	result[3] = (byte) (i);

	return result;
    }

    private static int byteArrayToInt(byte[] b) 
    {
	int value = 0;
	for (int i = 0; i < 4; i++) {
	    int shift = (4 - 1 - i) * 8;
	    value += (b[i] & 0x000000FF) << shift;
	}
	return value;
    }


    @Override
    public void sync() {
	Cache c = Cache.getInstance();
	c.sync();
    }

}
