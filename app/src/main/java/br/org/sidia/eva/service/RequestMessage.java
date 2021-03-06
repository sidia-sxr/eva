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

import br.org.sidia.eva.connection.socket.bluetooth.BTMessage;
import br.org.sidia.eva.service.data.RequestStatus;

import java.io.Serializable;

public class RequestMessage<Data extends Serializable> extends BTMessage<Data> {

    private String mActionName;
    private RequestStatus mStatus;

    public RequestMessage(@IMessageService.MessageType String mActionName, Data data) {
        super(data);
        this.mActionName = mActionName;
    }

    @IMessageService.MessageType
    public String getActionName() {
        return mActionName;
    }

    public RequestStatus getStatus() {
        return mStatus;
    }

    public void setStatus(RequestStatus mStatus) {
        this.mStatus = mStatus;
    }

    @NonNull
    @Override
    public String toString() {
        return "RequestMessage{" +
                "mActionName='" + mActionName + '\'' +
                "} " + super.toString();
    }
}
