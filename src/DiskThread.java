import java.io.IOException;

import virtualdisk.DVirtualDisk;


public class DiskThread extends Thread 
{
	public void run()
	{
		try {
			DVirtualDisk.getInstance().executeRequests();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
