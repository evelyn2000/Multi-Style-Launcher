package com.android.qshome.style.samsung;

import java.util.ArrayList;
import java.util.Collections;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.CellLayout;
import com.android.qshome.ctrl.DeleteZone;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.Folder;
import com.android.qshome.ctrl.QsScreenIndicator;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.BaseStyleObjectWidget;
import com.android.qshome.style.defaults.StyleDefaultsApps;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.ThemeStyle;
import com.android.qshome.util.Utilities;

import com.android.qshome.R;

public class StyleSamsungWidget extends BaseStyleObjectWidget{
	
	private TextView[] mButtonGroups = new TextView[4];
	private ViewGroup mBottomLayout;
	private DeleteZone mDeleteZone;
	private QsScreenIndicator mScreenIndicator;
	private Drawable mAllAppImg;
	private Drawable mHomeImg;
	
	public StyleSamsungWidget(Launcher context, LauncherModel model, IconCache iconCache){
		super(context, model, iconCache, ThemeStyle.Unkown);
	}
    
	public StyleSamsungWidget(Launcher context, LauncherModel model, IconCache iconCache, ThemeStyle appStyle){
		super(context, model, iconCache, appStyle);
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
	@Override
	protected BaseStyleObjectApps getBaseStyleObjectApps(ThemeStyle appStyle){
		if(appStyle == ThemeStyle.Samsung || appStyle == ThemeStyle.Unkown){
			return new StyleSamsungApps(mLauncher, mModel, mIconCache, this);
		}
		return super.getBaseStyleObjectApps(ThemeStyle.Default);
	}
	@Override
	public void onCreate(DragLayer dragLayer, DragController dragController){
		
		ViewStub stub = (ViewStub)dragLayer.findViewById(R.id.stub_workspace_style_samsung);
		mWorkspaceViewStub = (ViewGroup)stub.inflate();
		final ViewGroup topLayer = mWorkspaceViewStub;
		
		mWorkspace = (Workspace)topLayer.findViewById(R.id.workspace);
        final Workspace workspace = mWorkspace;
        //workspace.setHapticFeedbackEnabled(false);
        
        mBottomLayout = (ViewGroup)topLayer.findViewById(R.id.all_apps_button_cluster);
        
        int nCount = Math.min(mBottomLayout.getChildCount(), mButtonGroups.length);
        //QsLog.LogD("onCreate()==nCount:"+nCount+"==len:"+mButtonGroups.length+"==child:"+mBottomLayout.getChildCount());
        for(int i=0; i<nCount; i++){
        	mButtonGroups[i] = (TextView)mBottomLayout.getChildAt(i);
        	mButtonGroups[i].setOnClickListener(getLauncher());
        }
        
        mAllAppImg = getLauncher().getResources().getDrawable(R.drawable.zzz_samsung_btn_apps);
        mHomeImg = getLauncher().getResources().getDrawable(R.drawable.zzz_samsung_btn_home);
        mButtonGroups[3].setCompoundDrawablesWithIntrinsicBounds(null, mAllAppImg, 
        		null, null);
        mButtonGroups[3].setText(R.string.group_applications);

        DeleteZone deleteZone = (DeleteZone) topLayer.findViewById(R.id.delete_zone);
        mDeleteZone = deleteZone;
        
        mScreenIndicator = (QsScreenIndicator) topLayer.findViewById(R.id.screen_indicator);
        mScreenIndicator.initial(getLauncher(), workspace);
        
        workspace.setOnLongClickListener(getLauncher());
        workspace.setDragController(dragController);
        workspace.setLauncher(getLauncher());

        deleteZone.setLauncher(getLauncher());
        deleteZone.setDragController(dragController);
        deleteZone.setHandle(mBottomLayout);
        
        dragController.setDragScoller(workspace);
        dragController.addDragListener(deleteZone);
        dragController.setScrollView(dragLayer);
        dragController.setMoveTarget(workspace);

        // The order here is bottom to top.
        dragController.addDropTarget(workspace);
        dragController.addDropTarget(deleteZone);
        
        //mButtonGroups[1].setText(item.title);
        
        
        final PackageManager mPackageManager = getLauncher().getPackageManager();
        // phone
        ComponentName pkg = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity");
        try {
	        ActivityInfo ai = mPackageManager.getActivityInfo(pkg, 0);
	        mButtonGroups[0].setText(ai.loadLabel(mPackageManager));
        }catch (PackageManager.NameNotFoundException e) {
        	mButtonGroups[0].setText("Phone");
        }
        // contacts
        pkg = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity");
        try {
        	ActivityInfo ai = mPackageManager.getActivityInfo(pkg, 0);
	        mButtonGroups[1].setText(ai.loadLabel(mPackageManager));
        }catch (PackageManager.NameNotFoundException e) {
        	mButtonGroups[1].setText("Contacts");
        }
        
        // sms
        pkg = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationList");
        try {
        	ActivityInfo ai = mPackageManager.getActivityInfo(pkg, 0);
	        mButtonGroups[2].setText(ai.loadLabel(mPackageManager));
        }catch (PackageManager.NameNotFoundException e) {
        	mButtonGroups[2].setText("Messaging");
        }
        
        super.onCreate(dragLayer, dragController);
	}
	
	@Override
    public boolean showAllApps(boolean animated){
    	//QsLog.LogD("StyleSamsungWidget::showAllApps(0)===");
    	if(super.showAllApps(animated)){
    		//QsLog.LogD("StyleSamsungWidget::showAllApps(1)===");
	    	// TODO: fade these two too
	        mDeleteZone.setVisibility(View.GONE);
	        mButtonGroups[3].setCompoundDrawablesWithIntrinsicBounds(null, 
	        		mHomeImg, 
	        		null, null);
	        mButtonGroups[3].setText(R.string.all_apps_home_button_label);
	        
	        //mDragController.removeDropTarget(mWorkspace);
	        mWorkspace.hideWorkspace();
	        mScreenIndicator.setVisibility(View.GONE);
			return true;
    	}
    	return false;
	}
    @Override
    public boolean closeAllApps(boolean animated){
    	//QsLog.LogD("StyleSamsungWidget::closeAllApps(0)===");
    	if(super.closeAllApps(animated)){
    		mButtonGroups[3].setCompoundDrawablesWithIntrinsicBounds(null, 
    				mAllAppImg, 
            		null, null);
            mButtonGroups[3].setText(R.string.group_applications);
            
            mWorkspace.showWorkspace();
            mScreenIndicator.setVisibility(View.VISIBLE);
            mDragController.setDragScoller(mWorkspace);
		    return true;
    	}
    	return false;
    }
    
    @Override
	public void onNewIntent(Intent intent){
    	
    	// Close the apps
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && mButtonGroups[3] != null) {
            
            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            if(alreadyOnHome && mWorkspace.isDefaultScreenShowing() && !isAllAppsVisible()){
            	getLauncher().showPreviews(mButtonGroups[3]);
            	return;
            }
        }
        
    	super.onNewIntent(intent);
    }
    
    @Override
    public boolean onLongClick(View v){
    	if(super.onLongClick(v))
    		return true;
    	
    	if (v == mButtonGroups[3] && !isAllAppsVisible()) {
    		
            mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            getLauncher().showPreviews(v);
            
            return true;
    	}
    	return false;
    }
    
    @Override
    public boolean onClick(View v){
    	//QsLog.LogD("StyleDefautsWidget::onClick(0)===");
    	if (v == mButtonGroups[3]) {
    		//QsLog.LogD("StyleDefautsWidget::onClick(1)=mHandleView==");
    		if (isAllAppsVisible()) {
            	closeAllApps(true);
            } else {
            	showAllApps(true);
            }
    		return true;
        }
    	
    	if(v == mButtonGroups[0]){ // dial
    		//Uri phoneUri = Uri.fromParts("tel", "", null);
    		Utilities.sendActionForDial(getLauncher());
    		return true;
    	}
    	
    	if(v == mButtonGroups[1]){ // contacts
    		//Intent intent = new Intent("com.android.contacts.action.LIST_CONTACTS");
    		Utilities.sendActionForContacts(getLauncher());
    		return true;
    	}
    	
    	if(v == mButtonGroups[2]){
    		Utilities.sendActionForSms(getLauncher());
    		return true;
    	}

    	return super.onClick(v);
    }
    /*@Override
	public void bindAllApplications(ArrayList<ApplicationInfo> apps){
		int N = apps.size();
		int nCount = 0;
        for (int i=0; i<N && nCount < 3; i++) {
            final ApplicationInfo item = apps.get(i);
            final String pkgname = item.componentName.getPackageName();
            //QsLog.LogD("onBindAllApplications()==pkg:"+pkgname+"=classname:"+classname);
            int index = -1;
            if(pkgname.equals("com.android.contacts")){
            	final String classname = item.componentName.getClassName();
            	if(classname.equals("com.android.contacts.DialtactsActivity")){
            		index = 0;
            	}else if(classname.equals("com.android.contacts.DialtactsContactsEntryActivity")){
            		index = 1;
            	}
            }else if(pkgname.equals("com.android.mms")){
            	if(item.componentName.getClassName().equals("com.android.mms.ui.ConversationList")){
            		index = 2;
            	}
            }
            
            if(index >= 0){
            	apps.remove(i);
            	i--;
            	N--;
//            	item.iconBitmap.setDensity(Bitmap.DENSITY_NONE);
//            	mButtonGroups[index].setCompoundDrawablesWithIntrinsicBounds(null, 
//        				new BitmapDrawable(item.iconBitmap), 
//                		null, null);
        		mButtonGroups[index].setText(item.title);
        		nCount++;
        		mButtonGroups[index].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                    	getLauncher().startActivitySafely(item.intent, item);
                    }
                });
        	}
        }
        
        //mButtonGroups[3].setCompoundDrawablesWithIntrinsicBounds(null, mAllAppImg, 
        //		null, null);
        mButtonGroups[3].setText(R.string.group_applications);
        mButtonGroups[3].setOnClickListener(getLauncher());
        
        super.bindAllApplications(apps);
	}*/
	
	@Override
	public int getWorkspaceScreenLayout(){
    	return R.layout.workspace_screen_samsung;
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState){
//		if (isAllAppsVisible()) {
//            closeAllApps(false);
//        }
		
		super.onSaveInstanceState(outState);
		outState.putBoolean(Launcher.RUNTIME_STATE_ALL_APPS_FOLDER, false);
	}
	
	@Override
	public boolean isBindItemsFirst(){
    	return true;
    }
	
	public int getFolderIconLayout(){
    	return R.layout.folder_icon_samsung;
    }
}
