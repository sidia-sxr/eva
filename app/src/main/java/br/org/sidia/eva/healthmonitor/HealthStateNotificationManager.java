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

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public final class HealthStateNotificationManager extends ContextWrapper {

    private static final String TAG = "HealthStateNotificationManager";

    private ComponentName mServiceComponent;
    private JobScheduler mJobScheduler;
    private SharedPreferenceHelper mSharedPreferenceHelper;
    private HealthStateNotificationHelper mHealthStateNotificationHelper;
    private boolean mShowNotifications = true;

    private static volatile HealthStateNotificationManager sInstance;

    public static HealthStateNotificationManager getInstance(Context context) {
        synchronized (HealthStateNotificationManager.class) {
            if (sInstance == null) {
                synchronized (HealthStateNotificationManager.class) {
                    sInstance = new HealthStateNotificationManager(context);
                }
            }
        }
        return sInstance;
    }

    private HealthStateNotificationManager(Context context) {
        super(context);
        mServiceComponent = new ComponentName(context, NotificationService.class);
        mJobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mSharedPreferenceHelper = new SharedPreferenceHelper(context);
        mHealthStateNotificationHelper = new HealthStateNotificationHelper(context);
        initSavedHealthStates();
    }

    private void initSavedHealthStates() {
        if (mSharedPreferenceHelper.getHealthState(Notifications.HEALTH_ID_DRINK) == null) {
            mJobScheduler.cancelAll();
            initSavedHealthState(Notifications.HEALTH_ID_DRINK);
            initSavedHealthState(Notifications.HEALTH_ID_SLEEP);
            initSavedHealthState(Notifications.HEALTH_ID_PEE);
            initSavedHealthState(Notifications.HEALTH_ID_PLAY);
        }
    }

    private void initSavedHealthState(@Notifications.HealthId int id) {
        mSharedPreferenceHelper.setHealthState(new HealthState(id));
    }

    public synchronized void scheduleNotifications() {
        scheduleNotification(Notifications.HEALTH_ID_DRINK);
        scheduleNotification(Notifications.HEALTH_ID_SLEEP);
        scheduleNotification(Notifications.HEALTH_ID_PEE);
        scheduleNotification(Notifications.HEALTH_ID_PLAY);
    }

    private void scheduleNotification(@Notifications.HealthId int id) {
        if (!isScheduled(id) && savedStatusIs(id, Notifications.HEALTH_STATUS_NORMAL)) {
            if (scheduleJob(id, getDelayUntilWarning(id))) {
                EventBus.getDefault().post(
                        new HealthStateNotificationEvent(id, Notifications.HEALTH_STATUS_NORMAL));
            }
        }
    }

    public void rescheduleNotification(@Notifications.HealthId int id) {
        cancelScheduled(id);
        scheduleNotification(id);
    }

    public void cancelScheduled(@Notifications.HealthId int id) {
        mJobScheduler.cancel(id);
        mHealthStateNotificationHelper.clearNotification(id);
        initSavedHealthState(id);
    }

    private boolean savedStatusIs(@Notifications.HealthId int id, @Notifications.HealthStatus int status) {
        return mSharedPreferenceHelper.getHealthState(id).getStatus() == status;
    }

    public void setShowNotifications(boolean showNotifications) {
        this.mShowNotifications = showNotifications;
    }

    public void clearNotifications() {
        mHealthStateNotificationHelper.clearAll();
    }

    private boolean isScheduled(@Notifications.HealthId int id) {
        return mJobScheduler.getPendingJob(id) != null;
    }

    public synchronized void onHealthStateNotified(@Notifications.HealthId int id) {

        HealthState notification = mSharedPreferenceHelper.getHealthState(id);

        if (notification.getStatus() == Notifications.HEALTH_STATUS_NORMAL) {

            if (scheduleJob(id, getDelayUntilCritical(id))) {
                showNotification(id, Notifications.HEALTH_STATUS_WARNING);
                notification.setStatus(Notifications.HEALTH_STATUS_WARNING);
                mSharedPreferenceHelper.setHealthState(notification);
                EventBus.getDefault().post(
                        new HealthStateNotificationEvent(id, Notifications.HEALTH_STATUS_WARNING));
            }

        } else if (notification.getStatus() == Notifications.HEALTH_STATUS_WARNING) {

            showNotification(id, Notifications.HEALTH_STATUS_CRITICAL);
            notification.setStatus(Notifications.HEALTH_STATUS_CRITICAL);
            mSharedPreferenceHelper.setHealthState(notification);
            EventBus.getDefault().post(
                    new HealthStateNotificationEvent(id, Notifications.HEALTH_STATUS_CRITICAL));
        }
    }

    private long getDelayUntilWarning(@Notifications.HealthId int id) {
        return HealthStateConfiguration.getById(id).getDelayUntilWarning();
    }

    private long getDelayUntilCritical(@Notifications.HealthId int id) {
        return HealthStateConfiguration.getById(id).getDelayUntilCritical();
    }

    public synchronized ScheduledHealthNotificationInfo getHealthNotificationInfo(@Notifications.HealthId int id) {
        return new ScheduledHealthNotificationInfo(mSharedPreferenceHelper.getHealthState(id));
    }

    private boolean scheduleJob(int jobId, long delay) {

        JobInfo.Builder builder = new JobInfo.Builder(jobId, mServiceComponent);
        builder.setMinimumLatency(delay);
        builder.setPersisted(true);

        JobInfo jobInfo = builder.build();

        if (mJobScheduler.schedule(jobInfo) == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled: " + jobInfo.getId());
            return true;
        } else {
            Log.d(TAG, "Failure scheduling job " + jobId);
            return false;
        }
    }

    private void showNotification(@Notifications.HealthId int id, @Notifications.HealthStatus int status) {
        if (mShowNotifications) {
            mHealthStateNotificationHelper.notify(id, mHealthStateNotificationHelper.getHealthNotification(id, status));
        }
    }
}
