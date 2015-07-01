package com.android.qshome.style.defaults;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.DeleteZone;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.HandleView;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.FolderInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.BaseStyleObjectWidget;
import com.android.qshome.style.IBaseStyleInterfaceWidget;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.Utilities;
import com.android.qshome.util.ThemeStyle;

import com.android.qshome.R;

public class StyleDefautsWidget extends BaseStyleObjectWidget{

	private DeleteZone mDeleteZone;
    private HandleView mHandleView;
    
	private ImageView mPreviousView;
    private ImageView mNextView;
    private ImageView hotseatLeft;
    private ImageView hotseatRight;
    
	// Hotseats (quick-launch icons next to AllApps)
    //private static final int NUM_HOTSEATS = 2;
//    private String[] mHotseatConfig = null;
//    private Intent[] mHotseats = null;
//    private Drawable[] mHotseatIcons = null;
//    private CharSequence[] mHotseatLabels = null;
    
    public StyleDefautsWidget(Launcher context, LauncherModel model, IconCache iconCache){
		super(context, model, iconCache, ThemeStyle.Unkown);
	}
    
	public StyleDefautsWidget(Launcher context, LauncherModel model, IconCache iconCache, ThemeStyle appStyle){
		super(context, model, iconCache, appStyle);
	}
	@Override
	public ThemeStyle getThemeStyle(){
		return ThemeStyle.Default;
	}
		
	public int getThemePreviewImage(){
		return 0;
	}
	
	public int getThemeTitleResourceId(){
		return 0;
	}
	@Override
	protected BaseStyleObjectApps getBaseStyleObjectApps(ThemeStyle appStyle){
		if(appStyle == ThemeStyle.Default || appStyle == ThemeStyle.Unkown){
			return new StyleDefaultsApps(mLauncher, mModel, mIconCache, this);
		}
		return super.getBaseStyleObjectApps(appStyle);
	}
	
	public void onCreate(DragLayer dragLayer, DragController dragController){
		//loadHotseats();
		
		ViewStub stub = (ViewStub)dragLayer.findViewById(R.id.stub_workspace_style_default);
		mWorkspaceViewStub = (ViewGroup)stub.inflate();
		final ViewGroup topLayer = mWorkspaceViewStub;
		
		mWorkspace = (Workspace)topLayer.findViewById(R.id.workspace);
        final Workspace workspace = mWorkspace;
        workspace.setHapticFeedbackEnabled(false);

        DeleteZone deleteZone = (DeleteZone) topLayer.findViewById(R.id.delete_zone);
        mDeleteZone = deleteZone;

        mHandleView = (HandleView) topLayer.findViewById(R.id.all_apps_button);
        mHandleView.setLauncher(getLauncher());
        mHandleView.setOnClickListener(getLauncher());
        mHandleView.setOnLongClickListener(getLauncher());

        hotseatLeft = (ImageView) topLayer.findViewById(R.id.hotseat_left);
        //hotseatLeft.setContentDescription(mHotseatLabels[0]);
        //hotseatLeft.setImageDrawable(mHotseatIcons[0]);
        hotseatLeft.setOnClickListener(getLauncher());
        
        hotseatRight = (ImageView) topLayer.findViewById(R.id.hotseat_right);
        //hotseatRight.setContentDescription(mHotseatLabels[1]);
        //hotseatRight.setImageDrawable(mHotseatIcons[1]);
        hotseatRight.setOnClickListener(getLauncher());

        mPreviousView = (ImageView) topLayer.findViewById(R.id.previous_screen);
        mNextView = (ImageView) topLayer.findViewById(R.id.next_screen);

        Drawable previous = mPreviousView.getDrawable();
        Drawable next = mNextView.getDrawable();
        mWorkspace.setIndicators(previous, next);

        mPreviousView.setHapticFeedbackEnabled(false);
        mPreviousView.setOnClickListener(getLauncher());
        mPreviousView.setOnLongClickListener(getLauncher());
        
        mNextView.setHapticFeedbackEnabled(false);
        mPreviousView.setOnClickListener(getLauncher());
        mNextView.setOnLongClickListener(getLauncher());

        workspace.setOnLongClickListener(getLauncher());
        workspace.setDragController(dragController);
        workspace.setLauncher(getLauncher());

        deleteZone.setLauncher(getLauncher());
        deleteZone.setDragController(dragController);
        deleteZone.setHandle(dragLayer.findViewById(R.id.all_apps_button_cluster));

        dragController.setDragScoller(workspace);
        dragController.addDragListener(deleteZone);
        dragController.setScrollView(dragLayer);
        dragController.setMoveTarget(workspace);

        // The order here is bottom to top.
        dragController.addDropTarget(workspace);
        dragController.addDropTarget(deleteZone);
        
        
        super.onCreate(dragLayer, dragController);
	}
	
	
    @Override
    public boolean showAllApps(boolean animated){
    	//QsLog.LogD("StyleDefautsWidget::showAllApps(0)===");
    	if(super.showAllApps(animated)){
    		//QsLog.LogD("StyleDefautsWidget::showAllApps(1)===");
	    	// TODO: fade these two too
	        mDeleteZone.setVisibility(View.GONE);
	        mHandleView.setVisibility(View.GONE);
	        mPreviousView.setVisibility(View.GONE);
	        mNextView.setVisibility(View.GONE);
			hotseatLeft.setVisibility(View.GONE);
			hotseatRight.setVisibility(View.GONE);
			return true;
    	}
    	return false;
	}
    @Override
    public boolean closeAllApps(boolean animated){
    	//QsLog.LogD("StyleDefautsWidget::closeAllApps(0)===");
    	if(super.closeAllApps(animated)){
    		//QsLog.LogD("StyleDefautsWidget::closeAllApps(1)===");
	    	mHandleView.setVisibility(View.VISIBLE);
	        mPreviousView.setVisibility(View.VISIBLE);
	        mNextView.setVisibility(View.VISIBLE);
		    hotseatLeft.setVisibility(View.VISIBLE);
		    hotseatRight.setVisibility(View.VISIBLE);
		    return true;
    	}
    	return false;
    }
    @Override
	public void onNewIntent(Intent intent){
    	
    	// Close the apps
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && mHandleView != null) {
            
            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            if(alreadyOnHome && mWorkspace.isDefaultScreenShowing() && !isAllAppsVisible()){
            	getLauncher().showPreviews(mHandleView);
            	return;
            }
        }
        
    	super.onNewIntent(intent);
    }
    @Override
    public boolean onClick(View v){
    	//QsLog.LogD("StyleDefautsWidget::onClick(0)===");
    	if (v == mHandleView) {
    		//QsLog.LogD("StyleDefautsWidget::onClick(1)=mHandleView==");
    		if (isAllAppsVisible()) {
            	closeAllApps(true);
            } else {
            	showAllApps(true);
            }
    		return true;
        }
    	
    	if(super.onClick(v))
    		return true;
    	
    	int id = v.getId();
    	switch(id){
	    	case R.id.previous_screen:
	    		mWorkspace.scrollLeft();
	    		return true;
	    	case R.id.next_screen:
	    		mWorkspace.scrollRight();
	    		return true;
	    	case R.id.hotseat_left:{
	    		Intent intent = new Intent(Intent.ACTION_MAIN);
	    		intent.setComponent(ComponentName.unflattenFromString("com.android.contacts/com.android.contacts.DialtactsActivity"));
	    		getLauncher().startActivitySafely(intent, "phone");
	    		return true;
	    	}
	    	case R.id.hotseat_right:{
	    		//launchHotSeat(v);
	    		String defaultUri = getLauncher().getString(R.string.default_browser_url);
	    		Intent intent = new Intent(
	                  Intent.ACTION_VIEW,
	                  ((defaultUri != null)
	                      ? Uri.parse(defaultUri)
	                      : Utilities.getDefaultBrowserUri(getLauncher()))
	              ).addCategory(Intent.CATEGORY_BROWSABLE);
	    		getLauncher().startActivitySafely(intent, "browser");
	    		return true;
		    }
    	}
    	
    	return false;
    }
    @Override
    public boolean onLongClick(View v){
    	if(super.onLongClick(v))
    		return true;
    	
    	switch (v.getId()) {
        case R.id.previous_screen:
            if (!isAllAppsVisible()) {
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                getLauncher().showPreviews(v);
            }
            return true;
        case R.id.next_screen:
            if (!isAllAppsVisible()) {
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                getLauncher().showPreviews(v);
            }
            return true;
        case R.id.all_apps_button:
            if (!isAllAppsVisible()) {
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                getLauncher().showPreviews(v);
            }
            return true;
    	}
    	return false;
    }
    
//    @Override
//    public void onLocaleChanged(boolean localeChanged){
//    	if(localeChanged)
//    		loadHotseats();
//    }
    @Override
    public void onPause(){
    	getLauncher().dismissPreview(mPreviousView);
    	getLauncher().dismissPreview(mNextView);
    }
    @Override
	public void onDestroy(){
    	getLauncher().dismissPreview(mPreviousView);
    	getLauncher().dismissPreview(mNextView);
    }
    @Override
	public void onBackPressed(){
    	getLauncher().dismissPreview(mPreviousView);
    	getLauncher().dismissPreview(mNextView);
    }
    
//    public void launchHotSeat(View v) {
//
//        int index = -1;
//        if (v.getId() == R.id.hotseat_left) {
//            index = 0;
//        } else if (v.getId() == R.id.hotseat_right) {
//            index = 1;
//        }
//
//        // reload these every tap; you never know when they might change
//        //loadHotseats();
//        if (index >= 0 && index < mHotseats.length && mHotseats[index] != null) {
//            Intent intent = mHotseats[index];
//            getLauncher().startActivitySafely(
//                mHotseats[index],
//                "hotseat"
//            );
//        }
//    }
    
 // Load the Intent templates from arrays.xml to populate the hotseats. For
    // each Intent, if it resolves to a single app, use that as the launch
    // intent & use that app's label as the contentDescription. Otherwise,
    // retain the ResolveActivity so the user can pick an app.
//    private void loadHotseats() {
//        if (mHotseatConfig == null) {
//            mHotseatConfig = getLauncher().getResources().getStringArray(R.array.hotseats);
//            if (mHotseatConfig.length > 0) {
//                mHotseats = new Intent[mHotseatConfig.length];
//                mHotseatLabels = new CharSequence[mHotseatConfig.length];
//                mHotseatIcons = new Drawable[mHotseatConfig.length];
//            } else {
//                mHotseats = null;
//                mHotseatIcons = null;
//                mHotseatLabels = null;
//            }
//
//            TypedArray hotseatIconDrawables = getLauncher().getResources().obtainTypedArray(R.array.hotseat_icons);
//            for (int i=0; i<mHotseatConfig.length; i++) {
//                // load icon for this slot; currently unrelated to the actual activity
//                try {
//                    mHotseatIcons[i] = hotseatIconDrawables.getDrawable(i);
//                } catch (ArrayIndexOutOfBoundsException ex) {
//                    //Log.w(TAG, "Missing hotseat_icons array item #" + i);
//                    mHotseatIcons[i] = null;
//                }
//            }
//            hotseatIconDrawables.recycle();
//        }
//
//        PackageManager pm = getLauncher().getPackageManager();
//        for (int i=0; i<mHotseatConfig.length; i++) {
//            Intent intent = null;
//            if (mHotseatConfig[i].equals("*BROWSER*")) {
//                // magic value meaning "launch user's default web browser"
//                // replace it with a generic web request so we can see if there is indeed a default
//                String defaultUri = getLauncher().getString(R.string.default_browser_url);
//                intent = new Intent(
//                        Intent.ACTION_VIEW,
//                        ((defaultUri != null)
//                            ? Uri.parse(defaultUri)
//                            : Utilities.getDefaultBrowserUri(getLauncher()))
//                    ).addCategory(Intent.CATEGORY_BROWSABLE);
//                // note: if the user launches this without a default set, she
//                // will always be taken to the default URL above; this is
//                // unavoidable as we must specify a valid URL in order for the
//                // chooser to appear, and once the user selects something, that 
//                // URL is unavoidably sent to the chosen app.
//            } else {
//                try {
//                    intent = Intent.parseUri(mHotseatConfig[i], 0);
//                } catch (java.net.URISyntaxException ex) {
//                    //Log.w(TAG, "Invalid hotseat intent: " + mHotseatConfig[i]);
//                    // bogus; leave intent=null
//                }
//            }
//            
//            if (intent == null) {
//                mHotseats[i] = null;
//                mHotseatLabels[i] = getLauncher().getText(R.string.activity_not_found);
//                continue;
//            }
//
//            ResolveInfo bestMatch = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//            List<ResolveInfo> allMatches = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//
//            // did this resolve to a single app, or the resolver?
//            if (allMatches.size() == 0 || bestMatch == null) {
//                // can't find any activity to handle this. let's leave the 
//                // intent as-is and let Launcher show a toast when it fails 
//                // to launch.
//                mHotseats[i] = intent;
//
//                // set accessibility text to "Not installed"
//                mHotseatLabels[i] = getLauncher().getText(R.string.activity_not_found);
//            } else {
//                boolean found = false;
//                for (ResolveInfo ri : allMatches) {
//                    if (bestMatch.activityInfo.name.equals(ri.activityInfo.name)
//                        && bestMatch.activityInfo.applicationInfo.packageName
//                            .equals(ri.activityInfo.applicationInfo.packageName)) {
//                        found = true;
//                        break;
//                    }
//                }
//                
//                if (!found) {
//                    //if (LOGD) Log.d(TAG, "Multiple options, no default yet");
//                    // the bestMatch is probably the ResolveActivity, meaning the
//                    // user has not yet selected a default
//                    // so: we'll keep the original intent for now
//                    mHotseats[i] = intent;
//
//                    // set the accessibility text to "Select shortcut"
//                    mHotseatLabels[i] = getLauncher().getText(R.string.title_select_shortcut);
//                } else {
//                    // we have an app!
//                    // now reconstruct the intent to launch it through the front
//                    // door
//                    ComponentName com = new ComponentName(
//                        bestMatch.activityInfo.applicationInfo.packageName,
//                        bestMatch.activityInfo.name);
//                    mHotseats[i] = new Intent(Intent.ACTION_MAIN).setComponent(com);
//
//                    // load the app label for accessibility
//                    mHotseatLabels[i] = bestMatch.activityInfo.loadLabel(pm);
//                }
//            }
//        }
//    }
    
 
}
