package com.yuncore.bdsync.util;


public class Stopwatch {

	String TAG = "Stopwatch";

	private long start;

	private long end;

	public synchronized void start() {
		start = System.currentTimeMillis();
	}

	public synchronized void stop() {
		stop(null);
	}

	public synchronized void stop(String tag) {
		end = System.currentTimeMillis();
		if (null == tag) {
			Log.i(TAG, "time:" + (end - start));
		} else {
			Log.i(TAG, tag + " time:" + (end - start));
		}
	}
}
