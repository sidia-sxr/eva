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

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

final class HealthLiveSettings {

    private int mId;
    private long mScheduledAt; // used to calculate ic_health_preferences level when levelAutoUpdate is set true (default)
    private long mRecoveryStartedAt; // used to calculate recovery progress when recovery is configured
    private float mLevel = 1; // holds level set by user when the configuration levelAutoUpdate is set false

    HealthLiveSettings(@HealthId int id) {
        this.mId = id;
    }

    @HealthId
    public int getId() {
        return mId;
    }

    long getScheduledAt() {
        return mScheduledAt;
    }

    void setScheduledAt(long scheduledAt) {
        this.mScheduledAt = scheduledAt;
    }

    long getRecoveryStartedAt() {
        return mRecoveryStartedAt;
    }

    void setRecoveryStartedAt(long recoveryStartedAt) {
        this.mRecoveryStartedAt = recoveryStartedAt;
    }

    @FloatRange(from = 0, to = 1)
    float getLevel() {
        return mLevel;
    }

    void setLevel(@FloatRange(from = 0, to = 1) float mLevel) {
        this.mLevel = mLevel;
    }

    @NonNull

    @Override
    public String toString() {
        return "HealthLiveSettings{" +
                "mId=" + mId +
                ", mScheduledAt=" + mScheduledAt +
                ", mRecoveryStartedAt=" + mRecoveryStartedAt +
                ", mLevel=" + mLevel +
                '}';
    }
}
