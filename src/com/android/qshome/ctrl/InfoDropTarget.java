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

import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.qshome.AllAppsView;
import com.android.qshome.R;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.util.QsLog;

public class InfoDropTarget extends ButtonDropTarget {

    private ColorStateList mOriginalTextColor;
    private TransitionDrawable mDrawable;
    private int mHoverColor = 0xFF0000FF;

    public InfoDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mOriginalTextColor = getTextColors();

        // Get the hover color
        Resources r = getResources();
        mHoverColor = r.getColor(R.color.info_target_hover_tint);
        mHoverPaint.setColorFilter(new PorterDuffColorFilter(
                mHoverColor, PorterDuff.Mode.SRC_ATOP));
        //if(mLauncher.isScreenPortrait())
        mDrawable = (TransitionDrawable) getCompoundDrawables()[0];
        if(mDrawable == null)
        	mDrawable = (TransitionDrawable) getCompoundDrawables()[1];
        mDrawable.setCrossFadeEnabled(true);

        // Remove the text in the Phone UI in landscape
//        int orientation = getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            if (!LauncherApplication.isScreenLarge()) {
//                setText("");
//            }
//        }
    }

    private boolean isAllAppsApplication(DragSource source, Object info) {
        return (source instanceof AllAppsView) && (info instanceof ApplicationInfo);
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        // acceptDrop is called just before onDrop. We do the work here, rather than
        // in onDrop, because it allows us to reject the drop (by returning false)
        // so that the object being dragged isn't removed from the drag source.
        ComponentName componentName = null;
        if (dragObject.dragInfo instanceof ApplicationInfo) {
            componentName = ((ApplicationInfo) dragObject.dragInfo).componentName;
        } else if (dragObject.dragInfo instanceof ShortcutInfo) {
            componentName = ((ShortcutInfo) dragObject.dragInfo).intent.getComponent();
        }
        if (componentName != null) {
            mLauncher.startApplicationDetailsActivity(componentName);
        }
        return false;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        boolean isVisible = true;

        // If we are dragging a widget or shortcut, hide the info target
        if (!isAllAppsApplication(source, info)) {
            isVisible = false;
        }

        mActive = isVisible;
        mDrawable.resetTransition();
        setTextColor(mOriginalTextColor);
        ((ViewGroup) getParent()).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        
        //QsLog.LogD("InfoDropTarget::onDragStart()==mActive:"+mActive);
    }

    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
    }

    public void onDragEnter(DragObject dragObject) {
    	super.onDragEnter(dragObject);
    	//QsLog.LogD("InfoDropTarget::onDragEnter()=======");
        mDrawable.startTransition(mTransitionDuration);
        setTextColor(mHoverColor);
    }

    public void onDragExit(DragObject dragObject) {
    	super.onDragExit(dragObject);
    	//QsLog.LogD("InfoDropTarget::onDragExit()=======");
        if (!dragObject.dragComplete) {
            mDrawable.resetTransition();
            setTextColor(mOriginalTextColor);
        }
    }
}
