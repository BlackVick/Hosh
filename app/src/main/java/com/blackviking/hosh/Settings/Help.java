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
    private TextView faq, contactUs, privacyPolicy, appInfo, terms;

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
        terms = (TextView)findViewById(R.id.termOfUse);


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
            }
        });


        /*---   CONTACT US   ---*/
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                String[] to = {"blackvikinc@gmail.com"};
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

                String policyUrl = "https://www.freeprivacypolicy.com/privacy/view/2fedeaa03584b40c4de4f0f6c6f09f2b";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(policyUrl));
                startActivity(i);

            }
        });


        /*---   TERMS OF USE   ---*/
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String termsUrl = "https://www.freeprivacypolicy.com/terms/view/9ed4017380d75a141eec8651ca9c2a5d";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(termsUrl));
                startActivity(i);

            }
        });


        /*---   APP INFO   ---*/
        appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appInfoIntent = new Intent(Help.this, AppInfo.class);
                startActivity(appInfoIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
