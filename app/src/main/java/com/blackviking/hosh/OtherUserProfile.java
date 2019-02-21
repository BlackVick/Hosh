package com.blackviking.hosh;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.ImageViewers.BlurImage;
import com.blackviking.hosh.ImageViewers.OtherProfileImageView;
import com.blackviking.hosh.ImageViewers.ProfileImageView;
import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Model.DataMessage;
import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.Model.ImageModel;
import com.blackviking.hosh.Model.MyResponse;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.Notification.APIService;
import com.blackviking.hosh.ViewHolder.FeedViewHolder;
import com.blackviking.hosh.ViewHolder.UserProfileGalleryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;
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
    private DatabaseReference userRef, timelineRef, likeRef, commentRef;
    private CoordinatorLayout rootLayout;
    private UserModel currentUser;
    private int BLUR_PRECENTAGE = 50;
    private RecyclerView timelineRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HopdateModel, FeedViewHolder> adapter;
    private Target target;
    private APIService mService;


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


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");
        timelineRef = db.getReference("Hopdate");
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("HopdateComments");


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
        timelineRecycler = (RecyclerView)findViewById(R.id.otherUserTimelineRecycler);


        /*---   TOOLBAR   ---*/
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        /*---   FAB VISIBILITY   ---*/
        if (Common.isConnectedToInternet(getBaseContext())){
            followUserFab.setVisibility(View.VISIBLE);
        } else {
            followUserFab.setVisibility(View.GONE);
        }


        /*---   FOLLOW CHECK   ---*/
        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("Following").child(userId).exists()) {

                    followUserFab.setImageResource(R.drawable.ic_unfollow_user);
                    timelineRecycler.setVisibility(View.VISIBLE);
                    loadUserTimeline(userId);

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
                                                            Snackbar.make(rootLayout, "You have un followed @" + currentUser.getUserName(), Snackbar.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

                    /*---   FABs   ---*/
                    messageUserFab.setVisibility(View.VISIBLE);

                } else {

                    timelineRecycler.setVisibility(View.GONE);
                    followUserFab.setImageResource(R.drawable.ic_follow_user);

                    followUserFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            userRef.child(currentUid).child("Following").child(userId).child("date").setValue(ServerValue.TIMESTAMP)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userRef.child(userId).child("Followers").child(currentUid).child("date").setValue(ServerValue.TIMESTAMP)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Snackbar.make(rootLayout, "You are now following @" + currentUser.getUserName(), Snackbar.LENGTH_LONG).show();
                                                            sendFollowNotification(currentUser.getUserName(), currentUid);

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

    private void sendFollowNotification(String userName, String currentUid) {

        Map<String, String> dataSend = new HashMap<>();
        dataSend.put("title", "Account");
        dataSend.put("message", "@"+userName+" Just Followed You");
        dataSend.put("user_id", currentUid);
        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userId).toString(), dataSend);

        mService.sendNotification(dataMessage)
                .enqueue(new retrofit2.Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Snackbar.make(rootLayout, "Error Sending Notification !", Snackbar.LENGTH_LONG).show();
                    }
                });

    }

    private void loadUserTimeline(String userId) {

        /*---   TIMELINE RECYCLER   ---*/
        timelineRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        timelineRecycler.setLayoutManager(layoutManager);

        Query myTimeline = timelineRef.orderByChild("sender").equalTo(userId);

        adapter = new FirebaseRecyclerAdapter<HopdateModel, FeedViewHolder>(
                HopdateModel.class,
                R.layout.feed_item,
                FeedViewHolder.class,
                myTimeline
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final HopdateModel model, final int position) {

                /*---   OPTIONS   ---*/
                viewHolder.options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /*---   POPUP MENU FOR HOPDATE   ---*/
                        PopupMenu popup = new PopupMenu(OtherUserProfile.this, viewHolder.options);
                        popup.inflate(R.menu.feed_item_menu);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_feed_delete:

                                        AlertDialog alertDialog = new AlertDialog.Builder(OtherUserProfile.this)
                                                .setTitle("Delete Hopdate !")
                                                .setIcon(R.drawable.ic_delete_feed)
                                                .setMessage("Are You Sure You Want To Delete This Hopdate From Your Timeline?")
                                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        timelineRef.child(adapter.getRef(position).getKey()).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Snackbar.make(rootLayout, "Hopdate Deleted !", Snackbar.LENGTH_LONG).show();
                                                                    }
                                                                });

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

                                        return true;
                                    case R.id.action_feed_share:

                                        Intent i = new Intent(android.content.Intent.ACTION_SEND);
                                        i.setType("text/plain");
                                        i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Hosh Invite");
                                        i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey, \n \n Check Out My New Story On HOSH. You Can Download For Free On PlayStore And Connect With Other Hoshers. ");
                                        startActivity(Intent.createChooser(i,"Share via"));

                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });

                        popup.show();
                    }
                });


                /*---   POSTER DETAILS   ---*/
                if (!currentUser.getProfilePictureThumb().equals("")){

                    Picasso.with(getBaseContext())
                            .load(currentUser.getProfilePictureThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.posterImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(currentUser.getProfilePictureThumb())
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(viewHolder.posterImage);
                                }
                            });

                } else {

                    viewHolder.posterImage.setImageResource(R.drawable.empty_profile);

                }


                    /*---   POST IMAGE   ---*/
                if (!model.getImageThumbUrl().equals("")){

                    Picasso.with(getBaseContext())
                            .load(model.getImageThumbUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.post_loading_icon)
                            .into(viewHolder.postImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(model.getImageThumbUrl())
                                            .placeholder(R.drawable.post_loading_icon)
                                            .into(viewHolder.postImage);
                                }
                            });

                } else {

                    viewHolder.postImage.setVisibility(View.GONE);

                }


                    /*---   HOPDATE   ---*/
                if (!model.getHopdate().equals("")){

                    viewHolder.postText.setText(model.getHopdate());

                } else {

                    viewHolder.postText.setVisibility(View.GONE);

                }


                    /*---  TIME   ---*/
                viewHolder.postTime.setText(model.getTimestamp());


                    /*---   LIKES   ---*/
                final String feedId = adapter.getRef(position).getKey();
                likeRef.child(feedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        viewHolder.likeCount.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()){

                            viewHolder.likeBtn.setImageResource(R.drawable.liked_icon);

                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(feedId).child(currentUid).removeValue();
                                }
                            });

                        } else {

                            viewHolder.likeBtn.setImageResource(R.drawable.unliked_icon);

                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(feedId).child(currentUid).setValue("liked");
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                /*---   COMMENTS   ---*/
                commentRef.child(feedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int countComment = (int) dataSnapshot.getChildrenCount();

                        viewHolder.commentCount.setText(String.valueOf(countComment));

                        viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                                feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                                startActivity(feedDetail);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                    /*---   FEED IMAGE CLICK   ---*/
                viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);

                    }
                });


                    /*---   FEED TEXT CLICK   ---*/
                viewHolder.postText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);

                    }
                });


            }
        };
        timelineRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void loadUserProfile(final String userId) {

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

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        }
                    });

                }


                /*---   MESSAGING FAB   ---*/
                messageUserFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent messagingIntent = new Intent(OtherUserProfile.this, Messaging.class);
                        messagingIntent.putExtra("UserId", userId);
                        messagingIntent.putExtra("UserName", currentUser.getUserName());
                        startActivity(messagingIntent);
                    }
                });

                /*---   GALLERY   ---*/
                if (Common.isConnectedToInternet(getBaseContext())) {
                    openGallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent goToUserGallery = new Intent(OtherUserProfile.this, UserImageGallery.class);
                            goToUserGallery.putExtra("UserId", userId);
                            startActivity(goToUserGallery);
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
                userRef.child(userId).child("Followers").addListenerForSingleValueEvent(new ValueEventListener() {
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
}
