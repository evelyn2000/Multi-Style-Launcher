/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherAppWidgetHost;
import com.android.qshome.LauncherModel;
import com.android.qshome.R;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.util.LauncherSettings;


/**
 * Implements a DropTarget.
 */
public class ButtonDropTarget extends TextView implements DropTarget, DragController.DragListener {

    protected final int mTransitionDuration;

    protected Launcher mLauncher;
    private DragController mDragController;
    private View mHandle;
    private int mBottomDragPadding;
    protected TextView mText;
    protected SearchDropTargetBar mSearchDropTargetBar;

    /** Whether this drop target is active for the current drag */
    protected boolean mActive;

    /** The paint applied to the drag view on hover */
    protected final Paint mHoverPaint = new Paint();

    public ButtonDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources r = getResources();
        mTransitionDuration = r.getInteger(R.integer.config_dropTargetBgTransitionDuration);
        mBottomDragPadding = r.getDimensionPixelSize(R.dimen.drop_target_drag_padding);
    }

    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }
    
    public void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    public void setHandle(View view) {
        mHandle = view;
    }
    
    public boolean acceptDrop(DragObject dragObject) {
    	return false;
    }
    
    public Rect estimateDropLocation(DragObject dragObject, Rect recycle) {
        return null;
    }

    public boolean onDrop(DragObject dragObject) {
    	// Do nothing
        return false;
    }

    public void onDragEnter(DragObject dragObject) {
    	dragObject.dragView.setPaint(mHoverPaint);
    }

    public void onDragOver(DragObject dragObject) {
    	// Do nothing
    }

    public void onDragExit(DragObject dragObject) {
    	dragObject.dragView.setPaint(null);
    }

    public void onDragStart(DragSource source, Object info, int dragAction) {
    	// Do nothing
    }

    public void onDragEnd() {
    	// Do nothing
    }
    
    public boolean isDropEnabled() {
        return mActive;
    }

    public void setSearchDropTargetBar(SearchDropTargetBar searchDropTargetBar) {
        mSearchDropTargetBar = searchDropTargetBar;
    }

    @Override
    public void getHitRect(android.graphics.Rect outRect) {
        super.getHitRect(outRect);
        outRect.bottom += mBottomDragPadding;
    }

    @Override
    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    public void getLocationInDragLayer(int[] loc) {
        //mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    	getLocationOnScreen(loc);
    }
}
