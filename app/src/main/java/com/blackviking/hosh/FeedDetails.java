package com.blackviking.hosh;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Model.CommentModel;
import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.Settings.Help;
import com.blackviking.hosh.ViewHolder.CommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FeedDetails extends AppCompatActivity {

    private ImageView exitActivity, help, postImage, likeBtn, sendComment, options;
    private CircleImageView posterImage;
    private TextView activityName, posterName, postText, likeCount, commentCount, postTime;
    private EditText commentBox;
    private RelativeLayout rootLayout;
    private RecyclerView commentRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<CommentModel, CommentViewHolder> adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, hopdateRef, likeRef, commentRef;
    private String currentFeedId, currentUid;
    private HopdateModel currentHopdate;
    private CommentModel newComment;
    private String offenceString = "";

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

        setContentView(R.layout.activity_feed_details);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   INTENT DATA   ---*/
        currentFeedId = getIntent().getStringExtra("CurrentFeedId");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        userRef = db.getReference("Users");
        hopdateRef = db.getReference("Hopdate").child(currentFeedId);
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("HopdateComments");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.feedDetailsRootLayout);
        commentRecycler = (RecyclerView)findViewById(R.id.feedCommentRecycler);
        posterImage = (CircleImageView)findViewById(R.id.feedDetailPosterImage);
        postImage = (ImageView)findViewById(R.id.feedDetailPostImage);
        likeBtn = (ImageView)findViewById(R.id.feedDetailLikeBtn);
        sendComment = (ImageView)findViewById(R.id.sendCommentBtn);
        options = (ImageView)findViewById(R.id.feedDetailOptions);
        posterName = (TextView)findViewById(R.id.feedDetailPosterUsername);
        postText = (TextView)findViewById(R.id.feedDetailPostText);
        likeCount = (TextView)findViewById(R.id.feedDetailLikesCount);
        commentCount = (TextView)findViewById(R.id.feedDetailCommentCount);
        postTime = (TextView)findViewById(R.id.feedDetailPostTime);
        commentBox = (EditText)findViewById(R.id.commentBox);


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Hopdate Info");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(FeedDetails.this, Faq.class);
                startActivity(helpIntent);
            }
        });

        /*---   SEND COMMENT   ---*/
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTheComment();
            }
        });

        loadCurrentHopdate();
        loadComments();
    }

    private void sendTheComment() {

        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy HH:mm");
        final String dateString = sdf.format(date);

        currentUid = mAuth.getCurrentUser().getUid();

        final String theComment = commentBox.getText().toString();

        if (Common.isConnectedToInternet(getBaseContext())){

            if (!TextUtils.isEmpty(theComment)){

                newComment = new CommentModel(theComment, currentUid, dateString);
                commentRef.child(currentFeedId).push().setValue(newComment);
                commentBox.setText("");

            }

        }

    }

    private void loadComments() {

        /*---   RECYCLER CONTROLLER   ---*/
        commentRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<CommentModel, CommentViewHolder>(
                CommentModel.class,
                R.layout.comment_item,
                CommentViewHolder.class,
                commentRef.child(currentFeedId)
        ) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, CommentModel model, int position) {
                viewHolder.time.setText(model.getCommentTime());
                viewHolder.comment.setText(model.getComment());

                userRef.child(model.getCommenter()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.username.setText(dataSnapshot.child("userName").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        commentRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void loadCurrentHopdate() {

        hopdateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentHopdate = dataSnapshot.getValue(HopdateModel.class);

                /*---   POSTER DETAILS   ---*/
                userRef.child(currentHopdate.getSender()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String imageThumbLink = dataSnapshot.child("profilePictureThumb").getValue().toString();
                        final String username = dataSnapshot.child("userName").getValue().toString();

                        if (!imageThumbLink.equals("")){

                            Picasso.with(getBaseContext())
                                    .load(imageThumbLink)
                                    .placeholder(R.drawable.ic_loading_animation)
                                    .into(posterImage);

                        }

                        posterName.setText(username);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                /*---   OPTIONS   ---*/
                if (currentHopdate.getSender().equals(currentUid)){

                    options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            /*---   POPUP MENU FOR HOPDATE   ---*/
                            PopupMenu popup = new PopupMenu(FeedDetails.this, options);
                            popup.inflate(R.menu.feed_item_menu);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_feed_delete:

                                            AlertDialog alertDialog = new AlertDialog.Builder(FeedDetails.this)
                                                    .setTitle("Delete Hopdate !")
                                                    .setIcon(R.drawable.ic_delete_feed)
                                                    .setMessage("Are You Sure You Want To Delete This Hopdate From Your Timeline?")
                                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            hopdateRef.removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            finish();
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
                                            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Hosh Share");
                                            i.putExtra(android.content.Intent.EXTRA_TEXT, "Check Out My New Story On HOSH Mobile App On PlayStore. ");
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

                } else {

                    options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        /*---   POPUP MENU FOR HOPDATE   ---*/
                            PopupMenu popup = new PopupMenu(FeedDetails.this, options);
                            popup.inflate(R.menu.feed_item_menu_other);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_feed_other_report:

                                            openReportDialog(currentHopdate.getSender());

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


                /*---   FEED DETAILS   ---*/
                /*---   POST IMAGE   ---*/
                if (!currentHopdate.getImageThumbUrl().equals("")){

                    Picasso.with(getBaseContext())
                            .load(currentHopdate.getImageUrl())
                            .placeholder(R.drawable.post_loading_icon)
                            .into(postImage);

                } else {

                    postImage.setVisibility(View.GONE);

                }


                /*---   HOPDATE   ---*/
                if (!currentHopdate.getHopdate().equals("")){

                    postText.setText(currentHopdate.getHopdate());

                } else {

                    postText.setVisibility(View.GONE);

                }


                /*---  TIME   ---*/
                postTime.setText(currentHopdate.getTimestamp());


                /*---   LIKES   ---*/
                likeRef.child(currentFeedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        likeCount.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()){

                            likeBtn.setImageResource(R.drawable.liked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(currentFeedId).child(currentUid).removeValue();
                                    Snackbar.make(rootLayout, "Un Liked", Snackbar.LENGTH_SHORT).show();
                                }
                            });

                        } else {

                            likeBtn.setImageResource(R.drawable.unliked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(currentFeedId).child(currentUid).setValue("liked");
                                    Snackbar.make(rootLayout, "Liked", Snackbar.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                /*---   COMMENTS   ---*/
                commentRef.child(currentFeedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int countComment = (int) dataSnapshot.getChildrenCount();

                        commentCount.setText(String.valueOf(countComment));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                /*---   POSTER DETS CLICK   ---*/
                if (currentHopdate.getSender().equals(currentUid)) {

                    /*---   POSTER NAME CLICK   ---*/
                    posterName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(FeedDetails.this, MyProfile.class);
                            startActivity(posterProfile);

                        }
                    });


                    /*---   POSTER IMAGE CLICK   ---*/
                    posterImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(FeedDetails.this, MyProfile.class);
                            startActivity(posterProfile);

                        }
                    });

                } else {

                    /*---   POSTER NAME CLICK   ---*/
                    posterName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(FeedDetails.this, OtherUserProfile.class);
                            posterProfile.putExtra("UserId", currentHopdate.getSender());
                            startActivity(posterProfile);

                        }
                    });


                    /*---   POSTER IMAGE CLICK   ---*/
                    posterImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent posterProfile = new Intent(FeedDetails.this, OtherUserProfile.class);
                            posterProfile.putExtra("UserId", currentHopdate.getSender());
                            startActivity(posterProfile);


                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openReportDialog(final String sender) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
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
        dataAdapterOffence = new ArrayAdapter(FeedDetails.this, android.R.layout.simple_spinner_item, offenceList);

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

                if (Common.isConnectedToInternet(getBaseContext())){

                    if (offenceString.equals("") || TextUtils.isEmpty(offenceDetails.getText().toString())){

                        Snackbar.make(rootLayout, "Invalid Report", Snackbar.LENGTH_SHORT).show();

                    } else {

                        final Map<String, Object> reportUserMap = new HashMap<>();
                        reportUserMap.put("reporter", currentUid);
                        reportUserMap.put("reported", sender);
                        reportUserMap.put("reportClass", offenceString);
                        reportUserMap.put("reportDetails", offenceDetails.getText().toString());

                        reportRef.push().setValue(reportUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(rootLayout, "Report Logged, Don't Be A Snitch", Snackbar.LENGTH_SHORT).show();
                            }
                        });

                    }

                }else {

                    Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
