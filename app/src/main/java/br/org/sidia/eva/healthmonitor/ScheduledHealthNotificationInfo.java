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

import android.support.annotation.NonNull;

import java.util.Locale;

public class ScheduledHealthNotificationInfo {

    private HealthState state;
    private long now;

    public ScheduledHealthNotificationInfo(HealthState notification) {
        this.state = notification;
        this.now = System.currentTimeMillis();
    }

    public long getRemainingTimeUntilNextNotification() {
        int nextStatus = state.getStatus();
        long scheduledAt = state.getUpdatedAt();
        HealthStateConfiguration option = HealthStateConfiguration.getById(state.getId());
        long delay = nextStatus == Notifications.HEALTH_STATUS_NORMAL ? option.getDelayUntilWarning() : option.getDelayUntilCritical();
        long remaining = (scheduledAt + delay) - now;
        return remaining >= 0 ? remaining : 0;
    }

    private String getStatusString(@Notifications.HealthStatus int status) {
        if (status == Notifications.HEALTH_STATUS_NORMAL) {
            return "NORMAL";
        } else if (status == Notifications.HEALTH_STATUS_WARNING) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    public String getStatusString() {
        return getStatusString(state.getStatus());
    }

    public String getNextStatusString() {
        if (state.getStatus() == Notifications.HEALTH_STATUS_NORMAL) {
            return getStatusString(Notifications.HEALTH_STATUS_WARNING);
        } else if (state.getStatus() == Notifications.HEALTH_STATUS_WARNING) {
            return getStatusString(Notifications.HEALTH_STATUS_CRITICAL);
        } else {
            return getStatusString(Notifications.HEALTH_STATUS_CRITICAL);
        }
    }

    @Notifications.HealthId
    public int getId() {
        return state.getId();
    }

    @Notifications.HealthStatus
    public int getStatus() {
        return state.getStatus();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "Next state (%s) in %d seconds",
                getNextStatusString(), getRemainingTimeUntilNextNotification() / 1000);
    }
}
