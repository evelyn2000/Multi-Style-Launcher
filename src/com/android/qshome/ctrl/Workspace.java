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
import java.util.HashSet;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.TextView;
//import android.graphics.PorterDuff;
//import android.graphics.Region.Op;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherApplication;
import com.android.qshome.LauncherModel;
import com.android.qshome.R;
import com.android.qshome.ctrl.CellLayout.CellInfo;
import com.android.qshome.ctrl.CellLayout.LayoutParams;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.model.LiveFolderInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;
import com.android.qshome.util.LauncherSettings.Favorites;
import com.android.qshome.util.ThemeStyle;

/**
 * The workspace is a wide area with a wallpaper and a finite number of screens. Each
 * screen contains a number of icons, folders or widgets the user can interact with.
 * A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends ViewGroup implements DropTarget, DragSource, DragScroller, QsScreenIndicatorLister {
    @SuppressWarnings({"UnusedDeclaration"})
    private static final String TAG = "Launcher.Workspace";
    private static final boolean ENABLE_GOOGLE_SMOOTH = false;
    public static final int INVALID_SCREEN = -1;
    
    /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */
    public static final int SNAP_VELOCITY = 256;

    private final WallpaperManager mWallpaperManager;
    
    private int mDefaultScreen;
    private boolean mIsApplicationMode;
    private boolean mIsEnableWallpaper;
    private int mShortcutLayoutResource;
    private int mDefaultScreenCount;
    private int mQsContainerType;

    private boolean mFirstLayout = true;

    private int mCurrentScreen;
    private int mNextScreen = INVALID_SCREEN;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    /**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;
    
    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = null;

    private float mLastMotionX;
    private float mLastMotionY;
    
    public final static int TOUCH_STATE_REST = 0;
    public final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private OnLongClickListener mLongClickListener;

    private Launcher mLauncher;
    private IconCache mIconCache;
    private DragController mDragController;
    
    /**
     * Cache of vacant cells, used during drag events and invalidated as needed.
     */
    private CellLayout.CellInfo mVacantCache = null;
    
    private int[] mTempCell = new int[2];
    private int[] mTempEstimate = new int[2];

    private boolean mAllowLongPress = true;

    private int mTouchSlop;
    private int mMaximumVelocity;
    private int mOverscrollDistance;
    
    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;
    
    private Drawable mPreviousIndicator;
    private Drawable mNextIndicator;
    
    private static final float NANOTIME_DIV = 1000000000.0f;
    private static final float SMOOTHING_SPEED = 0.75f;
    private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
    private float mSmoothingTime;
    private float mTouchX;

    private WorkspaceOvershootInterpolator mScrollInterpolator;

    public static final float BASELINE_FLING_VELOCITY = 2500.f;
    public static final float FLING_VELOCITY_INFLUENCE = 0.06f;
    
    // for ics
//    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
//    private Bitmap mDragOutline = null;
//    private final Rect mTempRect = new Rect();
//    private final int[] mTempXY = new int[2];
//    private int mDragViewMultiplyColor;
//    private float mOverscrollFade = 0;
    
    private static class WorkspaceOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;
        private float mTension;

        public WorkspaceOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }
        
        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
        }

        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }
    
    // jz
    private QsScreenIndicatorCallback mQsWorkspaceCallback;
    
    private CellLayoutIcs mDragTargetLayout = null;
    
    
    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     * @param defStyle Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mWallpaperManager = WallpaperManager.getInstance(context);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Workspace, defStyle, 0);
        mDefaultScreen = a.getInt(R.styleable.Workspace_defaultScreen, 2);
        mIsApplicationMode = a.getBoolean(R.styleable.Workspace_isApplicationMode, false);
        mIsEnableWallpaper = a.getBoolean(R.styleable.Workspace_isEnableWallpaper, true);
        mShortcutLayoutResource = a.getResourceId(R.styleable.Workspace_shortcut_layout, R.layout.application);
        mQsContainerType = a.getInt(R.styleable.Workspace_container, LauncherSettings.Favorites.CONTAINER_DESKTOP);
        a.recycle();

        setHapticFeedbackEnabled(false);
        initWorkspace();
        
        mDefaultScreenCount = super.getChildCount();
    }

    /**
     * Initializes various states for this workspace.
     */
    private void initWorkspace() {
        Context context = getContext();
        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(context, mScrollInterpolator);
        mCurrentScreen = mDefaultScreen;
        Launcher.setScreen(mCurrentScreen);
        LauncherApplication app = (LauncherApplication)context.getApplicationContext();
        mIconCache = app.getIconCache();

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop()*2/3;
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
        //QsLog.LogD("Workspace::===mOverscrollDistance:"+mOverscrollDistance);
    }
    
    public boolean getIsApplicationMode(){
    	return mIsApplicationMode;
    }
    
    public int getDefaultScreenCount(){
    	return mDefaultScreenCount;
    }
    
    public int getShortcutLayoutResource(){
    	return mShortcutLayoutResource;
    }
    
    public View createShortcut(ShortcutInfo info){
        return createShortcut(info, getCurrentScreen());
    }
    
    public View createShortcut(ShortcutInfo info, ViewGroup parent) {    		
//    	if(mQsContainerType != LauncherSettings.Favorites.CONTAINER_DESKTOP)
//    		info.container = mQsContainerType;
    	
        return mLauncher.createShortcut(mShortcutLayoutResource, parent, info);
    }
    
    public View createShortcut(ShortcutInfo info, int screen) {    	
        return mLauncher.createShortcut(mShortcutLayoutResource, (ViewGroup)getChildAt(screen), info);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index, params);
        
        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onPageCountChanged(getScreenCount());
    }

    @Override
    public void addView(View child) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child);
        
        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onPageCountChanged(getScreenCount());
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index);
        
        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onPageCountChanged(getScreenCount());
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, width, height);
        
        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onPageCountChanged(getScreenCount());
    }

    @Override
    public void addView(View child, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, params);
        
        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onPageCountChanged(getScreenCount());
    }

    /**
     * @return The open folder on the current screen, or null if there is none
     */
    public Folder getOpenFolder() {
        CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
        if(currentScreen != null){
	        int count = currentScreen.getChildCount();
	        for (int i = 0; i < count; i++) {
	            View child = currentScreen.getChildAt(i);
	            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
	            if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
	                return (Folder) child;
	            }
	        }
        }
        return null;
    }

    public ArrayList<Folder> getOpenFolders() {
        final int screens = getChildCount();
        ArrayList<Folder> folders = new ArrayList<Folder>(screens);

        for (int screen = 0; screen < screens; screen++) {
            CellLayout currentScreen = (CellLayout) getChildAt(screen);
            if(currentScreen != null){
	            int count = currentScreen.getChildCount();
	            for (int i = 0; i < count; i++) {
	                View child = currentScreen.getChildAt(i);
	                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
	                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
	                    folders.add((Folder) child);
	                    break;
	                }
	            }
            }
        }

        return folders;
    }

    public boolean isDefaultScreenShowing() {
        return mCurrentScreen == mDefaultScreen;
    }

    /**
     * Returns the index of the currently displayed screen.
     *
     * @return The index of the currently displayed screen.
     */
    public int getCurrentScreen() {
        return mCurrentScreen;
    }
    
    public int getScreenCount(){
    	return super.getChildCount();
    }
    
    public void setQsScreenIndicatorCallback(QsScreenIndicatorCallback callback){
    	mQsWorkspaceCallback = callback;
    }

    /**
     * Sets the current screen.
     *
     * @param currentScreen
     */
    public void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished()) mScroller.abortAnimation();
        clearVacantCache();
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        if(mPreviousIndicator != null){
	        mPreviousIndicator.setLevel(mCurrentScreen);
	        mNextIndicator.setLevel(mCurrentScreen);
        }
        //QsLog.LogD("Workspace::setCurrentScreen()===Width:"+getWidth()+"==height:"+super.getHeight()
        //		+"==mCurrentScreen:"+mCurrentScreen);
        
        if(getWidth() > 0){
        	scrollTo(mCurrentScreen * getWidth(), 0);
        	updateWallpaperOffset();
        }

        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onChangeToScreen(mCurrentScreen);
        invalidate();
    }

    /**
     * Adds the specified child in the current screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    public void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the current screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    public void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    public void addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, screen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    public void addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
        if (screen < 0 || screen >= getChildCount()) {
            Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
                + " (was " + screen + "); skipping child");
            return;
        }

        clearVacantCache();

        final CellLayout group = (CellLayout) getChildAt(screen);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }
        group.addView(child, insert ? 0 : -1, lp);
        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget)child);
        }
    }

    public CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
        CellLayout group = (CellLayout) getChildAt(mCurrentScreen);
        if (group != null) {
            return group.findAllVacantCells(occupied, null);
        }
        return null;
    }

    private void clearVacantCache() {
        if (mVacantCache != null) {
            mVacantCache.clearVacantCells();
            mVacantCache = null;
        }
    }

    /**
     * Registers the specified listener on each screen contained in this workspace.
     *
     * @param l The listener used to respond to long clicks.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    private void updateWallpaperOffset() {
    	if(mIsEnableWallpaper)
    		updateWallpaperOffset(getChildAt(getChildCount() - 1).getRight() - (mRight - mLeft));
    }

    private void updateWallpaperOffset(int scrollRange) {
        IBinder token = getWindowToken();
        if (token != null) {
            mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 0 );
            mWallpaperManager.setWallpaperOffsets(getWindowToken(),
                    Math.max(0.f, Math.min(mScrollX/(float)scrollRange, 1.f)), 0);
        }
    }
    
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if(ENABLE_GOOGLE_SMOOTH) {
            mTouchX = x;
            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
        }
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    	super.onScrollChanged(l, t, oldl, oldt);
    	//QsLog.LogD("Workspace::onScrollChanged()==l:"+l+"=t:"+t+"=oldl:"+oldl+"==oldt:"+oldt);
    	if(mQsWorkspaceCallback != null){
        	mQsWorkspaceCallback.onScrollChangedCallback(l, t, oldl, oldt);
        }
    }
    @Override
    public void scrollBy(int x, int y) {
        //super.onScrollChanged(l, t, oldl, oldt);
    	super.scrollBy(x, y);
    }
    
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
        	if(ENABLE_GOOGLE_SMOOTH) {
        		mTouchX = mScrollX = mScroller.getCurrX();
        		mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
            } else {
				mScrollX = mScroller.getCurrX();
            }
            mScrollY = mScroller.getCurrY();
            updateWallpaperOffset();
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            if(mPreviousIndicator != null){
	            mPreviousIndicator.setLevel(mCurrentScreen);
	            mNextIndicator.setLevel(mCurrentScreen);
            }
            Launcher.setScreen(mCurrentScreen);
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache();
        } else if (ENABLE_GOOGLE_SMOOTH && mTouchState == TOUCH_STATE_SCROLLING) {
            final float now = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((now - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX - mScrollX;
            mScrollX += dx * e;
            mSmoothingTime = now;

            // Keep generating points as long as we're more than 1px away from the target
            if (dx > 1.f || dx < -1.f) {
                updateWallpaperOffset();
                postInvalidate();
            }
        }
        
    }
    

    @Override
    protected void dispatchDraw(Canvas canvas) {
        boolean restore = false;
        int restoreCount = 0;

        // ViewGroup.dispatchDraw() supports many features we don't need:
        // clip to padding, layout animation, animation listener, disappearing
        // children, etc. The following implementation attempts to fast-track
        // the drawing dispatch by drawing only what we know needs to be drawn.

        if(mLauncher.getCurrentWidgetObjectStyle() == ThemeStyle.DroidIcs){
        	final int width = getWidth();
            final int height = getHeight();
            final int pageHeight = getChildAt(0).getHeight();

            // Set the height of the outline to be the height of the page
            final int offset = (height - pageHeight - getPaddingTop() - getPaddingBottom()) / 2;
            final int paddingTop = getPaddingTop() + offset;
            final int paddingBottom = getPaddingBottom() + offset;

            final CellLayoutIcs leftPage = (CellLayoutIcs) getChildAt(mCurrentScreen - 1);
            final CellLayoutIcs rightPage = (CellLayoutIcs) getChildAt(mCurrentScreen + 1);

            if (leftPage != null && leftPage.getIsDragOverlapping()) {
                final Drawable d = getResources().getDrawable(R.drawable.zzzz_ics_page_hover_left_holo);
                d.setBounds(getScrollX(), paddingTop, mScrollX + d.getIntrinsicWidth(),
                        height - paddingBottom);
                d.draw(canvas);
            } else if (rightPage != null && rightPage.getIsDragOverlapping()) {
                final Drawable d = getResources().getDrawable(R.drawable.zzzz_ics_page_hover_right_holo);
                d.setBounds(mScrollX + width - d.getIntrinsicWidth(), paddingTop, mScrollX + width,
                        height - paddingBottom);
                d.draw(canvas);
            }
            
        	//return;
        }
        
        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
        // If we are not scrolling or flinging, draw only the current screen
        if (fastDraw) {
        	View child = getChildAt(mCurrentScreen);
        	if(child != null)
        		drawChild(canvas, child, getDrawingTime());
        } else {
            final long drawingTime = getDrawingTime();
            final float scrollPos = (float) mScrollX / getWidth();
            final int leftScreen = (int) scrollPos;
            final int rightScreen = leftScreen + 1;
            if (leftScreen >= 0) {
            	
            	View child = getChildAt(leftScreen);
            	if(child != null)
            		drawChild(canvas, child, drawingTime);
            }
            if (scrollPos != leftScreen && rightScreen < getChildCount()) {
            	
            	View child = getChildAt(rightScreen);
            	if(child != null)
            		drawChild(canvas, child, drawingTime);
            }
        }

        if (restore) {
            canvas.restoreToCount(restoreCount);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
        if(mDragController != null)
        	mDragController.setWindowToken(getWindowToken());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        
        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        //QsLog.LogE("Workspace::onMeasure()===width:"+width+"==height:"+height+"==width1:"+super.getWidth()+"===height1:"+super.getHeight()+"=mFirstLayout:"+mFirstLayout);
        if (mFirstLayout && super.getHeight() > 0) {
            setHorizontalScrollBarEnabled(false);
            scrollTo(mCurrentScreen * width, 0);
            setHorizontalScrollBarEnabled(true);
            if(mIsEnableWallpaper)
            	updateWallpaperOffset(width * (getChildCount() - 1));
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            if (!mLauncher.isWorkspaceLocked()) {
                snapToScreen(screen);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (getIsApplicationMode() || !mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                return openFolder.requestFocus(direction, previouslyFocusedRect);
            } else {
                int focusableScreen;
                if (mNextScreen != INVALID_SCREEN) {
                    focusableScreen = mNextScreen;
                } else {
                    focusableScreen = mCurrentScreen;
                }
                
                View child = getChildAt(focusableScreen);
                if(child != null)
                	child.requestFocus(direction, previouslyFocusedRect);
            }
        }
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentScreen() > 0) {
                snapToScreen(getCurrentScreen() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentScreen() < getChildCount() - 1) {
                snapToScreen(getCurrentScreen() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (getIsApplicationMode() || !mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder == null) {
                getChildAt(mCurrentScreen).addFocusables(views, direction);
                if (direction == View.FOCUS_LEFT) {
                    if (mCurrentScreen > 0) {
                        getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
                    }
                } else if (direction == View.FOCUS_RIGHT){
                    if (mCurrentScreen < getChildCount() - 1) {
                        getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
                    }
                }
            } else {
                openFolder.addFocusables(views, direction);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mLauncher.isWorkspaceLocked() || (!getIsApplicationMode() && mLauncher.isAllAppsVisible())) {
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final boolean workspaceLocked = mLauncher.isWorkspaceLocked();
        final boolean allAppsVisible = (!getIsApplicationMode() && mLauncher.isAllAppsVisible());
        if (workspaceLocked || allAppsVisible) {
            return false; // We don't want the events.  Let them fall through to the all apps view.
        }
        //QsLog.LogD("Workspace::onInterceptTouchEvent()==mTouchState:"+mTouchState+"=action:"+(ev.getAction() & MotionEvent.ACTION_MASK));
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionX is set to the y value
                 * of the down event.
                 */
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if(pointerIndex < 0)
                	break;
                
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;
                
                if (xMoved || yMoved) {
                    
                    if (xMoved) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                        if(ENABLE_GOOGLE_SMOOTH) {
                            mLastMotionX = x;
                            mTouchX = mScrollX;
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;	
                        }                    
                        enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                    }
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;
                        // Try canceling the long press. It could also have been scheduled
                        // by a distant descendant, so use the mAllowLongPress flag to block
                        // everything
                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }
                //QsLog.LogD("Workspace::onInterceptTouchEvent(ACTION_MOVE)==mTouchState:"+mTouchState+"=xMoved:"+xMoved+"=yMoved:"+yMoved+"=xDiff:"+xDiff+"=yDiff:"+yDiff+"=touchSlop:"+touchSlop);
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);
                mAllowLongPress = true;

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                
                if (mTouchState != TOUCH_STATE_SCROLLING) {
                    final CellLayout currentScreen = (CellLayout)getChildAt(mCurrentScreen);
                    if (!currentScreen.lastDownOnOccupiedCell()) {
                    	//QsLog.LogD("Workspace::onInterceptTouchEvent(2)==mTouchState:"+mTouchState);
                        getLocationOnScreen(mTempCell);
                        // Send a tap to the wallpaper if the last down was on empty space
                        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                        if(pointerIndex < 0)
                        	break;
                        
                        mWallpaperManager.sendWallpaperCommand(getWindowToken(), 
                                "android.wallpaper.tap",
                                mTempCell[0] + (int) ev.getX(pointerIndex),
                                mTempCell[1] + (int) ev.getY(pointerIndex), 0, null);
                    }
                }
                
                // Release the drag
                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                mAllowLongPress = false;
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
                
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        //QsLog.LogD("Workspace::onInterceptTouchEvent(end)==mTouchState:"+mTouchState);
        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mLastMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    /**
     * If one of our descendant views decides that it could be focused now, only
     * pass that along if it's on the current screen.
     *
     * This happens when live folders requery, and if they're off screen, they
     * end up calling requestFocus, which pulls it on screen.
     */
    @Override
    public void focusableViewAvailable(View focused) {
        View current = getChildAt(mCurrentScreen);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View)v.getParent();
            } else {
                return;
            }
        }
    }

    public void enableChildrenCache(int fromScreen, int toScreen) {
        if (fromScreen > toScreen) {
            final int temp = fromScreen;
            fromScreen = toScreen;
            toScreen = temp;
        }
        
        final int count = getChildCount();

        fromScreen = Math.max(fromScreen, 0);
        toScreen = Math.min(toScreen, count - 1);

        for (int i = fromScreen; i <= toScreen; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

    public void clearChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	//QsLog.LogD("Workspace::onTouchEvent()==mTouchState:"+mTouchState+"==action:"+(ev.getAction() & MotionEvent.ACTION_MASK));
        if (mLauncher.isWorkspaceLocked()) {
            return false; // We don't want the events.  Let them fall through to the all apps view.
        }
        if (!getIsApplicationMode() && mLauncher.isAllAppsVisible()) {
            // Cancel any scrolling that is in progress.
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            snapToScreen(mCurrentScreen);
            return false; // We don't want the events.  Let them fall through to the all apps view.
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionX = ev.getX();
            mActivePointerId = ev.getPointerId(0);
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                // Scroll to follow the motion event
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if(pointerIndex < 0)
                	break;
                
                final float x = ev.getX(pointerIndex);
                final float deltaX = mLastMotionX - x;
                mLastMotionX = x;

                if (deltaX < 0) {
                    if (ENABLE_GOOGLE_SMOOTH) {
                        if (mTouchX > -mOverscrollDistance) {
                            mTouchX += Math.max(-mOverscrollDistance, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                    } else {
						if (mScrollX > -mOverscrollDistance) {
							scrollBy((int) Math.max(-mOverscrollDistance, deltaX), 0);
							updateWallpaperOffset();
						}
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight()
                            - mScrollX - getWidth() + mOverscrollDistance;
                    if (availableToScroll >  0) {
                        if (ENABLE_GOOGLE_SMOOTH) {
                            mTouchX += Math.min(mOverscrollDistance, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        } else {
                            scrollBy((int) Math.min(mOverscrollDistance, deltaX), 0);
                            updateWallpaperOffset();
                        }
                    }
                } else {
                    if (ENABLE_GOOGLE_SMOOTH) {
                        awakenScrollBars();
                    }
                }
                
//                if (deltaX < 0) {
//                    if(ENABLE_GOOGLE_SMOOTH) {
//	                    if (mTouchX > 0) {
//	                        mTouchX += Math.max(-mTouchX, deltaX);
//	                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
//	                        invalidate();
//	                    }
//                    } else {
//                        if (mScrollX > 0) {
//                            scrollBy((int)Math.max(-mScrollX, deltaX), 0);
//                            updateWallpaperOffset();
//                        }
//                    }
//                } else if (deltaX > 0) {
//                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight() -
//                            mScrollX - getWidth();
//                    if (availableToScroll > 0) {                        
//                        if(ENABLE_GOOGLE_SMOOTH) {
//                            mTouchX += Math.min(availableToScroll, deltaX);
//                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
//                            invalidate();
//                        } else {
//                            scrollBy((int)Math.min(availableToScroll, deltaX), 0);
//                            updateWallpaperOffset();
//                        }
//
//
//                    }
//                } else {
//                    awakenScrollBars();
//                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
                
                final int screenWidth = getWidth();
                final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
                final float scrolledPos = (float) mScrollX / screenWidth;
                               
                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                    // Fling hard enough to move left.
                    // Don't fling across more than one screen at a time.
                    final int bound = scrolledPos < whichScreen ?
                            mCurrentScreen - 1 : mCurrentScreen;
                    snapToScreen(Math.min(whichScreen, bound), velocityX, true);
                } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                    // Fling hard enough to move right
                    // Don't fling across more than one screen at a time.
                    final int bound = scrolledPos > whichScreen ?
                            mCurrentScreen + 1 : mCurrentScreen;
                    snapToScreen(Math.max(whichScreen, bound), velocityX, true);
                } else {
                    snapToScreen(whichScreen, 0, true);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            break;
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
        }

        return true;
    }
    
    
    public void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0, false);
    }

    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
        //if (!mScroller.isFinished()) return;
    	
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        //QsLog.LogD("Workspace::snapToScreen(0)===mCurrentScreen:"+mCurrentScreen+"=whichScreen:"+whichScreen);
        clearVacantCache();
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;
        if(mPreviousIndicator != null){
	        mPreviousIndicator.setLevel(mNextScreen);
	        mNextIndicator.setLevel(mNextScreen);
        }
        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen &&
                focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = (screenDelta + 1) * 100;
        //QsLog.LogD("Workspace::snapToScreen(1)==newX:"+newX+"=mCurrentScreen:"+mCurrentScreen+"=delta:"+delta+"=mScrollX:"+mScrollX);
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        if (settle) {
            mScrollInterpolator.setDistance(screenDelta);
        } else {
            mScrollInterpolator.disableSettle();
        }
        
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
                    * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 32;
        }

        awakenScrollBars(duration);
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        
        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onChangeToScreen(whichScreen);
        invalidate();
    }
    
//    /**
//     * Draw the View v into the given Canvas.
//     *
//     * @param v the view to draw
//     * @param destCanvas the canvas to draw on
//     * @param padding the horizontal and vertical padding to use when drawing
//     */
//    private void drawDragView(View v, Canvas destCanvas, int padding, boolean pruneToDrawable) {
//        final Rect clipRect = mTempRect;
//        v.getDrawingRect(clipRect);
//
//        boolean textVisible = false;
//
//        destCanvas.save();
//        if (v instanceof TextView && pruneToDrawable) {
//            Drawable d = ((TextView) v).getCompoundDrawables()[1];
//            clipRect.set(0, 0, d.getIntrinsicWidth() + padding, d.getIntrinsicHeight() + padding);
//            destCanvas.translate(padding / 2, padding / 2);
//            d.draw(destCanvas);
//        } else {
//            /*if (v instanceof FolderIcon) {
//                // For FolderIcons the text can bleed into the icon area, and so we need to
//                // hide the text completely (which can't be achieved by clipping).
//                if (((FolderIcon) v).getTextVisible()) {
//                    ((FolderIcon) v).setTextVisible(false);
//                    textVisible = true;
//                }
//            } else */if (v instanceof BubbleTextViewIcs) {
//                final BubbleTextViewIcs tv = (BubbleTextViewIcs) v;
//                clipRect.bottom = tv.getExtendedPaddingTop() - (int) tv.getPaddingV() +
//                        tv.getLayout().getLineTop(0);
//            } else if (v instanceof TextView) {
//                final TextView tv = (TextView) v;
//                clipRect.bottom = tv.getExtendedPaddingTop() - tv.getCompoundDrawablePadding() +
//                        tv.getLayout().getLineTop(0);
//            }
//            destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
//            destCanvas.clipRect(clipRect, Op.REPLACE);
//            v.draw(destCanvas);
//
//            // Restore text visibility of FolderIcon if necessary
////            if (textVisible) {
////                ((FolderIcon) v).setTextVisible(true);
////            }
//        }
//        destCanvas.restore();
//    }
//    
//    /**
//     * Returns a new bitmap to show when the given View is being dragged around.
//     * Responsibility for the bitmap is transferred to the caller.
//     */
//    public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
//        final int outlineColor = getResources().getColor(R.color.holo_blue_light);
//        Bitmap b;
//
//        if (v instanceof TextView) {
//            Drawable d = ((TextView) v).getCompoundDrawables()[1];
//            b = Bitmap.createBitmap(d.getIntrinsicWidth() + padding,
//                    d.getIntrinsicHeight() + padding, Bitmap.Config.ARGB_8888);
//        } else {
//            b = Bitmap.createBitmap(
//                    v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
//        }
//
//        canvas.setBitmap(b);
//        drawDragView(v, canvas, padding, true);
//        mOutlineHelper.applyOuterBlur(b, canvas, outlineColor);
//        canvas.drawColor(mDragViewMultiplyColor, PorterDuff.Mode.MULTIPLY);
//        //canvas.setBitmap(null);
//
//        return b;
//    }
//    
//    /**
//     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
//     * Responsibility for the bitmap is transferred to the caller.
//     */
//    private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
//        final int outlineColor = getResources().getColor(R.color.holo_blue_light);
//        final Bitmap b = Bitmap.createBitmap(
//                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
//
//        canvas.setBitmap(b);
//        drawDragView(v, canvas, padding, true);
//        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
//        //canvas.setBitmap(null);
//        return b;
//    }
//    
//    public void beginDragShared(View child, DragSource source) {
//        Resources r = getResources();
//
//        // We need to add extra padding to the bitmap to make room for the glow effect
//        final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
//
//        // The drag bitmap follows the touch point around on the screen
//        final Bitmap b = createDragBitmap(child, new Canvas(), bitmapPadding);
//
//        final int bmpWidth = b.getWidth();
//
//        //mLauncher.getDragLayer().getLocationInDragLayer(child, mTempXY);
//        child.getLocationInWindow(mTempXY);
//        final int dragLayerX = (int) mTempXY[0] + (child.getWidth() - bmpWidth) / 2;
//        int dragLayerY = mTempXY[1] - bitmapPadding / 2;
//
//        Point dragVisualizeOffset = null;
//        Rect dragRect = null;
//        if (child instanceof BubbleTextViewIcs/* || child instanceof PagedViewIcon*/) {
//            int iconSize = r.getDimensionPixelSize(android.R.dimen.app_icon_size);
//            int iconPaddingTop = r.getDimensionPixelSize(R.dimen.app_icon_padding_top);
//            int top = child.getPaddingTop();
//            int left = (bmpWidth - iconSize) / 2;
//            int right = left + iconSize;
//            int bottom = top + iconSize;
//            dragLayerY += top;
//            // Note: The drag region is used to calculate drag layer offsets, but the
//            // dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
//            dragVisualizeOffset = new Point(-bitmapPadding / 2, iconPaddingTop - bitmapPadding / 2);
//            dragRect = new Rect(left, top, right, bottom);
//        } /*else if (child instanceof FolderIcon) {
//            int previewSize = r.getDimensionPixelSize(R.dimen.folder_preview_size);
//            dragRect = new Rect(0, 0, child.getWidth(), previewSize);
//        }*/
//
//        // Clear the pressed state if necessary
//        if (child instanceof BubbleTextViewIcs) {
//        	BubbleTextViewIcs icon = (BubbleTextViewIcs) child;
//            icon.clearPressedOrFocusedBackground();
//        }
//
//        mDragController.startDrag(b, dragLayerX, dragLayerY, dragVisualizeOffset, dragRect, source, child.getTag(),
//                DragController.DRAG_ACTION_MOVE);
//        b.recycle();
//    }
//    
//    public void startDragIcs(CellLayout.CellInfo cellInfo) {
//        View child = cellInfo.cell;
//
//        // Make sure the drag was started by a long press as opposed to a long click.
//        if (!child.isInTouchMode()) {
//            return;
//        }
//
//        mDragInfo = cellInfo;
//        mDragInfo.screen = mCurrentScreen;
//        child.setVisibility(GONE);
//
//        child.clearFocus();
//        child.setPressed(false);
//
//        final Canvas canvas = new Canvas();
//
//        // We need to add extra padding to the bitmap to make room for the glow effect
//        final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
//
//        // The outline is used to visualize where the item will land if dropped
//        mDragOutline = createDragOutline(child, canvas, bitmapPadding);
//        beginDragShared(child, this);
//    }

    public void startDrag(CellLayout.CellInfo cellInfo) {
        View child = cellInfo.cell;
        //QsLog.LogD("Workspace::startDrag(0)=======");
        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }
        
        mDragInfo = cellInfo;
        mDragInfo.screen = mCurrentScreen;
        
        CellLayout current = ((CellLayout) getChildAt(mCurrentScreen));
        //QsLog.LogD("Workspace::startDrag(1)=======");
        current.onDragChild(child);
        mDragController.startDrag(child, this, child.getTag(), DragController.DRAG_ACTION_MOVE);
        invalidate();
    }
    
    public void startDrag(CellLayout.CellInfo cellInfo, int dragAction){
    	View child = cellInfo.cell;
    	//QsLog.LogD("Workspace::startDrag(00)=======");
        // Make sure the drag was started by a long press as opposed to a long click.
        if (child == null || !child.isInTouchMode()) {
            return;
        }
        
        mDragInfo = cellInfo;
        mDragInfo.screen = mCurrentScreen;
        
        CellLayout current = ((CellLayout) getChildAt(mCurrentScreen));

        // if copy ,then show desktop
    	if(dragAction != DragController.DRAG_ACTION_COPY ){
    		current.onDragChild(child);
    	}//else if(getIsApplicationMode())
    	//	mLauncher.closeAllApps(true);
    	//QsLog.LogD("Workspace::startDrag(01)=======");
    	mDragController.startDrag(child, this, child.getTag(), dragAction);
    	invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentScreen;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != -1) {
            mCurrentScreen = savedState.currentScreen;
            Launcher.setScreen(mCurrentScreen);
        }
    }

    public void addApplicationShortcut(ShortcutInfo info, CellLayout.CellInfo cellInfo) {
        addApplicationShortcut(info, cellInfo, false);
    }

    public void addApplicationShortcut(ShortcutInfo info, CellLayout.CellInfo cellInfo,
            boolean insertAtFirst) {
        final CellLayout layout = (CellLayout) getChildAt(cellInfo.screen);
        final int[] result = new int[2];

        layout.cellToPoint(cellInfo.cellX, cellInfo.cellY, result);
        onDropExternal(null, result[0], result[1], info, layout, insertAtFirst);
    }

    public boolean onDrop(DragObject dragObject) {
        final CellLayout cellLayout = getCurrentDropLayout();

        if (dragObject.dragSource!= this) {
            return onDropExternal(dragObject, cellLayout);
        } else {
            // Move internally
            if (mDragInfo != null) {
                final View cell = mDragInfo.cell;
                int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;        
                
                if (index != mDragInfo.screen) {
                    final CellLayout originalCellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                    originalCellLayout.removeView(cell);
                    cellLayout.addView(cell);
                }
                mTargetCell = estimateDropCell(dragObject.x - dragObject.xOffset, dragObject.y - dragObject.yOffset,
                        mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout, mTargetCell);
                
                cellLayout.onDropChild(cell, mTargetCell);
                
                final ItemInfo info = (ItemInfo) cell.getTag();
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                               
                LauncherModel.moveItemInDatabase(mLauncher, info,
                		mQsContainerType, index, lp.cellX, lp.cellY, mIsApplicationMode);
            }
        }
        
        return true;
    }

    public void onDragEnter(DragObject dragObject) {
        clearVacantCache();
        //QsLog.LogD("Workspace::onDragEnter()=======");
		final Folder openFolder = getOpenFolder();
		if( openFolder != null ) {
			mLauncher.closeFolder( openFolder );
		}
		
		if (mDragTargetLayout != null) {
            mDragTargetLayout.setIsDragOverlapping(false);
            mDragTargetLayout.onDragExit();
        }
		
		CellLayout layout = getCurrentDropLayout();
		if(layout instanceof CellLayoutIcs){
	        mDragTargetLayout = (CellLayoutIcs)layout;
	        mDragTargetLayout.setIsDragOverlapping(true);
	        mDragTargetLayout.onDragEnter();
		}
    }

    public void onDragOver(DragObject dragObject) {
    	//QsLog.LogD("Workspace::onDragOver()=======");
    	CellLayout layout = null;
    	if (layout == null) {
            layout = getCurrentDropLayout();
        }
    	
        if (layout != mDragTargetLayout) {
            if (mDragTargetLayout != null) {
                mDragTargetLayout.setIsDragOverlapping(false);
                mDragTargetLayout.onDragExit();
            }

    		if(layout instanceof CellLayoutIcs){
	            mDragTargetLayout = (CellLayoutIcs)layout;
	            mDragTargetLayout.setIsDragOverlapping(true);
	            mDragTargetLayout.onDragEnter();
    		}
        }
    }

    public void onDragExit(DragObject dragObject) {
        clearVacantCache();
        //QsLog.LogD("Workspace::onDragExit()=======");
        
        if (mDragTargetLayout != null) {
            mDragTargetLayout.setIsDragOverlapping(false);
            mDragTargetLayout.onDragExit();
        }
//        mLastDragOverView = null;
//        mSpringLoadedDragController.cancel();
    }

    private boolean onDropExternal(DragObject dragObject, CellLayout cellLayout) {
        return onDropExternal(dragObject.dragSource, dragObject.x, dragObject.y, dragObject.dragInfo, cellLayout, false);
    }
    
    private boolean onDropExternal(DragSource source, int x, int y, Object dragInfo, CellLayout cellLayout,
            boolean insertAtFirst) {
        // Drag from somewhere else
        ItemInfo info = (ItemInfo) dragInfo;
        boolean bIsUpdateSmsIcon = false;
        View view;
        switch (info.itemType) {
        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
        	// jz if workspace is source and copy from app workspace to desktop workspace...
        	if(source != null && (source instanceof Workspace) && ((Workspace)source).getIsApplicationMode()){
        		//QsLog.LogD("Workspace::onDropExternal(2)===reset container===isapp:"+getIsApplicationMode());
        		info.container = NO_ID;
        	}
        	
            if (info.container == NO_ID && info instanceof ApplicationInfo) {
            	//QsLog.LogD("Workspace::onDropExternal(app)==componentName:"+((ApplicationInfo)info).componentName.toString());
            	//bIsUpdateSmsIcon = Launcher.checkIsSmsComponentName(((ApplicationInfo)info).componentName);
                // Came from all apps -- make a copy
                info = new ShortcutInfo((ApplicationInfo)info);
            }
            
            //view = mLauncher.createShortcut(R.layout.application, cellLayout, (ShortcutInfo)info);
            view = createShortcut((ShortcutInfo)info, cellLayout);
            break;
        case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
            view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
                    (ViewGroup) getChildAt(mCurrentScreen), ((UserFolderInfo) info));
            break;
        default:
            throw new IllegalStateException("Unknown item type: " + info.itemType);
        }
        if(view == null) {
            // do not add the view if view is null.
        	QsLog.LogE("Workspace::onDropExternal(1)===view is null===isapp:"+getIsApplicationMode());
            return false;
        }
        
        
        cellLayout.addView(view, insertAtFirst ? 0 : -1);
        view.setHapticFeedbackEnabled(false);
        view.setOnLongClickListener(mLongClickListener);
        if (view instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) view);
        }

        mTargetCell = estimateDropCell(x, y, 1, 1, view, cellLayout, mTargetCell);
        cellLayout.onDropChild(view, mTargetCell);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();

        LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
        		mQsContainerType, mCurrentScreen, lp.cellX, lp.cellY, mIsApplicationMode);
        
//        if(bIsUpdateSmsIcon){
//        	mLauncher.updateCurrentSmsIconByMessage();
//        }
        
        return true;
    }
    
    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    private CellLayout getCurrentDropLayout() {
        int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
        return (CellLayout) getChildAt(index);
    }

    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragObject dragObject) {
        final CellLayout layout = getCurrentDropLayout();
        final CellLayout.CellInfo cellInfo = mDragInfo;
        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;

        if (mVacantCache == null) {
            final View ignoreView = cellInfo == null ? null : cellInfo.cell;
            mVacantCache = layout.findAllVacantCells(null, ignoreView);
        }

        return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public Rect estimateDropLocation(DragObject dragObject, Rect recycle) {
        final CellLayout layout = getCurrentDropLayout();
        
        final CellLayout.CellInfo cellInfo = mDragInfo;
        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
        final View ignoreView = cellInfo == null ? null : cellInfo.cell;
        
        final Rect location = recycle != null ? recycle : new Rect();
        
        // Find drop cell and convert into rectangle
        int[] dropCell = estimateDropCell(dragObject.x - dragObject.xOffset, dragObject.y - dragObject.yOffset,
                spanX, spanY, ignoreView, layout, mTempCell);
        
        if (dropCell == null) {
            return null;
        }
        
        layout.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
        location.left = mTempEstimate[0];
        location.top = mTempEstimate[1];
        
        layout.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY, mTempEstimate);
        location.right = mTempEstimate[0];
        location.bottom = mTempEstimate[1];
        
        return location;
    }

    public boolean isDropEnabled() {
        return true;
    }
    
    @Override
    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }
    
    /**
     * Calculate the nearest cell where the given object would be dropped.
     */
    private int[] estimateDropCell(int pixelX, int pixelY,
            int spanX, int spanY, View ignoreView, CellLayout layout, int[] recycle) {
        // Create vacant cell cache if none exists
        if (mVacantCache == null) {
            mVacantCache = layout.findAllVacantCells(null, ignoreView);
        }

        // Find the best target drop location
        return layout.findNearestVacantArea(pixelX, pixelY,
                spanX, spanY, mVacantCache, recycle);
    }
    
    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setDragController(DragController dragController) {
    	if(mDragController == null)
    		dragController.setWindowToken(getWindowToken());
        mDragController = dragController;
    }

    public void onDropCompleted(View target, DragObject d, boolean success) {
        clearVacantCache();
        //QsLog.LogD("Workspace::onDropCompleted(0)====success:"+success+"==isself:"+(target == this));
        if (success){
            if (target != this && mDragInfo != null) {
            	// jz if workspace only set as drag source, can't remove 
            	if(mDragController.getDragActionMode() != DragController.DRAG_ACTION_COPY){
	                final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
	                cellLayout.removeView(mDragInfo.cell);
            	}
                if (mDragInfo.cell instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget)mDragInfo.cell);
                }
                //final Object tag = mDragInfo.cell.getTag();
            }
        } else {
            if (mDragInfo != null) {
                final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                cellLayout.onDropAborted(mDragInfo.cell);
            }
        }

        mDragInfo = null;
    }

    public void scrollLeft() {
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen > 0) snapToScreen(mCurrentScreen - 1);
        } else {
            if (mNextScreen > 0) snapToScreen(mNextScreen - 1);            
        }
    }

    public void scrollRight() {
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen < getChildCount() -1) snapToScreen(mCurrentScreen + 1);
        } else {
            if (mNextScreen < getChildCount() -1) snapToScreen(mNextScreen + 1);            
        }
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    public Folder getFolderForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                    Folder f = (Folder) child;
                    if (f.getInfo() == tag) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    public View getViewForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }
    
    /**
     * Set true to allow long-press events to be triggered, usually checked by
     * {@link Launcher} to accept or block dpad-initiated long-presses.
     */
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }
    
    public void removeViews() {
    	int count = getChildCount();
    	for (int i = 0; i < count; i++) {
            final CellLayout cellLayout = (CellLayout) getChildAt(i);
            cellLayout.removeAllViews();
        }
    }
    
    public void hideWorkspace() {
    	setVisibility(View.GONE);
    	int count = getChildCount();
    	for( int i = 0; i < count; i++) {
			final CellLayout cellLayout = (CellLayout) getChildAt(i);
			cellLayout.setVisibility(View.GONE);
			int childCount = cellLayout.getChildCount();

			for (int j = 0; j < childCount; j++) {
				View child = (View) cellLayout.getChildAt(j);
				child.setVisibility(View.GONE);
			}
		}
    }
    
    public void showWorkspace( ) {
    	setVisibility(View.VISIBLE);
    	int count = getChildCount();
    	for( int i = 0; i < count; i++) {
			final CellLayout cellLayout = (CellLayout) getChildAt(i);
			cellLayout.setVisibility(View.VISIBLE);
			int childCount = cellLayout.getChildCount();

			for (int j = 0; j < childCount; j++) {
				View child = (View) cellLayout.getChildAt(j);
				child.setVisibility(View.VISIBLE);
			}
		}
    	updateWallpaperOffset();
    }

    public void removeItems(final ArrayList<ApplicationInfo> apps) {
        final int count = getChildCount();
        final PackageManager manager = getContext().getPackageManager();
        final AppWidgetManager widgets = AppWidgetManager.getInstance(getContext());

        final HashSet<String> packageNames = new HashSet<String>();
        final int appCount = apps.size();
        for (int i = 0; i < appCount; i++) {
            packageNames.add(apps.get(i).componentName.getPackageName());
        }

        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    childrenToRemove.clear();
        
                    int childCount = layout.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        final View view = layout.getChildAt(j);
                        Object tag = view.getTag();
        
                        if (tag instanceof ShortcutInfo) {
                            final ShortcutInfo info = (ShortcutInfo) tag;
                            final Intent intent = info.intent;
                            final ComponentName name = intent.getComponent();
        
                            if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                for (String packageName: packageNames) {
                                    if (packageName.equals(name.getPackageName())) {
                                        // TODO: This should probably be done on a worker thread
                                        LauncherModel.deleteItemFromDatabase(mLauncher, info, mIsApplicationMode);
                                        childrenToRemove.add(view);
                                    }
                                }
                            }
                        } else if (tag instanceof UserFolderInfo) {
                            final UserFolderInfo info = (UserFolderInfo) tag;
                            final ArrayList<ShortcutInfo> contents = info.contents;
                            final ArrayList<ShortcutInfo> toRemove = new ArrayList<ShortcutInfo>(1);
                            final int contentsCount = contents.size();
                            boolean removedFromFolder = false;
        
                            for (int k = 0; k < contentsCount; k++) {
                                final ShortcutInfo appInfo = contents.get(k);
                                final Intent intent = appInfo.intent;
                                final ComponentName name = intent.getComponent();
        
                                if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                    for (String packageName: packageNames) {
                                        if (packageName.equals(name.getPackageName())) {
                                            toRemove.add(appInfo);
                                            // TODO: This should probably be done on a worker thread
                                            LauncherModel.deleteItemFromDatabase(
                                                    mLauncher, appInfo, mIsApplicationMode);
                                            removedFromFolder = true;
                                        }
                                    }
                                }
                            }
        
                            contents.removeAll(toRemove);
                            if (removedFromFolder) {
                                final Folder folder = getOpenFolder();
                                if (folder != null) folder.notifyDataSetChanged();
                            }
                        } else if(!mIsApplicationMode){
                        	
                        	if (tag instanceof LiveFolderInfo) {
	                            final LiveFolderInfo info = (LiveFolderInfo) tag;
	                            final Uri uri = info.uri;
	                            final ProviderInfo providerInfo = manager.resolveContentProvider(
	                                    uri.getAuthority(), 0);
	
	                            if (providerInfo != null) {
	                                for (String packageName: packageNames) {
	                                    if (packageName.equals(providerInfo.packageName)) {
	                                        // TODO: This should probably be done on a worker thread
	                                        LauncherModel.deleteItemFromDatabase(mLauncher, info, mIsApplicationMode);
	                                        childrenToRemove.add(view);                        
	                                    }
	                                }
	                            }
	                        } else if (tag instanceof LauncherAppWidgetInfo) {
	                            final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
	                            final AppWidgetProviderInfo provider =
	                                    widgets.getAppWidgetInfo(info.appWidgetId);
	                            if (provider != null) {
	                                for (String packageName: packageNames) {
	                                    if (packageName.equals(provider.provider.getPackageName())) {
	                                        // TODO: This should probably be done on a worker thread
	                                        LauncherModel.deleteItemFromDatabase(mLauncher, info, mIsApplicationMode);
	                                        childrenToRemove.add(view);                                
	                                    }
	                                }
	                            }
	                        }
                        }// end for app mode
                        
                    }
                    //layout.dumpCellInfo();
                    childCount = childrenToRemove.size();
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        layout.removeViewInLayout(child);
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget)child);
                        }
                    }
                    //layout.dumpCellInfo();
                    if (childCount > 0) {
                    	
                    	if(mIsApplicationMode)
                    		layout.reSortCellsForApps();
                    	
                        layout.requestLayout();
                        layout.invalidate();
                    }
                }
            });
        }
    }

    public void updateShortcuts(ArrayList<ApplicationInfo> apps) {
        final PackageManager pm = mLauncher.getPackageManager();

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo)tag;
                    // We need to check for ACTION_MAIN otherwise getComponent() might
                    // return null for some shortcuts (for instance, for shortcuts to
                    // web pages.)
                    final Intent intent = info.intent;
                    final ComponentName name = intent.getComponent();
                    if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                            Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                        final int appCount = apps.size();
                        for (int k=0; k<appCount; k++) {
                            ApplicationInfo app = apps.get(k);
                            if (app.componentName.equals(name)) {
                                info.setIcon(mIconCache.getIcon(info.intent));
                                ((TextView)view).setCompoundDrawablesWithIntrinsicBounds(null,
                                        new FastBitmapDrawable(info.getIcon(mIconCache)),
                                        null, null);
                                }
                        }
                    }
                }
            }
        }
    }

    public void moveToDefaultScreen(boolean animate) {
        if (animate) {
            snapToScreen(mDefaultScreen);
        } else {
            setCurrentScreen(mDefaultScreen);
        }
        getChildAt(mDefaultScreen).requestFocus();
    }

    public void setIndicators(Drawable previous, Drawable next) {
        mPreviousIndicator = previous;
        mNextIndicator = next;
        previous.setLevel(mCurrentScreen);
        next.setLevel(mCurrentScreen);
    }

    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
