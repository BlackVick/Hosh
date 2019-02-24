package com.blackviking.hosh.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.FeedDetails;
import com.blackviking.hosh.Model.DataMessage;
import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.Model.MyResponse;
import com.blackviking.hosh.MyProfile;
import com.blackviking.hosh.Notification.APIService;
import com.blackviking.hosh.OtherUserProfile;
import com.blackviking.hosh.R;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.ViewHolder.FeedViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class Feed extends Fragment {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RecyclerView feedRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HopdateModel, FeedViewHolder> adapter;
    private String currentUid;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference feedRef, userRef, likeRef, commentRef;
    private String offenceString = "";
    private APIService mService;

    public Feed() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);


        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        currentUid = mAuth.getCurrentUser().getUid();

        feedRef = db.getReference("Hopdate");
        feedRef.keepSynced(true);
        userRef = db.getReference("Users");
        userRef.keepSynced(true);
        likeRef = db.getReference("Likes");
        likeRef.keepSynced(true);
        commentRef = db.getReference("HopdateComments");
        commentRef.keepSynced(true);


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)v.findViewById(R.id.exitActivity);
        help = (ImageView)v.findViewById(R.id.helpIcon);
        activityName = (TextView)v.findViewById(R.id.activityName);
        feedRecycler = (RecyclerView)v.findViewById(R.id.feedRecycler);


        /*---   EXIT   ---*/
        exitActivity.setImageResource(R.drawable.ic_action_close_app);
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Hosh Feed");


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faqIntent = new Intent(getContext(), Faq.class);
                startActivity(faqIntent);
            }
        });


        loadFeed();

        return v;
    }

    private void loadFeed() {

        /*---   RECYCLER CONTROLLER   ---*/
        feedRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        feedRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<HopdateModel, FeedViewHolder>(
                HopdateModel.class,
                R.layout.feed_item,
                FeedViewHolder.class,
                feedRef.limitToLast(50)
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final HopdateModel model, final int position) {

                /*---   OPTIONS   ---*/
                if (model.getSender().equals(currentUid)){

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

                                                            feedRef.child(adapter.getRef(position).getKey()).removeValue()
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

                                        case R.id.action_feed_stop_notification:

                                            openNotificationDialog(adapter.getRef(position).getKey());

                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });

                            popup.show();
                        }
                    });

                } else {

                    viewHolder.options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        /*---   POPUP MENU FOR HOPDATE   ---*/
                            PopupMenu popup = new PopupMenu(getContext(), viewHolder.options);
                            popup.inflate(R.menu.feed_item_menu_other);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_feed_other_report:

                                            openReportDialog(model.getSender());

                                            return true;
                                        case R.id.action_feed_other_share:

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

                }




                /*---   POSTER DETAILS   ---*/
                userRef.child(model.getSender()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String imageLink = dataSnapshot.child("profilePicture").getValue().toString();
                        final String imageThumbLink = dataSnapshot.child("profilePictureThumb").getValue().toString();
                        String username = dataSnapshot.child("userName").getValue().toString();

                        if (!imageThumbLink.equals("")){

                            Picasso.with(getContext())
                                    .load(imageThumbLink)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.ic_loading_animation)
                                    .into(viewHolder.posterImage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getContext())
                                                    .load(imageThumbLink)
                                                    .placeholder(R.drawable.ic_loading_animation)
                                                    .into(viewHolder.posterImage);
                                        }
                                    });

                        } else {

                            viewHolder.posterImage.setImageResource(R.drawable.empty_profile);

                        }

                        viewHolder.posterName.setText(username);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                    /*---   POST IMAGE   ---*/
                if (!model.getImageThumbUrl().equals("")){

                    viewHolder.postImage.setVisibility(View.VISIBLE);

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

                    viewHolder.postText.setVisibility(View.VISIBLE);
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

                                    if (!model.getSender().equalsIgnoreCase(currentUid)){
                                        sendLikeNotification(feedId);
                                    }
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


                if (model.getSender().equals(currentUid)) {

                    /*---   POSTER NAME CLICK   ---*/
                    viewHolder.posterName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(getContext(), MyProfile.class);
                            startActivity(posterProfile);

                        }
                    });


                    /*---   POSTER IMAGE CLICK   ---*/
                    viewHolder.posterImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(getContext(), MyProfile.class);
                            startActivity(posterProfile);

                        }
                    });

                } else {

                    /*---   POSTER NAME CLICK   ---*/
                    viewHolder.posterName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(getContext(), OtherUserProfile.class);
                            posterProfile.putExtra("UserId", model.getSender());
                            startActivity(posterProfile);

                        }
                    });


                    /*---   POSTER IMAGE CLICK   ---*/
                    viewHolder.posterImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(getContext(), OtherUserProfile.class);
                            posterProfile.putExtra("UserId", model.getSender());
                            startActivity(posterProfile);

                        }
                    });

                }

            }
        };
        feedRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void openNotificationDialog(final String key) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.feed_notification_layout,null);

        final RadioGroup notificationGroup = (RadioGroup) viewOptions.findViewById(R.id.notificationGroup);
        final RadioButton notificationOn = (RadioButton) viewOptions.findViewById(R.id.notificationOn);
        final RadioButton notificationOff = (RadioButton) viewOptions.findViewById(R.id.notificationOff);
        String notiState = Paper.book().read(Common.FEED_NOTIFICATION_TOPIC+key);

        /*---   NOTIFICATION SWITCH SETTINGS HANDLER   ---*/
        if (notiState == null || TextUtils.isEmpty(notiState) || notiState.equals("false")) {
            notificationOff.setChecked(true);
            notificationOn.setChecked(false);
        } else {
            notificationOff.setChecked(false);
            notificationOn.setChecked(true);
        }

        notificationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){

                    case R.id.notificationOn:
                        FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+key);
                        Paper.book().write(Common.FEED_NOTIFICATION_TOPIC+key, "true");

                    case R.id.notificationOff:
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+key);
                        Paper.book().write(Common.FEED_NOTIFICATION_TOPIC+key, "false");
                }
            }
        });

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 200; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        alertDialog.show();

    }

    private void openReportDialog(final String sender) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.report_form,null);

        final Spinner offenceClass = (Spinner) viewOptions.findViewById(R.id.reportTypeSpinner);
        final EditText offenceDetails = (EditText) viewOptions.findViewById(R.id.reportDetails);
        final Button submitReport = (Button) viewOptions.findViewById(R.id.submitReportBtn);
        final DatabaseReference reportRef = db.getReference("Reports");


        /*---   SETUP SPINNER   ---*/
        /*---   FILL GENDER SPINNER   ---*/
        List<String> offenceList = new ArrayList<>();
        offenceList.add(0, "Report Type");
        offenceList.add("Inappropriate Content");
        offenceList.add("Offensive Acts");
        offenceList.add("Bullying");

        ArrayAdapter<String> dataAdapterOffence;
        dataAdapterOffence = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, offenceList);

        dataAdapterOffence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        offenceClass.setAdapter(dataAdapterOffence);


        /*---    GENDER SPINNER   ---*/
        offenceClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Report Type")){

                    offenceString = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 200; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        submitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getContext())){

                    if (offenceString.equals("") || TextUtils.isEmpty(offenceDetails.getText().toString())){

                        Snackbar.make(getView(), "Invalid Report", Snackbar.LENGTH_SHORT).show();

                    } else {

                        final Map<String, Object> reportUserMap = new HashMap<>();
                        reportUserMap.put("reporter", currentUid);
                        reportUserMap.put("reported", sender);
                        reportUserMap.put("reportClass", offenceString);
                        reportUserMap.put("reportDetails", offenceDetails.getText().toString());

                        reportRef.push().setValue(reportUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(getView(), "Snitch", Snackbar.LENGTH_SHORT).show();
                            }
                        });

                    }

                }else {

                    Snackbar.make(getView(), "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    private void sendLikeNotification(final String feedId) {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("userName").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "Hosh Feed");
                dataSend.put("message", "@"+userName+" Just Liked Your Post");
                dataSend.put("feed_id", feedId);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.FEED_NOTIFICATION_TOPIC+feedId).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Snackbar.make(getView(), "Error Sending Notification !", Snackbar.LENGTH_LONG).show();
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
