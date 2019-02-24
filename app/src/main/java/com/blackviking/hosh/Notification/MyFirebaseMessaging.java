package com.blackviking.hosh.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.FeedDetails;
import com.blackviking.hosh.Messaging;
import com.blackviking.hosh.OtherUserProfile;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.blackviking.hosh.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.paperdb.Paper;

import static com.blackviking.hosh.Common.PersistenceClass.CHANNEL_1_ID;
import static com.blackviking.hosh.Common.PersistenceClass.CHANNEL_2_ID;
import static com.blackviking.hosh.Common.PersistenceClass.CHANNEL_3_ID;

/**
 * Created by Scarecrow on 2/21/2019.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Paper.init(this);
        String appState = Paper.book().read(Common.APP_STATE);

        if (remoteMessage.getData() != null && appState.equalsIgnoreCase("Background")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                sendNotificationAPI26(remoteMessage);

            } else {

                sendNotification(remoteMessage);

            }

        } else if (remoteMessage.getData() != null && appState.equalsIgnoreCase("Foreground")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                sendNotificationAPI26Internally(remoteMessage);

            } else {

                sendNotificationInternally(remoteMessage);

            }

        }
    }

    private void sendNotificationInternally(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String otherUserId = data.get("user_id");
        String otherUserName = data.get("user_name");
        String otherUserImage = data.get("user_image");
        String myId = data.get("my_id");
        String myName = data.get("my_name");
        String feedId = data.get("feed_id");


        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Hosh Feed")) {

            Intent feedBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            feedBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(feedBroadcastIntent);

        } else if (title.equalsIgnoreCase("New Message")) {

            String theMessage = "@"+otherUserName+" Just Sent You A Message";

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", theMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Account")) {

            Intent accountBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            accountBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(accountBroadcastIntent);

        }

    }

    private void sendNotificationAPI26Internally(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String myId = data.get("my_id");
        String myName = data.get("my_name");
        String otherUserId = data.get("user_id");
        String otherUserName = data.get("user_name");
        String otherUserImage = data.get("user_image");
        String feedId = data.get("feed_id");

        String accountNoti = Paper.book().read(Common.ACCOUNT_NOTIFICATION);


        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Hosh Feed")) {

            Intent feedBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            feedBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(feedBroadcastIntent);

        } else if (title.equalsIgnoreCase("New Message")) {

            String theMessage = "@"+otherUserName+" Just Sent You A Message";

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", theMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Account")) {

            Intent accountBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            accountBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(accountBroadcastIntent);

        }

    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String myId = data.get("my_id");
        String myName = data.get("my_name");
        String otherUserId = data.get("user_id");
        String otherUserName = data.get("user_name");
        String otherUserImage = data.get("user_image");
        String feedId = data.get("feed_id");

        String accountNoti = Paper.book().read(Common.ACCOUNT_NOTIFICATION);

        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Hosh Feed")) {

            Intent feedIntent = new Intent(this, FeedDetails.class);
            feedIntent.putExtra("CurrentFeedId", feedId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, feedIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.hosh_logo_faded_white, "View Feed", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(3, notification);

        } else if (title.equalsIgnoreCase("New Message")) {

            Intent messageIntent = new Intent(this, Messaging.class);
            messageIntent.putExtra("UserId", otherUserId);
            messageIntent.putExtra("UserName", otherUserName);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, messageIntent, 0);

            if (!otherUserImage.equalsIgnoreCase("")) {

                try {
                    Bitmap bmp = Picasso.with(getApplicationContext()).load(otherUserImage).get();

                    Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                            .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setLargeIcon(bmp)
                            .setSubText(myName)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(message)
                                    .setBigContentTitle("Message Details")
                                    .setSummaryText("Messaging"))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setColor(getResources().getColor(R.color.colorPrimaryDark))
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .addAction(R.drawable.hosh_logo_faded_white, "Open Message", contentIntent)
                            .build();

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NotificationID.getID(), notification);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSubText(myName)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message)
                                .setBigContentTitle("Message Details")
                                .setSummaryText("Messaging"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .addAction(R.drawable.hosh_logo_faded_white, "Open Message", contentIntent)
                        .build();

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationID.getID(), notification);

            }


        } else if (title.equalsIgnoreCase("Account")) {

            Intent accountIntent = new Intent(this, OtherUserProfile.class);
            accountIntent.putExtra("UserId", otherUserId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, accountIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_3_ID)
                    .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_EMAIL)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.hosh_logo_faded_white, "View Profile", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notification);

        }

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String otherUserId = data.get("user_id");
        String otherUserName = data.get("user_name");
        String otherUserImage = data.get("user_image");
        String myId = data.get("my_id");
        String myName = data.get("my_name");
        String feedId = data.get("feed_id");

        String accountNoti = Paper.book().read(Common.ACCOUNT_NOTIFICATION);

        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Hosh Feed")) {

            Intent feedIntent = new Intent(this, FeedDetails.class);
            feedIntent.putExtra("CurrentFeedId", feedId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, feedIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.hosh_logo_faded_white, "Check Feed", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.hosh_notification))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(3, notification);

        } else if (title.equalsIgnoreCase("New Message")) {


            Intent messageIntent = new Intent(this, Messaging.class);
            messageIntent.putExtra("UserId", otherUserId);
            messageIntent.putExtra("UserName", otherUserName);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, messageIntent, 0);

            if (!otherUserImage.equalsIgnoreCase("")) {

                try {
                    Bitmap bmp = Picasso.with(getApplicationContext()).load(otherUserImage).get();

                    Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                            .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setLargeIcon(bmp)
                            .setSubText(myName)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setColor(getResources().getColor(R.color.colorPrimaryDark))
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .addAction(R.drawable.hosh_logo_faded_white, "Open Message", contentIntent)
                            .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.hosh_notification))
                            .build();

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NotificationID.getID(), notification);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSubText(myName)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .addAction(R.drawable.hosh_logo_faded_white, "Open Message", contentIntent)
                        .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.hosh_notification))
                        .build();

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationID.getID(), notification);

            }


        } else if (title.equalsIgnoreCase("Account")) {

            Intent accountIntent = new Intent(this, OtherUserProfile.class);
            accountIntent.putExtra("UserId", otherUserId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, accountIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_3_ID)
                    .setSmallIcon(R.drawable.ic_stat_hosh_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_EMAIL)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.hosh_logo_faded_white, "View Profile", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.hosh_notification))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notification);
        }

    }

    public static class NotificationID {
        private static final AtomicInteger c = new AtomicInteger(0);

        public static int getID() {
            return c.incrementAndGet();
        }
    }
}
