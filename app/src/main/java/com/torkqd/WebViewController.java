package com.torkqd;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class WebViewController extends WebViewClient {

    private static final String LOG_TAG = "test";
    ProgressDialog progressDialog;
    private WebView myWebView;
    private Button btn1;


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if(url.contains("http://torqkd.com/upload"))
        {

            /*myWebView = (WebView) view.findViewById(R.id.webView1);
            myWebView.setVisibility(View.INVISIBLE);*/
            Context context = view.getContext();
            Intent cameraintent = new Intent(context, upload.class);

            // Launch default browser
            context.startActivity(cameraintent);
            //btn1 = (Button) findViewById(R.id.btn1);
            //btn1.setVisibility(View.INVISIBLE);
            //MainActivity myActivity = new MainActivity();
            //myActivity.showButton();


           // imgv.setVisibility(View.VISIBLE);

           /* btn = (Button) view.findViewById(R.id.imgcancelbtn);
            btn1 = (Button) view.findViewById(R.id.imguploadbtn);
            tbale = (TableLayout) view.findViewById(R.id.tablel);
            imgv = (ImageView) view.findViewById(R.id.ImageView);

            btn.setVisibility(View.VISIBLE);
            btn1.setVisibility(View.VISIBLE);
            tbale.setVisibility(View.VISIBLE);
            imgv.setVisibility(View.VISIBLE);*/

            return true ;




        }
        if(url.contains("http://torqkd.com/torqkd_demo/"))
        {



            if(url.contains("url=http://torqkd.com/")
                    || url.contains("link=http://torqkd.com") || url.contains("redirect_uri=http://torqkd.com")
                    || url.contains("http://torqkd.com/user/profile/fbVidShareAndroid/")
                    || url.contains("http://torqkd.com/user/profile/fbImgShareAndroid/user_id/")
                    || url.contains("http://torqkd.com/user/profile/fbImgShareAndroid/")
                    || url.contains("http://torqkd.com/user/profile/fbVidShareAndroid/")
                    || url.contains("http://torqkd.com/user/profile/twittershare2/")
                    || url.contains("http://192.168.0.131/torqkd/user/profile/autoImgUp1/")
                    || url.contains("fbgetAT")

                    || url.contains("http://torqkd.com/user/profile/autoImgUp/"))
            {
                Context context = view.getContext();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                // Launch default browser
                context.startActivity(browserIntent);

                return true;

            }

            view.loadUrl(url);
            return  false;
        }
        else{

            Context context = view.getContext();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            // Launch default browser
            context.startActivity(browserIntent);

            return true;
        }
    }


    public void onLoadResource (WebView view, String url) {
        if (progressDialog == null) {
            // in standard case YourActivity.this
            Context context = view.getContext();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }
    }
    public void onPageFinished(WebView view, String url) {
        try{
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }catch(Exception exception){
            exception.printStackTrace();
        }
    }





    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,     SslError error) {
        super.onReceivedSslError(view, handler, error);

        // this will ignore the Ssl error and will go forward to your site
        handler.proceed();
        error.getCertificate();
    }



    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }
    }



    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        Log.d(LOG_TAG, message);
        // This shows the dialog box.  This can be commented out for dev
        Context context = view.getContext();
        AlertDialog.Builder alertBldr = new AlertDialog.Builder(context);
        alertBldr.setMessage(message);
        alertBldr.setTitle("Alert");
        alertBldr.show();
        result.confirm();
        return true;
    }

    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }

}
