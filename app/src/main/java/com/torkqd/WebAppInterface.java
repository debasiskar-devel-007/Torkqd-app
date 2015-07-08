package com.torkqd;

import android.content.Context;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Created by KTA-PC 21 on 3/30/2015.
 */
public class WebAppInterface extends MainActivity  {
    Context mContext;
    int TAKE_PHOTO_CODE = 0;
    public static int count=0;
    private WebView myWebView;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {


        //View button= view.findViewById(R.id.button);
        Context context = getApplicationContext();
        CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast1 = Toast.makeText(context, text, duration);
        toast1.show();
        myWebView = (WebView) findViewById(R.id.webView1);
        myWebView.setVisibility(View.INVISIBLE);


    }








}
