package com.blackviking.hosh;

import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;

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
    private DatabaseReference userListsRef;
    private String currentUid, activityType;

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

        setContentView(R.layout.activity_user_list);


        /*---   INTENT DATA   ---*/
        activityType = getIntent().getStringExtra("Type");
        currentUid = getIntent().getStringExtra("CurrentUserId");


        /*---   IMAGE FACE DETECTION   ---*/
        PicassoFaceDetector.initialize(this);


        /*---   FIREBASE   ---*/
        userListsRef = db.getReference("Users").child(currentUid).child(activityType);


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
                Snackbar.make(rootLayout, "Help Function Still Under Dev !", Snackbar.LENGTH_LONG).show();
            }
        });


        /*---   RECYCLER CONTROLLER   ---*/
        userListsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        userListsRecycler.setLayoutManager(layoutManager);
    }
}
