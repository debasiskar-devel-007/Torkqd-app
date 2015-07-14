package com.torkqd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
//import org.apache.http.entity.mime.MultipartEntity;


public class upload extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.choose_uploader);











    }






    public void opencamera(View view) {
        Intent intent = new Intent(this, cameraActivity.class);

        startActivity(intent);
    }



    public void openvideo(View view) {
        Intent intent = new Intent(this, videoActivity.class);

        startActivity(intent);
    }



}