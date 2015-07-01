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

import android.graphics.Rect;

/**
 * Interface defining an object that can receive a drag.
 *
 */
public interface DropTarget {
	
	public class DragObject {
        public int x = -1;
        public int y = -1;

        /** X offset from the upper-left corner of the cell to where we touched.  */
        public int xOffset = -1;

        /** Y offset from the upper-left corner of the cell to where we touched.  */
        public int yOffset = -1;

        /** This indicates whether a drag is in final stages, either drop or cancel. It
         * differentiates onDragExit, since this is called when the drag is ending, above
         * the current drag target, or when the drag moves off the current drag object.
         */
        public boolean dragComplete = false;

        /** The view that moves around while you drag.  */
        public DragView dragView = null;

        /** The data associated with the object being dragged */
        public Object dragInfo = null;

        /** Where the drag originated */
        public DragSource dragSource = null;

        /** Post drag animation runnable */
        public Runnable postAnimationRunnable = null;

        /** Indicates that the drag operation was cancelled */
        public boolean cancelled = false;

        public DragObject() {
        }
    }
	/**
     * Used to temporarily disable certain drop targets
     *
     * @return boolean specifying whether this drop target is currently enabled
     */
	public boolean isDropEnabled();
    /**
     * Handle an object being dropped on the DropTarget
     * 
     * @param source DragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the original
     *          touch happened
     * @param yOffset Vertical offset with the object being dragged where the original
     *          touch happened
     * @param dragView The DragView that's being dragged around on screen.
     * @param dragInfo Data associated with the object being dragged
     * 
     */
	public boolean onDrop(DragObject dragObject);
    
	public void onDragEnter(DragObject dragObject);

	public void onDragOver(DragObject dragObject);

	public void onDragExit(DragObject dragObject);
	
	/**
     * Allows a DropTarget to delegate drag and drop events to another object.
     *
     * Most subclasses will should just return null from this method.
     *
     * @param source DragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the original
     *          touch happened
     * @param yOffset Vertical offset with the object being dragged where the original
     *          touch happened
     * @param dragView The DragView that's being dragged around on screen.
     * @param dragInfo Data associated with the object being dragged
     *
     * @return The DropTarget to delegate to, or null to not delegate to another object.
     */
	public DropTarget getDropTargetDelegate(DragObject dragObject);
	
//	public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
//            DragView dragView, Object dragInfo);
//    
//	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
//            DragView dragView, Object dragInfo);
//
//	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
//            DragView dragView, Object dragInfo);
//
//	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
//            DragView dragView, Object dragInfo);

    /**
     * Check if a drop action can occur at, or near, the requested location.
     * This may be called repeatedly during a drag, so any calls should return
     * quickly.
     * 
     * @param source DragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the
     *            original touch happened
     * @param yOffset Vertical offset with the object being dragged where the
     *            original touch happened
     * @param dragView The DragView that's being dragged around on screen.
     * @param dragInfo Data associated with the object being dragged
     * @return True if the drop will be accepted, false otherwise.
     */
    public boolean acceptDrop(DragObject dragObject);
    
//	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
//            DragView dragView, Object dragInfo);

    /**
     * Estimate the surface area where this object would land if dropped at the
     * given location.
     * 
     * @param source DragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the
     *            original touch happened
     * @param yOffset Vertical offset with the object being dragged where the
     *            original touch happened
     * @param dragView The DragView that's being dragged around on screen.
     * @param dragInfo Data associated with the object being dragged
     * @param recycle {@link Rect} object to be possibly recycled.
     * @return Estimated area that would be occupied if object was dropped at
     *         the given location. Should return null if no estimate is found,
     *         or if this target doesn't provide estimations.
     */
	public Rect estimateDropLocation(DragObject dragObject, Rect recycle);
	
//	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
//            DragView dragView, Object dragInfo, Rect recycle);

    // These methods are implemented in Views
	public void getHitRect(Rect outRect);
	public void getLocationOnScreen(int[] loc);
    public int getLeft();
    public int getTop();
}
