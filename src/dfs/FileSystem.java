package dfs;

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


    public static FileSystem getInstance()
    {
	if (mySingleton == null)
	{
	    mySingleton = new FileSystem();
	}

	return mySingleton;
    }


    @Override
    public boolean format() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public DFileID createDFile() {
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
    public void destroyDFile(DFileID dFID) {
	int x = dFID.getIntId();
	availFileId.put(x, false);
    }

    @Override
    public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {

	Cache c = Cache.getInstance();
	int x = (dFID.getIntId() % DVirtualDisk.iNodesPerBlock) + 1;
	DBuffer b = c.getBlock(x);
	b.startFetch();
	b.waitClean();
	byte [] dummyBuf = new byte [Constants.BLOCK_SIZE];
	int startDummyRead = 0;
	b.read(dummyBuf, startDummyRead, Constants.BLOCK_SIZE);
	int numBlocksToUse = count / Constants.BLOCK_SIZE;
	int actualINodeStart = dummyBuf[dFID.getIntId() -DVirtualDisk.iNodesPerBlock*(x-1)];
	byte [] INodeArray = Arrays.copyOfRange(dummyBuf, actualINodeStart,
		actualINodeStart+(Constants.BLOCK_SIZE)/DVirtualDisk.iNodesPerBlock);
	for (int y=0; y<numBlocksToUse; y++)
	{
	    byte [] pointerArray = new byte[4];
	    pointerArray = Arrays.copyOfRange(INodeArray, 12 + 4*y, 16 + 4*y);
	    int pointer = byteArrayToInt(pointerArray);
	    b = c.getBlock(pointer);
	    b.read(buffer, Constants.BLOCK_SIZE*y, Constants.BLOCK_SIZE);
	}

	c.releaseBlock(b);
	return count;
    }

    @Override
    public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {

	Cache c = Cache.getInstance();
	int x = (dFID.getIntId() % DVirtualDisk.iNodesPerBlock) + 1;
	DBuffer b = c.getBlock(x);
	b.startFetch();
	b.waitClean();
	byte [] dummyBuf = new byte [Constants.BLOCK_SIZE];
	int startDummyRead = 0;
	b.read(dummyBuf, startDummyRead, Constants.BLOCK_SIZE);
	int numBlocksToUse = count / Constants.BLOCK_SIZE;
	int actualINodeStart = dummyBuf[dFID.getIntId() -DVirtualDisk.iNodesPerBlock*(x-1)];
	byte [] changedHasFile = new byte [4];
	byte [] changedId = new byte [4];
	byte [] changedSize = new byte [4];
	changedHasFile = toBytes(1);
	changedId = toBytes(dFID.getIntId());
	changedSize = toBytes(numBlocksToUse);
	for (int y=0; x<4; y++)
	{
	    dummyBuf[actualINodeStart + y] = changedHasFile[y];
	    dummyBuf[actualINodeStart + 4 + y] = changedId[y];
	    dummyBuf[actualINodeStart + 8 + y] = changedSize[y];
	}

	for (int z=0; z<numBlocksToUse; z++)
	{
	    for (Integer xx: DVirtualDisk.myBitmap.keySet())
	    {
		if (DVirtualDisk.myBitmap.get(xx) == false)
		{
		    b = c.getBlock(xx);
		    DVirtualDisk.myBitmap.put(xx, true);
		    b.write(buffer, startOffset + Constants.BLOCK_SIZE*z, Constants.BLOCK_SIZE);
		    byte [] changePointer = new byte [4];
		    changePointer = toBytes(xx);
		    for (int yy=0; yy<4; yy++)
		    {
			dummyBuf[actualINodeStart + 8 + (z+1)*4 + yy] = changePointer[yy];
		    }
		}
		break;
	    }
	}
	c.releaseBlock(b);
	return count;
    }

    @Override
    public int sizeDFile(DFileID dFID) {

	Cache c = Cache.getInstance();
	int x = (dFID.getIntId() % DVirtualDisk.iNodesPerBlock) + 1;
	DBuffer b = c.getBlock(x);
	b.startFetch();
	b.waitClean();
	byte [] dummyBuf = new byte [Constants.BLOCK_SIZE];
	int startDummyRead = 0;
	b.read(dummyBuf, startDummyRead, Constants.BLOCK_SIZE);
	int actualINodeStart = dummyBuf[dFID.getIntId() -DVirtualDisk.iNodesPerBlock*(x-1)];
	byte [] INodeArray = Arrays.copyOfRange(dummyBuf, actualINodeStart, actualINodeStart+Constants.BLOCK_SIZE);
	byte [] sizeArray = Arrays.copyOfRange(INodeArray, 8, 12);
	int size = byteArrayToInt(sizeArray);	

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

}
