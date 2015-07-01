package com.android.qshome.style.droidics;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.BubbleTextViewIcs;
import com.android.qshome.ctrl.CellLayout;
import com.android.qshome.ctrl.CellLayoutWithDrag;
import com.android.qshome.ctrl.DeleteZone;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.Folder;
import com.android.qshome.ctrl.QsScreenIndicator;
import com.android.qshome.ctrl.QsScrollbar;
import com.android.qshome.ctrl.SearchDropTargetBar;
import com.android.qshome.ctrl.Workspace;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.FolderInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.BaseStyleObjectWidget;
import com.android.qshome.style.samsung.StyleSamsungApps;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.ThemeStyle;
import com.android.qshome.util.Utilities;

import com.android.qshome.R;

public class StyleDroidIcsWidget extends BaseStyleObjectWidget {
	
	private CellLayoutWithDrag mBottomLayout;
	//private DeleteZone mDeleteZone;
	private QsScrollbar mScreenIndicator;
	private SearchDropTargetBar mSearchDropTargetBar;
	
	//private BubbleTextViewIcs mWaitingForResume;
	private View mBarDivider;
	
	public StyleDroidIcsWidget(Launcher context, LauncherModel model, IconCache iconCache){
		super(context, model, iconCache, ThemeStyle.Unkown);
	}
    
	public StyleDroidIcsWidget(Launcher context, LauncherModel model, IconCache iconCache, ThemeStyle appStyle){
		super(context, model, iconCache, appStyle);
	}
	@Override
	public ThemeStyle getThemeStyle(){
		return ThemeStyle.DroidIcs;
	}
	
	@Override
	protected BaseStyleObjectApps getBaseStyleObjectApps(ThemeStyle appStyle){
		if(appStyle == ThemeStyle.DroidIcs || appStyle == ThemeStyle.Unkown){
			return new StyleDroidIcsApps(mLauncher, mModel, mIconCache, this);
		}
		return super.getBaseStyleObjectApps(ThemeStyle.Default);
	}
	
	@Override
	public void onCreate(DragLayer dragLayer, DragController dragController){
		
		ViewStub stub = (ViewStub)dragLayer.findViewById(R.id.stub_workspace_style_droidics);
		mWorkspaceViewStub = (ViewGroup)stub.inflate();
		final ViewGroup topLayer = mWorkspaceViewStub;
		
		mWorkspace = (Workspace)topLayer.findViewById(R.id.workspace);
        final Workspace workspace = mWorkspace;
        //workspace.setHapticFeedbackEnabled(false);
        
        mBottomLayout = (CellLayoutWithDrag)topLayer.findViewById(R.id.all_apps_button_cluster);
        
//        int nCount = Math.min(mBottomLayout.getChildCount(), mButtonGroups.length);
//        QsLog.LogD("onCreate()==nCount:"+nCount+"==len:"+mButtonGroups.length+"==child:"+mBottomLayout.getChildCount());
//        for(int i=0; i<nCount; i++){
//        	mButtonGroups[i] = (TextView)mBottomLayout.getChildAt(i);
//        	mButtonGroups[i].setOnClickListener(getLauncher());
//        }
//        
//        mAllAppImg = getLauncher().getResources().getDrawable(R.drawable.zzz_samsung_btn_apps);
//        mHomeImg = getLauncher().getResources().getDrawable(R.drawable.zzz_samsung_btn_home);
//        mButtonGroups[3].setCompoundDrawablesWithIntrinsicBounds(null, mAllAppImg, 
//        		null, null);
//        mButtonGroups[3].setText(R.string.group_applications);

//        DeleteZone deleteZone = (DeleteZone) topLayer.findViewById(R.id.delete_zone);
//        mDeleteZone = deleteZone;
        
        mScreenIndicator = (QsScrollbar) topLayer.findViewById(R.id.screen_indicator);
        mScreenIndicator.initial(getLauncher(), workspace);
        
        // Get the search/delete bar
        mSearchDropTargetBar = (SearchDropTargetBar) topLayer.findViewById(R.id.qsb_bar);

        mBottomLayout.setOnLongClickListener(getLauncher());
        mBottomLayout.setDragController(dragController);
        mBottomLayout.setLauncher(getLauncher());
        
        workspace.setOnLongClickListener(getLauncher());
        workspace.setDragController(dragController);
        workspace.setLauncher(getLauncher());

        //deleteZone.setLauncher(getLauncher());
        //deleteZone.setDragController(dragController);
        //deleteZone.setHandle(mBottomLayout);
        
        dragController.setDragScoller(workspace);
        //dragController.setDragListener(deleteZone);
        dragController.setScrollView(dragLayer);
        dragController.setMoveTarget(workspace);

        // The order here is bottom to top.
        dragController.addDropTarget(workspace);
        dragController.addDropTarget(mBottomLayout);
        //dragController.addDropTarget(deleteZone);
        
        //mButtonGroups[1].setText(item.title);
        
        mBarDivider = topLayer.findViewById(R.id.screen_bar_divider);
        
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(getLauncher(), dragController);
        }
        
        super.onCreate(dragLayer, dragController);
	}
	
	@Override
    public boolean showAllApps(boolean animated){
    	//QsLog.LogD("StyleSamsungWidget::showAllApps(0)===");
    	if(super.showAllApps(animated)){
    		//QsLog.LogD("StyleSamsungWidget::showAllApps(1)===");
	    	// TODO: fade these two too
//	        mDeleteZone.setVisibility(View.GONE);
	        //mDragController.removeDropTarget(mWorkspace);
    		mBottomLayout.setVisibility(View.GONE);
	        mWorkspace.hideWorkspace();
	        mScreenIndicator.setVisibility(View.GONE);
	        mSearchDropTargetBar.hideSearchBar(false);
	        mDragController.setDragScoller(null);
	        
	        mBarDivider.setVisibility(View.GONE);
			return true;
    	}
    	return false;
	}
    @Override
    public boolean closeAllApps(boolean animated){
    	//QsLog.LogD("StyleSamsungWidget::closeAllApps(0)===");
    	if(super.closeAllApps(animated)){

    		if(!mDragController.isDraging())
    			mSearchDropTargetBar.showSearchBar(false);
    		
    		mBottomLayout.setVisibility(View.VISIBLE);
            mWorkspace.showWorkspace();
            mScreenIndicator.setVisibility(View.VISIBLE);
            mDragController.setDragScoller(mWorkspace);
            
            mBarDivider.setVisibility(View.VISIBLE);
            
		    return true;
    	}
    	return false;
    }
    
    @Override
    public void onResume(){
    	
    	super.onResume();
    	
//    	if (mWaitingForResume != null) {
//            mWaitingForResume.setStayPressed(false);
//        }
    }
    
    @Override
	public void onNewIntent(Intent intent){
    	
    	// Close the apps
//        if (Intent.ACTION_MAIN.equals(intent.getAction()) && mButtonGroups[3] != null) {
//            
//            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
//                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//            if(alreadyOnHome && mWorkspace.isDefaultScreenShowing() && !isAllAppsVisible()){
//            	getLauncher().showPreviews(mButtonGroups[3]);
//            	return;
//            }
//        }
        
    	super.onNewIntent(intent);
    }
    
    @Override
    public boolean onLongClick(View v){
    	if(super.onLongClick(v))
    		return true;
    	   	
    	if(!isAllAppsVisible()){
    		
    		if (!(v instanceof CellLayout)) {
                v = (View) v.getParent();
            }

            CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

            // This happens when long clicking an item with the dpad/trackball
            if (cellInfo == null) {
                return false;
            }
            
            if (v instanceof CellLayoutWithDrag) {
                if (cellInfo.cell != null) {
                    if (!(cellInfo.cell instanceof Folder)) {
                        // User long pressed on an item
                    	mBottomLayout.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    	((CellLayoutWithDrag)mBottomLayout).startDrag(cellInfo);
                    }
                }
                
                return true;
            }
            
//            if ((v.getParent() instanceof Workspace) && mWorkspace.allowLongPress()) {
//                if (cellInfo.cell != null) {
//                    if (!(cellInfo.cell instanceof Folder)) {
//                        // User long pressed on an item
//                        mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
//                                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
//                        mWorkspace.startDragIcs(cellInfo);
//                        
//                        return true;
//                    }
//                }
//            }
    	}
    	
    	return false;
    }
    
    @Override
    public boolean onClick(View v){
    	//QsLog.LogD("StyleDefautsWidget::onClick(0)===");
    	if(!super.onClick(v)){
    		
	    	Object tag = v.getTag();
	    	if(tag != null && (tag instanceof ShortcutInfo)) {
		            // Open shortcut
	    		//QsLog.LogD("StyleDroidIcsWidget::onClick(0)===container:"+((ShortcutInfo)tag).container
	    		//		+"==itemType:"+((ShortcutInfo)tag).itemType);
	    		
				if(((ShortcutInfo)tag).container == LauncherSettings.Favorites.CONTAINER_HOTSET) {
					 
					if(((ShortcutInfo)tag).itemType == LauncherSettings.Favorites.ITEM_TYPE_QS_FUNC_ALL_APPS){
						showAllApps(false);
						return true;
					}
					
				    final Intent intent = ((ShortcutInfo) tag).intent;
				    int[] pos = new int[2];
				    v.getLocationOnScreen(pos);
				    intent.setSourceBounds(new Rect(pos[0], pos[1],
				            pos[0] + v.getWidth(), pos[1] + v.getHeight()));
				    if(mLauncher.startActivitySafely(intent, tag)){
				    	
//				    	if (v instanceof BubbleTextViewIcs) {
//			                mWaitingForResume = (BubbleTextViewIcs) v;
//			                mWaitingForResume.setStayPressed(true);
//			            }
				    }
				    
				    return true;
				}
	    	}
    	}
    	
    	return false;
    }
 	
	@Override
	public int getWorkspaceScreenLayout(){
    	return R.layout.workspace_screen_droidics;
    }
	
	public int getDefaultQsExtAppRes(){
    	return R.xml.default_apps_droidics;
    }
	
	public int getFolderIconLayout(){
    	return R.layout.folder_icon_ics;
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState){

		super.onSaveInstanceState(outState);
		outState.putBoolean(Launcher.RUNTIME_STATE_ALL_APPS_FOLDER, false);
	}
	
	@Override
	public void bindQsExtItems(ArrayList<ItemInfo> list){
    	QsLog.LogE("bindQsExtItems()====size:"+list.size());
    	
    	mBottomLayout.removeAllViews();
    	// hot bar only add 5 view...
		final int N = Math.min(5, list.size());
        for (int i=0; i<N; i++) {
            final ItemInfo item = list.get(i);
            if(item.container == LauncherSettings.Favorites.CONTAINER_HOTSET){
            //final ShortcutInfo shortcutInfo = item.makeShortcut();
	            View shortcut = createApplicationView(item);//mLauncher.createShortcut(mShortcutLayoutResource, null, shortcutInfo);
	            if(shortcut != null){
	            	addAppInScreen(shortcut, item);
	            }
            }
        }
     }
	
	private boolean addAppInScreen(View shortcut, ItemInfo info){
    	final CellLayout cellLayout = (CellLayout)mBottomLayout;
    	int[] vacant = new int[2];
    	
    	if (cellLayout.getVacantCell(vacant, 1, 1)) {
    		
    		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) shortcut.getLayoutParams();
            if (lp == null) {
            	
                lp = new CellLayout.LayoutParams(info.cellX, info.cellY, 
                		info.spanX, info.spanY);
                
            } else {
                lp.cellX = info.cellX;
                lp.cellY = info.cellY;
                lp.cellHSpan = info.spanX;
                lp.cellVSpan = info.spanY;
            }
            
            cellLayout.addView(shortcut, -1, lp);
            return true;
        }
    	return false;
    }
	
	private View createApplicationView(ItemInfo info) {

		ImageView favorite = (ImageView) mLauncher.getLayoutInflater().inflate(R.layout.hot_set_btn_item, mBottomLayout, false);
		
		if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_QS_FUNC_ALL_APPS){
			if(Utilities.QS_ENABLE_APP_SMALL_ICON)
				favorite.setImageResource(R.drawable.zzzz_all_apps_button_icon_small);
			else
				favorite.setImageResource(R.drawable.zzzz_all_apps_button_icon);
			
			favorite.setBackgroundColor(0);
		}
		else{
			favorite.setImageBitmap(((ShortcutInfo)info).getIcon(mIconCache));
		}

        favorite.setTag(info);
        favorite.setOnClickListener(mLauncher);
        
        if((info.qsExtParam & LauncherSettings.Favorites.QS_EXT_PARAM_LOCKED) == 0){
        	favorite.setOnLongClickListener(mLauncher);
        }
        
        return favorite;
    }
}
