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

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.PersistableBundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.format.DateUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import br.org.sidia.eva.BuildConfig;

public final class HealthManager extends ContextWrapper {

    private static final String TAG = "HealthManager";

    public static final int HEALTH_ID_DRINK = 1;
    public static final int HEALTH_ID_SLEEP = 2;
    public static final int HEALTH_ID_PEE = 4;
    public static final int HEALTH_ID_PLAY = 8;

    public static final int HEALTH_STATUS_NORMAL = 16;
    public static final int HEALTH_STATUS_WARNING = 32;
    public static final int HEALTH_STATUS_CRITICAL = 64;
    public static final int HEALTH_STATUS_RECOVERING = 128;

    private ComponentName mServiceComponent;
    private JobScheduler mJobScheduler;
    private HealthPreferencesHelper mHealthPreferencesHelper;
    private HealthNotificationHelper mHealthNotificationHelper;
    private boolean mShowNotifications = true;
    private List<OnLevelUpdateListener> mUpdateListeners = new ArrayList<>();

    private static volatile HealthManager sInstance;

    private HealthManager(Context context) {
        super(context);
        mServiceComponent = new ComponentName(context, NotificationService.class);
        mJobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mHealthPreferencesHelper = new HealthPreferencesHelper(context);
        mHealthNotificationHelper = new HealthNotificationHelper(context);
        loadPreferences();
        initSavedHealthStates();
        mUpdateListeners.add(new LevelUpdateRules());
    }

    public static HealthManager getInstance(Context context) {
        synchronized (HealthManager.class) {
            if (sInstance == null) {
                synchronized (HealthManager.class) {
                    sInstance = new HealthManager(context);
                }
            }
        }
        return sInstance;
    }

    private void loadPreferences() {
        HealthConfiguration[] preferences = mHealthPreferencesHelper.getHealthPreferences();
        if (BuildConfig.ENABLE_HEALTH_PREFERENCES) {
            if (preferences == null) {
                // Init preferences with default configuration
                mHealthPreferencesHelper.setHealthPreferences(
                        getDefaultConfiguration().toArray(new HealthConfiguration[0]));
            } else {
                // Sets current configuration with
                HealthConfiguration.setCurrent(preferences);
            }
        } else {
            if (preferences != null) {
                mHealthPreferencesHelper.removeHealthPreferences();
            }
        }
    }

    public void savePreferences() {
        if (BuildConfig.ENABLE_HEALTH_PREFERENCES) {
            mHealthPreferencesHelper.setHealthPreferences(
                    getCurrentConfiguration().toArray(new HealthConfiguration[0]));
        }
    }

    public void resetConfigurationToDefault() {
        if (BuildConfig.ENABLE_HEALTH_PREFERENCES) {
            HealthConfiguration.resetToDefault();
            mHealthPreferencesHelper.setHealthPreferences(
                    getCurrentConfiguration().toArray(new HealthConfiguration[0]));
        }
    }

    private void initSavedHealthStates() {
        if (mHealthPreferencesHelper.getHealthSettings(HEALTH_ID_DRINK) == null) {
            mJobScheduler.cancelAll();
            for (HealthConfiguration configuration : getCurrentConfiguration()) {
                initLiveSettings(configuration.getId());
            }
        }
    }

    public List<HealthConfiguration> getCurrentConfiguration() {
        return HealthConfiguration.getCurrent();
    }

    public List<HealthConfiguration> getDefaultConfiguration() {
        return HealthConfiguration.getDefault();
    }

    public HealthConfiguration getConfigurationById(@HealthId int id) {
        return HealthConfiguration.getById(id);
    }

    public void setLevel(@HealthId int id, @FloatRange(from = -1f, to = 1f) float desiredLevel) {
        HealthLiveSettings settings = mHealthPreferencesHelper.getHealthSettings(id);
        HealthConfiguration configuration = HealthConfiguration.getById(id);
        float level = getLevel(configuration, settings);
        addLevel(id, desiredLevel - level, settings);
    }

    public void addLevel(@HealthId int id, @FloatRange(from = -1f, to = 1f) float addValue) {
        addLevel(id, addValue, mHealthPreferencesHelper.getHealthSettings(id));
    }

    private void addLevel(
            @HealthId int id,
            @FloatRange(from = -1f, to = 1f) float value,
            HealthLiveSettings settings) {

        if (isRecoveringHealth(id)) {
            Log.d(TAG, "addLevel: cannot set level during recovery mode");
            return;
        }

        HealthConfiguration configuration = HealthConfiguration.getById(id);
        float currentLevel = getLevel(configuration, settings);
        float adjustedValue;

        if (value < 0) { // decrease level
            adjustedValue = (currentLevel + value) < 0 ? -currentLevel : value;
        } else { // increase level
            adjustedValue = (currentLevel + value) > 1 ? 1 - currentLevel : value;
        }

        HealthNotificationConfig previousStatus =
                resolveCurrentNotificationConfig(configuration, settings);

        if (configuration.isAutoUpdateLevelEnabled()) {

            long newScheduledTime = settings.getScheduledAt() +
                    Math.round(configuration.getLevelAutoUpdatePeriod() * adjustedValue);

            settings.setScheduledAt(newScheduledTime);

        } else {
            settings.setLevel(settings.getLevel() + adjustedValue);
        }

        mHealthPreferencesHelper.setHealthSettings(settings);
        updateNotificationJob(id);

        HealthNotificationConfig newStatus = resolveCurrentNotificationConfig(configuration, settings);

        // Checks and notifies lost notification
        if (value < 0 && newStatus != null && newStatus != previousStatus) {

            if (newStatus.getLevelNotificationConfig().isShowNotification()) {
                if (isHighestPriority(configuration.getPriority())) {
                    showNotification(id, configuration.getResourceId(),
                            newStatus.getLevelNotificationConfig().getTitle(),
                            newStatus.getLevelNotificationConfig().getText());
                }
            }
            if (EventBus.getDefault().hasSubscriberForEvent(HealthNotificationEvent.class)) {
                EventBus.getDefault().post(new HealthNotificationEvent(
                        id, getLevel(configuration, settings), newStatus.getStatus()));
            }
        }

        notifyLevelUpdated(this, id, currentLevel, currentLevel + adjustedValue);
    }

    public void recoverHealth(@HealthId int id) {

        HealthConfiguration configuration = HealthConfiguration.getById(id);

        if (!configuration.isAutoUpdateLevelEnabled()) {
            Log.d(TAG, "recoverHealth: cannot update the level automatically for id == " + id +
                    ". the level is configured to be updated by user.");
            return;
        }

        if (isRecoveringHealth(id)) {
            Log.d(TAG, "recoverHealth: already is recovering ic_health_preferences for id == " + id);
            return;
        }

        HealthLiveSettings settings = mHealthPreferencesHelper.getHealthSettings(id);
        HealthNotificationConfig config = resolveCurrentNotificationConfig(configuration, settings);

        if (config != null) {

            HealthRecoveryNotificationConfig recoveryNotificationConfig =
                    config.getRecoveryNotificationConfig();

            if (recoveryNotificationConfig != null) {

                cancelNotification(id);

                settings.setRecoveryStartedAt(System.currentTimeMillis());
                mHealthPreferencesHelper.setHealthSettings(settings);

                long duration = recoveryNotificationConfig.getDuration();

                PersistableBundle bundle = new PersistableBundle();
                bundle.putLong("recoveryDuration", duration);
                bundle.putDouble("recoveryRemainingLevel", 1 - getLevel(configuration, settings));

                int recoveryId = addRecoveryStatusFlag(id);

                if (scheduleNotificationJob(recoveryId, duration, bundle)) {
                    String formatString = "Recovery notification job id= %d scheduled to start after %s";
                    Log.d(TAG, String.format(Locale.getDefault(), formatString,
                            recoveryId, DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(duration))));
                }

            } else {
                Log.d(TAG, "recoverHealth: no recovery configuration found for id == "
                        + id + " and status == " + config.getStatus());
            }
        } else {
            Log.d(TAG, "recoverHealth: ic_health_preferences id " + id + " is in normal status");
        }
    }

    public void cancelNotification(@HealthId int id) {
        if (mJobScheduler.getPendingJob(id) != null) {
            mJobScheduler.cancel(id);
            Log.d(TAG, "cancelNotification: " + id);
        } else {
            int recId = addRecoveryStatusFlag(id);
            if (mJobScheduler.getPendingJob(recId) != null) {
                mJobScheduler.cancel(recId);
                Log.d(TAG, "cancelNotification: " + id);
            }
        }
    }

    private int addRecoveryStatusFlag(@HealthId int id) {
        return containsRecoveryStatusFlag(id) ? id : id | HEALTH_STATUS_RECOVERING;
    }

    private boolean containsRecoveryStatusFlag(@HealthId int id) {
        return HEALTH_STATUS_RECOVERING == (id & HEALTH_STATUS_RECOVERING);
    }

    private int removeRecoveryStatusFlag(@HealthId int id) {
        return containsRecoveryStatusFlag(id) ? id & ~HEALTH_STATUS_RECOVERING : id;
    }

    public void resetHealth(@HealthId int id) {
        cancelNotification(id);
        initLiveSettings(id);
        startNotification(id);
    }

    public void setShowNotifications(boolean showNotifications) {
        this.mShowNotifications = showNotifications;
    }

    public void startNotifications() {
        for (HealthConfiguration configuration : getCurrentConfiguration()) {
            if (configuration.isAutoUpdateLevelEnabled()) {
                startNotification(configuration.getId());
            }
        }
    }

    public void addOnLevelUpdateListener(OnLevelUpdateListener listener) {
        if (!mUpdateListeners.contains(listener)) {
            mUpdateListeners.add(listener);
            Log.d(TAG, "addOnLevelUpdateListener: " + listener);
        }
    }

    public void removeOnLevelUpdateListener(OnLevelUpdateListener listener) {
        if (mUpdateListeners.contains(listener)) {
            mUpdateListeners.remove(listener);
            Log.d(TAG, "removeOnLevelUpdateListener: " + listener);
        }
    }

    private void notifyLevelUpdated(
            HealthManager healthManager,
            @HealthId int id,
            @FloatRange(from = 0, to = 1) float oldValue,
            @FloatRange(from = 0, to = 1) float newValue) {

        mUpdateListeners.forEach(listener -> listener.onUpdate(healthManager, id, oldValue, newValue));
    }

    private void startNotification(@HealthId int id) {
        if (!hasScheduledJob(id)) {
            updateNotificationJob(id);
        }
    }

    void handleNotification(@HealthId final int id) {

        if (containsRecoveryStatusFlag(id)) {
            int _id = removeRecoveryStatusFlag(id);
            resetHealth(_id);
            HealthConfiguration configuration = HealthConfiguration.getById(_id);
            if (configuration.isShowRecoveryNotification()) {
                showNotification(_id, configuration.getResourceId(),
                        configuration.getRecoveryNotificationTitle(),
                        configuration.getRecoveryNotificationText());
            }
            return;
        }

        HealthConfiguration configuration = HealthConfiguration.getById(id);
        HealthLiveSettings settings = mHealthPreferencesHelper.getHealthSettings(id);
        HealthNotificationConfig config = resolveCurrentNotificationConfig(configuration, settings);

        if (config != null) {
            if (config.getLevelNotificationConfig().isShowNotification()) {
                if (isHighestPriority(configuration.getPriority())) {
                    showNotification(id, configuration.getResourceId(),
                            config.getLevelNotificationConfig().getTitle(),
                            config.getLevelNotificationConfig().getText());
                }
            }
            if (EventBus.getDefault().hasSubscriberForEvent(HealthNotificationEvent.class)) {
                EventBus.getDefault().post(new HealthNotificationEvent(
                        id, getLevel(configuration, settings), config.getStatus()));
            }
        } else {
            Log.d(TAG, "handleNotification: level status is NORMAl");
        }

        updateNotificationJob(id);
    }

    private boolean isHighestPriority(int priority) {
        List<HealthConfiguration> result = HealthConfiguration.getCurrent()
                .stream()
                .filter(conf -> {
                    int i = resolveCurrentNotificationConfigIndex(conf, mHealthPreferencesHelper.getHealthSettings(conf.getId()));
                    return i == conf.getHealthNotificationConfig().length - 1;
                })
                .sorted((c1, c2) -> c1.getPriority() - c2.getPriority())
                .collect(Collectors.toList());
        return result.isEmpty() || priority <= result.get(0).getPriority();
    }

    public HealthStateSummary getHealthStateSummary(@HealthId int id) {

        HealthLiveSettings settings = mHealthPreferencesHelper.getHealthSettings(id);
        HealthConfiguration configuration = HealthConfiguration.getById(id);

        if (settings.getScheduledAt() == 0) {
            return new HealthStateSummary(id, 1f, HEALTH_STATUS_NORMAL,
                    configuration.getLevelAutoUpdatePeriod(), isRecoveringHealth(id));
        }

        float level = getLevel(configuration, settings);
        int status = getStatus(configuration, level);
        long remainingTime = getRemainingTime(configuration, level);

        return new HealthStateSummary(id, level, status, remainingTime, isRecoveringHealth(id));
    }

    private long getRemainingTime(HealthConfiguration configuration, @FloatRange(from = 0, to = 1) float level) {
        JobInfo recoveryJob = getRecoveryPendingJob(configuration.getId());
        if (recoveryJob != null) {
            long duration = recoveryJob.getExtras().getLong("recoveryDuration");
            return Math.round(duration - getRecoveryTimeProgress(configuration.getId()) * duration);
        } else {
            return Math.round(configuration.getLevelAutoUpdatePeriod() * level);
        }
    }

    @FloatRange(from = 0, to = 1)
    private float getLevel(HealthConfiguration configuration, @NonNull HealthLiveSettings settings) {
        if (!configuration.isAutoUpdateLevelEnabled()) {
            return settings.getLevel();
        } else if (isRecoveringHealth(configuration.getId())) {
            return getRecoveryProgress(configuration.getId());
        } else {
            float elapsed = System.currentTimeMillis() - settings.getScheduledAt();
            return 1f - Math.max(0, Math.min(elapsed / configuration.getLevelAutoUpdatePeriod(), 1f));
        }
    }

    @FloatRange(from = 0, to = 1)
    private float getRecoveryProgress(@HealthId int id) {
        JobInfo scheduledJob = getRecoveryPendingJob(id);
        if (scheduledJob != null) {
            float remainingLevel = (float) scheduledJob.getExtras().getDouble("recoveryRemainingLevel");
            return 1 - remainingLevel + (getRecoveryTimeProgress(id) * remainingLevel);
        }
        return 0;
    }

    @FloatRange(from = 0, to = 1)
    private float getRecoveryTimeProgress(@HealthId int id) {
        JobInfo scheduledJob = getRecoveryPendingJob(id);
        if (scheduledJob != null) {
            long duration = scheduledJob.getExtras().getLong("recoveryDuration");
            HealthLiveSettings settings = mHealthPreferencesHelper.getHealthSettings(id);
            float elapsed = System.currentTimeMillis() - settings.getRecoveryStartedAt();
            return Math.max(0, Math.min(elapsed / duration, 1f));
        }
        return 0;
    }

    private JobInfo getRecoveryPendingJob(@HealthId int id) {
        return mJobScheduler.getPendingJob(addRecoveryStatusFlag(id));
    }

    @HealthStatus
    private int getStatus(
            @NonNull HealthConfiguration configuration,
            @FloatRange(from = 0f, to = 1f) float level) {

        HealthNotificationConfig[] levels = configuration.getHealthNotificationConfig();

        for (int i = levels.length - 1; i >= 0; i--) {
            HealthNotificationConfig l = levels[i];
            if (level <= l.getLevelNotificationConfig().getTargetLevel()) {
                return l.getStatus();
            }
        }

        return HEALTH_STATUS_NORMAL;
    }

    private boolean isRecoveringHealth(@HealthId int id) {
        return getRecoveryPendingJob(id) != null;
    }

    private boolean hasScheduledJob(@HealthId int id) {
        return mJobScheduler.getPendingJob(id) != null
                || getRecoveryPendingJob(id) != null;
    }

    private void initLiveSettings(@HealthId int id) {
        HealthLiveSettings settings = new HealthLiveSettings(id);
        settings.setScheduledAt(System.currentTimeMillis());
        mHealthPreferencesHelper.setHealthSettings(settings);
    }

    @Nullable
    private HealthNotificationConfig resolveCurrentNotificationConfig(
            HealthConfiguration configuration,
            HealthLiveSettings settings) {

        HealthNotificationConfig[] configs = configuration.getHealthNotificationConfig();
        float curLevelValue = getLevel(configuration, settings);

        for (int i = configs.length - 1; i >= 0; i--) {
            HealthNotificationConfig config = configs[i];
            if (curLevelValue <= config.getLevelNotificationConfig().getTargetLevel()) {
                return config;
            }
        }

        return null; // level status NORMAL
    }

    private int resolveCurrentNotificationConfigIndex(
            HealthConfiguration configuration,
            HealthLiveSettings settings) {

        HealthNotificationConfig[] configs = configuration.getHealthNotificationConfig();
        float curLevelValue = getLevel(configuration, settings);

        for (int i = configs.length - 1; i >= 0; i--) {
            HealthNotificationConfig config = configs[i];
            if (curLevelValue <= config.getLevelNotificationConfig().getTargetLevel()) {
                return i;
            }
        }

        return -1; // level status NORMAL
    }

    private boolean scheduleNotificationJob(int jobId, long delay) {
        return scheduleNotificationJob(jobId, delay, null);
    }

    private boolean scheduleNotificationJob(int jobId, long delay, PersistableBundle bundle) {

        JobInfo.Builder builder = new JobInfo.Builder(jobId, mServiceComponent);
        builder.setMinimumLatency(delay);
        builder.setPersisted(true);

        if (bundle != null) {
            builder.setExtras(bundle);
        }

        JobInfo jobInfo = builder.build();

        if (mJobScheduler.schedule(jobInfo) == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled: " + jobInfo.getId());
            return true;
        } else {
            Log.d(TAG, "Failure scheduling job " + jobId);
            return false;
        }
    }

    private void showNotification(@DrawableRes int id, @DrawableRes int resourceId,
                                  @StringRes int title, @StringRes int text) {
        if (mShowNotifications) {
            mHealthNotificationHelper.notify(id,
                    mHealthNotificationHelper.getHealthNotification(resourceId, title, text));
        }
    }

    private void updateNotificationJob(@HealthId int id) {

        cancelNotification(id);

        HealthConfiguration configuration = HealthConfiguration.getById(id);
        HealthLiveSettings settings = mHealthPreferencesHelper.getHealthSettings(id);
        HealthNotificationConfig[] configs = configuration.getHealthNotificationConfig();
        HealthNotificationConfig nextLevelConfig = null;

        float curLevel = getLevel(configuration, settings);

        for (HealthNotificationConfig config : configs) {
            if (curLevel > config.getLevelNotificationConfig().getTargetLevel()) {
                nextLevelConfig = config;
                break;
            }
        }

        if (nextLevelConfig != null) {

            if (configuration.isAutoUpdateLevelEnabled()) {
                long notificationDelayLength = Math.round(configuration.getLevelAutoUpdatePeriod()
                        * (1 - nextLevelConfig.getLevelNotificationConfig().getTargetLevel()));
                long notificationTime = settings.getScheduledAt() + notificationDelayLength;
                long delay = notificationTime - System.currentTimeMillis();

                if (delay >= 0 && scheduleNotificationJob(configuration.getId(), delay)) {
                    Log.d(TAG, "updateNotificationJob: scheduling notification for the next level: " + nextLevelConfig);
                    logScheduledJob(id, nextLevelConfig.getStatus(), delay);
                }
            }

        } else { // no configuration registered or last level reached

            Log.d(TAG, "updateNotificationJob: no more next level notification to schedule. rescheduling the current");
            HealthNotificationConfig curConfig = resolveCurrentNotificationConfig(configuration, settings);
            if (curConfig != null) {
                long delay = configuration.getLastLevelNotificationRepeatDelay();
                if (delay > 0 && scheduleNotificationJob(configuration.getId(), delay)) {
                    logScheduledJob(id, curConfig.getStatus(), delay);
                }
            }
        }
    }

    private void logScheduledJob(int jobId, @HealthStatus int status, long delay) {
        String formatString = "Level notification job id= %d, status= %d scheduled to start after %s";
        Log.d(TAG, String.format(Locale.getDefault(), formatString,
                jobId, status, DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(delay))));
    }

}
