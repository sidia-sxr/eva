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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface EvaConstants {

    int HOST_VISIBILITY_DURATION = 5 * 60; // in seconds
    int TEXTURE_BUFFER_SIZE = 2048;
    float MODEL3D_DEFAULT_SCALE = 0.003f;
    boolean ENABLE_NOTIFICATION_POINTS = true;

    int SHARE_MODE_NONE = 0;
    int SHARE_MODE_HOST = 1;
    int SHARE_MODE_GUEST = 2;

    @IntDef({SHARE_MODE_NONE, SHARE_MODE_HOST, SHARE_MODE_GUEST})
    @Retention(RetentionPolicy.SOURCE)
    @interface ShareMode {
    }
}
