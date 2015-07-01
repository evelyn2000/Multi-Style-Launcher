/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.qshome;

import com.android.common.Search;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.LiveFolders;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Time;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.DataInputStream;

import android.os.ServiceManager;
import android.view.IWindowManager;

import com.android.qshome.LauncherAppWidgetHostView;
import com.android.qshome.R;
import com.android.qshome.ctrl.CellLayout;
import com.android.qshome.ctrl.DeleteZone;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.DropTarget;
import com.android.qshome.ctrl.FastBitmapDrawable;
import com.android.qshome.ctrl.Folder;
import com.android.qshome.ctrl.FolderIcon;
import com.android.qshome.ctrl.HandleView;
import com.android.qshome.ctrl.LiveFolderIcon;
import com.android.qshome.ctrl.UserFolder;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.FolderInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.model.LiveFolderInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.BaseStyleObjectWidget;
import com.android.qshome.style.defaults.StyleDefaultsApps;
import com.android.qshome.style.defaults.StyleDefautsWidget;
import com.android.qshome.style.droidics.StyleDroidIcsWidget;
import com.android.qshome.style.htc.StyleHtcWidget;
import com.android.qshome.style.samsung.StyleSamsungWidget;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.Utilities;
import com.android.qshome.util.ThemeStyle;


//jz for sms
import android.provider.Telephony.Mms;
import static com.google.android.mms.pdu.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.google.android.mms.pdu.PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF;
import android.provider.Telephony.Sms;
import android.graphics.drawable.BitmapDrawable;

import com.android.qshome.ctrl.BubbleTextViewIcs;

/**
 * Default launcher application.
 */
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener/*, LauncherModel.Callbacks*/, AllAppsView.Watcher {
	public static final String TAG = "Launcher";
    public static final boolean LOGD = false;

    public static final boolean PROFILE_STARTUP = false;
    public static final boolean DEBUG_WIDGETS = false;
    public static final boolean DEBUG_USER_INTERFACE = false;

    public static final int WALLPAPER_SCREENS_SPAN = 2;

    public static final int MENU_GROUP_ADD = 1;
    public static final int MENU_GROUP_WALLPAPER = MENU_GROUP_ADD + 1;

    public static final int MENU_ADD = Menu.FIRST + 1;
    public static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;
    public static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;
    public static final int MENU_NOTIFICATIONS = MENU_SEARCH + 1;
    public static final int MENU_SETTINGS = MENU_NOTIFICATIONS + 1;
    
    public static final int MENU_SWITCH_STYLE = MENU_SETTINGS + 1;

    public static final int REQUEST_CREATE_SHORTCUT = 1;
    public static final int REQUEST_CREATE_LIVE_FOLDER = 4;
    public static final int REQUEST_CREATE_APPWIDGET = 5;
    public static final int REQUEST_PICK_APPLICATION = 6;
    public static final int REQUEST_PICK_SHORTCUT = 7;
    public static final int REQUEST_PICK_LIVE_FOLDER = 8;
    public static final int REQUEST_PICK_APPWIDGET = 9;
    public static final int REQUEST_PICK_WALLPAPER = 10;

    public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    public static final int SCREEN_COUNT = 5;
    public static final int DEFAULT_SCREEN = 2;
    public static final int NUMBER_CELLS_X = Utilities.QS_ENABLE_WORKSPACE_5_5 ? 5 : 4;
    public static final int NUMBER_CELLS_Y = Utilities.QS_ENABLE_WORKSPACE_5_5 ? 5 : 4;

    public static final int DIALOG_CREATE_SHORTCUT = 1;
    public static final int DIALOG_RENAME_FOLDER = 2;

    public static final String PREFERENCES = "launcher.preferences";

    // Type: int
    public static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: boolean
    public static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";
    // Type: long
    public static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";
    // Type: int
    public static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    public static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
    // Type: int
    public static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
    // Type: int
    public static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
    // Type: int
    public static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
    // Type: int
    public static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
    // Type: int
    public static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
    // Type: int[]
    public static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";
    // Type: boolean
    public static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    public static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";

    public static final int APPWIDGET_HOST_ID = 1024;

    private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREEN;

    private final BroadcastReceiver mCloseSystemDialogsReceiver
            = new CloseSystemDialogsIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private DragController mDragController;
    private Workspace mWorkspace;

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private CellLayout.CellInfo mAddItemCellInfo;
    private CellLayout.CellInfo mMenuAddInfo;
    private final int[] mCellCoordinates = new int[2];
    private FolderInfo mFolderInfo;

    
    private Bundle mSavedState;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    public boolean mPaused = true;
    public boolean mRestoring;
    public boolean mWaitingForResult;
    public boolean mOnResumeNeedsLoad;

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;
    private IconCache mIconCache;

    private ArrayList<ItemInfo> mDesktopItems = new ArrayList<ItemInfo>();
    private static HashMap<Long, FolderInfo> mFolders = new HashMap<Long, FolderInfo>();

    private IWindowManager wm ;
    private float savedTransitionAnimationScale ;

    private BaseStyleObjectWidget mThemeWidgetObject;
    private ViewGroup mStyleChooserStub;
    private DragLayer mDragLayer;
    private Dialog mProgressDialog = null;
    //protected boolean mPortrait;
    private int mOrientation;
    
    public final static String QS_LAUNCHER_STYLE_WIDGET_KEY = "qslauncherwidgetstyle";
    public final static String QS_LAUNCHER_STYLE_APPS_KEY = "qslauncherappsstyle";
    private final static int MSG_SHOW_STYLE_CHOOSER = 300;
    
    public final static boolean QS_DISABLE_THEME_SWITCH = false;
    
    public ThemeStyle getSharedPreferencesWidgetStyle(){
    	
    	if(QS_DISABLE_THEME_SWITCH)
    		return ThemeStyle.Samsung;
    	
    	String style = PreferenceManager.getDefaultSharedPreferences(this).getString(QS_LAUNCHER_STYLE_WIDGET_KEY, "");
    	
    	//QsLog.LogD("getSharedPreferencesStyle()==style:"+style);
    	if(style.trim().length() > 0)
    		return ThemeStyle.valueOf(style);
    	return ThemeStyle.Unkown;
    }
    
    public ThemeStyle getSharedPreferencesAppsStyle(){
    	
    	String style = PreferenceManager.getDefaultSharedPreferences(this).getString(QS_LAUNCHER_STYLE_APPS_KEY, "");
    	
    	//QsLog.LogD("getSharedPreferencesStyle()==style:"+style);
    	if(style.trim().length() > 0)
    		return ThemeStyle.valueOf(style);
    	return ThemeStyle.Unkown;
    }
    
    public void saveSharedPreferencesStyle(ThemeStyle widgetStyle, ThemeStyle appStyle){
    	
    	android.content.SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putString(QS_LAUNCHER_STYLE_WIDGET_KEY, widgetStyle.toString());
    	editor.putString(QS_LAUNCHER_STYLE_APPS_KEY, appStyle.toString());
    	editor.commit();
    }
    
    public static void changeSharedPreferencesStyleByOutside(Context context, String style){
    	if(style == null)
    		return;
    	String widgetStyle = ThemeStyle.DroidIcs.toString();
    	if(!style.equalsIgnoreCase(widgetStyle)){

	    	widgetStyle = ThemeStyle.Samsung.toString();
	    	if(!style.equalsIgnoreCase(widgetStyle)){
	    		
	    		widgetStyle = ThemeStyle.Htc.toString();
	    		if(!style.equalsIgnoreCase(widgetStyle)){
	    			
	    			widgetStyle = ThemeStyle.Default.toString();
	        		if(!style.equalsIgnoreCase(widgetStyle)){
	        			QsLog.LogD("changeSharedPreferencesStyleByOutside(fail)==unkown style:"+style);
	        			return;
	        		}
	    		}//else{
		    	//	if(com.mediatek.featureoption.FeatureOption.Qs_Sub_Project_Name.startsWith("M005")){
		    	//		widgetStyle = ThemeStyle.DroidIcs.toString();
		    	//	}
		    	//}
	    	}//else{
	    		//if(com.mediatek.featureoption.FeatureOption.Qs_Sub_Project_Name.startsWith("M005")){
	    		//	widgetStyle = ThemeStyle.DroidIcs.toString();
	    		//}
	    	//}
    	}
    	
    	android.content.SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString(QS_LAUNCHER_STYLE_WIDGET_KEY, widgetStyle);
    	editor.putString(QS_LAUNCHER_STYLE_APPS_KEY, ThemeStyle.Unkown.toString());
    	editor.commit();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LauncherApplication app = ((LauncherApplication)getApplication());
        mModel = app.getModel();//setLauncher(this);
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();
        
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();
        
        //mPortrait = (getResources().getDisplayMetrics().widthPixels < getResources().getDisplayMetrics().heightPixels);
        
        mOrientation = getResources().getConfiguration().orientation;
        
        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing("/sdcard/launcher");
        }
        //QsLog.LogW("Launcher::onCreate(0)=====mRestoring:"+mRestoring+"==savedInstanceState:"+(savedInstanceState == null ? "null" : "valid"));
        //loadHotseats();
        checkForLocaleChange();
        setWallpaperDimension();

        //setContentView(R.layout.launcher);
        setContentView(R.layout.main);
        
        mSavedState = savedInstanceState;
        
        DragLayer dragLayer = mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        dragLayer.setDragController(mDragController);
        mStyleChooserStub = (ViewGroup)dragLayer.findViewById(R.id.qs_select_style_main_scrollview);
        if(QS_DISABLE_THEME_SWITCH){
        	init(ThemeStyle.DroidIcs);
        }else{
	        ThemeStyle nDefStyle = getSharedPreferencesWidgetStyle();
	
	        //QsLog.LogW("Launcher::onCreate(1)==style:"+nDefStyle+"==mRestoring:"+mRestoring);
	        if(nDefStyle == ThemeStyle.Unkown){
				try {
					String configStyle = getResources().getString(R.string.config_defaultTheme);
					if(configStyle != null && configStyle.trim().length() > 0){
						nDefStyle = ThemeStyle.valueOf(configStyle);
						
						if(nDefStyle != ThemeStyle.Unkown)
							saveSharedPreferencesStyle(nDefStyle, ThemeStyle.Unkown);
					}
				} catch (Resources.NotFoundException e) {
					
				}
	        }
	        //QsLog.LogW("Launcher::onCreate(2)==style:"+nDefStyle+"==mRestoring:"+mRestoring);
	        if(nDefStyle == ThemeStyle.Unkown)
	        	qsShowStyleChooser();
	        else
	        	init(nDefStyle);
        }
        
        
        //
        
        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }
        
        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);
        
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //filter.addAction("com.qishang.sms.UNREAD_COUNT_CHANGED");
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        
        //InitShowUnReadedSmsCount();
    }
    
    private void init(ThemeStyle nDefStyle){
    	//QsLog.LogD("Launcher::init()==nDefStyle:"+nDefStyle);
    	changeThemeStyle(nDefStyle, ThemeStyle.Unkown, false);
        
        lockAllApps();
        registerContentObservers();
        restoreState(mSavedState);

        if (!mRestoring) {
        	
        	//if(mSavedState == null)
        		showHomeLoadingDialog();
        	
        	mModel.setAllAppsDirty();
            mModel.startLoader(this, true);
        }
    }
    
    public boolean changeThemeStyle(ThemeStyle widgetStyle, 
    		ThemeStyle appStyle, boolean isReload){
    	boolean bRetWidget = false;
    	QsLog.LogW("Launcher::changeThemeStyle()==widgetStyle:"+widgetStyle
    			+"==mRestoring:"+mRestoring+"==mOnResumeNeedsLoad:"+mOnResumeNeedsLoad
    			+"==old:"+(mThemeWidgetObject == null ? "null":mThemeWidgetObject.getThemeStyle()));
    	if(mThemeWidgetObject == null || 
    			(mThemeWidgetObject.getThemeStyle() != widgetStyle && widgetStyle != ThemeStyle.Unkown))
    	{
    		boolean bIsFirst = (mThemeWidgetObject == null);
    		if(!bIsFirst && isReload) //if(!mRestoring && !mOnResumeNeedsLoad)
    			showHomeLoadingDialog();
    		mModel.stopLoader();
    		
    		BaseStyleObjectWidget tempWidget = null;
    		if(widgetStyle == ThemeStyle.Default){
    			if(appStyle != ThemeStyle.Unkown)
    				tempWidget = new StyleDefautsWidget(this, mModel, mIconCache, appStyle);
    			else
    				tempWidget = new StyleDefautsWidget(this, mModel, mIconCache);
    		}else if(widgetStyle == ThemeStyle.Htc){
    			tempWidget = new StyleHtcWidget(this, mModel, mIconCache);
    		}else if(widgetStyle == ThemeStyle.Samsung){
    			tempWidget = new StyleSamsungWidget(this, mModel, mIconCache);
    		}else if(widgetStyle == ThemeStyle.DroidIcs){
    			tempWidget = new StyleDroidIcsWidget(this, mModel, mIconCache);
    		}
    		
    		if(tempWidget != null)
    		{
    			mIconCache.changeAppStyle(widgetStyle);
    			Utilities.changeThemeStyle(this, widgetStyle, false);
    			
    			mDragController.clearDragListener();
    			mDragController.clearDropTargets();
    			mDesktopItems.clear();
    			
    			if(!bIsFirst){
    				mThemeWidgetObject.release();
    				mThemeWidgetObject = null;
    				
    				// first call application.init
    				mModel.setCallbacks(tempWidget);
    				setScreen(DEFAULT_SCREEN);
    				
    				setContentView(R.layout.main);
    				
    				DragLayer dragLayer = mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
    		        dragLayer.setDragController(mDragController);
    		        mStyleChooserStub = (ViewGroup)dragLayer.findViewById(R.id.qs_select_style_main_scrollview);
    			}
    			else{
    				((LauncherApplication)getApplication()).setCallbacks(tempWidget);
    			}
    			
    			mThemeWidgetObject = tempWidget;
    			tempWidget.initialize(mDesktopItems, mFolders, mAppWidgetManager, mAppWidgetHost);
    			
    			mThemeWidgetObject.onCreate(mDragLayer, mDragController);
            	//mWorkspace = mThemeWidgetObject.getWorkspace();
            	
    			bRetWidget = true;
    			
    			if(!bIsFirst && isReload){
    				 //mIconCache.flush();
    				 
    		    	 mWorkspaceLoading = true;
    		    	 if(bIsFirst)
    		    		 mModel.setAllAppsDirty();
    		         mModel.startLoader(this, true);
    		         mRestoring = false;
    		         mOnResumeNeedsLoad = false;
    			}
    		}
    	}

    	return bRetWidget;
    }
    
    public BaseStyleObjectWidget getStyleWidgetObject() {
        return mThemeWidgetObject;
    }
    
    public ThemeStyle getCurrentWidgetObjectStyle(){
    	if(mThemeWidgetObject != null)
    		return mThemeWidgetObject.getThemeStyle();
    	
    	return ThemeStyle.Unkown;
    }
    
    public void closeAllApps(boolean animated){
    	mThemeWidgetObject.closeAllApps(animated);
    }
    
    public boolean isScreenPortrait(){
    	return (mOrientation != Configuration.ORIENTATION_LANDSCAPE);
    }
    
    public void closeAllApps(){
    	mThemeWidgetObject.closeAllApps(false);
    }
    private ThemeStyle mTempSelectWidgetStyle;
    private ThemeStyle mTempSelectAppsStyle;
    private final static int QS_STYLE_CHECK_FLAG_WIDGET = 1;
    private final static int QS_STYLE_CHECK_FLAG_APPS = 2;
    public void qsShowStyleChooser(){
    	if(!QS_DISABLE_THEME_SWITCH){
	    	if(mStyleChooserStub != null && mStyleChooserStub.getVisibility() == View.VISIBLE){
	    		QsLog.LogD("qsShowStyleChooser(0)===visiable===");   
	    		return;
	    	}
	    	
	    	mTempSelectWidgetStyle = getSharedPreferencesWidgetStyle();
	    	mTempSelectAppsStyle = getSharedPreferencesAppsStyle();
	    	
	    	mStyleChooserStub.setVisibility(View.VISIBLE);
	    	mStyleChooserStub.setEnabled(true);
			ViewGroup chooser = getStyleChooserDialogChooserLayout();
			
			View btn = mStyleChooserStub.findViewById(R.id.qs_select_style_main_button);
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mTempSelectWidgetStyle == ThemeStyle.Unkown){
						Toast.makeText(Launcher.this, R.string.qs_select_fail_toast, Toast.LENGTH_SHORT)
						.show();
						
						return;
					}
					mStyleChooserStub.setEnabled(false);
					qsDoSelectStyle(mTempSelectWidgetStyle, ThemeStyle.Unkown);
				}
			});
			
			if(mTempSelectWidgetStyle == ThemeStyle.Unkown)
				mTempSelectWidgetStyle = ThemeStyle.Samsung;
			
			// default
			qsAddNewStyleInfo(chooser, ThemeStyle.Default, R.drawable.preview_default, "Default",
					ThemeStyle.Default, R.drawable.preview_default_a, "Default",
					((mTempSelectWidgetStyle == ThemeStyle.Default ? QS_STYLE_CHECK_FLAG_WIDGET : 0) | 
					(mTempSelectAppsStyle == ThemeStyle.Default ? QS_STYLE_CHECK_FLAG_APPS : 0)));
			
			//if(!com.mediatek.featureoption.FeatureOption.Qs_Sub_Project_Name.startsWith("M005")){
				// htc
				qsAddNewStyleInfo(chooser, ThemeStyle.Htc, R.drawable.preview_htc, "Style 1",
						ThemeStyle.Htc, R.drawable.preview_htc_a, "Style 1",
						((mTempSelectWidgetStyle == ThemeStyle.Htc ? QS_STYLE_CHECK_FLAG_WIDGET : 0) | 
								(mTempSelectAppsStyle == ThemeStyle.Htc ? QS_STYLE_CHECK_FLAG_APPS : 0)));
				// samsung
				qsAddNewStyleInfo(chooser, ThemeStyle.Samsung, R.drawable.preview_samsung, "Style 2",
						ThemeStyle.Samsung, R.drawable.preview_samsung_a, "Style 2",
						((mTempSelectWidgetStyle == ThemeStyle.Samsung ? QS_STYLE_CHECK_FLAG_WIDGET : 0) | 
								(mTempSelectAppsStyle == ThemeStyle.Samsung ? QS_STYLE_CHECK_FLAG_APPS : 0)));
			//}
			// droid ics
			qsAddNewStyleInfo(chooser, ThemeStyle.DroidIcs, R.drawable.preview_ics, "Ics",
					ThemeStyle.DroidIcs, R.drawable.preview_ics_a, "Ics",
					((mTempSelectWidgetStyle == ThemeStyle.DroidIcs ? QS_STYLE_CHECK_FLAG_WIDGET : 0) | 
							(mTempSelectAppsStyle == ThemeStyle.DroidIcs ? QS_STYLE_CHECK_FLAG_APPS : 0)));
			
			qsCheckOneStyle(mTempSelectWidgetStyle);
    	}
    }
    
    private void qsAddNewStyleInfo(ViewGroup chooser, 
    		final ThemeStyle widgetStyle, int widgeticon, String strWidget, 
    		final ThemeStyle appStyle, int appIcon, String strApp,
    		int nCheckValue){
    	
    	ViewGroup layout = (ViewGroup) mInflater.inflate(
				R.layout.launcher_style_chooser_line_merge, chooser, false);
		
		chooser.addView(layout);
		
		layout.setTag(widgetStyle);
		//TextView v = (TextView)layout.findViewById(R.id.preview_widget_img);//.getChildAt(0);
		TextView v = (TextView)layout.getChildAt(0);
		v.setCompoundDrawablesWithIntrinsicBounds( 
				getResources().getDrawable(widgeticon), null, getResources().getDrawable(appIcon), null);
		//v.setText(strWidget);
		//final View vImgWidget = layout.findViewById(R.id.preview_widget_img_check);
		//if((nCheckValue & QS_STYLE_CHECK_FLAG_WIDGET) > 0)
		//	vImgWidget.setVisibility(View.VISIBLE);
		v.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				qsCheckOneStyle(widgetStyle);
				//mTempSelectWidgetStyle = widgetStyle;
			}
		});

		v = (TextView)layout.getChildAt(1);
		v.setText(strWidget);
		v.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				qsCheckOneStyle(widgetStyle);
				//mTempSelectWidgetStyle = widgetStyle;
			}
		});
    }
    
    private void qsCheckOneStyle(View vCheck){
    	ViewGroup chooser = getStyleChooserDialogChooserLayout();
    	int nCount = chooser.getChildCount();
    	//QsLog.LogD("qsDoSelectStyle(0)====nCount:"+nCount);
    	
    	for(int i=0; i<nCount; i++){
    		ViewGroup vLine = (ViewGroup)chooser.getChildAt(i);
    		int nChildCount = vLine.getChildCount();
    		for(int j=0; j<nChildCount; j++){
    			View v = vLine.getChildAt(j);
	    		//QsLog.LogD("qsDoSelectStyle()====check:"+vCheck.getTag()+"==uncheck:"+v.getTag());
	    		if(vCheck.getTag().equals(v.getTag())){
	    			if(vCheck != v)
	    				v.setVisibility(View.GONE);
	    		}
    		}
    	}
    	
    	vCheck.setVisibility(View.VISIBLE);
    }
    
    private void qsCheckOneStyle(ThemeStyle style){
    	ViewGroup chooser = (ViewGroup)mStyleChooserStub.findViewById(R.id.qs_select_style_main_toplayout);
    	int nCount = chooser.getChildCount();
    	Drawable dbOn = getResources().getDrawable(R.drawable.btn_check_buttonless_on);
    	Drawable dbOff = getResources().getDrawable(R.drawable.btn_check_buttonless_off);
    	
    	//QsLog.LogD("qsDoSelectStyle(0)====nCount:"+nCount);
    	for(int i=0; i<nCount; i++){
    		ViewGroup vLine = (ViewGroup)chooser.getChildAt(i);
    		TextView v = (TextView)vLine.getChildAt(1);
    		if(style.equals(vLine.getTag())){
    			v.setCompoundDrawablesWithIntrinsicBounds(null, null, dbOn, null);
    		}
    		else{
    			v.setCompoundDrawablesWithIntrinsicBounds(null, null, dbOff, null);
    		}
    	}
    	
    	mTempSelectWidgetStyle = style;
    }
    
    private void qsDoSelectStyle(ThemeStyle widgetStyle, ThemeStyle appStyle){
    	
    	ThemeStyle oldstyle = getSharedPreferencesWidgetStyle();
    	ThemeStyle oldAppstyle = getSharedPreferencesAppsStyle();
    	//QsLog.LogD("qsDoSelectStyle()====oldstyle:"+oldstyle+"==oldAppstyle:"+oldAppstyle+"==widgetStyle:"+widgetStyle+"=appStyle:"+appStyle);
    	if(oldstyle != widgetStyle || appStyle != oldAppstyle){
    		saveSharedPreferencesStyle(widgetStyle, appStyle);
    		
    		if(oldstyle == ThemeStyle.Unkown)
    			init(widgetStyle);
    		else{
    			changeThemeStyle(widgetStyle, appStyle, true);
    		}
    	}
    	closeStyleChooser();
    }
    
    private ViewGroup getStyleChooserDialogChooserLayout(){
    	if(mStyleChooserStub != null)
    		return (ViewGroup)mStyleChooserStub.findViewById(R.id.qs_select_style_main_toplayout);
    	return null;
    }
    
    private boolean closeStyleChooser(){
    	if(mStyleChooserStub != null && mStyleChooserStub.getVisibility() == View.VISIBLE){

    		//QsLog.LogD("closeStyleChooser()==");
    		mStyleChooserStub.setVisibility(View.GONE);
    		ViewGroup chooser = getStyleChooserDialogChooserLayout();
    		chooser.removeAllViewsInLayout();
        	
        	return true;
		}
    	//QsLog.LogD("closeStyleChooser()=false=");
    	return false;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
	{ 
    	
    	boolean isLandscape = (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    	
    	//QsLog.LogW("onConfigurationChanged()====mOrientation:"+mOrientation
    	//		+"==new:"+newConfig.orientation+"==isLandscape:"+isLandscape);
    	
    	if(newConfig.orientation != mOrientation){
    		
    		mOrientation = newConfig.orientation;
    		mModel.stopLoader();
    		//mPortrait = (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    		
    		//int nCurrent = mWorkspace.getCurrentScreen();
    		setScreen(DEFAULT_SCREEN);
    		//mWorkspace.removeViews();
    		mDesktopItems.clear();
    		
    		mDragController.clearDragListener();
			mDragController.clearDropTargets();
			
    		setContentView(R.layout.main);
			
			DragLayer dragLayer = mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
	        dragLayer.setDragController(mDragController);
	        mStyleChooserStub = (ViewGroup)dragLayer.findViewById(R.id.qs_select_style_main_scrollview);

			mThemeWidgetObject.onCreate(mDragLayer, mDragController);

			//mWorkspace.setCurrentScreen(nCurrent);
        	//mModel.startLoader(this, true);
			mWorkspaceLoading = true;
	    	//if(bIsFirst)
	    	//	 mModel.setAllAppsDirty();
	        mModel.startLoader(this, false);
	        mRestoring = false;
	        mOnResumeNeedsLoad = false;
        	//QsLog.LogW("onConfigurationChanged(2)====CurrentScreen:"+mWorkspace.getCurrentScreen()
        	//		+"==old:"+nCurrent+"==mPaused:"+mPaused);
        	
        	//mWorkspace.postInvalidate();
    	}
    	super.onConfigurationChanged(newConfig);
    	
	}
    
    private void checkForLocaleChange() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        readConfiguration(this, localeConfiguration);

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = localeConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = localeConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = localeConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (localeChanged) {
            localeConfiguration.locale = locale;
            localeConfiguration.mcc = mcc;
            localeConfiguration.mnc = mnc;

            writeConfiguration(this, localeConfiguration);
            mIconCache.flush();
        }
        
        if(mThemeWidgetObject != null)
        	mThemeWidgetObject.onLocaleChanged(localeChanged);
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    public static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    private void setWallpaperDimension() {
        WallpaperManager wpm = (WallpaperManager)getSystemService(WALLPAPER_SERVICE);

        Display display = getWindowManager().getDefaultDisplay();
        boolean isPortrait = display.getWidth() < display.getHeight();

        final int width = isPortrait ? display.getWidth() : display.getHeight();
        final int height = isPortrait ? display.getHeight() : display.getWidth();
        wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingForResult = false;

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.

        if (resultCode == RESULT_OK && mAddItemCellInfo != null) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                    completeAddApplication(this, data, mAddItemCellInfo);
                    return;
                case REQUEST_PICK_SHORTCUT:
                    processShortcut(data);
                    return;
                case REQUEST_CREATE_SHORTCUT:
                    completeAddShortcut(data, mAddItemCellInfo);
                    return;
                case REQUEST_PICK_LIVE_FOLDER:
                    addLiveFolder(data);
                    return;
                case REQUEST_CREATE_LIVE_FOLDER:
                    completeAddLiveFolder(data, mAddItemCellInfo);
                    return;
                case REQUEST_PICK_APPWIDGET:
                    addAppWidget(data);
                    return;
                case REQUEST_CREATE_APPWIDGET:
                    completeAddAppWidget(data, mAddItemCellInfo);
                    return;
                case REQUEST_PICK_WALLPAPER:
                    // We just wanted the activity result here so we can clear mWaitingForResult
                	return;
            }
        } else if ((requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET) && resultCode == RESULT_CANCELED &&
                data != null) {
            // Clean up the appWidgetId if we canceled
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                return;
            }
        }
        
        if(mThemeWidgetObject != null && mThemeWidgetObject.onActivityResult(requestCode, resultCode, data))
        	return;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //QsLog.LogD("Launcher::onResume()=====mRestoring:"+mRestoring);
        // Disable animation while coming back to Home Screen for better user experience.
        wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        try {
            savedTransitionAnimationScale = wm.getAnimationScale(1) ;
            wm.setAnimationScale(1, 0.0f);
        }catch(Exception e){
            e.printStackTrace() ;
        }

        mPaused = false;
        if(mThemeWidgetObject != null){
        	
        	mThemeWidgetObject.onResume();
        	//QsLog.LogE("Launcher::onResume()=====mRestoring:"+mRestoring+"==mOnResumeNeedsLoad:"+mOnResumeNeedsLoad);
	        if (mRestoring || mOnResumeNeedsLoad) {
	            mWorkspaceLoading = true;
	            mModel.startLoader(this, true);
	            mRestoring = false;
	            mOnResumeNeedsLoad = false;
	        }
        }
        mDragController.cancelDrag();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        
        //QsLog.LogD("Launcher::onPause()=====mRestoring:"+mRestoring+"==mOnResumeNeedsLoad:"+mOnResumeNeedsLoad);
        // Leaving Homescreen. Restore animation option selected by user.
        wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        try {
            wm.setAnimationScale(1, savedTransitionAnimationScale);
        }catch(Exception e) {
            e.printStackTrace() ;
        }
        
        if(mThemeWidgetObject != null)
        	mThemeWidgetObject.onPause();

//        dismissPreview(mPreviousView);
//        dismissPreview(mNextView);
        mDragController.cancelDrag();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
        mModel.stopLoader();
//        if(mThemeAppsObject != null)
//        	mThemeAppsObject.onRetainNonConfigurationInstance();
        
        if(mThemeWidgetObject != null)
        	mThemeWidgetObject.onRetainNonConfigurationInstance();
        
        //mAllAppsGrid.surrender();
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open.  So don't bother
    /*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            final InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            inputManager.hideSoftInputFromWindow(lp.token, 0, new android.os.ResultReceiver(new
                        android.os.Handler()) {
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            Log.d(TAG, "ResultReceiver got resultCode=" + resultCode);
                        }
                    });
            Log.d(TAG, "called hideSoftInputFromWindow from onWindowFocusChanged");
        }
    }
    */

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        
//        if(mThemeAppsObject != null && mThemeAppsObject.onKeyDown(keyCode, event))
//        	return true;
        
        if(mThemeWidgetObject != null && mThemeWidgetObject.onKeyDown(keyCode, event))
        	return true;
        
        if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }
        QsLog.LogD("Launcher::restoreState(0)==mRestoring:"+mRestoring+"=mOnResumeNeedsLoad:"+mOnResumeNeedsLoad);
        if(mThemeWidgetObject != null)
        	mThemeWidgetObject.restoreState(savedState);

        final int addScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
        if (addScreen > -1) {
            mAddItemCellInfo = new CellLayout.CellInfo();
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            addItemCellInfo.valid = true;
            addItemCellInfo.screen = addScreen;
            addItemCellInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            addItemCellInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            addItemCellInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            addItemCellInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            addItemCellInfo.findVacantCellsFromOccupied(
                    savedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
            mRestoring = true;
            QsLog.LogE("Launcher::restoreState(1)====mOnResumeNeedsLoad:"+mOnResumeNeedsLoad);
        }

        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, mFolders, id, false);
            mRestoring = true;
            QsLog.LogE("Launcher::restoreState(2)====mOnResumeNeedsLoad:"+mOnResumeNeedsLoad);
        }
        QsLog.LogD("Launcher::restoreState(end)==mRestoring:"+mRestoring+"=addScreen:"+addScreen+"=renameFolder:"+renameFolder);
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        
        
    }
    
    public int getWorkspaceScreenLayout(){
    	int nResId = 0;
    	if(mThemeWidgetObject != null){
    		nResId = mThemeWidgetObject.getWorkspaceScreenLayout();
    	}
    	
    	if(nResId == 0)
			nResId = R.layout.workspace_screen;
    	return nResId;
    }
    
    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    public View createShortcut(ShortcutInfo info) {
        return mWorkspace.createShortcut(info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    public View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
//    	if(parent == null){
//    		QsLog.LogE("Launcher::createShortcut()==parent is null==");
//    		return null;
//    	}
    	View v = mInflater.inflate(layoutResId, parent, false);
    	/*if(v instanceof BubbleTextViewIcs){
    		
    		((BubbleTextViewIcs)v).applyFromShortcutInfo(info, mIconCache);
    		
    	}else */if(v instanceof TextView){
    		
	        TextView favorite = (TextView)v;
	
	        favorite.setCompoundDrawablesWithIntrinsicBounds(null,
	                new FastBitmapDrawable(info.getIcon(mIconCache)),
	                null, null);
	        favorite.setText(info.title);
	        
    	} else if(v instanceof ImageView){
    		
    		ImageView favorite = (ImageView)v;
    		favorite.setImageBitmap(((ShortcutInfo)info).getIcon(mIconCache));
    		
    	}
    	
        v.setTag(info);
        v.setOnClickListener(this);

        return v;
    }
    
    
    
    

    /**
     * Add an application shortcut to the workspace.
     *
     * @param data The intent describing the application.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    public void completeAddApplication(Context context, Intent data, CellLayout.CellInfo cellInfo) {
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        final ShortcutInfo info = mModel.getShortcutInfo(context.getPackageManager(),
                data, context);

        if (info != null) {
            info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            info.container = ItemInfo.NO_ID;
            mWorkspace.addApplicationShortcut(info, cellInfo, isWorkspaceLocked());
        } else {
            Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
        }
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, CellLayout.CellInfo cellInfo) {
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        final ShortcutInfo info = mModel.addShortcut(this, data, cellInfo, false);

        if (!mRestoring) {
            final View view = createShortcut(info);
            mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY, 1, 1,
                    isWorkspaceLocked());
        }
    }


    /**
     * Add a widget to the workspace.
     *
     * @param data The intent describing the appWidgetId.
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(Intent data, CellLayout.CellInfo cellInfo) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        if (LOGD) Log.d(TAG, "dumping extras content=" + extras.toString());

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
        int[] spans = layout.rectToCell(appWidgetInfo.minWidth, appWidgetInfo.minHeight);

        // Try finding open space on Launcher screen
        final int[] xy = mCellCoordinates;
        if (!findSlot(cellInfo, xy, spans[0], spans[1])) {
            if (appWidgetId != -1) mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId);
        launcherInfo.spanX = spans[0];
        launcherInfo.spanY = spans[1];

        LauncherModel.addItemToDatabase(this, launcherInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                mWorkspace.getCurrentScreen(), xy[0], xy[1], false, false);

        if (!mRestoring) {
            mDesktopItems.add(launcherInfo);

            // Perform actual inflation because we're live
            launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

            launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setTag(launcherInfo);

            mWorkspace.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1],
                    launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        mDesktopItems.remove(launcherInfo);
        launcherInfo.hostView = null;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public void closeSystemDialogs() {
        getWindow().closeAllPanels();

        try {
            dismissDialog(DIALOG_CREATE_SHORTCUT);
            // Unlock the workspace if the dialog was showing
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is fine
        }

        try {
            dismissDialog(DIALOG_RENAME_FOLDER);
            // Unlock the workspace if the dialog was showing
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is fine
        }

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        QsLog.LogD("Launcher::onNewIntent()====");
        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

//            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
//                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//            boolean allAppsVisible = isAllAppsVisible();
//            if (!mWorkspace.isDefaultScreenShowing()) {
//                mWorkspace.moveToDefaultScreen(alreadyOnHome && !allAppsVisible);
//            }
//            
//            if(mThemeWidgetObject != null)
//            	mThemeWidgetObject.closeAllApps(alreadyOnHome && allAppsVisible);

            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
        
        if(mThemeWidgetObject != null)
        	mThemeWidgetObject.onNewIntent(intent);
    }
    
    public void restoreInstanceState() {
        // Do not call super here
    	if (mSavedInstanceState != null) {
            super.onRestoreInstanceState(mSavedInstanceState);
            mSavedInstanceState = null;
        }
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Do not call super here
        mSavedInstanceState = savedInstanceState;
        QsLog.LogD("Launcher::onRestoreInstanceState()====");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if(mWorkspace == null)
    		return;
    	QsLog.LogD("Launcher::onSaveInstanceState()====");
        //outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getCurrentScreen());

        final ArrayList<Folder> folders = mWorkspace.getOpenFolders();
        if (folders.size() > 0) {
            final int count = folders.size();
            long[] ids = new long[count];
            for (int i = 0; i < count; i++) {
                final FolderInfo info = folders.get(i).getInfo();
                ids[i] = info.id;
            }
            outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
        } else {
            super.onSaveInstanceState(outState);
        }

        // TODO should not do this if the drawer is currently closing.
        /*if (isAllAppsVisible()) {
            outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
        }*/

        if (mAddItemCellInfo != null && mAddItemCellInfo.valid && mWaitingForResult) {
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            final CellLayout layout = (CellLayout) mWorkspace.getChildAt(addItemCellInfo.screen);

            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, addItemCellInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, addItemCellInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, addItemCellInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, addItemCellInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, addItemCellInfo.spanY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X, layout.getCountX());
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y, layout.getCountY());
            outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
                   layout.getOccupiedCells());
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }
        
        if(mThemeWidgetObject != null){
        	mThemeWidgetObject.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QsLog.LogE("Launcher::onDestroy()====");
        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }

        TextKeyListener.getInstance().release();

        mModel.stopLoader();

        unbindDesktopItems();

        if(mThemeWidgetObject != null){
        	mThemeWidgetObject.onDestroy();
        }
        //DeInitShowUnReadedSmsCount();
        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);
        
        dismissHomeLoadingDialog();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) mWaitingForResult = true;
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {

    	if(mThemeWidgetObject != null)
    		mThemeWidgetObject.closeAllApps(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
            clearTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString(Search.SOURCE, "launcher-search");
        }

        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchManager.startSearch(initialQuery, selectInitialQuery, getComponentName(),
            appSearchData, globalSearch);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	//QsLog.LogD("onCreateOptionsMenu(0)==size:"+menu.size());   
    	
//        if (isWorkspaceLocked() || (mStyleChooserStub != null && mStyleChooserStub.getVisibility() == View.VISIBLE)) {
//            return false;
//        }
        //QsLog.LogD("onCreateOptionsMenu(1)==v:"+mStyleChooserStub.getVisibility());        
        
        if(mThemeWidgetObject != null && mThemeWidgetObject.onCreateOptionsMenu(menu))
        	return super.onCreateOptionsMenu(menu);
        
        menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add)
                .setIcon(android.R.drawable.ic_menu_add)
                .setAlphabeticShortcut('A');
        if(!QS_DISABLE_THEME_SWITCH){
	        menu.add(0, MENU_SWITCH_STYLE, 0, R.string.qs_set_navigate_style)
		        .setIcon(com.android.internal.R.drawable.ic_menu_slideshow)
		        .setAlphabeticShortcut('S');
        }
        menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
                 .setIcon(android.R.drawable.ic_menu_gallery)
                 .setAlphabeticShortcut('W');
        menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
                .setIcon(android.R.drawable.ic_search_category_default)
                .setAlphabeticShortcut(SearchManager.MENU_KEY);
        /*menu.add(0, MENU_NOTIFICATIONS, 0, R.string.menu_notifications)
                .setIcon(R.drawable.ic_menu_notifications)
                .setAlphabeticShortcut('N');*/
        
        final Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
                .setIcon(android.R.drawable.ic_menu_preferences).setAlphabeticShortcut('P')
                .setIntent(settings);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
       
        if (isWorkspaceLocked() || (mStyleChooserStub != null && mStyleChooserStub.getVisibility() == View.VISIBLE)) {
            return false;
        }
        
        if(mThemeWidgetObject != null && mThemeWidgetObject.onPrepareOptionsMenu(menu)){
        	//QsLog.LogE("Launcher::onPrepareOptionsMenu(0)=========");
        	return false;
        }
        	
        // If all apps is animating, don't show the menu, because we don't know
        // which one to show.
//        if (mAllAppsGrid.isVisible() && !mAllAppsGrid.isOpaque()) {
//            return false;
//        }
//
//        // Only show the add and wallpaper options when we're not in all apps.
//        boolean visible = !mAllAppsGrid.isOpaque();
//        menu.setGroupVisible(MENU_GROUP_ADD, visible);
//        menu.setGroupVisible(MENU_GROUP_WALLPAPER, visible);
//
//        // Disable add if the workspace is full.
//        if (visible) {
            mMenuAddInfo = mWorkspace.findAllVacantCells(null);
            //menu.setGroupEnabled(MENU_GROUP_ADD, mMenuAddInfo != null && mMenuAddInfo.valid);
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                addItems();
                return true;
            case MENU_WALLPAPER_SETTINGS:
                startWallpaper();
                return true;
            case MENU_SEARCH:
                onSearchRequested();
                //if(mWorkspace != null && mWorkspace.getCurrentScreen() == 0)
                //	JzTestSaveAllWidgets();
                return true;
            /*case MENU_NOTIFICATIONS:
                showNotifications();
                return true;*/
            case MENU_SWITCH_STYLE:
            	qsShowStyleChooser();
            	return true;
        }
        
        if(mThemeWidgetObject != null && mThemeWidgetObject.onOptionsItemSelected(item))
        	return true;
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        if(mWorkspace != null && mWorkspace.getCurrentScreen() == 0)
             JzTestSaveAllWidgets();
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }
    
    public void setWorkspaceLocked(boolean bIs) {
        mWorkspaceLoading = bIs;
    }
    
    public boolean getWorkspaceLocked() {
        return mWorkspaceLoading;
    }

    private void addItems() {
    	if(mThemeWidgetObject != null)
    		mThemeWidgetObject.closeAllApps(true);
        showAddDialog(mMenuAddInfo);
    }

    public void addAppWidget(Intent data) {
        // TODO: catch bad widget exception when sent
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        if (appWidget.configure != null) {
            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidget.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
        }
    }

    public void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    public void addLiveFolder(Intent intent) {
        // Handle case where user selected "Folder"
        String folderName = getResources().getString(R.string.group_folder);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (folderName != null && folderName.equals(shortcutName)) {
            addFolder();
        } else {
            startActivityForResult(intent, REQUEST_CREATE_LIVE_FOLDER);
        }
    }

    public void addFolder() {
    	addFolder(mWorkspace);
    }
    
    public void addFolder(Workspace workspace){
    	
    	if(mThemeWidgetObject == null)
    		return;
    	
    	UserFolderInfo folderInfo = new UserFolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        CellLayout.CellInfo cellInfo = mAddItemCellInfo;
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        // Update the model
        LauncherModel.addItemToDatabase(this, folderInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                mWorkspace.getCurrentScreen(), cellInfo.cellX, cellInfo.cellY, false, mWorkspace.getIsApplicationMode());
        mFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder = FolderIcon.fromXml(mThemeWidgetObject.getFolderIconLayout(), this,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), folderInfo);
        mWorkspace.addInCurrentScreen(newFolder,
                cellInfo.cellX, cellInfo.cellY, 1, 1, isWorkspaceLocked());
    }

    public void removeFolder(FolderInfo folder) {
        mFolders.remove(folder.id);
    }

    private void completeAddLiveFolder(Intent data, CellLayout.CellInfo cellInfo) {
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        final LiveFolderInfo info = addLiveFolder(this, data, cellInfo, false);

        if (!mRestoring) {
            final View view = LiveFolderIcon.fromXml(R.layout.live_folder_icon, this,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
            mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY, 1, 1,
                    isWorkspaceLocked());
        }
    }

    public static LiveFolderInfo addLiveFolder(Context context, Intent data,
            CellLayout.CellInfo cellInfo, boolean notify) {

        Intent baseIntent = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
        String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);

        Drawable icon = null;
        Intent.ShortcutIconResource iconResource = null;

        Parcelable extra = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
        if (extra != null && extra instanceof Intent.ShortcutIconResource) {
            try {
                iconResource = (Intent.ShortcutIconResource) extra;
                final PackageManager packageManager = context.getPackageManager();
                Resources resources = packageManager.getResourcesForApplication(
                        iconResource.packageName);
                final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                icon = resources.getDrawable(id);
            } catch (Exception e) {
                Log.w(TAG, "Could not load live folder icon: " + extra);
            }
        }

        if (icon == null) {
            icon = context.getResources().getDrawable(R.drawable.ic_launcher_folder);
        }

        final LiveFolderInfo info = new LiveFolderInfo();
        info.icon = Utilities.createIconBitmap(icon, context);
        info.title = name;
        info.iconResource = iconResource;
        info.uri = data.getData();
        info.baseIntent = baseIntent;
        info.displayMode = data.getIntExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                LiveFolders.DISPLAY_MODE_GRID);

        LauncherModel.addItemToDatabase(context, info, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                cellInfo.screen, cellInfo.cellX, cellInfo.cellY, notify, false);
        mFolders.put(info.id, info);

        return info;
    }

    private boolean findSingleSlot(CellLayout.CellInfo cellInfo) {
        final int[] xy = new int[2];
        if (findSlot(cellInfo, xy, 1, 1)) {
            cellInfo.cellX = xy[0];
            cellInfo.cellY = xy[1];
            return true;
        }
        return false;
    }
    
    private boolean findSingleSlot(CellLayout.CellInfo cellInfo, Workspace workspace) {
        final int[] xy = new int[2];
        if (findSlot(cellInfo, xy, 1, 1, workspace)) {
            cellInfo.cellX = xy[0];
            cellInfo.cellY = xy[1];
            return true;
        }
        return false;
    }

    private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX, int spanY) {
        return this.findSlot(cellInfo, xy, spanX, spanY, mWorkspace);
    }
    
    private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX, int spanY, Workspace workspace) {
        if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
            boolean[] occupied = mSavedState != null ?
                    mSavedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS) : null;
            cellInfo = workspace.findAllVacantCells(occupied);
            if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
                Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /*private void showNotifications() {
        final StatusBarManager statusBar = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
        if (statusBar != null) {
            statusBar.expand();
        }
    }*/

    private void startWallpaper() {
    	if(mThemeWidgetObject != null)
    		mThemeWidgetObject.closeAllApps(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        // NOTE: Adds a configure option to the chooser if the wallpaper supports it
        //       Removed in Eclair MR1
//        WallpaperManager wm = (WallpaperManager)
//                getSystemService(Context.WALLPAPER_SERVICE);
//        WallpaperInfo wi = wm.getWallpaperInfo();
//        if (wi != null && wi.getSettingsActivity() != null) {
//            LabeledIntent li = new LabeledIntent(getPackageName(),
//                    R.string.configure_wallpaper, 0);
//            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
//            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
//        }
        startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (SystemProperties.getInt("debug.launcher2.dumpstate", 0) != 0) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
    	if(mThemeWidgetObject != null){
    		
    		if(closeStyleChooser()){
    			return;
    		}
    			
	        if (mThemeWidgetObject.isAllAppsVisible()) {
        		mThemeWidgetObject.closeAllApps(true);
	        } else {
	            closeFolder();
	        }

        	mThemeWidgetObject.onBackPressed();
    	}
    }

    private void closeFolder() {
        Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            closeFolder(folder);
        }
    }

    public void closeFolder(Folder folder) {
        folder.getInfo().opened = false;
        ViewGroup parent = (ViewGroup) folder.getParent();
        if (parent != null) {
            parent.removeView(folder);
            if (folder instanceof DropTarget) {
                // Live folders aren't DropTargets.
                mDragController.removeDropTarget((DropTarget)folder);
            }
        }
        folder.onClose();
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        mAppWidgetHost.startListening();
    }

    /**
     * Go through the and disconnect any of the callbacks in the drawables and the views or we
     * leak the previous Home screen on orientation change.
     */
    private void unbindDesktopItems() {
        for (ItemInfo item: mDesktopItems) {
            item.unbind();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
    	//QsLog.LogD("Launcher::onClick(0)=====");
    	if(mThemeWidgetObject != null && mThemeWidgetObject.onClick(v)){
       		return;
        }
    	
        Object tag = v.getTag();
        //QsLog.LogD("Launcher::onClick(1)====="+tag.toString());
        if(tag == null) return;
        
        if (tag instanceof ShortcutInfo) {
            // Open shortcut
            final Intent intent = ((ShortcutInfo) tag).intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));
            startActivitySafely(intent, tag);
        } else if (tag instanceof FolderInfo) {
            handleFolderClick((FolderInfo) tag);
        } 
    }
    
    public void onClickSearchButton(View v){
    	v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    	onSearchRequested();
    }
    
    public void onClickVoiceButton(View v){
    	v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }
    
    public void startApplicationDetailsActivity(ComponentName componentName) {
        String packageName = componentName.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    public void startApplicationUninstallActivity(ApplicationInfo appInfo) {

    	//final ResolveInfo resolveInfo = getPackageManager().resolveActivity(appInfo.intent, 0);
    	//if(resolveInfo != null){
    		if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0) {
	    	//if ((resolveInfo.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
	            // System applications cannot be installed. For now, show a toast explaining that.
	            // We may give them the option of disabling apps this way.
	            int messageId = R.string.uninstall_system_app_text;
	            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
	        } else {
	            String packageName = appInfo.componentName.getPackageName();
	            String className = appInfo.componentName.getClassName();
	            Intent intent = new Intent(
	                    Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	            startActivity(intent);
	        }
    	//}
    }

    public boolean startActivitySafely(Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
        
        return false;
    }
    
    public void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    private void handleFolderClick(FolderInfo folderInfo) {
        if (!folderInfo.opened) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderInfo);
        } else {
            // Find the open folder...
            Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getScreenForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentScreen()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderInfo);
                }
            }
        }
    }

    /**
     * Opens the user fodler described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    private void openFolder(FolderInfo folderInfo) {
        Folder openFolder;

        if (folderInfo instanceof UserFolderInfo) {
            openFolder = UserFolder.fromXml(this);
        } else if (folderInfo instanceof LiveFolderInfo) {
            openFolder = com.android.qshome.ctrl.LiveFolder.fromXml(this, folderInfo);
        } else {
            return;
        }

        openFolder.setDragController(mDragController);
        openFolder.setLauncher(this);

        openFolder.bind(folderInfo);
        folderInfo.opened = true;

        mWorkspace.addInScreen(openFolder, folderInfo.screen, 0, 0, 4, 4);
        openFolder.onOpen();
    }

    public boolean onLongClick(View v) {
        if (isWorkspaceLocked()) {
            return false;
        }
        
        if(mThemeWidgetObject != null && mThemeWidgetObject.onLongClick(v))
        	return true;

        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }

        CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();
        // This happens when long clicking an item with the dpad/trackball
        if (cellInfo == null) {
            return true;
        }
        
        if ((v.getParent() instanceof Workspace) && mWorkspace.allowLongPress()) {
            if (cellInfo.cell == null) {
                if (cellInfo.valid) {
                    // User long pressed on empty space
                    mWorkspace.setAllowLongPress(false);
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    showAddDialog(cellInfo);
                }
            } else {
                if (!(cellInfo.cell instanceof Folder)) {
                    // User long pressed on an item
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    mWorkspace.startDrag(cellInfo);
                }
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public void dismissPreview(final View v) {
        final PopupWindow window = (PopupWindow) v.getTag();
        if (window != null) {
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                public void onDismiss() {
                    ViewGroup group = (ViewGroup) v.getTag(R.id.workspace);
                    int count = group.getChildCount();
                    for (int i = 0; i < count; i++) {
                        ((ImageView) group.getChildAt(i)).setImageDrawable(null);
                    }
                    ArrayList<Bitmap> bitmaps = (ArrayList<Bitmap>) v.getTag(R.id.icon);
                    for (Bitmap bitmap : bitmaps) bitmap.recycle();

                    v.setTag(R.id.workspace, null);
                    v.setTag(R.id.icon, null);
                    window.setOnDismissListener(null);
                }
            });
            window.dismiss();
        }
        v.setTag(null);
    }

    public void showPreviews(View anchor) {
    		dismissPreview(anchor);
        showPreviews(anchor, 0, mWorkspace.getChildCount());
    }

    private void showPreviews(final View anchor, int start, int end) {
        final Resources resources = getResources();
        final Workspace workspace = mWorkspace;

        CellLayout cell = ((CellLayout) workspace.getChildAt(start));
        
        float max = workspace.getChildCount();
        
        final Rect r = new Rect();
        resources.getDrawable(R.drawable.preview_background).getPadding(r);
        int extraW = (int) ((r.left + r.right) * max);
        int extraH = r.top + r.bottom;

        int aW = cell.getWidth() - extraW;
        float w = aW / max;

        int width = cell.getWidth();
        int height = cell.getHeight();
        int x = cell.getLeftPadding();
        int y = cell.getTopPadding();
        width -= (x + cell.getRightPadding());
        height -= (y + cell.getBottomPadding());

        float scale = w / width;

        int count = end - start;

        final float sWidth = width * scale;
        float sHeight = height * scale;

        LinearLayout preview = new LinearLayout(this);

        PreviewTouchHandler handler = new PreviewTouchHandler(anchor);
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(count);

        for (int i = start; i < end; i++) {
            ImageView image = new ImageView(this);
            cell = (CellLayout) workspace.getChildAt(i);

            final Bitmap bitmap = Bitmap.createBitmap((int) sWidth, (int) sHeight,
                    Bitmap.Config.ARGB_8888);

            final Canvas c = new Canvas(bitmap);
            c.scale(scale, scale);
            c.translate(-cell.getLeftPadding(), -cell.getTopPadding());
            cell.dispatchDraw(c);

            image.setBackgroundDrawable(resources.getDrawable(R.drawable.preview_background));
            image.setImageBitmap(bitmap);
            image.setTag(i);
            image.setOnClickListener(handler);
            image.setOnFocusChangeListener(handler);
            image.setFocusable(true);
            if (i == mWorkspace.getCurrentScreen()) image.requestFocus();

            preview.addView(image,
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            bitmaps.add(bitmap);            
        }

        final PopupWindow p = new PopupWindow(this);
        p.setContentView(preview);
        p.setWidth((int) (sWidth * count + extraW));
        p.setHeight((int) (sHeight + extraH));
        p.setAnimationStyle(R.style.AnimationPreview);
        p.setOutsideTouchable(true);
        p.setFocusable(true);
        p.setBackgroundDrawable(new ColorDrawable(0));
        p.showAsDropDown(anchor, 0, 0);

        p.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                dismissPreview(anchor);
            }
        });

        anchor.setTag(p);
        anchor.setTag(R.id.workspace, preview);
        anchor.setTag(R.id.icon, bitmaps);        
    }

    public class PreviewTouchHandler implements View.OnClickListener, Runnable, View.OnFocusChangeListener {
        private final View mAnchor;

        public PreviewTouchHandler(View anchor) {
            mAnchor = anchor;
        }

        public void onClick(View v) {
            mWorkspace.snapToScreen((Integer) v.getTag());
            v.post(this);
        }

        public void run() {
            dismissPreview(mAnchor);            
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mWorkspace.snapToScreen((Integer) v.getTag());
            }
        }
    }

    public Workspace getWorkspace() {
    	// jz
    	if(mWorkspace == null && mThemeWidgetObject != null)
    		mWorkspace = mThemeWidgetObject.getWorkspace();
    	// jz change end
    	
        return mWorkspace;
    }
    
    public DragLayer getDragLayer() {
        return mDragLayer;
    }
    
    public void setWorkspace(Workspace w){
    	mWorkspace = w;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CREATE_SHORTCUT:
                return new CreateShortcut().createDialog();
            case DIALOG_RENAME_FOLDER:
                return new RenameFolder().createDialog();
        }
        Dialog dlg = null;
        
        if(mThemeWidgetObject != null && (dlg = mThemeWidgetObject.onCreateDialog(id)) != null)
        	return dlg;

        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_CREATE_SHORTCUT:
                break;
            case DIALOG_RENAME_FOLDER:
                if (mFolderInfo != null) {
                    EditText input = (EditText) dialog.findViewById(R.id.folder_name);
                    final CharSequence text = mFolderInfo.title;
                    input.setText(text);
                    input.setSelection(0, text.length());
                    input.addTextChangedListener(new AddDialogTextWatcher(dialog));
                    ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(input.length() == 0 ? false:true);
                }
                break;
        }
                
        if(mThemeWidgetObject != null && mThemeWidgetObject.onPrepareDialog(id, dialog))
        	return;
    }
    
    private class AddDialogTextWatcher implements TextWatcher {
    	private Dialog dlg;
    	
    	AddDialogTextWatcher(Dialog dialog) {
    		dlg = dialog;
    	}
        public void afterTextChanged(Editable s) {
                ((AlertDialog)dlg).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.length() == 0 ? false:true);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }   

    public void showRenameDialog(FolderInfo info) {
        mFolderInfo = info;
        mWaitingForResult = true;
        showDialog(DIALOG_RENAME_FOLDER);
    }

    private void showAddDialog(CellLayout.CellInfo cellInfo) {
        mAddItemCellInfo = cellInfo;
        mWaitingForResult = true;
        showDialog(DIALOG_CREATE_SHORTCUT);
    }

    private void pickShortcut() {
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
                        R.drawable.ic_launcher_application));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_shortcut));
        pickIntent.putExtras(bundle);

        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }

    private class RenameFolder {
        private EditText mInput;

        Dialog createDialog() {
            final View layout = View.inflate(Launcher.this, R.layout.rename_folder, null);
            mInput = (EditText) layout.findViewById(R.id.folder_name);

            AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
            builder.setIcon(0);
            builder.setTitle(getString(R.string.rename_folder_title));
            builder.setCancelable(true);
            builder.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    cleanup();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel_action),
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cleanup();
                    }
                }
            );
            builder.setPositiveButton(getString(R.string.rename_action),
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        changeFolderName(mWorkspace);
                    }
                }
            );
            builder.setView(layout);

            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    mWaitingForResult = true;
                    mInput.requestFocus();
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(mInput, 0);
                }
            });

            return dialog;
        }

        private void changeFolderName(Workspace workspace) {
            final String name = mInput.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                // Make sure we have the right folder info
                mFolderInfo = mFolders.get(mFolderInfo.id);
                mFolderInfo.title = name;
                LauncherModel.updateItemInDatabase(Launcher.this, mFolderInfo, workspace.getIsApplicationMode());

                if (mWorkspaceLoading) {
                    lockAllApps();
                    mModel.startLoader(Launcher.this, false);
                } else {
                    final FolderIcon folderIcon = (FolderIcon)
                            workspace.getViewForTag(mFolderInfo);
                    if (folderIcon != null) {
                        folderIcon.setText(name);
                        getWorkspace().requestLayout();
                    } else {
                        lockAllApps();
                        mWorkspaceLoading = true;
                        mModel.startLoader(Launcher.this, false);
                    }
                }
            }
            cleanup();
        }

        private void cleanup() {
            dismissDialog(DIALOG_RENAME_FOLDER);
            mWaitingForResult = false;
            mFolderInfo = null;
        }
    }

    // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
    public boolean isAllAppsVisible() {
        //return (mAllAppsGrid != null) ? mAllAppsGrid.isVisible() : false;
    	return (mThemeWidgetObject != null) ? mThemeWidgetObject.isAllAppsVisible() : false;
    }

    // AllAppsView.Watcher
    public void zoomed(float zoom) {
        if (zoom == 1.0f) {
            mWorkspace.setVisibility(View.GONE);
        }
    }

//    public void showAllApps(boolean animated) {
//        mAllAppsGrid.zoom(1.0f, animated);
//
//        ((View) mAllAppsGrid).setFocusable(true);
//        ((View) mAllAppsGrid).requestFocus();
//        
//        // TODO: fade these two too
//        mDeleteZone.setVisibility(View.GONE);
//        mHandleView.setVisibility(View.GONE);
//        mPreviousView.setVisibility(View.GONE);
//        mNextView.setVisibility(View.GONE);
//	hotseatLeft.setVisibility(View.GONE);
//	hotseatRight.setVisibility(View.GONE);
//    }
//
//    /**
//     * Things to test when changing this code.
//     *   - Home from workspace
//     *          - from center screen
//     *          - from other screens
//     *   - Home from all apps
//     *          - from center screen
//     *          - from other screens
//     *   - Back from all apps
//     *          - from center screen
//     *          - from other screens
//     *   - Launch app from workspace and quit
//     *          - with back
//     *          - with home
//     *   - Launch app from all apps and quit
//     *          - with back
//     *          - with home
//     *   - Go to a screen that's not the default, then all
//     *     apps, and launch and app, and go back
//     *          - with back
//     *          -with home
//     *   - On workspace, long press power and go back
//     *          - with back
//     *          - with home
//     *   - On all apps, long press power and go back
//     *          - with back
//     *          - with home
//     *   - On workspace, power off
//     *   - On all apps, power off
//     *   - Launch an app and turn off the screen while in that app
//     *          - Go back with home key
//     *          - Go back with back key  TODO: make this not go to workspace
//     *          - From all apps
//     *          - From workspace
//     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
//     *          - From all apps
//     *          - From the center workspace
//     *          - From another workspace
//     */
//    public void closeAllApps(boolean animated) {
//        if (mAllAppsGrid.isVisible()) {
//            mWorkspace.setVisibility(View.VISIBLE);
//            mAllAppsGrid.zoom(0.0f, animated);
//            ((View)mAllAppsGrid).setFocusable(false);
//            mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
//
//            mHandleView.setVisibility(View.VISIBLE);
//            mPreviousView.setVisibility(View.VISIBLE);
//            mNextView.setVisibility(View.VISIBLE);
//	    hotseatLeft.setVisibility(View.VISIBLE);
//	    hotseatRight.setVisibility(View.VISIBLE);
//        }
//    }

    public void lockAllApps() {
        // TODO
    	if(mThemeWidgetObject != null)
    		mThemeWidgetObject.lockAllApps();
    }

    public void unlockAllApps() {
        // TODO
    	if(mThemeWidgetObject != null)
    		mThemeWidgetObject.unlockAllApps();
    }

    /**
     * Displays the shortcut creation dialog and launches, if necessary, the
     * appropriate activity.
     */
    private class CreateShortcut implements DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener, DialogInterface.OnDismissListener,
            DialogInterface.OnShowListener {

        private AddAdapter mAdapter;

        Dialog createDialog() {
            mAdapter = new AddAdapter(Launcher.this);

            final AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
            builder.setTitle(getString(R.string.menu_item_add_item));
            builder.setAdapter(mAdapter, this);

            builder.setInverseBackgroundForced(true);

            AlertDialog dialog = builder.create();
            dialog.setOnCancelListener(this);
            dialog.setOnDismissListener(this);
            dialog.setOnShowListener(this);

            return dialog;
        }

        public void onCancel(DialogInterface dialog) {
            mWaitingForResult = false;
            cleanup();
        }

        public void onDismiss(DialogInterface dialog) {
        }

        private void cleanup() {
            try {
                dismissDialog(DIALOG_CREATE_SHORTCUT);
            } catch (Exception e) {
                // An exception is thrown if the dialog is not visible, which is fine
            }
        }

        /**
         * Handle the action clicked in the "Add to home" dialog.
         */
        public void onClick(DialogInterface dialog, int which) {
            Resources res = getResources();
            cleanup();

            switch (which) {
                case AddAdapter.ITEM_SHORTCUT: {
                    // Insert extra item to handle picking application
                    pickShortcut();
                    break;
                }

                case AddAdapter.ITEM_APPWIDGET: {
                    int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();

                    Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                    pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    // start the pick activity
                    startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
                    break;
                }

                case AddAdapter.ITEM_LIVE_FOLDER: {
                    // Insert extra item to handle inserting folder
                    Bundle bundle = new Bundle();

                    ArrayList<String> shortcutNames = new ArrayList<String>();
                    shortcutNames.add(res.getString(R.string.group_folder));
                    bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

                    ArrayList<ShortcutIconResource> shortcutIcons =
                            new ArrayList<ShortcutIconResource>();
                    shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
                            R.drawable.ic_launcher_folder));
                    bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

                    Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                    pickIntent.putExtra(Intent.EXTRA_INTENT,
                            new Intent(LiveFolders.ACTION_CREATE_LIVE_FOLDER));
                    pickIntent.putExtra(Intent.EXTRA_TITLE,
                            getText(R.string.title_select_live_folder));
                    pickIntent.putExtras(bundle);

                    startActivityForResult(pickIntent, REQUEST_PICK_LIVE_FOLDER);
                    break;
                }

                case AddAdapter.ITEM_WALLPAPER: {
                    startWallpaper();
                    break;
                }
            }
        }

        public void onShow(DialogInterface dialog) {
            mWaitingForResult = true;            
        }
    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	if(intent.getAction().equals("com.qishang.sms.UNREAD_COUNT_CHANGED"))
			{
				//QsLog.LogD("=QsHome====com.qishang.sms.UNREAD_COUNT_CHANGED=======");
				//updateSmsIcon();
				updateSmsIconByMessage();
				return;
			}
        	
            closeSystemDialogs();
            String reason = intent.getStringExtra("reason");
            if (!"homekey".equals(reason)) {
                boolean animate = true;
                if (mPaused || "lock".equals(reason)) {
                    animate = false;
                }
                if(mThemeWidgetObject != null)
                	mThemeWidgetObject.closeAllApps(animate);
            }
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }
    
    public void showHomeLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new Dialog(this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setContentView(R.layout.progressbar);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }
    
    public void dismissHomeLoadingDialog(){
    	if (mProgressDialog != null) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                // We catch exception here, because have no impact on user
                Log.e(TAG, "Exception when Dialog.dismiss()...");
            } finally {
                mProgressDialog = null;
            }
        }
    }
    
    public void onFinishBindingItems() {

    	if(!mOnResumeNeedsLoad){
    	if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
            }

            final long[] userFolders = mSavedState.getLongArray(RUNTIME_STATE_USER_FOLDERS);
            if (userFolders != null) {
                for (long folderId : userFolders) {
                    final FolderInfo info = mFolders.get(folderId);
                    if (info != null) {
                        openFolder(info);
                    }
                }
                final Folder openFolder = mWorkspace.getOpenFolder();
                if (openFolder != null) {
                    openFolder.requestFocus();
                }
            }

            mSavedState = null;
        }

        if (mSavedInstanceState != null) {
            super.onRestoreInstanceState(mSavedInstanceState);
            mSavedInstanceState = null;
        }
    	}

        mWorkspaceLoading = false;
        
        //updateSmsIconByMessage();
        
        dismissHomeLoadingDialog();
        
    }
//    /**
//     * If the activity is currently paused, signal that we need to re-run the loader
//     * in onResume.
//     *
//     * This needs to be called from incoming places where resources might have been loaded
//     * while we are paused.  That is becaues the Configuration might be wrong
//     * when we're not running, and if it comes back to what it was when we
//     * were paused, we are not restarted.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     *
//     * @return true if we are currently paused.  The caller might be able to
//     * skip some work in that case since we will come back again.
//     */
//    public boolean setLoadOnResume() {
//        if (mPaused) {
//            Log.i(TAG, "setLoadOnResume");
//            mOnResumeNeedsLoad = true;
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public int getCurrentWorkspaceScreen() {
//        if (mWorkspace != null) {
//            return mWorkspace.getCurrentScreen();
//        } else {
//            return SCREEN_COUNT / 2;
//        }
//    }
//
//    /**
//     * Refreshes the shortcuts shown on the workspace.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void startBinding() {
//        final Workspace workspace = mWorkspace;
//        int count = workspace.getChildCount();
//        for (int i = 0; i < count; i++) {
//            // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
//            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
//        }
//
//        if (DEBUG_USER_INTERFACE) {
//            android.widget.Button finishButton = new android.widget.Button(this);
//            finishButton.setText("Finish");
//            workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);
//
//            finishButton.setOnClickListener(new android.widget.Button.OnClickListener() {
//                public void onClick(View v) {
//                    finish();
//                }
//            });
//        }
//    }
//
//    /**
//     * Bind the items start-end from the list.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
//
//		setLoadOnResume();
//
//		final Workspace workspace = mWorkspace;
//
//        for (int i=start; i<end; i++) {
//            final ItemInfo item = shortcuts.get(i);
//            mDesktopItems.add(item);
//            switch (item.itemType) {
//                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
//                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
//                    final View shortcut = createShortcut((ShortcutInfo)item);
//                    workspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1,
//                            false);
//                    break;
//                case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
//                    final FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
//                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
//                            (UserFolderInfo) item);
//                    workspace.addInScreen(newFolder, item.screen, item.cellX, item.cellY, 1, 1,
//                            false);
//                    break;
//                case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
//                    final FolderIcon newLiveFolder = LiveFolderIcon.fromXml(
//                            R.layout.live_folder_icon, this,
//                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
//                            (LiveFolderInfo) item);
//                    workspace.addInScreen(newLiveFolder, item.screen, item.cellX, item.cellY, 1, 1,
//                            false);
//                    break;
//            }
//        }
//
//        workspace.requestLayout();
//    }
//
//    /**
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void bindFolders(HashMap<Long, FolderInfo> folders) {
//        setLoadOnResume();
//        mFolders.clear();
//        mFolders.putAll(folders);
//    }
//
//    /**
//     * Add the views for a widget to the workspace.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void bindAppWidget(LauncherAppWidgetInfo item) {
//        setLoadOnResume();
//
//        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
//        if (DEBUG_WIDGETS) {
//            Log.d(TAG, "bindAppWidget: " + item);
//        }
//        final Workspace workspace = mWorkspace;
//
//        final int appWidgetId = item.appWidgetId;
//        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
//        if (DEBUG_WIDGETS) {
//            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
//        }
//
//        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
//
//        item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
//        item.hostView.setTag(item);
//
//        workspace.addInScreen(item.hostView, item.screen, item.cellX,
//                item.cellY, item.spanX, item.spanY, false);
//
//        workspace.requestLayout();
//
//        mDesktopItems.add(item);
//
//        if (DEBUG_WIDGETS) {
//            Log.d(TAG, "bound widget id="+item.appWidgetId+" in "
//                    + (SystemClock.uptimeMillis()-start) + "ms");
//        }
//    }
//
//    /**
//     * Callback saying that there aren't any more items to bind.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void finishBindingItems() {
//        setLoadOnResume();
//
//        if (mSavedState != null) {
//            if (!mWorkspace.hasFocus()) {
//                mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
//            }
//
//            final long[] userFolders = mSavedState.getLongArray(RUNTIME_STATE_USER_FOLDERS);
//            if (userFolders != null) {
//                for (long folderId : userFolders) {
//                    final FolderInfo info = mFolders.get(folderId);
//                    if (info != null) {
//                        openFolder(info);
//                    }
//                }
//                final Folder openFolder = mWorkspace.getOpenFolder();
//                if (openFolder != null) {
//                    openFolder.requestFocus();
//                }
//            }
//
//            mSavedState = null;
//        }
//
//        if (mSavedInstanceState != null) {
//            super.onRestoreInstanceState(mSavedInstanceState);
//            mSavedInstanceState = null;
//        }
//
//        mWorkspaceLoading = false;
//    }
//    
//    
//
//    /**
//     * Add the icons for all apps.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
//        //mAllAppsGrid.setApps(apps);
//    }
//
//    /**
//     * A package was installed.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
//        //removeDialog(DIALOG_CREATE_SHORTCUT);
//        //mAllAppsGrid.addApps(apps);
//    }
//
//    /**
//     * A package was updated.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
//        //removeDialog(DIALOG_CREATE_SHORTCUT);
//        //mWorkspace.updateShortcuts(apps);
//        //mAllAppsGrid.updateApps(apps);
//    }
//
//    /**
//     * A package was uninstalled.
//     *
//     * Implementation of the method from LauncherModel.Callbacks.
//     */
//    public void bindAppsRemoved(ArrayList<ApplicationInfo> apps) {
//        //removeDialog(DIALOG_CREATE_SHORTCUT);
//        //mWorkspace.removeItems(apps);
//        //mAllAppsGrid.removeApps(apps);
//    }
    
    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "mDesktopItems.size=" + mDesktopItems.size());
        Log.d(TAG, "mFolders.size=" + mFolders.size());
        mModel.dumpState();
        //mAllAppsGrid.dumpState();
        Log.d(TAG, "END launcher2 dump state");
    }
    
    
	///////////////////////////////////////////////////////////////////////
	static final String MMSPACKAGENAME = "com.android.mms";
    static final String MMSCALSSNAME = "com.android.mms.ui.ConversationList";
    private static final int MESSAGE_UPDATE_NEWMSG_ICON = 100;
    private static final int MESSAGE_UPDATE_CURRENT_PAGE_ICON = 101;
    private static int mPrevSmsUnReadCount = 0;
    private static int mPrevIconCount = 0;
    private static Bitmap mSmsOrignalIcon = null;
    private static Bitmap mSmsIconNew = null;
	private final ContentObserver mSmsObserver = new QsMessageChangeObserver();
	private final ContentObserver mMmsObserver = new QsMessageChangeObserver();
	
	private void InitShowUnReadedSmsCount()
	{
		initSmsOraginalIcon();

		updateSmsIconByMessage();
        
		getContentResolver().registerContentObserver(Sms.CONTENT_URI, true, mSmsObserver);
		getContentResolver().registerContentObserver(Mms.CONTENT_URI, true, mMmsObserver);
	}
	
	private void DeInitShowUnReadedSmsCount()
	{	
		getContentResolver().unregisterContentObserver(mSmsObserver);
		getContentResolver().unregisterContentObserver(mMmsObserver);
	}
	
	private void initSmsOraginalIcon()
	{
		final PackageManager manager = getPackageManager();
		///final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
		//final ActivityInfo activityInfo = resolveInfo.activityInfo;
//		try {
//			final ActivityInfo activityInfo = manager.getActivityInfo(new ComponentName(MMSPACKAGENAME, MMSCALSSNAME), 0);
//			mSmsOrignalIcon = Utilities.createIconBitmap(activityInfo.loadIcon(manager), this);
//		} catch (PackageManager.NameNotFoundException e) {
//            Log.d("QiShang", "getSmsOraginalIcon() fail:" +e);
//        }
	}
	
    private class QsMessageChangeObserver extends ContentObserver {
        public QsMessageChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
             // update the sms icon ....
            //updateSmsIcon();
        	updateSmsIconByMessage();
        }
    }
    
    private class LauncherHander extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_UPDATE_NEWMSG_ICON: 
				//QsLog.LogD("LauncherHander::MESSAGE_UPDATE_NEWMSG_ICON===");
				updateSmsIcon(false, false);
				break;
			case MESSAGE_UPDATE_CURRENT_PAGE_ICON:
				updateSmsIcon(true, true);
				break;
			}
		}
    }
    
    private Handler mLauncherHander = new LauncherHander();

    /**
     * create mms icon with unread msg num on top left corner.add by zf -
     * QiShang
     */
    
    public static Bitmap createMmsIconWithIcon(int num, Bitmap icon, Context context) {
    	
        Bitmap numIcon = createNumIcon(num, context);

        int w = icon.getWidth();
        int h = icon.getHeight();

        //Log.v("QiShang", "unread sms num is: " + mPrevSmsUnReadCount+"==w:"+w+"==h:"+h);

        Bitmap Ret_Icon = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(Ret_Icon);

        canvas.drawBitmap(icon, 0, 0, null);

        // canvas.drawText(unReadNum, w - txtW - 3, txtH + 3, paint);
        if (numIcon != null) {
            canvas.drawBitmap(numIcon, w - numIcon.getWidth(), h - numIcon.getHeight(), null);
        }
        else
        {
        	Log.v("QiShang", "createMmsIconWithIcon()==numIcon is null==");
        }

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return Ret_Icon;
    }

    public static int[] numDrawable = {
            R.drawable.num_0, R.drawable.num_1, R.drawable.num_2, R.drawable.num_3,
            R.drawable.num_4, R.drawable.num_5, R.drawable.num_6, R.drawable.num_7,
            R.drawable.num_8, R.drawable.num_9
    };

    public static Bitmap createNumIcon(int num, Context context) {
        // ignore num >= 100
        if (num <= 0 || num >= 100)
            return null;
        Bitmap bk = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.round))
                .getBitmap();
        int w = bk.getWidth();
        int h = bk.getHeight();
        Bitmap numIcon = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(numIcon);
        canvas.drawBitmap(bk, 0, 0, null);

        int tenPos = num / 10;
        int unitPos = num % 10;
        //Log.v("QiShang", "icon num is : " + tenPos + "," + unitPos+"==w:"+w+"=h:"+h);
        Bitmap tenBit = ((BitmapDrawable) context.getResources().getDrawable(numDrawable[tenPos]))
                .getBitmap();
        Bitmap unitBit = ((BitmapDrawable) context.getResources().getDrawable(numDrawable[unitPos]))
                .getBitmap();
        if (tenPos <= 0) {
            canvas.drawBitmap(unitBit, (w - unitBit.getWidth()) / 2, (h - unitBit.getHeight()) / 2,
                    null);
        } else {
            canvas.drawBitmap(tenBit, (w - tenBit.getWidth() - unitBit.getWidth()) / 2,
                    (h - tenBit.getHeight()) / 2, null);
            canvas.drawBitmap(unitBit,
                    (w - tenBit.getWidth() - unitBit.getWidth()) / 2 + tenBit.getWidth(),
                    (h - unitBit.getHeight()) / 2, null);
        }

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return numIcon;
    }
    
    // jz 
    private static final String NEW_INCOMING_SM_CONSTRAINT =
        "(" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_INBOX
        + " AND " + Sms.SEEN + " = 0)";
    
    private static final String NEW_INCOMING_MM_CONSTRAINT =
        "(" + Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_INBOX
        + " AND " + Mms.SEEN + "=0"
        + " AND (" + Mms.MESSAGE_TYPE + "=" + MESSAGE_TYPE_NOTIFICATION_IND
        + " OR " + Mms.MESSAGE_TYPE + "=" + MESSAGE_TYPE_RETRIEVE_CONF + "))";
    
    private static final String[] SMS_STATUS_PROJECTION = new String[] {
        Sms.THREAD_ID };
    
    private static final String[] MMS_STATUS_PROJECTION = new String[] {
        Mms.THREAD_ID };

    public static int getUnReadMsgCount(Context context) {
    	
        int unReadMsgCount = 0;//Uri.parse("content://sms/inbox")
        Cursor cursor = context.getContentResolver().query(Sms.CONTENT_URI, 
        		SMS_STATUS_PROJECTION,
        		NEW_INCOMING_SM_CONSTRAINT, null, null);
        
        if (cursor != null) {
        	unReadMsgCount = cursor.getCount();
            cursor.close();
        }
        
        cursor = context.getContentResolver().query(Mms.CONTENT_URI,
                MMS_STATUS_PROJECTION, NEW_INCOMING_MM_CONSTRAINT,
                null, null);
        
        if (cursor != null) {
        	unReadMsgCount += cursor.getCount();
            cursor.close();
        }
        
        return unReadMsgCount;
    }
    
    private void updateSmsIcon(CellLayout currentScreen, Drawable icon)
    {
    	View child = null;
    	int count = currentScreen.getChildCount();
    	for (int i = 0; i < count; i++) {
            child = currentScreen.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof ShortcutInfo) {
            	ShortcutInfo info = (ShortcutInfo) tag;
                if (MMSPACKAGENAME.equals(info.intent.getComponent().getPackageName())
                        && MMSCALSSNAME.equals(info.intent.getComponent().getClassName())) {
                	
                    ((TextView) child).setCompoundDrawablesWithIntrinsicBounds(null,
                    		icon, null, null);
                }
            }
        }
    }
    
    public static Bitmap getUnReadSmsCountIcon(String packetname, String classname)
    {
    	if (MMSPACKAGENAME.equals(packetname) && MMSCALSSNAME.equals(classname)) {
    		return mSmsIconNew;
    	}
    	
    	return null;
    }
    
    public static Bitmap getUnReadSmsCountIcon(ComponentName componentName)
    {   	
    	return getUnReadSmsCountIcon(componentName.getPackageName(), componentName.getClassName());
    }
    
    public static boolean checkIsSmsComponentName(ComponentName componentName){
    	if (MMSPACKAGENAME.equals(componentName.getPackageName()) 
    			&& MMSCALSSNAME.equals(componentName.getClassName())) {
    		return true;
    	}
    	return false;
    }

    public void updateSmsIconByMessage(){
    	//Log.v("QiShang", "updateSmsIconByMessage()===");
    	mLauncherHander.removeMessages(MESSAGE_UPDATE_CURRENT_PAGE_ICON);
    	mLauncherHander.removeMessages(MESSAGE_UPDATE_NEWMSG_ICON);
        mLauncherHander.sendEmptyMessageDelayed(MESSAGE_UPDATE_NEWMSG_ICON, 300);
    }
    
    public void updateCurrentSmsIconByMessage(){
    	//Log.v("QiShang", "updateCurrentSmsIconByMessage()===");
    	mLauncherHander.removeMessages(MESSAGE_UPDATE_CURRENT_PAGE_ICON);
        mLauncherHander.sendEmptyMessageDelayed(MESSAGE_UPDATE_CURRENT_PAGE_ICON, 300);
    }    
    private void updateSmsIcon(boolean bOnlyIcon, boolean curPage) {
    	if(mSmsOrignalIcon == null)
    	{
    		Log.v("QiShang", "updateSmsIcon()=mSmsOrignalIcon is null==");
    		//getSmsOraginalIcon();
    		return;
    	}
    	
    	if(!bOnlyIcon || mSmsIconNew == null){
    		
	    	int nCount = getUnReadMsgCount(this);
	    	//Log.v("QiShang", "updateSmsIcon()=new:" + nCount + "==old:" + mPrevSmsUnReadCount+"==mWorkspaceLoading:"+mWorkspaceLoading);	        
	        if(mPrevIconCount != nCount){
	        	mPrevIconCount = nCount;
		        if(nCount == 0)
		        	mSmsIconNew = mSmsOrignalIcon;
		        else
		        	mSmsIconNew = createMmsIconWithIcon(nCount, mSmsOrignalIcon, this);
	        }
    	}
    	
        if(mSmsIconNew == null)
        {
        	Log.v("QiShang", "updateSmsIcon()=mSmsIconNew is null==");
        	return;
        }
        
        //if(mModel.isDesktopLoaded())
        if(!mWorkspaceLoading && (mPrevSmsUnReadCount != mPrevIconCount) || curPage)
        {
        	mPrevSmsUnReadCount = mPrevIconCount;
        	
        	mWorkspace.clearChildrenCache();
        	int screenCount = mWorkspace.getChildCount();
            int ncurScreen = mWorkspace.getCurrentScreen();
            
            Drawable icon = new FastBitmapDrawable(mSmsIconNew);
            
            updateSmsIcon((CellLayout) mWorkspace.getChildAt(ncurScreen), icon);
            if(!curPage){
	            for(int screen = 0; screen < ncurScreen; screen++)
	            {
	            	updateSmsIcon((CellLayout) mWorkspace.getChildAt(screen), icon);
	            }
	            
	            for(int screen = ncurScreen+1; screen < screenCount; screen++)
	            {
	            	updateSmsIcon((CellLayout) mWorkspace.getChildAt(screen), icon);
	            }
            }
        }
        else
    	{
    		Log.d("QiShang", "updateSmsIcon()==isDesktopLoaded is false==");
    	}
        
//        mModel.changeSmsShortcutIcon(new ComponentName(
//    			MMSPACKAGENAME,
//    			MMSCALSSNAME), mSmsIconNew);
        //mWorkspace.invalidate();
        //Log.v("QiShang", "updateSmsIcon(end)====="+ mPrevSmsUnReadCount);
    }
    ////////////////////////////////////////////////////////////////////////////////////
    
    
    private String JzGetItemInfoString(ItemInfo info, boolean bIsShowSpan) {
		String strWrite = "launcher:screen=\"" + info.screen + "\" \r\n"
				+ "launcher:x=\"" + info.cellX + "\" \r\n" + "launcher:y=\""
				+ info.cellY + "\" \r\n";

		if (bIsShowSpan) {
			strWrite += "launcher:spanX=\"" + info.spanX + "\" \r\n"
					+ "launcher:spanY=\"" + info.spanY + "\" \r\n";
		}
		return strWrite;
	}

	private String JzGetChildViewWriteString(View child) {
		String strWrite = "";
		Object tag = child.getTag();

		//if (child instanceof LauncherAppWidgetHostView) {
		if(tag instanceof LauncherAppWidgetInfo){

			LauncherAppWidgetInfo launcherInfo = (LauncherAppWidgetInfo) tag;
			// strWrite =
			// "customwidget==screen:"+launcherInfo.screen+",x:"+launcherInfo.cellX+",y:"+launcherInfo.cellY+
			// ",spanX:"+launcherInfo.spanX+",spanY:"+launcherInfo.spanY+",appWidgetId:"+launcherInfo.appWidgetId;
			// strWrite = "customwidget==" + JzGetItemInfoString(launcherInfo) +
			// ",appWidgetId:"+launcherInfo.appWidgetId;

			AppWidgetProviderInfo info = ((LauncherAppWidgetHostView) child)
					.getAppWidgetInfo();
			if (info != null) {
				strWrite = "<appwidget \r\n " + "launcher:packageName=\""
						+ info.provider.getPackageName() + "\" \r\n"
						+ "launcher:className=\""
						+ info.provider.getClassName() + "\" \r\n";
				// strWrite += ",compantname:"+info.provider.toString();
			} else {
				// strWrite += ",compantname is unknow";
				strWrite = "<!--  compantname is unknow \r\n"
						+ JzGetItemInfoString(launcherInfo, true) + " -->";
				return strWrite;
			}

			strWrite += JzGetItemInfoString(launcherInfo, true) + "/>\r\n";
		} else if (child instanceof Folder) {
			Folder f = (Folder) child;

			FolderInfo folderInfo = (FolderInfo) tag;// f.getInfo();
			strWrite = "<!--  Folder=="
					+ JzGetItemInfoString(folderInfo, false) + ", title:"
					+ folderInfo.title + " -->";
		} else if (tag instanceof ApplicationInfo) {
			ApplicationInfo info = (ApplicationInfo) tag;
			Intent intent = info.intent;
			ComponentName name = intent.getComponent();

			//if (name == null) {
			if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
				strWrite = "<shortcut \r\n" + "launcher:title=\"" + info.title
						+ "\" \r\n" + "launcher:uri=\"" + intent.toUri(0)
						+ "\" \r\n";

				// strWrite = "shortcut==" + JzGetItemInfoString(info, false) +
				// ", title:"+info.title + ", uri:"+intent.toUri(0);
			} else {
				strWrite = "<favorite \r\n" + "launcher:packageName=\""
						+ name.getPackageName() + "\" \r\n"
						+ "launcher:className=\"" + name.getClassName()
						+ "\" \r\n";
				// strWrite = "favorite==" + JzGetItemInfoString(info, false) +
				// ", title:"+info.title + ", component:"+name;
			}

			strWrite += JzGetItemInfoString(info, false) + "/>\r\n";
		} else if(tag instanceof ShortcutInfo){
			
			ShortcutInfo info = (ShortcutInfo) tag;
			Intent intent = info.intent;
			ComponentName name = intent.getComponent();
			
			if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION){
				strWrite = "<favorite \r\n" + "launcher:packageName=\""
					+ name.getPackageName() + "\" \r\n"
					+ "launcher:className=\"" + name.getClassName()
					+ "\" \r\n";
			}else{
				strWrite = "<shortcut \r\n" + "launcher:title=\"" + info.title
					+ "\" \r\n" + "launcher:uri=\"" + intent.toUri(0)
					+ "\" \r\n";
			}
			strWrite += JzGetItemInfoString(info, false) + "/>\r\n";
			
			//Log.d("QsHtcLauncher",
			//		"Launcher::JzGetChildViewWriteString()==ShortcutInfo item type===");
		}else {
		
			Log.d("QsLog",
					"Launcher::JzGetChildViewWriteString()==unknow item type===");
		}

		return strWrite;
	}

	private void JzTestSaveAllWidgets() {
		//Log.d("QsHtcLauncher", "Launcher::JzTestSaveAllWidgets()==start===");
		// jz for get user launcher configuare

		try {

			//long now = System.currentTimeMillis();
			//Time curTime = new Time();
			//curTime.set(now);

			final String status = Environment.getExternalStorageState();
			if(!status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
				android.util.Log.d("QsLog", "JzTestSaveAllWidgets()===no sdcard====");
				return;
			}
			
			String strFileNameString = "qs_default_workspace.xml";
			File outputfile = new File(Environment.getExternalStorageDirectory(), strFileNameString);
	    	if(outputfile.exists())
	    	{
	    		outputfile.delete();
	    	}
	    	
			FileOutputStream qsfout = new FileOutputStream(outputfile);
			//this.openFileOutput(strFileNameString, Context.MODE_WORLD_WRITEABLE); // new
													// FileOutputStream(qsfile);
			String strWrite = "";
			Log.d("QsLog", "Launcher::JzTestSaveAllWidgets()=======");
			if (qsfout != null) {
				// strWrite =
				// "============start======"+curTime.toString()+"==================\r\n";
				if(mWorkspace != null){
					strWrite = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
							+ "<favorites xmlns:launcher=\"http://schemas.android.com/apk/res/com.android.qshome\">\r\n";
					qsfout.write(strWrite.getBytes());
	
					int nCellLayoutCount = mWorkspace.getChildCount();
					for (int i = 0; i < nCellLayoutCount; i++) {
						CellLayout screen = (CellLayout) mWorkspace.getChildAt(i);
						int count = screen.getChildCount();
	
						for (int j = 0; j < count; j++) {
							View child = screen.getChildAt(j);
							if (child == null) {
								Log.d("QsHtcLauncher",
										"Launcher::JzTestSaveAllWidgets()==child:"
												+ j + " is null===");
								continue;
							}
							strWrite = JzGetChildViewWriteString(child);
	
							if (strWrite != "") {
								strWrite += "\r\n";
								qsfout.write(strWrite.getBytes());
							}
						}
					}
	
					strWrite = "</favorites>\r\n";
					qsfout.write(strWrite.getBytes());
				}
				
				qsfout.close();

			} else {
				Log.d("QsLog",
						"Launcher::JzTestSaveAllWidgets()==qsfout is null===");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block

			Log.d("QsLog",
					"Launcher::JzTestSaveAllWidgets()==open file error:"
							+ e.getMessage() + "==");
			//e.printStackTrace();
		}
		// //////////////////////////////////////////////////////////////////////////////
	}
    
}
