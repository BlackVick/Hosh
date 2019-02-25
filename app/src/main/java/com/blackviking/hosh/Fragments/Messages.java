package com.blackviking.hosh.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.Common.GetTimeAgo;
import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Messaging;
import com.blackviking.hosh.Model.MessageListModel;
import com.blackviking.hosh.R;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.ViewHolder.MessagesViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class Messages extends Fragment {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RecyclerView chatRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference chatRef, userRef, chatListRef, sessionIdRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseRecyclerAdapter<MessageListModel, MessagesViewHolder> adapter;
    private String currentUid;

    public Messages() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);


        /*---   FIREBASE   ---*/
        currentUid = mAuth.getCurrentUser().getUid();

        chatRef = db.getReference("Users").child(currentUid).child("Messages");
        chatListRef = db.getReference("Users").child(currentUid).child("MessageList");
        sessionIdRef = db.getReference("Users").child(currentUid).child("MessageSessions");
        userRef = db.getReference("Users");



        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)v.findViewById(R.id.exitActivity);
        help = (ImageView)v.findViewById(R.id.helpIcon);
        activityName = (TextView)v.findViewById(R.id.activityName);
        chatRecycler = (RecyclerView)v.findViewById(R.id.chatRecycler);


        /*---   EXIT   ---*/
        exitActivity.setImageResource(R.drawable.ic_action_close_app);
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Chats");


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faqIntent = new Intent(getContext(), Faq.class);
                startActivity(faqIntent);
            }
        });

        loadMessages();

        return v;
    }

    private void loadMessages() {

        /*---   RECYCLER HANDLER   ---*/
        chatRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);

        Query newestShit = chatListRef.orderByChild("timeStamp");

        adapter = new FirebaseRecyclerAdapter<MessageListModel, MessagesViewHolder>(
                MessageListModel.class,
                R.layout.messages_item,
                MessagesViewHolder.class,
                newestShit
        ) {
            @Override
            protected void populateViewHolder(final MessagesViewHolder viewHolder, final MessageListModel model, final int position) {

                final String messageId = adapter.getRef(position).getKey();


                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getTimeStamp();
                String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getActivity().getApplicationContext());


                if (model.getType().equals("Text")) {

                    viewHolder.pictureMsg.setVisibility(View.GONE);
                    viewHolder.theLastMessage.setVisibility(View.VISIBLE);
                    viewHolder.theLastMessage.setText(model.getMessage());
                    viewHolder.messageTimeStamp.setText(lastSeenTime);

                    if (model.getRead().equals("false")) {

                        viewHolder.theLastMessage.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

                    } else if (model.getRead().equals("true")) {

                        viewHolder.theLastMessage.setTextColor(getResources().getColor(R.color.black));

                    }
                } else if (model.getType().equals("Image")) {

                    viewHolder.pictureMsg.setVisibility(View.VISIBLE);
                    viewHolder.theLastMessage.setVisibility(View.GONE);

                }



                if (!model.getFrom().equals(currentUid)) {

                    userRef.child(model.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("userName").getValue().toString();
                            final String image = dataSnapshot.child("profilePictureThumb").getValue().toString();

                            viewHolder.friendUsername.setText(userName);

                            if (!image.equals("")) {

                                Picasso.with(getContext())
                                        .load(image)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.ic_loading_animation)
                                        .into(viewHolder.friendsPicture, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(getContext())
                                                        .load(image)
                                                        .placeholder(R.drawable.ic_loading_animation)
                                                        .into(viewHolder.friendsPicture);
                                            }
                                        });

                            }

                            viewHolder.setItemClickListener(new ItemClickListener() {
                                @Override
                                public void onClick(View view, int position, boolean isLongClick) {
                                    Intent messagingIntent = new Intent(getContext(), Messaging.class);
                                    messagingIntent.putExtra("UserId", model.getFrom());
                                    messagingIntent.putExtra("UserName", userName);
                                    startActivity(messagingIntent);
                                }
                            });

                            userRef.removeEventListener(this);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {

                    sessionIdRef.child(model.getSessionId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String friendId = dataSnapshot.child("friendId").getValue().toString();

                            userRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String userName = dataSnapshot.child("userName").getValue().toString();
                                    final String image = dataSnapshot.child("profilePictureThumb").getValue().toString();

                                    viewHolder.friendUsername.setText(userName);

                                    if (!image.equals("")) {

                                        Picasso.with(getContext())
                                                .load(image)
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .placeholder(R.drawable.ic_loading_animation)
                                                .into(viewHolder.friendsPicture, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(getContext())
                                                                .load(image)
                                                                .placeholder(R.drawable.ic_loading_animation)
                                                                .into(viewHolder.friendsPicture);
                                                    }
                                                });

                                    }

                                    viewHolder.setItemClickListener(new ItemClickListener() {
                                        @Override
                                        public void onClick(View view, int position, boolean isLongClick) {
                                            Intent messagingIntent = new Intent(getContext(), Messaging.class);
                                            messagingIntent.putExtra("UserId", friendId);
                                            messagingIntent.putExtra("UserName", userName);
                                            startActivity(messagingIntent);
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            sessionIdRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        };
        chatRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

}
