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

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;

import br.org.sidia.eva.connection.BaseDevice;
import br.org.sidia.eva.connection.Device;
import br.org.sidia.eva.connection.DeviceType;

public final class LocalBluetoothDevice {

    private static Device mDevice;

    private LocalBluetoothDevice() {
    }

    public static Device getDefault() {
        if (mDevice == null) {
            mDevice = new LocalBluetoothDevice().createDefault();
        }
        return mDevice;
    }

    private Device createDefault() {

        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();

        return new BaseDevice() {

            private String name = defaultAdapter.getName();
            @SuppressLint("HardwareIds")
            private String address = defaultAdapter.getAddress();

            @Override
            public String getName() {
                return name;
            }

            @SuppressLint("HardwareIds")
            @Override
            public String getAddress() {
                return address;
            }

            @Override
            public int getType() {
                return DeviceType.PHONE;
            }

            @Override
            public String toString() {
                return "Device{" +
                        "name='" + name + '\'' +
                        ", address='" + address + '\'' +
                        ", type=" + getType() +
                        "}";
            }
        };
    }
}
