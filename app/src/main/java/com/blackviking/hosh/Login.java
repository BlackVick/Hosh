package com.blackviking.hosh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Settings.Faq;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
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
    private Button google, email, anonymous, emailSignUp, emailSignIn;
    private RoundKornerRelativeLayout buttonSLayout;
    private android.app.AlertDialog mDialog;
    private ImageView help;
    private LinearLayout emailChoiceLayout;

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
        help = (ImageView)findViewById(R.id.helpButtonLogin);
        emailSignIn = (Button)findViewById(R.id.emailSignInButton);
        emailSignUp = (Button)findViewById(R.id.emailSignUpButton);
        emailChoiceLayout = (LinearLayout) findViewById(R.id.emailChoiceLayout);


        /*---   BUTTON REVEAL TIME   ---*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonSLayout.setVisibility(View.VISIBLE);
            }
        }, REVEAL_BUTTONS_TIME);

        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Login.this, Faq.class);
                startActivity(helpIntent);
            }
        });

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

                emailChoiceLayout.setVisibility(View.VISIBLE);

            }
        });


        /*---   SIGN UP   ---*/
        emailSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choiceShit != null){

                    if (!choiceShit.isEmpty()){

                        if (choiceShit.equals("EMail")){

                            google.setEnabled(false);
                            email.setEnabled(false);
                            anonymous.setEnabled(false);

                            openEmailSignUpDialog();

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
                    openEmailSignUpDialog();

                }
            }
        });


        /*---   SIGN IN   ---*/
        emailSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choiceShit != null){

                    if (!choiceShit.isEmpty()){

                        if (choiceShit.equals("EMail")){

                            google.setEnabled(false);
                            email.setEnabled(false);
                            anonymous.setEnabled(false);

                            openEmailSignInDialog();

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
                    openEmailSignInDialog();

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

    private void openEmailSignInDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.email_sign_in_layout,null);

        final MaterialEditText theEmail = (MaterialEditText) viewOptions.findViewById(R.id.signInEmail);
        final MaterialEditText thePassword = (MaterialEditText) viewOptions.findViewById(R.id.signInPassword);
        final Button signInBtn = (Button) viewOptions.findViewById(R.id.signInEmailButton);
        final TextView resetPass = (TextView) viewOptions.findViewById(R.id.resetPassword);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(theEmail.getText().toString()) || !isValidEmail(theEmail.getText().toString())){

                    Snackbar.make(rootLayout, "Please Enter A Valid E-Mail Address", Snackbar.LENGTH_SHORT).show();

                } else {

                    mAuth.sendPasswordResetEmail(theEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        Snackbar.make(rootLayout, "Password Reset Instructions Have Been Sent To Your Mail !", Snackbar.LENGTH_SHORT).show();
                                        alertDialog.dismiss();

                                    } else {

                                        Snackbar.make(rootLayout, "Password Reset Failed !", Snackbar.LENGTH_SHORT).show();
                                        alertDialog.dismiss();

                                    }
                                }
                            });

                }

            }
        });


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                    if (TextUtils.isEmpty(theEmail.getText().toString()) || !isValidEmail(theEmail.getText().toString())){

                        Snackbar.make(rootLayout, "Please Enter A Valid E-Mail Address", Snackbar.LENGTH_SHORT).show();

                    } else if (TextUtils.isEmpty(thePassword.getText().toString()) || thePassword.getText().toString().length() < 6){

                        Snackbar.make(rootLayout, "Password Too weak Or Invalid", Snackbar.LENGTH_SHORT).show();

                    } else {

                        signInWithEmail(theEmail.getText().toString(), thePassword.getText().toString());
                        alertDialog.dismiss();

                    }

                }
            }
        });

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                google.setEnabled(true);
                email.setEnabled(true);
                anonymous.setEnabled(true);
                emailChoiceLayout.setVisibility(View.GONE);
            }
        });


        alertDialog.show();

    }

    private void openEmailSignUpDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.email_sign_up_layout,null);

        final MaterialEditText theEmail = (MaterialEditText) viewOptions.findViewById(R.id.signUpEmail);
        final MaterialEditText thePassword = (MaterialEditText) viewOptions.findViewById(R.id.signUpPassword);
        final Button signInBtn = (Button) viewOptions.findViewById(R.id.signUpEmailButton);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                    if (TextUtils.isEmpty(theEmail.getText().toString()) || !isValidEmail(theEmail.getText().toString())){

                        Snackbar.make(rootLayout, "Please Enter A Valid E-Mail Address", Snackbar.LENGTH_SHORT).show();

                    } else if (TextUtils.isEmpty(thePassword.getText().toString()) || thePassword.getText().toString().length() < 6){

                        Snackbar.make(rootLayout, "Password Too weak Or Invalid", Snackbar.LENGTH_SHORT).show();

                    } else {

                        signUpWithEmail(theEmail.getText().toString(), thePassword.getText().toString());
                        alertDialog.dismiss();

                    }

                }
            }
        });

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                google.setEnabled(true);
                email.setEnabled(true);
                anonymous.setEnabled(true);
                emailChoiceLayout.setVisibility(View.GONE);
            }
        });


        alertDialog.show();

    }

    private void signUpWithEmail(String s, String s1) {

        mAuth.createUserWithEmailAndPassword(s, s1).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            handleSignInResponse();

                        } else {

                            Snackbar.make(rootLayout, "Sign Up Failed", Snackbar.LENGTH_SHORT).show();

                        }
                    }
                }
        );

    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void signInAnonymously() {

        mDialog = new SpotsDialog(Login.this, "Processing . . .");
        mDialog.show();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            mDialog.dismiss();

                            Log.d(TAG, "signInAnonymously:success");

                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            authed.child(uid).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    /*---   STORE CHOICE   ---*/
                                    Paper.book().write(Common.SIGN_UP_CHOICE, "Anonymous");

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);

                                }
                            });



                        } else {

                            mDialog.dismiss();
                            Snackbar.make(rootLayout, "Anonymous Authentication failed.", Snackbar.LENGTH_LONG).show();

                        }

                    }
                });

    }

    private void signInWithEmail(String theEmail, String tehPassword) {

        mAuth.signInWithEmailAndPassword(theEmail, tehPassword).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            handleSignInResponse();

                        } else {

                            Snackbar.make(rootLayout, "Sign In Failed", Snackbar.LENGTH_SHORT).show();

                        }
                    }
                }
        );
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

            handleSignInResponse();
            return;

        }
    }

    private void handleSignInResponse() {

        mDialog = new SpotsDialog(Login.this, "Processing . . .");
        mDialog.show();

        if (mAuth.getCurrentUser() != null) {

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            authed.child(uid).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                        /*---   STORE CHOICE   ---*/
                    Paper.book().write(Common.SIGN_UP_CHOICE, "EMail");

                    if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){

                        mDialog.dismiss();
                        FirebaseAuth.getInstance().getCurrentUser()
                                .sendEmailVerification();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        updateUI(user);

                    } else {

                        mDialog.dismiss();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        updateUI(user);

                    }

                }
            });

        } else {

            mDialog.dismiss();

        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        mDialog = new SpotsDialog(Login.this, "Processing . . .");
        mDialog.show();

        Log.d("LOGIN", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                                mDialog.dismiss();
                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                authed.child(uid).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        /*---   STORE CHOICE   ---*/
                                        Paper.book().write(Common.SIGN_UP_CHOICE, "Google");

                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);

                                    }
                                });




                            } else {

                                mDialog.dismiss();

                            }

                        } else {

                            mDialog.dismiss();
                            Snackbar.make(rootLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void updateUI(final FirebaseUser user) {

        if (FirebaseAuth.getInstance().getCurrentUser() !=  null){

            mDialog = new SpotsDialog(Login.this, "Processing . . .");
            mDialog.show();

            final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final String signInChoice = Paper.book().read(Common.SIGN_UP_CHOICE);


            if (signInChoice.equals("Google")){

                userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){

                            mDialog.dismiss();
                            Intent newGoogleUser = new Intent(Login.this, NewGoogleUser.class);
                            startActivity(newGoogleUser);
                            finish();

                        } else {

                            mDialog.dismiss();
                            Paper.book().write(Common.USER_ID, currentUid);

                            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
                            Paper.book().write(Common.NOTIFICATION_STATE, "true");

                            Intent goToHome = new Intent(Login.this, Home.class);
                            startActivity(goToHome);
                            finish();

                        }

                        userRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else if (signInChoice.equals("EMail")){

                userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){

                            mDialog.dismiss();
                            Intent newGoogleUser = new Intent(Login.this, NewEmailUser.class);
                            startActivity(newGoogleUser);
                            finish();

                        } else {

                            mDialog.dismiss();
                            Paper.book().write(Common.USER_ID, currentUid);

                            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
                            Paper.book().write(Common.NOTIFICATION_STATE, "true");

                            Intent goToHome = new Intent(Login.this, Home.class);
                            startActivity(goToHome);
                            finish();

                        }

                        userRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else if (signInChoice.equals("Anonymous")){

                userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){

                            mDialog.dismiss();
                            Intent newFacebookUser = new Intent(Login.this, NewAnonymousUser.class);
                            startActivity(newFacebookUser);
                            finish();

                        } else {

                            mDialog.dismiss();
                            Paper.book().write(Common.USER_ID, currentUid);

                            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
                            Paper.book().write(Common.NOTIFICATION_STATE, "true");

                            Intent goToHome = new Intent(Login.this, Home.class);
                            startActivity(goToHome);
                            finish();

                        }

                        userRef.removeEventListener(this);
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
        if (localUser != null && mAuth.getCurrentUser() != null){

            if (!localUser.isEmpty()){

                Intent goToHome = new Intent(Login.this, Home.class);
                startActivity(goToHome);
                finish();

            }

        }
    }
}
