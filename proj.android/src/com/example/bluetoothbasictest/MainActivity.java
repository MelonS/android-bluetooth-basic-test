package com.example.bluetoothbasictest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity";
	
	private BluetoothService _btService = null;
	
	private final Handler _handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
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
			if (_btService.enableBluetooth()) {
				_btService.scanDevice();
			}
		}
			break;
		}
	}
}
