package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.rey.material.widget.CheckBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NewGoogleUser extends AppCompatActivity {

    private MaterialEditText userName, eMail;
    private Button signUp;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private TextView activityName, policyText;
    private ImageView exitActivity, help;
    private RelativeLayout rootLayout;
    private String currentUid;
    private CheckBox privacy;
    private Spinner gender, interest;
    private String selectGender = "", selectInterest = "";

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

        setContentView(R.layout.activity_new_google_user);


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
        signUp = (Button)findViewById(R.id.registerBtn);
        signUp.setEnabled(false);
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.newGoogleRootLayout);
        privacy = (CheckBox)findViewById(R.id.privacyPolicyCheck);
        gender = (Spinner)findViewById(R.id.genderSpinner);
        interest = (Spinner)findViewById(R.id.interestSpinner);
        policyText = (TextView)findViewById(R.id.policyTextView);


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

                Intent helpIntent = new Intent(NewGoogleUser.this, Faq.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        /*---   EMAIL   ---*/
        if (mAuth.getCurrentUser() != null)
            eMail.setText(mAuth.getCurrentUser().getEmail());
        eMail.setEnabled(false);


        /*---   GENDER   ---*/
        /*---   FILL GENDER SPINNER   ---*/
        List<String> genderList = new ArrayList<>();
        genderList.add(0, "Gender");
        genderList.add("Male");
        genderList.add("Female");
        genderList.add("Indifferent");

        ArrayAdapter<String> dataAdapterGender;
        dataAdapterGender = new ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList);
        dataAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(dataAdapterGender);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Gender")){

                    selectGender = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        /*---   INTEREST   ---*/
        /*---   FILL INTEREST SPINNER   ---*/
        List<String> interestList = new ArrayList<>();
        interestList.add(0, "Interested In . .");
        interestList.add("Men");
        interestList.add("Women");
        interestList.add("Groupie");

        ArrayAdapter<String> dataAdapterInterest;
        dataAdapterInterest = new ArrayAdapter(this, android.R.layout.simple_spinner_item, interestList);
        dataAdapterInterest.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interest.setAdapter(dataAdapterInterest);
        interest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Interested In . .")){

                    selectInterest = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        /*---   PRIVACY SHIIIT   ---*/
        String text = "I have read and agreed to all the TERMS OF USE and PRIVACY POLICY";
        SpannableString ss = new SpannableString(text);
        ClickableSpan termsText = new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                String termUrl = "https://www.freeprivacypolicy.com/terms/view/9ed4017380d75a141eec8651ca9c2a5d";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(termUrl));
                startActivity(i);

            }
        };

        ClickableSpan privacyText = new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                String privacyUrl = "https://www.freeprivacypolicy.com/privacy/view/2fedeaa03584b40c4de4f0f6c6f09f2b";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(privacyUrl));
                startActivity(i);

            }
        };


        ss.setSpan(termsText, 34, 46, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacyText, 51, 65, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        policyText.setText(ss);
        policyText.setMovementMethod(LinkMovementMethod.getInstance());



        /*---   POLICY CHECK   ---*/
        privacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    signUp.setEnabled(true);

                } else {

                    signUp.setEnabled(false);

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

        } else if (selectGender.equalsIgnoreCase("")){

            Snackbar.make(rootLayout, "Are You Male Or Female?", Snackbar.LENGTH_LONG).show();

        } else if (selectInterest.equalsIgnoreCase("")){

            Snackbar.make(rootLayout, "What Are You Interested In?", Snackbar.LENGTH_LONG).show();

        } else {

            userRef.orderByChild("userName").equalTo(userNameTxt).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()){

                        userName.setTextColor(getResources().getColor(R.color.proceed));
                        UserModel newUser = new UserModel(userNameTxt, emailTxt, "", selectGender, "", "", "", "Online", "false", dateString, "",
                                                            "", "", "", "", "", "Customer", "", selectInterest, "Hello There! I am new to Hosh. Lets get chatting");
                        if (mAuth.getCurrentUser() != null)
                            currentUid = mAuth.getCurrentUser().getUid();

                        userRef.child(currentUid).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                currentUid = mAuth.getCurrentUser().getUid();
                                Paper.book().write(Common.USER_ID, currentUid);

                                Intent goToHome = new Intent(NewGoogleUser.this, Home.class);
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
