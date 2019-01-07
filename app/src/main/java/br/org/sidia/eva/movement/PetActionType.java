/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package br.org.sidia.eva.movement;

import android.support.annotation.IntDef;

@IntDef({
        PetActions.IDLE.ID,
        PetActions.TO_BALL.ID,
        PetActions.TO_PLAYER.ID,
        PetActions.TO_TAP.ID,
        PetActions.GRAB.ID,
        PetActions.TO_BED.ID,
        PetActions.TO_BOWL.ID,
        PetActions.TO_HYDRANT.ID,
        PetActions.DRINK_ENTER.ID,
        PetActions.DRINK_EXIT.ID,
        PetActions.DRINK_LOOP.ID,
        PetActions.HYDRANT_ENTER.ID,
        PetActions.HYDRANT_EXIT.ID,
        PetActions.HYDRANT_LOOP.ID,
        PetActions.SLEEP_ENTER.ID,
        PetActions.SLEEP_EXIT.ID,
        PetActions.SLEEP_LOOP.ID,
        PetActions.AT_EDIT.ID
})
public @interface PetActionType {
}
