package com.android.qshome.style;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.model.IconCache;
import com.android.qshome.util.ThemeStyle;

public abstract class BaseStyleObject {

	public final static int THEME_TYPE_WIDGET = 0;
	public final static int THEME_TYPE_ALLAPPS = 1;
	public final static int THEME_TYPE_ALL_IN_ONE = 2;
	
	public final static int QS_RET_FAIL = 0;
	public final static int QS_RET_SUCCESS = -1;
	public final static int QS_RET_CLOSE_ALLAPPS = -2;
	public final static int QS_RET_SHOW_ALLAPPS = -3;
	
	public abstract int getThemeType();
	public abstract ThemeStyle getThemeStyle();
	public abstract int getThemePreviewImage();
	public abstract int getThemeTitleResourceId();
	public abstract void onCreate(DragLayer dragLayer, DragController dragController);
	
	
	public abstract boolean showAllApps(boolean animated);
	public abstract boolean closeAllApps(boolean animated);
	public abstract boolean isAllAppsVisible();
	
	protected Launcher mLauncher;
	protected LauncherModel mModel;
	protected IconCache mIconCache;
	
	public BaseStyleObject(Launcher context, LauncherModel model, IconCache iconCache){
		mLauncher = context;
		mModel = model;
		mIconCache = iconCache;
	}
	
	public Launcher getLauncher(){
		return mLauncher;
	}
	
	public LauncherModel getLauncherModel(){
		return mModel;
	}
	
	public IconCache getIconCache(){
		return mIconCache;
	}
	
	public void lockAllApps(){}
	public void unlockAllApps(){}
	
	public void onResume(){}
	
	public void onNewIntent(Intent intent){}
	
	public void onPause(){}
	
	public void onDestroy(){}
	
	public void onBackPressed(){}
	
	public void onRetainNonConfigurationInstance(){}
	public void onSaveInstanceState(Bundle outState){}
	public void restoreState(Bundle savedState){}
	
	public void onLocaleChanged(boolean localeChanged){}
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data){
		return false;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event){
		return false;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
		return false;
	}
	
	public Dialog onCreateDialog(int id){
		return null;
	}
	
	public boolean onPrepareDialog(int id, Dialog dialog){
		return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		return false;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu){
		return false;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		return false;
	}
	
	public void release(){}
}
