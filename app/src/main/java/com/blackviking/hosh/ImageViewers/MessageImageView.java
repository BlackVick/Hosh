package com.blackviking.hosh.ImageViewers;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MessageImageView extends AppCompatActivity {

    private ImageView exitActivity, image, savePicture;
    private RelativeLayout rootLayout;
    private String userId, imageLink, imageThumbLink;
    private ProgressDialog pDialog;
    private DownloadManager downloadManager;
    final long date = System.currentTimeMillis();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Wigrum-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_message_image_view);


        /*---   INTENT DATA   ---*/
        imageLink = getIntent().getStringExtra("ImageUrl");
        imageThumbLink = getIntent().getStringExtra("ThumbUrl");


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)findViewById(R.id.exitThisActivity);
        savePicture = (ImageView)findViewById(R.id.saveThisPicture);
        rootLayout = (RelativeLayout)findViewById(R.id.messageImageViewRootLayout);
        image = (ImageView)findViewById(R.id.messageImageViewView);


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.scale_out, R.anim.scale_out);
            }
        });


        /*---   LOAD PROFILE IMAGE   ---*/
        if (Common.isConnectedToInternet(getBaseContext())) {

            Picasso.with(getBaseContext())
                    .load(imageThumbLink) // thumbnail url goes here
                    .placeholder(R.drawable.ic_loading_animation)
                    .into(image);

        }


        /*---   SAVE IMAGE TO GALLERY   ---*/
        savePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadFileFromURL().execute(imageLink);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_out, R.anim.scale_out);
    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /*---   INDICATE DOWNLOAD START   ---*/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Snackbar.make(rootLayout, "Download Started", Snackbar.LENGTH_SHORT).show();

        }

        /*---   DOWNLOADING IN BACKGROUND   ---*/
        @Override
        protected String doInBackground(String... f_url) {

            try {

                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(imageLink);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setDestinationInExternalPublicDir("/Hosh/Images/", "IMG_" + date + ".jpg");
                request.allowScanningByMediaScanner();
                Long reference = downloadManager.enqueue(request);

                BroadcastReceiver onComplete = new BroadcastReceiver() {
                    public void onReceive(Context ctxt, Intent intent) {

                        Toast.makeText(ctxt, "Download Finished", Toast.LENGTH_SHORT).show();

                    }
                };
                //register receiver for when file download is compete
                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            } catch (Exception e) {

                Log.e("Error: ", e.getMessage());

            }

            return null;
        }

        /*---   AFTER TASK COMPLETE   ---*/
        @Override
        protected void onPostExecute(String file_url) {
        }

    }
}
