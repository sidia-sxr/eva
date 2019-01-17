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
import android.support.annotation.StringRes;

import java.util.concurrent.TimeUnit;

import br.org.sidia.eva.R;

public enum HealthStateConfiguration {

    DRINK(Notifications.HEALTH_ID_DRINK, R.drawable.ic_bowl, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10), R.string.healthnotification_drink_title_when_warning, R.string.healthnotification_drink_text_when_warning, R.string.healthnotification_drink_title_when_critical, R.string.healthnotification_drink_text_when_critical),
    SLEEP(Notifications.HEALTH_ID_SLEEP, R.drawable.ic_bed, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10), R.string.healthnotification_sleep_title_when_warning, R.string.healthnotification_sleep_text_when_warning, R.string.healthnotification_sleep_title_when_critical, R.string.healthnotification_sleep_text_when_critical),
    PEE(Notifications.HEALTH_ID_PEE, R.drawable.ic_hydrant, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10), R.string.healthnotification_pee_title_when_warning, R.string.healthnotification_pee_text_when_warning, R.string.healthnotification_pee_title_when_critical, R.string.healthnotification_pee_text_when_critical),
    PLAY(Notifications.HEALTH_ID_PLAY, R.drawable.ic_play_bone, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10), R.string.healthnotification_play_title_when_warning, R.string.healthnotification_play_text_when_warning, R.string.healthnotification_play_title_when_critical, R.string.healthnotification_play_text_when_critical);

    private int id;

    private int resourceId;

    private long delayUntilWarning;
    private long delayUntilCritical;

    private int titleWhenWarning;
    private int textWhenWarning;
    private int titleWhenCritical;
    private int textWhenCritical;

    HealthStateConfiguration(@Notifications.HealthId int id, @DrawableRes int resourceId,
                             long delayUntilWarning, long delayUntilCritical, int titleWhenWarning,
                             int textWhenWarning, int titleWhenCritical, int textWhenCritical) {
        this.id = id;
        this.resourceId = resourceId;
        this.delayUntilWarning = delayUntilWarning;
        this.delayUntilCritical = delayUntilCritical;
        this.titleWhenWarning = titleWhenWarning;
        this.textWhenWarning = textWhenWarning;
        this.titleWhenCritical = titleWhenCritical;
        this.textWhenCritical = textWhenCritical;
    }

    @Notifications.HealthId
    public int getId() {
        return id;
    }

    @DrawableRes
    public int getResourceId() {
        return resourceId;
    }

    @StringRes
    public long getDelayUntilWarning() {
        return delayUntilWarning;
    }

    @StringRes
    public long getDelayUntilCritical() {
        return delayUntilCritical;
    }

    @StringRes
    public int getTitleWhenWarning() {
        return titleWhenWarning;
    }

    @StringRes
    public int getTextWhenWarning() {
        return textWhenWarning;
    }

    @StringRes
    public int getTitleWhenCritical() {
        return titleWhenCritical;
    }

    @StringRes
    public int getTextWhenCritical() {
        return textWhenCritical;
    }

    public static HealthStateConfiguration getById(@Notifications.HealthId int id) {
        return values()[id - Notifications.HEALTH_ID_DRINK];
    }
}
