package com.torkqd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class cameraActivity extends Activity {

    private Uri outputFileUri;
    private ImageView imgView;
    private Button upload, cancel;
    private Bitmap bitmap;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.camera_activity);

        //super.setNavigationVisibility(View.GONE, View.GONE);

        imgView = (ImageView) findViewById(R.id.ImageView);
        upload = (Button) findViewById(R.id.imguploadbtn);
        cancel = (Button) findViewById(R.id.imgcancelbtn);

        upload.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (bitmap == null) {
                    Toast.makeText(getApplicationContext(),
                            "Please select image", Toast.LENGTH_SHORT).show();
                } else {
                    dialog = ProgressDialog.show(cameraActivity.this,
                            "Uploading", "Please wait...", true);
                    new ImageUploadTask().execute();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cameraActivity.this.finish();
            }
        });

        openImageIntent();

    }

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
        Log.e("Root", root.toString());
        root.mkdirs();
        // final String fname = Utils.getUniqueImageFilename();
        final String fname = "photo_" + new Date().getTime() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        Log.e("outputFileUri --", outputFileUri.toString());
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName,	res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }
        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent,"Select Source");
        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,	cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri selectedImageUri = null;
        String filePath = null;

        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action
                                .equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Log.e("isCamera", ""+isCamera);

                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                Log.e("selectedImageUri", selectedImageUri.toString());
            }
        }

        if (selectedImageUri != null) {
            try {
                // OI FILE Manager
                String filemanagerstring = selectedImageUri.getPath();

                // MEDIA GALLERY
                String selectedImagePath = getPath(selectedImageUri);

                if (selectedImagePath != null) {
                    filePath = selectedImagePath;
                } else if (filemanagerstring != null) {
                    filePath = filemanagerstring;
                } else {
                    Toast.makeText(getApplicationContext(), "Unknown path",
                            Toast.LENGTH_LONG).show();
                    Log.e("Bitmap", "Unknown path");
                }

                if (filePath != null) {
                    decodeFile(filePath);
                } else {
                    bitmap = null;
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Internal error",
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
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

        imgView.setImageBitmap(bitmap);

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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
            byte[] ba = bao.toByteArray();
            String ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
            ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("image", ba1));
            nameValuePairs.add(new BasicNameValuePair("cmd", "image_android"));
            Log.v("log_tag", System.currentTimeMillis() + ".jpg");

            Toast.makeText(getApplicationContext(), "Unknown path",
                    Toast.LENGTH_LONG).show();
            MultipartEntity reqEntity = new MultipartEntity();
            FileBody bin = new FileBody(new File("C:/ABC.txt"));
            try {
                reqEntity.addPart("IMG", new StringBody(ba1));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //reqEntity.addPart("fileup1", comment);

            try {
                reqEntity.addPart("ONE", new StringBody("11111111"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                reqEntity.addPart("TWO", new StringBody("222222222"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }



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
