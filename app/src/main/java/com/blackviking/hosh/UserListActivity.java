package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.ViewHolder.UserListViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserListActivity extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RelativeLayout rootLayout;
    private RecyclerView userListsRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userListsRef, userRef;
    private String currentUid, activityType;
    private FirebaseRecyclerAdapter<UserModel, UserListViewHolder> adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Thin.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_user_list);


        /*---   INTENT DATA   ---*/
        activityType = getIntent().getStringExtra("Type");
        currentUid = getIntent().getStringExtra("CurrentUserId");

        /*---   FIREBASE   ---*/
        userListsRef = db.getReference("Users").child(currentUid).child(activityType);
        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.userListsRootLayout);
        userListsRecycler = (RecyclerView)findViewById(R.id.userListsRecycler);


        /*---   ACTIVITY NAME   ---*/
        activityName.setText(activityType);


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
                Intent helpIntent = new Intent(UserListActivity.this, Faq.class);
                startActivity(helpIntent);
            }
        });

        loadUserList();
    }

    private void loadUserList() {

        /*---   RECYCLER CONTROLLER   ---*/
        userListsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        userListsRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<UserModel, UserListViewHolder>(
                UserModel.class,
                R.layout.user_item_layout,
                UserListViewHolder.class,
                userListsRef
        ) {
            @Override
            protected void populateViewHolder(final UserListViewHolder viewHolder, UserModel model, int position) {

                final String userId = adapter.getRef(position).getKey();

                userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theUserName = dataSnapshot.child("userName").getValue().toString();
                        String theUserStatus = dataSnapshot.child("status").getValue().toString();
                        String theUserOnlineStatus = dataSnapshot.child("onlineState").getValue().toString();
                        String theUserImage = dataSnapshot.child("profilePictureThumb").getValue().toString();

                        viewHolder.name.setText("@"+theUserName);
                        viewHolder.status.setText(theUserStatus);
                        viewHolder.onlineStat.setText(theUserOnlineStatus);

                        if (!theUserImage.equals("")){

                            Picasso.with(getBaseContext())
                                    .load(theUserImage)
                                    .placeholder(R.drawable.ic_loading_animation)
                                    .into(viewHolder.userImage);

                        }

                        viewHolder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View view, int position, boolean isLongClick) {
                                Intent userIntent = new Intent(UserListActivity.this, OtherUserProfile.class);
                                userIntent.putExtra("UserId", userId);
                                startActivity(userIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        userListsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
