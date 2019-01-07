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
import android.util.Log;

import br.org.sidia.eva.manager.cloud.anchor.CloudAnchor;
import br.org.sidia.eva.manager.connection.IPetConnectionManager;
import br.org.sidia.eva.manager.connection.PetConnectionManager;
import br.org.sidia.eva.manager.connection.event.MessageReceivedEvent;
import br.org.sidia.eva.service.data.BallCommand;
import br.org.sidia.eva.service.data.PetActionCommand;
import br.org.sidia.eva.service.data.RequestStatus;
import br.org.sidia.eva.service.data.ViewCommand;
import br.org.sidia.eva.service.event.BallCommandReceivedMessage;
import br.org.sidia.eva.service.event.PetActionCommandReceivedMessage;
import br.org.sidia.eva.service.event.PetAnchorReceivedMessage;
import br.org.sidia.eva.service.event.ReceivedMessage;
import br.org.sidia.eva.service.event.RequestStatusReceivedMessage;
import br.org.sidia.eva.service.event.UpdatePosesReceivedMessage;
import br.org.sidia.eva.service.event.ViewCommandReceivedMessage;
import br.org.sidia.eva.service.share.SharedObjectPose;
import br.org.sidia.eva.util.EventBusUtils;

import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class MessageService implements IMessageService {

    private static final String TAG = MessageService.class.getSimpleName();

    private IPetConnectionManager mConnectionManager;

    private static Map<String, Class> mReceivedMessageTypes = new HashMap<>();

    static {
        mReceivedMessageTypes.put(MESSAGE_TYPE_PET_ANCHOR, PetAnchorReceivedMessage.class);
        mReceivedMessageTypes.put(MESSAGE_TYPE_VIEW_COMMAND, ViewCommandReceivedMessage.class);
        mReceivedMessageTypes.put(MESSAGE_TYPE_BALL_COMMAND, BallCommandReceivedMessage.class);
        mReceivedMessageTypes.put(MESSAGE_TYPE_PET_ACTION_COMMAND, PetActionCommandReceivedMessage.class);
        mReceivedMessageTypes.put(MESSAGE_TYPE_UPDATE_POSES, UpdatePosesReceivedMessage.class);
        mReceivedMessageTypes.put(MESSAGE_TYPE_REQUEST_STATUS, RequestStatusReceivedMessage.class);
    }

    private static class InstanceHolder {
        private static final IMessageService INSTANCE = new MessageService();
    }

    private MessageService() {
        EventBusUtils.register(this);
        this.mConnectionManager = PetConnectionManager.getInstance();
    }

    public static IMessageService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public int sharePetAnchor(@NonNull CloudAnchor petAnchor) {
        RequestMessage<CloudAnchor> request = new RequestMessage<>(MESSAGE_TYPE_PET_ANCHOR, petAnchor);
        request.setStatus(new RequestStatus(request.getId()));
        return sendRequest(request);
    }

    @Override
    public void sendViewCommand(@NonNull ViewCommand command) {
        sendRequest(new RequestMessage<>(MESSAGE_TYPE_VIEW_COMMAND, command));
    }

    @Override
    public void sendBallCommand(@NonNull BallCommand command) {
        sendRequest(new RequestMessage<>(MESSAGE_TYPE_BALL_COMMAND, command));
    }

    @Override
    public void sendPetActionCommand(@NonNull PetActionCommand command) {
        sendRequest(new RequestMessage<>(MESSAGE_TYPE_PET_ACTION_COMMAND, command));
    }

    @Override
    public void updatePoses(@NonNull SharedObjectPose[] poses) {
        sendRequest(new RequestMessage<>(MESSAGE_TYPE_UPDATE_POSES, poses));
    }

    @Override
    public void sendRequestStatus(@NonNull RequestStatus status) {
        sendRequest(new RequestMessage<>(MESSAGE_TYPE_REQUEST_STATUS, status));
    }

    private int sendRequest(RequestMessage request) {
        int id = request.getId();
        mConnectionManager.sendMessage(request,
                totalSent -> logForRequest(request, "Request sent: " + request));
        return id;
    }

    @Subscribe
    public void handleConnectionEvent(MessageReceivedEvent event) {
        handleRequestMessage((RequestMessage) event.getData());
    }

    @SuppressWarnings("unchecked")
    private void handleRequestMessage(RequestMessage request) {
        try {
            Class messageType = mReceivedMessageTypes.get(request.getActionName());
            Class dataType = request.getData().getClass();
            ReceivedMessage message = (ReceivedMessage) messageType.getConstructor(dataType).newInstance(request.getData());
            message.setRequestStatus(request.getStatus());
            EventBusUtils.post(message);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(TAG, "Error instantiating class for received message " + request, e);
        }
    }

    private void logForRequest(RequestMessage message, CharSequence text) {
        Log.d(TAG, String.format(Locale.getDefault(), "Request(%d): %s", message.getId(), text));
    }
}