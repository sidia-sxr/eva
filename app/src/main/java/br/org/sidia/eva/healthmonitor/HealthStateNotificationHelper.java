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
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import br.org.sidia.eva.BuildConfig;
import br.org.sidia.eva.EvaActivity;
import br.org.sidia.eva.R;

public class HealthStateNotificationHelper extends ContextWrapper {

    public static final String HEALTH_STATE_CHANNEL_ID = "HEALTH_STATE_CHANNEL_ID";
    private static final int APP_NOTIFICATIONS_GROUP_ID = 9999;
    private static final String HEALTH_STATE_GROUP_KEY = BuildConfig.APPLICATION_ID + ".HEALTH_STATUS";

    private final NotificationCompat.Builder mHealthGroupBuilder;

    public HealthStateNotificationHelper(Context context) {
        super(context);
        mHealthGroupBuilder = new NotificationCompat.Builder(getApplicationContext(), HEALTH_STATE_CHANNEL_ID)
                .setGroup(HEALTH_STATE_GROUP_KEY)
                .setGroupSummary(true)
                .setSmallIcon(getSmallIcon());
        createNotificationChannel();
    }

    public NotificationCompat.Builder getHealthNotification(@Notifications.HealthId int id, @Notifications.HealthStatus int status) {

        return new NotificationCompat.Builder(getApplicationContext(), HEALTH_STATE_CHANNEL_ID)
                .setGroup(HEALTH_STATE_GROUP_KEY)
                .setSmallIcon(getSmallIcon())
                .setContentTitle("Eva needs your attention")
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(getRemoteViews(id, status))
                .setContentIntent(getNotificationContentIntent())
                .setAutoCancel(true);
    }

    public void notify(int id, NotificationCompat.Builder notification) {
        NotificationManagerCompat.from(this).notify(APP_NOTIFICATIONS_GROUP_ID, mHealthGroupBuilder.build());
        NotificationManagerCompat.from(this).notify(id, notification.build());
    }

    public void clearAll() {
        new Handler().postDelayed(() ->
                NotificationManagerCompat.from(this).cancelAll(), 300);
    }

    public void clearNotification(@Notifications.HealthId int id) {
        NotificationManagerCompat.from(this).cancel(id);
    }

    private int getSmallIcon() {
        return R.mipmap.ic_launcher_foreground;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(HEALTH_STATE_CHANNEL_ID,
                    "Eva Health Status", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private RemoteViews getRemoteViews(@Notifications.HealthId int id, @Notifications.HealthStatus int status) {

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_health_notification);
        HealthStateConfiguration configuration = HealthStateConfiguration.getById(id);

        String title = null, text = null;
        @DrawableRes int bgStatus = 0;

        if (status == Notifications.HEALTH_STATUS_WARNING) {
            title = getString(configuration.getTitleWhenWarning());
            text = getString(configuration.getTextWhenWarning());
            bgStatus = R.drawable.bg_health_status_warning;
        } else if (status == Notifications.HEALTH_STATUS_CRITICAL) {
            title = getString(configuration.getTitleWhenCritical());
            text = getString(configuration.getTextWhenCritical());
            bgStatus = R.drawable.bg_health_status_critical;
        }

        remoteViews.setTextViewText(R.id.text_title, title);
        remoteViews.setTextViewText(R.id.text_text, text);
        remoteViews.setImageViewResource(R.id.image_type, configuration.getResourceId());
        remoteViews.setImageViewResource(R.id.image_background, bgStatus);

        return remoteViews;
    }

    private PendingIntent getNotificationContentIntent() {
        Intent intent = new Intent(this, EvaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

    }
}
