package com.example.judongseok.unicity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * WebViewInterface.java
 *
 * MainActivity의 WebView에서 사용하는 Class.<br>
 * 웹뷰와 네이티브간 Bridge를 형성해준다.
 *
 * @auther jong-hyun.jeong
 * @date 2014. 8. 21.
 */
public class WebViewInterface {

	public static final int GOOGLE_STT = 1001;

	public static final int REQUEST_SETTING = 2001;

	private WebView mAppView;
	private Activity mContext;
	private TelephonyManager mTelephonyManager;
	private Intent mIntent;
	private int mSelectedIndex;

	private String mSttTargetId;

	public WebViewInterface(Activity activity, WebView view) {
		mAppView = view;
		mContext = activity;
		mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	public WebViewInterface(Activity activity, WebView view, Intent intent) {
		mAppView = view;
		mContext = activity;
		mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		mIntent = intent;
	}

	// Show toast for a long time
	/**
	 * 안드로이드 백키 이벤트 제어
	 */
	@JavascriptInterface
	public void back() {
		mContext.onBackPressed();
	}

	/**
	 * 앱종료
	 */
	@JavascriptInterface
	public void finish() {
		mContext.finish();
	}

	// Show toast for a long time
	/**
	 * 안드로이드 토스트를 출력한다. Time Long.
	 * @param message : 메시지
	 */
	@JavascriptInterface
	public void toastLong (String message) {
		Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
	}
	/**
	 * 안드로이드 토스트를 출력한다. Time Short.
	 * @param message : 메시지
	 */
	@JavascriptInterface
	public void toastShort (String message) { // Show toast for a short time
		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 핸드폰 번호를 받아온다.
	 * @return String : 핸드폰 번호.
	 */
//	@JavascriptInterface
//	public String getPhoneNumber () {
//
//		String telephone = mTelephonyManager.getLine1Number();
//		if ( telephone == null ) {
//			return "";
//		}
//		return telephone;
//	}

	// Show toast for a long time
	@JavascriptInterface
	public void toast(int type, String message) {

		if (type == 0) {
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		}
	}

	@JavascriptInterface
	public String getSttTargetId() {
		return mSttTargetId;
	}

	/**
	 * 이메일 정보를 얻어온다.
	 */
	@JavascriptInterface
	public String getEmail() {

		String regExp = "([_0-9a-zA-Z-]+[_a-z0-9-.]{1,})@([a-z0-9-]{2,}.[a-z0-9]{1,}.[a-z0-9]{1,})*";

		AccountManager accountManager = AccountManager.get(mContext);

		Account[] accounts = accountManager.getAccounts();

		String gmail = null;
		String daum = null;
		String hanmail = null;
		String naver = null;


		for ( Account c : accounts ) {

			if ( c.name.contains("gmail") ) {
				if ( gmail == null ) {
					gmail = c.name;
				}
			}
			if ( c.name.contains("daum") ) {
				if ( daum == null ) {
					daum = c.name;
				}
			}
			if ( c.name.contains("hanmail") ) {
				if ( hanmail == null ) {
					hanmail = c.name;
				}
			}
			if ( c.name.contains("naver") ) {
				if ( naver == null ) {
					naver = c.name;
				}
			}
		}

		if ( gmail != null ) {
			return gmail;
		}
		if ( daum != null ) {
			return daum;
		}
		if ( hanmail != null ) {
			return hanmail;
		}
		if ( naver != null ) {
			return naver;
		}
		if ( accounts.length > 0 && accounts[0].name.matches(regExp))  {
			return accounts[0].name;
		}
		return "";
	}
	/**
	 */
	@JavascriptInterface
	public Uri open(String key, String thumbnailId ) {
		WebViewImageUploadHelper.getInstance(mContext, mAppView).open(key, thumbnailId);
		return null;
	}
	/**
	 */
	@JavascriptInterface
	public File send(String jsonString) {
		WebViewImageUploadHelper.getInstance(mContext, mAppView).send(jsonString);
		return null;
	}
}
