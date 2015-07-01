package com.android.qshome.ctrl;

import com.android.qshome.Launcher;
import com.android.qshome.util.QsLog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Scroller;


import com.android.qshome.R;

public class QsScrollbar extends View implements QsScreenIndicatorCallback {
	//private Workspace mWorkspace;
	//private Scroller mScroller;
	public static final int ORIENTATION_HORIZONTAL = 1;
	private int mDirection = 0;
	
	private int mThumbWidth;
	
    private int mOffsetX;
    private float mfXstep = 1.0f;
    private int mCurPosX;
    
    private Drawable mThumb;
    private Bitmap mThumbBitmap;
    private Drawable mTrackBg;
    
    private int mScreenWidth;
    private int mScreenPagesCount;
    private int mCurrentPage;
    private final int mAutoHideThumbTime;
    private boolean mThumbIsVisible = true;
	
	public QsScrollbar(Context context) {
        this(context, null);
    }

    public QsScrollbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QsScrollbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //mBitmapForegroud = ((BitmapDrawable) getResources().getDrawable(R.drawable.hud_pageturn_foreground)).getBitmap();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QsScrollbar, defStyle, 0);
        mDirection = a.getInt(R.styleable.QsScrollbar_direction, ORIENTATION_HORIZONTAL);
        mThumb = a.getDrawable(R.styleable.QsScrollbar_scrollbarThumbImage);
        if (mThumb != null && !(mThumb instanceof NinePatchDrawable)) {
        	mThumbBitmap = ((BitmapDrawable)mThumb).getBitmap();
        }
        
        mTrackBg = a.getDrawable(R.styleable.QsScrollbar_scrollbarTrackImage);
        mAutoHideThumbTime  = a.getInt(R.styleable.QsScrollbar_autoHideThumb, 0);
        
        a.recycle();
    }
        
    public void initial(Context context, QsScreenIndicatorLister lister){
    	
    	mCurrentPage = lister.getCurrentScreen();
    	mScreenPagesCount = lister.getScreenCount();
    	
    	lister.setQsScreenIndicatorCallback(this);
    }
    
    private void computeThumbInfo(){
    	if(super.getWidth() == 0 || super.getHeight() == 0)
    		return;
    	
    	//mScreenWidth = dm.widthPixels;
    	if(mDirection != ORIENTATION_HORIZONTAL){
    		mScreenWidth = super.getHeight();
    	}else{
    		mScreenWidth = super.getWidth();// - super.getPaddingLeft() - super.getPaddingRight();
    	}
        mThumbWidth = mScreenWidth / mScreenPagesCount;
        mOffsetX = mScreenWidth % mScreenPagesCount;

        //mScrollX = -(mThumbWidth * currentScreen + mOffsetX / 2);
        //super.scrollTo(-(mThumbWidth * currentScreen + mOffsetX / 2), 0);
        mfXstep = 1.0f / (mScreenPagesCount - 1);
        
        mCurPosX = /*super.getPaddingLeft() + */(mThumbWidth * mCurrentPage + mOffsetX / 2);
        
        //QsLog.LogD("QsScrollbar::computeThumbInfo()====mScreenWidth:"+mScreenWidth+"=Width:"+getWidth()+"=mThumbWidth:"+mThumbWidth+"=mOffsetX:"+mOffsetX+"=mCurPosX:"+mCurPosX);
    }
    
    Runnable mAutoHideRunnable = new Runnable() {
        public void run() {
        	mThumbIsVisible = false;
        	invalidate();
        }
    };
    
    private void updateThumbStatus(){
    	if(mAutoHideThumbTime > 0){
    		removeCallbacks(mAutoHideRunnable);
    		postDelayed(mAutoHideRunnable, mAutoHideThumbTime * 1000);
    	}
    }
    
    private void showThumbStatus(){
    	mThumbIsVisible = true;
    	postInvalidate();
    }
    
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
    	//QsLog.LogE("QsScrollbar::onVisibilityChanged()========visibility:"+visibility);
    	if(mAutoHideThumbTime > 0){
	        if (visibility == VISIBLE) {
	        	showThumbStatus();
	        }else{
	        	removeCallbacks(mAutoHideRunnable);
	        }
    	}
        
        super.onVisibilityChanged(changedView, visibility);
    }
    
    @Override
    public void invalidate(){
    	updateThumbStatus();
    	super.invalidate();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    	//QsLog.LogD("QsScrollbar::onSizeChanged()====w:"+w+"==h:"+h);
    	
    	computeThumbInfo();
    }

    public void onScrollChangedCallback(int l, int t, int oldl, int oldt){
    	//postInvalidate();
    	int nSpace = l - oldl;
    	if((nSpace > 0 && nSpace < mScreenWidth ) || (nSpace < 0 && nSpace > -mScreenWidth)){
    		mCurPosX += (int)(nSpace*mfXstep);
    		showThumbStatus();
    		//super.invalidate();
    	}
    	//QsLog.LogD("QsScrollbar::onScrollChangedCallback()====mCurPosX:"+mCurPosX+"==nSpace:"+nSpace);
    }
    
	public void onChangeToScreen(int whichScreen){
		int nPosX = /*super.getPaddingLeft() + */(mThumbWidth * whichScreen + mOffsetX / 2);
		if(nPosX != mCurPosX){
			mCurPosX = nPosX;
			showThumbStatus();
			//postInvalidate();
		}
	}
	
	public void onPageCountChanged(int nNewCount){
		if(mScreenPagesCount != nNewCount){
			mScreenPagesCount = nNewCount;
			
			computeThumbInfo();
			
			showThumbStatus();
		}
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
		if(mThumbWidth == 0){
			computeThumbInfo();
		}
		
		if(mThumbWidth == 0 || mThumb == null)
			return;

		Drawable bg = getBackground();
		if(bg != null){
//			bg.setBounds(super.getPaddingLeft(), super.getPaddingTop(), 
//					super.getWidth() - super.getPaddingRight(), 
//					super.getHeight() - super.getPaddingBottom());
			bg.draw(canvas);
		}
		
        if(mThumbBitmap != null){
        	
            canvas.drawColor(Color.TRANSPARENT);
            
	        Rect src = new Rect();
	        Rect dst = new Rect();
	        if(mDirection != ORIENTATION_HORIZONTAL){
	        
	        	src.left = 0;
		        src.top = mCurPosX;
		        src.right = src.left + mThumbBitmap.getWidth();
		        src.bottom = src.top + mThumbWidth;
		        
		        dst.left = super.getWidth() - mThumbBitmap.getWidth();
		        dst.top = src.top;
		        dst.right = super.getWidth();
		        dst.bottom = dst.top + mThumbWidth;
		        canvas.drawBitmap(mThumbBitmap, src, dst, null);

	        }else{
		        src.left = mCurPosX;
		        src.top = 0;
		        src.right = src.left + mThumbWidth;
		        src.bottom = src.top + mThumbBitmap.getHeight();
		        
		        //QsLog.LogD("QsScrollbar::onDraw()====mCurPosX:"+mCurPosX+"=h:"+super.getHeight()+"==imgh:"+mThumbBitmap.getHeight()+"==t:"+src.top+"=l:"+src.left);
		        dst.left = src.left;
		        dst.top = super.getHeight() - mThumbBitmap.getHeight();
		        dst.right = dst.left + mThumbWidth;
		        dst.bottom = getHeight();
		        canvas.drawBitmap(mThumbBitmap, src, dst, null);
	        }
	        src = null;
	        dst = null;
	        
        }else{
        	
        	if(mThumbIsVisible){
	        	mThumb.setBounds(mCurPosX, 0, mCurPosX + mThumbWidth, getHeight());
	        	mThumb.draw(canvas);
        	}
        	
        }
        //Log.d("QsHtcLauncher", "PageTurnView==height:"+mBitmapForegroud.getHeight()+"=");
        //PageTurnView.drawImage(canvas, mBitmapForegroud, 0, 0, mDrawW, mBitmapForegroud.getHeight(), -mScrollX, 0);
        
        super.onDraw(canvas);
    }
}
