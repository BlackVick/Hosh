package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.ImageViewers.BlurImage;
import com.blackviking.hosh.ImageViewers.OtherProfileImageView;
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
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OtherUserProfile extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton messageUserFab, followUserFab;
    private String userId, currentUid;
    private ImageView userProfileImage, coverPhoto;
    private TextView username, status, online, gender, followersCount, location, interest, dateJoined, bio;
    private Button viewFollowers, openGallery;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private CoordinatorLayout rootLayout;
    private UserModel currentUser;
    private int BLUR_PRECENTAGE = 50;
    private FirebaseRecyclerAdapter<ImageModel, UserProfileGalleryViewHolder> adapter;
    private Target target;

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

        setContentView(R.layout.activity_other_user_profile);


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


        /*---   INTENT DATA   ---*/
        userId = getIntent().getStringExtra("UserId");


        /*---   WIDGETS   ---*/
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        messageUserFab = (FloatingActionButton)findViewById(R.id.messageUser);
        followUserFab = (FloatingActionButton)findViewById(R.id.followUser);
        coverPhoto = (ImageView)findViewById(R.id.userProfilePictureBlur);
        userProfileImage = (ImageView)findViewById(R.id.userProfilePicture);
        rootLayout = (CoordinatorLayout)findViewById(R.id.otherUserProfileRootLayout);
        username = (TextView)findViewById(R.id.userUsername);
        status = (TextView)findViewById(R.id.userStatus);
        online = (TextView)findViewById(R.id.userOnlineStatus);
        gender = (TextView)findViewById(R.id.userGender);
        followersCount = (TextView)findViewById(R.id.userFollowers);
        location = (TextView)findViewById(R.id.userLocation);
        interest = (TextView)findViewById(R.id.userInterest);
        dateJoined = (TextView)findViewById(R.id.userDateJoined);
        bio = (TextView)findViewById(R.id.userBio);
        viewFollowers = (Button)findViewById(R.id.viewUserFollowers);
        openGallery = (Button)findViewById(R.id.openUserGalleryButton);


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


        /*---   FAB VISIBILITY   ---*/
        if (Common.isConnectedToInternet(getBaseContext())){
            followUserFab.setVisibility(View.VISIBLE);
        } else {
            followUserFab.setVisibility(View.GONE);
        }



        /*---   MESSAGING FAB   ---*/
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                messageUserFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent messagingIntent = new Intent(OtherUserProfile.this, Messaging.class);
                        messagingIntent.putExtra("UserId", userId);
                        messagingIntent.putExtra("UserName", dataSnapshot.child("userName").getValue().toString());
                        startActivity(messagingIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("Following").child(userId).exists()){

                    followUserFab.setImageResource(R.drawable.ic_unfollow_user);

                    followUserFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            userRef.child(currentUid).child("Following").child(userId).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userRef.child(userId).child("Followers").child(currentUid).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Snackbar.make(rootLayout, "You have un followed @"+currentUser.getUserName(), Snackbar.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

                    /*---   FABs   ---*/
                    messageUserFab.setVisibility(View.VISIBLE);

                } else {

                    followUserFab.setImageResource(R.drawable.ic_follow_user);

                    followUserFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            userRef.child(currentUid).child("Following").child(userId).setValue("Following")
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userRef.child(userId).child("Followers").child(currentUid).setValue("Follows You")
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Snackbar.make(rootLayout, "You are now following @"+currentUser.getUserName(), Snackbar.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   LOAD PROFILE   ---*/
        if (Common.isConnectedToInternet(getBaseContext())){
            loadUserProfile(userId);
        } else {
            Snackbar.make(rootLayout, "Could not Load This Profile. . .   No Internet Access !", Snackbar.LENGTH_LONG).show();
        }

    }

    private void loadUserProfile(final String userId) {

        userRef.child(userId).addValueEventListener(new ValueEventListener() {
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


                    /*---   PROFILE IMAGE CLICK   ---*/
                    userProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileImgIntent = new Intent(OtherUserProfile.this, OtherProfileImageView.class);
                            profileImgIntent.putExtra("UserId", userId);
                            profileImgIntent.putExtra("ImageUrl", currentUser.getProfilePicture());
                            profileImgIntent.putExtra("ImageThumbUrl", currentUser.getProfilePictureThumb());
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
                            Intent goToUserGallery = new Intent(OtherUserProfile.this, UserImageGallery.class);
                            goToUserGallery.putExtra("UserId", userId);
                            startActivity(goToUserGallery);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    });
                } else {

                    openGallery.setVisibility(View.GONE);

                }


                /*---   ONLINE, BIO, GENDER, LOCATION, INTEREST, DATE JOINED, STATUS   ---*/
                online.setText(currentUser.getOnlineState());
                bio.setText(currentUser.getBio());
                gender.setText(currentUser.getSex());
                location.setText(currentUser.getLocation());
                interest.setText(currentUser.getLookingFor());
                dateJoined.setText(currentUser.getDateJoined());
                status.setText(currentUser.getStatus());

                /*---   FOLLOWERS   ---*/
                userRef.child(userId).child("Followers").addValueEventListener(new ValueEventListener() {
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
                        Intent showUsersListIntent = new Intent(OtherUserProfile.this, UserListActivity.class);
                        showUsersListIntent.putExtra("Type", "Followers");
                        showUsersListIntent.putExtra("CurrentUserId", userId);
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
