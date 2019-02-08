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

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import br.org.sidia.eva.R;

import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_CRITICAL;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_STATUS_WARNING;

public class HealthPreferencesAdapter extends RecyclerView.Adapter {

    private static final String TAG = "HealthPreferencesAdapter";

    private List<HealthPreferenceViewModel> preferences;

    public HealthPreferencesAdapter(List<HealthPreferenceViewModel> preferences) {
        this.preferences = preferences;
        Log.d(TAG, "HealthPreferencesAdapter: " + preferences.size());
    }

    public void setPreferences(List<HealthPreferenceViewModel> preferences) {
        this.preferences = preferences;
        notifyDataSetChanged();
    }

    public List<HealthPreferenceViewModel> getPreferences() {
        return preferences;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PreferenceViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_health_preferences_item, viewGroup, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        Log.d(TAG, "onBindViewHolder: ");
        HealthPreferenceViewModel viewModel = preferences.get(i);
        PreferenceViewHolder holder = (PreferenceViewHolder) viewHolder;

        holder.type.setImageResource(viewModel.resourceId);
        holder.progressLevel.setProgress(Math.round(viewModel.level * 100));
        holder.setStatusColor(viewModel.level, viewModel.status);
        holder.setLevelText(viewModel.level);
        holder.setRemainingTime(viewModel.remainingTime, viewModel.duration);
        holder.setWarningLevel(viewModel.warningLevel);
        holder.setCriticalLevel(viewModel.criticalLevel);

        holder.setDuration(viewModel.duration);
        holder.setRepeatDelay(viewModel.criticalRepeatDelay);
        holder.setRecoveryDurationWarning(viewModel.recoveryDurationWhenWarning);
        holder.setRecoveryDurationCritical(viewModel.recoveryDurationWhenCritical);

        int incrementString = Integer.valueOf(viewHolder.itemView.getContext()
                .getString(R.string.increment_by).split(" ")[0]);

        final long increment = TimeUnit.MINUTES.toMillis(incrementString);

        holder.buttonIncrementDuration.setOnClickListener(view -> {
            viewModel.duration += increment;
            holder.setDuration(viewModel.duration);
        });
        holder.buttonDecrementDuration.setOnClickListener(view -> {
            viewModel.duration -= increment;
            if (viewModel.duration < increment) {
                viewModel.duration = increment;
            }
            holder.setDuration(viewModel.duration);
        });

        holder.buttonIncrementRepeatDelay.setOnClickListener(view -> {
            viewModel.criticalRepeatDelay += increment;
            holder.setRepeatDelay(viewModel.criticalRepeatDelay);
        });
        holder.buttonDecrementRepeatDelay.setOnClickListener(view -> {
            viewModel.criticalRepeatDelay -= increment;
            if (viewModel.criticalRepeatDelay < increment) {
                viewModel.criticalRepeatDelay = increment;
            }
            holder.setRepeatDelay(viewModel.criticalRepeatDelay);
        });

        holder.buttonIncrementRecoveryDurationWarning.setOnClickListener(view -> {
            viewModel.recoveryDurationWhenWarning += increment;
            holder.setRecoveryDurationWarning(viewModel.recoveryDurationWhenWarning);
        });
        holder.buttonDecrementRecoveryDurationWarning.setOnClickListener(view -> {
            viewModel.recoveryDurationWhenWarning -= increment;
            if (viewModel.recoveryDurationWhenWarning < increment) {
                viewModel.recoveryDurationWhenWarning = increment;
            }
            holder.setRecoveryDurationWarning(viewModel.recoveryDurationWhenWarning);
        });

        holder.buttonIncrementRecoveryDurationCritical.setOnClickListener(view -> {
            viewModel.recoveryDurationWhenCritical += increment;
            holder.setRecoveryDurationCritical(viewModel.recoveryDurationWhenCritical);
        });
        holder.buttonDecrementRecoveryDurationCritical.setOnClickListener(view -> {
            viewModel.recoveryDurationWhenCritical -= increment;
            if (viewModel.recoveryDurationWhenCritical < increment) {
                viewModel.recoveryDurationWhenCritical = increment;
            }
            holder.setRecoveryDurationCritical(viewModel.recoveryDurationWhenCritical);
        });
    }

    @Override
    public int getItemCount() {
        return preferences.size();
    }

    private static class PreferenceViewHolder extends RecyclerView.ViewHolder {

        ImageView type;
        ProgressBar progressLevel;
        TextView inputWarningLevel;
        TextView inputCriticalLevel;
        TextView textLevel;
        TextView textRemainingTime;

        TextView inputDuration;
        TextView inputRepeatDelay;
        TextView inputRecoveryDurationWarning;
        TextView inputRecoveryDurationCritical;

        Button buttonIncrementDuration;
        Button buttonDecrementDuration;
        Button buttonIncrementRepeatDelay;
        Button buttonDecrementRepeatDelay;
        Button buttonIncrementRecoveryDurationWarning;
        Button buttonDecrementRecoveryDurationWarning;
        Button buttonIncrementRecoveryDurationCritical;
        Button buttonDecrementRecoveryDurationCritical;

        PreferenceViewHolder(@NonNull View itemView) {
            super(itemView);

            type = itemView.findViewById(R.id.image_health_type);
            progressLevel = itemView.findViewById(R.id.progress_indicator);
            inputWarningLevel = itemView.findViewById(R.id.input_warning_level);
            inputCriticalLevel = itemView.findViewById(R.id.input_warning_critical);
            textLevel = itemView.findViewById(R.id.text_level);
            textRemainingTime = itemView.findViewById(R.id.text_remaining_time);

            // inputs
            inputDuration = itemView.findViewById(R.id.input_duration);
            inputRepeatDelay = itemView.findViewById(R.id.input_repeat_delay);
            inputRecoveryDurationWarning = itemView.findViewById(R.id.input_recovery_duration_warning);
            inputRecoveryDurationCritical = itemView.findViewById(R.id.input_recovery_duration_critical);

            buttonIncrementDuration = itemView.findViewById(R.id.button_increment_duration);
            buttonDecrementDuration = itemView.findViewById(R.id.button_decrement_duration);

            buttonIncrementRepeatDelay = itemView.findViewById(R.id.button_increment_repeat_delay);
            buttonDecrementRepeatDelay = itemView.findViewById(R.id.button_decrement_repeat_delay);

            buttonIncrementRecoveryDurationWarning = itemView.findViewById(R.id.button_increment_recovery_duration_warning);
            buttonDecrementRecoveryDurationWarning = itemView.findViewById(R.id.button_decrement_recovery_duration_warning);

            buttonIncrementRecoveryDurationCritical = itemView.findViewById(R.id.button_increment_recovery_duration_critical);
            buttonDecrementRecoveryDurationCritical = itemView.findViewById(R.id.button_decrement_recovery_duration_critical);
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        void setLevelText(float level) {
            textLevel.setText(String.format("%.1f%%", level * 100));
        }

        void setStatusColor(float level, @HealthStatus int status) {
            if (status == HEALTH_STATUS_WARNING) {
                progressLevel.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#fff4cf")));
            } else if (status == HEALTH_STATUS_CRITICAL) {
                progressLevel.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#f9babc")));
                if (level <= 0) {
                    progressLevel.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#f9babc")));
                }
            } else {
                progressLevel.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#bfffc9")));
            }
        }

        @SuppressLint("SetTextI18n")
        void setWarningLevel(float level) {
            if (level < 0) {
                ((View) inputWarningLevel.getParent()).setVisibility(View.GONE);
            } else {
                ((View) inputWarningLevel.getParent()).setVisibility(View.VISIBLE);
                inputWarningLevel.setText(String.valueOf(Math.round(level * 100) + "%"));
            }
        }

        @SuppressLint("SetTextI18n")
        void setCriticalLevel(float level) {
            inputCriticalLevel.setText(String.valueOf(Math.round(level * 100) + "%"));
        }

        @SuppressLint("SetTextI18n")
        void setRemainingTime(long time, long duration) {
            if (duration < 0) {
                textRemainingTime.setVisibility(View.GONE);
            } else {
                textRemainingTime.setVisibility(View.VISIBLE);
                textRemainingTime.setText("remaining time " + formatTime(time));
            }
        }

        void setRepeatDelay(long delay) {
            inputRepeatDelay.setText(formatTime(delay));
        }

        void setDuration(long duration) {
            if (duration < 0) {
                ((View) inputDuration.getParent().getParent()).setVisibility(View.GONE);
            } else {
                ((View) inputDuration.getParent().getParent()).setVisibility(View.VISIBLE);
                inputDuration.setText(formatTime(duration));
            }
        }

        void setRecoveryDurationWarning(long duration) {
            if (duration <= 0) {
                ((View) inputRecoveryDurationWarning.getParent().getParent()).setVisibility(View.GONE);
            } else {
                ((View) inputRecoveryDurationWarning.getParent().getParent()).setVisibility(View.VISIBLE);
                inputRecoveryDurationWarning.setText(formatTime(duration));
            }
        }

        void setRecoveryDurationCritical(long duration) {
            if (duration <= 0) {
                ((View) inputRecoveryDurationCritical.getParent().getParent()).setVisibility(View.GONE);
            } else {
                ((View) inputRecoveryDurationCritical.getParent().getParent()).setVisibility(View.VISIBLE);
                inputRecoveryDurationCritical.setText(formatTime(duration));
            }
        }

        private static String formatTime(long time) {
            return formatTime(time, false);
        }

        @SuppressLint("DefaultLocale")
        private static String formatTime(long time, boolean fullTime) {

            long hours = 0, minutes = 0, seconds;

            if (time >= 3600000) {
                hours = time / 3600000;
                time -= hours * 3600000;
            }
            if (time >= 60000) {
                minutes = time / 60000;
                time -= minutes * 60000;
            }
            seconds = time / 1000;

            if (fullTime) {
                return String.format("%02dh%02dm%02ds", hours, minutes, seconds);
            } else {
                return String.format("%02dh%02dm", hours, minutes);
            }
        }
    }
}
