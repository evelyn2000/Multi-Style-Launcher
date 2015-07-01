package com.android.qshome.ctrl;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.android.qshome.LauncherModel;
import com.android.qshome.R;
import com.android.qshome.ShortcutsAdapter;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.FolderInfo;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.LauncherSettings.Favorites;

/**
 * Folder which contains applications or shortcuts chosen by the user.
 *
 */
public class UserFolder extends Folder implements DropTarget {
    private static final String TAG = "Launcher.UserFolder";

    public UserFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * Creates a new UserFolder, inflated from R.layout.user_folder.
     *
     * @param context The application's context.
     *
     * @return A new UserFolder.
     */
    public static UserFolder fromXml(Context context) {
        return (UserFolder) LayoutInflater.from(context).inflate(R.layout.user_folder, null);
    }

    public boolean acceptDrop(DragObject dragObject) {
        final ItemInfo item = (ItemInfo) dragObject.dragInfo;
        final int itemType = item.itemType;
        return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                    itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
                && item.container != mInfo.id;
    }
    
    public Rect estimateDropLocation(DragObject dragObject, Rect recycle) {
        return null;
    }

    public boolean onDrop(DragObject dragObject) {
        ShortcutInfo item;
        if (dragObject.dragInfo instanceof ApplicationInfo) {
            // Came from all apps -- make a copy
            item = ((ApplicationInfo)dragObject.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo)dragObject.dragInfo;
        }
        ((ShortcutsAdapter)mContent.getAdapter()).add(item);
        
        boolean isAppMode = false;
        if (dragObject.dragSource instanceof Workspace)
        	isAppMode = ((Workspace)dragObject.dragSource).getIsApplicationMode();
        
        LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0, isAppMode);
        return true;
    }

    public void onDragEnter(DragObject dragObject) {
    }

    public void onDragOver(DragObject dragObject) {
    }

    public void onDragExit(DragObject dragObject) {
    }
    
    public boolean isDropEnabled(){
    	return true;
    }
    
    public DropTarget getDropTargetDelegate(DragObject dragObject){
    	return null;
    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean success) {
        if (success) {
            ShortcutsAdapter adapter = (ShortcutsAdapter)mContent.getAdapter();
            adapter.remove(mDragItem);
        }
    }

    public void bind(FolderInfo info) {
        super.bind(info);
        setContentAdapter(new ShortcutsAdapter(getContext(), ((UserFolderInfo) info).contents));
    }

    // When the folder opens, we need to refresh the GridView's selection by
    // forcing a layout
    @Override
    public void onOpen() {
        super.onOpen();
        requestFocus();
    }
}
