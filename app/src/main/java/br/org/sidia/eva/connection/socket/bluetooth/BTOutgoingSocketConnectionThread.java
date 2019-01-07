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

package br.org.sidia.eva.connection.socket.bluetooth;

import android.support.annotation.NonNull;

import br.org.sidia.eva.connection.Connection;
import br.org.sidia.eva.connection.Device;
import br.org.sidia.eva.connection.OnConnectionListener;
import br.org.sidia.eva.connection.OnMessageListener;
import br.org.sidia.eva.connection.socket.OutgoingSocketConnectionThread;

import java.io.IOException;

public class BTOutgoingSocketConnectionThread extends OutgoingSocketConnectionThread<BTSocket> {

    private OnMessageListener mMessageListener;

    BTOutgoingSocketConnectionThread(
            @NonNull BTDevice device,
            @NonNull OnMessageListener messageListener,
            @NonNull OnConnectionListener connectionListener) {

        super(device, connectionListener);
        this.mMessageListener = messageListener;
    }

    @Override
    protected BTSocket createSocket(Device device) throws IOException {
        return ((BTDevice) device).createSocket(BTConstants.SOCKET_SERVER_UUID);
    }

    @Override
    protected Connection createConnection(BTSocket socket) {
        return new BTOngoingSocketConnectionThread(socket, mMessageListener, mOnConnectionListener);
    }
}
