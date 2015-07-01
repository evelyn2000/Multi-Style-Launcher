package com.android.qshome.ctrl;


import java.util.ArrayList;
import java.util.Collections;

import com.android.qshome.AllAppsView;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;

import com.android.qshome.R;

public class AllAppsWorkspace extends FrameLayout implements AllAppsView,
			DragSource{
	private Launcher mLauncher;
    private DragController mDragController;
    
    private Workspace mWorkspace;
    
	public AllAppsWorkspace(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }
	
	public AllAppsWorkspace(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);
        setSoundEffectsEnabled(false);
    }
	
	@Override
    protected void onFinishInflate() {
        setBackgroundColor(Color.BLACK);
        
        //mWorkspace = (Workspace)super.findViewById(R.id.workspace_all_app);
        
        //QsScreenIndicator screenIndicator = (QsScreenIndicator) findViewById(R.id.all_app_screen_indicator);
        //screenIndicator.initial(getContext(), mWorkspace);

        //QsLog.LogD("AllAppsWorkspace::onFinishInflate()===");
    }
	
	protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //QsLog.LogD("AllAppsWorkspace::onAttachedToWindow()===");
    }
	
	public Workspace getWorkspace(){
		return mWorkspace;
	}
	
	public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }
	
	public void setDragController(DragController dragger) {
        mDragController = dragger;
        
        mWorkspace.setOnLongClickListener(mLauncher);
        mWorkspace.setDragController(mDragController);
        mWorkspace.setLauncher(mLauncher);
    }

    public void onDropCompleted(View target, DragObject d, boolean success) {
    }
    
    public void zoom(float zoom, boolean animate) {
    	if(zoom > 0.0f){
    		if(!isVisible()){
	    		setVisibility(View.VISIBLE);
	    		
//	    		mLauncher.setWorkspace(mWorkspace);
//	    		
	    		mDragController.setDragScoller(mWorkspace);
//	    		mDragController.setMoveTarget(mWorkspace);
//	    		mDragController.addDropTarget(mWorkspace);
	    		mWorkspace.showWorkspace();
    		}
    	}
    	else{
    		if(isVisible()){
	    		mWorkspace.hideWorkspace();
	    		setVisibility(View.GONE);
//	    		mDragController.removeDropTarget(mWorkspace);
    		}
    	}
    }
    
    public boolean isVisible() {
        return (super.getVisibility() == View.VISIBLE);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }
    
    private boolean addAppInScreen(int nScreen, View shortcut, ShortcutInfo shortcutInfo){
    	final CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(nScreen);
    	int[] vacant = new int[2];
    	
    	if (cellLayout.getVacantCell(vacant, 1, 1)) {
        	mWorkspace.addInScreen(shortcut, nScreen, vacant[0], vacant[1], 1, 1);
            // insert into database
            LauncherModel.addItemToDatabase(mLauncher, shortcutInfo,
                    LauncherSettings.Favorites.CONTAINER_CUSTOM_APPS, nScreen, vacant[0],
                    vacant[1], false, true);
            return true;
        }
    	return false;
    }
    
    private void addNewLayout(){
    	CellLayout layout = (CellLayout) mLauncher.getLayoutInflater().inflate(
				mLauncher.getWorkspaceScreenLayout(), mWorkspace, false);
    	layout.setClickable(true);
        mWorkspace.addView(layout, -1);
    }

    public void setApps(ArrayList<ApplicationInfo> list) {
    	if(mWorkspace == null)
    		return;
    	mWorkspace.removeViews();
    	
    	final int N = list.size();
    	int nScreen = 0; 
    	//int nScreenCount = mWorkspace.getChildCount();
    	//int[] vacant = new int[2];
    	//CellLayout cellLayout;
    	boolean bGoted;
        for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            final ShortcutInfo shortcutInfo = item.makeShortcut();
            View shortcut = mWorkspace.createShortcut(shortcutInfo, null);

            do{
            	
            	if(!(bGoted = addAppInScreen(nScreen, shortcut, shortcutInfo))){
            		//QsLog.LogE("AllAppsWorkspace::setApps(0)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
	            	nScreen++;
	            	if(nScreen >= mWorkspace.getChildCount()){
	            		addNewLayout();
	            	}
	            	//QsLog.LogE("AllAppsWorkspace::setApps(1)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
	            	//cellLayout = (CellLayout) mWorkspace.getChildAt(nScreen);
            	}
	            
            }while(!bGoted);
        }
    }
    
    public void reorderApps(){
    	
    }

    public void addApps(ArrayList<ApplicationInfo> list) {
    	final int N = list.size();
    	int nScreen = 0; 
    	int nScreenCount = mWorkspace.getChildCount();
    	for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            final ShortcutInfo shortcutInfo = item.makeShortcut();
            View shortcut = mWorkspace.createShortcut(shortcutInfo, null);

            for(nScreen=0; nScreen<nScreenCount; nScreen++){
            	if (addAppInScreen(nScreen, shortcut, shortcutInfo)) {
	                break;
	            }
            }
            //QsLog.LogE("AllAppsWorkspace::setApps(0)==nScreen:"+nScreen+"==count:"+nScreenCount);
            if(nScreen >= nScreenCount){
            	addNewLayout();
            	nScreenCount = mWorkspace.getChildCount();
            	addAppInScreen(nScreen, shortcut, shortcutInfo);
            	//QsLog.LogE("AllAppsWorkspace::setApps(1)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
            }
        }
    }

    public void removeApps(ArrayList<ApplicationInfo> list) {
    	
    	mWorkspace.removeItems(list);
    }

    public void updateApps(ArrayList<ApplicationInfo> list) {
        // Just remove and add, because they may need to be re-sorted.
//        removeApps(list);
//        addApps(list);
    	mWorkspace.updateShortcuts(list);
    }


    public void dumpState() {
        //ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList", mAllAppsList);
    }
    
    public void surrender() {
    }
    
    public void release(){
    	if(mWorkspace != null)
    		mWorkspace.removeViews();
    }
    //end implements
    
    
    private static int findAppByComponent(ArrayList<ApplicationInfo> list, ApplicationInfo item) {
        ComponentName component = item.intent.getComponent();
        final int N = list.size();
        for (int i=0; i<N; i++) {
            ApplicationInfo x = list.get(i);
            if (x.intent.getComponent().equals(component)) {
                return i;
            }
        }
        return -1;
    }
}
