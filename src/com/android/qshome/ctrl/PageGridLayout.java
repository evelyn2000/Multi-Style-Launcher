package com.android.qshome.ctrl;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.qshome.R;
import com.android.qshome.util.QsLog;

public class PageGridLayout extends ViewGroup{
	private static final int ORIENTATION_HORIZONTAL = 1;
	private int mDirection = 1;
	
	private int mCellWidth;
    private int mCellHeight;
    
    private int mMaxCellsCount;
    private int mMaxRowsCount;
    //private int mChildPadding = 0;
    
	public PageGridLayout(Context context) {
        this(context, null);
    }

    public PageGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PageGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageGridLayout, defStyle, 0);

        mCellWidth = a.getDimensionPixelSize(R.styleable.PageGridLayout_cellWidth, 60);
        mCellHeight = a.getDimensionPixelSize(R.styleable.PageGridLayout_cellHeight, 60);
        
        mMaxCellsCount = a.getInt(R.styleable.PageGridLayout_maxCellsCount, 4);
        mMaxRowsCount = a.getInt(R.styleable.PageGridLayout_maxRowsCount, 4);
        
        mDirection = a.getInt(R.styleable.PageGridLayout_direction, ORIENTATION_HORIZONTAL);

        a.recycle();
        
        //mChildPadding = context.getResources().getDimensionPixelSize(R.id.droidics_page_grid_item_hor_padding);
    }
    
    public int addItem(View item){
    	return InsertItem(item, -1);
    }
    
    public int InsertItem(View item, int index){
    	final int count = getChildCount();
    	if(count < mMaxCellsCount * mMaxRowsCount){
    		
    		if(index < 0){
    			super.addView(item);
        		return count;
    		}
    		
    		super.addView(item, index);
    		return index;
    	}
    	
    	return -1;
    }
    
//    @Override
//    public void addView(View child, int index) {
//        super.addView(child, index);
//        
//        child.setPadding(mChildPadding, 0, mChildPadding, 0);
//    }

    public boolean isFull(){
    	return ((getChildCount() < (mMaxCellsCount * mMaxRowsCount)) ? false : true);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    	final int count = getChildCount();
    	//final int nTopPadding = ((bottom - top) - (mMaxRowsCount * mCellHeight))/2;
    	//final int nLeftPadding = ((right - left) - (mMaxCellsCount * mCellWidth))/2;
    	final int vSpace = ((bottom - top - super.getPaddingTop() - super.getPaddingBottom()) - (mMaxRowsCount * mCellHeight)) / (mMaxRowsCount + 1);
    	final int hSpace = ((right - left - super.getPaddingLeft() - super.getPaddingRight()) - (mMaxCellsCount * mCellWidth)) / (mMaxCellsCount + 1);
    	
    	int x = 0;//hSpace;//nLeftPadding;
    	int y = 0;//vSpace;//nTopPadding;
    	if(mDirection == ORIENTATION_HORIZONTAL){
    		
    		for (int i = 0; i < count; i++) {
            	
            	final View childView = getChildAt(i);
            	final int childWidth = Math.min(mCellWidth, childView.getMeasuredWidth());
            	final int childHeight = Math.min(mCellHeight, childView.getMeasuredHeight());
            	//QsLog.LogD("====w:"+childView.getWidth()+"==w1:"+childWidth+"==w2:"+childView.getMeasuredWidth());
            	//int nRow = i / mMaxCellsCount;
            	int nCell = i % mMaxCellsCount;
//            	int x = nLeftPadding + nCell * mCellWidth + (mCellWidth - childWidth) / 2;
//            	int y = nTopPadding + nRow * mCellHeight + (mCellHeight - childHeight) / 2;
            	if(nCell == 0){
           			y += vSpace + (i > 0 ? mCellHeight : super.getPaddingTop());// + (mCellHeight - childHeight) / 2;
            		x = hSpace + super.getPaddingLeft();//nLeftPadding;
            	}
            	int xPos = x + (mCellWidth - childWidth) / 2;
            	int yPos = y + (mCellHeight - childHeight) / 2;
            	
                childView.layout(xPos, yPos, xPos + childWidth, yPos + childHeight);
                x += mCellWidth + hSpace;
                
            }
    		
    	}else{
    		
    		for (int i = 0; i < count; i++) {
            	
            	final View childView = getChildAt(i);
            	final int childWidth = Math.min(mCellWidth, childView.getMeasuredWidth());
            	final int childHeight = Math.min(mCellHeight, childView.getMeasuredHeight());
            	//QsLog.LogD("====w:"+childView.getWidth()+"==w1:"+childWidth+"==w2:"+childView.getMeasuredWidth());
            	int nRow = i / mMaxRowsCount;
            	//int nCell = i % mMaxRowsCount;
            	//int x = nLeftPadding + nCell * mCellWidth + (mCellWidth - childWidth) / 2;
            	//int y = nTopPadding + nRow * mCellHeight + (mCellHeight - childHeight) / 2;
            	if(nRow == 0){
            		x += hSpace + (i > 0 ? mCellWidth : super.getPaddingLeft());// + (mCellHeight - childHeight) / 2;
            		y = vSpace + super.getPaddingTop(); //nTopPadding;
            	}
            	
            	int xPos = x + (mCellWidth - childWidth) / 2;
            	int yPos = y + (mCellHeight - childHeight) / 2;
            	
                childView.layout(xPos, yPos, xPos + childWidth, yPos + childHeight);
                y += mCellHeight + vSpace;
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
            throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }

        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
        }
        
        final int nMaxCellWidth = Math.min(mCellWidth, (width - super.getPaddingLeft() - super.getPaddingRight())/mMaxCellsCount);
        final int nMaxCellHeight = Math.min(mCellHeight, (height - super.getPaddingTop() - super.getPaddingBottom())/mMaxRowsCount);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
        	
        	final View childView = getChildAt(i);
        	final ViewGroup.LayoutParams lp =
                (ViewGroup.LayoutParams) childView.getLayoutParams();
        	
        	int nChildMeasureWidth = 0;
        	if(lp.width > 0)
        		nChildMeasureWidth = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        	else //if(lp.width == ViewGroup.LayoutParams.MATCH_PARENT || lp.width == ViewGroup.LayoutParams.FILL_PARENT)
        		nChildMeasureWidth = MeasureSpec.makeMeasureSpec(nMaxCellWidth, MeasureSpec.EXACTLY);
        	//else
        	//	nChildMeasureWidth = MeasureSpec.makeMeasureSpec(childView.getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        	
        	int nChildMeasureHeight = 0;
        	if(lp.height > 0)
        		nChildMeasureHeight = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        	else //if(lp.height == ViewGroup.LayoutParams.MATCH_PARENT || lp.height == ViewGroup.LayoutParams.FILL_PARENT)
        		nChildMeasureHeight = MeasureSpec.makeMeasureSpec(nMaxCellHeight, MeasureSpec.EXACTLY);
        	//else
        	//	nChildMeasureHeight = MeasureSpec.makeMeasureSpec(childView.getMeasuredHeight(), MeasureSpec.UNSPECIFIED);
        	//android.util.Log.d("QsLog", "onMeasure==i:"+i+"=width:"+childView.getMeasuredWidth()+"==height:"+childView.getMeasuredHeight()+"=lpw:"+lp.width+"=lph:"+lp.height);

        	childView.measure(nChildMeasureWidth, nChildMeasureHeight);
        }
        
//        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED){
//        	setMeasuredDimension(mMaxCellsCount * mCellWidth, mMaxRowsCount * mCellHeight);
//        }
        
        //setMeasuredDimension(width, height);
    }
    
    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            view.buildDrawingCache(true);
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }
}
