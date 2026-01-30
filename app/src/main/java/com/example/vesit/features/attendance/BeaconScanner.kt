package com.example.vesit.features.attendance.data

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.bluetooth.BluetoothManager
import kotlinx.coroutines.flow.MutableStateFlow

class BeaconScanner(context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val scanner = bluetoothManager.adapter.bluetoothLeScanner
    val isTeacherNearby = MutableStateFlow(false)

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Check if the signal is strong enough (Proximity Shield)
            if (result.rssi > -70) {
                isTeacherNearby.value = true
                stopScanning()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        scanner?.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        scanner?.stopScan(scanCallback)
    }
}