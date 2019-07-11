package me.djnwzs.bysj

import android.bluetooth.*
import android.util.Log
import android.widget.Toast
import me.djnwzs.bysj.MainActivity.Companion

object GattCallback {
    private val TAG = ""
    private val characterUUID1 = "0000ff01-0000-1000-8000-00805f9b34fb"//APP发送命令
    private val characterUUID2 = "0000ff02-0000-1000-8000-00805f9b34fb"//BLE用于回复命令


    var mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        }

        override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
        }

        //当连接状态发生改变
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.e("GATT", "onConnectionStateChange")
            val str = gatt.device.name + ": " + gatt.device.address
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt.discoverServices() //搜索连接设备所支持的service
                    //setTextViewState(str+" 已连接");
                    Log.e("BluetoothState", "STATE_CONNECTED")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (Companion.mBluetoothGatt != null) Companion.mBluetoothGatt!!.disconnect()
                    //setTextViewState(str+" 已断开");
                    Log.e("BluetoothState", "STATE_DISCONNECTED")
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    Companion.setTextViewState("$str 正在连接")
                    Log.e("BluetoothState", "STATE_CONNECTING")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Companion.setTextViewState("$str 正在断开")
                    Companion.mBluetoothGatt!!.close()
                    Log.e("BluetoothState", "STATE_DISCONNECTING")
                }
            }
            super.onConnectionStateChange(gatt, status, newState)
        }

        //发现新服务，即调用了mBluetoothGatt.discoverServices()后，返回的数据
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Companion.serviceList = gatt.services
                for (i in 0 until Companion.serviceList!!.size) {
                    val theService = Companion.serviceList!!.get(i)
                    Log.e("ServiceName", theService.getUuid().toString())
                    Companion.characterList = theService.getCharacteristics()
                    for (j in 0 until Companion.characterList!!.size) {
                        val uuid = Companion.characterList!!.get(j).getUuid().toString()
                        Log.e("CharacterName", uuid)
                        if (uuid == characterUUID1) {
                            Companion.mCharacteristic = Companion.characterList!!.get(j)
                        } else if (uuid == characterUUID2) {
                            Companion.mCharacteristic2 = Companion.characterList!!.get(j)
                        }
                    }
                }
                Log.e("Character2Name:", Companion.mCharacteristic2!!.getUuid().toString())
                val b = Companion.mBluetoothGatt!!.setCharacteristicNotification(Companion.mCharacteristic2, true)
                if (b) {
                    val descriptors = Companion.mCharacteristic2!!.getDescriptors()
                    for (descriptor in descriptors) {
                        val b1 = descriptor.setValue(
                                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
                        if (b1) {
                            Companion.mBluetoothGatt!!.writeDescriptor(descriptor)
                            Log.d("startRead: ", "监听收数据")
                        }
                    }
                }

            }
            super.onServicesDiscovered(gatt, status)
        }

        //调用mBluetoothGatt.readCharacteristic(characteristic)读取数据回调，在这里面接收数据
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            characteristic.value
            Log.e(String(characteristic.value), "onCharacteristicRead: ")
            Toast.makeText(Companion.mactivity, String(characteristic.value), Toast.LENGTH_SHORT).show()
        }

        //发送数据后的回调
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value
            Log.e(TAG, "onCharacteristicChanged: $value")
            val s0 = Integer.toHexString(value[0].toInt())
            val s = Integer.toHexString(value[1].toInt())
            Log.e(TAG, "onCharacteristicChanged: $s0、$s")
            for (b in value) {
                Log.e(TAG, "onCharacteristicChanged: $b")
            }
            super.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor,
                                      status: Int) {//descriptor读
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor,
                                       status: Int) {//descriptor写
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.e("onDescriptorWrite: ", "设置成功")
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
        }

        //调用mBluetoothGatt.readRemoteRssi()时的回调，rssi即信号强度
        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {//读Rssi
            super.onReadRemoteRssi(gatt, rssi, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
        }
    }

}
