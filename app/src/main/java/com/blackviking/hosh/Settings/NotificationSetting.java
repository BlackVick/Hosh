package com.blackviking.hosh.Settings;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.R;
import com.google.firebase.messaging.FirebaseMessaging;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NotificationSetting extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RelativeLayout rootLayout;
    private SwitchCompat notificationSwitch;

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

        setContentView(R.layout.activity_notification_setting);


        /*---   LOCAL   ---*/
        Paper.init(this);
        String isSubscribeToNoti = Paper.book().read(Common.ACCOUNT_NOTIFICATION);

        if (isSubscribeToNoti == null){

            Paper.book().write(Common.ACCOUNT_NOTIFICATION, "true");

        }


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.notificationRootLayout);
        notificationSwitch = (SwitchCompat)findViewById(R.id.notificationSwitch);


        /*---   NOTIFICATION SWITCH SETTINGS HANDLER   ---*/
        if (isSubscribeToNoti == null || TextUtils.isEmpty(isSubscribeToNoti) || isSubscribeToNoti.equals("false")) {
            notificationSwitch.setChecked(false);
        } else {
            notificationSwitch.setChecked(true);
        }

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    /*---   SUBSCRIBE TO NOTIFICATION   ---*/
                    Paper.book().write(Common.ACCOUNT_NOTIFICATION, "true");
                } else {
                    /*---   UNSUBSCRIBE FROM NOTIFICATION   ---*/
                    Paper.book().write(Common.ACCOUNT_NOTIFICATION, "false");
                }
            }
        });


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Notifications");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
