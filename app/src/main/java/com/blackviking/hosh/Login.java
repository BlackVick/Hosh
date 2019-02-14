package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.blackviking.hosh.Common.Common;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {

    private RelativeLayout rootLayout;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private static final int PER_LOGIN = 1000;
    private static final int ANON_SIGN = 2309;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, authed;
    private String TAG = "Hosh:Login";
    private static final int REVEAL_BUTTONS_TIME = 2500;
    private Button google, email, anonymous;
    private RoundKornerRelativeLayout buttonSLayout;

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

        setContentView(R.layout.activity_login);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        authed = db.getReference("AuthedUsers");


        /*---   WIDGETS   ---*/
        google = (Button)findViewById(R.id.signInWithGoogle);
        email = (Button)findViewById(R.id.signInWithEmail);
        anonymous = (Button)findViewById(R.id.signInAnonymously);
        rootLayout = (RelativeLayout)findViewById(R.id.loginRootLayout);
        buttonSLayout = (RoundKornerRelativeLayout)findViewById(R.id.loginButtonLayout);


        /*---   BUTTON REVEAL TIME   ---*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonSLayout.setVisibility(View.VISIBLE);
            }
        }, REVEAL_BUTTONS_TIME);


        /*---   GOOGLE API INITIALIZATION   ---*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Snackbar.make(rootLayout, "Unknown Error Occurred", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        /*---   CHOICE   ---*/
        final String choiceShit = Paper.book().read(Common.SIGN_UP_CHOICE);


        /*---   WITH GOOGLE   ---*/
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choiceShit != null){

                    if (!choiceShit.isEmpty()){

                        if (choiceShit.equals("Google")){

                            google.setEnabled(false);
                            email.setEnabled(false);
                            anonymous.setEnabled(false);
                            signInWithGoogle();

                        } else if (choiceShit.equals("EMail")){

                            Snackbar.make(rootLayout, "Sorry Mate You Already Signed In With Email, Please Continue Registration", Snackbar.LENGTH_LONG).show();

                        } else if (choiceShit.equals("Facebook")){

                            Snackbar.make(rootLayout, "Sorry Mate You Already Signed In With Anonymous, Please Continue Registration", Snackbar.LENGTH_LONG).show();

                        }

                    }

                } else {

                    google.setEnabled(false);
                    email.setEnabled(false);
                    anonymous.setEnabled(false);
                    signInWithGoogle();

                }

            }
        });


        /*---   WITH EMAIL   ---*/
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choiceShit != null){

                    if (!choiceShit.isEmpty()){

                        if (choiceShit.equals("EMail")){

                            google.setEnabled(false);
                            email.setEnabled(false);
                            anonymous.setEnabled(false);
                            signInWithEmail();

                        } else if (choiceShit.equals("Google")){

                            Snackbar.make(rootLayout, "Sorry Mate You Already Signed In With Gmail, Please Continue Registration", Snackbar.LENGTH_LONG).show();

                        } else if (choiceShit.equals("Facebook")){

                            Snackbar.make(rootLayout, "Sorry Mate You Already Signed In With Anonymous, Please Continue Registration", Snackbar.LENGTH_LONG).show();

                        }

                    }

                } else {

                    google.setEnabled(false);
                    email.setEnabled(false);
                    anonymous.setEnabled(false);
                    signInWithEmail();

                }

            }
        });


        /*---   ANONYMOUS   ---*/
        anonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choiceShit != null){

                    if (!choiceShit.isEmpty()){

                        if (choiceShit.equals("Anonymous")){

                            google.setEnabled(false);
                            email.setEnabled(false);
                            anonymous.setEnabled(false);
                            //displayHash();  //to display hashkey of app for facebook login
                            signInAnonymously();

                        } else if (choiceShit.equals("Google")){

                            Snackbar.make(rootLayout, "Sorry Mate You Already Signed In With Gmail, Please Continue Registration", Snackbar.LENGTH_LONG).show();

                        } else if (choiceShit.equals("EMail")){

                            Snackbar.make(rootLayout, "Sorry Mate You Already Signed In With Email, Please Continue Registration", Snackbar.LENGTH_LONG).show();

                        }

                    }

                } else {

                    google.setEnabled(false);
                    email.setEnabled(false);
                    anonymous.setEnabled(false);
                    //displayHash();  //to display hashkey of app for facebook login
                    signInAnonymously();

                }
            }
        });

    }

    private void signInAnonymously() {

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInAnonymously:success");

                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            authed.child(uid).setValue("true");

                            /*---   STORE CHOICE   ---*/
                            Paper.book().write(Common.SIGN_UP_CHOICE, "Anonymous");

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {

                            Snackbar.make(rootLayout, "Anonymous Authentication failed.", Snackbar.LENGTH_LONG).show();

                        }

                    }
                });

    }

    private void signInWithEmail() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAllowNewEmailAccounts(true)
                .setLogo(R.drawable.hosh_colored_logo)
                .setTheme(R.style.EmailBackground)
                .build(), PER_LOGIN);
    }

    private void signInWithGoogle() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()){

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {

                google.setEnabled(true);
                email.setEnabled(true);
                anonymous.setEnabled(true);
                Snackbar.make(rootLayout, "Google Sign In Failed", Snackbar.LENGTH_LONG).show();

            }
        } else if (requestCode == PER_LOGIN){

            handleSignInResponse(resultCode, data);
            return;

        }
    }

    private void handleSignInResponse(int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                authed.child(uid).setValue("true");

                /*---   STORE CHOICE   ---*/
                Paper.book().write(Common.SIGN_UP_CHOICE, "EMail");

                if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){

                    FirebaseAuth.getInstance().getCurrentUser()
                            .sendEmailVerification();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    updateUI(user);

                } else {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    updateUI(user);

                }

            }



        } else {

            Snackbar.make(rootLayout, "Login Failed !", Snackbar.LENGTH_LONG).show();
            finish();

        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("LOGIN", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                authed.child(uid).setValue("true");


                                /*---   STORE CHOICE   ---*/
                                Paper.book().write(Common.SIGN_UP_CHOICE, "Google");

                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);

                            }

                        } else {

                            Snackbar.make(rootLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void updateUI(final FirebaseUser user) {

        if (FirebaseAuth.getInstance().getCurrentUser() !=  null){

            final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final String signInChoice = Paper.book().read(Common.SIGN_UP_CHOICE);

            if (signInChoice.equals("Google")){

                userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            Paper.book().write(Common.USER_ID, currentUid);
                            Intent goToHome = new Intent(Login.this, Home.class);
                            startActivity(goToHome);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();

                        } else {

                            Intent newGoogleUser = new Intent(Login.this, NewGoogleUser.class);
                            startActivity(newGoogleUser);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else if (signInChoice.equals("EMail")){

                userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            Paper.book().write(Common.USER_ID, currentUid);
                            Intent goToHome = new Intent(Login.this, Home.class);
                            startActivity(goToHome);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();

                        } else {

                            Intent newGoogleUser = new Intent(Login.this, NewEmailUser.class);
                            startActivity(newGoogleUser);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else if (signInChoice.equals("Anonymous")){

                userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            Paper.book().write(Common.USER_ID, currentUid);
                            Intent goToHome = new Intent(Login.this, Home.class);
                            startActivity(goToHome);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();

                        } else {

                            Intent newFacebookUser = new Intent(Login.this, NewAnonymousUser.class);
                            startActivity(newFacebookUser);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        final String localUser = Paper.book().read(Common.USER_ID);
        if (localUser != null){

            if (!localUser.isEmpty()){

                Intent goToHome = new Intent(Login.this, Home.class);
                startActivity(goToHome);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }

        }
    }
}
