package com.example.bluetoothbasictest;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import com.android.code.DeviceListActivity;


public class BluetoothService {
	private static final String TAG = "BluetoothService";
	
	public static final int REQUEST_ENABLE_BT      = 36159355;
	public static final int REQUEST_CONNECT_DEVICE = 39635348; 
	
	private Activity _activity;
	private Handler _handler;
	
	private BluetoothAdapter _btAdapter;
	
	/*
	 * Constructor
	 */
	public BluetoothService(Activity act, Handler h) {
		_activity = act;
		_handler = h;
		
		_btAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	/*
	 * 스마트폰이 블루투스 통신을 지원하는지 여부 체크 (false인 기기는 블루투스 미지원기기;) 
	 */
	public boolean isBluetoothSupportDevice() {
		Log.i(TAG, "getDeviceState");
		
		if (_btAdapter == null) {
			Log.i(TAG, "Bluetooth is not availabe");
			
			return false;
		}else{
			Log.i(TAG, "Bluetooth is availabe");
			
			return true;
		}
	}
	
	/*
	 * 블루투스를 켜주는 메소드.
	 */
	public boolean enableBluetooth() {
		if (_btAdapter == null) return false;
		if (_btAdapter.enable()) return true;
		else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			_activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
			return false;
		}
	}
	
	/*
	 * 블루투스 기기를 검색하는 메소드.
	 */
	public void scanDevice() {
		Log.i(TAG, "scanDevice");
		
		Intent intent = new Intent(_activity, DeviceListActivity.class);
		_activity.startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
	}
	
	/*
	 * 전달받은 Intent데이터에서 주소를 받아서 device클래스 리턴.
	 */
	public BluetoothDevice getDeviceInfo(Intent data) {
		Log.i(TAG, "getDeviceInfo data:"+data);
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = _btAdapter.getRemoteDevice(address);
		return device;
	}
	
	public void connect(BluetoothDevice device) {
		Log.i(TAG, "connect");
		String address = device.getAddress();
		Log.i(TAG, "address:"+address);
		String name = device.getName();
		Log.i(TAG, "name:"+name);
		ParcelUuid[] uuids =  device.getUuids();
		for (ParcelUuid uuid : uuids) {
			UUID id = uuid.getUuid();
			Log.i(TAG, "id:"+id.toString());
		}
	}
	
	/*
	 * private methods
	 */
}
