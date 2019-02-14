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
        listFaqTitle.add("FORGOTTEN PASSWORD");
        listFaqTitle.add("HOME PAGE");
        listFaqTitle.add("COURSE OPTIONS");
        listFaqTitle.add("DOWNLOADED FILES");
        listFaqTitle.add("CONTACT US");


        List<String> signUp = new ArrayList<>();
        signUp.add("Users are required to sign up with their Google Certified Mail");
        signUp.add("Step 1 : Click on the Google Sign In button below the logo");
        signUp.add("Step 2 : Select Account you want as your personal login Account");
        signUp.add("Step 3 : Fill in the details required by the app in the next page.");

        List<String> login = new ArrayList<>();
        login.add("Once user is signed up, you are directed to another page where you will Login");
        login.add("Step 1 : Click on the Google Sign In button below the logo");
        login.add("Step 2 : Enter Your Password and click the login button");

        List<String> forgotPass = new ArrayList<>();
        forgotPass.add("Step 1 : Click on the Forgot Password link");
        forgotPass.add("Step 2 : Provide an answer to the Secure Question");
        forgotPass.add("Step 3 : Enter the recovered password and login");

        List<String> homePage = new ArrayList<>();
        homePage.add("The home page consist of (2) main Pages that can be accessed by swiping left or right");
        homePage.add("1 : Courses Page");
        homePage.add("Users can click on any of the courses they would love to explore");
        homePage.add("2 : News Page");
        homePage.add("Users can read up on the latest news and also add to the news feed simply by; ");
        homePage.add("Step 1 : Click on the button at the bottom right of the news page");
        homePage.add("Step 2 : Enter the full description and topic of the news");
        homePage.add("Images can be included if available");
        homePage.add("PLEASE NOTE : That both the topic and the description are censored so, please you are advised to use maturely");

        List<String> coursesOptions = new ArrayList<>();
        coursesOptions.add("Each course has (5) different options which include; ");
        coursesOptions.add("1. Materials");
        coursesOptions.add("Materials can be downloaded easily from the materials page");
        coursesOptions.add("2. Audio Tutorials");
        coursesOptions.add("Audio tutorials are audio lessons provided by the lecturer and can also be downloaded easily");
        coursesOptions.add("3. Past Questions and Solutions");
        coursesOptions.add("The Past Questions and solutions are also available for download on this page");
        coursesOptions.add("4. Practice Test");
        coursesOptions.add("Practice tests are online quiz users are allowed to take and scored immediately based on their performances");
        coursesOptions.add("5. Group Chat");
        coursesOptions.add("Group Chats are available to every user for different courses");

        List<String> downloadedFiles = new ArrayList<>();
        downloadedFiles.add("Downloaded Files can only be accessed through the SmartLinks App");
        downloadedFiles.add("The downloaded files are accessible by;");
        downloadedFiles.add("Step 1 : Click on the 3Stack icon on the top left part of the Home Page");
        downloadedFiles.add("Step 2 : Click on Downloads");

        List<String> contactUs = new ArrayList<>();
        contactUs.add("Users can contact us by sending us a mail directly from the map");
        contactUs.add("Click on the 3Dot menu on the Home Page located at the top right corner of the Home Page");
        contactUs.add("Step 1 : Click on the Help Menu");
        contactUs.add("Step 2 : Click on Contact Us");


        listHash.put(listFaqTitle.get(0), signUp);
        listHash.put(listFaqTitle.get(1), login);
        listHash.put(listFaqTitle.get(2), forgotPass);
        listHash.put(listFaqTitle.get(3), homePage);
        listHash.put(listFaqTitle.get(4), coursesOptions);
        listHash.put(listFaqTitle.get(5), downloadedFiles);
        listHash.put(listFaqTitle.get(6), contactUs);

    }
}
