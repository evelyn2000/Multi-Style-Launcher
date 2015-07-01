package com.android.qshome.ctrl;

import com.android.qshome.LauncherAppWidgetHost;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.DropTarget.DragObject;
import com.android.qshome.model.ItemInfo;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.model.ShortcutInfo;
import com.android.qshome.model.UserFolderInfo;
import com.android.qshome.util.LauncherSettings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;

public class UninstallZone extends DeleteZone {
	
	public UninstallZone(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UninstallZone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }
    
    @Override
    public boolean onDrop(DragObject dragObject) {

    	if(super.onDrop(dragObject)){
    		
    		// uninstall app
    		
    		return true;
    	}
        
        return false;
    }
}
