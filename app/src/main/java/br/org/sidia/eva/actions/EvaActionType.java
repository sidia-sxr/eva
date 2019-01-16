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

package br.org.sidia.eva.actions;

import android.support.annotation.IntDef;

@IntDef({
        EvaActions.IDLE.ID,
        EvaActions.TO_BALL.ID,
        EvaActions.TO_PLAYER.ID,
        EvaActions.TO_TAP.ID,
        EvaActions.GRAB.ID,
        EvaActions.TO_BED.ID,
        EvaActions.TO_BOWL.ID,
        EvaActions.TO_HYDRANT.ID,
        EvaActions.DRINK_ENTER.ID,
        EvaActions.DRINK_EXIT.ID,
        EvaActions.DRINK_LOOP.ID,
        EvaActions.HYDRANT_ENTER.ID,
        EvaActions.HYDRANT_EXIT.ID,
        EvaActions.HYDRANT_LOOP.ID,
        EvaActions.SLEEP_ENTER.ID,
        EvaActions.SLEEP_EXIT.ID,
        EvaActions.SLEEP_LOOP.ID
})
public @interface EvaActionType {
}
