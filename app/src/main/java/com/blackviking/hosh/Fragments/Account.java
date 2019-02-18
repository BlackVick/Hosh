package com.blackviking.hosh.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackviking.hosh.AccountSettings;
import com.blackviking.hosh.ImageViewers.ProfileImageView;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.MyProfile;
import com.blackviking.hosh.R;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.UserListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private TextView activityName, following, followers, hookups, profileSetting, accountSetting;
    private CircleImageView profileImage;
    private LinearLayout followingLayout, followersLayout, hookupsLayout;
    private RecyclerView timelineRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, timelineRef;
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
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        timelineRef = db.getReference("Hopdate");


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)v.findViewById(R.id.exitActivity);
        help = (ImageView)v.findViewById(R.id.helpIcon);
        activityName = (TextView)v.findViewById(R.id.activityName);
        followers = (TextView)v.findViewById(R.id.followersCount);
        following = (TextView)v.findViewById(R.id.followingCount);
        hookups = (TextView)v.findViewById(R.id.hookupCount);
        profileSetting = (TextView)v.findViewById(R.id.profileSetting);
        accountSetting = (TextView)v.findViewById(R.id.accountSetting);
        profileImage = (CircleImageView)v.findViewById(R.id.profileImage);
        followingLayout = (LinearLayout)v.findViewById(R.id.followingCountLayout);
        followersLayout = (LinearLayout)v.findViewById(R.id.followersCountLayout);
        hookupsLayout = (LinearLayout)v.findViewById(R.id.hookupCountLayout);
        timelineRecycler = (RecyclerView)v.findViewById(R.id.timelineRecycler);
        
        
        /*---   TIMELINE   ---*/
        timelineRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        timelineRecycler.setLayoutManager(layoutManager);
        
        
        /*---   CURRENT USER   ---*/
        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentUser = dataSnapshot.getValue(UserModel.class);

                /*---   ACTIVITY NAME   ---*/
                activityName.setText("@"+currentUser.getUserName());


                /*---   FOLLOWERS   ---*/
                if (dataSnapshot.child("Followers").exists()){

                    userRef.child(currentUid).child("Followers").addValueEventListener(new ValueEventListener() {
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
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                /*---   FOLLOWING   ---*/
                if (dataSnapshot.child("Following").exists()){

                    userRef.child(currentUid).child("Following").addValueEventListener(new ValueEventListener() {
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
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                /*---   HOOKUPS   ---*/
                if (dataSnapshot.child("Hookups").exists()){

                    userRef.child(currentUid).child("Hookups").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            int count = (int) dataSnapshot.getChildrenCount();
                            hookups.setText(String.valueOf(count));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }

                hookupsLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showUsersListIntent = new Intent(getContext(), UserListActivity.class);
                        showUsersListIntent.putExtra("Type", "Hookups");
                        showUsersListIntent.putExtra("CurrentUserId", currentUid);
                        startActivity(showUsersListIntent);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                /*---   PROFILE PICTURE   ---*/
                if (!currentUser.getProfilePictureThumb().equals("")){

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
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                /*---   PROFILE SETTING   ---*/
                profileSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent posterProfile = new Intent(getContext(), MyProfile.class);
                        startActivity(posterProfile);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                /*---   ACCOUNT SETTING   ---*/
                accountSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent accountIntent = new Intent(getContext(), AccountSettings.class);
                        startActivity(accountIntent);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        return v;
    }
}
