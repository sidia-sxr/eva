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
import android.support.annotation.StringRes;

public final class HealthLevelNotificationConfig implements Comparable<HealthLevelNotificationConfig> {

    private float mTargetLevel;
    private int mTitle;
    private int mText;
    private boolean mShowNotification;

    HealthLevelNotificationConfig(
            @FloatRange(from = 0, to = 1) float targetLevel,
            boolean showNotification) {
        this(targetLevel, -1, -1, showNotification);
    }

    HealthLevelNotificationConfig(
            @FloatRange(from = 0, to = 1) float targetLevel,
            @StringRes int title,
            @StringRes int text,
            boolean showNotification) {

        this.mTargetLevel = targetLevel;
        this.mTitle = title;
        this.mText = text;
        this.mShowNotification = showNotification;
    }

    @FloatRange(from = 0, to = 1)
    public float getTargetLevel() {
        return mTargetLevel;
    }

    @StringRes
    int getTitle() {
        return mTitle;
    }

    @StringRes
    int getText() {
        return mText;
    }

    public boolean isShowNotification() {
        return mShowNotification;
    }

    @Override
    public int compareTo(HealthLevelNotificationConfig o) {
        return (int) (o.mTargetLevel - mTargetLevel);
    }

    @NonNull
    @Override
    public String toString() {
        return "HealthLevelNotificationConfig{" +
                "mTargetLevel=" + mTargetLevel +
                ", mTitle=" + mTitle +
                ", mText=" + mText +
                ", mShowNotification=" + mShowNotification +
                '}';
    }
}
