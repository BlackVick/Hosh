package com.blackviking.hosh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.blackviking.hosh.Fragments.Account;
import com.blackviking.hosh.Fragments.Feed;
import com.blackviking.hosh.Fragments.HookUp;
import com.blackviking.hosh.Fragments.Hopdate;
import com.blackviking.hosh.Fragments.Messaging;

import java.lang.reflect.Field;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity {

    private ImageView feed, hookup, hopdate, messaging, account;

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

        setContentView(R.layout.activity_home);


        /*---   FRAGMENTS   ---*/
        final Feed feedFragment = new Feed();
        final HookUp hookupFragment = new HookUp();
        final Hopdate hopdateFragment = new Hopdate();
        final Messaging messagingFragment = new Messaging();
        final Account accountFragment = new Account();


        /*---   WIDGETS   ---*/
        feed = (ImageView)findViewById(R.id.feed);
        hookup = (ImageView)findViewById(R.id.hookup);
        hopdate = (ImageView)findViewById(R.id.hopdate);
        messaging = (ImageView)findViewById(R.id.messaging);
        account = (ImageView)findViewById(R.id.account);



        /*---   BOTTOM NAVIGATION   ---*/
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.color.bottom_nav_clicked);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(feedFragment);
            }
        });

        hookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.color.bottom_nav_clicked);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(hookupFragment);
            }
        });

        hopdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.color.bottom_nav_clicked);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(hopdateFragment);
            }
        });

        messaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.color.bottom_nav_clicked);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                setFragment(messagingFragment);
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                hookup.setBackgroundResource(R.drawable.white_grey_border_top);
                hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
                messaging.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.color.bottom_nav_clicked);

                setFragment(accountFragment);
            }
        });


        /*---   SET BASE FRAGMENT   ---*/
        setBaseFragment(feedFragment);

    }

    private void setBaseFragment(Fragment feedFragment) {

        setFragment(feedFragment);

        feed.setBackgroundResource(R.color.bottom_nav_clicked);
        hookup.setBackgroundResource(R.drawable.white_grey_border_top);
        hopdate.setBackgroundResource(R.drawable.white_grey_border_top);
        messaging.setBackgroundResource(R.drawable.white_grey_border_top);
        account.setBackgroundResource(R.drawable.white_grey_border_top);

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }
}
