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

package br.org.sidia.eva.manager.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import br.org.sidia.eva.PetContext;
import br.org.sidia.eva.connection.Connection;
import br.org.sidia.eva.connection.ManagerState;
import br.org.sidia.eva.connection.Message;
import br.org.sidia.eva.connection.SendMessageCallback;
import br.org.sidia.eva.connection.exception.ConnectionException;
import br.org.sidia.eva.connection.socket.bluetooth.BTConnectionManager;
import br.org.sidia.eva.connection.socket.bluetooth.BTDevice;
import br.org.sidia.eva.connection.socket.bluetooth.BTServerDeviceFinder;
import br.org.sidia.eva.constant.PetConstants;
import br.org.sidia.eva.context.ActivityResultEvent;
import br.org.sidia.eva.manager.connection.event.MessageReceivedEvent;
import br.org.sidia.eva.manager.connection.event.PetConnectionEvent;
import br.org.sidia.eva.util.EventBusUtils;

import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.util.Arrays;

public final class PetConnectionManager extends BTConnectionManager implements IPetConnectionManager {

    private static final int REQUEST_ENABLE_BT = 1000;
    private static final int REQUEST_ENABLE_HOST_VISIBILITY = 1001;

    private PetContext mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BTServerDeviceFinder mServerFinder;
    private OnEnableBluetoothCallback mEnableBTCallback;
    private OnEnableDiscoverableCallback mEnableVisibilityCallback;
    private DeviceVisibilityMonitor mDeviceVisibilityMonitor;
    private boolean mDisconnectSilently;

    private static volatile PetConnectionManager sInstance;

    public static IPetConnectionManager getInstance() {
        if (sInstance == null) {
            synchronized (IPetConnectionManager.class) {
                if (sInstance == null) {
                    sInstance = new PetConnectionManager();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void init(@NonNull PetContext context) {
        if (mContext == null) {
            mContext = context;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mServerFinder = new BTServerDeviceFinder(mContext.getActivity());
            mDeviceVisibilityMonitor = new DeviceVisibilityMonitor(context, this::onVisibilitySateChanged);
        }
    }

    @Override
    public void startInvitation() {
        checkInitialization();
        if (stateIs(ManagerState.IDLE)) {
            Log.d(TAG, "Request enable BT");
            enableBluetooth(() -> {
                Log.d(TAG, "OK, BT enabled. Request device visible");
                enableHostVisibility(() -> {
                    Log.d(TAG, "OK, now this device is visible for "
                            + PetConstants.HOST_VISIBILITY_DURATION + " seconds. Waiting for connections");
                    mDeviceVisibilityMonitor.setEnabled(true);
                    notifyManagerEvent(PetConnectionManager.EVENT_ON_LISTENING_TO_GUESTS);
                    super.startConnectionListener(this::onMessageReceived);
                });
            });
        }
    }

    @Override
    public void stopInvitation() {
        checkInitialization();
        mDeviceVisibilityMonitor.setEnabled(false);
        this.stopConnectionListener();
    }

    @Override
    public void stopInvitationAndDisconnect() {
        checkInitialization();
        mDeviceVisibilityMonitor.setEnabled(false);
        super.stopConnectionListener();
        disconnect();
    }

    @Override
    public void findInvitationThenConnect() {
        checkInitialization();
        if (stateIs(ManagerState.IDLE)) {
            enableBluetooth(() -> {
                Log.d(TAG, "Finding a server device...");
                // Broadcast all devices found and saves the first successfully connection
                notifyManagerEvent(EVENT_ON_REQUEST_CONNECTION_TO_HOST);
                mServerFinder.find(this::onServersFound);
            });
        }
    }

    @Override
    public void stopFindInvitationAndDisconnect() {
        checkInitialization();
        mServerFinder.cancel();
        disconnect();
    }

    @Override
    public int getConnectionMode() {
        checkInitialization();
        return super.getConnectionMode();
    }

    @Override
    public int getTotalConnected() {
        checkInitialization();
        return super.getTotalConnected();
    }

    @Override
    public void sendMessage(Message message, @NonNull SendMessageCallback callback) {
        checkInitialization();
        super.sendMessage(message, callback);
    }

    private void checkInitialization() {
        if (mContext == null) {
            throw new IllegalStateException("The manager must be initialized calling init() method");
        }
    }

    private void onVisibilitySateChanged(@DeviceVisibilityMonitor.State int state) {
        switch (state) {
            case DeviceVisibilityMonitor.VISIBILITY_OFF:
                Log.d(TAG, "Host visibility expired");
                enableHostVisibility(() -> Log.d(TAG, "Host visibility allowed again"));
                break;
            case DeviceVisibilityMonitor.VISIBILITY_ON:
                Log.d(TAG, "Host visibility ON");
                break;
            default:
                break;
        }
    }

    /**
     * Handle devices found by {@link BTServerDeviceFinder}.
     *
     * @param servers Servers found.
     */
    private void onServersFound(BTDevice[] servers) {
        Log.d(TAG, "Servers found = " + servers.length);
        if (servers.length > 0) {
            Log.d(TAG, "Trying connect to servers " + Arrays.toString(servers));
            connectToDevices(this::onMessageReceived, servers);
        } else {
            notifyManagerEvent(EVENT_NO_CONNECTION_FOUND);
        }
    }

    /**
     * Handle message received form remote device.
     *
     * @param message Message form remote device.
     */
    private void onMessageReceived(Message message) {
        Log.d(TAG, "onMessageReceived: " + message);
        notifyManagerEvent(EVENT_MESSAGE_RECEIVED, message);
    }

    @Override
    public void onConnectionEstablished(Connection connection) {
        Log.d(TAG, "onConnectionEstablished: " + connection.getRemoteDevice());
        // Stop trying connection when first connection is successful
        if (stateIs(ManagerState.CONNECTING_TO_REMOTE)) {
            super.onConnectionEstablished(connection);
            cancelOutgoingConnectionsThreads();
            notifyManagerEvent(EVENT_CONNECTION_ESTABLISHED);
        } else {
            super.onConnectionEstablished(connection);
            notifyManagerEvent(EVENT_GUEST_CONNECTION_ESTABLISHED,
                    connection.getRemoteDevice());
        }
    }

    @Override
    public void onConnectionFailure(ConnectionException error) {
        Log.d(TAG, "onConnectionFailure: " + error.getMessage());
        if (stateIs(ManagerState.CONNECTING_TO_REMOTE)) {
            super.onConnectionFailure(error);
            if (stateIs(ManagerState.IDLE)) {
                Log.d(TAG, "onConnectionFailure: No connection found.");
                notifyManagerEvent(EVENT_NO_CONNECTION_FOUND);
            }
        } else {
            super.onConnectionFailure(error);
        }
    }

    @Override
    public void onConnectionLost(Connection connection, ConnectionException error) {
        super.onConnectionLost(connection, error);
        Log.d(TAG, "onConnectionLost: " + connection.getRemoteDevice());
        if (!mDisconnectSilently) {
            notifyManagerEvent(EVENT_ONE_CONNECTION_LOST, connection.getRemoteDevice());
            if (getTotalConnected() == 0) {
                notifyManagerEvent(EVENT_ALL_CONNECTIONS_LOST);
            }
        }
        if (getTotalConnected() == 0) {
            mDisconnectSilently = false;
        }
    }

    @Override
    public void stopConnectionListener() {
        if (stateIs(ManagerState.LISTENING_TO_CONNECTIONS)) {
            Log.d(TAG, "stopConnectionListener: force stop connection listener");
            super.stopConnectionListener();
            if (getTotalConnected() > 0) {
                Log.d(TAG, "stopConnectionListener: total connected = " + getTotalConnected());
                notifyManagerEvent(EVENT_CONNECTION_ESTABLISHED);
            } else {
                notifyManagerEvent(EVENT_NO_CONNECTION_FOUND);
            }
        }
    }

    @Override
    public void disconnect() {
        checkInitialization();
        mDisconnectSilently = true;
        super.disconnect();
    }

    @Override
    public PetContext getContext() {
        return mContext;
    }

    @Subscribe
    public void handleContextEvent(ActivityResultEvent event) {
        EventBusUtils.unregister(this);
        if (event.getRequestCode() == REQUEST_ENABLE_BT) {
            if (event.getResultCode() == Activity.RESULT_OK) {
                mEnableBTCallback.onEnabled();
            } else {
                notifyManagerEvent(EVENT_ENABLE_BLUETOOTH_DENIED);
            }
        } else if (event.getRequestCode() == REQUEST_ENABLE_HOST_VISIBILITY) {
            if (event.getResultCode() != Activity.RESULT_CANCELED) {
                mEnableVisibilityCallback.onEnabled();
            } else {
                Log.d(TAG, "Host visibility denied by user");
                notifyManagerEvent(EVENT_HOST_VISIBILITY_DENIED);
            }
        }
    }

    private void notifyManagerEvent(@EventType int type) {
        notifyManagerEvent(type, null);
    }

    private void notifyManagerEvent(@EventType int type, Serializable data) {
        if (type == IPetConnectionManager.EVENT_MESSAGE_RECEIVED) {
            EventBusUtils.post(new MessageReceivedEvent(data));
        } else {
            EventBusUtils.post(new PetConnectionEvent(type, data));
        }
    }

    private void enableBluetooth(OnEnableBluetoothCallback callback) {
        mEnableBTCallback = callback;
        if (!mBluetoothAdapter.isEnabled()) {
            EventBusUtils.register(this);
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.getActivity().startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            EventBusUtils.unregister(this);
            callback.onEnabled();
        }
    }

    private void enableHostVisibility(OnEnableDiscoverableCallback callback) {
        mEnableVisibilityCallback = callback;
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            EventBusUtils.register(this);
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, PetConstants.HOST_VISIBILITY_DURATION);
            mContext.getActivity().startActivityForResult(discoverableIntent, REQUEST_ENABLE_HOST_VISIBILITY);
        } else {
            EventBusUtils.unregister(this);
            mEnableVisibilityCallback.onEnabled();
        }
    }

    @FunctionalInterface
    private interface OnEnableBluetoothCallback {
        void onEnabled();
    }

    @FunctionalInterface
    private interface OnEnableDiscoverableCallback {
        void onEnabled();
    }
}

