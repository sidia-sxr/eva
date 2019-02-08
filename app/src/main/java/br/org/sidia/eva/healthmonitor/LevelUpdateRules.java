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

class LevelUpdateRules implements OnLevelUpdateListener {

    @Override
    public void onUpdate(
            HealthManager healthManager,
            @HealthId int id,
            @FloatRange(from = 0, to = 1) float oldLevel,
            @FloatRange(from = 0, to = 1) float newLevel) {

        float levelDiff = newLevel - oldLevel;

        if (id == HealthManager.HEALTH_ID_DRINK) {
            if (levelDiff > 0) {
                healthManager.addLevel(HealthManager.HEALTH_ID_PEE, -levelDiff);
            }
        } else if (id == HealthManager.HEALTH_ID_PLAY) {
            if (levelDiff > 0) {
                healthManager.addLevel(HealthManager.HEALTH_ID_SLEEP, -2 * levelDiff);
            }
        }
    }
}
