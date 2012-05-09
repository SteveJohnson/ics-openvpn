package de.blinkt.openvpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;

import android.util.Log;

public class OpenVPNThread implements Runnable {
	private static final String TAG = "OpenVPN";
	private OpenVpnService mService;
	private String[] mArgv;
	private Process mProcess;

	public OpenVPNThread(OpenVpnService service,String[] argv)
	{
		mService = service;
		mArgv = argv;
	}
	
	public void stopProcess() {
		mProcess.destroy();
	}

	@Override
	public void run() {
		try {
			Log.i(TAG, "Starting o");


			OpenVPN.setCallback(mService);


			// We try to create the tunnel for several times. The better way
			// is to work with ConnectivityManager, such as trying only when
			// the network is avaiable. Here we just use a counter to keep
			// things simple.
			//for (int attempt = 0; attempt < 10; ++attempt) {
			mService.getHandler().sendEmptyMessage(R.string.connecting);

			// Log argv

			OpenVPN.logMessage(0, "argv:" , Arrays.toString(mArgv));

			startOpenVPNThreadArgs(mArgv);

			
			//}
			Log.i(TAG, "Giving up");
		} catch (Exception e) {
			Log.e(TAG, "Got " + e.toString());
		} finally {
			try {
				///    mInterface.close();
			} catch (Exception e) {
				// ignore	
			}
			//mInterface = null;


			mService.getHandler().sendEmptyMessage(R.string.disconnected);
			// Not a good place to do it, but will do
			OpenVPN.logBuilderConfig(null);
			Log.i(TAG, "Exiting");
		}
	}

	private void startOpenVPNThreadArgs(String[] argv) {
		LinkedList<String> argvlist = new LinkedList<String>();
		
		for(String arg:argv)
			argvlist.add(arg);
	
		ProcessBuilder pb = new ProcessBuilder(argvlist);
		// Hack O rama
		pb.environment().put("LD_LIBRARY_PATH", "/data/data/de.blinkt.openvpn/lib");
		pb.redirectErrorStream(true);
		try {
			mProcess = pb.start();
			// Close the output, since we don't need it
			mProcess.getOutputStream().close();
			InputStream in = mProcess.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			
			while(true) {
				String logline = br.readLine();
				if(logline==null)
					return;
				OpenVPN.logMessage(0, "P:", logline);
			}
		
		
		} catch (IOException e) {
			e.printStackTrace();
			stopProcess();
		}
		
		
	}
}
