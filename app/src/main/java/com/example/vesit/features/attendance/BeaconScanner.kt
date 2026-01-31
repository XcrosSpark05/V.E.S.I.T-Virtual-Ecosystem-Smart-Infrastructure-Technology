package com.example.vesit.features.attendance.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class BeaconScanner(context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    val isTeacherNearby = MutableStateFlow(false)

    // The identical UUID used in the Teacher's Broadcaster
    private val targetServiceUuid = ParcelUuid(UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb"))

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val serviceUuids = result.scanRecord?.serviceUuids
            if (serviceUuids != null && serviceUuids.contains(targetServiceUuid)) {
                isTeacherNearby.value = true // Teacher detected!
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        isTeacherNearby.value = false
        val filter = ScanFilter.Builder().setServiceUuid(targetServiceUuid).build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(listOf(filter), settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        scanner?.stopScan(scanCallback)
    }
}