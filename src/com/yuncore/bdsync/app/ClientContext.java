//package com.yuncore.bdsync.app;
//
//import java.util.Map;
//
//import com.yuncore.bdsync.api.FSApi;
//import com.yuncore.bdsync.api.imple.FSApiImple;
//import com.yuncore.bdsync.app.imple.AppContext;
//import com.yuncore.bdsync.exception.ApiException;
//import com.yuncore.bdsync.exception.BDSyncException;
//
//public class ClientContext extends AppContext {
//
//	static final String TAG = "ClientContext";
//
//	@Override
//	public boolean load() throws BDSyncException {
//		if (time == 0 || System.currentTimeMillis() - time > interval) {
//			time = System.currentTimeMillis();
//			final FSApi api = new FSApiImple();
//			try {
//				final Map<String, String> diskHomePage = api.diskHomePage();
//				if (null != diskHomePage && !diskHomePage.isEmpty()) {
//					properties.putAll(diskHomePage);
//					return true;
//				} else {
//					properties.clear();
//					return false;
//				}
//			} catch (ApiException e) {
//				throw new BDSyncException("load diskHomePage error", e);
//			}
//
//		}
//		return true;
//	}
//
//}
