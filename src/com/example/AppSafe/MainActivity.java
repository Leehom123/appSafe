package com.example.AppSafe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.AppSafe.dao.AppLockDBDao;
import com.example.AppSafe.domain.AppInfo;
import com.example.AppSafe.service.WatchDogService;
import com.example.AppSafe.sound.SoundSet;
import com.example.AppSafe.sound.SoundUse;
import com.example.AppSafe.utils.AppInfoProvider;
import com.example.AppSafe.utils.MD5Utils;
import com.example.AppSafe.utils.ServiceUtils;
import com.example.AppSafe.ContentAdapter;
import com.example.AppSafe.ContentModel;
import com.iflytek.cloud.SpeechUtility;

public class MainActivity extends Activity {
	private DrawerLayout drawerLayout;
	private RelativeLayout leftLayout;
	private RelativeLayout rightLayout;
	private List<ContentModel> list; 
	private ContentAdapter setadapter;
	private Button bt_moreSet;
	private ContentModel ItemText1;
	private ContentModel ItemText2;

	private SharedPreferences sp;
	private AppInfo appInfo;
	private ListView lv_app;
	private LinearLayout ll_proBar;
	private List<AppInfo> appInfos;
	private List<AppInfo> userAppInfos;
	private Button bt_setting;
	private FocusedTextView ftv_status;
	private List<AppInfo> systemAppInfos;
	private TextView tv_status;
	private MyAdapter adapter;
	private AppLockDBDao dao;
	private static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = getSharedPreferences("config", MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		SpeechUtility
				.createUtility(this, "appid=" + getString(R.string.app_id));
		setContentView(R.layout.activity_main);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		leftLayout = (RelativeLayout) findViewById(R.id.left);
		bt_moreSet = (Button) findViewById(R.id.bt_moreSet);
		ListView listView = (ListView) leftLayout
				.findViewById(R.id.left_listview);

		initData();
		setadapter = new ContentAdapter(this, list);
		listView.setAdapter(setadapter); 
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (list.get(position).equals(ItemText1)) {
					showPasswordSetDialog();
				}
				if (list.get(position).equals(ItemText2)) {
					startActivity(new Intent(MainActivity.this, SoundSet.class));
				}
			}
		});

		dao = new AppLockDBDao(this);
		bt_setting = (Button) findViewById(R.id.bt_setting);
		ftv_status = (FocusedTextView) findViewById(R.id.ftv_status);
		lv_app = (ListView) findViewById(R.id.lv_app);
		ll_proBar = (LinearLayout) findViewById(R.id.ll_probar);
		tv_status = (TextView) findViewById(R.id.tv_status);
		final Intent intent = new Intent(this, WatchDogService.class);

		bt_moreSet.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				drawerLayout.openDrawer(Gravity.START);
			}
		});
		bt_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (bt_setting.getText().equals("开启")) {
					startService(intent);
					ftv_status
							.setText("程序锁已开启，请点击右上角关闭!      程序锁已开启，请点击右上角关闭!");
					bt_setting.setText("关闭");
				} else {
					stopService(intent);
					ftv_status
							.setText("程序锁已关闭，请点击右上角开启!      程序锁已关闭，请点击右上角开启!");
					bt_setting.setText("开启");
				}

			}
		});
		fillData();
		lv_app.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0 || position == userAppInfos.size() + 1) { 
					return;
				} else if (position <= userAppInfos.size()) {
					int newPosition = position - 1;
					appInfo = userAppInfos.get(newPosition);
				} else {
					int newPosition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newPosition);
				}
				ViewHolder holder = (ViewHolder) view.getTag();
				ImageView iv_applock = holder.iv_applock;
				if (dao.find(appInfo.getPackName())) {
					dao.delete(appInfo.getPackName());
					iv_applock.setImageResource(R.drawable.unlock);
				} else {
					dao.add(appInfo.getPackName());
					iv_applock.setImageResource(R.drawable.lock);
				}

			}

		});
		lv_app.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (userAppInfos != null && systemAppInfos != null) {
					if (firstVisibleItem > userAppInfos.size()) {
						tv_status.setText("系统应用" + systemAppInfos.size() + "个");
						tv_status.setBackgroundColor(getResources().getColor(
								R.color.bac));
						tv_status.setVisibility(View.VISIBLE);
					} else {
						tv_status.setText("用户应用" + userAppInfos.size() + "个");
						tv_status.setBackgroundColor(getResources().getColor(
								R.color.bac));
						tv_status.setVisibility(View.VISIBLE);
					}
				}
			}
		});

	}

	private void initData() {
		list = new ArrayList<ContentModel>();
		ItemText1 = new ContentModel(R.drawable.lock_txt, "文本密码设置");
		ItemText2 = new ContentModel(R.drawable.lock_sound, "语音密码设置");
		list.add(ItemText1);
		list.add(ItemText2);

	}

	private void fillData() {
		new Thread() {
			public void run() {
				appInfos = AppInfoProvider.getAppInfos(MainActivity.this);
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo info : appInfos) {
					if (info.isUserApp()) {
						userAppInfos.add(info);
					} else {
						systemAppInfos.add(info);
					}
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						ll_proBar.setVisibility(View.INVISIBLE);
						if (adapter == null) {
							adapter = new MyAdapter();
							lv_app.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged();
						}
					}
				});

			}
		}.start();
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return appInfos.size() + 2;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			if (position == 0) {
				TextView textView = new TextView(MainActivity.this);
				textView.setText("用户应用" + userAppInfos.size() + "个");
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundColor(getResources()
						.getColor(R.color.bac));
				return textView;
			}
			if (position == userAppInfos.size() + 1) {
				TextView textView = new TextView(MainActivity.this);
				textView.setText("系统应用" + systemAppInfos.size() + "个");
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundColor(getResources()
						.getColor(R.color.bac));
				return textView;
			}
			if (convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(MainActivity.this,
						R.layout.list_item_applock, null);
				holder = new ViewHolder();
				holder.iv_app_icon = (ImageView) view
						.findViewById(R.id.iv_app_icon);
				holder.tv_app_name = (TextView) view
						.findViewById(R.id.tv_app_name);
				holder.tv_app_location = (TextView) view
						.findViewById(R.id.tv_app_location);
				holder.iv_applock = (ImageView) view
						.findViewById(R.id.iv_applock);
				view.setTag(holder);
			}
			if (position < userAppInfos.size() + 1) {
				holder.iv_app_icon.setImageDrawable(userAppInfos.get(
						position - 1).getAppIcon());
				holder.tv_app_name.setText(userAppInfos.get(position - 1)
						.getName());
				if (userAppInfos.get(position - 1).isInRom()) {
					holder.tv_app_location.setText("手机内存");
				} else {
					holder.tv_app_location.setText("外部存储");
				}
				if (dao.find(userAppInfos.get(position - 1).getPackName())) {
					holder.iv_applock.setImageResource(R.drawable.lock);
				} else {
					holder.iv_applock.setImageResource(R.drawable.unlock);
				}
			}
			if (position > userAppInfos.size() + 1) {
				holder.iv_app_icon.setImageDrawable(systemAppInfos.get(
						position - userAppInfos.size() - 2).getAppIcon());
				holder.tv_app_name.setText(systemAppInfos.get(
						position - userAppInfos.size() - 2).getName());
				if (systemAppInfos.get(position - userAppInfos.size() - 2)
						.isInRom()) {
					holder.tv_app_location.setText("手机内存");
				} else {
					holder.tv_app_location.setText("外部存储");
				}
				if (dao.find(systemAppInfos.get(
						position - userAppInfos.size() - 2).getPackName())) {
					holder.iv_applock.setImageResource(R.drawable.lock);
				} else {
					holder.iv_applock.setImageResource(R.drawable.unlock);
				}
			}

			return view;
		}

	}

	static class ViewHolder {
		ImageView iv_app_icon;
		TextView tv_app_name;
		TextView tv_app_location;
		ImageView iv_applock;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	protected void showPasswordInputDialog() {
		View view = View.inflate(MainActivity.this,
				R.layout.dialog_password_input, null);
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		et_password = (EditText) view.findViewById(R.id.et_password);
		bt_ok = (Button) view.findViewById(R.id.ok);
		bt_cancle = (Button) view.findViewById(R.id.cancle);
		dialog = builder.create();
		dialog.setCancelable(false);
		bt_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
		bt_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String password = sp.getString("applockpassword", "");
				if (password.equals(MD5Utils.md5Encode(et_password.getText()
						.toString().trim()))) {
					dialog.dismiss();

				} else {
					Toast.makeText(MainActivity.this, "密码输入错误", 0).show();
					et_password.setText("");
				}
			}
		});
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

	}

	AlertDialog dialog;
	EditText et_password;
	EditText et_password_confirm;
	Button bt_ok;
	Button bt_cancle;

	protected void showPasswordSetDialog() {
		View view = View.inflate(MainActivity.this,
				R.layout.dialog_password_set, null);
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		et_password = (EditText) view.findViewById(R.id.et_password);
		et_password_confirm = (EditText) view
				.findViewById(R.id.et_password_confirm);
		bt_ok = (Button) view.findViewById(R.id.ok);
		bt_cancle = (Button) view.findViewById(R.id.cancle);

		bt_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
		bt_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String password = et_password.getText().toString().trim();
				String password_confirm = et_password_confirm.getText()
						.toString().trim();
				if (TextUtils.isEmpty(password)
						|| TextUtils.isEmpty(password_confirm)) {
					Toast.makeText(MainActivity.this, "密码不能为空", 0).show();
					return;
				}
				if (password.equals(password_confirm)) {
					Editor editor = sp.edit();
					editor.putString("applockpassword",
							MD5Utils.md5Encode(password));
					editor.commit();
					dialog.dismiss();
					Toast.makeText(MainActivity.this, "密码设置成功", 0).show();

				} else {
					Toast.makeText(MainActivity.this, "两次密码输入不一致", 0).show();
					et_password.setText("");
					et_password_confirm.setText("");

				}

			}
		});
		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

	}

	protected boolean isPaswordSet() {
		String password = sp.getString("applockpassword", "");
		return TextUtils.isEmpty(password);
	}

	@Override
	protected void onStart() {

		if (isPaswordSet()) {
			showPasswordSetDialog();
		} else {
			showPasswordInputDialog();
		}
		if (ServiceUtils.isRunningService(this,
				"com.yang.applock.service.WatchDogService")) {
			ftv_status.setText("程序锁已开启，请点击右上角关闭!      程序锁已开启，请点击右上角关闭!");
			bt_setting.setText("关闭");
		} else {
			ftv_status.setText("程序锁已关闭，请点击右上角开启!      程序锁已关闭，请点击右上角开启!");
			bt_setting.setText("开启");
		}
		super.onStart();
	}

}