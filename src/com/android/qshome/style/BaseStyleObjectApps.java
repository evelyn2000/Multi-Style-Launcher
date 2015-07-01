package com.android.qshome.style;

import java.util.ArrayList;

import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.android.qshome.AllAppsView;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.ThemeStyle;

public abstract class BaseStyleObjectApps extends BaseStyleObject
	implements IBaseStyleInterfaceApps {

	protected AllAppsView mAllAppsGrid;
	protected IBaseStyleInterfaceWidget mCallbackWidget;
	
	public BaseStyleObjectApps(Launcher context, LauncherModel model, IconCache iconCache, IBaseStyleInterfaceWidget callback){
		super(context, model, iconCache);
		mCallbackWidget = callback;
	}
	
	public int getThemeType(){
		return THEME_TYPE_ALLAPPS;
	}
	
	public ThemeStyle getThemeStyle(){
		return ThemeStyle.Unkown;
	}
	
	public int getThemePreviewImage(){
		return 0;
	}
	
	public int getThemeTitleResourceId(){
		return 0;
	}
	
	public void setIBaseStyleInterfaceWidget(IBaseStyleInterfaceWidget callback){
		mCallbackWidget = null;
		mCallbackWidget = callback;
	}
	
	public boolean showAllApps(boolean animated){
		//QsLog.LogD("BaseStyleObjectApps::showAllApps(0)===");
		if(mAllAppsGrid != null && !isAllAppsVisible()){
			mAllAppsGrid.zoom(1.0f, animated);
			//QsLog.LogD("BaseStyleObjectApps::showAllApps(1)===");
	        ((View) mAllAppsGrid).setFocusable(true);
	        ((View) mAllAppsGrid).requestFocus();
	        
//	        if(mCallbackWidget != null)
//            	mCallbackWidget.onShowAllApps(animated);
	        return true;
		}
		
		return false;
	}
	
	public boolean closeAllApps(boolean animated){
		//QsLog.LogD("BaseStyleObjectApps::closeAllApps(0)===");
		if (isAllAppsVisible()) {
			//QsLog.LogD("BaseStyleObjectApps::closeAllApps(1)===");
            mAllAppsGrid.zoom(0.0f, animated);
            ((View)mAllAppsGrid).setFocusable(false);
            
//            if(mCallbackWidget != null)
//            	mCallbackWidget.onCloseAllApps(animated);
            
            return true;
        }
		
		return false;
	}
	
	
	public int onClick(View v){
		return QS_RET_FAIL;
	}
	
	public int onLongClick(View v){
		return QS_RET_FAIL;
	}
	
//	public void onCreate(DragLayer dragLayer, DragController dragController){
//		
//	}
	
	public void bindQsExtItems(ArrayList<ItemInfo> shortcuts){
    	
    }
	
    public void bindAllApplications(ArrayList<ApplicationInfo> apps){
    	mAllAppsGrid.setApps(apps);
    	//QsLog.LogD("BaseStyleObjectApps::bindAllApplications(1)===size:"+apps.size());
    }
    
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps){
        mAllAppsGrid.addApps(apps);
        //QsLog.LogD("BaseStyleObjectApps::bindAppsAdded(1)===size:"+apps.size());
    }
    
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps){
        mAllAppsGrid.updateApps(apps);
        //QsLog.LogD("BaseStyleObjectApps::bindAppsUpdated(1)===size:"+apps.size());
    }
    
    public void bindAppsRemoved(ArrayList<ApplicationInfo> apps, boolean permanent){
        mAllAppsGrid.removeApps(apps);
        //QsLog.LogD("BaseStyleObjectApps::bindAppsRemoved(1)===size:"+apps.size());
    }
    
    public boolean isAllAppsVisible(){
    	return (mAllAppsGrid != null) ? mAllAppsGrid.isVisible() : false;
    }
    
    public void release(){
    	mCallbackWidget = null;
    	// destory callback handle...
    	QsLog.LogE("BaseStyleObjectApps::release()=========");
    	if(mAllAppsGrid != null){
    		
    		mAllAppsGrid.release();
    		
			//final ViewGroup v = (ViewGroup)((View)mAllAppsGrid).getParent();
			//v.removeView((View)mAllAppsGrid);
			//mAllAppsGrid = null;
		}
    }
    
    @Override
    public void onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
    	if(mAllAppsGrid != null)
    		mAllAppsGrid.surrender();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If all apps is animating, don't show the menu, because we don't know
        // which one to show.
        //if (isAllAppsVisible() && !mAllAppsGrid.isOpaque()) {
    //	QsLog.LogE("BaseStyleObjectApps::onPrepareOptionsMenu()=========isVisible:"+mAllAppsGrid.isVisible()+
    //			"==isOpaque:"+mAllAppsGrid.isOpaque()+"==all:"+isAllAppsVisible());
    	if(isAllAppsVisible() && mAllAppsGrid != null && !mAllAppsGrid.isOpaque() ){
            return true;
        }
        return false;
    }
}
