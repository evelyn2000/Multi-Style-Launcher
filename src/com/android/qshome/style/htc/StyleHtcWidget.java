package com.android.qshome.style.htc;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SlidingDrawer;

import com.android.qshome.AllAppsView;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.AllAppsGridView;
import com.android.qshome.ctrl.DeleteZone;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.HandleView;
import com.android.qshome.ctrl.QsScrollbar;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.BaseStyleObjectWidget;
import com.android.qshome.style.IBaseStyleInterfaceApps;
import com.android.qshome.style.defaults.StyleDefaultsApps;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.Utilities;
import com.android.qshome.util.ThemeStyle;

import com.mediatek.common.featureoption.FeatureOption;


import com.android.qshome.R;

public class StyleHtcWidget extends BaseStyleObjectWidget 
	implements IBaseStyleInterfaceApps{
	
	//private View mLayoutBottom;
	private View mLayoutThreeBtn;
	
	private DeleteZone mDeleteZone;
	
	private ImageButton mImageBtnHandle;
	private Button mImageBtnDial;
	private ImageButton mImageBtnAdd;
	private QsScrollbar mQsScrollbar;
	
	protected AllAppsView mAllAppsGrid;
	private SlidingDrawer mDrawer;
	private DragLayer mDragLayer;
	//private HandleView mHandleView;
	
	public StyleHtcWidget(Launcher context, LauncherModel model, IconCache iconCache){
		super(context, model, iconCache, ThemeStyle.Htc);
	}
    
	public StyleHtcWidget(Launcher context, LauncherModel model, IconCache iconCache, ThemeStyle appStyle){
		super(context, model, iconCache, appStyle);
	}
	
	public int getThemeType(){
		return THEME_TYPE_ALL_IN_ONE;
	}
	
	public ThemeStyle getThemeStyle(){
		return ThemeStyle.Htc;
	}
	
	@Override
    public void bindAllApplications(ArrayList<ApplicationInfo> apps){
    	mAllAppsGrid.setApps(apps);
    }
	@Override
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps){
    	super.bindAppsAdded(apps);
        mAllAppsGrid.addApps(apps);
    }
	@Override
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps){
    	super.bindAppsUpdated(apps);
        mAllAppsGrid.updateApps(apps);
    }
	@Override
    public void bindAppsRemoved(ArrayList<ApplicationInfo> apps, boolean permanent){
    	super.bindAppsRemoved(apps, permanent);
        mAllAppsGrid.removeApps(apps);
    }
	@Override
    public boolean isAllAppsVisible(){
    	return isDrawerUp();//(mAllAppsGrid != null) ? mAllAppsGrid.isVisible() : false;
    }
	
    @Override
    public void finishBindingItems(){
    	super.finishBindingItems();
    	//QsLog.LogD("StyleHtcsWidget::finishBindingItems(0)===");
    	mDrawer.unlock();
    }
    @Override
    public void lockAllApps(){
    	if(mDrawer != null)
    		mDrawer.lock();
    }
    @Override
	public void unlockAllApps(){
    	if(mDrawer != null)
    		mDrawer.unlock();
	}
    
    @Override
    public boolean showAllApps(boolean animated){
    	//QsLog.LogD("StyleHtcsWidget::showAllApps(0)===");
    	if (isDrawerDown()){
	    	mDrawer.setVisibility(View.VISIBLE);
			mQsScrollbar.setVisibility(View.GONE);
			mDrawer.animateOpen();
			return true;
    	}
    	return false;
	}
    @Override
    public boolean closeAllApps(boolean animated){
    	//QsLog.LogD("StyleHtcsWidget::closeAllApps(0)===");
    	if (isDrawerUp()){
    		mDrawer.animateClose();
    		return true;
    	}
    	return false;
    }
    @Override
	public void onNewIntent(Intent intent){
    	
    	// Close the apps
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && mImageBtnHandle != null) {
            
            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            if(alreadyOnHome && mWorkspace.isDefaultScreenShowing() && !isAllAppsVisible()){
            	getLauncher().showPreviews(mImageBtnHandle);
            	return;
            }
        }
        
    	super.onNewIntent(intent);
    }
    @Override
    public boolean onClick(View v){
    	//QsLog.LogD("StyleDefautsWidget::onClick(0)===");
    	if (v == mImageBtnHandle) {
    		if (isDrawerDown()) {
				showAllApps(true);
			} else if (isDrawerUp()) {
				closeAllApps(true);
			}
    		return true;
        }else if(v == mImageBtnDial){
        	
        	Utilities.sendActionForDial(getLauncher());
        	
			return true;
        }else if(v == mImageBtnAdd){
        	if (isDrawerUp()) {
				mDrawer.animateClose();
			}
			
			getLauncher().startActivity(new Intent(Intent.ACTION_VIEW, 
					Utilities.getDefaultBrowserUri(getLauncher()))
			.addCategory(Intent.CATEGORY_BROWSABLE));
			
        	return true;
        }
    	
    	if(super.onClick(v))
    		return true;

    	return false;
    }
    @Override
    public void onCreate(DragLayer dragLayer, DragController dragController){
    	if(!Launcher.QS_DISABLE_THEME_SWITCH){
    	
    		mDragLayer = dragLayer;
			ViewStub stub = (ViewStub)dragLayer.findViewById(R.id.stub_workspace_style_htc);
			mWorkspaceViewStub = (ViewGroup)stub.inflate();
			final ViewGroup topLayer = mWorkspaceViewStub;
			
			mWorkspace = (Workspace)topLayer.findViewById(R.id.workspace);
	        final Workspace workspace = mWorkspace;
	        workspace.setHapticFeedbackEnabled(false);
	
	        DeleteZone deleteZone = (DeleteZone) topLayer.findViewById(R.id.delete_zone);
	        mDeleteZone = deleteZone;
	
	        //mLayoutBottom = dragLayer.findViewById(R.id.layoutbottomall);
	        mLayoutThreeBtn = topLayer.findViewById(R.id.layoutthreebtn);
	    	mQsScrollbar = (QsScrollbar)topLayer.findViewById(R.id.pageturn_scrollbar);
	    	
	    	mImageBtnHandle = (ImageButton) topLayer.findViewById(R.id.btnhandle);
			final ImageButton imageBtnHandle = mImageBtnHandle;
			imageBtnHandle.setOnClickListener(getLauncher());
	
			mImageBtnDial = (Button) topLayer.findViewById(R.id.btndial);
			// final IconTextView imageBtnDial = mImageBtnDial;
			final Button imageBtnDial = mImageBtnDial;
			imageBtnDial.setOnClickListener(getLauncher());
			if(FeatureOption.Qs_Sub_Project_Name.startsWith("A610")){
				imageBtnDial.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
	
			mImageBtnAdd = (ImageButton) topLayer.findViewById(R.id.btnadd);
			final ImageButton imageBtnAdd = mImageBtnAdd;
			imageBtnAdd.setOnClickListener(getLauncher());
			
			mDrawer = (SlidingDrawer) topLayer.findViewById(R.id.drawer);
			final SlidingDrawer drawer = mDrawer;
	
			mAllAppsGrid = (AllAppsView) drawer.getContent();
			final AllAppsGridView grid =(AllAppsGridView) mAllAppsGrid;
			
	//		mHandleView = (HandleView) drawer.findViewById(R.id.zz_htc_all_apps_handle);
	//		mHandleView.setLauncher(getLauncher());
			
			
			drawer.lock();
			final DrawerManager drawerManager = new DrawerManager();
			drawer.setOnDrawerOpenListener(drawerManager);
			drawer.setOnDrawerCloseListener(drawerManager);
			drawer.setOnDrawerScrollListener(drawerManager);
	
			grid.setTextFilterEnabled(false);
			grid.setDragController(dragController);
			grid.setLauncher(getLauncher());
			
	    	
			workspace.setOnLongClickListener(getLauncher());
			workspace.setDragController(dragController);
			workspace.setLauncher(getLauncher());
	
			mQsScrollbar.initial(getLauncher(), workspace);
	
			deleteZone.setLauncher(getLauncher());
	        deleteZone.setDragController(dragController);
	        deleteZone.setHandle(dragLayer.findViewById(R.id.layoutbottomall));
	
	        dragController.setDragScoller(workspace);
	        //dragController.setDragListener(deleteZone);
	        dragController.addDragListener(deleteZone);
	        dragController.setScrollView(dragLayer);
	        dragController.setMoveTarget(workspace);
	        
	     // The order here is bottom to top.
	        dragController.addDropTarget(workspace);
	        dragController.addDropTarget(deleteZone);

    	}
        super.onCreate(dragLayer, dragController);
	}
    @Override
    public void onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
    	if(mAllAppsGrid != null)
    		mAllAppsGrid.surrender();
    }
    
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If all apps is animating, don't show the menu, because we don't know
//        // which one to show.
//        if (mAllAppsGrid.isVisible() && !mAllAppsGrid.isOpaque()) {
//            return true;
//        }
//        return false;
//    }
    
    boolean isDrawerDown() {
		return !mDrawer.isMoving() && !mDrawer.isOpened();
	}

	boolean isDrawerUp() {
		return mDrawer.isOpened() && !mDrawer.isMoving();
	}

	boolean isDrawerMoving() {
		return mDrawer.isMoving();
	}
    
    private class DrawerManager implements SlidingDrawer.OnDrawerOpenListener,
		SlidingDrawer.OnDrawerCloseListener,
		SlidingDrawer.OnDrawerScrollListener {
		private boolean mOpen;
		
		public void onDrawerOpened() {
			//QsLog.LogD("StyleHtcWidget::onDrawerOpened()===mOpen:"+mOpen);
			if (!mOpen) {
				// mHandleIcon.startTransition(150);
				mImageBtnHandle
						.setImageResource(R.drawable.zz_htc_hud_tray_handle_down);
		
				mQsScrollbar.setVisibility(View.GONE);
				//mLayoutBottom.setBackgroundResource(0);
//				final Rect bounds = mWorkspace.mDrawerBounds;
//				offsetBoundsToDragLayer(bounds, mAllAppsGrid);
		
				mOpen = true;
			}
		}
		
		private void offsetBoundsToDragLayer(Rect bounds, View view) {
			view.getDrawingRect(bounds);
		
			while (view != mDragLayer) {
				bounds.offset(view.getLeft(), view.getTop());
				view = (View) view.getParent();
			}
		}
		
		public void onDrawerClosed() {
			//QsLog.LogD("StyleHtcWidget::onDrawerClosed()===mOpen:"+mOpen);
			if (mOpen) {
				// mHandleIcon.resetTransition();
				mImageBtnHandle.setImageResource(R.drawable.zz_htc_hud_tray_handle_up);
				mQsScrollbar.setVisibility(View.VISIBLE);
				mDrawer.setVisibility(View.GONE);
				mOpen = false;
			}
		
			((AllAppsGridView)mAllAppsGrid).setSelection(0);
			((AllAppsGridView)mAllAppsGrid).clearTextFilter();
		}
		
		public void onScrollStarted() {
//			if (PROFILE_DRAWER) {
//				android.os.Debug.startMethodTracing("/sdcard/launcher-drawer");
//			}
			//QsLog.LogD("StyleHtcWidget::onScrollStarted()===");
			//mWorkspace.mDrawerContentWidth = mAllAppsGrid.getWidth();
			//mWorkspace.mDrawerContentHeight = mAllAppsGrid.getHeight();
		}
		
		public void onScrollEnded() {
//			if (PROFILE_DRAWER) {
//				android.os.Debug.stopMethodTracing();
//			}
			//QsLog.LogD("StyleHtcWidget::onScrollEnded()===");
		}
	}
}
