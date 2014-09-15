package com.OsMoDroid;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AuthActivity extends Activity {

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authwebview);
		 
		webView = (WebView) findViewById(R.id.authWebView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		       
		            view.loadUrl(url);
		       
		        return true;
		    }

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.d(getClass().getSimpleName(), "open url = "+url);
				if(url.contains("z.osmo.mobi"))
				{
					
						Uri uri = Uri.parse(url);
						String u = uri.getQueryParameter("u");
						String p = uri.getQueryParameter("p");
						Log.d(getClass().getSimpleName(), "u = "+u + " p="+p);
						OsMoDroid.editor.putString("p", p);
						OsMoDroid.editor.putString("u", u);
						OsMoDroid.editor.commit();
						if (getParent() == null) {
						    setResult(Activity.RESULT_OK);
						} else {
						    getParent().setResult(Activity.RESULT_OK);
						}
						Toast.makeText(getApplicationContext(), "Authed OK", Toast.LENGTH_LONG);
						finish();
						
					
				}
			}
		   
		});
		webView.loadUrl("http://osmo.mobi/signin?type=z");
 
	}



}
