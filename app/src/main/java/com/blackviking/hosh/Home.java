package com.blackviking.hosh;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Fragments.Account;
import com.blackviking.hosh.Fragments.Feed;
import com.blackviking.hosh.Fragments.HookUp;
import com.blackviking.hosh.Fragments.Hopdate;
import com.blackviking.hosh.Fragments.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;

import java.lang.reflect.Field;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity {

    private ImageView feed, hookup, hopdate, messaging, account;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private String currentUid;
    private RelativeLayout rootLayout;
    private BroadcastReceiver mMessageReceiver = null;

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

        setContentView(R.layout.activity_home);


        /*---   LOCAL   ---*/
        Paper.init(this);
        Paper.book().write(Common.APP_STATE, "Foreground");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");
        userRef.child(currentUid).child("onlineState").setValue("Online");


        /*---   SECURITY MEASURE   ---*/
        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String status = dataSnapshot.child("userType").getValue().toString();

                if (status.equalsIgnoreCase("Watched")){

                    FirebaseAuth.getInstance().signOut();
                    Paper.book().destroy();
                    Intent signoutIntent = new Intent(Home.this, Login.class);
                    signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signoutIntent);
                    finish();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   FRAGMENTS   ---*/
        final Feed feedFragment = new Feed();
        final HookUp hookupFragment = new HookUp();
        final Hopdate hopdateFragment = new Hopdate();
        final Messages messagesFragment = new Messages();
        final Account accountFragment = new Account();


        /*---   WIDGETS   ---*/
        feed = (ImageView)findViewById(R.id.feed);
        hookup = (ImageView)findViewById(R.id.hookup);
        hopdate = (ImageView)findViewById(R.id.hopdate);
        messaging = (ImageView)findViewById(R.id.messaging);
        account = (ImageView)findViewById(R.id.account);
        rootLayout = (RelativeLayout)findViewById(R.id.homeRootLayout);


        /*---   IN APP NOTIFICATION   ---*/
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("Message");

                Snackbar snackbar = Snackbar
                        .make(rootLayout, message, Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                sbView.setBackgroundColor(ContextCompat.getColor(Home.this, R.color.colorPrimaryDark));
                snackbar.setDuration(2500);
                snackbar.show();
            }
        };



        /*---   BOTTOM NAVIGATION   ---*/
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.color.bottom_nav_clicked);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(feedFragment);
            }
        });

        hookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.color.bottom_nav_clicked);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(hookupFragment);
            }
        });

        hopdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.color.bottom_nav_clicked);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(hopdateFragment);
            }
        });

        messaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.color.bottom_nav_clicked);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(messagesFragment);
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.color.bottom_nav_clicked);

                setFragment(accountFragment);
            }
        });


        /*---   SET BASE FRAGMENT   ---*/
        setBaseFragment(feedFragment);

    }

    private void setBaseFragment(Fragment feedFragment) {

        setFragment(feedFragment);

        feed.setBackgroundResource(R.color.bottom_nav_clicked);
        hookup.setBackgroundResource(R.drawable.white_grey_border_top);
        hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
        messaging.setBackgroundResource(R.drawable.white_grey_border_top);
        account.setBackgroundResource(R.drawable.white_grey_border_top);

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*---   ONLINE STATE   ---*/
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("NOTIFICATION_BROADCAST"));
        userRef.child(currentUid).child("onlineState").setValue("Online");
        Paper.book().write(Common.APP_STATE, "Foreground");
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*---   ONLINE STATE   ---*/
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        userRef.child(currentUid).child("onlineState").setValue("Offline");
        Paper.book().write(Common.APP_STATE, "Background");
    }

}
