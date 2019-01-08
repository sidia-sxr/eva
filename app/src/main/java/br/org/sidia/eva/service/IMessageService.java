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

package br.org.sidia.eva.service;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import br.org.sidia.eva.manager.cloud.anchor.CloudAnchor;
import br.org.sidia.eva.service.data.BallCommand;
import br.org.sidia.eva.service.data.EvaActionCommand;
import br.org.sidia.eva.service.data.RequestStatus;
import br.org.sidia.eva.service.data.ViewCommand;
import br.org.sidia.eva.service.share.SharedObjectPose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IMessageService {

    String MESSAGE_TYPE_EVA_ANCHOR = "MESSAGE_TYPE_EVA_ANCHOR";
    String MESSAGE_TYPE_VIEW_COMMAND = "MESSAGE_TYPE_VIEW_COMMAND";
    String MESSAGE_TYPE_BALL_COMMAND = "MESSAGE_TYPE_BALL_COMMAND";
    String MESSAGE_TYPE_EVA_ACTION_COMMAND = "MESSAGE_TYPE_EVA_ACTION_COMMAND";
    String MESSAGE_TYPE_UPDATE_POSES = "MESSAGE_TYPE_UPDATE_POSES";
    String MESSAGE_TYPE_REQUEST_STATUS = "MESSAGE_TYPE_REQUEST_STATUS";


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            MESSAGE_TYPE_EVA_ANCHOR,
            MESSAGE_TYPE_VIEW_COMMAND,
            MESSAGE_TYPE_BALL_COMMAND,
            MESSAGE_TYPE_EVA_ACTION_COMMAND,
            MESSAGE_TYPE_UPDATE_POSES,
            MESSAGE_TYPE_REQUEST_STATUS
    })
    @interface MessageType {
    }

    int shareEvaAnchor(@NonNull CloudAnchor anchor);

    void sendViewCommand(@NonNull ViewCommand command);

    void sendBallCommand(@NonNull BallCommand command);

    void sendEvaActionCommand(@NonNull EvaActionCommand command);

    void updatePoses(@NonNull SharedObjectPose[] poses);

    void sendRequestStatus(RequestStatus status);
}