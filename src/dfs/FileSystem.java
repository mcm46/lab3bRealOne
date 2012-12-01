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
	
	
	return 0;
    }

    @Override
    public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
	Cache c = Cache.getInstance();
	int x = (dFID.getIntId() % 4) + 1;
	DBuffer b = c.getBlock(x);
	b.startFetch();
	b.waitClean();
	byte [] dummyBuf = new byte [Constants.BLOCK_SIZE];
	int startDummyRead = 0;
	b.read(dummyBuf, startDummyRead, Constants.BLOCK_SIZE);
	int numBlocksToUse = count / Constants.BLOCK_SIZE;
	//DVirtualDisk.myBitmap.put(dFID.getIntId(), true);
	int actualINodeStart = dummyBuf[dFID.getIntId() - 4*(x-1)];
	byte [] changedHasFile = new byte [4];
	byte [] changedId = new byte [4];
	byte [] changedSize = new byte [4];
	changedHasFile = toBytes(1);
	changedId = toBytes(dFID.getIntId());
	changedSize = toBytes(count);
	
	
	
	
	
	return 0;
    }

    @Override
    public int sizeDFile(DFileID dFID) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public List<DFileID> listAllDFiles() {
	// TODO Auto-generated method stub
	return null;
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

}
