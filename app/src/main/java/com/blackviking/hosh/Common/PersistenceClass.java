package com.blackviking.hosh.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.blackviking.hosh.R;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Scarecrow on 6/18/2018.
 */

public class PersistenceClass extends MultiDexApplication {

    public static final String CHANNEL_1_ID = "messagingChannel";
    public static final String CHANNEL_2_ID = "feedChannel";
    public static final String CHANNEL_3_ID = "accountChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /*-------PICASSO--------*/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        createNotificationChannels();

    }

    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            /*---   OREO CUSTOM SOUND   ---*/
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            Uri sound = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.hosh_notification);



            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Messaging",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel1.setDescription("Messaging Channel");
            channel1.setSound(sound, attributes);
            channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Hosh Feed",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel2.setDescription("Feed Channel");
            channel2.setSound(sound, attributes);

            NotificationChannel channel3 = new NotificationChannel(
                    CHANNEL_3_ID,
                    "Account",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel3.setDescription("Account Channel");
            channel3.setSound(sound, attributes);


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            manager.createNotificationChannel(channel3);
        }

    }
}
