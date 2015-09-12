/**
 * @(#) ShutDownBDSyncService.java Created on Sep 9, 2015
 *
 * 
 */
package com.yuncore.bdsync;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * The class <code>ShutDownBDSyncService</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class ShutDownBDSyncService {

	static final String SERVICE_PORT = "service_port";

	public synchronized void start() {
		final int port = Integer.parseInt(System.getProperty(SERVICE_PORT,
				"18081"));
		try {
			final Socket socket = new Socket();
			socket.setSoTimeout(5000);
			socket.connect(new InetSocketAddress("127.0.0.1", port));
			final InputStream inputStream = socket.getInputStream();
			while (true) {
				final byte[] buffer = new byte[1];
				if (inputStream.read(buffer) > 0 && buffer[0] != 0) {
					System.out.println("shutdown success");
					break;
				}
			}
			socket.close();
			System.exit(0);
		} catch (SocketTimeoutException e) {
			System.exit(0);
		} catch (IOException e) {
			System.exit(1);
		}

	}

}
