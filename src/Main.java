import dblockcache.Buffer;
import dblockcache.Cache;
import dblockcache.DBuffer;


public class Main 
{
	private static Cache myCache;
	
	public static void main(String[] args)
	{
		myCache = Cache.getInstance();
		
		DBuffer test = myCache.getBlock(10);
	}
}
