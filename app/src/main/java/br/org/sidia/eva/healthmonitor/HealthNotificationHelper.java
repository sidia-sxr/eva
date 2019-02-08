/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.org.sidia.eva.healthmonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Calendar;

import br.org.sidia.eva.EvaActivity;
import br.org.sidia.eva.R;

public class HealthNotificationHelper extends ContextWrapper {

    private static final String TAG = "HealthNotificationHelper";

    private static final String HEALTH_STATE_CHANNEL_ID_DAYTIME = "HEALTH_STATE_CHANNEL_ID_DAYTIME";
    private static final String HEALTH_STATE_CHANNEL_ID_NIGHTLY = "HEALTH_STATE_CHANNEL_ID_NIGHTLY";

    private static final String HEALTH_STATE_GROUP_KEY = "HEALTH_STATE_GROUP_KEY";

    public HealthNotificationHelper(Context context) {
        super(context);
        createNotificationChannel();
    }

    public Notification.Builder getHealthNotification(
            @DrawableRes int resourceId,
            @StringRes int title, @StringRes int text) {

        return new Notification.Builder(getApplicationContext(), HEALTH_STATE_CHANNEL_ID_DAYTIME)
                .setSmallIcon(getSmallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), resourceId))
                .setContentTitle(getString(title))
                .setContentText(getString(text))
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setContentIntent(getNotificationContentIntent())
                .setGroup(HEALTH_STATE_GROUP_KEY)
                .setAutoCancel(true);
    }

    public void notify(int id, Notification.Builder notification) {
        boolean isDayTime = isDayTime();
        Log.d(TAG, "notify: isDayTime = " + isDayTime);
        if (!isDayTime) {
            notification.setChannelId(HEALTH_STATE_CHANNEL_ID_NIGHTLY);
        }
        NotificationManagerCompat.from(this).notify(id, notification.build());
    }

    private int getSmallIcon() {
        return R.drawable.ic_notification_small;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel daytimeChannel = new NotificationChannel(HEALTH_STATE_CHANNEL_ID_DAYTIME,
                    "Eva Health Status (daytime)", NotificationManager.IMPORTANCE_HIGH);
            daytimeChannel.setLightColor(Color.BLUE);
            daytimeChannel.enableLights(true);
            daytimeChannel.setShowBadge(true);
            daytimeChannel.enableVibration(true);
            daytimeChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            setSound(daytimeChannel);

            NotificationChannel nightlyChannel = new NotificationChannel(HEALTH_STATE_CHANNEL_ID_NIGHTLY,
                    "Eva Health Status (nightly)", NotificationManager.IMPORTANCE_HIGH);
            daytimeChannel.setLightColor(Color.BLUE);
            nightlyChannel.enableLights(false);
            nightlyChannel.setShowBadge(true);
            nightlyChannel.enableVibration(false);
            nightlyChannel.setSound(null, null);
            nightlyChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(daytimeChannel);
                manager.createNotificationChannel(nightlyChannel);
            }
        }
    }

    private boolean isDayTime() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 6);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 18);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        Calendar now = Calendar.getInstance();
        return now.getTimeInMillis() >= start.getTimeInMillis()
                && now.getTimeInMillis() < end.getTimeInMillis();
    }

    private void setSound(NotificationChannel channel) {
//            Uri soundUri = Uri.parse(
//                    "android.resource://"
//                            + getPackageName()
//                            + "/"
//                            + R.raw.beep);
//            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
//                    .build();
//            channel.setSound(soundUri, audioAttributes);
    }

    private PendingIntent getNotificationContentIntent() {
        Intent intent = new Intent(this, EvaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
