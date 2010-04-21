package edwardawebb.queueman.core;


import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class AboutHelp extends Activity {
	WebView mWebView;
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about_help);

	    mWebView = (WebView) findViewById(R.id.abouthelp);
	    mWebView.getSettings().setJavaScriptEnabled(true);
	    
	    //now turn text file into input stream to be handled as html
		Resources resources = getResources();
		InputStream is = null;
		try {
			is = resources.openRawResource(R.raw.about);
			byte[] reader = new byte[is.available()];
			while (is.read(reader) != -1) {
			}
			//this.readOutput.setText(Html.fromHtml(Html.toHtml(new String(reader))));
			mWebView.loadData((new String(reader)), "text/html","utf-8");
			mWebView.setWebViewClient(new HelloWebViewClient());
		} catch (IOException e) {
			FlurryAgent.onError("About", "Failed to load text as html", "AboutHelp");
			Toast.makeText(this, "Sorry, unable to load, try again", Toast.LENGTH_LONG).show();
		}
	}
	
	
	
/*	 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
	        mWebView.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}*/
	
	
	private class HelloWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if(url.toString().contains("youtube")){
	        	//let the android youtube synergy do their thing...
	        	startActivity( new Intent(Intent.ACTION_VIEW,Uri.parse(url)));	
	        	return true;
	        }else{
	        	//we are using intenral links to jump around the page
		    	view.loadUrl(url);
		        Log.d("AboutHelp","Not Youtube: "+url.toString());
		        return true;
	        }
	    }
	}
}
