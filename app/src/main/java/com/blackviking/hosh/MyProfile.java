package com.blackviking.hosh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.ImageViewers.BlurImage;
import com.blackviking.hosh.ImageViewers.ProfileImageView;
import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Model.ImageModel;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.ViewHolder.UserProfileGalleryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MyProfile extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton editProfileFab;
    private String currentUid;
    private ImageView userProfileImage, coverPhoto, changeProfilePic;
    private TextView username, status, gender, followersCount, viewFollowers, followingCount, viewFollowing, hookupCount, viewHookups, location, interest, bio;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private CoordinatorLayout rootLayout;
    private UserModel currentUser;
    private int BLUR_PRECENTAGE = 50;
    private Target target;
    private Button openGallery;

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

        setContentView(R.layout.activity_my_profile);


        /*---   TOOLBAR   ---*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*---   IMAGE FACE DETECTION   ---*/
        PicassoFaceDetector.initialize(this);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.myCollapsing);
        editProfileFab = (FloatingActionButton)findViewById(R.id.editMyProfile);
        coverPhoto = (ImageView)findViewById(R.id.myProfilePictureBlur);
        userProfileImage = (ImageView)findViewById(R.id.myProfilePicture);
        changeProfilePic = (ImageView)findViewById(R.id.changeMyProfilePicture);
        rootLayout = (CoordinatorLayout)findViewById(R.id.myProfileRootLayout);
        username = (TextView)findViewById(R.id.myUsername);
        status = (TextView)findViewById(R.id.myStatus);
        gender = (TextView)findViewById(R.id.myGender);
        viewFollowers = (TextView)findViewById(R.id.viewFollowers);
        followersCount = (TextView)findViewById(R.id.followersCount);
        viewFollowing = (TextView)findViewById(R.id.viewFollowing);
        followingCount = (TextView)findViewById(R.id.followingCount);
        viewHookups = (TextView)findViewById(R.id.viewHookups);
        hookupCount = (TextView)findViewById(R.id.hookupsCount);
        location = (TextView)findViewById(R.id.myLocation);
        interest = (TextView)findViewById(R.id.myInterest);
        bio = (TextView)findViewById(R.id.myBio);
        openGallery = (Button)findViewById(R.id.openGalleryButton);


        /*---   BLUR COVER   ---*/
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                coverPhoto.setImageBitmap(BlurImage.fastblur(bitmap, 1f,
                        BLUR_PRECENTAGE));
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                coverPhoto.setImageResource(R.drawable.empty_profile);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };


        /*---   TOOLBAR   ---*/
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);


        /*---   FAB   ---*/
        if (Common.isConnectedToInternet(getBaseContext())){
            editProfileFab.setVisibility(View.VISIBLE);
        } else {
            editProfileFab.setVisibility(View.GONE);
        }
        editProfileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openEditProfileDialog();

            }
        });


        /*---   CHANGE PROFILE PICTURE   ---*/
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent newProfilePic = new Intent(MyProfile.this, ImageGallery.class);
                startActivity(newProfilePic);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });


        /*---   LOAD PROFILE   ---*/
        if (Common.isConnectedToInternet(getBaseContext())) {
            loadMyProfile(currentUid);
        } else {
            Snackbar.make(rootLayout, "Could not Load Your Profile. . .   No Internet Access !", Snackbar.LENGTH_LONG).show();
        }
    }

    private void openEditProfileDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.edit_profile_layout,null);

        final MaterialEditText statusEdt = (MaterialEditText) viewOptions.findViewById(R.id.editYourStatus);
        final MaterialSpinner genderChange = (MaterialSpinner) viewOptions.findViewById(R.id.genderSpinner);
        final MaterialSpinner interestChange = (MaterialSpinner) viewOptions.findViewById(R.id.interestSpinner);
        final EditText editBio = (EditText) viewOptions.findViewById(R.id.changeYourBio);
        final Button saveChanges = (Button) viewOptions.findViewById(R.id.saveProfileChanges);



        /*---   SPINNERS   ---*/
        genderChange.setItems("Male", "Female", "I am not sure", "Indifferent", "Not Specified");
        interestChange.setItems("Dating", "Friendship", "Hookup", "Not Specified");


        /*---   TEXT   ---*/
        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserModel currentUserUpdate = dataSnapshot.getValue(UserModel.class);

                statusEdt.setText(currentUserUpdate.getStatus());
                editBio.setText(currentUserUpdate.getBio());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        /*---   SAVE BUTTON   ---*/
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedGender = "";
                String selectedInterest = "";
                final String newStatus = statusEdt.getText().toString();
                final String newBio = editBio.getText().toString();

                /*---   SPINNERS GENDER   ---*/
                switch (genderChange.getSelectedIndex()){
                    case 0:
                        selectedGender = "Male";

                    case 1:
                        selectedGender = "Female";

                    case 2:
                        selectedGender = "I am not sure";

                    case 3:
                        selectedGender = "Indifferent";

                    case 4:
                        selectedGender = "Not Specified";
                }


                /*---   SPINNER INTEREST   ---*/
                switch (interestChange.getSelectedIndex()){
                    case 0:
                        selectedInterest = "Dating";

                    case 1:
                        selectedInterest = "Friendship";

                    case 2:
                        selectedInterest = "Hookup";

                    case 3:
                        selectedInterest = "Not Specified";


                }

                final String finalSelectedGender = selectedGender;
                final String finalSelectedInterest = selectedInterest;
                userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel currentUserUpdate2 = dataSnapshot.getValue(UserModel.class);

                        if (!newStatus.equals(currentUserUpdate2.getStatus()) || !finalSelectedGender.equals(currentUserUpdate2.getSex())
                                || !newBio.equals(currentUserUpdate2.getBio()) || !finalSelectedInterest.equals(currentUserUpdate2.getLookingFor())){

                            final Map<String, Object> profileUpdate = new HashMap<>();
                            profileUpdate.put("status", newStatus);
                            profileUpdate.put("sex", finalSelectedGender);
                            profileUpdate.put("lookingFor", finalSelectedInterest);
                            profileUpdate.put("bio", newBio);

                            final android.app.AlertDialog waitingDialog = new SpotsDialog(MyProfile.this, "Update Processing . . .");
                            waitingDialog.show();

                            userRef.child(currentUid).updateChildren(profileUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    waitingDialog.dismiss();
                                    alertDialog.dismiss();
                                    Snackbar.make(rootLayout, "Profile Updated !", Snackbar.LENGTH_LONG).show();
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void loadMyProfile(final String currentUid) {

        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentUser = dataSnapshot.getValue(UserModel.class);

                /*---   USER NAME   ---*/
                collapsingToolbarLayout.setTitle("@"+currentUser.getUserName());
                username.setText("@"+currentUser.getUserName());


                /*---   IMAGE   ---*/
                if (!currentUser.getProfilePictureThumb().equals("")){

                    /*---   PROFILE IMAGE   ---*/
                    Picasso.with(getBaseContext())
                            .load(currentUser.getProfilePictureThumb())
                            .placeholder(R.drawable.ic_loading_animation)
                            .transform(new FaceCenterCrop(400, 400))
                            .into(userProfileImage);


                    /*---   COVER PHOTO   ---*/
                    Picasso.with(getBaseContext())
                            .load(currentUser.getProfilePictureThumb())
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(target);


                    userProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileImgIntent = new Intent(MyProfile.this, ProfileImageView.class);
                            startActivity(profileImgIntent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    });
                }


                /*---   GALLERY   ---*/
                if (Common.isConnectedToInternet(getBaseContext())) {
                    openGallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent newProfilePic = new Intent(MyProfile.this, ImageGallery.class);
                            startActivity(newProfilePic);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    });
                } else {

                    openGallery.setVisibility(View.GONE);

                }


                /*---   BIO, GENDER, LOCATION, INTEREST, DATE JOINED, STATUS   ---*/
                bio.setText(currentUser.getBio());
                gender.setText(currentUser.getSex());
                location.setText(currentUser.getLocation());
                interest.setText(currentUser.getLookingFor());
                status.setText(currentUser.getStatus());

                /*---   FOLLOWERS   ---*/
                userRef.child(currentUid).child("Followers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int count = (int) dataSnapshot.getChildrenCount();

                        followersCount.setText(String.valueOf(count));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewFollowers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showUsersListIntent = new Intent(MyProfile.this, UserListActivity.class);
                        showUsersListIntent.putExtra("Type", "Followers");
                        showUsersListIntent.putExtra("CurrentUserId", currentUid);
                        startActivity(showUsersListIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                /*---   FOLLOWING   ---*/
                userRef.child(currentUid).child("Following").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int count = (int) dataSnapshot.getChildrenCount();

                        followingCount.setText(String.valueOf(count));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewFollowing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showUsersListIntent = new Intent(MyProfile.this, UserListActivity.class);
                        showUsersListIntent.putExtra("Type", "Following");
                        showUsersListIntent.putExtra("CurrentUserId", currentUid);
                        startActivity(showUsersListIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                /*---   HOOKUPS   ---*/
                userRef.child(currentUid).child("Hookups").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int count = (int) dataSnapshot.getChildrenCount();

                        hookupCount.setText(String.valueOf(count));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHookups.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showUsersListIntent = new Intent(MyProfile.this, UserListActivity.class);
                        showUsersListIntent.putExtra("Type", "Hookups");
                        showUsersListIntent.putExtra("CurrentUserId", currentUid);
                        startActivity(showUsersListIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoFaceDetector.releaseDetector();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PicassoFaceDetector.releaseDetector();
    }

    @Override
    protected void onStop() {
        super.onStop();
        PicassoFaceDetector.releaseDetector();
    }
}
