package com.blackviking.hosh.ImageViewers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blackviking.hosh.Model.ImageModel;
import com.blackviking.hosh.R;
import com.daimajia.slider.library.SliderLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MyGalleryImageView extends AppCompatActivity {

    private ImageView exitActivity, image;
    private RelativeLayout rootLayout;
    private String currentImage, currentUid;
    private FloatingActionButton deleteImage, setProfilePic;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, imageRef;
    private ImageModel theCurrentImage;

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

        setContentView(R.layout.activity_my_gallery_image_view);


        /*---   LOCAL   ---*/
        currentImage = getIntent().getStringExtra("CurrentImage");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        userRef = db.getReference("Users").child(currentUid);
        imageRef = db.getReference("Users").child(currentUid).child("Gallery");



        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        rootLayout = (RelativeLayout)findViewById(R.id.myGalleryImageViewRootLayout);
        image = (ImageView)findViewById(R.id.myGalleryImageViewImage);
        deleteImage = (FloatingActionButton)findViewById(R.id.deletePicture);
        setProfilePic = (FloatingActionButton)findViewById(R.id.setAsProfilePicture);


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.scale_out, R.anim.scale_out);
            }
        });


        /*---   THE CURRENT IMAGE   ---*/
        imageRef.child(currentImage).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                theCurrentImage = dataSnapshot.getValue(ImageModel.class);

                Picasso.with(getBaseContext())
                        .load(theCurrentImage.getImageThumbUrl()) // thumbnail url goes here
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                Picasso.with(getBaseContext())
                                        .load(theCurrentImage.getImageUrl()) // image url goes here
                                        .placeholder(image.getDrawable())
                                        .into(image);
                            }
                            @Override
                            public void onError() {

                            }
                        });


                /*---   SET AS PROFILE PIC   ---*/
                setProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openDialogForProfilePic();

                    }
                });


                /*---   REMOVE PICTURE   ---*/
                deleteImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openDialogForDelete();

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openDialogForDelete() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Delete Image !")
                .setIcon(R.drawable.ic_delete_image)
                .setMessage("Are You Sure You Want To Delete Image From Your Hosh Gallery?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteImageFromGallery();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }

    private void deleteImageFromGallery() {

        imageRef.child(currentImage).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        });

    }

    private void openDialogForProfilePic() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Profile Alert !")
                .setIcon(R.drawable.ic_set_as_profile_pic)
                .setMessage("Are You Sure You Want To Make This Picture Your New Profile Picture?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        changeProfilePicture();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }

    private void changeProfilePicture() {

        userRef.child("profilePicture").setValue(theCurrentImage.getImageUrl()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                userRef.child("profilePictureThumb").setValue(theCurrentImage.getImageThumbUrl()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(rootLayout, "Completed !", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_out, R.anim.scale_out);
    }
}
