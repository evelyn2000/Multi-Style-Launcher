package com.android.qshome.ctrl;

import com.android.qshome.model.IconCache;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.util.QsLog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import android.graphics.Region;
import android.graphics.Region.Op;

import com.android.qshome.R;

public class BubbleTextViewIcs extends TextView {
	static final float CORNER_RADIUS = 4.0f;
	static final float PADDING_H = 8.0f;
    static final float PADDING_V = 3.0f;
    
    static final float SHADOW_LARGE_RADIUS = 4.0f;
    static final float SHADOW_SMALL_RADIUS = 1.75f;
    static final float SHADOW_Y_OFFSET = 2.0f;
    static final int SHADOW_LARGE_COLOUR = 0xDD000000;
    static final int SHADOW_SMALL_COLOUR = 0xCC000000;
    private int mPrevAlpha = -1;
    private float mBubbleColorAlpha;

//    private final Rect mTempRect = new Rect();
//    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
//    private final Canvas mTempCanvas = new Canvas();
//    
//    private boolean mDidInvalidateForPressedState;
//    //private Bitmap mPressedOrFocusedBackground;
//    private int mFocusedOutlineColor;
//    private int mFocusedGlowColor;
//    private int mPressedOutlineColor;
//    private int mPressedGlowColor;
//    
//    private boolean mStayPressed;
    
    private boolean mBackgroundSizeChanged;
    private Drawable mBackground;
    protected float mCornerRadius;
    protected float mPaddingH;
    protected float mPaddingV;
    protected Paint mPaint;
    
	public BubbleTextViewIcs(Context context) {
        this(context, null);
    }

    public BubbleTextViewIcs(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleTextViewIcs(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    protected void init() {
        setFocusable(true);
        mBackground = getBackground();

        final Resources res = getContext().getResources();
        int bubbleColor = res.getColor(R.color.bubble_dark_background);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(bubbleColor);
        mBubbleColorAlpha = Color.alpha(bubbleColor) / 255.0f;
//        mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor =
//            res.getColor(R.color.holo_blue_light);

        setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
        
        final float scale = res.getDisplayMetrics().density;
        mCornerRadius = CORNER_RADIUS * scale;
        mPaddingH = PADDING_H * scale;
        //noinspection PointlessArithmeticExpression
        mPaddingV = PADDING_V * scale;
    }
    
    @Override
    protected boolean setFrame(int left, int top, int right, int bottom) {
    	if(super.setFrame(left, top, right, bottom)){
        	mBackgroundSizeChanged = true;
        	return true;
        }
        
        return false;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mBackground || super.verifyDrawable(who);
    }
    
//    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache) {
//        Bitmap b = info.getIcon(iconCache);
//
//        setCompoundDrawablesWithIntrinsicBounds(null,
//                new FastBitmapDrawable(b),
//                null, null);
//        setText(info.title);
//        setTag(info);
//    }
    
    @Override
    protected void drawableStateChanged() {
//    	QsLog.LogD("BubbleTextViewIcs::drawableStateChanged(0)=====isPressed:"+isPressed()+"==mStayPressed:"+mStayPressed);
//    	if (isPressed()) {
//            // In this case, we have already created the pressed outline on ACTION_DOWN,
//            // so we just need to do an invalidate to trigger draw
//            if (!mDidInvalidateForPressedState) {
//                setCellLayoutPressedOrFocusedIcon();
//            }
//        } else {
//            // Otherwise, either clear the pressed/focused background, or create a background
//            // for the focused state
//            final boolean backgroundEmptyBefore = mPressedOrFocusedBackground == null;
//            if (!mStayPressed) {
//                mPressedOrFocusedBackground = null;
//            }
//            if (isFocused()) {
//                if (getLayout() == null) {
//                    // In some cases, we get focus before we have been layed out. Set the
//                    // background to null so that it will get created when the view is drawn.
//                    mPressedOrFocusedBackground = null;
//                } else {
//                    mPressedOrFocusedBackground = createGlowingOutline(
//                            mTempCanvas, mFocusedGlowColor, mFocusedOutlineColor);
//                }
//                mStayPressed = false;
//                setCellLayoutPressedOrFocusedIcon();
//            }
//            final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
//            if (!backgroundEmptyBefore && backgroundEmptyNow) {
//                setCellLayoutPressedOrFocusedIcon();
//            }
//        }
    	
    	Drawable d = mBackground;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }
    
    /**
     * Draw this BubbleTextView into the given Canvas.
     *
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
//    private void drawWithPadding(Canvas destCanvas, int padding) {
//        final Rect clipRect = mTempRect;
//        getDrawingRect(clipRect);
//
//        // adjust the clip rect so that we don't include the text label
//        clipRect.bottom =
//            getExtendedPaddingTop() - (int)mPaddingV + getLayout().getLineTop(0);
//
//        // Draw the View into the bitmap.
//        // The translate of scrollX and scrollY is necessary when drawing TextViews, because
//        // they set scrollX and scrollY to large values to achieve centered text
//        destCanvas.save();
//        destCanvas.translate(-getScrollX() + padding / 2, -getScrollY() + padding / 2);
//        destCanvas.clipRect(clipRect, Op.REPLACE);
//        draw(destCanvas);
//        destCanvas.restore();
//    }
//
//    /**
//     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
//     * Responsibility for the bitmap is transferred to the caller.
//     */
//    private Bitmap createGlowingOutline(Canvas canvas, int outlineColor, int glowColor) {
//        final int padding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
//        final Bitmap b = Bitmap.createBitmap(
//                getWidth() + padding, getHeight() + padding, Bitmap.Config.ARGB_8888);
//
//        canvas.setBitmap(b);
//        drawWithPadding(canvas, padding);
//        mOutlineHelper.applyExtraThickExpensiveOutlineWithBlur(b, canvas, glowColor, outlineColor);
//        //canvas.setBitmap(null);
//
//        return b;
//    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // Call the superclass onTouchEvent first, because sometimes it changes the state to
//        // isPressed() on an ACTION_UP
//        boolean result = super.onTouchEvent(event);
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                // So that the pressed outline is visible immediately when isPressed() is true,
//                // we pre-create it on ACTION_DOWN (it takes a small but perceptible amount of time
//                // to create it)
//            	QsLog.LogD("BubbleTextViewIcs::onTouchEvent(ACTION_DOWN)=====isPressed:"+isPressed()+"==");
//                if (mPressedOrFocusedBackground == null) {
//                    mPressedOrFocusedBackground = createGlowingOutline(
//                            mTempCanvas, mPressedGlowColor, mPressedOutlineColor);
//                }
//                // Invalidate so the pressed state is visible, or set a flag so we know that we
//                // have to call invalidate as soon as the state is "pressed"
//                if (isPressed()) {
//                    mDidInvalidateForPressedState = true;
//                    setCellLayoutPressedOrFocusedIcon();
//                } else {
//                    mDidInvalidateForPressedState = false;
//                }
//                break;
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                // If we've touched down and up on an item, and it's still not "pressed", then
//                // destroy the pressed outline
//                if (!isPressed()) {
//                    mPressedOrFocusedBackground = null;
//                }
//                break;
//        }
//        return result;
//    }

//    public void setStayPressed(boolean stayPressed) {
//        mStayPressed = stayPressed;
//        if (!stayPressed) {
//            mPressedOrFocusedBackground = null;
//        }
//        setCellLayoutPressedOrFocusedIcon();
//    }
//
//    public void setCellLayoutPressedOrFocusedIcon() {
//        if (getParent() instanceof CellLayoutIcs) {
//        	CellLayoutIcs parent = (CellLayoutIcs) getParent();
//            if (parent != null) {
//            	parent.setPressedOrFocusedIcon((mPressedOrFocusedBackground != null) ? this : null);
//            }
//        }
//    }
//
//    public void clearPressedOrFocusedBackground() {
//        mPressedOrFocusedBackground = null;
//        setCellLayoutPressedOrFocusedIcon();
//    }
//
//    public Bitmap getPressedOrFocusedBackground() {
//        return mPressedOrFocusedBackground;
//    }

//    public int getPressedOrFocusedBackgroundPadding() {
//        return HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS / 2;
//    }
    
    @Override
    public void draw(Canvas canvas) {
    	final Drawable background = mBackground;
        if (background != null) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();

            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0,  getRight() - getLeft(), getBottom() - getTop());
                mBackgroundSizeChanged = false;
            }

            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                background.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
        // We enhance the shadow by drawing the shadow twice
        getPaint().setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
        super.draw(canvas);
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(), getScrollX() + getWidth(),
                getScrollY() + getHeight(), Region.Op.INTERSECT);
        getPaint().setShadowLayer(SHADOW_SMALL_RADIUS, 0.0f, 0.0f, SHADOW_SMALL_COLOUR);
        super.draw(canvas);
        canvas.restore();
    }
//    @Override
//    protected void onDrawCustom(Canvas canvas){
//    	
//    }
    
	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mBackground != null) mBackground.setCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBackground != null) mBackground.setCallback(null);
    }
    
    @Override
    protected boolean onSetAlpha(int alpha) {
        if (mPrevAlpha != alpha) {
            mPrevAlpha = alpha;
            mPaint.setAlpha((int) (alpha * mBubbleColorAlpha));
            super.onSetAlpha(alpha);
        }
        return true;
    }
    
    public float getPaddingV(){
    	return mPaddingV;
    }
    
}
