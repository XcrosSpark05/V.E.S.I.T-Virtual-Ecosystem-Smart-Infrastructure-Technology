package com.example.vesit.features.teacher.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import java.util.*

class BeaconBroadcaster {
    private val advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner?.let {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
    }

    // Unique UUID for VESIT MCA Sem 1
    private val serviceUuid = ParcelUuid(UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb"))

    fun startBroadcasting(callback: AdvertiseCallback) {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) // Fast detection
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // Classroom range
            .setConnectable(false)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(serviceUuid)
            .build()

        advertiser?.startAdvertising(settings, data, callback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun stopBroadcasting(callback: AdvertiseCallback) {
        advertiser?.stopAdvertising(callback)
    }
}