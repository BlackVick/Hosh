package com.blackviking.hosh.Settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Help extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RelativeLayout rootLayout;
    private TextView faq, contactUs, privacyPolicy, appInfo;

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

        setContentView(R.layout.activity_help);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.helpRootLayout);
        faq = (TextView)findViewById(R.id.faq);
        contactUs = (TextView)findViewById(R.id.contactUs);
        privacyPolicy = (TextView)findViewById(R.id.privacyPolicy);
        appInfo = (TextView)findViewById(R.id.appInfo);


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Help");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setVisibility(View.GONE);


        /*---   FAQ   ---*/
        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faqIntent = new Intent(Help.this, Faq.class);
                startActivity(faqIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        /*---   CONTACT US   ---*/
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                String[] to = {"bv.inc@gmail.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, to);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Hosh !");
                intent.putExtra(Intent.EXTRA_TEXT, "Please Describe Your Enquiry Below, Also Provide Screenshots If Available As Well For Complaints");
                intent.setType("message/rfc822");
                Intent chooser = Intent.createChooser(intent, "Send Request");
                startActivity(chooser);
            }
        });


        /*---   PRIVACY POLICY   ---*/
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String policyUrl = "http://www.teenqtech.com/customer-service-privacy-policy";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(policyUrl));
                startActivity(i);

            }
        });


        /*---   APP INFO   ---*/
        appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appInfoIntent = new Intent(Help.this, AppInfo.class);
                startActivity(appInfoIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }
}
