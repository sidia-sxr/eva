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
import android.support.annotation.Nullable;

public class HealthNotificationConfig implements Comparable<HealthNotificationConfig> {

    private int status;
    private HealthLevelNotificationConfig levelNotificationConfig;
    private HealthRecoveryNotificationConfig recoveryNotificationConfig;

    HealthNotificationConfig(
            @HealthStatus int status,
            @NonNull HealthLevelNotificationConfig levelNotificationConfig) {
        this(status, levelNotificationConfig, null);
    }

    HealthNotificationConfig(
            @HealthStatus int status,
            @NonNull HealthLevelNotificationConfig levelNotificationConfig,
            @Nullable HealthRecoveryNotificationConfig recoveryNotificationConfig) {

        this.status = status;
        this.levelNotificationConfig = levelNotificationConfig;
        this.recoveryNotificationConfig = recoveryNotificationConfig;
    }

    @HealthStatus
    public int getStatus() {
        return status;
    }

    public HealthLevelNotificationConfig getLevelNotificationConfig() {
        return levelNotificationConfig;
    }

    @Nullable
    public HealthRecoveryNotificationConfig getRecoveryNotificationConfig() {
        return recoveryNotificationConfig;
    }

    @Override
    public int compareTo(@NonNull HealthNotificationConfig o) {
        return levelNotificationConfig.compareTo(o.levelNotificationConfig);
    }

    @NonNull
    @Override
    public String toString() {
        return "HealthNotificationConfig{" +
                "status=" + status +
                ", levelNotificationConfig=" + levelNotificationConfig +
                ", recoveryNotificationConfig=" + recoveryNotificationConfig +
                '}';
    }
}
