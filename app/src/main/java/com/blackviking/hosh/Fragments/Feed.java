package com.blackviking.hosh.Fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.R;
import com.blackviking.hosh.ViewHolder.FeedViewHolder;
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

import io.paperdb.Paper;

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
    private DatabaseReference feedRef, userRef;


    public Feed() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);


        /*---   IMAGE FACE DETECTION   ---*/
        PicassoFaceDetector.initialize(getContext());


        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        feedRef = db.getReference("Hopdate");
        feedRef.keepSynced(true);
        userRef = db.getReference("Users");


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
                Snackbar.make(getView(), "Help Function Under Dev ", Snackbar.LENGTH_LONG).show();
            }
        });


        /*---   RECYCLER CONTROLLER   ---*/
        feedRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        feedRecycler.setLayoutManager(layoutManager);


        loadFeed();

        return v;
    }

    private void loadFeed() {

        adapter = new FirebaseRecyclerAdapter<HopdateModel, FeedViewHolder>(
                HopdateModel.class,
                R.layout.feed_item,
                FeedViewHolder.class,
                feedRef
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final HopdateModel model, int position) {

                if (model.getSender().equals(currentUid)){

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
                                        .transform(new FaceCenterCrop(400, 400))
                                        .into(viewHolder.posterImage, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(getContext())
                                                        .load(imageThumbLink)
                                                        .placeholder(R.drawable.ic_loading_animation)
                                                        .transform(new FaceCenterCrop(400, 400))
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
                    feedRef.child(feedId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Likes").exists()){

                                feedRef.child(feedId).child("Likes").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {

                                        int countLike = (int) dataSnapshot.getChildrenCount();

                                        viewHolder.likeCount.setText(String.valueOf(countLike));

                                        if (dataSnapshot.child(currentUid).exists()){

                                            viewHolder.likeBtn.setImageResource(R.drawable.liked_icon);

                                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    feedRef.child(feedId).child("Likes").child(currentUid).removeValue();
                                                    Snackbar.make(getView(), "Un Liked", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            viewHolder.likeBtn.setImageResource(R.drawable.unliked_icon);

                                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    feedRef.child(feedId).child("Likes").child(currentUid).setValue("liked");
                                                    Snackbar.make(getView(), "Liked", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {

                                feedRef.child(feedId).child("Likes").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {

                                        int countLike = (int) dataSnapshot.getChildrenCount();

                                        viewHolder.likeCount.setText(String.valueOf(countLike));

                                        if (dataSnapshot.child(currentUid).exists()){

                                            viewHolder.likeBtn.setImageResource(R.drawable.liked_icon);

                                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    feedRef.child(feedId).child("Likes").child(currentUid).removeValue();
                                                    Snackbar.make(getView(), "Un Liked", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            viewHolder.likeBtn.setImageResource(R.drawable.unliked_icon);

                                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    feedRef.child(feedId).child("Likes").child(currentUid).setValue("liked");
                                                    Snackbar.make(getView(), "Liked", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {

                    viewHolder.options.setVisibility(View.GONE);

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
                                        .transform(new FaceCenterCrop(400, 400))
                                        .into(viewHolder.posterImage, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(getContext())
                                                        .load(imageThumbLink)
                                                        .placeholder(R.drawable.ic_loading_animation)
                                                        .transform(new FaceCenterCrop(400, 400))
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
                    feedRef.child(feedId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Likes").exists()){

                                feedRef.child(feedId).child("Likes").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {

                                        int countLike = (int) dataSnapshot.getChildrenCount();

                                        viewHolder.likeCount.setText(String.valueOf(countLike));

                                        if (dataSnapshot.child(currentUid).exists()){

                                            viewHolder.likeBtn.setImageResource(R.drawable.liked_icon);

                                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    feedRef.child(feedId).child("Likes").child(currentUid).removeValue();
                                                    Snackbar.make(getView(), "Un Liked", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            viewHolder.likeBtn.setImageResource(R.drawable.unliked_icon);

                                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    feedRef.child(feedId).child("Likes").child(currentUid).setValue("liked");
                                                    Snackbar.make(getView(), "Liked", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        };
        feedRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

}
