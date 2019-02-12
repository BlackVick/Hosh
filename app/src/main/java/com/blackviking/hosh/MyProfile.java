package com.blackviking.hosh;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.ImageViewers.BlurImage;
import com.blackviking.hosh.Model.ImageModel;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.ViewHolder.UserProfileGalleryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MyProfile extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton editProfileFab;
    private String currentUid;
    private ImageView userProfileImage, coverPhoto, changeProfilePic;
    private TextView username, status, gender, followersCount, viewFollowers, followingCount, viewFollowing, hookupCount, viewHookups, location, interest, bio;
    private RecyclerView myGalleryRecycler;
    private LinearLayoutManager layoutManager;
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
        followersCount = (TextView)findViewById(R.id.userFollowers);
        viewFollowing = (TextView)findViewById(R.id.viewFollowing);
        followingCount = (TextView)findViewById(R.id.followingCount);
        viewHookups = (TextView)findViewById(R.id.viewHookups);
        hookupCount = (TextView)findViewById(R.id.hookupsCount);
        location = (TextView)findViewById(R.id.myLocation);
        interest = (TextView)findViewById(R.id.myInterest);
        bio = (TextView)findViewById(R.id.myBio);
        myGalleryRecycler = (RecyclerView)findViewById(R.id.myGalleryRecycler);


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


        /*---   RECYCLER CONTROLLER   ---*/
        myGalleryRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myGalleryRecycler.setLayoutManager(layoutManager);


        /*---   FAB   ---*/
        editProfileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*---   CHANGE PROFILE PICTURE   ---*/
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*---   VIEW FOLLOWERS   ---*/
        viewFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*---   VIEW FOLLOWING   ---*/
        viewFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*---   VIEW HOOKUPS   ---*/
        viewHookups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*---   LOAD PROFILE   ---*/
        loadMyProfile(currentUid);
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
