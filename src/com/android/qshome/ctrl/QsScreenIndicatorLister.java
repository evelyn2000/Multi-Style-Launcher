package com.android.qshome.ctrl;

public interface QsScreenIndicatorLister {
	
	public int getCurrentScreen();
	public int getScreenCount();
	public void setQsScreenIndicatorCallback(QsScreenIndicatorCallback callback);
	
	//protected QsScreenIndicatorCallback mQsWorkspaceCallback;
//	public void setQsScreenIndicatorCallback(QsScreenIndicatorCallback callback){
//		mQsWorkspaceCallback = callback;
//	}
}
