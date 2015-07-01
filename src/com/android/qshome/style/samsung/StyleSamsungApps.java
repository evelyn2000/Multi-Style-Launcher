package com.android.qshome.style.samsung;

import java.util.ArrayList;
import java.util.Random;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewStub;

import com.android.qshome.AllAppsView;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.AllAppsGridLayoutPages;
import com.android.qshome.ctrl.AllAppsWorkspace;
import com.android.qshome.ctrl.CellLayout;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.Folder;
import com.android.qshome.ctrl.QsScreenIndicator;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.IBaseStyleInterfaceWidget;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.Utilities;
import com.android.qshome.util.ThemeStyle;


import com.android.qshome.R;

public class StyleSamsungApps extends BaseStyleObjectApps /*implements IconCache.Callbacks */{
	private View mAllAppsLayout;
	
	public StyleSamsungApps(Launcher context, LauncherModel model, IconCache iconCache, IBaseStyleInterfaceWidget callback){
		super(context, model, iconCache, callback);
		
		//iconCache.setCallbacks(this);
	}
	@Override
	public ThemeStyle getThemeStyle(){
		return ThemeStyle.Samsung;
	}
	
	public int getThemePreviewImage(){
		return 0;
	}
	
	public int getThemeTitleResourceId(){
		return 0;
	}
	
	public void onCreate(DragLayer dragLayer, DragController dragController){
		//QsLog.LogW("StyleSamsungApps::onCreate(start)===");
		ViewStub stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_samsung);
		mAllAppsLayout = stub.inflate();
		
        //mAllAppsGrid = (AllAppsView) stub.inflate();//(AllAppsView)dragLayer.findViewById(R.id.all_apps_view);
		mAllAppsGrid = (AllAppsView)mAllAppsLayout.findViewById(R.id.all_app_grid_pages);
		AllAppsGridLayoutPages allAppPages = (AllAppsGridLayoutPages)mAllAppsGrid;
		
		allAppPages.setLauncher(getLauncher());
		allAppPages.setDragController(dragController);
		allAppPages.setWillNotDraw(false); // We don't want a hole punched in our window.
        // Manage focusability manually since this thing is always visible
		allAppPages.setFocusable(false); 
		
		mAllAppsLayout.setVisibility(View.GONE);
		
		QsScreenIndicator screenIndicator = (QsScreenIndicator) mAllAppsLayout.findViewById(R.id.all_app_screen_indicator);
        screenIndicator.initial(getLauncher(), allAppPages);
        
        
//        Workspace workspace = ((AllAppsWorkspace)mAllAppsGrid).getWorkspace();
//        workspace.setOnLongClickListener(getLauncher());
//        workspace.setDragController(dragController);
//        workspace.setLauncher(getLauncher());
        

//        if(mIconBackgroundBitmap == null){
//	        final int[] iconBgBimap = new int[] {/*R.drawable.zzz_samsung_icon_alarm_bg,
//	        		R.drawable.zzz_samsung_icon_carmount_bg,
//	        		R.drawable.zzz_samsung_icon_clock_bg,
//	        		R.drawable.zzz_samsung_icon_dial_bg,
//	        		R.drawable.zzz_samsung_icon_market_bg,
//	        		R.drawable.zzz_samsung_icon_mysingle_bg,
//	        		R.drawable.zzz_samsung_icon_gmail_bg,
//	        		R.drawable.zzz_samsung_icon_weather_bg,
//	        		R.drawable.zzz_samsung_icon_googlemap_bg,
//	        		R.drawable.zzz_samsung_icon_game_bg,
//	        		R.drawable.zzz_samsung_icon_june_bg,*/
//	        		R.drawable.zzz_samsung_icon_camera_bg ,
//	        		R.drawable.zzz_samsung_icon_fileviewer_bg ,
//	        		R.drawable.zzz_samsung_icon_gallery_bg ,
//	        		R.drawable.zzz_samsung_icon_market_bg ,
//	        		R.drawable.zzz_samsung_icon_fmradio_bg ,
//	        		R.drawable.zzz_samsung_icon_voicedialer_bg ,
//	        		R.drawable.zzz_samsung_icon_qq_bg ,
//	        		R.drawable.zzz_samsung_icon_allshare_bg ,
//	        		R.drawable.zzz_samsung_icon_daily_birefing_bg ,
//	        		R.drawable.zzz_samsung_icon_dictionary_bg ,
//	        		R.drawable.zzz_samsung_icon_digitalframe_bg ,
//	        		R.drawable.zzz_samsung_icon_e_book_bg ,
//	        		R.drawable.zzz_samsung_icon_english_bg ,
//	        		R.drawable.zzz_samsung_icon_game_bg ,
//	        		R.drawable.zzz_samsung_icon_googletalk_bg ,
//	        		R.drawable.zzz_samsung_icon_homemount_bg ,
//	        		R.drawable.zzz_samsung_icon_kaixin_bg ,
//	        		R.drawable.zzz_samsung_icon_memo_bg ,
//	        		R.drawable.zzz_samsung_icon_minidiary_bg ,
//	        		R.drawable.zzz_samsung_icon_mysingle_bg ,
//	        		R.drawable.zzz_samsung_icon_natebrowser_bg ,
//	        		R.drawable.zzz_samsung_icon_nevermap_bg ,
//	        		R.drawable.zzz_samsung_icon_samsungapps_bg ,
//	        		R.drawable.zzz_samsung_icon_smartreader_bg ,
//	        		R.drawable.zzz_samsung_icon_thinkfree_bg ,
//	        		R.drawable.zzz_samsung_icon_tmap_bg ,
//	        		R.drawable.zzz_samsung_icon_ucbrowser_bg ,
//	        		R.drawable.zzz_samsung_icon_videostudio_bg ,
//	        		R.drawable.zzz_samsung_icon_voicecommand_bg ,
//	        		R.drawable.zzz_samsung_icon_weather_bg ,
//	        };
//	        int nLength = iconBgBimap.length;
//	        mIconBackgroundBitmap = new Bitmap[nLength];
//	    	for( int i = 0; i < nLength; i++ ) {
//	    		//Bitmap iconBgBitmap = BitmapFactory.decodeResource(getLauncher().getResources(), iconBgBimap[i]);
//	    		mIconBackgroundBitmap[i] = BitmapFactory.decodeResource(getLauncher().getResources(), iconBgBimap[i]);
//	    	}
//        }
        //QsLog.LogW("StyleSamsungApps::onCreate(end)===nLength:"+nLength+"==len:"+mIconBackgroundBitmap.length);
	}
	
//	private Bitmap getIconBitmapBackground(ComponentName componentName){
//		if(mIconBackgroundBitmap == null || mIconBackgroundBitmap.length == 0){
//			QsLog.LogE("StyleSamsungApps::getIconBitmapBackground()===no images==");
//			return null;
//		}
//		
//		String classname = componentName.getShortClassName();
//		//QsLog.LogD("StyleSamsungApps::getIconBitmapBackground()==classname:"+classname+"=componentName:"+componentName);
//		if(classname.equalsIgnoreCase("camera"))
//			return mIconBackgroundBitmap[0];
//		else if(classname.equalsIgnoreCase("FileManagerActivity"))
//			return mIconBackgroundBitmap[1];
//		else if(classname.equalsIgnoreCase("Gallery") || classname.equalsIgnoreCase("GalleryPicker"))
//			return mIconBackgroundBitmap[2];
//		else if(classname.equalsIgnoreCase("FMRadioActivity"))
//			return mIconBackgroundBitmap[4];
//		else if(classname.equalsIgnoreCase("VoiceDialerActivity"))
//			return mIconBackgroundBitmap[5];
//		
//		Random random = new Random(System.currentTimeMillis());
//		final int index = Math.abs(random.nextInt()) % mIconBackgroundBitmap.length;
//		//QsLog.LogW("StyleSamsungApps::getIconBitmapBackground()===index:"+index+"==len:"+mIconBackgroundBitmap.length);
//		return mIconBackgroundBitmap[index];
//	}
//	
//	public Bitmap createIconBitmap(ComponentName componentName, ResolveInfo info, Context context, final PackageManager packageManager){
//		//Random random = new Random(System.currentTimeMillis());
//		Bitmap bg = getIconBitmapBackground(componentName);//mIconBackgroundBitmap[Math.abs(random.nextInt())% mIconBackgroundBitmap.length];
//
//		return Utilities.createIconBitmap(info.activityInfo.loadIcon(packageManager), bg, context);
//	}
//	//static final int INITIAL_ICON_BACKGROUND_CAPACITY = 10;
//	private Bitmap[] mIconBackgroundBitmap;// = new Bitmap[INITIAL_ICON_BACKGROUND_CAPACITY];
//	
	@Override
	public boolean isAllAppsVisible(){
		if(mAllAppsLayout != null)
			return (mAllAppsLayout.getVisibility() == View.VISIBLE ? true : false);
    	return false;
    }
	
	@Override
	public boolean showAllApps(boolean animated){
		//QsLog.LogD("StyleSamsungApps::showAllApps(0)===");
		if(super.showAllApps(animated)){
			//Workspace workspace = ((AllAppsWorkspace)mAllAppsGrid).getWorkspace();
//			mDragController.setDragScoller(workspace);
//    		mDragController.setMoveTarget(workspace);
//    		mDragController.addDropTarget(workspace);
			
			mAllAppsLayout.setVisibility(View.VISIBLE);
			return true;
		}
		
		return false;
	}
	@Override
	public boolean closeAllApps(boolean animated){
		//QsLog.LogD("StyleSamsungApps::closeAllApps(0)===");
		if (isAllAppsVisible()) {
			//QsLog.LogD("StyleSamsungApps::closeAllApps(1)===");
			
			mAllAppsLayout.setVisibility(View.GONE);
			
            mAllAppsGrid.zoom(0.0f, animated);
            ((View)mAllAppsGrid).setFocusable(false);
            
            return true;
        }
		
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
				
//		outState.putInt(Launcher.RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getCurrentScreen());
//		if (isAllAppsVisible()) {
//            outState.putBoolean(Launcher.RUNTIME_STATE_ALL_APPS_FOLDER, true);
//        }
	}
	@Override
	public void restoreState(Bundle savedState){

//        final int currentScreen = savedState.getInt(Launcher.RUNTIME_STATE_CURRENT_SCREEN, -1);
//        if (currentScreen > -1) {
//            mWorkspace.setCurrentScreen(currentScreen);
//        }
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
	public void release(){
//		mIconCache.setCallbacks(null);
//		if(mIconBackgroundBitmap != null){
//			int nLength = mIconBackgroundBitmap.length;
//			for( int i = 0; i < nLength; i++ ) {
//	    		//Bitmap iconBgBitmap = BitmapFactory.decodeResource(getLauncher().getResources(), iconBgBimap[i]);
//	    		mIconBackgroundBitmap[i].recycle();
//	    		mIconBackgroundBitmap[i] = null;
//	    	}
//		}
		super.release();
	}
}
