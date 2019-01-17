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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Notifications {

    int HEALTH_ID_DRINK = 1000;
    int HEALTH_ID_SLEEP = 1001;
    int HEALTH_ID_PEE = 1002;
    int HEALTH_ID_PLAY = 1003;

    int HEALTH_STATUS_NORMAL = 2000;
    int HEALTH_STATUS_WARNING = 2001;
    int HEALTH_STATUS_CRITICAL = 2002;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HEALTH_STATUS_NORMAL, HEALTH_STATUS_WARNING, HEALTH_STATUS_CRITICAL})
    @interface HealthStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HEALTH_ID_DRINK, HEALTH_ID_SLEEP, HEALTH_ID_PEE, HEALTH_ID_PLAY})
    @interface HealthId {
    }
}
