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

import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import br.org.sidia.eva.EvaContext;

public class HealthPreferencesViewHelper {

    private EvaContext mEvaContext;
    private HealthManager mHealthManager;

    public HealthPreferencesViewHelper(EvaContext mEvaContext, HealthManager mHealthManager) {
        this.mEvaContext = mEvaContext;
        this.mHealthManager = mHealthManager;
    }

    public void apply(IHealthPreferencesView view) {

        AtomicBoolean changed = new AtomicBoolean(false);

        view.getPreferences().forEach((HealthPreferenceViewModel p) -> {

            HealthConfiguration conf = mHealthManager.getConfigurationById(p.getId());

            if (conf.getLevelAutoUpdatePeriod() != p.getDuration()) {
                changed.set(true);
                mHealthManager.cancelNotification(conf.getId());
                conf.setLevelAutoUpdatePeriod(p.getDuration());
                mHealthManager.resetHealth(conf.getId());
            }

            HealthNotificationConfig configWarning = findConfigurationByStatus(
                    HealthManager.HEALTH_STATUS_WARNING, conf.getHealthNotificationConfig());
            HealthNotificationConfig configCritical = findConfigurationByStatus(
                    HealthManager.HEALTH_STATUS_CRITICAL, conf.getHealthNotificationConfig());

            if (configWarning != null && configWarning.getRecoveryNotificationConfig() != null) {
                long oldValue = configWarning.getRecoveryNotificationConfig().getDuration();
                long newValue = p.getRecoveryDurationWhenWarning();
                if (oldValue != newValue) {
                    changed.set(true);
                    configWarning.getRecoveryNotificationConfig().setDuration(newValue);
                }
            }

            if (configCritical != null && configCritical.getRecoveryNotificationConfig() != null) {
                long oldValue = configCritical.getRecoveryNotificationConfig().getDuration();
                long newValue = p.getRecoveryDurationWhenCritical();
                if (oldValue != newValue) {
                    changed.set(true);
                    configCritical.getRecoveryNotificationConfig().setDuration(newValue);
                }
            }

            if (conf.getLastLevelNotificationRepeatDelay() != p.getCriticalRepeatDelay()) {
                changed.set(true);
                conf.setLastLevelNotificationRepeatDelay(p.getCriticalRepeatDelay());
            }

        });

        if (changed.get()) {
            mHealthManager.savePreferences();
            Toast.makeText(mEvaContext.getActivity(), "Changes applied", Toast.LENGTH_SHORT).show();
            view.setPreferences(getViewModels(mHealthManager.getCurrentConfiguration()));
        } else {
            Toast.makeText(mEvaContext.getActivity(), "No change to apply", Toast.LENGTH_SHORT).show();

        }
    }

    public List<HealthPreferenceViewModel> getViewModels(List<HealthConfiguration> configurations) {

        List<HealthPreferenceViewModel> viewModels = new ArrayList<>();

        configurations.forEach(conf -> {

            HealthStateSummary summary = mHealthManager.getHealthStateSummary(conf.getId());
            HealthNotificationConfig configWarning = findConfigurationByStatus(
                    HealthManager.HEALTH_STATUS_WARNING, conf.getHealthNotificationConfig());
            HealthNotificationConfig configCritical = findConfigurationByStatus(
                    HealthManager.HEALTH_STATUS_CRITICAL, conf.getHealthNotificationConfig());

            HealthRecoveryNotificationConfig warningRecoveryConfig = configWarning != null ?
                    configWarning.getRecoveryNotificationConfig() : null;

            HealthRecoveryNotificationConfig criticalRecoveryConfig = configCritical != null ?
                    configCritical.getRecoveryNotificationConfig() : null;

            HealthPreferenceViewModel viewModel = new HealthPreferenceViewModel(
                    summary.getId(),
                    summary.getLevel(),
                    summary.getRemainingTime(),
                    summary.getStatus(),
                    conf.getResourceId(),
                    conf.getLevelAutoUpdatePeriod(),
                    configWarning != null ? configWarning.getLevelNotificationConfig().getTargetLevel() : -1,
                    configCritical != null ? configCritical.getLevelNotificationConfig().getTargetLevel() : -1,
                    conf.getLastLevelNotificationRepeatDelay(),
                    warningRecoveryConfig != null ? warningRecoveryConfig.getDuration() : -1,
                    criticalRecoveryConfig != null ? criticalRecoveryConfig.getDuration() : -1
            );

            viewModels.add(viewModel);
        });

        return viewModels;
    }

    @Nullable
    private static HealthNotificationConfig findConfigurationByStatus(
            @HealthStatus int status, HealthNotificationConfig[] configs) {
        return Arrays.stream(configs)
                .filter(c -> c.getStatus() == status)
                .findFirst()
                .orElse(null);
    }

}
