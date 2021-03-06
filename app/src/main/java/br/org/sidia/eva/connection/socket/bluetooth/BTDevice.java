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

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

import br.org.sidia.eva.connection.BaseDevice;
import br.org.sidia.eva.connection.DeviceType;

import java.io.IOException;
import java.util.UUID;

public class BTDevice extends BaseDevice {

    private String name;
    private String address;
    private int type;

    private BluetoothDevice mDevice;

    public BTDevice(BluetoothDevice device) {

        this.mDevice = device;
        this.name = device.getName();
        this.address = device.getAddress();
        int majorType = device.getBluetoothClass().getMajorDeviceClass();

        switch (majorType) {
            case BluetoothClass.Device.Major.PHONE:
                this.type = DeviceType.PHONE;
            default:
                this.type = DeviceType.UNKNOWN;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BTDevice{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", type=" + type +
                "} " + super.toString();
    }

    public BTSocket createSocket(UUID uuid) throws IOException {
        return new BTSocket(mDevice.createInsecureRfcommSocketToServiceRecord(uuid));
    }
}
