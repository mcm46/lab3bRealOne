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
		//myCache = Cache.getInstance();
		
		//DBuffer test = myCache.getBlock(10);
		try {
			myDisk=DVirtualDisk.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}
}
