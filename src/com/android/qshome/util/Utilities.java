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

package com.android.qshome.util;

import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.TableMaskFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;

import com.android.qshome.R;
import com.android.qshome.style.BaseStyleObject;


/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {

	public final static boolean QS_ENABLE_WORKSPACE_5_5 = false;
	public final static boolean QS_ENABLE_APP_SMALL_ICON = false;
    private static final String TAG = "Launcher.Utilities";

    private static final boolean TEXT_BURN = false;

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    private static int sIconTextureWidth = -1;
    private static int sIconTextureHeight = -1;
    
    // for some icon with bound...
//    private static int sMaskWidth = -1;
//    private static int sMaskHeight = -1;
//    private static int sFolderIconIconWidth = -1;
//    private static int sFolderIconIconHeight = -1;
//    private static int sFolderIconIconLeftPadding = -1;
//    private static int sFolderIconIconTopPadding = -1;
//    private static int sFolderIconIconRowMargin = -1;
//    private static int sFolderIconIconColumnMargin = -1;
    // end
    

    private static final Paint sPaint = new Paint();
    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
    private static final Rect sBounds = new Rect();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    
    private static ThemeStyle sCurrentThemeStyle = ThemeStyle.Unkown;
    public static void changeThemeStyle(Context context, ThemeStyle style, boolean reInit){
    	if(style != sCurrentThemeStyle){
    		sCurrentThemeStyle = style;
    		
    		if(reInit)
    			initStatics(context, style);
    	}
    }

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    public static Bitmap centerToFit(Bitmap bitmap, int width, int height, Context context) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        if (bitmapWidth < width || bitmapHeight < height) {
            int color = context.getResources().getColor(R.color.window_background);

            Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width : bitmapWidth,
                    bitmapHeight < height ? height : bitmapHeight, Bitmap.Config.RGB_565);
            centered.setDensity(bitmap.getDensity());
            Canvas canvas = new Canvas(centered);
            canvas.drawColor(color);
            canvas.drawBitmap(bitmap, (width - bitmapWidth) / 2.0f, (height - bitmapHeight) / 2.0f,
                    null);

            bitmap = centered;
        }

        return bitmap;
    }

    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    public static Bitmap createCompoundBitmap(Bitmap icon, Bitmap bg, Context context) {
    	synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
	    		    	
	    	final int bgWidth = sIconTextureWidth;
	    	final int bgHeight = sIconTextureHeight;
	    	int width = sIconWidth;
			int height = sIconHeight;
			
			int sourceWidth = icon.getWidth();
            int sourceHeight = icon.getHeight();

            if (sourceWidth > 0 && sourceWidth > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // It's small, use the size they gave us.
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }
	
			Bitmap compoundBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
			final Canvas canvas = sCanvas;
	        canvas.setBitmap(compoundBitmap);
	        canvas.drawBitmap(bg, 0, 0, null);
			canvas.drawBitmap(icon, (bgWidth - width) / 2, (bgHeight - height) / 2, null);
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			
			return compoundBitmap;
    	}
    }
    
    public static Bitmap scaleBitmap(Drawable icon, Context context){
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}
			
			BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
			return scaleBitmap(bitmapDrawable.getBitmap(), context);
		}
    }
    
    public static Bitmap scaleBitmap(Bitmap bm, Context context){
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}
			return scaleBitmap(bm, sIconTextureWidth, sIconTextureHeight);
//			if (bm.getWidth() == sMaskWidth
//					&& bm.getHeight() == sMaskHeight) {
//				return bm;
//			} 
//			
//			return Bitmap.createScaledBitmap(bm, sMaskWidth, sMaskHeight, true); 
		}
    }
    
    public static Bitmap scaleBitmap(Bitmap bm, int w, int h){
    	if (bm.getWidth() == w
				&& bm.getHeight() == w) {
			return bm;
		} 
		
		return Bitmap.createScaledBitmap(bm, w, h, true); 
    }
    
    public final static int ADD_ICON_ORG_LEFT = 1;
    public final static int ADD_ICON_ORG_RIGHT = 2;
    public final static int ADD_ICON_ORG_TOP = 4;
    public final static int ADD_ICON_ORG_BOTTOM = 8;
    public static Bitmap addIconToBackground(Bitmap icon, Bitmap bg, int nOrg, Context context){
    	synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}
			
			final int bgWidth = bg.getWidth();
	    	final int bgHeight = bg.getHeight();
	    	final int iconWidth = icon.getWidth();
			final int iconHeight = icon.getHeight();
			int x = 0;
			int y = 0;
			
			if((nOrg & ADD_ICON_ORG_RIGHT) > 0){
				x = bgWidth - iconWidth;
			}
			else if((nOrg & ADD_ICON_ORG_LEFT) > 0){
				x = 0;
			}
			else{ // center
				x = (bgWidth - iconWidth)/2;
			}
			
			if((nOrg & ADD_ICON_ORG_TOP) > 0){
				y = 0;
			}
			else if((nOrg & ADD_ICON_ORG_BOTTOM) > 0){
				y = bgHeight - iconHeight;
			}
			else{
				y = (bgHeight - iconHeight)/2;
			}
			
			Bitmap compoundBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
			final Canvas canvas = sCanvas;
	        canvas.setBitmap(compoundBitmap);
	        canvas.drawBitmap(bg, 0, 0, null);
			canvas.drawBitmap(icon, x, y, null);
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			
			return compoundBitmap;
		}
    }
    
    public static Bitmap createIconBitmap(Drawable icon, Bitmap bg, Context context){
    	synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;
            if(bg == null)
            {
                width = sIconTextureWidth;
                height = sIconTextureHeight;
            }
            
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();

            if (sourceWidth > 0 && sourceWidth > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // It's small, use the size they gave us.
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);
            
            if(bg != null)
            {
            	bg = scaleBitmap(bg, textureWidth, textureHeight);
            	canvas.drawBitmap(bg, 0, 0, null);
            }

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            if (false) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);

            return bitmap;
        }
    }
    
    
    /**
     * Returns a bitmap suitable for the all apps view.  The bitmap will be a power
     * of two sized ARGB_8888 bitmap that can be used as a gl texture.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;
            
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();

            if (sourceWidth > 0 && sourceWidth > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // It's small, use the size they gave us.
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            if (false) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);

            return bitmap;
        }
    }

    public static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth, int destHeight,
            boolean pressed, Bitmap src) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                // We can't have gotten to here without src being initialized, which
                // comes from this file already.  So just assert.
                //initStatics(context);
                throw new RuntimeException("Assertion failed: Utilities not initialized");
            }

            dest.drawColor(0, PorterDuff.Mode.CLEAR);

            int[] xy = new int[2];
            Bitmap mask = src.extractAlpha(sBlurPaint, xy);

            float px = (destWidth - src.getWidth()) / 2;
            float py = (destHeight - src.getHeight()) / 2;
            dest.drawBitmap(mask, px + xy[0], py + xy[1],
                    pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

            mask.recycle();
        }
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    public static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                return createIconBitmap(new BitmapDrawable(bitmap), context);
            }
        }
    }

    public static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(disabled);
            
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

            return disabled;
        }
    }

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;
        
        if(QS_ENABLE_APP_SMALL_ICON)
        	sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size_small);
        else
        	sIconWidth = sIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth + (int)(density*4);
        
//        sMaskWidth = sMaskHeight = sIconTextureWidth;//(int) resources.getDimensionPixelSize(R.dimen.iphone_mask_size);
//        sFolderIconIconWidth = (int) resources
//	        	.getDimensionPixelSize(R.dimen.iphone_folder_icon_icon_width);
//	
//		sFolderIconIconHeight = (int) resources
//		        .getDimensionPixelSize(R.dimen.iphone_folder_icon_icon_height);
//		
//		sFolderIconIconLeftPadding = (int) resources
//		        .getDimensionPixelSize(R.dimen.iphone_folder_icon_left_padding);
//		
//		sFolderIconIconTopPadding = (int) resources
//		        .getDimensionPixelSize(R.dimen.iphone_folder_icon_top_padding);
//		
//		sFolderIconIconRowMargin = (int) resources
//		        .getDimensionPixelSize(R.dimen.iphone_folder_icon_row_margin);
//		
//		sFolderIconIconColumnMargin = (int) resources
//		        .getDimensionPixelSize(R.dimen.iphone_folder_icon_column_margin);
//		
//        QsLog.LogD("initStatics(0)======sIconWidth:"+sIconWidth+"==sMaskWidth:"+sMaskWidth);

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        sGlowColorFocusedPaint.setColor(0xffff8e00);
        sGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }
    
    public static void initStatics(Context context, ThemeStyle style) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;
        
        if(QS_ENABLE_APP_SMALL_ICON)
        	sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size_small);
        else
        	sIconWidth = sIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth + (int)(density*4);
        
//        if(style == BaseStyleObject.ThemeStyle.Iphone || BaseStyleObject.ThemeStyle.Samsung == style){
//	        sMaskWidth = sMaskHeight = (int) resources.getDimensionPixelSize(R.dimen.iphone_mask_size);
//	        sFolderIconIconWidth = (int) resources
//		        	.getDimensionPixelSize(R.dimen.iphone_folder_icon_icon_width);
//		
//			sFolderIconIconHeight = (int) resources
//			        .getDimensionPixelSize(R.dimen.iphone_folder_icon_icon_height);
//			
//			sFolderIconIconLeftPadding = (int) resources
//			        .getDimensionPixelSize(R.dimen.iphone_folder_icon_left_padding);
//			
//			sFolderIconIconTopPadding = (int) resources
//			        .getDimensionPixelSize(R.dimen.iphone_folder_icon_top_padding);
//			
//			sFolderIconIconRowMargin = (int) resources
//			        .getDimensionPixelSize(R.dimen.iphone_folder_icon_row_margin);
//			
//			sFolderIconIconColumnMargin = (int) resources
//			        .getDimensionPixelSize(R.dimen.iphone_folder_icon_column_margin);
//        }
//        QsLog.LogD("initStatics(1)======sIconWidth:"+sIconWidth+"==");

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        sGlowColorFocusedPaint.setColor(0xffff8e00);
        sGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }

    public static class BubbleText {
        private static int MAX_LINES = 1;

        private final TextPaint mTextPaint;

        private final RectF mBubbleRect = new RectF();

        private float mTextWidth;
        private int mLeading;
        private int mFirstLineY;
        private int mLineHeight;

        private int mBitmapWidth;
        private int mBitmapHeight;
        private int mDensity;
        
        public void initStyle(Context context, ThemeStyle style){
        	final Resources resources = context.getResources();
        	//final DisplayMetrics metrics = resources.getDisplayMetrics();
            final float scale = resources.getDisplayMetrics().density;
            
        	final float paddingLeft;// = 2.0f * scale;
            final float paddingRight;// = 2.0f * scale;
            final float cellWidth;// = resources.getDimension(R.dimen.title_texture_width);
            
//        	if(style == ThemeStyle.DroidIcs){
//        		MAX_LINES = 2;
//        		paddingLeft = 2.0f * scale;
//        		paddingRight = 2.0f * scale;
//        		cellWidth = resources.getDimension(R.dimen.ics_title_texture_width);
//        		
//        	}else{
        		MAX_LINES = 1;
        		
        		paddingLeft = 2.0f * scale;
        		paddingRight = 2.0f * scale;
        		cellWidth = resources.getDimension(R.dimen.title_texture_width);
        	//}


            RectF bubbleRect = mBubbleRect;
            bubbleRect.left = 0;
            bubbleRect.top = 0;
            bubbleRect.right = (int) cellWidth;

            mTextWidth = cellWidth - paddingLeft - paddingRight;

            TextPaint textPaint = mTextPaint;
            

            float ascent = -textPaint.ascent();
            float descent = textPaint.descent();
            float leading = 0.0f;//(ascent+descent) * 0.1f;
            mLeading = (int)(leading + 0.5f);
            mFirstLineY = (int)(leading + ascent + 0.5f);
            mLineHeight = (int)(leading + ascent + descent + 0.5f);

            mBitmapWidth = (int)(mBubbleRect.width() + 0.5f);
            mBitmapHeight = roundToPow2((int)((MAX_LINES * mLineHeight) + leading + 0.5f));

            mBubbleRect.offsetTo((mBitmapWidth-mBubbleRect.width())/2, 0);
        }
        
        public BubbleText(Context context, ThemeStyle style){
        	final Resources resources = context.getResources();
        	final DisplayMetrics metrics = resources.getDisplayMetrics();
            final float scale = metrics.density;
            mDensity = metrics.densityDpi;
            
        	mTextPaint = new TextPaint();
        	TextPaint textPaint = mTextPaint;
            textPaint.setTypeface(Typeface.DEFAULT);
            if(QS_ENABLE_APP_SMALL_ICON)
            	textPaint.setTextSize(10*scale);
            else
            	textPaint.setTextSize(13*scale);
            textPaint.setColor(0xffffffff);
            textPaint.setAntiAlias(true);
            if (TEXT_BURN) {
                textPaint.setShadowLayer(8, 0, 0, 0xff000000);
            }
            
            initStyle(context, style);
        }

        public BubbleText(Context context) {
        	
            
        	this(context, ThemeStyle.Default);
        	
//            if (false) {
//                Log.d(TAG, "mBitmapWidth=" + mBitmapWidth + " mBitmapHeight="
//                        + mBitmapHeight + " w=" + ((int)(mBubbleRect.width() + 0.5f))
//                        + " h=" + ((int)((MAX_LINES * mLineHeight) + leading + 0.5f)));
//            }
        }

        /** You own the bitmap after this and you must call recycle on it. */
        public Bitmap createTextBitmap(String text) {
            Bitmap b = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ALPHA_8);
            b.setDensity(mDensity);
            Canvas c = new Canvas(b);

            StaticLayout layout = new StaticLayout(text, mTextPaint, (int)mTextWidth,
                    Alignment.ALIGN_CENTER, 1, 0, true);
            int lineCount = layout.getLineCount();
            if (lineCount > MAX_LINES) {
                lineCount = MAX_LINES;
            }
            //if (!TEXT_BURN && lineCount > 0) {
                //RectF bubbleRect = mBubbleRect;
                //bubbleRect.bottom = height(lineCount);
                //c.drawRoundRect(bubbleRect, mCornerRadius, mCornerRadius, mRectPaint);
            //}
            for (int i=0; i<lineCount; i++) {
                //int x = (int)((mBubbleRect.width() - layout.getLineMax(i)) / 2.0f);
                //int y = mFirstLineY + (i * mLineHeight);
                final String lineText = text.substring(layout.getLineStart(i), layout.getLineEnd(i));
                int x = (int)(mBubbleRect.left
                        + ((mBubbleRect.width() - mTextPaint.measureText(lineText)) * 0.5f));
                int y = mFirstLineY + (i * mLineHeight);
                c.drawText(lineText, x, y, mTextPaint);
            }

            return b;
        }

        private int height(int lineCount) {
            return (int)((lineCount * mLineHeight) + mLeading + mLeading + 0.0f);
        }

        public int getBubbleWidth() {
            return (int)(mBubbleRect.width() + 0.5f);
        }

        public int getMaxBubbleHeight() {
            return height(MAX_LINES);
        }

        public int getBitmapWidth() {
            return mBitmapWidth;
        }

        public int getBitmapHeight() {
            return mBitmapHeight;
        }
    }

    /** Only works for positive numbers. */
    public static int roundToPow2(int n) {
        int orig = n;
        n >>= 1;
        int mask = 0x8000000;
        while (mask != 0 && (n & mask) == 0) {
            mask >>= 1;
        }
        while (mask != 0) {
            n |= mask;
            mask >>= 1;
        }
        n += 1;
        if (n != orig) {
            n <<= 1;
        }
        return n;
    }
    
 // Note: This doesn't do all the client-id magic that BrowserProvider does
    // in Browser. (http://b/2425179)
    public static Uri getDefaultBrowserUri(Context context) {
        String url = context.getString(R.string.default_browser_url);
        if (url.indexOf("{CID}") != -1) {
            url = url.replace("{CID}", "android-google");
        }
        return Uri.parse(url);
    }
    
    public static void sendActionForDial(Context context){
    	Intent intent = new Intent(Intent.ACTION_DIAL);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    public static void sendActionForContacts(Context context){
    	Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setType("vnd.android.cursor.dir/person");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    public static void sendActionForSms(Context context){
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.setType("vnd.android-dir/mms-sms");
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
