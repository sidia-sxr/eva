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

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class NotificationService extends JobService {

    private static final String TAG = "NotificationService";

    private HealthManager mHealthManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mHealthManager =
                HealthManager.getInstance(getApplicationContext());
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Does not show notifications if activity is active
        //mHealthManager.setShowNotifications(false);
        //mHealthManager.clearNotifications();
        mHealthManager.startNotifications();
        Log.d(TAG, "Service started");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        mHealthManager.handleNotification(params.getJobId());
        Log.d(TAG, "Job finished: " + params.getJobId());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: " + params.getJobId());
        return false;
    }
}
