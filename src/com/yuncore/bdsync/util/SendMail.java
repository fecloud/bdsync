package com.yuncore.bdsync.util;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.yuncore.bdsync.Environment;

public class SendMail {

	static final String TAG = "SendMail";
	
	public static boolean sendMail(String title, String subject)
			throws EmailException {
		Log.w(TAG,"sendMail title:" + title + " subject:" + subject);
		final SimpleEmail email = new SimpleEmail();
		email.setHostName("smtp.qq.com");
		email.setSSL(true);
		email.setSmtpPort(465);
		email.setAuthentication(Environment.getMailFrom(), Environment.getMailFromPass());
		email.addTo(Environment.getMailTo());
		email.setFrom(Environment.getMailFrom(), "来自" + Environment.getName());
		email.setSubject(title);
		email.setContent(subject, "text/plain;charset=UTF-8");
		final String result = email.send();
		if(result != null){
			Log.w(TAG,result);
			return true;
		}
		return false;
	}
	
}
