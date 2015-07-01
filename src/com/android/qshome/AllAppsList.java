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

package com.android.qshome;

import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.internal.util.XmlUtils;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Stores the list of all applications for the all apps view.
 */
public class AllAppsList {
	static final String TAG = "QsHome.AllAppsList";
    public static final int DEFAULT_APPLICATIONS_NUMBER = 42;
    
    private static final String TAG_TOPPACKAGES = "toppackages";
    private static final String TAG_TOPPACKAGE = "TopPackage";
    
    /** The list off all apps. */
    public ArrayList<ApplicationInfo> data =
            new ArrayList<ApplicationInfo>(DEFAULT_APPLICATIONS_NUMBER);
    /** The list of apps that have been added since the last notify() call. */
    public ArrayList<ApplicationInfo> added =
            new ArrayList<ApplicationInfo>(DEFAULT_APPLICATIONS_NUMBER);
    /** The list of apps that have been removed since the last notify() call. */
    public ArrayList<ApplicationInfo> removed = new ArrayList<ApplicationInfo>();
    /** The list of apps that have been modified since the last notify() call. */
    public ArrayList<ApplicationInfo> modified = new ArrayList<ApplicationInfo>();

    public static final String STK_PACKAGE = "com.android.stk";
    public static final String STK2_PACKAGE = "com.android.stk2";
    
    private IconCache mIconCache;
    
    public static ArrayList<TopPackage> mTopPackages;
    
    public static class TopPackage {
    	public TopPackage (String packagename,String classname,int order) {
    		mPackageName = packagename;
    		mClassName = classname;
    		mOrder = order;
    		mIndex = -1;
    		
    	}
    	
    	public String mPackageName;
    	public String mClassName;
    	public int mOrder;
    	
    	public int mIndex;
    }

    /**
     * Boring constructor.
     */
    public AllAppsList(IconCache iconCache) {
        mIconCache = iconCache;
    }

    /**
     * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
     * list to broadcast when notify() is called.
     *
     * If the app is already in the list, doesn't add it.
     */
    public void add(ApplicationInfo info) {
        if (findActivity(data, info.componentName)) {
            return;
        }
        data.add(info);
        added.add(info);
    }
    
    public void clear() {
        data.clear();
        // TODO: do we clear these too?
        added.clear();
        removed.clear();
        modified.clear();
    }

    public int size() {
        return data.size();
    }

    public ApplicationInfo get(int index) {
        return data.get(index);
    }

    /**
     * Add the icons for the supplied apk called packageName.
     */
    public void addPackage(Context context, String packageName) {
    	final PackageManager packageManager = context.getPackageManager();
    	
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);

        if (matches.size() > 0) {
            for (ResolveInfo info : matches) {
                add(new ApplicationInfo(packageManager, info, mIconCache));
            }
        }
    }

    /**
     * Remove the apps for the given apk identified by packageName.
     */
    public void removePackage(String packageName) {
        final List<ApplicationInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            ApplicationInfo info = data.get(i);
            final ComponentName component = info.intent.getComponent();
            if (packageName.equals(component.getPackageName())) {
                removed.add(info);
                data.remove(i);
            }
        }
        // This is more aggressive than it needs to be.
        mIconCache.flush();
    }

    /**
     * Add and remove icons for this package which has been updated.
     */
    public void updatePackage(Context context, String packageName) {
    	final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
        if (matches.size() > 0) {
            // Find disabled/removed activities and remove them from data and add them
            // to the removed list.
            for (int i = data.size() - 1; i >= 0; i--) {
                final ApplicationInfo applicationInfo = data.get(i);
                final ComponentName component = applicationInfo.intent.getComponent();
                if (packageName.equals(component.getPackageName())) {
                    if (!findActivity(matches, component)) {
                        removed.add(applicationInfo);
                        mIconCache.remove(component);
                        data.remove(i);
                    }
                }
            }

            // Find enabled activities and add them to the adapter
            // Also updates existing activities with new labels/icons
            int count = matches.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = matches.get(i);
                ApplicationInfo applicationInfo = findApplicationInfoLocked(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name);
                if (applicationInfo == null) {
                    add(new ApplicationInfo(packageManager, info, mIconCache));
                } else {
                    mIconCache.remove(applicationInfo.componentName);
                    mIconCache.getTitleAndIcon(applicationInfo, info);
                    modified.add(applicationInfo);
                }
            }
        } else {
            // findActivitiesForPackage cannot get disabled Activity.
            // a simple process for STK Test
            if(packageName.compareTo(STK_PACKAGE) == 0 || packageName.compareTo(STK2_PACKAGE) == 0) {
                removeDisabledStkActivity(packageName);
            }
        }
    }

    private void removeDisabledStkActivity(String packageName) {
        for (int i=data.size()-1; i>=0; i--) {
            final ApplicationInfo applicationInfo = data.get(i);
            final ComponentName component = applicationInfo.intent.getComponent();
            if (packageName.equals(component.getPackageName())) {
                removed.add(applicationInfo);
                mIconCache.remove(component);
                data.remove(i);
            }
        }
    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     */
    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

        if (apps != null) {
            // Find all activities that match the packageName
            int count = apps.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = apps.get(i);
                final ActivityInfo activityInfo = info.activityInfo;
                if (packageName.equals(activityInfo.packageName)) {
                    matches.add(info);
                }
            }
        }

        return matches;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(List<ResolveInfo> apps, ComponentName component) {
        final String className = component.getClassName();
        for (ResolveInfo info : apps) {
            final ActivityInfo activityInfo = info.activityInfo;
            if (activityInfo.name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(ArrayList<ApplicationInfo> apps, ComponentName component) {
        final int N = apps.size();
        for (int i=0; i<N; i++) {
            final ApplicationInfo info = apps.get(i);
            if (info.componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find an ApplicationInfo object for the given packageName and className.
     */
    private ApplicationInfo findApplicationInfoLocked(String packageName, String className) {
        for (ApplicationInfo info: data) {
            final ComponentName component = info.intent.getComponent();
            if (packageName.equals(component.getPackageName())
                    && className.equals(component.getClassName())) {
                return info;
            }
        }
        return null;
    }
    
    
    /**
     * Loads the default set of default to packages from an xml file.
     *
     * @param context The context 
     */
    public static boolean loadTopPackage(Context context) {
    	boolean bRet = false;
    	
    	if (mTopPackages == null) {
    		mTopPackages = new ArrayList<TopPackage>();
    	} else {
    		return true;
    	}

        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.default_toppackage);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_TOPPACKAGES);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) 
            		&& type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }                    

                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopPackage);                    
                
                mTopPackages.add(new TopPackage(a.getString(R.styleable.TopPackage_topPackageName),
                		a.getString(R.styleable.TopPackage_topClassName),
                		a.getInt(R.styleable.TopPackage_topOrder, 0)));
                
                //Log.d(TAG, "loadTopPackage packageName==" + a.getString(R.styleable.TopPackage_topPackageName)); 
                //Log.d(TAG, "loadTopPackage className==" + a.getString(R.styleable.TopPackage_topClassName));

                a.recycle();
            }
        } catch (XmlPullParserException e) {
            //Log.w(TAG, "Got exception parsing toppackage.", e);
        } catch (IOException e) {
            //Log.w(TAG, "Got exception parsing toppackage.", e);
        }

        return bRet;
    }   
    
    public static int getTopPackageIndex(ApplicationInfo appInfo) {
    	int retIndex = -1;
        if (mTopPackages == null || mTopPackages.isEmpty() || appInfo == null) {
        	return retIndex;
        } 
        
        for (TopPackage tp : mTopPackages) {        	                
    		if (appInfo.componentName.getPackageName().equals(tp.mPackageName) 
    				&& appInfo.componentName.getClassName().equals(tp.mClassName)) {

    			retIndex = tp.mOrder;    			
    			break;
    		}               	                
        } 
        
        return retIndex;        
    }
    
    public void dumpData() {
        int loop2 = 0;
    	for (ApplicationInfo ai : data) {
//    		if (DEBUG_LOADERS_REORDER) {
//    			Log.d(TAG, "reorderApplist data loop2==" + loop2);
//    			Log.d(TAG, "reorderApplist data packageName==" + ai.componentName.getPackageName()); 
//    		}
    		loop2++;
    	} 	
    }
    
    public void reorderApplist() {
        //final long sortTime = DEBUG_LOADERS_REORDER ? SystemClock.uptimeMillis() : 0;                                
        
        if (mTopPackages == null || mTopPackages.isEmpty()) {
        	return ;
        }
        
        ArrayList<ApplicationInfo> dataReorder =
            new ArrayList<ApplicationInfo>(DEFAULT_APPLICATIONS_NUMBER); 
        
        for (TopPackage tp : mTopPackages) { 
        	int loop = 0;
        	int newIndex = 0;
        	for (ApplicationInfo ai : added) {
//        		if (DEBUG_LOADERS_REORDER) {
//        			Log.d(TAG, "reorderApplist remove loop==" + loop);
//        			Log.d(TAG, "reorderApplist remove packageName==" + ai.componentName.getPackageName()); 
//        		}
                
        		if (ai.componentName.getPackageName().equals(tp.mPackageName) 
        				&& ai.componentName.getClassName().equals(tp.mClassName)) {
//            		if (DEBUG_LOADERS_REORDER) {
//            			Log.d(TAG, "reorderApplist remove newIndex==" + newIndex); 
//            		}
            		
            		data.remove(ai);
            		dataReorder.add(ai);	

        			dumpData();
        			
        			break;
        		}
        		loop++;
        	}                	                
        }  
        
        for (TopPackage tp : mTopPackages) { 
        	int loop = 0;
        	int newIndex = 0;
        	for (ApplicationInfo ai : dataReorder) {
//        		if (DEBUG_LOADERS_REORDER) {
//        			Log.d(TAG, "reorderApplist added loop==" + loop);
//        			Log.d(TAG, "reorderApplist added packageName==" + ai.componentName.getPackageName()); 
//        		}
                
        		if (ai.componentName.getPackageName().equals(tp.mPackageName) 
        				&& ai.componentName.getClassName().equals(tp.mClassName)) {
        			newIndex = Math.min(Math.max(tp.mOrder, 0), added.size());
//            		if (DEBUG_LOADERS_REORDER) {
//            			Log.d(TAG, "reorderApplist added newIndex==" + newIndex); 
//            		}

            		data.add(newIndex,ai);	

        			dumpData();
        			
        			break;
        		}
        		loop++;
        	}                	                
        } 
        
        if (added.size() == data.size()) {
        	added = (ArrayList<ApplicationInfo>) data.clone();	
        	//Log.d(TAG, "reorderApplist added.size() == data.size()");
        }
        
//        if (DEBUG_LOADERS_REORDER) {
//            Log.d(TAG, "sort and reorder took "
//                    + (SystemClock.uptimeMillis()-sortTime) + "ms");
//        }            	
    }
}
