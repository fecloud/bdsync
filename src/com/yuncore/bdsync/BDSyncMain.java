/**
 * @(#) BDSyncMain.java Created on 2015-9-7
 *
 * 
 */
package com.yuncore.bdsync;

import com.yuncore.bdsync.BDSycService.BDSyncServiceListener;
import com.yuncore.bdsync.sync.Sync;

/**
 * The class <code>BDSyncMain</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class BDSyncMain implements BDSyncServiceListener {

	private Sync pcsSync;

	private BDSyncMain(String[] args) {
		pcsSync = new Sync(args);
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			printHelp();
		} else {
			final String action = args[0].trim();
			if ("sync".equalsIgnoreCase(action)) {
				if (args.length >= 2) {
					final BDSyncMain bdsyncMain = new BDSyncMain(args);
					new BDSycService(bdsyncMain).start();
				} else {
					printHelp();
				}
			} else if ("stop".equalsIgnoreCase(action)) {
				new ShutDownBDSyncService().start();
			} else {
				printHelp();
			}
		}
	}

	public void onStop() {
		pcsSync.stop();
	}

	public void onStart() {
		pcsSync.start();
	}

	private static final void printHelp() {
		System.err.println("");
		System.err.println("Usage:bdsync [sync|stop]");
		System.err.println("");
		System.err
				.println("sync <local_dir> [-p port] [-l exinclude dir] [-c exinclude dir]");
		System.err.println("");
		System.err.println("stop");
		System.err.println("");
	}

}
