package com.android.qshome.ctrl;

import java.util.ArrayList;
import java.util.HashSet;

import com.android.qshome.AllAppsView;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.model.LiveFolderInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnLongClickListener;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.TextView;

import com.android.qshome.R;

public class AllAppsGridLayoutPages  extends ViewGroup implements AllAppsView,
				DragSource, QsScreenIndicatorLister{
	
	private Launcher mLauncher;
    private DragController mDragController;
    private final LayoutInflater mInflater;
    
	private int mDefaultScreen;
    //private boolean mIsEnableWallpaper;
    private int mPageLayoutResource;
    private int mShortcutLayoutResource;
    
    private int mCurrentScreen;
    private int mNextScreen = Workspace.INVALID_SCREEN;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    
    //private static final int TOUCH_STATE_REST = 0;

    //private static final int TOUCH_STATE_SCROLLING = 1;

    //private static final int SNAP_VELOCITY = 150;
    //private boolean mAllowLongPress = true;
    private int mTouchState = Workspace.TOUCH_STATE_REST;

    private int mTouchSlop;

    private float mLastMotionX;

    private float mLastMotionY;

    private int mMaximumVelocity;

    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;

    //private final static boolean isScrollMode = true;

    private WorkspaceOvershootInterpolator mScrollInterpolator;

    //private static final float BASELINE_FLING_VELOCITY = 2500.f;

    //private static final float FLING_VELOCITY_INFLUENCE = 0.06f;
    
    private int mOverscrollDistance; 
    private OnLongClickListener mLongClickListener;
    
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
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }
    
    private QsScreenIndicatorCallback mQsWorkspaceCallback;
    public void setQsScreenIndicatorCallback(QsScreenIndicatorCallback callback){
    	mQsWorkspaceCallback = callback;
    }
    
	public AllAppsGridLayoutPages(Context context) {
        this(context, null);
    }

    public AllAppsGridLayoutPages(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public AllAppsGridLayoutPages(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AllAppsGridLayoutPages, defStyle, 0);
        mDefaultScreen = a.getInt(R.styleable.AllAppsGridLayoutPages_defaultScreen, 0);
        //mIsEnableWallpaper = a.getBoolean(R.styleable.AllAppsGridLayoutPages_isEnableWallpaper, true);
        mPageLayoutResource = a.getResourceId(R.styleable.AllAppsGridLayoutPages_page_layout, R.layout.page_grid_layout);
        mShortcutLayoutResource = a.getResourceId(R.styleable.AllAppsGridLayoutPages_shortcut_layout, R.layout.application_boxed_ics);
        
        a.recycle();
        
        mInflater = LayoutInflater.from(context);
        
        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(context, mScrollInterpolator);
                
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop()*2/3;
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
    }
    
//    /**
//     * @return True is long presses are still allowed for the current touch
//     */
//    public boolean allowLongPress() {
//        return mAllowLongPress;
//    }
//    
//    /**
//     * Set true to allow long-press events to be triggered, usually checked by
//     * {@link Launcher} to accept or block dpad-initiated long-presses.
//     */
//    public void setAllowLongPress(boolean allowLongPress) {
//        mAllowLongPress = allowLongPress;
//    }
    
    public void setToScreen(int whichScreen) {
	     //whichScreen = Math.max(0, Math.min(whichScreen, super.getChildCount() - 1));
	     //mCurrentScreen = whichScreen;
	     //scrollTo(whichScreen * getWidth(), 0);
	     
	     snapToScreen(whichScreen, 0, false);
    }
    
    public void setToDefaultScreen()
    {
        //android.util.Log.d("QsLog", "AllAppsGridLayoutPages==mDefaultScreen:"+mDefaultScreen+"==mTotalVisiablePageCount:"+mTotalVisiablePageCount);
        setToScreen(mDefaultScreen);
    }
    
    public int getCurrentScreen() {
        return mCurrentScreen;
    }
    
    public int getScreenCount(){
    	return super.getChildCount();
    }

    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            //scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        	mScrollX = mScroller.getCurrX();
        	mScrollY = mScroller.getCurrY();
        	
            postInvalidate();
        } else if (mNextScreen != Workspace.INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            mNextScreen = Workspace.INVALID_SCREEN;
            //clearChildrenCache();
        }
    }
        
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    	super.onScrollChanged(l, t, oldl, oldt);
    	//QsLog.LogW("AllAppsGridLayoutPages::onScrollChanged()==l:"+l+"=t:"+t+"=oldl:"+oldl+"==oldt:"+oldt);
    	if(mQsWorkspaceCallback != null){
        	mQsWorkspaceCallback.onScrollChangedCallback(l, t, oldl, oldt);
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
        //super.setOnLongClickListener(l);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mTouchState = Workspace.TOUCH_STATE_SCROLLING;
                mLastMotionX = event.getX();
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState == Workspace.TOUCH_STATE_SCROLLING) {
                    final int pointerIndex = event.findPointerIndex(mActivePointerId);
                    if(pointerIndex < 0)
                    	break;
                    
                    final float x = event.getX(pointerIndex);
                    float deltaX = mLastMotionX - x;
                    mLastMotionX = x;
                    if (deltaX < 0) {
                        if (getScrollX() > 0) {
                            scrollBy((int) Math.max(-getScrollX(), deltaX), 0);
                        }
                        else if(-getScrollX() < mOverscrollDistance)
                        {
                        	scrollBy((int) Math.max(-mOverscrollDistance-getScrollX(), deltaX), 0);
                        }
                    } else if (deltaX > 0) {

                        final int availableToScroll = super.getChildCount() * getWidth()
                                - getScrollX() - getWidth() + mOverscrollDistance;
                        //android.util.Log.d("QsLog", "onTouchEvent==ACTION_MOVE==availableToScroll:"+availableToScroll+"==deltaX:"+deltaX+"==scx:"+getScrollX());
                        
                        if (availableToScroll > 0) {

                            scrollBy((int) Math.min(availableToScroll, deltaX), 0);
                        }
                    } else {
                        awakenScrollBars();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == Workspace.TOUCH_STATE_SCROLLING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
                    final int screenWidth = getWidth();
                    int whichScreen = 0;
                    if(getScrollX() < 0)
                    	whichScreen = 0;
                    else 
                    {
                    	whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
                    	if(whichScreen >= super.getChildCount())
                    		whichScreen = super.getChildCount() - 1;
                    }
                    
                    final float scrolledPos = (float) getScrollX() / screenWidth;
                    
                    if (velocityX > Workspace.SNAP_VELOCITY && mCurrentScreen > 0) {
                        // Fling hard enough to move left.
                        // Don't fling across more than one screen at a
                        // time.
                        final int bound = scrolledPos < whichScreen ? mCurrentScreen - 1
                                : mCurrentScreen;
                        snapToScreen(Math.min(whichScreen, bound), velocityX, true);
                    } else if (velocityX < -Workspace.SNAP_VELOCITY && mCurrentScreen < super.getChildCount() - 1) {
                        // Fling hard enough to move right
                        // Don't fling across more than one screen at a
                        // time.
                        final int bound = scrolledPos > whichScreen ? mCurrentScreen + 1
                                : mCurrentScreen;
                        snapToScreen(Math.max(whichScreen, bound), velocityX, true);
                    } else {
                        snapToScreen(whichScreen, 0, true);
                    }
                }
                mTouchState = Workspace.TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = Workspace.TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
        }
        return true;

    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != Workspace.TOUCH_STATE_REST)) {
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if(pointerIndex < 0)
                	break;
                
                final float x = ev.getX(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                if (xMoved) {
                    mTouchState = Workspace.TOUCH_STATE_SCROLLING;
                }
            }
                break;
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? Workspace.TOUCH_STATE_REST : Workspace.TOUCH_STATE_SCROLLING;
            }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = Workspace.TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return mTouchState != Workspace.TOUCH_STATE_REST;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
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

    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
        whichScreen = Math.max(0, Math.min(whichScreen, super.getChildCount() - 1));
        
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen
                && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }

        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - getScrollX();
        int duration = (screenDelta + 1) * 100;

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
            duration += (duration / (velocity / Workspace.BASELINE_FLING_VELOCITY))
                    * Workspace.FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 32;
        }

        awakenScrollBars(duration);
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);

        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onChangeToScreen(whichScreen);
        
        invalidate();
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
            final PageGridLayout layout = (PageGridLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

    public void clearChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final PageGridLayout layout = (PageGridLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
        }
    }
    
    private void addNewPage(){
    	PageGridLayout layout = (PageGridLayout) mLauncher.getLayoutInflater().inflate(
    			mPageLayoutResource, this, false);
    	layout.setClickable(true);
        addView(layout, -1);
        
        if(mQsWorkspaceCallback != null)
        	mQsWorkspaceCallback.onPageCountChanged(getScreenCount());
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    	final int count = getChildCount();

    	int nLeft = 0;
    	for (int i = 0; i < count; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
            	
            	nLeft += super.getPaddingLeft();
                childView.layout(nLeft, super.getPaddingTop(), 
                		nLeft + childView.getMeasuredWidth(),
                		super.getPaddingTop() + childView.getMeasuredHeight());
                
                nLeft += childView.getMeasuredWidth() + super.getPaddingRight();
            }
        }
    }

    /**
     * This method is called twice in practice. The first time both
     * with and height are constraint by AT_MOST. The second time, the
     * width is still AT_MOST and the height is EXACTLY. Either way
     * the full width/height should be in mWidth and mHeight and we
     * use 'resolveSize' to do the right thing.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        
        if (widthMode != MeasureSpec.EXACTLY) {
        	return;
            //throw new IllegalStateException("AllAppsGridLayoutPages only can run at EXACTLY mode!");
        }

        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
        	return;
            //throw new IllegalStateException("AllAppsGridLayoutPages only can run at EXACTLY mode!");
        }
        
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
        	
        	final View childView = getChildAt(i);
        	final ViewGroup.LayoutParams lp =
                (ViewGroup.LayoutParams) childView.getLayoutParams();
        	
        	int nChildMeasureWidth = 0;
        	if(lp.width > 0)
        		nChildMeasureWidth = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        	else if(lp.width == ViewGroup.LayoutParams.MATCH_PARENT || lp.width == ViewGroup.LayoutParams.FILL_PARENT)
        		nChildMeasureWidth = MeasureSpec.makeMeasureSpec(width - super.getPaddingLeft() - super.getPaddingRight(), MeasureSpec.EXACTLY);
        	else
        		nChildMeasureWidth = MeasureSpec.makeMeasureSpec(childView.getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        	
        	int nChildMeasureHeight = 0;
        	if(lp.height > 0)
        		nChildMeasureHeight = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        	else if(lp.height == ViewGroup.LayoutParams.MATCH_PARENT || lp.height == ViewGroup.LayoutParams.FILL_PARENT)
        		nChildMeasureHeight = MeasureSpec.makeMeasureSpec(height - super.getPaddingTop() - super.getPaddingBottom(), MeasureSpec.EXACTLY);
        	else
        		nChildMeasureHeight = MeasureSpec.makeMeasureSpec(childView.getMeasuredHeight(), MeasureSpec.UNSPECIFIED);
        	//android.util.Log.d("QsLog", "onMeasure==i:"+i+"=width:"+childView.getMeasuredWidth()+"==height:"+childView.getMeasuredHeight()+"=lpw:"+lp.width+"=lph:"+lp.height);

        	childView.measure(nChildMeasureWidth, nChildMeasureHeight);
        }
               
        //setMeasuredDimension(width, height);
    }
    
    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }
	
	public void setDragController(DragController dragger) {
        mDragController = dragger;
    }
	
	public void startDrag(View view, ApplicationInfo info){
		
		mDragController.startDrag(view, this, info, DragController.DRAG_ACTION_COPY);
	}
	
	public View createApplicationView(ApplicationInfo info, ViewGroup parent) {

        TextView favorite = (TextView) mInflater.inflate(mShortcutLayoutResource, parent, false);

        info.iconBitmap.setDensity(Bitmap.DENSITY_NONE);
        
        favorite.setCompoundDrawablesWithIntrinsicBounds(null,
                new FastBitmapDrawable(info.iconBitmap),
                null, null);
        favorite.setText(info.title);
        favorite.setTag(info);
        
        favorite.setOnClickListener(mLauncher);
        favorite.setOnLongClickListener(mLauncher);

        return favorite;
    }
	
	private void updateApplicationViewInfo(View v, ApplicationInfo info){
		TextView favorite = (TextView)v;

        info.iconBitmap.setDensity(Bitmap.DENSITY_NONE);
        
        favorite.setCompoundDrawablesWithIntrinsicBounds(null,
                new FastBitmapDrawable(info.iconBitmap),
                null, null);
        favorite.setText(info.title);
        favorite.setTag(info);
	}
    
    public void invalidatePages(){
    	invalidatePages(mCurrentScreen);
    }
    
    private void invalidatePages(int screen){
    	getChildAt(screen).invalidate();
    }
    
    private boolean addAppInScreen(int nScreen, View shortcut){
    	final PageGridLayout cellLayout = (PageGridLayout) getChildAt(nScreen);
    	
    	if(cellLayout.addItem(shortcut) < 0){
    		return false;
    	}
    		
    	return true;
    }

    public void onDropCompleted(View target, DragObject d, boolean success) {
    }
    
    public void zoom(float zoom, boolean animate) {
    	if(zoom > 0.0f){
    		if(!isVisible()){
	    		setVisibility(View.VISIBLE);
    		}
    	}
    	else{
    		if(isVisible()){
	    		//mWorkspace.hideWorkspace();
	    		setVisibility(View.GONE);
    		}
    	}
    }
    
    public boolean isVisible() {
        return (super.getVisibility() == View.VISIBLE);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }


    public void setApps(ArrayList<ApplicationInfo> list) {
    	if(mLauncher == null)
    		return;
    	
    	super.removeAllViews();
    	addNewPage();
    	
    	final int N = list.size();
    	int nScreen = 0; 
    	boolean bGoted;
        for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            //final ShortcutInfo shortcutInfo = item.makeShortcut();
            View shortcut = createApplicationView(item, null);//mLauncher.createShortcut(mShortcutLayoutResource, null, shortcutInfo);

            do{
            	
            	if(!(bGoted = addAppInScreen(nScreen, shortcut))){
            		//QsLog.LogE("AllAppsWorkspace::setApps(0)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
	            	nScreen++;
	            	if(nScreen >= getChildCount()){
	            		addNewPage();
	            	}
	            	//QsLog.LogE("AllAppsWorkspace::setApps(1)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
            	}
	            
            }while(!bGoted);
        }
    }
    
    public void reorderApps(){
    	
    }

    public void addApps(ArrayList<ApplicationInfo> list) {
    	final int N = list.size();
    	int nScreen = 0; 
    	int nPagesCount = getChildCount();
    	for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            //final ShortcutInfo shortcutInfo = item.makeShortcut();
            View shortcut = createApplicationView(item, null);//mLauncher.createShortcut(mShortcutLayoutResource, null, shortcutInfo);
            
            while(nScreen < nPagesCount){
            	
            	if (addAppInScreen(nScreen, shortcut)) {
	                break;
	            }
            	
            	nScreen++;
            }
            
//            for(nScreen=0; nScreen<nPagesCount; nScreen++){
//            	if (addAppInScreen(nScreen, shortcut, shortcutInfo)) {
//	                break;
//	            }
//            }
            //QsLog.LogE("AllAppsWorkspace::setApps(0)==nScreen:"+nScreen+"==count:"+nScreenCount);
            if(nScreen >= nPagesCount){
            	addNewPage();
            	nPagesCount = getChildCount();
            	addAppInScreen(nScreen, shortcut);
            	//QsLog.LogE("AllAppsWorkspace::setApps(1)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
            }
        }
    }

    public void removeApps(ArrayList<ApplicationInfo> list) {
    	
    	final int count = getChildCount();
        final PackageManager manager = getContext().getPackageManager();
        final AppWidgetManager widgets = AppWidgetManager.getInstance(getContext());

        final HashSet<ComponentName> packageNames = new HashSet<ComponentName>();
        final int appCount = list.size();
        for (int i = 0; i < appCount; i++) {
            packageNames.add(list.get(i).componentName);
        }
        
        //int nPackageCount = appCount;
        for (int i = 0; i < count && packageNames.size() > 0; i++) {
            final ViewGroup pagelayout = (ViewGroup) getChildAt(i);

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    childrenToRemove.clear();
        
                    int childCount = pagelayout.getChildCount();
                    for (int j = 0; j < childCount && packageNames.size() > 0; j++) {
                    	
                        final View view = pagelayout.getChildAt(j);
                        Object tag = view.getTag();
        
                        if (tag != null && (tag instanceof ApplicationInfo)) {
                        	
                            final ApplicationInfo info = (ApplicationInfo) tag;
        
                            for (ComponentName packageName: packageNames) {
                                if (packageName.equals(info.componentName)) {
                                    // TODO: This should probably be done on a worker thread
                                     childrenToRemove.add(view);
                                     packageNames.remove(packageName);
                                     break;
                                }
                            }
                        } 
                    }
                    //layout.dumpCellInfo();
                    childCount = childrenToRemove.size();
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        pagelayout.removeViewInLayout(child);
                    }
                    //layout.dumpCellInfo();
                    if (childCount > 0) {
                    	pagelayout.requestLayout();
                    	//pagelayout.invalidate();
                    }
                }
            });
        }
        
        invalidatePages();
    }

    public void updateApps(ArrayList<ApplicationInfo> list) {
    	
        // Just remove and add, because they may need to be re-sorted.
    	final PackageManager pm = mLauncher.getPackageManager();

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final PageGridLayout layout = (PageGridLayout) getChildAt(i);
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();
                if(tag != null && (tag instanceof ApplicationInfo)) 
                {               	
                	ApplicationInfo info = (ApplicationInfo)tag;

                	final int appCount = list.size();
                    for (int k=0; k<appCount; k++) 
                    {
                        ApplicationInfo app = list.get(k);
                        if (app.componentName.equals(info.componentName)) 
                        {
                        	updateApplicationViewInfo(view, app);
                        	break;
                        }
                    }
                }
            }
        }
        
        invalidatePages();
    }


    public void dumpState() {
        //ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList", mAllAppsList);
    }
    
    public void surrender() {
    }
    
    public void release(){
    	super.removeAllViews();
    }
    //end implements
    
    
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
}
