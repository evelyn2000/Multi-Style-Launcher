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
import android.content.res.TypedArray;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.app.WallpaperManager;

import java.util.ArrayList;

import com.android.qshome.LauncherModel;
import com.android.qshome.R;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;

public class CellLayoutIcs extends CellLayout {
     
    
    //private BubbleTextViewIcs mPressedOrFocusedIcon;
    
    private Drawable mCrosshairsDrawable = null;
    //private InterruptibleInOutAnimator mCrosshairsAnimator = null;
    private float mCrosshairsVisibility = 0.0f;
    
    private final Point mDragCenter = new Point();
    
    private final PointF mTmpPointF = new PointF();
    
//    private Drawable mNormalBackground;
//    private Drawable mActiveGlowBackground;
//    private Drawable mOverScrollForegroundDrawable;
//    private Drawable mOverScrollLeft;
//    private Drawable mOverScrollRight;
//    private Rect mBackgroundRect;
//    private Rect mForegroundRect;
//    private int mForegroundPadding;
    
    private boolean mIsDragOverlapping = false;

    public CellLayoutIcs(Context context) {
        this(context, null);
    }

    public CellLayoutIcs(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayoutIcs(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setWillNotDraw(false);
        
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayoutIcs, defStyle, 0);
//
//        mForegroundPadding = a.getDimensionPixelSize(R.styleable.CellLayoutIcs_overscrollDrawablePadding, 0);
//
//        a.recycle();
        
        final Resources res = getResources();

//        mNormalBackground = res.getDrawable(R.drawable.zzzz_ics_homescreen_blue_normal_holo);
//        mActiveGlowBackground = res.getDrawable(R.drawable.zzzz_ics_homescreen_blue_strong_holo);
//
//        mOverScrollLeft = res.getDrawable(R.drawable.zzzz_ics_overscroll_glow_left);
//        mOverScrollRight = res.getDrawable(R.drawable.zzzz_ics_overscroll_glow_right);
//
//        mNormalBackground.setFilterBitmap(true);
//        mActiveGlowBackground.setFilterBitmap(true);

        // Initialize the data structures used for the drag visualization.

        mCrosshairsDrawable = res.getDrawable(R.drawable.zzzz_ics_gardening_crosshairs);
        
        //super.setBackgroundColor(0xFF00e023);
    }
    
//    public void setPressedOrFocusedIcon(BubbleTextViewIcs icon) {
//        // We draw the pressed or focused BubbleTextView's background in CellLayout because it
//        // requires an expanded clip rect (due to the glow's blur radius)
//    	BubbleTextViewIcs oldIcon = mPressedOrFocusedIcon;
//        mPressedOrFocusedIcon = icon;
//        //QsLog.LogD("CellLayoutIcs::setPressedOrFocusedIcon()======"+(mPressedOrFocusedIcon == null));
//        if (oldIcon != null) {
//            invalidateBubbleTextView(oldIcon);
//        }
//        if (mPressedOrFocusedIcon != null) {
//            invalidateBubbleTextView(mPressedOrFocusedIcon);
//        }
//    }
//    
//    private void invalidateBubbleTextView(BubbleTextViewIcs icon) {
//        final int padding = icon.getPressedOrFocusedBackgroundPadding();
//        invalidate(icon.getLeft() + getPaddingLeft() - padding,
//                icon.getTop() + getPaddingTop() - padding,
//                icon.getRight() + getPaddingLeft() + padding,
//                icon.getBottom() + getPaddingTop() + padding);
//    }
//    
    public void setIsDragOverlapping(boolean isDragOverlapping) {
        if (mIsDragOverlapping != isDragOverlapping) {
            mIsDragOverlapping = isDragOverlapping;
            invalidate();
        }
    }

    public boolean getIsDragOverlapping() {
        return mIsDragOverlapping;
    }
 
    /**
     * A drag event has begun over this layout.
     * It may have begun over this layout (in which case onDragChild is called first),
     * or it may have begun on another layout.
     */
    public void onDragEnter() {
    	mCrosshairsVisibility = 0.8f;
    	//QsLog.LogD("CellLayoutIcs::onDragEnter()======");
    }

    /**
     * Called when drag has left this CellLayout or has been completed (successfully or not)
     */
    public void onDragExit() {
        // This can actually be called when we aren't in a drag, e.g. when adding a new
        // item to this layout via the customize drawer.
        // Guard against that case.
    	mCrosshairsVisibility = 0.0f;
    	//QsLog.LogD("CellLayoutIcs::onDragExit()======");
    }
       
    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        
        //QsLog.LogD("CellLayoutIcs::dispatchDraw(0)======");
        
        if (mCrosshairsVisibility > 0.0f) {
            final int countX = getCountX();
            final int countY = getCountY();

            final float MAX_ALPHA = 0.6f;
            final int MAX_VISIBLE_DISTANCE = 600;
            final float DISTANCE_MULTIPLIER = 0.002f;
            final int alphaValue = (int) (MAX_ALPHA * 255 * mCrosshairsVisibility);

            final Drawable d = mCrosshairsDrawable;
            final int width = d.getIntrinsicWidth();
            final int height = d.getIntrinsicHeight();
            //QsLog.LogE("CellLayoutIcs::dispatchDraw(0)======countX:"+countX+"==countY:"+countY
            //		+"==top:"+getPaddingTop()+"=left:"+getLeftPadding()
            //		+"=mWidthGap:"+mWidthGap+"=mHeightGap:"+mHeightGap+"=mCellHeight:"+mCellHeight);
            int x = getLeftPadding()/* - (mWidthGap / 2)*/ - (width / 2);
            
            for (int col = 0; col <= countX; col++) {
                int y = getTopPadding()/* - (mHeightGap / 2)*/ - (height / 2);//getPaddingTop() - (mHeightGap / 2) - (height / 2);
                //QsLog.LogW("CellLayoutIcs::dispatchDraw(1)======col:"+col+"==x:"+x+"==y:"+y);
                for (int row = 0; row <= countY; row++) {
                	//QsLog.LogD("CellLayoutIcs::dispatchDraw(2)======row:"+row+"==x:"+x+"==y:"+y);
//                    mTmpPointF.set(x - mDragCenter.x, y - mDragCenter.y);
//                    float dist = mTmpPointF.length();
                    // Crosshairs further from the drag point are more faint
//                    float alpha = Math.min(MAX_ALPHA,
//                            DISTANCE_MULTIPLIER * (MAX_VISIBLE_DISTANCE - dist));
//                    if (alpha > 0.0f) {
                        d.setBounds(x, y, x + width, y + height);
                        //d.setAlpha((int) (MAX_ALPHA * 255 * mCrosshairsVisibility));
                        d.setAlpha(alphaValue);
                        d.draw(canvas);
                    //}
                    if(row < countY)
                    	y += mCellHeight + mHeightGap;
                    else
                    	y += mCellHeight;
                }
                
                if(col < countX)
                	x += mCellWidth + mWidthGap;
                else
                	x += mCellWidth;
            }
        }
        
        // We draw the pressed or focused BubbleTextView's background in CellLayout because it
        // requires an expanded clip rect (due to the glow's blur radius)
//        if (mPressedOrFocusedIcon != null) {
//        	QsLog.LogD("CellLayoutIcs::dispatchDraw(5)======");
//            final int padding = mPressedOrFocusedIcon.getPressedOrFocusedBackgroundPadding();
//            final Bitmap b = mPressedOrFocusedIcon.getPressedOrFocusedBackground();
//            if (b != null) {
//                canvas.drawBitmap(b,
//                        mPressedOrFocusedIcon.getLeft() + getPaddingLeft() - padding,
//                        mPressedOrFocusedIcon.getTop() + getPaddingTop() - padding,
//                        null);
//            }
//        }
    }

}


