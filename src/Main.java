import java.io.IOException;

import virtualdisk.DVirtualDisk;
import virtualdisk.VirtualDisk;
import dblockcache.Buffer;
import dblockcache.Cache;
import dblockcache.DBuffer;


public class Main 
{
	private static Cache myCache;
	private static DVirtualDisk myDisk;
	
	public static void main(String[] args)
	{
		//construct the disk
		try {
			myDisk = DVirtualDisk.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//start the disk execute thread
		DiskThread thread = new DiskThread();
		thread.start();
		
		int test = 1;
	}
}
