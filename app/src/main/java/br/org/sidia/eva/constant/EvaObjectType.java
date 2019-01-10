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

package br.org.sidia.eva.constant;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({
        EvaObjectType.EVA, EvaObjectType.BED, EvaObjectType.BOWL,
        EvaObjectType.HYDRANT, EvaObjectType.CAMERA, EvaObjectType.PLANE, EvaObjectType.PLAYER})
@Retention(RetentionPolicy.SOURCE)
public @interface EvaObjectType {
    String EVA = "EVA";
    String BED = "BED";
    String BOWL = "BOWL";
    String HYDRANT = "HYDRANT";
    String CAMERA = "CAMERA";
    String PLANE = "PLANE";
    String PLAYER = "PLAYER";
}