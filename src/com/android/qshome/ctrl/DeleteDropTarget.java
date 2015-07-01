/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.qshome.ctrl;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.android.qshome.AllAppsView;
import com.android.qshome.LauncherAppWidgetHost;
import com.android.qshome.LauncherModel;
import com.android.qshome.R;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.FolderInfo;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;

public class DeleteDropTarget extends ButtonDropTarget {

    private static int DELETE_ANIMATION_DURATION = 250;
    private ColorStateList mOriginalTextColor;
    private int mHoverColor = 0xFFFF0000;
    private TransitionDrawable mUninstallDrawable;
    private TransitionDrawable mRemoveDrawable;
    private TransitionDrawable mCurrentDrawable;

    public DeleteDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the drawable
        mOriginalTextColor = getTextColors();

        // Get the hover color
        Resources r = getResources();
        mHoverColor = r.getColor(R.color.delete_target_hover_tint);
        mHoverPaint.setColorFilter(new PorterDuffColorFilter(
                mHoverColor, PorterDuff.Mode.SRC_ATOP));
        mUninstallDrawable = (TransitionDrawable) 
                r.getDrawable(R.drawable.zzzz_ics_uninstall_target_selector);
        mRemoveDrawable = (TransitionDrawable) r.getDrawable(R.drawable.zzzz_ics_remove_target_selector);

        mRemoveDrawable.setCrossFadeEnabled(true);
        mUninstallDrawable.setCrossFadeEnabled(true);

        // The current drawable is set to either the remove drawable or the uninstall drawable 
        // and is initially set to the remove drawable, as set in the layout xml.
        mCurrentDrawable = (TransitionDrawable) getCompoundDrawables()[0];

        // Remove the text in the Phone UI in landscape
//        int orientation = getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            if (!LauncherApplication.isScreenLarge()) {
//                setText("");
//            }
//        }
    }

    private boolean isAllAppsApplication(DragSource source, Object info) {
    	if(info instanceof ApplicationInfo){
    		if(source instanceof AllAppsView)
    			return true;
    		
//    		if((source instanceof Workspace))
//    			return ((Workspace)source).getIsApplicationMode();
    		
    	}
        return false;//(source instanceof AllAppsGridLayoutPages) && (info instanceof ApplicationInfo);
    }
    private boolean isAllAppsWidget(DragSource source, Object info) {
        return false;//(source instanceof AppsCustomizePagedView) && (info instanceof PendingAddWidgetInfo);
    }
    private boolean isDragSourceWorkspaceOrFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) || (d.dragSource instanceof Folder);
    }
    private boolean isWorkspaceOrFolderApplication(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof ShortcutInfo);
    }
    private boolean isWorkspaceOrFolderWidget(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof LauncherAppWidgetInfo);
    }
    private boolean isWorkspaceFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) && (d.dragInfo instanceof FolderInfo);
    }

    @Override
    //public boolean acceptDrop(DragObject d) {
    public boolean acceptDrop(DragObject dragObject) {
        // We can remove everything including App shortcuts, folders, widgets, etc.
        return true;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        boolean isVisible = true;
        boolean isUninstall = false;

        // If we are dragging a widget from AppsCustomize, hide the delete target
//        if (isAllAppsWidget(source, info)) {
//            isVisible = false;
//        }

        // If we are dragging an application from AppsCustomize, only show the control if we can
        // delete the app (it was downloaded), and rename the string to "uninstall" in such a case
        if (isAllAppsApplication(source, info)) {
            ApplicationInfo appInfo = (ApplicationInfo) info;
            if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) != 0) {
                isUninstall = true;
            } else {
                isVisible = false;
            }
        }
        //QsLog.LogE("DeleteDropTarget::onDragStart()==isVisible:"+isVisible+"==isUninstall:"+isUninstall);
        //if(mLauncher.isScreenPortrait()){
        	if (isUninstall) {
        		setCompoundDrawablesWithIntrinsicBounds(mUninstallDrawable, null, null, null);
	        } else {
	        	setCompoundDrawablesWithIntrinsicBounds(mRemoveDrawable, null, null, null);
	        }
        	mCurrentDrawable = (TransitionDrawable) getCompoundDrawables()[0];
//        }else{
//        	if (isUninstall) {
//        		setCompoundDrawablesWithIntrinsicBounds(null, mUninstallDrawable, null, null);
//	        } else {
//	        	setCompoundDrawablesWithIntrinsicBounds(null, mRemoveDrawable, null, null);
//	        }
//        	mCurrentDrawable = (TransitionDrawable) getCompoundDrawables()[1];
//        }
        
        mActive = isVisible;
        mCurrentDrawable.resetTransition();
        setTextColor(mOriginalTextColor);
        ((ViewGroup) getParent()).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        
        if (getText().length() > 0) {
            setText(isUninstall ? R.string.delete_target_uninstall_label
                : R.string.delete_target_label);
        }
        
        //QsLog.LogD("DeleteDropTarget::onDragStart()==mActive:"+mActive);
    }

    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
    }

    public void onDragEnter(DragObject dragObject) {
    	super.onDragEnter(dragObject);
    	//QsLog.LogD("DeleteDropTarget::onDragEnter()==mActive:"+mActive);
        mCurrentDrawable.startTransition(mTransitionDuration);
        setTextColor(mHoverColor);
    }

    public void onDragExit(DragObject dragObject) {
    	super.onDragExit(dragObject);
    	//QsLog.LogD("DeleteDropTarget::onDragExit()==");
        if (!dragObject.dragComplete) {
            mCurrentDrawable.resetTransition();
            setTextColor(mOriginalTextColor);
        }
    }

    private void animateToTrashAndCompleteDrop(final DragObject d) {
//        DragLayer dragLayer = mLauncher.getDragLayer();
//        Rect from = new Rect();
//        Rect to = new Rect();
//        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
//        dragLayer.getViewRectRelativeToSelf(this, to);
//
//        int width = mCurrentDrawable.getIntrinsicWidth();
//        int height = mCurrentDrawable.getIntrinsicHeight();
//        to.set(to.left + getPaddingLeft(), to.top + getPaddingTop(),
//                to.left + getPaddingLeft() + width, to.bottom);
//
//        // Center the destination rect about the trash icon
//        int xOffset = (int) -(d.dragView.getMeasuredWidth() - width) / 2;
//        int yOffset = (int) -(d.dragView.getMeasuredHeight() - height) / 2;
//        to.offset(xOffset, yOffset);

        mSearchDropTargetBar.deferOnDragEnd();
        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                mSearchDropTargetBar.onDragEnd();
                //mLauncher.exitSpringLoadedDragMode();
                completeDrop(d);
            }
        };
//        dragLayer.animateView(d.dragView, from, to, 0.1f, 0.1f,
//                DELETE_ANIMATION_DURATION, new DecelerateInterpolator(2),
//                new DecelerateInterpolator(1.5f), onAnimationEndRunnable, false);
    }

    private void completeDrop(DragObject dragObject) {
        ItemInfo item = (ItemInfo) dragObject.dragInfo;
        //QsLog.LogE("DeleteDropTarget::completeDrop()=="+item.toString());
        
        if (isAllAppsApplication(dragObject.dragSource, item)) {        	
        	//QsLog.LogE("DeleteDropTarget::completeDrop(1)====app===");
        	
        	// Uninstall the application if it is being dragged from AppsCustomize
		    mLauncher.startApplicationUninstallActivity((ApplicationInfo) item);
		    
		    if((dragObject.dragSource instanceof Workspace)){
		    	LauncherModel.deleteItemFromDatabase(mLauncher, item, false);
		    }
		    return;
		}

        if (item.container == -1) return;
        
        if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            if (item instanceof LauncherAppWidgetInfo) {
                mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
            }
        } else {
            if (dragObject.dragSource instanceof UserFolder) {
                final UserFolder userFolder = (UserFolder) dragObject.dragSource;
                final UserFolderInfo userFolderInfo = (UserFolderInfo) userFolder.getInfo();
                // Item must be a ShortcutInfo otherwise it couldn't have been in the folder
                // in the first place.
                userFolderInfo.remove((ShortcutInfo)item);
            }
        }
        
        if (item instanceof UserFolderInfo) {
            final UserFolderInfo userFolderInfo = (UserFolderInfo)item;
            LauncherModel.deleteUserFolderContentsFromDatabase(mLauncher, userFolderInfo, false);
            mLauncher.removeFolder(userFolderInfo);
        } else if (item instanceof LauncherAppWidgetInfo) {
            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
            }
        }
        LauncherModel.deleteItemFromDatabase(mLauncher, item, false);
    }
    @Override
    public boolean onDrop(DragObject d) {
    	//QsLog.LogE("DeleteDropTarget::onDrop(1)=======");
        //animateToTrashAndCompleteDrop(d);
        completeDrop(d);
        return true;
    }
}
