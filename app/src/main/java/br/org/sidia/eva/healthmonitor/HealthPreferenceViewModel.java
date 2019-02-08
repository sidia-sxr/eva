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

import android.support.annotation.DrawableRes;

public class HealthPreferenceViewModel {

    @HealthId
    int id;
    @DrawableRes
    int resourceId;
    float level;
    long remainingTime;
    float warningLevel;
    float criticalLevel;
    @HealthStatus
    int status;

    long duration;
    long criticalRepeatDelay;
    long recoveryDurationWhenWarning;
    long recoveryDurationWhenCritical;

    public HealthPreferenceViewModel(@HealthId int id, float level, long remainingTime,
                                     @HealthStatus int status,
                                     @DrawableRes int resourceId, long duration,
                                     float warningLevel, float criticalLevel,
                                     long criticalRepeatDelay, long recoveryDurationWhenWarning,
                                     long recoveryDurationWhenCritical) {
        this.id = id;
        this.resourceId = resourceId;
        this.level = level;
        this.remainingTime = remainingTime;
        this.duration = duration;
        this.warningLevel = warningLevel;
        this.criticalLevel = criticalLevel;
        this.criticalRepeatDelay = criticalRepeatDelay;
        this.status = status;
        this.recoveryDurationWhenWarning = recoveryDurationWhenWarning;
        this.recoveryDurationWhenCritical = recoveryDurationWhenCritical;
    }

    public int getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public long getCriticalRepeatDelay() {
        return criticalRepeatDelay;
    }

    public long getRecoveryDurationWhenWarning() {
        return recoveryDurationWhenWarning;
    }

    public long getRecoveryDurationWhenCritical() {
        return recoveryDurationWhenCritical;
    }
}
