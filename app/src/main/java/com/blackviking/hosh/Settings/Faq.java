package com.blackviking.hosh.Settings;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.R;
import com.blackviking.hosh.ViewHolder.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Faq extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName;
    private ExpandableListView listView;
    private ExpandableListAdapter adapter;
    private List<String> listFaqTitle;
    private HashMap<String, List<String>> listHash;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Wigrum-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_faq);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        listView = (ExpandableListView)findViewById(R.id.faqExpandable);


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("FAQ");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setVisibility(View.GONE);


        /*---   INITIALIZE EXPANDABLE   ---*/
        initData();
        adapter = new ExpandableListAdapter(this, listFaqTitle, listHash);
        listView.setAdapter(adapter);
    }

    private void initData() {

        listFaqTitle = new ArrayList<>();
        listHash = new HashMap<>();

        listFaqTitle.add("SIGNING UP");
        listFaqTitle.add("LOGGING IN");
        listFaqTitle.add("HOME");
        listFaqTitle.add("HOSH FEED");
        listFaqTitle.add("HOOKUP");
        listFaqTitle.add("HOPDATE");
        listFaqTitle.add("MESSAGING");
        listFaqTitle.add("ACCOUNT");


        List<String> signUp = new ArrayList<>();
        signUp.add("There are 3 sign up options");
        signUp.add("New users can sign up with either their google account, valid email address or anonymously");
        signUp.add("Google Step 1 : Click on the Google Sign In button below the Hosh logo");
        signUp.add("Google Step 2 : Select Account you want as your personal login Account");
        signUp.add("Google Step 3 : Fill in the details required by the app in the next page.");
        signUp.add("Email Step 1 : Click on the Email Sign In button below the Hosh logo");
        signUp.add("Email Step 2 : A verification would be sent to your mail.");
        signUp.add("Email Step 3 : After verifying Fill in the details required by the app in the next page.");
        signUp.add("Anonymous Step 1 : Click on the Anonymous Sign In button below the Hosh logo");
        signUp.add("Anonymous Step 2 : Fill in the details required by the app in the next page.");
        signUp.add("Anonymous Step 3 : The Catch here is, Once user signs out, All the details are gone");

        List<String> login = new ArrayList<>();
        login.add("Once user is signed up, logging in from another phone is quite easy.");
        login.add("Step 1 : Click the sign in button you used to sign up and you are taken to your homepage.");


        List<String> homePage = new ArrayList<>();
        homePage.add("The home page consist of (5) main Pages that can be accessed by clicking on the bottom navigation");
        homePage.add("1. Hosh Feed, 2. Hookup, 3. Hopdate, 4. Messages, 5. Account");
        homePage.add("1. Hosh Feed : This contains updates from every user on the Hosh App");
        homePage.add("2. Hookup : This is a search medium to find other users close to you");
        homePage.add("3. Hopdate : This is where you can add content to your timeline for everyone to see");
        homePage.add("4. Messages : This contains messages to and from every contact you make.");
        homePage.add("5. Account : This contains brief details about user and also timeline.");

        List<String> hoshFeed = new ArrayList<>();
        hoshFeed.add("Each feed can be ;");
        hoshFeed.add("1. Viewed for more details");
        hoshFeed.add("2. Liked / Unliked");
        hoshFeed.add("3. Commented on");
        hoshFeed.add("4. Shared");
        hoshFeed.add("5. Reported");
        hoshFeed.add("6. Viewing Poster's Profile is also possible");

        List<String> hookup = new ArrayList<>();
        hookup.add("The Search results on this page shows people you are interested in and is also based on proximity");

        List<String> hopdate = new ArrayList<>();
        hopdate.add("Hopdate can contain Image Only, Text Only or Both");

        List<String> messaging = new ArrayList<>();
        messaging.add("This contains all messages sent and received");

        List<String> account = new ArrayList<>();
        account.add("Here Users can find their details by clicking on the profile button and also make adjustments as they like");
        account.add("Each user also have an image gallery where they can pick their profile pictures from");
        account.add("Each user has a list of other users that are followers and they are following");


        listHash.put(listFaqTitle.get(0), signUp);
        listHash.put(listFaqTitle.get(1), login);
        listHash.put(listFaqTitle.get(2), homePage);
        listHash.put(listFaqTitle.get(3), hoshFeed);
        listHash.put(listFaqTitle.get(4), hookup);
        listHash.put(listFaqTitle.get(5), hopdate);
        listHash.put(listFaqTitle.get(6), messaging);
        listHash.put(listFaqTitle.get(7), account);

    }
}
