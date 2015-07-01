package com.android.qshome.util;

public final class QsLog {
	private static final String TAG = "QsHome";
	
	public static void LogV(String msg){
		android.util.Log.v(TAG, msg);
	}
	
	public static void LogD(String msg){
		android.util.Log.d(TAG, msg);
	}
	
	public static void LogW(String msg){
		android.util.Log.w(TAG, msg);
	}
	
	public static void LogE(String msg){
		android.util.Log.e(TAG, msg);
	}
}
