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
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.IBinder;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import com.android.qshome.Launcher;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.util.QsLog;


/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController {
    @SuppressWarnings({"UnusedDeclaration"})
    private static final String TAG = "Launcher.DragController";

    /** Indicates the drag is a move.  */
    public static int DRAG_ACTION_MOVE = 0;

    /** Indicates the drag is a copy.  */
    public static int DRAG_ACTION_COPY = 1;
    
    /** Indicates the drag is a ADD. */
    public static int DRAG_ACTION_ADD = 2;

    /** Indicates the drag is a ADD. */
    public static int DRAG_ACTION_NONE = -1;

    /** Indicates the drag MODE. */
    private int mDragActionMode = DRAG_ACTION_NONE;
    private static final int SCROLL_DELAY = 600;
    private static final int SCROLL_ZONE = 20;
    private static final int VIBRATE_DURATION = 35;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    private static final int SCROLL_OUTSIDE_ZONE = 0;
    private static final int SCROLL_WAITING_IN_ZONE = 1;

    private static final int SCROLL_LEFT = 0;
    private static final int SCROLL_RIGHT = 1;

    private Context mContext;
    private Handler mHandler;
    private final Vibrator mVibrator;// = new Vibrator();

    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];

    /** Whether or not we're dragging. */
    private boolean mDragging;

    /** X coordinate of the down event. */
    private int mMotionDownX;

    /** Y coordinate of the down event. */
    private int mMotionDownY;

    /** Info about the screen for clamping. */
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    /** Original view that is being dragged.  */
    private View mOriginator;
//
//    /** X offset from the upper-left corner of the cell to where we touched.  */
//    private float mTouchOffsetX;
//
//    /** Y offset from the upper-left corner of the cell to where we touched.  */
//    private float mTouchOffsetY;
//
//    /** Where the drag originated */
//    private DragSource mDragSource;
//
//    /** The data associated with the object being dragged */
//    private Object mDragInfo;
//
//    /** The view that moves around while you drag.  */
//    private DragView mDragView;

    private DropTarget.DragObject mDragObject;
    
    /** Who can receive drop events */
    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();

    //private DragListener mListener;
    private ArrayList<DragListener> mListeners = new ArrayList<DragListener>();

    /** The window token used as the parent for the DragView. */
    private IBinder mWindowToken;

    /** The view that will be scrolled when dragging to the left and right edges of the screen. */
    private View mScrollView;

    private View mMoveTarget;

    private DragScroller mDragScroller;
    private int mScrollState = SCROLL_OUTSIDE_ZONE;
    private ScrollRunnable mScrollRunnable = new ScrollRunnable();

    private RectF mDeleteRegion;
    private DropTarget mLastDropTarget;

    private InputMethodManager mInputMethodManager;

    /**
     * Interface to receive notifications when a drag starts or stops
     */
    public interface DragListener {
        
        /**
         * A drag has begun
         * 
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *        or {@link DragController#DRAG_ACTION_COPY}
         */
    	public void onDragStart(DragSource source, Object info, int dragAction);
        
        /**
         * The drag has eneded
         */
    	public void onDragEnd();
    }
    
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     */
    public DragController(Context context) {
        mContext = context;
        mHandler = new Handler();
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Starts a drag.
     * 
     * @param v The view that is being dragged
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction) {
    	startDrag(v, source, dragInfo, dragAction, null);
    }
    
    /**
     * Starts a drag.
     *
     * @param v The view that is being dragged
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     * @param dragRegion Coordinates within the bitmap b for the position of item being dragged.
     *          Makes dragging feel more precise, e.g. you can clip out a transparent border
     */
    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction,
            Rect dragRegion) {
    	
    	mDragActionMode = dragAction;
        mOriginator = v;

        Bitmap b = getViewBitmap(v);

        if (b == null) {
            // out of memory?
            return;
        }

        int[] loc = mCoordinatesTemp;
        v.getLocationOnScreen(loc);
        int screenX = loc[0];
        int screenY = loc[1];

        startDrag(b, screenX, screenY, 0, 0, b.getWidth(), b.getHeight(),
                source, dragInfo, dragAction);

        b.recycle();

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(View.GONE);
        }
        
    }

    /**
     * Starts a drag.
     * 
     * @param b The bitmap to display as the drag image.  It will be re-scaled to the
     *          enlarged size.
     * @param screenX The x position on screen of the left-top of the bitmap.
     * @param screenY The y position on screen of the left-top of the bitmap.
     * @param textureLeft The left edge of the region inside b to use.
     * @param textureTop The top edge of the region inside b to use.
     * @param textureWidth The width of the region inside b to use.
     * @param textureHeight The height of the region inside b to use.
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(Bitmap b, int screenX, int screenY,
            int textureLeft, int textureTop, int textureWidth, int textureHeight,
            DragSource source, Object dragInfo, int dragAction) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

//        if (mListener != null) {
//            mListener.onDragStart(source, dragInfo, dragAction);
//        }
        QsLog.LogD("DragController::startDrag(1)======"+mListeners.size());
        for (DragListener listener : mListeners) {
            listener.onDragStart(source, dragInfo, dragAction);
        }

        int registrationX = ((int)mMotionDownX) - screenX;
        int registrationY = ((int)mMotionDownY) - screenY;

        //mTouchOffsetX = mMotionDownX - screenX;
        //mTouchOffsetY = mMotionDownY - screenY;

        mDragging = true;
        mDragObject = new DropTarget.DragObject();
        
        mDragObject.dragComplete = false;
        mDragObject.xOffset = mMotionDownX - screenX;
        mDragObject.yOffset = mMotionDownY - screenY;
        mDragObject.dragSource = source;
        mDragObject.dragInfo = dragInfo;
        
//        mDragSource = source;
//        mDragInfo = dragInfo;

        mVibrator.vibrate(VIBRATE_DURATION);
        
        DragView dragView = mDragObject.dragView = new DragView(mContext, b, registrationX, registrationY,
                textureLeft, textureTop, textureWidth, textureHeight);
        dragView.show(mWindowToken, (int)mMotionDownX, (int)mMotionDownY);
    }
    
//    public void startDrag(Bitmap b, int screenX, int screenY, Point dragOffset, Rect dragRegion,
//            DragSource source, Object dragInfo, int dragAction) {
//    	if (PROFILE_DRAWING_DURING_DRAG) {
//            android.os.Debug.startMethodTracing("Launcher");
//        }
//
//        // Hide soft keyboard, if visible
//        if (mInputMethodManager == null) {
//            mInputMethodManager = (InputMethodManager)
//                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        }
//        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);
//
//        for (DragListener listener : mListeners) {
//            listener.onDragStart(source, dragInfo, dragAction);
//        }
//
//        final int registrationX = mMotionDownX - screenX;
//        final int registrationY = mMotionDownY - screenY;
//
//        final int dragRegionLeft = dragRegion == null ? 0 : dragRegion.left;
//        final int dragRegionTop = dragRegion == null ? 0 : dragRegion.top;
//
//        mDragging = true;
//
//        mDragObject = new DropTarget.DragObject();
//
//        mDragObject.dragComplete = false;
//        mDragObject.xOffset = mMotionDownX - (screenX + dragRegionLeft);
//        mDragObject.yOffset = mMotionDownY - (screenY + dragRegionTop);
//        mDragObject.dragSource = source;
//        mDragObject.dragInfo = dragInfo;
//
//        mVibrator.vibrate(VIBRATE_DURATION);
//
//        final DragView dragView = mDragObject.dragView = new DragView(mContext, b, registrationX,
//                registrationY, 0, 0, b.getWidth(), b.getHeight());
//
//        if (dragOffset != null) {
//            dragView.setDragVisualizeOffset(new Point(dragOffset));
//        }
//        if (dragRegion != null) {
//            dragView.setDragRegion(new Rect(dragRegion));
//        }
//
//        dragView.show(mWindowToken, (int)mMotionDownX, (int)mMotionDownY);
//        //handleMoveEvent(mMotionDownX, mMotionDownY);
//    }

    /**
     * Draw the view into a bitmap.
     */
    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    /**
     * Call this from a drag source view like this:
     *
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        if (mDragging) {
        	if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.cancelled = true;
            mDragObject.dragComplete = true;
            mDragObject.dragSource.onDropCompleted(null, mDragObject, false);
            //mDragSource.onDropCompleted((View) mDragSource, false);
        }
        endDrag();
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
            if (mOriginator != null) {
                mOriginator.setVisibility(View.VISIBLE);
            }
            
            for (DragListener listener : mListeners) {
                listener.onDragEnd();
            }
            if (mDragObject.dragView != null) {
                mDragObject.dragView.remove();
                mDragObject.dragView = null;
            }
            
//            if (mListener != null) {
//                mListener.onDragEnd();
//            }
//            if (mDragView != null) {
//                mDragView.remove();
//                mDragView = null;
//            }
        }
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (false) {
            Log.d(Launcher.TAG, "DragController.onInterceptTouchEvent " + ev + " mDragging="
                    + mDragging);
        }
        final int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            recordScreenSize();
        }

        final int screenX = clamp((int)ev.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int)ev.getRawY(), 0, mDisplayMetrics.heightPixels);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = screenX;
                mMotionDownY = screenY;
                mLastDropTarget = null;
                break;
            case MotionEvent.ACTION_UP:
                if (mDragging) {
                    drop(screenX, screenY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return mDragging;
    }

    /**
     * Sets the view that should handle move events.
     */
    public void setMoveTarget(View view) {
        mMoveTarget = view;
    }    

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        View scrollView = mScrollView;

        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final int screenX = clamp((int)ev.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int)ev.getRawY(), 0, mDisplayMetrics.heightPixels);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            // Remember where the motion event started
            mMotionDownX = screenX;
            mMotionDownY = screenY;

            if ((screenX < SCROLL_ZONE) || (screenX > scrollView.getWidth() - SCROLL_ZONE)) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else {
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }

            break;
        case MotionEvent.ACTION_MOVE:
            // Update the drag view.  Don't use the clamped pos here so the dragging looks
            // like it goes off screen a little, intead of bumping up against the edge.
        	mDragObject.dragView.move((int)ev.getRawX(), (int)ev.getRawY());

            // Drop on someone?
            final int[] coordinates = mCoordinatesTemp;
            DropTarget dropTarget = findDropTarget(screenX, screenY, coordinates);
            if (dropTarget != null) {
                if (mLastDropTarget == dropTarget) {
//                    dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
//                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                    dropTarget.onDragOver(mDragObject);
                } else {
                    if (mLastDropTarget != null) {
//                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
//                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                        mLastDropTarget.onDragExit(mDragObject);
                    }
//                    dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
//                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                    //QsLog.LogW("DragController::onTouchEvent(4)====onDragEnter===");
                    dropTarget.onDragEnter(mDragObject);
                }
            } else {
            	//QsLog.LogW("DragController::onTouchEvent(5)====null===");
                if (mLastDropTarget != null) {
//                    mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
//                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                    mLastDropTarget.onDragExit(mDragObject);
                }
            }
            mLastDropTarget = dropTarget;

            // Scroll, maybe, but not if we're in the delete region.
            boolean inDeleteRegion = false;
            if (mDeleteRegion != null) {
                inDeleteRegion = mDeleteRegion.contains(screenX, screenY);
            }
            if (!inDeleteRegion && screenX < SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_LEFT);
                    mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else if (!inDeleteRegion && screenX > scrollView.getWidth() - SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else {
                if (mScrollState == SCROLL_WAITING_IN_ZONE) {
                    mScrollState = SCROLL_OUTSIDE_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    mHandler.removeCallbacks(mScrollRunnable);
                }
            }

            break;
        case MotionEvent.ACTION_UP:
            mHandler.removeCallbacks(mScrollRunnable);
            if (mDragging) {
                drop(screenX, screenY);
            }
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
            cancelDrag();
        }

        return true;
    }

    public boolean isSystemApp(ShortcutInfo info) {
    	final ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(info.intent, 0);
    	if ((resolveInfo.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }

        return false;
    }
    
    private boolean drop(float x, float y) {
        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);
        
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        boolean accepted = false;
        if (dropTarget != null) {
            mDragObject.dragComplete = true;
            dropTarget.onDragExit(mDragObject);
            if (dropTarget.acceptDrop(mDragObject)) {
                dropTarget.onDrop(mDragObject);
                accepted = true;
            }
        }
        mDragObject.dragSource.onDropCompleted((View) dropTarget, mDragObject, accepted);
        return (dropTarget != null);
//        if (dropTarget != null) {
//        	if(dropTarget instanceof Workspace){
//        		QsLog.LogW("DragController::drop(Workspace)==x:"+x+"==y:"+y+"==");
//        	}else if(dropTarget instanceof CellLayoutWithDrag){
//        		QsLog.LogW("DragController::drop(CellLayoutWithDrag)==x:"+x+"==y:"+y+"==");
//        	}else{
//        		QsLog.LogW("DragController::drop(other)==x:"+x+"==y:"+y+"==");
//        	}
//        	
//            dropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
//                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
//            if (dropTarget.acceptDrop(mDragSource, coordinates[0], coordinates[1],
//                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo)) {
//                dropTarget.onDrop(mDragSource, coordinates[0], coordinates[1],
//                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
//                mDragSource.onDropCompleted((View) dropTarget, true);
//                return true;
//            } else {
//                mDragSource.onDropCompleted((View) dropTarget, false);
//                return true;
//            }
//        }
//        return false;
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        final Rect r = mRectTemp;

        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i=count-1; i>=0; i--) {
            final DropTarget target = dropTargets.get(i);
            if (!target.isDropEnabled())
                continue;
            
            
            target.getHitRect(r);
            target.getLocationOnScreen(dropCoordinates);
            r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1] - target.getTop());
            
            mDragObject.x = x;
            mDragObject.y = y;
            
            if (r.contains(x, y)) {
                dropCoordinates[0] = x - dropCoordinates[0];
                dropCoordinates[1] = y - dropCoordinates[1];
                return target;
            }
        }
        return null;
    }

    /**
     * Get the screen size so we can clamp events to the screen size so even if
     * you drag off the edge of the screen, we find something.
     */
    private void recordScreenSize() {
        ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    /**
     * Clamp val to be &gt;= min and &lt; max.
     */
    private static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        } else if (val >= max) {
            return max - 1;
        } else {
            return val;
        }
    }

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listner which will be notified when a drag starts or ends.
     */
//    public void setDragListener(DragListener l) {
//        mListener = l;
//    }
    
    public void addDragListener(DragListener l) {
    	mListeners.add(l);
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        //mListener = null;
    	mListeners.remove(l);
    }
    
    public void clearDragListener(){
    	mListeners.clear();
    }
    
    public DragView getDragView() {
    	if(mDragObject != null)
    		return mDragObject.dragView;
    	
    	return null;
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }
    
    public void removeAllDropTargets() {
    	mDropTargets.clear();
    }
    
    public void clearDropTargets() {
    	mDropTargets.clear();
    }

    /**
     * Set which view scrolls for touch events near the edge of the screen.
     */
    public void setScrollView(View v) {
        mScrollView = v;
    }

    /**
     * Specifies the delete region.  We won't scroll on touch events over the delete region.
     *
     * @param region The rectangle in screen coordinates of the delete region.
     */
    public void setDeleteRegion(RectF region) {
        mDeleteRegion = region;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
                } else {
                    mDragScroller.scrollRight();
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }
    
    public boolean isDraging(){
    	return mDragging;
    }
    public int getDragActionMode() {
        return mDragActionMode;
    }
}
