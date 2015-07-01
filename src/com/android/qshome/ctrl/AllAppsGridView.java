/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 */

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

package com.android.qshome.ctrl;

import java.util.ArrayList;
import java.util.Collections;

import com.android.qshome.AllAppsList;
import com.android.qshome.AllAppsView;
import com.android.qshome.ApplicationsAdapter;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.AllApps2D.AppsAdapter;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.util.QsLog;

import android.widget.GridView;
import android.widget.AdapterView;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;

import com.android.qshome.R;

public class AllAppsGridView extends GridView implements AllAppsView, 
		AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, DragSource {

    private DragController mDragController;
    private Launcher mLauncher;
    private Bitmap mTexture;
    private Paint mPaint;
    private int mTextureWidth;
    private int mTextureHeight;
    
    private ArrayList<ApplicationInfo> mAllAppsList = new ArrayList<ApplicationInfo>();
    private ApplicationsAdapter mAppsAdapter;

    public AllAppsGridView(Context context) {
        super(context);
    }

    public AllAppsGridView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.gridViewStyle);
    }

    public AllAppsGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AllAppsGridView, defStyle, 0);
        final int textureId = a.getResourceId(R.styleable.AllAppsGridView_texture, 0);
        if (textureId != 0) {
            mTexture = BitmapFactory.decodeResource(getResources(), textureId);
            mTextureWidth = mTexture.getWidth();
            mTextureHeight = mTexture.getHeight();

            mPaint = new Paint();
            mPaint.setDither(false);
        }
        a.recycle();
        
        mAppsAdapter = new ApplicationsAdapter(getContext(), mAllAppsList);
        mAppsAdapter.setNotifyOnChange(false);
        
        //super.setRecycleScroll(true);
    }

    @Override
    public boolean isOpaque() {
        return !mTexture.hasAlpha();
    }
    
    public boolean isVisible() {
        return (super.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onFinishInflate() {
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
        
        setAdapter(mAppsAdapter);
    }

    @Override
    public void draw(Canvas canvas) {
        final Bitmap texture = mTexture;
        final Paint paint = mPaint;

        final int width = getWidth();
        final int height = getHeight();

        final int textureWidth = mTextureWidth;
        final int textureHeight = mTextureHeight;

        int x = 0;
        int y;

        while (x < width) {
            y = 0;
            while (y < height) {
                canvas.drawBitmap(texture, x, y, paint);
                y += textureHeight;
            }
            x += textureWidth;
        }

        super.draw(canvas);
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
        ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
        mLauncher.startActivitySafely(app.intent, app);
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!view.isInTouchMode()) {
            return false;
        }

        ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
        app = new ApplicationInfo(app);

        mDragController.startDrag(view, this, app, DragController.DRAG_ACTION_COPY);
        mLauncher.closeAllApps();

        return true;
    }

    public void setDragController(DragController dragger) {
    	mDragController = dragger;
    }

    public void onDropCompleted(View target, DragObject d, boolean success) {
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }
    
    public void setApps(ArrayList<ApplicationInfo> list) {
        mAllAppsList.clear();
        //addApps(list);
        mAllAppsList.addAll(list);
        mAppsAdapter.notifyDataSetChanged();
    }
    
    public void reorderApps(){
    	
    	if (AllAppsList.mTopPackages == null || AllAppsList.mTopPackages.isEmpty()) {
        	return ;
        }
        
        ArrayList<ApplicationInfo> appsClone = ( ArrayList<ApplicationInfo>)(mAllAppsList.clone());
        
        for (AllAppsList.TopPackage tp : AllAppsList.mTopPackages) {
        	
        	for (ApplicationInfo ri : appsClone) {
        		if (ri.componentName.getPackageName().equals(tp.mPackageName) 
        				&& ri.componentName.getClassName().equals(tp.mClassName)) {
        			mAllAppsList.remove(ri);
        			mAllAppsList.add(Math.min(Math.max(tp.mOrder, 0), mAllAppsList.size()), ri);
        			
        			break;
        		}
        	}
        }
    }

    public void addApps(ArrayList<ApplicationInfo> list) {
//        Log.d(TAG, "addApps: " + list.size() + " apps: " + list.toString());

        final int N = list.size();

        for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            int index =  Collections.binarySearch(mAllAppsList, item,
	                    LauncherModel.APP_NAME_COMPARATOR);

            if (index < 0) {
                index = -(index+1);
            }
            mAllAppsList.add(index, item);
        }
        
        //reorderApps();
        
        mAppsAdapter.notifyDataSetChanged();
    }

    public void removeApps(ArrayList<ApplicationInfo> list) {
        final int N = list.size();
        for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            int index = findAppByComponent(mAllAppsList, item);
            if (index >= 0) {
                mAllAppsList.remove(index);
            } else {
                QsLog.LogW("couldn't find a match for item \"" + item + "\"");
                // Try to recover.  This should keep us from crashing for now.
            }
        }
        mAppsAdapter.notifyDataSetChanged();
    }

    public void updateApps(ArrayList<ApplicationInfo> list) {
        // Just remove and add, because they may need to be re-sorted.
        removeApps(list);
        addApps(list);
    }
    
    public void surrender() {
    }
    
    public void release(){
    	mAllAppsList.clear();
    	//mAllAppsList = null;
    }
    
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
    
    public void dumpState(){
    	
    }
    
    public void zoom(float zoom, boolean animate) {

    }
}
