package com.android.qshome.ctrl;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.android.qshome.Launcher;
import com.android.qshome.util.QsLog;

import com.android.qshome.R;

public class QsScreenIndicator extends View implements QsScreenIndicatorCallback{
	private int mDirection = 0;
	private Bitmap mCurScreenImg;
	private Bitmap mDefaultScreenImg;
	private Bitmap mMoreScreenImg;
	private int mImgPadding = 0;
	//private boolean mIsCreateNum = false;
	private int mTextSize = 0;
	private int mScreenPagesCount = 0;
	private int mCurrentScreen = 0;
	private final TextPaint mTextPaint;
	private Workspace mWorkspace;
	private Rect mBgPadding = new Rect();
	
	public QsScreenIndicator(Context context) {
        this(context, null);
    }

    public QsScreenIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QsScreenIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //mBitmapForegroud = ((BitmapDrawable) getResources().getDrawable(R.drawable.hud_pageturn_foreground)).getBitmap();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QsScreenIndicator, defStyle, 0);
        mDirection = a.getInt(R.styleable.QsScreenIndicator_direction, mDirection);
        
        Drawable thumb = a.getDrawable(R.styleable.QsScreenIndicator_curScreenImage);
        if (thumb != null) {
        	mCurScreenImg = ((BitmapDrawable)thumb).getBitmap();
        	thumb.getPadding(mBgPadding);
        }else{
        	mBgPadding.set(0, 0, 0, 0);
        }
        
        thumb = a.getDrawable(R.styleable.QsScreenIndicator_defaultScreenImage);
        if (thumb != null) {
        	mDefaultScreenImg = ((BitmapDrawable)thumb).getBitmap();
        }
        
        thumb = a.getDrawable(R.styleable.QsScreenIndicator_moreScreenImage);
        if (thumb != null) {
        	mMoreScreenImg = ((BitmapDrawable)thumb).getBitmap();
        }
       
        mImgPadding = a.getDimensionPixelSize(R.styleable.QsScreenIndicator_imagePadding, mImgPadding);
        if(a.getBoolean(R.styleable.QsScreenIndicator_isCreateNumber, false)){
        	mTextPaint = new TextPaint();
        	mTextPaint.setTypeface(Typeface.DEFAULT);
        	int nValue = a.getDimensionPixelSize(R.styleable.QsScreenIndicator_textSize, 12);
        	mTextPaint.setTextSize(nValue);
        	nValue = a.getColor(R.styleable.QsScreenIndicator_textColor, 0xffffffff);
        	mTextPaint.setColor(nValue);
        	mTextPaint.setAntiAlias(true);
        	mTextPaint.setTextAlign(Align.LEFT);
        }
        else
        	mTextPaint = null;
        
        a.recycle();
        
       //mScroller = new Scroller(getContext());
    }
//    
//    public void initial(Workspace workspace, Context context) {
//    	mWorkspace = workspace;
//    	mCurrentScreen = workspace.getCurrentScreen();
//        
//        workspace.setQsScreenIndicatorCallback(this);
//    }
    
    public void initial(Context context, QsScreenIndicatorLister lister){
    	mCurrentScreen = lister.getCurrentScreen();
    	mScreenPagesCount = lister.getScreenCount();
    	
    	lister.setQsScreenIndicatorCallback(this);
    }
    
    public void onScrollChangedCallback(int l, int t, int oldl, int oldt){
    	//postInvalidate();
    	
    }
    
	public void onChangeToScreen(int whichScreen){
		if(mCurrentScreen != whichScreen)
		{
			mCurrentScreen = whichScreen;
			super.invalidate();
		}
	}
	
	public void onPageCountChanged(int nNewCount){
		
		if(nNewCount != mScreenPagesCount){
			mScreenPagesCount = nNewCount;
			
			if(mCurrentScreen >= mScreenPagesCount){
				mCurrentScreen = mScreenPagesCount - 1;
			}
			
			super.invalidate();
		}
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
		
		Drawable bg = getBackground();
		if(bg != null)
			bg.draw(canvas);
		else
			canvas.drawColor(Color.TRANSPARENT);
		
		if(mCurScreenImg != null){
			// like samsung page indicator style
			if(mDefaultScreenImg != null){
		        if(mDirection > 0)//
		        {
		        	if(mScreenPagesCount > 0)
		        		drawHorizontal(canvas, mScreenPagesCount);
		        	else if(mWorkspace != null)
		        		drawHorizontal(canvas, mWorkspace.getChildCount());
		        }
		        else
		        {
		        	if(mScreenPagesCount > 0)
		        		drawVertical(canvas, mScreenPagesCount);
		        	else if(mWorkspace != null)
		        		drawVertical(canvas, mWorkspace.getChildCount());
		        }
			}
		}
        //Log.d("QsHtcLauncher", "PageTurnView==height:"+mBitmapForegroud.getHeight()+"=");
        //PageTurnView.drawImage(canvas, mBitmapForegroud, 0, 0, mDrawW, mBitmapForegroud.getHeight(), -mScrollX, 0);
        super.onDraw(canvas);
    }
	
//	private void drawNormalScrollBar(Canvas canvas, int nScreenCount){
//		
//		int nScrollWidth = (super.getWidth() - super.getPaddingLeft() - super.getPaddingRight()) / nScreenCount;
//		int xPos = mCurrentScreen / nScreenCount;
//		
//	}
	
	private void drawHorizontal(Canvas canvas, int nScreenCount){
		//int layout = getGravity();
		if(nScreenCount <= 0)
			return;
		
		int width = getWidth();
		int nTotalWidth = (nScreenCount - 1) * mDefaultScreenImg.getWidth() + (nScreenCount + 1) * mImgPadding + mCurScreenImg.getWidth();
		//QsLog.LogD("drawHorizontal()==w:"+width+"=nTotalWidth:"+nTotalWidth);
		boolean bShowMore = false;
		if(nTotalWidth > width)
			bShowMore = true;
		
		int nLeft = (width - nTotalWidth)/2;
		int nTop = (getHeight() - mDefaultScreenImg.getHeight())/2;
		for(int i=0; i<mCurrentScreen; i++){
			canvas.drawBitmap(mDefaultScreenImg, nLeft, nTop, null);
			nLeft += mDefaultScreenImg.getWidth() + mImgPadding;
		}
		
		int nCurTop = (getHeight() - mCurScreenImg.getHeight())/2;
		canvas.drawBitmap(mCurScreenImg, nLeft, nCurTop, null);
		if(mTextPaint != null){
			Rect bounds = new Rect();
			String str = String.valueOf(mCurrentScreen+1);
			mTextPaint.getTextBounds(str, 0, str.length(), bounds);
			int x = nLeft + (mCurScreenImg.getWidth() - bounds.width() - mBgPadding.left - mBgPadding.right)/2;
			int y = nCurTop + (mCurScreenImg.getHeight() + bounds.height())/2;
			//QsLog.LogD("drawHorizontal(1)==str:"+str+"=nLeft:"+nLeft+"==nCurTop:"+nCurTop+"==x:"+x+"==y:"+y+"==th:"+bounds.height()+"=ih:"+mCurScreenImg.getHeight());
			canvas.drawText(str, x, y, mTextPaint);
		}
		nLeft += mCurScreenImg.getWidth() + mImgPadding;
		
		for(int i=mCurrentScreen+1; i<nScreenCount; i++){
			canvas.drawBitmap(mDefaultScreenImg, nLeft, nTop, null);
			nLeft += mDefaultScreenImg.getWidth() + mImgPadding;
		}
	}
	
	private void drawVertical(Canvas canvas, int nScreenCount){
		if(nScreenCount <= 0)
			return;
		int height = getHeight();
		int nTotalHeight = (nScreenCount - 1) * mDefaultScreenImg.getWidth() + (nScreenCount + 1) * mImgPadding + mCurScreenImg.getWidth();
		//QsLog.LogD("drawHorizontal()==w:"+width+"=nTotalWidth:"+nTotalWidth);
		boolean bShowMore = false;
		if(nTotalHeight > height)
			bShowMore = true;
		
		int nTop = (height - nTotalHeight)/2;
		int nLeft  = (getWidth() - mDefaultScreenImg.getWidth())/2;
		for(int i=0; i<mCurrentScreen; i++){
			canvas.drawBitmap(mDefaultScreenImg,nLeft,nTop, null);
			nTop += mDefaultScreenImg.getHeight() + mImgPadding;
		}
        final Resources resources = getResources();
        final float gap_x = resources.getDimension(R.dimen.screenindeictor_Vertical_gap_x);
        final float gap_y = resources.getDimension(R.dimen.screenindeictor_Vertical_gap_y);
        
		int nCurLeft = (getWidth() - mCurScreenImg.getWidth())/2;
		canvas.drawBitmap(mCurScreenImg,nCurLeft , nTop, null);
		if(mTextPaint != null){
			Rect bounds = new Rect();
			String str = String.valueOf(mCurrentScreen+1);
			mTextPaint.getTextBounds(str, 0, str.length(), bounds);
			int y = nTop + (mCurScreenImg.getHeight() - bounds.height() - mBgPadding.left - mBgPadding.right)/2 + (int)gap_y;
			int x = nCurLeft + (mCurScreenImg.getWidth() + bounds.width())/2 - (int)gap_x ;
			//QsLog.LogD("drawHorizontal(1)==str:"+str+"=nLeft:"+nLeft+"==nCurTop:"+nCurTop+"==x:"+x+"==y:"+y+"==th:"+bounds.height()+"=ih:"+mCurScreenImg.getHeight());
			canvas.drawText(str, x, y, mTextPaint);
		}
		nTop += mCurScreenImg.getHeight() + mImgPadding;
		
		for(int i=mCurrentScreen+1; i<nScreenCount; i++){
			canvas.drawBitmap(mDefaultScreenImg, nLeft, nTop, null);
			nTop += mDefaultScreenImg.getWidth() + mImgPadding;
		}
	}
}

