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

package com.android.qshome.model;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;

import com.android.qshome.util.LauncherSettings;
import com.android.qshome.util.LauncherSettings.BaseLauncherColumns;
import com.android.qshome.util.QsLog;

/**
 * Represents an app in AllAppsView.
 */
public class ApplicationInfo extends ItemInfo {

    /**
     * The application name.
     */
	public CharSequence title;

    /**
     * A bitmap of the application's text in the bubble.
     */
	public Bitmap titleBitmap;

    /**
     * The intent used to start the application.
     */
	public Intent intent;

    /**
     * A bitmap version of the application icon.
     */
	public Bitmap iconBitmap;

	public ComponentName componentName;

    public static final int DOWNLOADED_FLAG = 1;
    public static final int UPDATED_SYSTEM_APP_FLAG = 2;

    public int flags = 0;
    
	public ApplicationInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    /**
     * Must not hold the Context.
     */
    public ApplicationInfo(PackageManager pm, ResolveInfo info, IconCache iconCache) {
        //this.componentName = new ComponentName(
        final String packageName = info.activityInfo.applicationInfo.packageName;

        this.componentName = new ComponentName(packageName, info.activityInfo.name);
        this.container = ItemInfo.NO_ID;
        this.setActivity(componentName,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        try {
            int appFlags = pm.getApplicationInfo(packageName, 0).flags;
            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                flags |= DOWNLOADED_FLAG;

                if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    flags |= UPDATED_SYSTEM_APP_FLAG;
                }
            }
            //firstInstallTime = pm.getPackageInfo(packageName, 0).firstInstallTime;
        } catch (NameNotFoundException e) {
            QsLog.LogE("PackageManager.getApplicationInfo failed for " + packageName);
        }
        iconCache.getTitleAndIcon(this, info);
    }
    
    public ApplicationInfo(ApplicationInfo info) {
        super(info);
        componentName = info.componentName;
        title = info.title.toString();
        intent = new Intent(info.intent);
        flags = info.flags;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    public final void setActivity(ComponentName className, int launchFlags) {
    	String action = Intent.ACTION_MAIN;
    	if(className != null && "com.android.contacts".equals(className.getPackageName())){
            if("com.android.contacts.DialtactsActivity".equals(className.getClassName())){
            	action = Intent.ACTION_DIAL;
            }
        }
        intent = new Intent(action);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + title.toString() + ")" + super.toString();
    }

    public static void dumpApplicationInfoList(String tag, String label,
            ArrayList<ApplicationInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (ApplicationInfo info: list) {
            Log.d(tag, "   title=\"" + info.title + "\" titleBitmap=" + info.titleBitmap
                    + " iconBitmap=" + info.iconBitmap);
        }
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }
}
