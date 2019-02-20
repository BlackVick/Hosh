package com.blackviking.hosh.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.blackviking.hosh.AccountSettings;
import com.blackviking.hosh.FeedDetails;
import com.blackviking.hosh.ImageViewers.ProfileImageView;
import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.MyProfile;
import com.blackviking.hosh.OtherUserProfile;
import com.blackviking.hosh.R;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.UserListActivity;
import com.blackviking.hosh.ViewHolder.FeedViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class Account extends Fragment {

    private ImageView exitActivity, help;
    private TextView activityName, following, followers, profileSetting, accountSetting;
    private CircleImageView profileImage;
    private LinearLayout followingLayout, followersLayout;
    private RecyclerView timelineRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HopdateModel, FeedViewHolder> adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, timelineRef, likeRef, commentRef;
    private String currentUid;
    private UserModel currentUser;

    public Account() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   CURRENT USER TOKEN   ---*/
        currentUid = mAuth.getCurrentUser().getUid();

        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        timelineRef = db.getReference("Hopdate");
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("HopdateComments");


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)v.findViewById(R.id.exitActivity);
        help = (ImageView)v.findViewById(R.id.helpIcon);
        activityName = (TextView)v.findViewById(R.id.activityName);
        followers = (TextView)v.findViewById(R.id.followersCount);
        following = (TextView)v.findViewById(R.id.followingCount);
        profileSetting = (TextView)v.findViewById(R.id.profileSetting);
        accountSetting = (TextView)v.findViewById(R.id.accountSetting);
        profileImage = (CircleImageView)v.findViewById(R.id.profileImage);
        followingLayout = (LinearLayout)v.findViewById(R.id.followingCountLayout);
        followersLayout = (LinearLayout)v.findViewById(R.id.followersCountLayout);
        timelineRecycler = (RecyclerView)v.findViewById(R.id.timelineRecycler);
        
        /*---   CURRENT USER   ---*/
        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentUser = dataSnapshot.getValue(UserModel.class);

                /*---   ACTIVITY NAME   ---*/
                activityName.setText("@" + currentUser.getUserName());


                /*---   FOLLOWERS   ---*/
                if (dataSnapshot.child("Followers").exists()) {

                    userRef.child(currentUid).child("Followers").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            int count = (int) dataSnapshot.getChildrenCount();
                            followers.setText(String.valueOf(count));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                followersLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showUsersListIntent = new Intent(getContext(), UserListActivity.class);
                        showUsersListIntent.putExtra("Type", "Followers");
                        showUsersListIntent.putExtra("CurrentUserId", currentUid);
                        startActivity(showUsersListIntent);
                    }
                });


                /*---   FOLLOWING   ---*/
                if (dataSnapshot.child("Following").exists()) {

                    userRef.child(currentUid).child("Following").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            int count = (int) dataSnapshot.getChildrenCount();
                            following.setText(String.valueOf(count));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                followingLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showUsersListIntent = new Intent(getContext(), UserListActivity.class);
                        showUsersListIntent.putExtra("Type", "Following");
                        showUsersListIntent.putExtra("CurrentUserId", currentUid);
                        startActivity(showUsersListIntent);
                    }
                });


                /*---   PROFILE PICTURE   ---*/
                if (!currentUser.getProfilePictureThumb().equals("")) {

                    Picasso.with(getContext())
                            .load(currentUser.getProfilePictureThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(profileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
                                            .load(currentUser.getProfilePictureThumb())
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(profileImage);
                                }
                            });

                }

                profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileImgIntent = new Intent(getContext(), ProfileImageView.class);
                        /*profileImgIntent.putExtra("ImageUrl", currentUser.getProfilePicture());
                        profileImgIntent.putExtra("ImageThumbUrl", currentUser.getProfilePictureThumb());*/
                        startActivity(profileImgIntent);
                    }
                });


                /*---   PROFILE SETTING   ---*/
                profileSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent posterProfile = new Intent(getContext(), MyProfile.class);
                        startActivity(posterProfile);
                    }
                });


                /*---   ACCOUNT SETTING   ---*/
                accountSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent accountIntent = new Intent(getContext(), AccountSettings.class);
                        startActivity(accountIntent);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   EXIT   ---*/
        exitActivity.setImageResource(R.drawable.ic_action_close_app);
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faqIntent = new Intent(getContext(), Faq.class);
                startActivity(faqIntent);
            }
        });


        loadMyTimeline();

        return v;
    }

    private void loadMyTimeline() {

        /*---   TIMELINE   ---*/
        timelineRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        timelineRecycler.setLayoutManager(layoutManager);

        Query myTimeline = timelineRef.orderByChild("sender").equalTo(currentUid);

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
                        PopupMenu popup = new PopupMenu(getContext(), viewHolder.options);
                        popup.inflate(R.menu.feed_item_menu);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_feed_delete:

                                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
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
                                                                        Snackbar.make(getView(), "Hopdate Deleted !", Snackbar.LENGTH_LONG).show();
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

                    Picasso.with(getContext())
                            .load(currentUser.getProfilePictureThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.posterImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
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

                    Picasso.with(getContext())
                            .load(model.getImageThumbUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.post_loading_icon)
                            .into(viewHolder.postImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
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
                                Intent feedDetail = new Intent(getContext(), FeedDetails.class);
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

                        Intent feedDetail = new Intent(getContext(), FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);

                    }
                });


                    /*---   FEED TEXT CLICK   ---*/
                viewHolder.postText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent feedDetail = new Intent(getContext(), FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);

                    }
                });


            }
        };
        timelineRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}
