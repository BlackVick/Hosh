package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Model.CommentModel;
import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.ViewHolder.CommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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


        /*---   IMAGE FACE DETECTION   ---*/
        PicassoFaceDetector.initialize(this);


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
                Snackbar.make(rootLayout, "Help Function Still Under Dev !", Snackbar.LENGTH_LONG).show();
            }
        });

        
        /*---   SEND COMMENT   ---*/
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTheComment();
            }
        });



        /*---   RECYCLER CONTROLLER   ---*/
        commentRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentRecycler.setLayoutManager(layoutManager);
        

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

                userRef.child(model.getCommenter()).addValueEventListener(new ValueEventListener() {
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
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.ic_loading_animation)
                                    .transform(new FaceCenterCrop(400, 400))
                                    .into(posterImage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getBaseContext())
                                                    .load(imageThumbLink)
                                                    .placeholder(R.drawable.ic_loading_animation)
                                                    .transform(new FaceCenterCrop(400, 400))
                                                    .into(posterImage);
                                        }
                                    });

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
                            Snackbar.make(rootLayout, "Still in dev !", Snackbar.LENGTH_LONG).show();
                        }
                    });

                } else {

                    options.setVisibility(View.GONE);

                }


                /*---   FEED DETAILS   ---*/
                /*---   POST IMAGE   ---*/
                if (!currentHopdate.getImageThumbUrl().equals("")){

                    Picasso.with(getBaseContext())
                            .load(currentHopdate.getImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.post_loading_icon)
                            .into(postImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(currentHopdate.getImageUrl())
                                            .placeholder(R.drawable.post_loading_icon)
                                            .into(postImage);
                                }
                            });

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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    @Override
    public void onBackPressed() {
        finish();
    }
}
