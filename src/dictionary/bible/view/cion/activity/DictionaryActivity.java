package dictionary.bible.view.cion.activity;

import com.admixer.AdAdapter;
import com.admixer.AdInfo;
import com.admixer.AdMixerManager;
import com.admixer.InterstitialAd;
import com.admixer.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import dictionary.bible.view.cion.R;
import dictionary.bible.view.cion.util.Crypto;
import dictionary.bible.view.cion.util.Utils;

public class DictionaryActivity extends Activity implements InterstitialAdListener {
	private Context context;;
	private boolean retry_alert = false;
	private Handler handler = new Handler();
	private NativeExpressAdView admobNative;
	private WebView webview;
	private com.admixer.InterstitialAd interstialAd;
	private TextView txt_dictionary_title;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dictionary);
		txt_dictionary_title = (TextView)findViewById(R.id.txt_dictionary_title);
		context = this;
		AdMixerManager.getInstance().setAdapterDefaultAppCode(AdAdapter.ADAPTER_ADMIXER, "aq03oj5h");
		AdMixerManager.getInstance().setAdapterDefaultAppCode(AdAdapter.ADAPTER_ADMOB, "ca-app-pub-4637651494513698/6004452964");
		AdMixerManager.getInstance().setAdapterDefaultAppCode(AdAdapter.ADAPTER_ADMOB_FULL, "ca-app-pub-4637651494513698/7481186160");
		
		RelativeLayout nativeContainer = (RelativeLayout) findViewById(R.id.admob_native);
		AdRequest adRequest = new AdRequest.Builder().build();	    
		admobNative = new NativeExpressAdView(this);
		admobNative.setAdSize(new AdSize(360, 100));
		admobNative.setAdUnitId("ca-app-pub-4637651494513698/8957919362");
		nativeContainer.addView(admobNative);
		admobNative.loadAd(adRequest);
		
		retry_alert = true;
		display_list();
	}
	
	private void display_list(){
		String get_name = getIntent().getStringExtra("name");
		txt_dictionary_title.setText(get_name);
		webview = new WebView(this);
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.setVerticalScrollbarOverlay(true);
		webview.setVerticalScrollBarEnabled(true);
		webview.setWebViewClient(new WebViewClientClass());		
		webview.setWebChromeClient(new WebChromeClientClass());
		try {
			webview.loadUrl(Crypto.decrypt(Utils.data, context.getString(R.string.txt_str0)) + get_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		admobNative.pause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		admobNative.resume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		admobNative.destroy();
		retry_alert = false;
		finish();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		display_list();
	}
	
	private class WebChromeClientClass extends WebChromeClient { 
		ProgressBar pb_item01 = (ProgressBar) findViewById(R.id.pb_item01);
		@Override
		public void onProgressChanged(WebView view, int progress) {
			super.onProgressChanged(view, progress);
			pb_item01.setProgress(progress); // ProgressBar값 설정
			if (progress == 100) { // 모두 로딩시 Progressbar를 숨김
				pb_item01.setVisibility(View.GONE);
			} else {
				pb_item01.setVisibility(View.VISIBLE);
			}
		}
		public WebChromeClientClass() {
		}
	}
	
	private class WebViewClientClass extends WebViewClient{
    private WebViewClientClass() {
    }
    
    public void onPageFinished(WebView paramWebView, String paramString){
      super.onPageFinished(paramWebView, paramString);
    }
    
    public void onPageStarted(WebView paramWebView, String paramString, Bitmap paramBitmap){
      super.onPageStarted(paramWebView, paramString, paramBitmap);
    }
    
    
    public void onReceivedError(WebView paramWebView, int paramInt, String paramString1, String paramString2){
//      finish();
//      Toast.makeText(context, context.getString(R.string.txt_network_error), Toast.LENGTH_SHORT).show();
    	NetworkErrorAlertShow(context.getString(R.string.txt_network_error));
    }
    
    public boolean shouldOverrideUrlLoading(WebView webview, String url){
    	if (url.indexOf("/bible/kor") != -1) {
    		try{
    			Intent localIntent2 = getPackageManager().getLaunchIntentForPackage("com.good.worshipbible.nos");
    			startActivity(localIntent2);
    			return true;
    		} catch (NullPointerException localNullPointerException) {
    			GoMarketAlertShow(context.getString(R.string.txt_goodworshipbible_msg));
    			return true;
    		}
    	}
    	webview.loadUrl(url);
    	return true;
    }
    
    public boolean GoMarketAlertShow(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle(context.getString(R.string.txt_goodworshipbible_title));
		builder.setMessage(msg);
		builder.setInverseBackgroundForced(true);
		builder.setNeutralButton(context.getString(R.string.txt_confirm), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton){
				Intent localIntent1 = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.good.worshipbible.nos"));
    			startActivity(localIntent1);
			}
		});
		builder.setNegativeButton(context.getString(R.string.txt_cancel), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton){
				display_list();
				dialog.dismiss();
			}
		});
		AlertDialog myAlertDialog = builder.create();
		if(retry_alert) myAlertDialog.show();
		return true;
	}
    
    public void NetworkErrorAlertShow(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setMessage(msg);
		builder.setInverseBackgroundForced(true);
		builder.setNeutralButton(context.getString(R.string.txt_confirm), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton){
				gotoSettingNetwork();
			}
		});
		builder.setNegativeButton(context.getString(R.string.txt_cancel), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton){
				finish();
			}
		});
		AlertDialog myAlertDialog = builder.create();
		if(retry_alert) myAlertDialog.show();
	}
    
    public void gotoSettingNetwork() {
		Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
  }
	public void addInterstitialView() {
    	if(interstialAd == null) {
        	AdInfo adInfo = new AdInfo("aq03oj5h");
//        	adInfo.setTestMode(false);
        	interstialAd = new com.admixer.InterstitialAd(this);
        	interstialAd.setAdInfo(adInfo, this);
        	interstialAd.setInterstitialAdListener(this);
        	interstialAd.startInterstitial();
    	}
    }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			Toast.makeText(context, context.getString(R.string.txt_after_ad), Toast.LENGTH_LONG).show();
			addInterstitialView();
			 handler.postDelayed(new Runnable() {
				 @Override
				 public void run() {
					 onDestroy();
					
				 }
			 },3000);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onInterstitialAdClosed(InterstitialAd arg0) {
		interstialAd = null;
		onDestroy();
	}

	@Override
	public void onInterstitialAdFailedToReceive(int arg0, String arg1, InterstitialAd arg2) {
		interstialAd = null;
	}

	@Override
	public void onInterstitialAdReceived(String arg0, InterstitialAd arg1) {
		interstialAd = null;
	}

	@Override
	public void onInterstitialAdShown(String arg0, InterstitialAd arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeftClicked(String arg0, InterstitialAd arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRightClicked(String arg0, InterstitialAd arg1) {
		// TODO Auto-generated method stub
		
	}
}
