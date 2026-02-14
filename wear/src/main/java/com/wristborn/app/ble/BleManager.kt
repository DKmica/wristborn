package com.wristborn.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.*

class BleManager(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? by lazy { adapter?.bluetoothLeScanner }
    private val advertiser: BluetoothLeAdvertiser? by lazy { adapter?.bluetoothLeAdvertiser }

    var nearbyDuelistFound by mutableStateOf<String?>(null)
        private set

    private val serviceUuid = ParcelUuid.fromString("0000fb01-0000-1000-8000-00805f9b34fb") // Example UUID

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name ?: "Unknown Duelist"
            if (nearbyDuelistFound == null) {
                nearbyDuelistFound = deviceName
                Log.d("BleManager", "Found duelist: $deviceName")
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d("BleManager", "Advertising started")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BleManager", "Advertising failed: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (adapter == null || !adapter.isEnabled) return

        // Start Scanning
        val filter = ScanFilter.Builder().setServiceUuid(serviceUuid).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner?.startScan(listOf(filter), settings, scanCallback)

        // Start Advertising
        val advSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()
        val advData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(serviceUuid)
            .build()
        advertiser?.startAdvertising(advSettings, advData, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        scanner?.stopScan(scanCallback)
        advertiser?.stopAdvertising(advertiseCallback)
        nearbyDuelistFound = null
    }
    
    fun clearDuelist() {
        nearbyDuelistFound = null
    }
}
