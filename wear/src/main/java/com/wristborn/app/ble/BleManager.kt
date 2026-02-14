package com.wristborn.app.ble

import android.annotation.SuppressLint
import android.bluetooth.*
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
    var discoveredDevice: BluetoothDevice? = null
        private set

    private val serviceUuid = UUID.fromString("0000fb01-0000-1000-8000-00805f9b34fb")
    private val charUuid = UUID.fromString("0000fb02-0000-1000-8000-00805f9b34fb")

    private var gattServer: BluetoothGattServer? = null
    private var gattClient: BluetoothGatt? = null
    private var eventCallback: ((DuelEvent) -> Unit)? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name ?: "Unknown Duelist"
            if (nearbyDuelistFound == null) {
                nearbyDuelistFound = deviceName
                discoveredDevice = result.device
                Log.d("BleManager", "Found duelist: $deviceName at ${result.device.address}")
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

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (characteristic?.uuid == charUuid && value != null) {
                val event = decodeEvent(value)
                eventCallback?.invoke(event)
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d("BleManager", "Services discovered")
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (adapter == null || !adapter.isEnabled) return
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner?.startScan(listOf(filter), settings, scanCallback)

        val advSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()
        val advData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(serviceUuid))
            .build()
        advertiser?.startAdvertising(advSettings, advData, advertiseCallback)

        setupGattServer()
    }

    @SuppressLint("MissingPermission")
    private fun setupGattServer() {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(serviceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val char = BluetoothGattCharacteristic(charUuid, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        service.addCharacteristic(char)
        gattServer?.addService(service)
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        scanner?.stopScan(scanCallback)
        advertiser?.stopAdvertising(advertiseCallback)
        nearbyDuelistFound = null
        discoveredDevice = null
    }

    @SuppressLint("MissingPermission")
    fun connectToDuelist(device: BluetoothDevice, onEvent: (DuelEvent) -> Unit) {
        eventCallback = onEvent
        gattClient = device.connectGatt(context, false, gattClientCallback)
    }

    @SuppressLint("MissingPermission")
    fun sendEvent(event: DuelEvent) {
        val gatt = gattClient ?: return
        val service = gatt.getService(serviceUuid) ?: return
        val char = service.getCharacteristic(charUuid) ?: return
        char.value = encodeEvent(event)
        gatt.writeCharacteristic(char)
    }

    private fun encodeEvent(event: DuelEvent): ByteArray {
        // Simple encoding for MVP: type(1) + damage(4) + hash(4)
        val buffer = java.nio.ByteBuffer.allocate(9)
        buffer.put(event.type.ordinal.toByte())
        buffer.putInt(event.damage)
        buffer.putInt(event.spellHash)
        return buffer.array()
    }

    private fun decodeEvent(data: ByteArray): DuelEvent {
        val buffer = java.nio.ByteBuffer.wrap(data)
        val typeIdx = buffer.get().toInt()
        val damage = buffer.getInt()
        val hash = buffer.getInt()
        return DuelEvent(EventType.entries[typeIdx], 0L, hash, damage)
    }

    fun clearDuelist() {
        nearbyDuelistFound = null
        discoveredDevice = null
    }

    @SuppressLint("MissingPermission")
    fun close() {
        gattClient?.close()
        gattServer?.close()
    }
}
