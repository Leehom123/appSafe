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
							showTip("�����ʼ���ɹ�");
						} else {
							showTip("�����ʼ��ʧ�ܣ������룺" + errorCode);
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
				showTip("���ȡ�������в���");
				return;
			}
			mVerifier.setParameter(SpeechConstant.ISV_PWD, mNumPwd);
			((TextView) findViewById(R.id.showPwd)).setText(mNumPwd.substring(
					0, 8));
			mShowMsgTextView.setText("ѵ�� ��" + 1 + "�飬ʣ��4��");
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
				showTip("��ȡʧ�ܣ�" + error.getErrorCode());
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
						showTip("ɾ���ɹ�");
					} else if (ret == ErrorCode.MSP_ERROR_FAIL) {
						showTip("ɾ��ʧ�ܣ�ģ�Ͳ�����");
					}
				} else if ("que".equals(cmd)) {
					if (ret == ErrorCode.SUCCESS) {
						showTip("ģ�ʹ���");
					} else if (ret == ErrorCode.MSP_ERROR_FAIL) {
						showTip("ģ�Ͳ�����");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onCompleted(SpeechError error) {

			if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
				showTip("����ʧ�ܣ�" + error.getPlainDescription(true));
			}
		}
	};

	private VerifierListener mVerifyListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("��ǰ����˵����������С��" + volume);
			Log.d(TAG, "������Ƶ���ݣ�" + data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			mShowMsgTextView.setText(result.source);

			if (result.ret == 0) {
				mShowMsgTextView.setText("��֤ͨ��");
			} else {
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mShowMsgTextView.setText("�ں��쳣");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mShowMsgTextView.setText("���ֽط�");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mShowMsgTextView.setText("̫������");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mShowMsgTextView.setText("¼��̫��");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mShowMsgTextView.setText("��֤��ͨ�������������ı���һ��");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mShowMsgTextView.setText("����̫��");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mShowMsgTextView.setText("��Ƶ���ﲻ������˵��Ҫ��");
					break;
				default:
					mShowMsgTextView.setText("��֤��ͨ��");
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
				mShowMsgTextView.setText("ģ�Ͳ����ڣ�����ע��");
				break;

			default:
				showTip("onError Code��" + error.getPlainDescription(true));
				break;
			}
		}

		@Override
		public void onEndOfSpeech() {
			showTip("����˵��");
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("��ʼ˵��");
		}
	};
	private VerifierListener mRegisterListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("��ǰ����˵����������С��" + volume);
			Log.d(TAG, "������Ƶ���ݣ�" + data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			((TextView) findViewById(R.id.showMsg)).setText(result.source);

			if (result.ret == ErrorCode.SUCCESS) {
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mShowMsgTextView.setText("�ں��쳣");
					break;
				case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
					mShowRegFbkTextView.setText("ѵ���ﵽ������");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mShowRegFbkTextView.setText("���ֽط�");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mShowRegFbkTextView.setText("̫������");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mShowRegFbkTextView.setText("¼��̫��");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mShowRegFbkTextView.setText("ѵ��ʧ�ܣ����������ı���һ��");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mShowRegFbkTextView.setText("����̫��");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mShowMsgTextView.setText("��Ƶ���ﲻ������˵��Ҫ��");
				default:
					mShowRegFbkTextView.setText("");
					break;
				}

				if (result.suc == result.rgn) {
					mShowMsgTextView.setText("ע��ɹ�");


				} else {
					int nowTimes = result.suc + 1;
					int leftTimes = result.rgn - nowTimes;

					mShowPwdTextView.setText(mNumPwdSegs[nowTimes - 1]);

					mShowMsgTextView.setText("ѵ�� ��" + nowTimes + "�飬ʣ��"
							+ leftTimes + "��");
				}

			} else {

				mShowMsgTextView.setText("ע��ʧ�ܣ������¿�ʼ��");
			}

		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
		}

		@Override
		public void onError(SpeechError error) {

			if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
				showTip("ģ���Ѵ��ڣ���������ע�ᣬ����ɾ��");
			} else {
				showTip("onError Code��" + error.getPlainDescription(true));
			}
		}

		@Override
		public void onEndOfSpeech() {
			showTip("����˵��");
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("��ʼ˵��");
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
