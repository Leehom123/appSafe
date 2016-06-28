package com.example.AppSafe.utils;

import java.util.ArrayList;
import java.util.List;

import com.example.AppSafe.domain.AppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;



public class AppInfoProvider {
	public static List<AppInfo> getAppInfos(Context context){
		List<AppInfo> appInfos=new ArrayList<AppInfo>();
		PackageManager manager=context.getPackageManager();//得到清单文件
		 List<PackageInfo> infos=manager.getInstalledPackages(0);
		 for(PackageInfo info:infos){
			 String packName=info.packageName;
			 if(!packName.equals(context.getPackageName())){
			 String name=(String) info.applicationInfo.loadLabel(manager);
			 Drawable appIcon=info.applicationInfo.loadIcon(manager);
			 boolean inRom;
			 boolean userApp;
			 int flag=info.applicationInfo.flags;
			 if((flag&ApplicationInfo.FLAG_SYSTEM)==0){
				 userApp=true;
			 }else{
				 userApp=false;
			 }
			 if((flag&ApplicationInfo.FLAG_EXTERNAL_STORAGE)==0){
				 inRom=true;
			 }else{
				 inRom=false;
			 }
			 
			 AppInfo appInfo=new AppInfo(name, packName, appIcon, inRom, userApp);
			 
			 appInfos.add(appInfo);}
		 }
		 return appInfos;
	}
}
