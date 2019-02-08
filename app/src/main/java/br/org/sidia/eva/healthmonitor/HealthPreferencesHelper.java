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
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import br.org.sidia.eva.BuildConfig;

@SuppressLint("ApplySharedPref")
class HealthPreferencesHelper {

    private static final String PREF_NAME = BuildConfig.APPLICATION_ID + ".HEALTH_STATE";
    private static final String PREF_HEALTH_SETTINGS = "PREF_HEALTH_SETTINGS";
    private static final String PREF_HEALTH_PREFERENCES = "PREF_HEALTH_PREFERENCES";

    private SharedPreferences mSharedPref;

    HealthPreferencesHelper(Context context) {
        mSharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    HealthLiveSettings getHealthSettings(@HealthId int id) {
        return readObject(PREF_HEALTH_SETTINGS + "." + id, HealthLiveSettings.class);
    }

    void setHealthSettings(@NonNull HealthLiveSettings notification) {
        writeObject(PREF_HEALTH_SETTINGS + "." + notification.getId(), notification);
    }

    HealthConfiguration[] getHealthPreferences() {
        return readObject(PREF_HEALTH_PREFERENCES, HealthConfiguration[].class);
    }

    void setHealthPreferences(HealthConfiguration... preferences) {
        writeObject(PREF_HEALTH_PREFERENCES, preferences);
    }

    void removeHealthPreferences() {
        mSharedPref.edit().remove(PREF_HEALTH_PREFERENCES).commit();
    }

    private void writeObject(String name, Object obj) {
        SharedPreferences sharedPref = mSharedPref;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name, new Gson().toJson(obj));
        editor.commit();
    }

    private <T> T readObject(String name, Class<T> type) {
        String gsonString = mSharedPref.getString(name, null);
        return gsonString != null ? new Gson().fromJson(gsonString, type) : null;
    }
}
