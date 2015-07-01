package com.android.qshome.style;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherAppWidgetHost;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.Folder;
import com.android.qshome.ctrl.FolderIcon;
import com.android.qshome.ctrl.LiveFolderIcon;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.FolderInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.model.LiveFolderInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.style.defaults.StyleDefaultsApps;
import com.android.qshome.style.droidics.StyleDroidIcsApps;
import com.android.qshome.style.htc.StyleHtcWidget;
import com.android.qshome.style.samsung.StyleSamsungApps;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.ThemeStyle;

import com.android.qshome.R;

public abstract class BaseStyleObjectWidget extends BaseStyleObject 
	implements IBaseStyleInterfaceWidget, LauncherModel.Callbacks {
	
	public static final int SCREEN_COUNT = 5;
    public static final int DEFAULT_SCREEN = 2;
    
    protected BaseStyleObjectApps mAllAppsObject;
    
	protected Workspace mWorkspace;
	protected DragController mDragController;
	
	//private boolean mPaused = true;
	//private boolean mOnResumeNeedsLoad;
	
	private ArrayList<ItemInfo> mDesktopItems;
	private HashMap<Long, FolderInfo> mFolders;
	
	protected AppWidgetManager mAppWidgetManager;
	protected LauncherAppWidgetHost mAppWidgetHost;
	
	protected ViewGroup mWorkspaceViewStub;
	
	public BaseStyleObjectWidget(Launcher context, LauncherModel model, IconCache iconCache, 
			ThemeStyle appStyle){
		super(context, model, iconCache);

		if(!changeAppsThemeStyle(appStyle))
		{
//			if(getThemeType() == THEME_TYPE_ALL_IN_ONE)
//				mModel.setCallbacksBindApps((LauncherModel.CallbacksBindApps)this);
		}
	}
	
	public void initialize(ArrayList<ItemInfo> desktopItems, HashMap<Long,FolderInfo> folders, 
			AppWidgetManager appWidgetManager, LauncherAppWidgetHost appWidgetHost){
		
		mDesktopItems = desktopItems;
		mFolders = folders;
		
		mAppWidgetManager = appWidgetManager;
		mAppWidgetHost = appWidgetHost;
	}

	public int getThemeType(){
		return THEME_TYPE_WIDGET;
	}
	
	//public abstract BaseStyleObject.ThemeStyle getThemeStyle();
	
	public int getThemePreviewImage(){
		return 0;
	}
	
	public int getThemeTitleResourceId(){
		return 0;
	}
	
	public int getThemeAppsPreviewImage(){
		if(mAllAppsObject != null){
			return mAllAppsObject.getThemePreviewImage();
		}
		return 0;
	}
	
	public int getThemeAppsTitleResourceId(){
		if(mAllAppsObject != null){
			return mAllAppsObject.getThemeTitleResourceId();
		}
		return 0;
	}
	
//	public LauncherModel.CallbacksBindApps getCallbacksBindApps(){
//		return mAllAppsObject;
//	}
	
	public BaseStyleObjectApps getBaseStyleObjectApps(){
		return mAllAppsObject;
	}
	
	public void initializeApps(){
		mAllAppsObject.onCreate((DragLayer) mLauncher.findViewById(R.id.drag_layer), mDragController);
		mAllAppsObject.onResume();
	}
	
	public boolean changeAppsThemeStyle(ThemeStyle appStyle){
		BaseStyleObjectApps tempApps = getBaseStyleObjectApps(appStyle);

		if(tempApps != null)
		{
			if(mAllAppsObject != null){
				mAllAppsObject.release();
				mAllAppsObject = null;
			}
			// change application bind callback
			//mModel.setCallbacksBindApps(tempApps);
			mAllAppsObject = tempApps;
			return true;
		}
		return false;
	}
	
	protected BaseStyleObjectApps getBaseStyleObjectApps(ThemeStyle appStyle){
		if(appStyle == ThemeStyle.Default){
			return new StyleDefaultsApps(mLauncher, mModel, mIconCache, this);
		}else if(appStyle == ThemeStyle.Samsung) {
			return new StyleSamsungApps(mLauncher, mModel, mIconCache, this);
		}else if(appStyle == ThemeStyle.DroidIcs) {
			return new StyleDroidIcsApps(mLauncher, mModel, mIconCache, this);
		}
		return null;
	}
	
	public DragController getDragController(){
		return mDragController;
	}
	
	public Workspace getWorkspace() {
        return mWorkspace;
    }
	
	// for apps begin
	public boolean showAllApps(boolean animated){
		if(mAllAppsObject != null && mAllAppsObject.showAllApps(animated)){
			return true;
		}
		return false;
	}
	
    public boolean closeAllApps(boolean animated){
    	mWorkspace.setVisibility(View.VISIBLE);

    	if(mAllAppsObject != null && mAllAppsObject.closeAllApps(animated)){

			mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
			return true;
		}
    	return false;
    }
    	
    //public void onCloseAllApps(boolean animated){ }
    
    
    // for apps end
    
    // Begin Implementation of the method from LauncherModel.Callbacks.
//	public void resetLoadOnResume(){
//		mOnResumeNeedsLoad = false;
//	}
//	public boolean getLoadOnResume(){
//		return mOnResumeNeedsLoad;
//	}
	
    public boolean setLoadOnResume(){
    	if (mLauncher.mPaused) {
            //QsLog.LogE("BaseStyleObjectWidget::setLoadOnResume=====");
    		mLauncher.mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }
    
	public int getCurrentWorkspaceScreen(){
		if (mWorkspace != null) {
            return mWorkspace.getCurrentScreen();
        }
		
		return SCREEN_COUNT / 2;
	}
	
    public void startBinding(){
    	final Workspace workspace = mWorkspace;
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
        }
    }
    
    public boolean isBindItemsFirst(){
    	return false;
    }
    
    public int getDefaultQsExtAppRes(){
    	return 0;
    }
    
    public void bindQsExtItems(ArrayList<ItemInfo> shortcuts){
    	setLoadOnResume();
    	if(mAllAppsObject != null)
			mAllAppsObject.bindQsExtItems(shortcuts);
    }
    
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end){
    	setLoadOnResume();

		final Workspace workspace = mWorkspace;

        for (int i=start; i<end; i++) {
            final ItemInfo item = shortcuts.get(i);
            mDesktopItems.add(item);
            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    final View shortcut = getLauncher().createShortcut((ShortcutInfo)item);
                    workspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1,
                            false);
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                    final FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, getLauncher(),
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
                            (UserFolderInfo) item);
                    workspace.addInScreen(newFolder, item.screen, item.cellX, item.cellY, 1, 1,
                            false);
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
                    final FolderIcon newLiveFolder = LiveFolderIcon.fromXml(
                            R.layout.live_folder_icon, getLauncher(),
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
                            (LiveFolderInfo) item);
                    workspace.addInScreen(newLiveFolder, item.screen, item.cellX, item.cellY, 1, 1,
                            false);
                    break;
            }
        }

        workspace.requestLayout();
    }
    
    public void bindFolders(HashMap<Long,FolderInfo> folders){
    	setLoadOnResume();
        mFolders.clear();
        mFolders.putAll(folders);
    }

    public void bindAppWidget(LauncherAppWidgetInfo item){
    	setLoadOnResume();

        final Workspace workspace = mWorkspace;

        final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        item.hostView = mAppWidgetHost.createView(getLauncher(), appWidgetId, appWidgetInfo);

        item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
        item.hostView.setTag(item);

        workspace.addInScreen(item.hostView, item.screen, item.cellX,
                item.cellY, item.spanX, item.spanY, false);

        workspace.requestLayout();

        mDesktopItems.add(item);
    }
    
    public void finishBindingItems(){
    	setLoadOnResume();
    	getLauncher().onFinishBindingItems();
    }
    
   
    public void bindAllApplications(ArrayList<ApplicationInfo> apps){
    	if(mAllAppsObject != null)
			mAllAppsObject.bindAllApplications(apps);
    }
    
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps){
    	setLoadOnResume();
    	getLauncher().removeDialog(Launcher.DIALOG_CREATE_SHORTCUT);
    	if(mAllAppsObject != null)
			mAllAppsObject.bindAppsAdded(apps);
    }
    
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps){
    	setLoadOnResume();
    	getLauncher().removeDialog(Launcher.DIALOG_CREATE_SHORTCUT);
    	mWorkspace.updateShortcuts(apps);
    	
    	if(mAllAppsObject != null){
			mAllAppsObject.bindAppsUpdated(apps);
		}
    }
    
    public void bindAppsRemoved(ArrayList<ApplicationInfo> apps, boolean permanent){
    	getLauncher().removeDialog(Launcher.DIALOG_CREATE_SHORTCUT);
    	
    	if(permanent)
    		mWorkspace.removeItems(apps);
    	
    	if(mAllAppsObject != null){
			mAllAppsObject.bindAppsRemoved(apps, permanent);
		}
    }
    
    public boolean isAllAppsVisible(){
    	return (mAllAppsObject != null) ? mAllAppsObject.isAllAppsVisible() : false;
    }
    // End Implementation of the method from LauncherModel.Callbacks.

    public int getWorkspaceScreenLayout(){
    	return R.layout.workspace_screen;
    }
    
    public int getFolderIconLayout(){
    	return R.layout.folder_icon;
    }
    // Begin system interface
    @Override
    public void onCreate(DragLayer dragLayer, DragController dragController){
    	mDragController = dragController;
    	if(mWorkspace == null){
    		//android.util.Log.e("QsHome", "BaseStyleObjectWidget::onCreate() mWorkspace is null...");
    		throw new IllegalArgumentException("BaseStyleObjectWidget::onCreate() Invalid mWorkspace, mWorkspace is null");
    	}
    	
    	mLauncher.setWorkspace(mWorkspace);
    	if(mAllAppsObject != null){
			mAllAppsObject.onCreate(dragLayer, dragController);
		}
    }
    @Override
    public void onResume(){
    	
    	if(mAllAppsObject != null){
			mAllAppsObject.onResume();
		}
    }
    @Override
	public void onNewIntent(Intent intent){
    	
    	// Close the apps
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            
            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            boolean allAppsVisible = isAllAppsVisible();
            if (!mWorkspace.isDefaultScreenShowing()) {
                mWorkspace.moveToDefaultScreen(alreadyOnHome && !allAppsVisible);
            }
            
            closeAllApps(alreadyOnHome && allAppsVisible);
        }
        
    	if(mAllAppsObject != null){
			mAllAppsObject.onNewIntent(intent);
		}
    }
	@Override
	public void onPause(){
		
		if (mWorkspace != null && mWorkspace.isShown()) {
			//QsLog.LogE("BaseStyleObjectWidget::onPause()==setCurrentScreen===");
            mWorkspace.setCurrentScreen(getCurrentWorkspaceScreen());
        }
		
		if(mAllAppsObject != null){
			mAllAppsObject.onPause();
		}
	}
	@Override
	public void onDestroy(){
		
		//mDesktopItems = null;
		
		if(mAllAppsObject != null){
			mAllAppsObject.onDestroy();
		}
	}
	@Override
	public void onBackPressed(){
		if(mAllAppsObject != null){
			mAllAppsObject.onBackPressed();
		}
	}
	@Override
	public void onSaveInstanceState(Bundle outState){
		if(mAllAppsObject != null){
			mAllAppsObject.onSaveInstanceState(outState);
		}
		
		outState.putInt(Launcher.RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getCurrentScreen());
		if (isAllAppsVisible()) {
            outState.putBoolean(Launcher.RUNTIME_STATE_ALL_APPS_FOLDER, true);
        }
	}
	@Override
	public void restoreState(Bundle savedState){
		if(mAllAppsObject != null){
			mAllAppsObject.restoreState(savedState);
		}
		
		final boolean allApps = savedState.getBoolean(Launcher.RUNTIME_STATE_ALL_APPS_FOLDER, false);
        if (allApps) {
       		showAllApps(false);
        }

        final int currentScreen = savedState.getInt(Launcher.RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentScreen(currentScreen);
        }
	}
	@Override
	public void onRetainNonConfigurationInstance(){
		if(mAllAppsObject != null){
			mAllAppsObject.onRetainNonConfigurationInstance();
		}
	}
	@Override
	public void onLocaleChanged(boolean localeChanged){
		if(mAllAppsObject != null){
			mAllAppsObject.onLocaleChanged(localeChanged);
		}
	}
	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data){
		if(mAllAppsObject != null && mAllAppsObject.onActivityResult(requestCode, resultCode, data)){
			return true;
		}
		return false;
	}	
	
	protected void doCommand(int nCmd){
		switch(nCmd){
		case QS_RET_CLOSE_ALLAPPS:
			closeAllApps(false);
			break;
		case QS_RET_SHOW_ALLAPPS:
			showAllApps(false);
			break;
		default:
			break;
		}
	}
	//@Override
	public boolean onClick(View v){
		//QsLog.LogD("BaseStyleObjectWidget::onClick(0)===");
		if(mAllAppsObject != null && mAllAppsObject.isAllAppsVisible()){
			int nRet = mAllAppsObject.onClick(v);
			if(nRet != QS_RET_FAIL){
				doCommand(nRet);
				return true;
			}
		}
		return false;
	}

	//@Override
	public boolean onLongClick(View v){
		if(mAllAppsObject != null && mAllAppsObject.isAllAppsVisible()){
			int nRet = mAllAppsObject.onLongClick(v);
			if(nRet != QS_RET_FAIL){
				doCommand(nRet);
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event){
		if(mAllAppsObject != null && mAllAppsObject.isAllAppsVisible()){
			return mAllAppsObject.onKeyUp(keyCode, event);
		}
		return false;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(mAllAppsObject != null && mAllAppsObject.isAllAppsVisible()){
			return mAllAppsObject.onKeyDown(keyCode, event);
		}
		return false;
	}
	@Override
	public Dialog onCreateDialog(int id){
		if(mAllAppsObject != null){
			return mAllAppsObject.onCreateDialog(id);
		}
		return null;
	}
	@Override
	public boolean onPrepareDialog(int id, Dialog dialog){
		if(mAllAppsObject != null && mAllAppsObject.onPrepareDialog(id, dialog)){
			return true;
		}
		return false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(mAllAppsObject != null && mAllAppsObject.onCreateOptionsMenu(menu)){
			return true;
		}
		return false;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(mAllAppsObject != null && mAllAppsObject.onPrepareOptionsMenu(menu)){
			return true;
		}
		return false;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(mAllAppsObject != null && mAllAppsObject.onOptionsItemSelected(item)){
			return true;
		}
		return false;
	}
	
	@Override
	public void release(){
		if(mAllAppsObject != null){
			mAllAppsObject.release();
			mAllAppsObject = null;
		}
		QsLog.LogE("BaseStyleObjectWidget::release()=========");
		mModel.setCallbacks(null);
		if(mDragController != null)
			mDragController.removeAllDropTargets();
		
		if(mWorkspace != null)
			mWorkspace.removeViews();
		
		if(mWorkspaceViewStub != null){
			//final ViewGroup v = (ViewGroup)mWorkspaceViewStub.getParent();
			//v.removeView(mWorkspaceViewStub);
			mWorkspaceViewStub.removeAllViewsInLayout();
			mWorkspaceViewStub = null;
		}
	}
}
