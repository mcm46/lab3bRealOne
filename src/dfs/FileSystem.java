package dfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import common.DFileID;
import dblockcache.Cache;
import dblockcache.DBuffer;

public class FileSystem extends DFS {

    private static FileSystem mySingleton;
    public HashMap<Integer, Boolean> availFileId = new HashMap<Integer, Boolean>();
    private HashMap<DFileID, Integer> fileIdToINodeMap = new HashMap<DFileID, Integer>();
    private static int maxFiles = 511;
    
    public FileSystem getInstance()
    {
	if (mySingleton == null)
	{
	    mySingleton = new FileSystem();
	    for (int x=0; x<maxFiles; x++)
	    {
		availFileId.put(x, false);
	    }
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
	for (int x=0; x<maxFiles; x++)
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
	byte [] dummyBuf = 
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

}
