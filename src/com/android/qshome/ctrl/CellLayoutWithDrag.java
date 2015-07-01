package com.android.qshome.ctrl;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.CellLayout.LayoutParams;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.QsLog;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;

import com.android.qshome.R;

public class CellLayoutWithDrag extends CellLayoutIcs implements DropTarget, DragSource, DragScroller {

	/**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;
    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = null;
    private int[] mTempCell = new int[2];
    private int[] mTempEstimate = new int[2];
    
    private Launcher mLauncher;
    private Workspace mWorkspace;
    private IconCache mIconCache;
    private DragController mDragController;
    private OnLongClickListener mLongClickListener;
    
    private int mShortcutLayoutResource;
    private int mQsContainerType;
    
    /**
     * Cache of vacant cells, used during drag events and invalidated as needed.
     */
    private CellLayout.CellInfo mVacantCache = null;
    
    
	public CellLayoutWithDrag(Context context) {
        this(context, null);
    }

    public CellLayoutWithDrag(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayoutWithDrag(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayoutWithDrag, defStyle, 0);
        mShortcutLayoutResource = a.getResourceId(R.styleable.CellLayoutWithDrag_shortcut_layout, R.layout.hot_set_btn_item);
        mQsContainerType = a.getInt(R.styleable.CellLayoutWithDrag_container, LauncherSettings.Favorites.CONTAINER_DESKTOP);

        a.recycle();
    }
    
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Generate an id for each view, this assumes we have at most 256x256 cells
        // per workspace screen
        final LayoutParams cellParams = (LayoutParams) params;
        if(cellParams.cellX >= super.getCountX() || cellParams.cellY >= super.getCountY()){
        	int tmp = cellParams.cellX;
        	cellParams.cellX = cellParams.cellY;
        	cellParams.cellY = tmp;
        }

        super.addView(child, index, cellParams);
    }
    
    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setDragController(DragController dragController) {
    	if(mDragController == null)
    		dragController.setWindowToken(getWindowToken());
        mDragController = dragController;
    }
    
    public void setWorkspace(Workspace workspace) {
    	mWorkspace = workspace;
    }
    
    public View createShortcut(ShortcutInfo info){
        return mLauncher.createShortcut(mShortcutLayoutResource, this, info);
    }
    
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
//        final int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            getChildAt(i).setOnLongClickListener(l);
//        }
        super.setOnLongClickListener(l);
    }
    
    
    public void startDrag(CellLayout.CellInfo cellInfo) {
        View child = cellInfo.cell;
        
        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }
        
        mDragInfo = cellInfo;
        mDragInfo.screen = 0;
        //QsLog.LogE("CellLayoutWithDrag::startDrag(0)===="+child.getTag().toString());
        
        onDragChild(child);
        mDragController.startDrag(child, this, child.getTag(), DragController.DRAG_ACTION_MOVE);
        invalidate();
    }
    
    public void startDrag(CellLayout.CellInfo cellInfo, int dragAction){
    	View child = cellInfo.cell;
        
        // Make sure the drag was started by a long press as opposed to a long click.
        if (child == null || !child.isInTouchMode()) {
            return;
        }
        
        mDragInfo = cellInfo;
        mDragInfo.screen = 0;
        
        // if copy ,then show desktop
    	if(dragAction != DragController.DRAG_ACTION_COPY ){
    		onDragChild(child);
    	}
    	
    	mDragController.startDrag(child, this, child.getTag(), dragAction);
    	invalidate();
    }
    
    public boolean onDrop(DragObject dragObject) {

        if (dragObject.dragSource != this) {
            return onDropExternal(dragObject, this);
        } else {
            // Move internally
        	//QsLog.LogW("CellLayoutWithDrag::onDrop(1)==move==");
            if (mDragInfo != null) {
                final View cell = mDragInfo.cell;
                //QsLog.LogD("CellLayoutWithDrag::onDrop(2)==move====cellX:"+mDragInfo.cellX+"==cellY:"+mDragInfo.cellY+"=="+cell.getTag().toString());
                mTargetCell = estimateDropCell(dragObject.x - dragObject.xOffset, dragObject.y - dragObject.yOffset,
                        mDragInfo.spanX, mDragInfo.spanY, cell, this, mTargetCell);
                
                //QsLog.LogD("CellLayoutWithDrag::onDrop(3)==move==cellX:"+mTargetCell[0]+"==cellY:"+mTargetCell[1]);
                
                onDropChild(cell, mTargetCell);

                final ItemInfo info = (ItemInfo) cell.getTag();
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                
                //QsLog.LogD("CellLayoutWithDrag::onDrop(4)==move==cellX:"+lp.cellX+"==cellY:"+lp.cellY+"=="+info.toString());
                
                LauncherModel.moveItemInDatabase(mLauncher, info,
                		mQsContainerType, 0, lp.cellX, lp.cellY, false);
            }
        }
        
        return true;
    }

    public void onDragEnter(DragObject dragObject) {
        clearVacantCache();
//		final Folder openFolder = getOpenFolder();
//		if( openFolder != null ) {
//			mLauncher.closeFolder( openFolder );
//		}
        //QsLog.LogE("CellLayoutWithDrag::onDragEnter(0)======");
        setIsDragOverlapping(true);
        super.onDragEnter();
    }

    public void onDragOver(DragObject dragObject) {
    }

    public void onDragExit(DragObject dragObject) {
        clearVacantCache();
        setIsDragOverlapping(false);
        super.onDragExit();
    }

    private boolean onDropExternal(DragObject dragObject, CellLayout cellLayout) {
        return onDropExternal(dragObject, cellLayout, false);
    }
    
    private boolean onDropExternal(DragObject dragObject, CellLayout cellLayout,
            boolean insertAtFirst) {
        // Drag from somewhere else
        ItemInfo info = (ItemInfo) dragObject.dragInfo;
        boolean bIsUpdateSmsIcon = false;
        View view = null;
        //QsLog.LogW("CellLayoutWithDrag::onDropExternal(1)==isself:"+(dragObject.dragSource == this)+"=="+info.toString());
        switch (info.itemType) {
        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
            if (info.container == NO_ID && info instanceof ApplicationInfo) {
                // Came from all apps -- make a copy
                info = new ShortcutInfo((ApplicationInfo)info);
            }
            
            view = createShortcut((ShortcutInfo)info);
            break;
        case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
//            view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
//                    (ViewGroup) getChildAt(mCurrentScreen), ((UserFolderInfo) info));
            break;
            
        default:
            throw new IllegalStateException("Unknown item type: " + info.itemType);
        }
        
        if(view == null) {
            // do not add the view if view is null.
        	//QsLog.LogE("CellLayoutWithDrag::onDropExternal(5)===view is null===");
            return false;
        }
       
        cellLayout.addView(view, insertAtFirst ? 0 : -1);
        view.setHapticFeedbackEnabled(false);
        view.setOnLongClickListener(mLongClickListener);
        if (view instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) view);
        }

        mTargetCell = estimateDropCell(dragObject.x, dragObject.y, 1, 1, view, cellLayout, mTargetCell);
        cellLayout.onDropChild(view, mTargetCell);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();

        LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
        		mQsContainerType, 0, lp.cellX, lp.cellY, false);
        
//        if(bIsUpdateSmsIcon){
//        	mLauncher.updateCurrentSmsIconByMessage();
//        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragObject dragObject) {
        //final CellLayout layout = getCurrentDropLayout();
        final CellLayout.CellInfo cellInfo = mDragInfo;
        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
        if (mVacantCache == null) {
            final View ignoreView = cellInfo == null ? null : cellInfo.cell;
            mVacantCache = findAllVacantCells(null, ignoreView);
        }

        return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public Rect estimateDropLocation(DragObject dragObject, Rect recycle) {
        //final CellLayout layout = getCurrentDropLayout();
        
        final CellLayout.CellInfo cellInfo = mDragInfo;
        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
        final View ignoreView = cellInfo == null ? null : cellInfo.cell;
        
        final Rect location = recycle != null ? recycle : new Rect();
        
        // Find drop cell and convert into rectangle
        int[] dropCell = estimateDropCell(dragObject.x - dragObject.xOffset, dragObject.y - dragObject.yOffset,
                spanX, spanY, ignoreView, this, mTempCell);
        
        if (dropCell == null) {
            return null;
        }
        
        super.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
        location.left = mTempEstimate[0];
        location.top = mTempEstimate[1];
        
        super.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY, mTempEstimate);
        location.right = mTempEstimate[0];
        location.bottom = mTempEstimate[1];
        
        return location;
    }
    
    public boolean isDropEnabled(){
    	return true;
    }
    
    public DropTarget getDropTargetDelegate(DragObject dragObject){
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
    
    public void onDropCompleted(View target, DragObject d, boolean success) {
        clearVacantCache();
        if (success){
            if (target != this && mDragInfo != null) {
            	
            	// jz if workspace only set as drag source, can't remove 
            	if(mDragController.getDragActionMode() != DragController.DRAG_ACTION_COPY){
	                removeView(mDragInfo.cell);
            	}
            	
                if (mDragInfo.cell instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget)mDragInfo.cell);
                }
                //final Object tag = mDragInfo.cell.getTag();
            }
        } else {
            if (mDragInfo != null) {
                //final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                onDropAborted(mDragInfo.cell);
            }
        }

        mDragInfo = null;
    }
    
    public void scrollLeft() {
        clearVacantCache();
    }

    public void scrollRight() {
        clearVacantCache();
    }
    
    private void clearVacantCache() {
        if (mVacantCache != null) {
            mVacantCache.clearVacantCells();
            mVacantCache = null;
        }
    }
    
}
