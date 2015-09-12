package com.yuncore.bdsync.http;


public class HttpInput extends Http {

	public HttpInput(String url, Method method) {
		super(url, method);
	}
	
	@Override
	protected void addRequestProperty() {
	}

//	public void saveFile(PCSDownloadFile pcsDownloadFile) {
//		if (null != pcsDownloadFile) {
//			try {
//				long sum = getContentLength();
//				final String saveFile = pcsDownloadFile.getTempPath(sum,
//						conn.getHeaderFields());
//				if (null != saveFile) {
//					final File file = new File(saveFile);
//					final FileOutputStream out = new FileOutputStream(file);
//					final InputStream in = getInputStream();
//					final byte[] buffer = new byte[1024 * 100];
//					int len = 0;
//
//					long finish = 0;
//					while (-1 != (len = in.read(buffer))) {
//						out.write(buffer, 0, len);
//						out.flush();
//						finish += len;
//						pcsDownloadFile.downloadProcces(saveFile, sum, finish,
//								len);
//					}
//					out.close();
//					final String save = pcsDownloadFile.savepath(sum,
//							conn.getHeaderFields());
//					pcsDownloadFile.saveResult(file.renameTo(new File(save)));
//				}
//			} catch (Exception e) {
//				pcsDownloadFile.downloadError(e);
//			}
//		}
//	}
}
