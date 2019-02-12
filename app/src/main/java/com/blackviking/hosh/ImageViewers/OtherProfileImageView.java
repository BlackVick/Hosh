package com.blackviking.hosh.ImageViewers;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OtherProfileImageView extends AppCompatActivity {

    private ImageView exitActivity, image;
    private RelativeLayout rootLayout;
    private String userId, imageLink, imageThumbLink;

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

        setContentView(R.layout.activity_other_profile_image_view);


        userId = getIntent().getStringExtra("UserId");
        imageLink = getIntent().getStringExtra("ImageUrl");
        imageThumbLink = getIntent().getStringExtra("ImageThumbUrl");


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        rootLayout = (RelativeLayout)findViewById(R.id.otherProfileImageViewRootLayout);
        image = (ImageView)findViewById(R.id.otherProfileImageViewImage);


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
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(getBaseContext())
                                    .load(imageThumbLink) // image url goes here
                                    .placeholder(image.getDrawable())
                                    .into(image);

                        }

                        @Override
                        public void onError() {

                        }
                    });

        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_out, R.anim.scale_out);
    }
}
