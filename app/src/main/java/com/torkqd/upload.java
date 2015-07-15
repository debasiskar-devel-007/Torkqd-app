package com.torkqd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
//import org.apache.http.entity.mime.MultipartEntity;




public class upload extends Activity {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private Bitmap bitmap;
    private ProgressDialog dialog;
    private String deviceId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.choose_uploader);


        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getFilePaths());
        gridView.setAdapter(gridAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                //Toast.makeText(getApplicationContext(), "image clicked "+item.getTitle(),
                // Toast.LENGTH_LONG).show();
                decodeFile(item.getTitle());

                dialog = ProgressDialog.show(upload.this,
                        "Uploading", "Please wait...", true);
                new ImageUploadTask().execute();

                //Create intent
                /*Intent intent = new Intent(upload.this, cameraActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);*/
            }
        });




    }




    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(filePath, o2);
        Toast.makeText(getApplicationContext(), filePath,
                Toast.LENGTH_LONG).show();




       // imgView.setImageBitmap(bitmap);

    }

    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, "Image#"));
        }
        return imageItems;
    }





    public ArrayList<ImageItem> getFilePaths()
    {


        //Uri u = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor c = null;
        Context context;
        SortedSet<String> dirList = new TreeSet<>();
        ArrayList<ImageItem> resultIAV = new ArrayList<>();

        String[] directories = null;
        //if (u != null) {
            c = this.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
        //}

        if ((c != null) && (c.moveToFirst()))
        {
            do
            {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try{
                    dirList.add(tempDir);
                }
                catch(Exception e)
                {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for(int i=0;i<dirList.size();i++)
        {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if(imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if(imagePath.isDirectory())
                    {
                        imageList = imagePath.listFiles();

                    }
                    if ( imagePath.getName().contains(".jpg")|| imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg")|| imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                            )
                    {



                        String path= imagePath.getAbsolutePath();
                        //Toast.makeText(getApplicationContext(), path,
                               // Toast.LENGTH_LONG).show();
                        Bitmap bitmap ;
                        bitmap = BitmapFactory.decodeFile(path);
                        resultIAV.add(new ImageItem(bitmap,  path));

                    }
                }
                //  }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return resultIAV;


    }





    public void opencamera(View view) {
        Intent intent = new Intent(this, cameraActivity.class);

        startActivity(intent);
    }
    public void seeimage(View view) {

        Toast.makeText(getApplicationContext(), "image clicked "+view,
                Toast.LENGTH_LONG).show();

    }



    public void openvideo(View view) {
        Intent intent = new Intent(this, videoActivity.class);

        startActivity(intent);
    }


    class ImageUploadTask extends AsyncTask<Void, Void, String> {
        @SuppressWarnings("unused")
        @Override
        protected String doInBackground(Void... unsued) {
            InputStream is;
            BitmapFactory.Options bfo;
            Bitmap bitmapOrg;
            ByteArrayOutputStream bao;

            bfo = new BitmapFactory.Options();
            bfo.inSampleSize = 2;
            /*bitmapOrg = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
             + "/" + customImage, bfo);*/

            bao = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
            byte[] ba = bao.toByteArray();

            String ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
            is=new ByteArrayInputStream(bao.toByteArray());
            ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("image", ba1));
            nameValuePairs.add(new BasicNameValuePair("cmd", "image_android"));
            Log.v("log_tag", System.currentTimeMillis() + ".jpg");

            /*Toast.makeText(getApplicationContext(), "Unknown path",
                    Toast.LENGTH_LONG).show();*/
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            reqEntity.addPart("myFile",
                    deviceId+ ".jpg", is);

            //FileBody bin = new FileBody(new File("C:/ABC.txt"));








            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new
                        // Here you need to put your server file address
                        HttpPost("http://torqkd.com/user/ajs/AddTempTable");
                // httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httppost.setEntity(reqEntity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();

                Context context = upload.this;
                Intent cameraintent = new Intent(context, MainActivity.class);

                // Launch default browser
                context.startActivity(cameraintent);
                Log.v("log_tag", "In the try Loop");
            } catch (Exception e) {
                Log.v("log_tag", "Error in http connection " + e.toString());
            }
            return "Success";
            // (null);
        }

        @Override
        protected void onProgressUpdate(Void... unused) {

        }

        @Override
        protected void onPostExecute(String sResponse) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }

    }



}