package com.android.qshome.style.droidics;

import java.util.ArrayList;
import java.util.Random;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.qshome.AllAppsView;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.AllAppsGridLayoutPages;
import com.android.qshome.ctrl.AllAppsWorkspace;
import com.android.qshome.ctrl.CellLayout;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.FastBitmapDrawable;
import com.android.qshome.ctrl.Folder;
import com.android.qshome.ctrl.QsScrollbar;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.IBaseStyleInterfaceWidget;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.Utilities;
import com.android.qshome.util.ThemeStyle;

import com.android.qshome.R;

public class StyleDroidIcsApps extends BaseStyleObjectApps implements IconCache.Callbacks {
	
	private View mAllAppsLayout;
	private TextView mTitlebarApps;
	
	public StyleDroidIcsApps(Launcher context, LauncherModel model, IconCache iconCache, IBaseStyleInterfaceWidget callback){
		super(context, model, iconCache, callback);
	}
	
	public Bitmap createIconBitmap(ComponentName componentName, ResolveInfo info, Context context, final PackageManager packageManager){

		int nDefId = getDefaultIconRes(componentName.getClassName());

        if(nDefId > 0){
        	return Utilities.createIconBitmap(context.getResources().getDrawable(nDefId),  context);
        }
		return null;
	}
	
	private int getDefaultIconRes(String pkg)
	{
	    if(pkg.equals("com.android.contacts.DialtactsActivity")) // dail
	    	return R.drawable.zzzz_ics_launcher_phone;
	    else if(pkg.equals("com.android.contacts.DialtactsContactsEntryActivity")) // contacts
	        return R.drawable.zzzz_ics_vcard_attach;
	    else if ("com.android.browser.BrowserActivity".equals(pkg)) {
			return R.drawable.zzzz_ics_launcher_browser;
	    }
	    
	    return 0;
	}
	 
	@Override
	public ThemeStyle getThemeStyle(){
		return ThemeStyle.DroidIcs;
	}
	
	public int getThemePreviewImage(){
		return 0;
	}
	
	public int getThemeTitleResourceId(){
		return 0;
	}
	
	public void onCreate(DragLayer dragLayer, DragController dragController){
		//QsLog.LogW("StyleSamsungApps::onCreate(start)===");
		mIconCache.setCallbacks(this);
		
		ViewStub stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_droidics);
		mAllAppsLayout = stub.inflate();
		mAllAppsGrid = (AllAppsView)mAllAppsLayout.findViewById(R.id.all_app_grid_pages);
		
		AllAppsGridLayoutPages allAppPages = (AllAppsGridLayoutPages)mAllAppsGrid;
		 
		allAppPages.setLauncher(getLauncher());
		allAppPages.setDragController(dragController);
 
        //((View) mAllAppsGrid).setOnLongClickListener(mLauncher);
        
		allAppPages.setWillNotDraw(false); // We don't want a hole punched in our window.
        // Manage focusability manually since this thing is always visible
		allAppPages.setFocusable(false); 
        
//        Workspace workspace = ((AllAppsWorkspace)mAllAppsGrid).getWorkspace();
//        workspace.setOnLongClickListener(getLauncher());
//        workspace.setDragController(dragController);
//        workspace.setLauncher(getLauncher());
        
        mAllAppsLayout.setVisibility(View.GONE);

        QsScrollbar mScreenIndicator = (QsScrollbar) mAllAppsLayout.findViewById(R.id.screen_indicator);
        
        mScreenIndicator.initial(getLauncher(), allAppPages);
        
        mTitlebarApps = (TextView) mAllAppsLayout.findViewById(R.id.ics_title_tab_show_all_apps);
        mTitlebarApps.setSelected(true);
	}
	
	
	
	@Override
	public int onLongClick(View v){
		if(isAllAppsVisible()){

			Object tag = v.getTag();
	        if (tag != null && (tag instanceof ApplicationInfo)) {

	        	((AllAppsGridLayoutPages) mAllAppsGrid).startDrag(v, new ApplicationInfo((ApplicationInfo)tag));
		        //closeAllApps(false);
		        
		        return QS_RET_CLOSE_ALLAPPS;
	        }
		}
		return super.onLongClick(v);
	}
	
	@Override
    public int onClick(View v){
		if(isAllAppsVisible()){

			Object tag = v.getTag();
	    	if(tag != null && (tag instanceof ApplicationInfo)) {
	    		ApplicationInfo info = (ApplicationInfo)tag;
	    		mLauncher.startActivitySafely(info.intent, info);
	    		return QS_RET_SUCCESS;
	    	}
		}
    	return super.onClick(v);
    }
	
	@Override
	public boolean showAllApps(boolean animated){
		//QsLog.LogD("StyleSamsungApps::showAllApps(0)===");
		if(mAllAppsLayout != null && !isAllAppsVisible()){
//			mAllAppsGrid.zoom(1.0f, animated);

			mAllAppsLayout.setVisibility(View.VISIBLE);
			((View) mAllAppsGrid).setFocusable(true);
	        ((View) mAllAppsGrid).requestFocus();
	        mTitlebarApps.setSelected(true);
	        return true;
		}
		
		return false;
	}
	
	@Override
	public boolean closeAllApps(boolean animated){
		if (isAllAppsVisible()) {
			mAllAppsLayout.setVisibility(View.GONE);
            
            return true;
        }
		
		return false;
	}
	
	@Override
	public boolean isAllAppsVisible(){
		if(mAllAppsLayout != null)
			return (mAllAppsLayout.getVisibility() == View.VISIBLE ? true : false);
    	return false;
    }
	
	@Override
	public void onPause(){
//		Workspace workspace = ((AllAppsWorkspace)mAllAppsGrid).getWorkspace();
//		if (workspace != null && workspace.isShown()) {
//			//QsLog.LogE("StyleSamsungApps::onPause()==setCurrentScreen===");
//			workspace.setCurrentScreen(workspace.getCurrentScreen());
//        }
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState){
				

	}
	@Override
	public void restoreState(Bundle savedState){


	}

	@Override
	public void release(){
		mIconCache.setCallbacks(null);

		super.release();
	}
}
