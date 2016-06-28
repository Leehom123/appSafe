package com.example.AppSafe;



import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.AppSafe.R;
import com.example.AppSafe.R.id;
import com.example.AppSafe.R.layout;
import com.example.AppSafe.utils.MD5Utils;

public class LockScreenActivity extends Activity {
	private EditText et_password;
	private SharedPreferences sp;
	private String packName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp=getSharedPreferences("config", MODE_PRIVATE);
		setContentView(R.layout.activity_lock_screen);
		
		et_password=(EditText) findViewById(R.id.et_password);
		
	}
	@Override
	public void onBackPressed() {
		Intent intent=new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addCategory("android.intent.category.MONKEY");
		startActivity(intent);
	}
	public void click(View view){
		packName=getIntent().getStringExtra("packName");
		String password=et_password.getText().toString().trim();
		if(TextUtils.isEmpty(password)){
			Toast.makeText(this, "密码不能为空", 0).show();
			return;
		}
		String lockapppassword=sp.getString("applockpassword", "");
		if(MD5Utils.md5Encode(password).equals(lockapppassword)){
			packName=getIntent().getStringExtra("packName");
			Intent intent=new Intent();
			intent.setAction("com.xiong.applock");
			intent.putExtra("packName", packName);
			sendBroadcast(intent);
			
			finish();
			
		}else{Toast.makeText(this, "密码错误，请重新输入", 0).show();
		et_password.setText("");
		return;
		}
		
	}
}
