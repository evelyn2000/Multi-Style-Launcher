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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.R;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.LauncherSettings.Favorites;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends BubbleTextView implements DropTarget {
    private UserFolderInfo mInfo;
    private Launcher mLauncher;
    private Drawable mCloseIcon;
    private Drawable mOpenIcon;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderIcon(Context context) {
        super(context);
    }

    public static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            UserFolderInfo folderInfo) {

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);

        final Resources resources = launcher.getResources();
        Drawable d = resources.getDrawable(R.drawable.ic_launcher_folder);
        icon.mCloseIcon = d;
        icon.mOpenIcon = resources.getDrawable(R.drawable.ic_launcher_folder_open);
        icon.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        
        return icon;
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
        mInfo.add(item);
        
        boolean isAppMode = false;
        if (dragObject.dragSource instanceof Workspace)
        	isAppMode = ((Workspace)dragObject.dragSource).getIsApplicationMode();
        
        LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0, isAppMode);
        
        return true;
    }

    public void onDragEnter(DragObject dragObject) {
        setCompoundDrawablesWithIntrinsicBounds(null, mOpenIcon, null, null);
    }

    public void onDragOver(DragObject dragObject) {
    }

    public void onDragExit(DragObject dragObject) {
        setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    }
    
    public boolean isDropEnabled(){
    	return true;
    }
    
    public DropTarget getDropTargetDelegate(DragObject dragObject){
    	return null;
    }
}
