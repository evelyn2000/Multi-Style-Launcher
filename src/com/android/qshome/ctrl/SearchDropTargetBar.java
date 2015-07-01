/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.android.qshome.Launcher;
import com.android.qshome.R;
import com.android.qshome.util.QsLog;

/*
 * Ths bar will manage the transition between the QSB search bar and the delete drop
 * targets so that each of the individual IconDropTargets don't have to.
 */
public class SearchDropTargetBar extends FrameLayout implements DragController.DragListener {

    private static final int sTransitionInDuration = 200;
    private static final int sTransitionOutDuration = 175;

    private AnimationSet mDropTargetBarFadeInAnim;
    private AnimationSet mDropTargetBarFadeOutAnim;
    private Animation mQSBSearchBarFadeInAnim;
    private Animation mQSBSearchBarFadeOutAnim;
    

    private boolean mIsSearchBarHidden;
    private View mQSBSearchBar;
    private View mDropTargetBar;
    private ButtonDropTarget mInfoDropTarget;
    private ButtonDropTarget mDeleteDropTarget;
    private int mBarHeight;
    private boolean mDeferOnDragEnd = false;
    private boolean mIsDraging = false;

    private Drawable mPreviousBackground;

    public SearchDropTargetBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchDropTargetBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setup(Launcher launcher, DragController dragController) {

        dragController.addDragListener(this);
        dragController.addDragListener(mInfoDropTarget);
        dragController.addDragListener(mDeleteDropTarget);
    	mInfoDropTarget.setDragController(dragController);
    	mDeleteDropTarget.setDragController(dragController);
    	
        dragController.addDropTarget(mInfoDropTarget);
        dragController.addDropTarget(mDeleteDropTarget);
        mInfoDropTarget.setLauncher(launcher);
        mDeleteDropTarget.setLauncher(launcher);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the individual components
        mQSBSearchBar = findViewById(R.id.qsb_search_bar);
        mDropTargetBar = findViewById(R.id.drag_target_bar);
        mInfoDropTarget = (ButtonDropTarget) mDropTargetBar.findViewById(R.id.info_target_text);
        mDeleteDropTarget = (ButtonDropTarget) mDropTargetBar.findViewById(R.id.delete_target_text);
        
        mBarHeight = getResources().getDimensionPixelSize(R.dimen.qsb_bar_height);

        mInfoDropTarget.setSearchDropTargetBar(this);
        mDeleteDropTarget.setSearchDropTargetBar(this);

//        boolean enableDropDownDropTargets =
//            getResources().getBoolean(R.bool.config_useDropTargetDownTransition);

        // Create the various fade animations
//        mDropTargetBar.setAlpha(0f);
//        ObjectAnimator fadeInAlphaAnim = ObjectAnimator.ofFloat(mDropTargetBar, "alpha", 1f);
//        fadeInAlphaAnim.setInterpolator(new DecelerateInterpolator());
//        mDropTargetBarFadeInAnim = new AnimationSet();
//        AnimationSet.Builder fadeInAnimators = mDropTargetBarFadeInAnim.play(fadeInAlphaAnim);
//        if (enableDropDownDropTargets) {
//            mDropTargetBar.setTranslationY(-mBarHeight);
//            fadeInAnimators.with(ObjectAnimator.ofFloat(mDropTargetBar, "translationY", 0f));
//        }
//        mDropTargetBarFadeInAnim.setDuration(sTransitionInDuration);
//        mDropTargetBarFadeInAnim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mDropTargetBar.setVisibility(View.VISIBLE);
//            }
//        });
//        ObjectAnimator fadeOutAlphaAnim = ObjectAnimator.ofFloat(mDropTargetBar, "alpha", 0f);
//        fadeOutAlphaAnim.setInterpolator(new AccelerateInterpolator());
//        mDropTargetBarFadeOutAnim = new AnimatorSet();
//        AnimatorSet.Builder fadeOutAnimators = mDropTargetBarFadeOutAnim.play(fadeOutAlphaAnim);
//        if (enableDropDownDropTargets) {
//            fadeOutAnimators.with(ObjectAnimator.ofFloat(mDropTargetBar, "translationY",
//                    -mBarHeight));
//        }
//        mDropTargetBarFadeOutAnim.setDuration(sTransitionOutDuration);
//        mDropTargetBarFadeOutAnim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mDropTargetBar.setVisibility(View.INVISIBLE);
//                mDropTargetBar.setLayerType(View.LAYER_TYPE_NONE, null);
//            }
//        });
//        mQSBSearchBarFadeInAnim = ObjectAnimator.ofFloat(mQSBSearchBar, "alpha", 1f);
//        mQSBSearchBarFadeInAnim.setDuration(sTransitionInDuration);
//        mQSBSearchBarFadeInAnim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mQSBSearchBar.setVisibility(View.VISIBLE);
//            }
//        });
//        mQSBSearchBarFadeOutAnim = ObjectAnimator.ofFloat(mQSBSearchBar, "alpha", 0f);
//        mQSBSearchBarFadeOutAnim.setDuration(sTransitionOutDuration);
//        mQSBSearchBarFadeOutAnim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mQSBSearchBar.setVisibility(View.INVISIBLE);
//            }
//        });
    }
    
    private void createAnimations() {
        if (mDropTargetBarFadeInAnim == null) {
        	mDropTargetBarFadeInAnim = new FastAnimationSet();
            final AnimationSet animationSet = mDropTargetBarFadeInAnim;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
            //if (mOrientation == DeleteZone.ORIENTATION_HORIZONTAL) {
            if(false){
                animationSet.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f));
            } else {
                animationSet.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                        1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(getTransitionInDuration());
            animationSet.setAnimationListener(new AnimationListener(){
            	@Override
            	public void onAnimationStart(Animation animation){
            		mDropTargetBar.setVisibility(View.VISIBLE);
            		//QsLog.LogD("SearchDropTargetBar::mDropTargetBarFadeInAnim::onAnimationStart()===");
            	}
            	@Override
            	public void onAnimationEnd(Animation animation){
            		//QsLog.LogD("SearchDropTargetBar::mDropTargetBarFadeInAnim::onAnimationEnd()===");
                }
            	@Override
            	public void onAnimationRepeat(Animation animation){
                	
                }
            });
        }
        if (mQSBSearchBarFadeInAnim == null) {
        	mQSBSearchBarFadeInAnim = new AlphaAnimation(0.0f, 1.0f);
        	mQSBSearchBarFadeInAnim.setDuration(getTransitionInDuration());
        	mQSBSearchBarFadeInAnim.setAnimationListener(new AnimationListener(){
            	@Override
            	public void onAnimationStart(Animation animation){
            		mQSBSearchBar.setVisibility(View.VISIBLE);
            		//QsLog.LogD("SearchDropTargetBar::mQSBSearchBarFadeInAnim::onAnimationStart()===");
            	}
            	@Override
            	public void onAnimationEnd(Animation animation){
            		//QsLog.LogD("SearchDropTargetBar::mQSBSearchBarFadeInAnim::onAnimationEnd()===");
                }
            	@Override
            	public void onAnimationRepeat(Animation animation){
                	
                }
            });
        }
        if (mDropTargetBarFadeOutAnim == null) {
        	mDropTargetBarFadeOutAnim = new FastAnimationSet();
            final AnimationSet animationSet = mDropTargetBarFadeOutAnim;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
            //if (mOrientation == DeleteZone.ORIENTATION_HORIZONTAL) {
            if(false){
                animationSet.addAnimation(new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f));
            } else {
                animationSet.addAnimation(new FastTranslateAnimation(Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(getTransitionOutDuration());
            
            animationSet.setAnimationListener(new AnimationListener(){
            	@Override
            	public void onAnimationStart(Animation animation){
            		//QsLog.LogD("SearchDropTargetBar::mDropTargetBarFadeOutAnim::onAnimationStart()===");
            	}
            	@Override
            	public void onAnimationEnd(Animation animation){
            		mDropTargetBar.setVisibility(View.INVISIBLE);
            		//QsLog.LogD("SearchDropTargetBar::mDropTargetBarFadeOutAnim::onAnimationEnd()===");
                }
            	@Override
            	public void onAnimationRepeat(Animation animation){
                	
                }
            });
        }
        if (mQSBSearchBarFadeOutAnim == null) {
        	mQSBSearchBarFadeOutAnim = new AlphaAnimation(1.0f, 0.0f);
        	mQSBSearchBarFadeOutAnim.setFillAfter(true);
        	mQSBSearchBarFadeOutAnim.setDuration(getTransitionOutDuration());
        	
        	mQSBSearchBarFadeOutAnim.setAnimationListener(new AnimationListener(){
            	@Override
            	public void onAnimationStart(Animation animation){
            		//QsLog.LogD("SearchDropTargetBar::mQSBSearchBarFadeOutAnim::onAnimationStart()===");
            	}
            	@Override
            	public void onAnimationEnd(Animation animation){
            		mQSBSearchBar.setVisibility(View.INVISIBLE);
            		//QsLog.LogD("SearchDropTargetBar::mQSBSearchBarFadeOutAnim::onAnimationEnd()===");
                }
            	@Override
            	public void onAnimationRepeat(Animation animation){
                	
                }
            });
        }
    }

    private void cancelAnimations() {
    	if(mDropTargetBarFadeInAnim != null){
	        mDropTargetBarFadeInAnim.cancel();
	        mDropTargetBarFadeOutAnim.cancel();
	        mQSBSearchBarFadeInAnim.cancel();
	        mQSBSearchBarFadeOutAnim.cancel();
    	}else{
    		createAnimations();
    	}
    }

    /*
     * Shows and hides the search bar.
     */
    public void showSearchBar(boolean animated) {
        cancelAnimations();
        //QsLog.LogD("SearchDropTargetBar::showSearchBar()===animated:"+animated);
        if (animated) {
            //mQSBSearchBarFadeInAnim.start();
        	mQSBSearchBar.startAnimation(mQSBSearchBarFadeInAnim);
        } else {
            mQSBSearchBar.setVisibility(View.VISIBLE);
            //mQSBSearchBar.setAlpha(1f);
        }
        mIsSearchBarHidden = false;
    }
    public void hideSearchBar(boolean animated) {
        cancelAnimations();
        //QsLog.LogD("SearchDropTargetBar::hideSearchBar()===animated:"+animated);
        if (animated) {
            //mQSBSearchBarFadeOutAnim.start();
        	mQSBSearchBar.startAnimation(mQSBSearchBarFadeOutAnim);
        } else {
            mQSBSearchBar.setVisibility(View.INVISIBLE);
            //mQSBSearchBar.setAlpha(0f);
        }
        mIsSearchBarHidden = true;
    }

    /*
     * Gets various transition durations.
     */
    public int getTransitionInDuration() {
        return sTransitionInDuration;
    }
    public int getTransitionOutDuration() {
        return sTransitionOutDuration;
    }

    /*
     * DragController.DragListener implementation
     */
    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        // Animate out the QSB search bar, and animate in the drop target bar
    	
    	if (info != null) {
    		mIsDraging = true;
    		createAnimations();
	        //mDropTargetBar.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	        //mDropTargetBar.buildLayer();
    		
	        mDropTargetBarFadeOutAnim.cancel();
	        mDropTargetBar.startAnimation(mDropTargetBarFadeInAnim);
	        //mDropTargetBarFadeInAnim.start();
	        //if (!mIsSearchBarHidden) {
	            mQSBSearchBarFadeInAnim.cancel();
	            mQSBSearchBar.startAnimation(mQSBSearchBarFadeOutAnim);
	            //mQSBSearchBarFadeOutAnim.start();
	        //}
    	}
    }

    public void deferOnDragEnd() {
    	//QsLog.LogE("SearchDropTargetBar::deferOnDragEnd()====mDeferOnDragEnd:"+mDeferOnDragEnd+"==mIsDraging:"+mIsDraging+"==mIsSearchBarHidden:"+mIsSearchBarHidden);
        //mDeferOnDragEnd = true;
    }

    @Override
    public void onDragEnd() {
    	//QsLog.LogE("SearchDropTargetBar::onDragEnd()====mDeferOnDragEnd:"+mDeferOnDragEnd+"==mIsDraging:"+mIsDraging+"==mIsSearchBarHidden:"+mIsSearchBarHidden);
        if (!mDeferOnDragEnd && mIsDraging) {
            // Restore the QSB search bar, and animate out the drop target bar
            mDropTargetBarFadeInAnim.cancel();
            mDropTargetBar.startAnimation(mDropTargetBarFadeOutAnim);
            //mDropTargetBarFadeOutAnim.start();
            //if (mIsSearchBarHidden) {
                mQSBSearchBarFadeOutAnim.cancel();
                mQSBSearchBar.startAnimation(mQSBSearchBarFadeInAnim);
                //mQSBSearchBarFadeInAnim.start();
            //}
        } else {
            mDeferOnDragEnd = false;
        }
        
        mIsDraging = false;
    }

    public void onSearchPackagesChanged(boolean searchVisible, boolean voiceVisible) {
        if (mQSBSearchBar != null) {
            Drawable bg = mQSBSearchBar.getBackground();
            if (bg != null && (!searchVisible && !voiceVisible)) {
                // Save the background and disable it
                mPreviousBackground = bg;
                mQSBSearchBar.setBackgroundResource(0);
            } else if (mPreviousBackground != null && (searchVisible || voiceVisible)) {
                // Restore the background
                mQSBSearchBar.setBackgroundDrawable(mPreviousBackground);
            }
        }
    }

    public Rect getSearchBarBounds() {
        if (mQSBSearchBar != null) {
            final float appScale = mQSBSearchBar.getContext().getResources()
                    .getCompatibilityInfo().applicationScale;
            final int[] pos = new int[2];
            mQSBSearchBar.getLocationOnScreen(pos);

            final Rect rect = new Rect();
            rect.left = (int) (pos[0] * appScale + 0.5f);
            rect.top = (int) (pos[1] * appScale + 0.5f);
            rect.right = (int) ((pos[0] + mQSBSearchBar.getWidth()) * appScale + 0.5f);
            rect.bottom = (int) ((pos[1] + mQSBSearchBar.getHeight()) * appScale + 0.5f);
            return rect;
        } else {
            return null;
        }
    }
    
    private static class FastTranslateAnimation extends TranslateAnimation {
        public FastTranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
                int fromYType, float fromYValue, int toYType, float toYValue) {
            super(fromXType, fromXValue, toXType, toXValue,
                    fromYType, fromYValue, toYType, toYValue);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }

    private static class FastAnimationSet extends AnimationSet {
        FastAnimationSet() {
            super(false);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }
}
