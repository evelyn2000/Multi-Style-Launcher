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

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class DragLayer extends FrameLayout {
	public DragController mDragController;

    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDragController(DragController controller) {
        mDragController = controller;
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	int type = ev.getAction();
    	boolean result = super.dispatchTouchEvent(ev);    	
    	boolean isDraging = mDragController.isDraging();
    	
    	if (type == MotionEvent.ACTION_CANCEL || type == MotionEvent.ACTION_UP) {
    		if (isDraging) {
    			//  if after MotionEvent.ACTION_UP event , the state is still draging
    			//  we need to call onIntercepTouchEvent to clear the draging state
    			//android.util.Log.w("Launcher2","(DragLayer)dispatchTouchEvent failed to process the event " ); 
    			onInterceptTouchEvent(ev);
			}
    	}

    	return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragController.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDragController.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mDragController.dispatchUnhandledMove(focused, direction);
    }
    
    public void getLocationInDragLayer(View child, int[] loc) {
        loc[0] = 0;
        loc[1] = 0;
        //getDescendantCoordRelativeToSelf(child, loc);
        child.getLocationOnScreen(loc);
    }
    
    /**
     * Given a coordinate relative to the descendant, find the coordinate in this DragLayer's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param coord The coordinate that we want mapped.
     * @return The factor by which this descendant is scaled relative to this DragLayer.
     */
//    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord) {
//        float scale = 1.0f;
//        float[] pt = {coord[0], coord[1]};
////        descendant.getMatrix().mapPoints(pt);
////        scale *= descendant.getScaleX();
////        pt[0] += descendant.getLeft();
////        pt[1] += descendant.getTop();
////        ViewParent viewParent = descendant.getParent();
////        while (viewParent instanceof View && viewParent != this) {
////            final View view = (View)viewParent;
////            view.getMatrix().mapPoints(pt);
////            scale *= view.getScaleX();
////            pt[0] += view.getLeft() - view.getScrollX();
////            pt[1] += view.getTop() - view.getScrollY();
////            viewParent = view.getParent();
////        }
//        coord[0] = (int) Math.round(pt[0]);
//        coord[1] = (int) Math.round(pt[1]);
//        return scale;
//    }
//
//    public void getViewRectRelativeToSelf(View v, Rect r) {
//        int[] loc = new int[2];
//        getLocationInWindow(loc);
//        int x = loc[0];
//        int y = loc[1];
//
//        v.getLocationInWindow(loc);
//        int vX = loc[0];
//        int vY = loc[1];
//
//        int left = vX - x;
//        int top = vY - y;
//        r.set(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
//    }
}
