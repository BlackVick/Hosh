package com.blackviking.hosh.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.blackviking.hosh.Notification.APIService;
import com.blackviking.hosh.Notification.RetrofitClient;

public class Common {

    /*---   ACCOUNT INFO   ---*/
    public static final String USER_ID = "User";
    public static final String SIGN_UP_CHOICE = "Choice";
    public static final String GOOD_TO_GO = "GoToGo";


    /*---   CONTEXT MENU   ---*/
    public static final String DELETE_BOTH = "Retract Message";
    public static final String DELETE_SINGLE = "Delete Message";


    /*---   DEVICE ONLINE OFFLINE   ---*/
    public static final String APP_STATE = "State";


    /*---   NOTIFICATION   ---*/
    public static final String NOTIFICATION_STATE = "Notification";
    public static final String ACCOUNT_NOTIFICATION = "Account";
    public static String FEED_NOTIFICATION_TOPIC = "Feed";
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    public static APIService getFCMService()    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }


    /*---   CHECK FOR INTERNET   ---*/
    public static boolean isConnectedToInternet(Context context)    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null)
            {
                for (int i = 0; i<info.length; i++)
                {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

}
