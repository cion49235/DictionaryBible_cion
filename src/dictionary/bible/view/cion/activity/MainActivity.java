package dictionary.bible.view.cion.activity;

import java.util.ArrayList;

import com.admixer.AdAdapter;
import com.admixer.AdInfo;
import com.admixer.AdMixerManager;
import com.admixer.AdView;
import com.admixer.AdViewListener;
import com.admixer.CustomPopup;
import com.admixer.CustomPopupListener;
import com.admixer.InterstitialAd;
import com.admixer.InterstitialAdListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import dictionary.bible.view.cion.R;
import dictionary.bible.view.cion.data.DictionnaryList;
import dictionary.bible.view.cion.data.Main_Data;
import dictionary.bible.view.cion.util.KoreanTextMatch;
import dictionary.bible.view.cion.util.KoreanTextMatcher;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener, OnScrollListener, AdViewListener, CustomPopupListener, InterstitialAdListener {
	private ListView listview_main;
	private DictionnaryList dl;
	private EditText edit_searcher;
	private String searchKeyword;
	private MainAdapter main_adapter;
	private ArrayList<Main_Data> list;
	private Main_Data main_data;
	private Handler handler = new Handler();
	private boolean flag;
	private Context context;
	private ImageButton btn_close;
	private LinearLayout layout_nodata;
	KoreanTextMatch match1, match2;
	public static AlertDialog alertDialog;
	private RelativeLayout ad_layout;
	private com.admixer.InterstitialAd interstialAd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		AdMixerManager.getInstance().setAdapterDefaultAppCode(AdAdapter.ADAPTER_ADMIXER, "aq03oj5h");
		AdMixerManager.getInstance().setAdapterDefaultAppCode(AdAdapter.ADAPTER_ADMOB, "ca-app-pub-4637651494513698/6004452964");
		AdMixerManager.getInstance().setAdapterDefaultAppCode(AdAdapter.ADAPTER_ADMOB_FULL, "ca-app-pub-4637651494513698/7481186160");
		
//		  Custom Popup 시작
		CustomPopup.setCustomPopupListener(this);
		CustomPopup.startCustomPopup(this, "aq03oj5h");
		
		addBannerView();
		
		context = this;
		init_ui();
		display_list();
		seacher_start();
		exit_handler();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		edit_searcher.setText("");
		// Custom Popup 종료
		CustomPopup.stopCustomPopup();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
//		addInterstitialView();
	}
	
	public void addBannerView() {
    	AdInfo adInfo = new AdInfo("aq03oj5h");
    	adInfo.setTestMode(false);
        com.admixer.AdView adView = new com.admixer.AdView(this);
        adView.setAdInfo(adInfo, this);
        adView.setAdViewListener(this);
        ad_layout = (RelativeLayout)findViewById(R.id.ad_layout);
        if(ad_layout != null){
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ad_layout.addView(adView, params);	
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
	
	public void exit_handler(){
    	handler = new Handler(){
    		@Override
    		public void handleMessage(Message msg) {
    			if(msg.what == 0){
    				flag = false;
    			}
    		}
    	};
    }
	
	private void init_ui(){
		layout_nodata = (LinearLayout)findViewById(R.id.layout_nodata);
		edit_searcher = (EditText)findViewById(R.id.edit_searcher);
		btn_close = (ImageButton)findViewById(R.id.btn_close);
		btn_close.setOnClickListener(this);
		listview_main = (ListView)findViewById(R.id.listview_main);
		listview_main.setOnScrollListener(this);
		listview_main.setOnItemClickListener(this);
	}
	
	private void seacher_start(){
		edit_searcher.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable arg0) {
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					searchKeyword = s.toString();
					display_list();
					if(main_adapter != null){
						main_adapter.notifyDataSetChanged();
					}
					if(s.length() == 0){
						btn_close.setVisibility(View.INVISIBLE);
					}else{
						btn_close.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
				}
			}
		});
	}
	
	private void display_list(){
		list = new ArrayList<Main_Data>();
		dl = new DictionnaryList();
		for(int i=1; i < dl.wordsmax; i++){
			main_data = new Main_Data(dl.words[i]);
			if(searchKeyword != null && "".equals(searchKeyword.trim()) == false){
				KoreanTextMatcher matcher1 = new KoreanTextMatcher(searchKeyword.toLowerCase());
				KoreanTextMatcher matcher2 = new KoreanTextMatcher(searchKeyword.toUpperCase());
				match1 = matcher1.match(dl.words[i].toLowerCase());
				match2 = matcher2.match(dl.words[i].toUpperCase());
				if(match1.success()){
					list.add(main_data);
				}else if (match2.success()){
					list.add(main_data);
				}
			}else{
				list.add(main_data);	
			}
		}
		main_adapter = new MainAdapter();
		listview_main.setAdapter(main_adapter);
		if(listview_main.getCount() > 0){
			layout_nodata.setVisibility(View.GONE);
		}else{
			layout_nodata.setVisibility(View.VISIBLE);			
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Main_Data main_data = (Main_Data)main_adapter.getItem(position);
		String name = main_data.getName();
		Intent intent = new Intent(this, DictionaryActivity.class);
		intent.putExtra("name", name);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	
	@Override
	public void onClick(View view) {
		if(view == btn_close){
			edit_searcher.setText("");
			display_list();
			if(main_adapter != null){
				main_adapter.notifyDataSetChanged();
			}
			InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);  
    		inputMethodManager.hideSoftInputFromWindow(edit_searcher.getWindowToken(), 0);
		}
	}
	
	public class MainAdapter extends BaseAdapter{
		public MainAdapter() {
		}
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			try{
				if(view == null){	
					LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
					view = layoutInflater.inflate(R.layout.activity_main_listrow, parent, false);
				}
				
				ImageView img_more = (ImageView)view.findViewById(R.id.img_more);
				img_more.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String name = list.get(position).getName();
						Intent intent = new Intent(context, DictionaryActivity.class);
						intent.putExtra("name", name);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
					}
				});
				img_more.setFocusable(false);
				
				TextView txt_subject = (TextView)view.findViewById(R.id.txt_name);
//				txt_subject.setText(list.get(position).getName());
				setTextViewColorPartial(txt_subject, list.get(position).getName(), searchKeyword, Color.RED);
				
			}catch (Exception e) {
			}finally{
			}
			return view;
		}
		
		private void setTextViewColorPartial(TextView view, String fulltext, String subtext, int color) {
			try{
				view.setText(fulltext, TextView.BufferType.SPANNABLE);
				Spannable str = (Spannable) view.getText();
				int i = fulltext.indexOf(subtext);
				str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}catch (IndexOutOfBoundsException e) {
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode == KeyEvent.KEYCODE_BACK){
			 if(!flag){
				 Toast.makeText(context, context.getString(R.string.txt_back) , Toast.LENGTH_SHORT).show();
				 flag = true;
				 handler.sendEmptyMessageDelayed(0, 2000);
			 return false;
			 }else{
				 try{
					 handler.postDelayed(new Runnable() {
						 @Override
						 public void run() {
							 finish();
						 }
					 },0);
				 }catch(Exception e){
				 }
			 }
            return false;	 
		 }
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == OnScrollListener.SCROLL_STATE_FLING){
			InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
    		inputMethodManager.hideSoftInputFromWindow(edit_searcher.getWindowToken(), 0);
    		listview_main.setFastScrollEnabled(true);
		}else{
			listview_main.setFastScrollEnabled(false);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInterstitialAdClosed(InterstitialAd arg0) {
		// TODO Auto-generated method stub
		
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
	//** CustomPopup 이벤트들 *************
	@Override
	public void onCloseCustomPopup(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHasNoCustomPopup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShowCustomPopup(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartedCustomPopup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWillCloseCustomPopup(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWillShowCustomPopup(String arg0) {
		// TODO Auto-generated method stub
		
	}
	//** BannerAd 이벤트들 *************
	@Override
	public void onClickedAd(String arg0, AdView arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFailedToReceiveAd(int arg0, String arg1, AdView arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivedAd(String arg0, AdView arg1) {
		// TODO Auto-generated method stub
		
	}
}
