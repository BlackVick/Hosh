package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.Settings.AccountSetting;
import com.blackviking.hosh.Settings.AppInfo;
import com.blackviking.hosh.Settings.Help;
import com.blackviking.hosh.Settings.NotificationSetting;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AccountSettings extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RelativeLayout rootLayout, appInfoLayout;
    private LinearLayout accountLayout, notificationLayout, inviteLayout, helpLayout;

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

        setContentView(R.layout.activity_account_settings);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.accountSettingsRootLayout);
        appInfoLayout = (RelativeLayout)findViewById(R.id.appInfoLayout);
        accountLayout = (LinearLayout)findViewById(R.id.accountSettings);
        notificationLayout = (LinearLayout)findViewById(R.id.notificationSetting);
        inviteLayout = (LinearLayout)findViewById(R.id.inviteFriends);
        helpLayout = (LinearLayout)findViewById(R.id.help);



        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Settings");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setVisibility(View.GONE);
        helpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(AccountSettings.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        /*---   APP INFO   ---*/
        appInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent infoIntent = new Intent(AccountSettings.this, AppInfo.class);
                startActivity(infoIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        /*---   INVITE FRIEND   ---*/
        inviteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Hosh Invite");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey, \n \n Check Out My New Story On HOSH. You Can Download For Free On PlayStore And Connect With Other Hoshers. ");
                startActivity(Intent.createChooser(i,"Share via"));
            }
        });


        /*---   NOTIFICATION   ---*/
        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notificationIntent = new Intent(AccountSettings.this, NotificationSetting.class);
                startActivity(notificationIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        /*---   ACCOUNT SETTINGS   ---*/
        accountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountIntent = new Intent(AccountSettings.this, AccountSetting.class);
                startActivity(accountIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }
}
