/**
 * @(#) BDSycService.java Created on Sep 9, 2015
 *
 * 
 */
package com.yuncore.bdsync;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.yuncore.bdsync.util.Log;

/**
 * The class <code>BDSycService</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class BDSycService extends ShutDownBDSyncService {

	private static final String TAG = "BDSycService";

	public BDSyncServiceListener listener;

	public BDSycService(BDSyncServiceListener listener) {
		this.listener = listener;
	}

	public synchronized void start() {
		final int port = Integer.parseInt(System.getProperty(SERVICE_PORT,
				"18081"));
		try {

			final ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
			if (listener != null) {
				listener.onStart();
			}

			final Socket accept = serverSocket.accept();
			Log.w(TAG, "request shutdown service");
			if (null != listener) {
				listener.onStop();
			}

			accept.getOutputStream().write("1".getBytes("UTF-8"));
			accept.getOutputStream().flush();

			accept.close();
			Thread.sleep(1000);
			serverSocket.close();
			Log.w(TAG, "service exit");
			System.exit(0);
		} catch (Exception e) {
			System.exit(1);
		}
	}

	/**
	 * @return the listener
	 */
	public BDSyncServiceListener getBDSyncServiceListener() {
		return listener;
	}

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setBDSyncServiceListener(BDSyncServiceListener listener) {
		this.listener = listener;
	}

	/**
	 * 当收到请求关闭程序时 The class <code>BDSyncServiceListener</code>
	 * 
	 * @author Feng OuYang
	 * @version 1.0
	 */
	public interface BDSyncServiceListener {

		void onStart();

		void onStop();

	}

}
