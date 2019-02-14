package com.blackviking.hosh.Settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.ImageGallery;
import com.blackviking.hosh.Login;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AccountSetting extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName, anonymousType, anonymousWarning, normalType, logout, username, location;
    private LinearLayout normalLayout, anonymousLayout, locationLayout;
    private RelativeLayout rootLayout;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private String signUpChoice, currentUid, selectedLocation;
    private UserModel currentUser;

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

        setContentView(R.layout.activity_account_setting);


        /*---   LOCAL   ---*/
        Paper.init(this);
        signUpChoice = Paper.book().read(Common.SIGN_UP_CHOICE);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.accountSettRootLayout);
        anonymousType = (TextView)findViewById(R.id.accountTypeAnonymous);
        anonymousWarning = (TextView)findViewById(R.id.accountTypeAnonymousWarning);
        username = (TextView)findViewById(R.id.accountUsername);
        normalType = (TextView)findViewById(R.id.accountTypeNormal);
        logout = (TextView)findViewById(R.id.logout);
        location = (TextView)findViewById(R.id.accountLocation);
        normalLayout = (LinearLayout)findViewById(R.id.accountTypeNormalLayout);
        anonymousLayout = (LinearLayout)findViewById(R.id.accountTypeAnonymousLayout);
        locationLayout = (LinearLayout)findViewById(R.id.locationLayout);


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Account Settings");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setVisibility(View.GONE);


        /*---   LOGOUT   ---*/
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogForLogout();
            }
        });


        loadAccount(currentUid, signUpChoice);
    }

    private void openDialogForLogout() {

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Log Out !")
                .setIcon(R.drawable.ic_log_out)
                .setMessage("Are You Sure You Want To Log Out?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseAuth.getInstance().signOut();
                        Paper.book().destroy();
                        Intent signoutIntent = new Intent(AccountSetting.this, Login.class);
                        signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(signoutIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();

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

    }

    private void loadAccount(String currentUid, final String signUpChoice) {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentUser = dataSnapshot.getValue(UserModel.class);

                username.setText("@"+currentUser.getUserName());


                /*---   LOCATION   ---*/
                location.setText(currentUser.getLocation());
                locationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openLocationDialog();
                    }
                });


                /*---   ACCOUNT TYPE   ---*/
                if (signUpChoice.equals("Anonymous")){

                    anonymousLayout.setVisibility(View.VISIBLE);
                    normalLayout.setVisibility(View.GONE);

                    anonymousType.setText(signUpChoice);

                    /*---   PRIVACY SHIIIT   ---*/
                    String text = "Your account still has an anonymous ID. So you will lose all your data if you log out. Consider converting to a PERMANENT ACCOUNT.";
                    SpannableString ss = new SpannableString(text);
                    ClickableSpan switchAccount = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {

                            openChangeAccountDialog();

                        }
                    };
                    ss.setSpan(switchAccount, 112, 129, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    anonymousWarning.setText(ss);
                    anonymousWarning.setMovementMethod(LinkMovementMethod.getInstance());


                } else {

                    anonymousLayout.setVisibility(View.GONE);
                    normalLayout.setVisibility(View.VISIBLE);

                    normalType.setText(signUpChoice);

                }


                userRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openLocationDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.location_edit,null);

        final Spinner locationSpinner = (Spinner)viewOptions.findViewById(R.id.locationSpinner);
        final Button saveLocation = (Button)viewOptions.findViewById(R.id.saveLocation);


        /*---   FILL LOCATION LIST   ---*/
        List<String> locationList = new ArrayList<>();
        locationList.add(0, "Location");
        locationList.add("Abuja");
        locationList.add("Abia");
        locationList.add("Adamawa");
        locationList.add("Akwa Ibom");
        locationList.add("Anambra");
        locationList.add("Bauchi");
        locationList.add("Benue");
        locationList.add("Borno");
        locationList.add("Cross River");
        locationList.add("Ebonyi");
        locationList.add("Ekiti");
        locationList.add("Jigawa");
        locationList.add("Kogi");
        locationList.add("Kwara");
        locationList.add("Lagos");
        locationList.add("Niger");
        locationList.add("Ogun");
        locationList.add("Ondo");
        locationList.add("Osun");
        locationList.add("Oyo");

        ArrayAdapter<String> dataAdapterLocation;
        dataAdapterLocation = new ArrayAdapter(this, android.R.layout.simple_spinner_item, locationList);

        dataAdapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        locationSpinner.setAdapter(dataAdapterLocation);


        /*---   LISTENER   ---*/
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Location")){

                    selectedLocation = parent.getItemAtPosition(position).toString();

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
        layoutParams.y = 180; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        /*---   SAVE LOCATION   ---*/
        saveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!selectedLocation.equals(dataSnapshot.child("location").getValue().toString())){

                            userRef.child(currentUid).child("location").setValue(selectedLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Snackbar.make(rootLayout, "Location changes might take a few minutes to reflect !", Snackbar.LENGTH_LONG).show();
                                    alertDialog.dismiss();
                                }
                            });

                        } else {

                            alertDialog.dismiss();

                        }

                        userRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });


        alertDialog.show();

    }

    private void openChangeAccountDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.account_choice,null);

        final ImageView google = (ImageView) viewOptions.findViewById(R.id.googleAccount);
        final ImageView email = (ImageView) viewOptions.findViewById(R.id.emailAccount);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 180; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);



        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(AccountSetting.this)){



                }else {

                    Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();

            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(AccountSetting.this)){



                }else {

                    Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }
}
