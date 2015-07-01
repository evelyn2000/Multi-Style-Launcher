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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.qshome.LauncherApplication;
import com.android.qshome.LauncherModel.Callbacks;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.util.Utilities;
import com.android.qshome.util.ThemeStyle;

import com.android.qshome.R;
/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {
    private static final String TAG = "Launcher.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private static class CacheEntry {
        public Bitmap icon;
        public String title;
        public Bitmap titleBitmap;
        
        //public Bitmap iconInEditMode;
    }

    private final Bitmap mDefaultIcon;
    private final LauncherApplication mContext;
    private final PackageManager mPackageManager;
    private final Utilities.BubbleText mBubble;
    private final HashMap<ComponentName, CacheEntry> mCache =
            new HashMap<ComponentName, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);
    
    //private BaseStyleObject.ThemeStyle mAppStyle;
    private Bitmap mDeleteIcon;
    
    private Callbacks mCallbacks;
    private final Object mLock = new Object();
    
    public interface Callbacks{
        public Bitmap createIconBitmap(ComponentName componentName, ResolveInfo info, Context context, final PackageManager packageManager);
    }
    
    public void setCallbacks(Callbacks callback){
    	synchronized (mLock) {
    		mCallbacks = callback;
    		this.flush();
        }
    }

    public IconCache(LauncherApplication context) {
        mContext = context;
        //mAppStyle = BaseStyleObject.ThemeStyle.Default;
        mPackageManager = context.getPackageManager();
        mBubble = new Utilities.BubbleText(context);
        mDefaultIcon = makeDefaultIcon();
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = mPackageManager.getDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        return b;
    }
    
    public void changeAppStyle(ThemeStyle style){
    	if(mBubble != null)
    		mBubble.initStyle(mContext, style);
    }
    
    public void changeDeleteIcon(int nResId){ // R.drawable.kill
    	if(nResId == 0){
    		mDeleteIcon.recycle();
    		mDeleteIcon = null;
    		return;
    	}
    	
    	mDeleteIcon = BitmapFactory.decodeResource(mContext.getResources(), nResId);
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info) {
        synchronized (mCache) {
            CacheEntry entry = cacheLocked(application.componentName, info);
            if (entry.titleBitmap == null) {
                entry.titleBitmap = mBubble.createTextBitmap(entry.title);
            }

            application.title = entry.title;
            application.titleBitmap = entry.titleBitmap;
            application.iconBitmap = entry.icon;
        }
    }

    public Bitmap getIcon(Intent intent) {
        synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
                return mDefaultIcon;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.icon;
        }
    }

    public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo) {
        synchronized (mCache) {
            if (resolveInfo == null || component == null) {
                return null;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.icon;
        }
    }
    
    public Bitmap getIconInEditMode(Intent intent) {
    	synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
//            	int random = (int) (Math.random() * (Launcher.mIconBackgroundBitmap.length - 1));
//                return Launcher.mIconBackgroundBitmap[random];
            	return null;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return null;//entry.iconInEditMode;
        }
    }
    
    public void changeSmsShortcutIcon(ComponentName component, Bitmap icon)
    {
    	synchronized (mCache) {
            if (component == null || icon == null) {
            	//Log.d("QiShang", "IconCache::changeSmsShortcutIcon()==pama error==");
                return;
            }

            CacheEntry entry = mCache.get(component);
            if (entry != null) {
            	entry.icon = icon;

                mCache.put(component, entry);
            }
            //else
            //{
            //	Log.d("QiShang", "IconCache::changeSmsShortcutIcon()==entry is null==");
            //}
        }
    }
//static Bitmap mBg = null;
    private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info) {
        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();

            mCache.put(componentName, entry);

            entry.title = info.loadLabel(mPackageManager).toString();
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }
            
            synchronized (mLock) {
        		if(mCallbacks != null){
        			entry.icon = mCallbacks.createIconBitmap(componentName, info, mContext, mPackageManager);
        		}
            }
            

            if(entry.icon == null){
	           	entry.icon = Utilities.createIconBitmap(
	            			info.activityInfo.loadIcon(mPackageManager), mContext);
            }
        }
        return entry;
    }
    
    public boolean isSystemApp(Intent intent) {
//    	if(intent != null){
//    		final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
//    		return isSystemApp(resolveInfo);
//    	}
        return true;
    }
    
    public boolean isSystemApp(ResolveInfo resolveInfo) {
    	if (resolveInfo != null && (resolveInfo.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }

        return false;
    }
}
