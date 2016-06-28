package com.example.AppSafe.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.example.AppSafe.LockScreenActivity;
import com.example.AppSafe.dao.AppLockDBDao;
import com.example.AppSafe.sound.SoundUse;

public class WatchDogService extends Service {
	private boolean flag;
	private String packName;
	private AppLockDBDao dao;
	private ActivityManager am;
	private MyReceiver receiver;
	private DBChangedReceiver receiver4;
	private LockScreenReceiver receiver2;
	private UnLockScreenReceiver receiver3;
	private String className;
	private String tempPackName;
	private Intent intent;
	private List<String> packNames;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		receiver=new MyReceiver();
		receiver2=new LockScreenReceiver();
		receiver3=new UnLockScreenReceiver();
		receiver4=new DBChangedReceiver();
			
		intent=new Intent(WatchDogService.this,SoundUse.class);
		
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		registerReceiver(receiver4, new IntentFilter("com.xiong.dbChanged"));
		registerReceiver(receiver2, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(receiver3, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(receiver, new IntentFilter("com.xiong.applock"));
		dao=new AppLockDBDao(this);
		packNames =dao.findAll();
		super.onCreate();
		flag=true;
		am=(ActivityManager) getSystemService(ACTIVITY_SERVICE);
		lock();
		
	}
	private void lock() {
		new Thread(){
			public void run(){
				while(flag){
					ComponentName runningActivity=am.getRunningTasks(1).get(0).topActivity;
					packName=runningActivity.getPackageName();
					if(packNames.contains(packName)){
						if(packName.equals(tempPackName)){
							
						}else{
							tempPackName=null;
					intent.putExtra("packName", packName);
					startActivity(intent);
						}
					}else if(!packName.equals(getPackageName())){
						tempPackName=null;
					}
				}
				
			}
		}.start();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		flag=false;
		unregisterReceiver(receiver);
		unregisterReceiver(receiver2);
		unregisterReceiver(receiver3);
		unregisterReceiver(receiver4);
		
		receiver4=null;
		receiver3=null;
		receiver2=null;
		receiver=null;
	}
	private class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			tempPackName=intent.getStringExtra("packName");
			
		}
		
	}
	private class LockScreenReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			tempPackName=null;
			flag=false;
		}
		
		
	}
private class UnLockScreenReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			flag=true;
			lock();
		}}
private class DBChangedReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		packNames=dao.findAll();
	}}
}
