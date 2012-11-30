package dfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import common.DFileID;

public class FileSystem extends DFS {

    private ArrayList<Integer> fileIds = new ArrayList<Integer>();
    private HashMap<DFileID, Integer> fileIdMap = new HashMap<DFileID, Integer>();
    
    @Override
    public boolean format() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public DFileID createDFile() {
	int x;
	if (fileIds.size() == 0)
	{
	    x = 0;
	    fileIds.add(x);
	}
	else
	{
	    x = Collections.max(fileIds) + 1;
	    fileIds.add(x);
	}
	
	//include a request to create a file on the disk --> method provided by the disk
	
	DFileID id = new DFileID(x);
	fileIdMap.put(id, x);
	return id;
    }

    @Override
    public void destroyDFile(DFileID dFID) {
	// a request to delete a file on the disk

    }

    @Override
    public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
	
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
	// TODO Auto-generated method stub
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
