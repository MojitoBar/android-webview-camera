package com.example.judongseok.unicity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import com.example.judongseok.unisity.MainActivity;
import com.example.judongseok.unisity.R;


/**
 * 
 * 킷캣에서 이미지 업로드 안되는 현상을 해결하기 위해 만든 클래스<br>
 * 
 * @author jong-hyun.jeong
 * @Since 2014. 4. 14.
 */
public class WebViewImageUploadHelper {

	private final static int INTENT_CALL_GALLERY = 3001;
	private final static int INTENT_CALL_CAMERA = 4001;
	

	public static final int KITKAT_FILECHOOSER = 10002;
	public static final int KITKAT_CAMERA = 10003;

	private static WebViewImageUploadHelper mHelper;

	private Context mContext;

	private static HashMap<String, File> mContent;

	private String mKey;
	private String mThumbnailId;
	private WebView mWebView;
	private File mTempFile;
	private OnClickListener mDialogCallbackListener;

	private WebViewImageUploadHelper(Context context) {
		mContext = context;
		mDialogCallbackListener = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (which == 0) {
					callCamera(INTENT_CALL_CAMERA);
				} else if (which == 1) {
					callGallery(INTENT_CALL_GALLERY);
				}
			}
		};
	}

	/**
	 * 생성자
	 * 
	 * @param context
	 *            : activity context
	 * @return
	 */
	public static final WebViewImageUploadHelper getInstance(Context context, WebView webView) {

		if (mHelper == null) {
			mHelper = new WebViewImageUploadHelper(context);
			mHelper.mWebView = webView;
			mContent = new HashMap<String, File>();
		}
		return mHelper;

	}

	/**
	 * 피커 열기.
	 * @param key : param key
	 * @param thumbnailId : thumbnail을 적용시킬 이미지.
	 */
	public final void open( String key, String thumbnailId) {

		
		mKey = key;
		mThumbnailId = thumbnailId;

//		Intent i = new Intent(Intent.ACTION_PICK);
////		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//		// i.addCategory(Intent.CATEGORY_DEFAULT);
////		i.addCategory(Intent.CATEGORY_OPENABLE);
//		i.setType("image/*");
//		((MainActivity) mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"),
//				KITKAT_FILECHOOSER);
		
		String[] strings = ((MainActivity) mContext).getResources().getStringArray(R.array.select_picture);
		String[] items = null;
		items = new String[] { strings[0], strings[1] } ;
		
//		mDialog.showListDialog(null, items, mDialogCallbackListener);

	}
	
	/**
	 * 갤러리를 호출한다.
	 */
	public void callGallery(int requestCode) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setType("image/*");
		// intent.setAction(Intent.ACTION_CHOOSER ); // 이거랑 아래꺼 소스가 갤러리 접근 소스
		// intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE
		// );
		((MainActivity) mContext).startActivityForResult(Intent.createChooser(intent, "File Chooser"), KITKAT_FILECHOOSER);
	}

	/**
	 * 카메라를 호출한다.
	 */
	public void callCamera(int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File directory = new File(Constant.DIRECTORY_PHOTO_PATH);
		if (!directory.exists()) {
			directory.mkdir();
		}

		directory.mkdir();
		mTempFile = new File(directory, "photo_" + new Date().getTime() + ".jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
		
		((MainActivity) mContext).startActivityForResult(intent, KITKAT_CAMERA);
	}

	public final boolean send(final String jsonString) {

		
		//Task를 실행시킨다. Thread를 돌리지 않으면, ANR로 앱이 강제종료될 수 있기 때문.
		new AsyncTask<Void, Void, Boolean>() {
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			private String mResponse;
			
			@Override
			protected Boolean doInBackground(Void... params) {
				//TODO : 서버로 전송하는 코드. 해시맵에서 값을 꺼내서....조합...한번에 던짐..
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if ( result ) {
					mWebView.loadUrl("javascript:alert(\"정상적으로 이미지가 등록되었습니다..\");");
				} else {
					mWebView.loadUrl("javascript:alert(\"정상적으로 이미지가 등록되지 않았습니다.<br> 다시 시도해 주시기 바랍니다.\");");
					
				}
			}
		}.execute();

		return true;
	}

	/**
	 * 갤러리로부터 받은 파일정보를 통해 웹뷰 화면을 업데이트 한다.
	 * @param uri : file path
	 */
	public final void updateContent(Uri uri) {
		if (uri == null) {
			return;
		}
		
		File file = uriToFile(uri);
		
		String type = getMimeType(uri);
		
		// 파일 path 저장
		mContent.put(mKey, file);

		// 웹뷰로 썸네일 보냄.
		updateImage(file, type);

	}
	/**
	 * 카메라로부터 받은 파일정보를 통해 웹뷰 화면을 업데이트 한다.
//	 * @param uri : file path
	 */
	public final void updateContent() {
		Uri uri = Uri.fromFile(mTempFile);
		
		// 미디어 스캐닝 실행.
		((MainActivity) mContext).sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
		
		File file = mTempFile;
		
		String type = getMimeType(uri);
		
		// 파일 path 저장
		
		mContent.put(mKey, file);
		
		// 웹뷰로 썸네일 보냄.
		updateImage(file, type);
		
		mTempFile = null;
		
	}

	/**
	 * 파일을 업로드한다.<br>
	 * MainActivity에서 불려진다.
//	 * @param webview
	 * @param file
	 * @param type
	 */
	private void updateImage(final File file,final String type) {

		//TODO : Spinner 추가.
		//Task를 실행시킨다. Thread를 돌리지 않으면, ANR로 앱이 강제종료될 수 있기 때문.
		new AsyncTask<Void, Void, String>() {
			
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected String doInBackground(Void... params) {
				String mimeType = type;
				String base64EncodedImage = fileToString(file);
				return "javascript:$(" + "\"#" + mThumbnailId + "\"" + ").attr(\"src\", " + "\"data:" + mimeType + ";base64," + base64EncodedImage + "\");";
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				mWebView.loadUrl(result);
				clearId();
			}
		}.execute();
	}
	
	/**
	 * 화면 세팅을 위해. mimetype을 알아낸다.
	 * @param uri
	 * @return
	 */
	public String getMimeType(Uri uri) {
		ContentResolver cR = mContext.getContentResolver();
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String type = cR.getType(uri);
//		String type = mime.getExtensionFromMimeType(cR.getType(uri));
	    return type;
	}
	
	/**
	 * Only support kitkat<br>
	 * uri 를 file로 변환
	 * @param uri
	 * @return
	 */
//	private File uriToFile ( Uri uri ) {
//		String id = uri.getLastPathSegment(); 
//	    final String[] imageColumns = {MediaStore.Images.Media.DATA };
//	    final String imageOrderBy = null;
//
//	    String selectedImagePath = "path";
//	    String scheme = uri.getScheme();
//	    if ( scheme.equalsIgnoreCase("content") ) {
//	    	 Cursor imageCursor = mContext.getContentResolver().query(uri, imageColumns, null, null, null);
//
//			    if (imageCursor.moveToFirst()) {
//			        selectedImagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
//			    }
//	    } else {
//	    	selectedImagePath = uri.getPath();
//	    }
//	    
//	    File file = new File( selectedImagePath );
//	    
//	    return file;
//	}
	
	/**
	 * 카메라 또는 갤러리로부터 받은 url정보를 file 정보로 변환한다.
	 * @param uri 
	 * @return
	 */
	@TargetApi(19)
	private File uriToFile ( Uri uri ) {
		
		String filePath = "";
		
		if ( uri.getPath().contains(":") ) {
			//:이 존재하는 경우		
	
			String wholeID = DocumentsContract.getDocumentId(uri);
	
			// Split at colon, use second item in the array
			String id = wholeID.split(":")[1];
	
			String[] column = { MediaStore.Images.Media.DATA };     
	
			// where id is equal to             
			String sel = MediaStore.Images.Media._ID + "=?";
	
			Cursor cursor = mContext.getContentResolver().
			                          query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
			                          column, sel, new String[]{ id }, null);
	
	
			int columnIndex = cursor.getColumnIndex(column[0]);
	
			if (cursor.moveToFirst()) {
			    filePath = cursor.getString(columnIndex);
			}   
	
			cursor.close();
			
		} else {
			//:이 존재하지 않을경우
			String id = uri.getLastPathSegment(); 
		    final String[] imageColumns = {MediaStore.Images.Media.DATA };
		    final String imageOrderBy = null;
	
		    String selectedImagePath = "path";
		    String scheme = uri.getScheme();
		    if ( scheme.equalsIgnoreCase("content") ) {
		    	 Cursor imageCursor = mContext.getContentResolver().query(uri, imageColumns, null, null, null);
	
				    if (imageCursor.moveToFirst()) {
				    	filePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
				    }
		    } else {
		    	filePath = uri.getPath();
		    }
		}
	    
	    File file = new File( filePath );
	    
	    return file;
	}
	

	/**
	 * 데이터들을 모두 제거한다.
	 */
	public final void clear() {
		mContent.clear();
		mKey = null;
		mThumbnailId = null;
	}

	/**
	 * 아이디 제거.
	 */
	public final void clearId() {
		mKey = null;
		mThumbnailId = null;
	}

	/**
	 * file 정보를 base64로 인코딩한다.
	 * @param file : target file
	 * @return
	 */
	public String fileToString(File file) {
		
		String fileString = new String();
		FileInputStream inputStream = null;
		ByteArrayOutputStream byteOutStream = null;

		try {
			inputStream = new FileInputStream(file);
			byteOutStream = new ByteArrayOutputStream();

			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = inputStream.read(buf)) != -1) {
				byteOutStream.write(buf, 0, len);
			}

			byte[] fileArray = byteOutStream.toByteArray();
			fileString = new String(Base64.encodeToString(fileArray, Base64.NO_WRAP));

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
				byteOutStream.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return fileString;
	}
}
