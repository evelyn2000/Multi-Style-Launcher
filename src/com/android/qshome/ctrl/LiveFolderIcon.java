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
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.Bitmap;

import com.android.qshome.Launcher;
import com.android.qshome.R;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.LiveFolderInfo;
import com.android.qshome.util.Utilities;

public class LiveFolderIcon extends FolderIcon {
    public LiveFolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveFolderIcon(Context context) {
        super(context);
    }

    public static LiveFolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            LiveFolderInfo folderInfo) {

        LiveFolderIcon icon = (LiveFolderIcon)
                LayoutInflater.from(launcher).inflate(resId, group, false);

        final Resources resources = launcher.getResources();
        Bitmap b = folderInfo.icon;
        if (b == null) {
            b = Utilities.createIconBitmap(resources.getDrawable(R.drawable.ic_launcher_folder),
                    launcher);
        }
        icon.setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(b), null, null);
        icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        
        return icon;
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        return false;
    }

    @Override
    public boolean onDrop(DragObject dragObject) {
    	return true;
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
    }

    @Override
    public void onDragOver(DragObject dragObject) {
    }

    @Override
    public void onDragExit(DragObject dragObject) {
    }
}
