package com.example.AppSafe.sound;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.AppSafe.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;

public class SoundSet extends Activity implements OnClickListener {
	private static final String TAG = SoundSet.class.getSimpleName();

	private static final int PWD_TYPE_NUM = 3;

	private static final InitListener InitListener = null;
	private int mPwdType = PWD_TYPE_NUM;
	private SpeakerVerifier mVerifier;
	private String mAuthId = "";
	private String mNumPwd = "";
	private String[] mNumPwdSegs;

	private TextView mShowPwdTextView;
	private TextView mShowMsgTextView;
	private TextView mShowRegFbkTextView;
	private TextView mRecordTimeTextView;
	private AlertDialog mTextPwdSelectDialog;
	private Toast mToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_sound_set);

		initUi();

		mVerifier = SpeakerVerifier.createVerifier(SoundSet.this,
				new InitListener() {

					@Override
					public void onInit(int errorCode) {
						if (ErrorCode.SUCCESS == errorCode) {
							showTip("引擎初始化成功");
						} else {
							showTip("引擎初始化失败，错误码：" + errorCode);
						}
					}
				});
	}

	@SuppressLint("ShowToast")
	private void initUi() {
		mShowPwdTextView = (TextView) findViewById(R.id.showPwd);
		mShowMsgTextView = (TextView) findViewById(R.id.showMsg);
		mShowRegFbkTextView = (TextView) findViewById(R.id.showRegFbk);
		mRecordTimeTextView = (TextView) findViewById(R.id.recordTime);

		findViewById(R.id.isv_register).setOnClickListener(SoundSet.this);
		findViewById(R.id.isv_verify1).setOnClickListener(SoundSet.this);
		findViewById(R.id.isv_cancel1).setOnClickListener(SoundSet.this);
		findViewById(R.id.isv_getpassword).setOnClickListener(SoundSet.this);

		mToast = Toast.makeText(SoundSet.this, "", Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
	}

	private void initTextView() {
		mNumPwd = null;
		mShowPwdTextView.setText("");
		mShowMsgTextView.setText("");
		mShowRegFbkTextView.setText("");
		mRecordTimeTextView.setText("");
	}

	private void performModelOperation(String operation, SpeechListener listener) {
		mVerifier.setParameter(SpeechConstant.PARAMS, null);
		mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);

		mVerifier.sendRequest(operation, mAuthId, listener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.isv_getpassword:
			mVerifier.cancel();
			initTextView();
			mVerifier.setParameter(SpeechConstant.PARAMS, null);
			mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
			mVerifier.getPasswordList(mPwdListenter);
			break;
		case R.id.isv_register:
			mVerifier.setParameter(SpeechConstant.PARAMS, null);
			mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH, Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/msc/test.pcm");
			if (TextUtils.isEmpty(mNumPwd)) {
				showTip("请获取密码后进行操作");
				return;
			}
			mVerifier.setParameter(SpeechConstant.ISV_PWD, mNumPwd);
			((TextView) findViewById(R.id.showPwd)).setText(mNumPwd.substring(
					0, 8));
			mShowMsgTextView.setText("训练 第" + 1 + "遍，剩余4遍");
			mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
			mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
			mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
			mVerifier.startListening(mRegisterListener);
			break;
		case R.id.isv_verify1:
			((TextView) findViewById(R.id.showMsg)).setText("");
			mVerifier.setParameter(SpeechConstant.PARAMS, null);
			mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH, Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/msc/verify.pcm");
			mVerifier = SpeakerVerifier.getVerifier();
			mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
			String verifyPwd = mVerifier.generatePassword(8);
			mVerifier.setParameter(SpeechConstant.ISV_PWD, verifyPwd);
			((TextView) findViewById(R.id.showPwd)).setText(verifyPwd);
			mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
			mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
			mVerifier.startListening(mVerifyListener);
			break;
		case R.id.isv_cancel1:
			mVerifier.cancel();
			initTextView();
			break;
		default:
			break;
		}
	}

	private SpeechListener mPwdListenter = new SpeechListener() {
		@Override
		public void onEvent(int eventType, Bundle params) {
		}

		@Override
		public void onBufferReceived(byte[] buffer) {

			String result = new String(buffer);
			StringBuffer numberString = new StringBuffer();
			try {
				JSONObject object = new JSONObject(result);
				if (!object.has("num_pwd")) {
					initTextView();
					return;
				}

				JSONArray pwdArray = object.optJSONArray("num_pwd");
				numberString.append(pwdArray.get(0));
				for (int i = 1; i < pwdArray.length(); i++) {
					numberString.append("-" + pwdArray.get(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mNumPwd = numberString.toString();
			mNumPwdSegs = mNumPwd.split("-");
		}

		@Override
		public void onCompleted(SpeechError error) {

			if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
				showTip("获取失败：" + error.getErrorCode());
			}
		}
	};

	private SpeechListener mModelOperationListener = new SpeechListener() {

		@Override
		public void onEvent(int eventType, Bundle params) {
		}

		@Override
		public void onBufferReceived(byte[] buffer) {

			String result = new String(buffer);
			try {
				JSONObject object = new JSONObject(result);
				String cmd = object.getString("cmd");
				int ret = object.getInt("ret");

				if ("del".equals(cmd)) {
					if (ret == ErrorCode.SUCCESS) {
						showTip("删除成功");
					} else if (ret == ErrorCode.MSP_ERROR_FAIL) {
						showTip("删除失败，模型不存在");
					}
				} else if ("que".equals(cmd)) {
					if (ret == ErrorCode.SUCCESS) {
						showTip("模型存在");
					} else if (ret == ErrorCode.MSP_ERROR_FAIL) {
						showTip("模型不存在");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onCompleted(SpeechError error) {

			if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
				showTip("操作失败：" + error.getPlainDescription(true));
			}
		}
	};

	private VerifierListener mVerifyListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据：" + data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			mShowMsgTextView.setText(result.source);

			if (result.ret == 0) {
				mShowMsgTextView.setText("验证通过");
			} else {
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mShowMsgTextView.setText("内核异常");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mShowMsgTextView.setText("出现截幅");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mShowMsgTextView.setText("太多噪音");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mShowMsgTextView.setText("录音太短");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mShowMsgTextView.setText("验证不通过，您所读的文本不一致");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mShowMsgTextView.setText("音量太低");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mShowMsgTextView.setText("音频长达不到自由说的要求");
					break;
				default:
					mShowMsgTextView.setText("验证不通过");
					break;
				}
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
		}

		@Override
		public void onError(SpeechError error) {

			switch (error.getErrorCode()) {
			case ErrorCode.MSP_ERROR_NOT_FOUND:
				mShowMsgTextView.setText("模型不存在，请先注册");
				break;

			default:
				showTip("onError Code：" + error.getPlainDescription(true));
				break;
			}
		}

		@Override
		public void onEndOfSpeech() {
			showTip("结束说话");
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}
	};
	private VerifierListener mRegisterListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据：" + data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			((TextView) findViewById(R.id.showMsg)).setText(result.source);

			if (result.ret == ErrorCode.SUCCESS) {
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mShowMsgTextView.setText("内核异常");
					break;
				case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
					mShowRegFbkTextView.setText("训练达到最大次数");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mShowRegFbkTextView.setText("出现截幅");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mShowRegFbkTextView.setText("太多噪音");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mShowRegFbkTextView.setText("录音太短");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mShowRegFbkTextView.setText("训练失败，您所读的文本不一致");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mShowRegFbkTextView.setText("音量太低");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mShowMsgTextView.setText("音频长达不到自由说的要求");
				default:
					mShowRegFbkTextView.setText("");
					break;
				}

				if (result.suc == result.rgn) {
					mShowMsgTextView.setText("注册成功");


				} else {
					int nowTimes = result.suc + 1;
					int leftTimes = result.rgn - nowTimes;

					mShowPwdTextView.setText(mNumPwdSegs[nowTimes - 1]);

					mShowMsgTextView.setText("训练 第" + nowTimes + "遍，剩余"
							+ leftTimes + "遍");
				}

			} else {

				mShowMsgTextView.setText("注册失败，请重新开始。");
			}

		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
		}

		@Override
		public void onError(SpeechError error) {

			if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
				showTip("模型已存在，如需重新注册，请先删除");
			} else {
				showTip("onError Code：" + error.getPlainDescription(true));
			}
		}

		@Override
		public void onEndOfSpeech() {
			showTip("结束说话");
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}
	};

	@Override
	public void finish() {
		if (null != mTextPwdSelectDialog) {
			mTextPwdSelectDialog.dismiss();
		}
		super.finish();
	}

	@Override
	protected void onDestroy() {
		if (null != mVerifier) {
			mVerifier.stopListening();
			mVerifier.destroy();
		}
		super.onDestroy();
	}

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

}
