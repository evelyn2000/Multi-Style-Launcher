package com.android.qshome.ctrl;

public interface QsScreenIndicatorCallback {
	public void onScrollChangedCallback(int l, int t, int oldl, int oldt);
	//public void onScrollBy(int x, int y);
	public void onChangeToScreen(int whichScreen);
	public void onPageCountChanged(int nNewCount);
}
