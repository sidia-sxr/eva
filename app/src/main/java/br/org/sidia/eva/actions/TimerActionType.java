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
 */

package br.org.sidia.eva.actions;

import android.support.annotation.LongDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@LongDef({TimerActionType.DRINK_NORMAL,
        TimerActionType.DRINK_WARNING,
        TimerActionType.DRINK_CRITICAL,
        TimerActionType.PEE_NORMAL,
        TimerActionType.PEE_WARNING,
        TimerActionType.PEE_CRITICAL})
public @interface TimerActionType {
    long DRINK_NORMAL = 3000; // 3s
    long DRINK_WARNING = 8000; // 8s
    long DRINK_CRITICAL = 14000; // 14s

    long PEE_NORMAL = 5500; // 5,5s
    long PEE_WARNING = 10500; // 10,5s
    long PEE_CRITICAL = 20500; // 20,5s
}
