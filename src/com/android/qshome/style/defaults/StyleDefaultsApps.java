package com.android.qshome.style.defaults;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;

import com.android.qshome.AllAppsView;
import com.android.qshome.Launcher;
import com.android.qshome.LauncherModel;
import com.android.qshome.ctrl.DeleteZone;
import com.android.qshome.ctrl.DragController;
import com.android.qshome.ctrl.DragLayer;
import com.android.qshome.ctrl.HandleView;
import com.android.qshome.model.ApplicationInfo;
import com.android.qshome.model.IconCache;
import com.android.qshome.model.LauncherAppWidgetInfo;
import com.android.qshome.style.BaseStyleObject;
import com.android.qshome.style.BaseStyleObjectApps;
import com.android.qshome.style.IBaseStyleInterfaceApps;
import com.android.qshome.style.IBaseStyleInterfaceWidget;
import com.android.qshome.util.QsLog;


import com.android.qshome.R;

public class StyleDefaultsApps extends BaseStyleObjectApps {
	
	public StyleDefaultsApps(Launcher context, LauncherModel model, IconCache iconCache, IBaseStyleInterfaceWidget callback){
		super(context, model, iconCache, callback);
	}

	public int getThemePreviewImage(){
		return 0;
	}
	
	public int getThemeTitleResourceId(){
		return 0;
	}
	
	public void onCreate(DragLayer dragLayer, DragController dragController){
		
		ViewStub stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_2d);
        mAllAppsGrid = (AllAppsView) stub.inflate();//(AllAppsView)dragLayer.findViewById(R.id.all_apps_view);
        mAllAppsGrid.setLauncher(getLauncher());
        mAllAppsGrid.setDragController(dragController);
        ((View) mAllAppsGrid).setWillNotDraw(false); // We don't want a hole punched in our window.
        // Manage focusability manually since this thing is always visible
        ((View) mAllAppsGrid).setFocusable(false); 
	}

}
