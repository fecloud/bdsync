/**
 * DiskHomeTimer.java Created on 2016年12月25日
 */
package com.yuncore.bdsync.sync;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>DiskHomeTimer</code>	
 * @author Feng OuYang
 * @version 1.0
 */
public class DiskHomeTimer extends Thread {

	private static final String TAG = "DiskHomeTimer";
	
	private boolean flag;
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#start()
	 */
	@Override
	public synchronized void start() {
		flag = true;
		super.start();
	}
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		setName(TAG);
		while(flag){
			checkCookie();
			try {
				Thread.sleep(1000 * 60 *2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param flag the flag to set
	 */
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	/**
	 * 检查cookie有效性
	 * 
	 * @return
	 */
	private boolean checkCookie() {
		Log.d(TAG, "checkCookie");
		final FSApi fsApi = new FSApiImple();
		try {
			final String who = fsApi.who();
			if (null != who && who.trim().length() > 0) {
				Log.w(TAG, "current cookie user:" + who);
				return true;
			}
		} catch (ApiException e) {
			e.printStackTrace();
		}
		Log.w(TAG, "cookie error exit");
		System.exit(1);
		return false;
	}
	
}
