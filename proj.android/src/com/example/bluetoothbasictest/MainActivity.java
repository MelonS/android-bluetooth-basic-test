package com.example.bluetoothbasictest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity";

	/*
	private TextView _tx1 = null;
	private TextView _tx2 = null;
	
	private Button _btn1 = null;
	
	private BluetoothSPP _bt = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		_tx1 = (TextView)findViewById(R.id.tx1);
		_tx2 = (TextView)findViewById(R.id.tx2);
		_btn1 = (Button)findViewById(R.id.btn1);
		_btn1.setOnClickListener(this);
		
		_bt = new BluetoothSPP(this);
		
		if (!_bt.isBluetoothAvailable()) {
			Toast.makeText(getApplicationContext(), "Bluetooth is not available!!!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		_bt.setOnDataReceivedListener(new OnDataReceivedListener() {
			@Override
			public void onDataReceived(byte[] data, String message) {
				Log.i(TAG, "data:"+data.toString());
				Log.i(TAG, "message:"+message);
				_tx2.append(message + "\n");
			}
		});
		
		_bt.setBluetoothStateListener(new BluetoothStateListener() {
			@Override
			public void onServiceStateChanged(int state) {
				if (state == BluetoothState.STATE_CONNECTED) {
					Log.i(TAG, "BluetoothState.STATE_CONNECTED");
				}else if (state == BluetoothState.STATE_CONNECTING) {
					Log.i(TAG, "BluetoothState.STATE_CONNECTING");
				}else if (state == BluetoothState.STATE_LISTEN) {
					Log.i(TAG, "BluetoothState.STATE_LISTEN");
				}else if (state == BluetoothState.STATE_NONE) {
					Log.i(TAG, "BluetoothState.STATE_NONE");
				}
			}
		});
		
		_bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
			@Override
			public void onDeviceDisconnected() {
				_tx1.setText("onDeviceDisconnected");
			}
			@Override
			public void onDeviceConnectionFailed() {
				_tx1.setText("onDeviceConnectionFailed");
			}
			@Override
			public void onDeviceConnected(String name, String address) {
				_tx1.setText("connect name:"+name+" address:"+address);
			}
		});
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();
		
		if (!_bt.isBluetoothEnabled()) {
			Log.i(TAG, "onStart Send Request");
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
		}else{
			Log.i(TAG, "onStart isEnabled");
			if (!_bt.isServiceAvailable()) {
				_bt.setupService();
				_bt.startService(BluetoothState.DEVICE_OTHER);
				//_bt.startService(BluetoothState.DEVICE_ANDROID);
				_bt.autoConnect("Mojing4-A");
				Log.i(TAG, "onStart startService");
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		_bt.stopService();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			Log.i(TAG, "onActivityResult BluetoothState.REQUEST_CONNECT_DEVICE");
			if (resultCode == Activity.RESULT_OK) {
				_bt.connect(data);
				Log.i(TAG, "onActivityResult connect");
			}
		} else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
			Log.i(TAG, "onActivityResult BluetoothState.REQUEST_ENABLE_BT");
			if (resultCode == Activity.RESULT_OK) {
				_bt.setupService();
				_bt.startService(BluetoothState.DEVICE_OTHER);
				//_bt.startService(BluetoothState.DEVICE_ANDROID);
				Log.i(TAG, "onActivityResult startService");
			} else {
				Toast.makeText(getApplicationContext(), "Bluetooth was not enabled.", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn1:
		{
			Log.i(TAG, "btn1 CLICK");
			if (_bt != null) {
				_bt.send("Test", true);
			}
		}
		}
	}
	*/
	
	
	private BluetoothService _btService = null;
	
	@SuppressLint("HandlerLeak")
	private final Handler _handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.i(TAG, "MainActivity::handleMessage msg:"+msg);
		}
	};
	
	Button _btn1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (_btService == null) {
			_btService = new BluetoothService(this, _handler);
		}
		
		if (!_btService.isBluetoothSupportDevice()) {
			Toast.makeText(getApplicationContext(), "스마트폰이 블루투스를 지원하지 않습니.", Toast.LENGTH_LONG).show();
		}
		
		_btn1 = (Button)findViewById(R.id.btn1);
		_btn1.setOnClickListener(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.i(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode+" data:"+data);
		
		switch (requestCode) {
		case BluetoothService.REQUEST_ENABLE_BT:
		{
			if (resultCode == Activity.RESULT_OK) {
				Log.i(TAG, "REQUEST_ENABLE_BT => Activity.RESULT_OK");
			}else{
				Log.i(TAG, "REQUEST_ENABLE_BT => Activity.RESULT_CANCELED");
			}
		}
			break;
		case BluetoothService.REQUEST_CONNECT_DEVICE:
		{
			if (resultCode == Activity.RESULT_OK) {
				Log.i(TAG, "REQUEST_CONNECT_DEVICE => Activity.RESULT_OK");
				BluetoothDevice device = _btService.getDeviceInfo(data);
				if (device != null) {
					_btService.connect(device);
				}
			}
		}
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btn1:
		{
			Log.i(TAG, "btn1 CLICK");
//			if (_btService.enableBluetooth()) {
//				_btService.scanDevice();
//			}
			
//			if (_btService.enableBluetooth()) {
//				_btService.acceptStart();
//			}
		}
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		Log.i(TAG, "onKeyDown keyCode:"+keyCode+" event:"+event);
		
		return super.onKeyDown(keyCode, event);
	}
	
	
	
}
