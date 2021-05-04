package com.example.blackbox.model;

import android.bluetooth.BluetoothDevice;

/**
 * Created by tiziano on 3/8/19.
 */

public class MyBluetoothDevice {

    private String deviceName ="Unknown Device";
    private String macAddress;
    private boolean connected = false;
    private BluetoothDevice bluetoothDevice;


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}
