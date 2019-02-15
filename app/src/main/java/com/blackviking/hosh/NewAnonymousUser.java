package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.Settings.Faq;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NewAnonymousUser extends AppCompatActivity {

    private MaterialEditText userName, eMail, sex;
    private Button signUp;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private TextView activityName;
    private ImageView exitActivity, help;
    private RelativeLayout rootLayout;
    private String currentUid;
    private RadioGroup sexGroup;

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

        setContentView(R.layout.activity_new_anonymous_user);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");


        /*---   CURRENT USER   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        userName = (MaterialEditText)findViewById(R.id.userNameRegEdt);
        eMail = (MaterialEditText)findViewById(R.id.userEmailRegEdt);
        sex = (MaterialEditText)findViewById(R.id.userSexRegEdt);
        signUp = (Button)findViewById(R.id.registerBtn);
        sexGroup = (RadioGroup)findViewById(R.id.sexGroup);
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.newAnonymousRootLayout);


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Sign Up");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setImageResource(R.drawable.ic_destroy_activity);
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Paper.book().destroy();
                FirebaseAuth.getInstance().signOut();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent helpIntent = new Intent(NewAnonymousUser.this, Faq.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        /*---   EMAIL   ---*/
        if (mAuth.getCurrentUser() != null)
            eMail.setText("Anonymous");
        eMail.setEnabled(false);


        /*---   SEX   ---*/
        sex.setEnabled(false);
        sexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){

                    case R.id.maleRadio:
                        sex.setText("Male");
                        break;

                    case R.id.femaleRadio:
                        sex.setText("Female");
                        break;

                }
            }
        });


        /*---   SIGN UP   ---*/
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                    signUpUser();

                } else {

                    Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();

                }
            }
        });
    }

    private void signUpUser() {
        final String userNameTxt = userName.getText().toString();
        final String emailTxt = eMail.getText().toString();
        final String sexTxt = sex.getText().toString();

        /*---   DATE JOINED   ---*/
        final long date = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM, yyyy");
        final String dateString = sdf.format(date);


        /*---   CURRENT COUNTRY   ---*/
        //final String locale = getBaseContext().getResources().getConfiguration().locale.getDisplayCountry();


        if (TextUtils.isEmpty(userNameTxt)){

            Snackbar.make(rootLayout, "Please Pick A UserName", Snackbar.LENGTH_LONG).show();

        } else if (TextUtils.isEmpty(emailTxt)){

            Snackbar.make(rootLayout, "Not Possible", Snackbar.LENGTH_LONG).show();

        } else if (TextUtils.isEmpty(sexTxt)){

            Snackbar.make(rootLayout, "Are You Male Or Female?", Snackbar.LENGTH_LONG).show();

        } else {

            userRef.orderByChild("userName").equalTo(userNameTxt).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()){

                        userName.setTextColor(getResources().getColor(R.color.proceed));
                        UserModel newUser = new UserModel(userNameTxt, emailTxt, "", sexTxt, "", "", "", "true", "false", dateString, "",
                                "", "", "", "", "", "Customer", "", "", "Hello There! I am new to Hosh. Lets get chatting");
                        if (mAuth.getCurrentUser() != null)
                            currentUid = mAuth.getCurrentUser().getUid();

                        userRef.child(currentUid).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                currentUid = mAuth.getCurrentUser().getUid();
                                Paper.book().write(Common.USER_ID, currentUid);

                                Intent goToHome = new Intent(NewAnonymousUser.this, Home.class);
                                startActivity(goToHome);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        });

                    } else {

                        Snackbar.make(rootLayout, "Username Already Taken, Please Pick Another", Snackbar.LENGTH_LONG).show();
                        userName.setTextColor(getResources().getColor(R.color.red));

                    }
                    userRef.removeEventListener(this);
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public void onBackPressed() {

        Paper.book().destroy();
        FirebaseAuth.getInstance().signOut();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();

    }
}
