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
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import br.org.sidia.eva.R;

import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_DRINK;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_PEE;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_PLAY;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_SLEEP;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_CRITICAL;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_WARNING;

public class HealthConfiguration {

    private static final SparseArray<HealthConfiguration> DEFAULT_CONFIGURATIONS_MAP = new SparseArray<>();
    private static final List<HealthConfiguration> DEFAULT_CONFIGURATIONS = new ArrayList<>();

    private static SparseArray<HealthConfiguration> CUR_CONFIGURATIONS_MAP;
    private static List<HealthConfiguration> CUR_CONFIGURATIONS;

    static {

        HealthConfiguration configuration;

        configuration = new HealthConfiguration.Builder(HEALTH_ID_PEE, R.drawable.ic_hydrant)
                .setPriority(0)
                .setLevelAutoUpdateEnabled(false)
                .setLastLevelNotificationRepeatDelay(hours(6))
                .setHealthNotificationConfigs(
                        new HealthNotificationConfig(HEALTH_STATUS_WARNING,
                                new HealthLevelNotificationConfig(
                                        .5f,
                                        false)),
                        new HealthNotificationConfig(HEALTH_STATUS_CRITICAL,
                                new HealthLevelNotificationConfig(
                                        .0f,
                                        R.string.healthnotification_pee_title_when_critical,
                                        R.string.healthnotification_pee_text_when_critical,
                                        true)))
                .build();

        DEFAULT_CONFIGURATIONS.add(configuration);
        DEFAULT_CONFIGURATIONS_MAP.put(HEALTH_ID_PEE, configuration);

        configuration = new HealthConfiguration.Builder(HEALTH_ID_DRINK, R.drawable.ic_bowl, hours(4))
                .setPriority(1)
                .setLastLevelNotificationRepeatDelay(hours(6))
                .setHealthNotificationConfigs(
                        new HealthNotificationConfig(HEALTH_STATUS_WARNING,
                                new HealthLevelNotificationConfig(
                                        .5f,
                                        false)),
                        new HealthNotificationConfig(HEALTH_STATUS_CRITICAL,
                                new HealthLevelNotificationConfig(
                                        0f,
                                        R.string.healthnotification_drink_title_when_critical,
                                        R.string.healthnotification_drink_text_when_critical,
                                        true)))
                .build();

        DEFAULT_CONFIGURATIONS.add(configuration);
        DEFAULT_CONFIGURATIONS_MAP.put(HEALTH_ID_DRINK, configuration);

        configuration = new HealthConfiguration.Builder(HEALTH_ID_SLEEP, R.drawable.ic_bed, hours(12))
                .setPriority(2)
                .setRecoveryNotificationTitle(R.string.healthnotification_wakeup_title_when_critical)
                .setRecoveryNotificationText(R.string.healthnotification_wakeup_text_when_critical)
                .setShowRecoveryNotification(true)
                .setLastLevelNotificationRepeatDelay(hours(6))
                .setHealthNotificationConfigs(
                        new HealthNotificationConfig(HEALTH_STATUS_WARNING,
                                new HealthLevelNotificationConfig(
                                        .5f,
                                        false),
                                new HealthRecoveryNotificationConfig(
                                        hours(3))),
                        new HealthNotificationConfig(HEALTH_STATUS_CRITICAL,
                                new HealthLevelNotificationConfig(
                                        0f,
                                        R.string.healthnotification_sleep_title_when_critical,
                                        R.string.healthnotification_sleep_text_when_critical,
                                        true),
                                new HealthRecoveryNotificationConfig(
                                        hours(6))))
                .build();

        DEFAULT_CONFIGURATIONS.add(configuration);
        DEFAULT_CONFIGURATIONS_MAP.put(HEALTH_ID_SLEEP, configuration);

        configuration = new HealthConfiguration.Builder(HEALTH_ID_PLAY, R.drawable.ic_play_bone, hours(6))
                .setPriority(3)
                .setLastLevelNotificationRepeatDelay(hours(6))
                .setHealthNotificationConfigs(
                        new HealthNotificationConfig(HEALTH_STATUS_CRITICAL,
                                new HealthLevelNotificationConfig(
                                        .0f,
                                        R.string.healthnotification_play_title_when_critical,
                                        R.string.healthnotification_play_text_when_critical,
                                        true)))
                .build();

        DEFAULT_CONFIGURATIONS.add(configuration);
        DEFAULT_CONFIGURATIONS_MAP.put(HEALTH_ID_PLAY, configuration);

        CUR_CONFIGURATIONS = DEFAULT_CONFIGURATIONS;
        CUR_CONFIGURATIONS_MAP = DEFAULT_CONFIGURATIONS_MAP;
    }

    private int mId;
    private int mResourceId;
    private HealthNotificationConfig[] mHealthNotificationConfigs;
    private long mLevelAutoUpdatePeriod;
    private int mRecoveryNotificationTitle;
    private int mRecoveryNotificationText;
    private boolean mShowRecoveryNotification;
    private boolean mAutoUpdateLevelEnabled;
    private long mLastLevelNotificationRepeatDelay;
    private int mPriority;

    private HealthConfiguration(
            @HealthId int id,
            int mPriority,
            @DrawableRes int resourceId,
            long levelAutoUpdatePeriod,
            @StringRes int recoveryNotificationTitle,
            @StringRes int recoveryNotificationText,
            boolean showRecoveryNotification,
            boolean autoUpdateLevelEnabled,
            long mLastLevelNotificationRepeatDelay,
            HealthNotificationConfig[] healthNotificationConfigs) {

        this.mId = id;
        this.mPriority = mPriority;
        this.mResourceId = resourceId;
        this.mLevelAutoUpdatePeriod = levelAutoUpdatePeriod;
        this.mRecoveryNotificationTitle = recoveryNotificationTitle;
        this.mRecoveryNotificationText = recoveryNotificationText;
        this.mShowRecoveryNotification = showRecoveryNotification;
        this.mAutoUpdateLevelEnabled = autoUpdateLevelEnabled;
        this.mLastLevelNotificationRepeatDelay = mLastLevelNotificationRepeatDelay;
        this.mHealthNotificationConfigs = healthNotificationConfigs;

        Arrays.sort(healthNotificationConfigs);
    }

    private static long hours(int hours) {
        return TimeUnit.HOURS.toMillis(hours);
    }

    @HealthId
    public int getId() {
        return mId;
    }

    @DrawableRes
    public int getResourceId() {
        return DEFAULT_CONFIGURATIONS_MAP.get(mId).mResourceId;
    }

    static List<HealthConfiguration> getCurrent() {
        return CUR_CONFIGURATIONS;
    }

    static List<HealthConfiguration> getDefault() {
        return DEFAULT_CONFIGURATIONS;
    }

    static HealthConfiguration getById(@HealthId int id) {
        return CUR_CONFIGURATIONS_MAP.get(id);
    }

    @StringRes
    int getRecoveryNotificationTitle() {
        return mRecoveryNotificationTitle;
    }

    @StringRes
    int getRecoveryNotificationText() {
        return mRecoveryNotificationText;
    }

    public HealthNotificationConfig[] getHealthNotificationConfig() {
        return mHealthNotificationConfigs;
    }

    public long getLevelAutoUpdatePeriod() {
        return mLevelAutoUpdatePeriod;
    }

    public void setLevelAutoUpdatePeriod(long mLevelAutoUpdatePeriod) {
        this.mLevelAutoUpdatePeriod = mLevelAutoUpdatePeriod;
    }

    public void setLastLevelNotificationRepeatDelay(long mLastLevelNotificationRepeatDelay) {
        this.mLastLevelNotificationRepeatDelay = mLastLevelNotificationRepeatDelay;
    }

    static void setCurrent(@NonNull HealthConfiguration[] configurations) {
        SparseArray<HealthConfiguration> configurationMap = new SparseArray<>();
        List<HealthConfiguration> configurationList = new ArrayList<>();
        for (HealthConfiguration configuration : configurations) {
            configurationList.add(configuration);
            configurationMap.put(configuration.getId(), configuration);
        }
        CUR_CONFIGURATIONS_MAP = configurationMap;
        CUR_CONFIGURATIONS = configurationList;
    }

    static void resetToDefault() {
        CUR_CONFIGURATIONS = DEFAULT_CONFIGURATIONS;
        CUR_CONFIGURATIONS_MAP = DEFAULT_CONFIGURATIONS_MAP;
    }

    int getPriority() {
        return mPriority;
    }

    boolean isShowRecoveryNotification() {
        return mShowRecoveryNotification;
    }

    boolean isAutoUpdateLevelEnabled() {
        return mAutoUpdateLevelEnabled;
    }

    public long getLastLevelNotificationRepeatDelay() {
        return mLastLevelNotificationRepeatDelay;
    }

    public static class Builder {

        private int mId;
        private int mResourceId;
        private HealthNotificationConfig[] mHealthNotificationConfigs;
        private long mLevelAutoUpdatePeriod;
        private int mRecoveryNotificationTitle;
        private int mRecoveryNotificationText;
        private boolean mShowRecoveryNotification;
        private boolean mAutoUpdateLevelEnabled = true;
        private long mLastLevelNotificationRepeatDelay;
        private int mPriority;

        Builder(@HealthId int mId, @DrawableRes int mResourceId) {
            this(mId, mResourceId, -1);
        }

        Builder(@HealthId int mId, @DrawableRes int mResourceId, long mLevelAutoUpdatePeriod) {
            this.mId = mId;
            this.mResourceId = mResourceId;
            this.mLevelAutoUpdatePeriod = mLevelAutoUpdatePeriod;
        }

        Builder setPriority(int mPriority) {
            this.mPriority = mPriority;
            return this;
        }

        Builder setHealthNotificationConfigs(HealthNotificationConfig... healthNotificationConfigs) {
            this.mHealthNotificationConfigs = healthNotificationConfigs;
            return this;
        }

        Builder setRecoveryNotificationTitle(int mRecoveryNotificationTitle) {
            this.mRecoveryNotificationTitle = mRecoveryNotificationTitle;
            return this;

        }

        Builder setRecoveryNotificationText(int mRecoveryNotificationText) {
            this.mRecoveryNotificationText = mRecoveryNotificationText;
            return this;
        }

        Builder setShowRecoveryNotification(boolean mShowRecoveryNotification) {
            this.mShowRecoveryNotification = mShowRecoveryNotification;
            return this;
        }

        Builder setLevelAutoUpdateEnabled(boolean mAutoUpdateLevelEnabled) {
            this.mAutoUpdateLevelEnabled = mAutoUpdateLevelEnabled;
            return this;
        }

        Builder setLastLevelNotificationRepeatDelay(long mLastLevelNotificationRepeatDelay) {
            this.mLastLevelNotificationRepeatDelay = mLastLevelNotificationRepeatDelay;
            return this;
        }

        HealthConfiguration build() {
            return new HealthConfiguration(mId, mPriority, mResourceId, mLevelAutoUpdatePeriod,
                    mRecoveryNotificationTitle, mRecoveryNotificationText,
                    mShowRecoveryNotification, mAutoUpdateLevelEnabled,
                    mLastLevelNotificationRepeatDelay, mHealthNotificationConfigs);
        }
    }
}
