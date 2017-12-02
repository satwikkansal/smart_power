package com.flash.brainbreaker.flash;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v4.app.NotificationCompat;

/**
 * Created by brainbreaker on 02/12/17.
 */

public class PluginControlReceiver extends BroadcastReceiver {
    public void onReceive(Context context , Intent intent) {
        String action = intent.getAction();
        Camera cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);

        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            cam.startPreview();
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_battery_charging)
                            .setContentTitle("Power ON")
                            .setContentText("You're on the grid!");

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_battery_alert)
                            .setContentTitle("Power OFF")
                            .setContentText("Pay the bills on time!");

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());

            cam.stopPreview();
            cam.release();
        }
    }
}