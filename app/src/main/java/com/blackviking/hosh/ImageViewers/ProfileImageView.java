package com.blackviking.hosh.ImageViewers;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.ImageGallery;
import com.blackviking.hosh.Model.ImageModel;
import com.blackviking.hosh.MyProfile;
import com.blackviking.hosh.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileImageView extends AppCompatActivity {

    private ImageView exitActivity, image;
    private RelativeLayout rootLayout;
    private FloatingActionButton newProfilePic;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private String currentUid;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Thin.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_profile_image_view);


        /*---   FIREBASE   ---*/
        currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        rootLayout = (RelativeLayout)findViewById(R.id.profileImageViewRootLayout);
        image = (ImageView)findViewById(R.id.profileImageViewImage);
        newProfilePic = (FloatingActionButton)findViewById(R.id.newProfilePictureFab);


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   NEW PROFILE IMAGE   ---*/
        newProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newProfilePic = new Intent(ProfileImageView.this, ImageGallery.class);
                startActivity(newProfilePic);
            }
        });


        /*---   LOAD PROFILE IMAGE   ---*/
        final android.app.AlertDialog waitingDialog = new SpotsDialog(ProfileImageView.this, "Please Wait . . .");
        waitingDialog.show();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String profilePicture = dataSnapshot.child("profilePicture").getValue().toString();
                final String profilePictureThumb = dataSnapshot.child("profilePictureThumb").getValue().toString();

                if (!profilePicture.equals("") && !profilePictureThumb.equals("")){

                    waitingDialog.dismiss();
                    Picasso.with(getBaseContext())
                            .load(profilePictureThumb) // thumbnail url goes here
                            .into(image);

                } else {

                    waitingDialog.dismiss();
                    image.setImageResource(R.drawable.empty_profile);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
