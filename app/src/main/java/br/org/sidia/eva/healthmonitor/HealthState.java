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

public class HealthState {

    @Notifications.HealthId
    private int id;

    @Notifications.HealthStatus
    private int status = Notifications.HEALTH_STATUS_NORMAL;

    private long updatedAt;

    public HealthState(@Notifications.HealthId int id) {
        this.id = id;
    }

    @Notifications.HealthId
    public int getId() {
        return id;
    }

    @Notifications.HealthStatus
    public int getStatus() {
        return status;
    }

    public void setStatus(@Notifications.HealthStatus int status) {
        this.status = status;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @NonNull
    @Override
    public String toString() {
        return "HealthState{" +
                "id=" + id +
                ", status=" + status +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
