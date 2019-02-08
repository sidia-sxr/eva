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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_CRITICAL;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_NORMAL;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_RECOVERING;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_WARNING;

@IntDef({HEALTH_STATUS_NORMAL, HEALTH_STATUS_WARNING, HEALTH_STATUS_CRITICAL, HEALTH_STATUS_RECOVERING})
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface HealthStatus {
}
