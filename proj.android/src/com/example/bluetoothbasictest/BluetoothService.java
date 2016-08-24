package com.example.bluetoothbasictest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.android.code.DeviceListActivity;


public class BluetoothService {
	private static final String TAG = "BluetoothService";

	public static final int REQUEST_ENABLE_BT      = 36159355;
	public static final int REQUEST_CONNECT_DEVICE = 39635348; 

	// Serial Port Profile
	private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

	// Human Interface Device Profile
	private static final UUID UUID_HID = UUID.fromString("00001124-0000-1000-8000-00805f9b34fb");

	private Activity _activity = null;
	@SuppressWarnings("unused")
	private Handler _handler = null;

	private BluetoothAdapter _btAdapter = null;

	private BluetoothDevice _btDevice = null;
	private BluetoothSocket _btSocket = null;

	private boolean _isConnected = false;

	private interface ConnectListener {
		void OnConnected(boolean isSuccess);
	}

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

	/*
	 * 블루투스 기기와 접속시에 사용.
	 */
	public void connect(BluetoothDevice device) {
		Log.i(TAG, "connect");

		PrintDeviceInformation(device);

		if (_btAdapter.isDiscovering()) {
			_btAdapter.cancelDiscovery();
		}

		//requestConnect(device, UUID_HID, new ConnectListener() {
		requestConnect(device, UUID_SPP, new ConnectListener() {
			@Override
			public void OnConnected(boolean isSuccess) {
				if (isSuccess) {
					Log.i(TAG, "Is Connected~");
					new Thread(new Runnable() {
						@Override
						public void run() {
							ConnectedThread thread = new ConnectedThread(_btSocket);
							thread.start();
						}
					}).start();
				}else{
					Log.i(TAG, "Not Connected!!!");
				}
			}
		});

	}

	public void acceptStart() {
		Log.i(TAG, "acceptStart");
		
		AcceptThread t = new AcceptThread();
		t.start();
	}


	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;


		//	      private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket)
		{
			mmSocket = socket;
			InputStream tmpIn = null;
			//	         OutputStream tmpOut = null;
			// Get the input and output streams, using temp objects because member streams are final
			try
			{
				tmpIn = socket.getInputStream();
				//	            tmpOut = socket.getOutputStream();
			}
			catch (IOException e)
			{
			}
			mmInStream = tmpIn;
			//	         mmOutStream = tmpOut;
		}


		public void run()
		{
			byte[] readBuffer = new byte[1024];
			int readBufferPosition = 0;
			byte delimiter = 0x00;
			// Keep listening to the InputStream until an exception occurs

			while (true)
			{
				try { 
					//Log.i(TAG, "RUN=====");
					int bytesAvailable = mmInStream.available();    // 수신 데이터 확인
					if(bytesAvailable > 0) {                     // 데이터가 수신된 경우
						Log.i(TAG, "AVAILABLE");
						byte[] packetBytes = new byte[bytesAvailable];
						mmInStream.read(packetBytes);
						for(int i=0 ; i<bytesAvailable; i++) {
							byte b = packetBytes[i];
							if(b == delimiter) {
								byte[] encodedBytes = new byte[readBufferPosition];
								System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
								final String data = new String(encodedBytes, "US-ASCII");
								readBufferPosition = 0;

								_handler.post(new Runnable() {
									public void run() {
										// 수신된 문자열 데이터에 대한 처리 작업
										Log.i(TAG, "h post=================");
									}
								});
							}else {
								readBuffer[readBufferPosition++] = b;
							}
						}
					}
				}
				catch (IOException ex) {
					// 데이터 수신 중 오류 발생. 

				}
			}
		}


		private String bytes2String(byte[] b, int count)
		{
			ArrayList<String> result = new ArrayList<String>();
			for (int i = 0; i < count; i++)
			{
				String myInt = Integer.toHexString((int) (b[i] & 0xFF));
				result.add("0x" + myInt);
			}
			return TextUtils.join("-", result);
		}


		/* Call this from the main Activity to send data to the remote device */
		//	      public void write(byte[] bytes)
		//	      {
		//	         try
		//	         {
		//	            mmOutStream.write(bytes);
		//	         }
		//	         catch (IOException e)
		//	         {
		//	         }
		//	      }

		/* Call this from the main Activity to shutdown the connection */
		public void cancel()
		{
			try
			{
				mmSocket.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private class AcceptThread extends Thread {
		
		private final BluetoothServerSocket _serverSocket;
		
		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			
			try {
				tmp = _btAdapter.listenUsingRfcommWithServiceRecord("testapp", UUID_SPP);
			} catch (IOException e) {
				Log.i(TAG, "AcceptThread Create Failed");
			}
			
			_serverSocket = tmp;
		}

		@Override
		public void run() {
			super.run();
			
			Log.i(TAG, "AcceptThread::run()");
			
			if (!_isConnected) {
				while (!_isConnected) {
					BluetoothSocket socket = null;
					
					try {
						Log.i(TAG, "AcceptThread READY!!");
						socket = _serverSocket.accept();
						_isConnected = true;
						Log.i(TAG, "Is Accepted!!!");
						
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			}else{
				Log.i(TAG, "AcceptThread run Failed IsConnected!!!");
			}
		}
	}

	/*
	 * private methods
	 */
	@SuppressLint("NewApi")
	private void PrintDeviceInformation(BluetoothDevice device) {
		Log.i(TAG, "===PrintDeviceInformation===");

		String address = device.getAddress();
		Log.i(TAG, "address:"+address);
		String name = device.getName();
		Log.i(TAG, "name:"+name);
		ParcelUuid[] uuids =  device.getUuids();
		if (uuids != null) {
			for (ParcelUuid uuid : uuids) {
				UUID id = uuid.getUuid();
				Log.i(TAG, "id:"+id.toString());
			}
		}

		if (Build.VERSION.SDK_INT >= 18){
			int type = device.getType();
			switch (type) {
			case BluetoothDevice.DEVICE_TYPE_CLASSIC:
				Log.i(TAG, "BluetoothDevice.DEVICE_TYPE_CLASSIC");
				break;
			case BluetoothDevice.DEVICE_TYPE_LE:
				Log.i(TAG, "BluetoothDevice.DEVICE_TYPE_LE");
				break;
			case BluetoothDevice.DEVICE_TYPE_DUAL:
				Log.i(TAG, "BluetoothDevice.DEVICE_TYPE_DUAL");
				break;
			case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
				Log.i(TAG, "BluetoothDevice.DEVICE_TYPE_UNKNOWN");
				break;
			}
		}

		int bondState = device.getBondState();
		switch (bondState) {
		case BluetoothDevice.BOND_BONDED:
			Log.i(TAG, "BluetoothDevice.BOND_BONDED");
			break;
		case BluetoothDevice.BOND_BONDING:
			Log.i(TAG, "BluetoothDevice.BOND_BONDING");
			break;
		case BluetoothDevice.BOND_NONE:
			Log.i(TAG, "BluetoothDevice.BOND_NONE");
			break;
		}
	}

	private BluetoothSocket getBluetoothSocket(BluetoothDevice device, UUID uuid) throws IOException {
		if (device == null) return null;
		return device.createInsecureRfcommSocketToServiceRecord(uuid);
		//return device.createRfcommSocketToServiceRecord(uuid);
	}

	private void requestConnect(BluetoothDevice device, UUID uuid, final ConnectListener listener) {
		Log.i(TAG, "requestConnect");
		try {
			Log.i(TAG, "requestConnect - 1");
			_btSocket = getBluetoothSocket(device, uuid);
			Log.i(TAG, "requestConnect - 2");
			if (_btSocket != null) {
				Log.i(TAG, "requestConnect - 3");
				new Thread(new Runnable() {
					@Override
					public void run() {
						Log.i(TAG, "requestConnect - 4");
						try {
							Log.i(TAG, "requestConnect - 5");
							_btSocket.connect();
							Log.i(TAG, "requestConnect - Success");
							_isConnected = true;
							listener.OnConnected(true);
							return;
						} catch (IOException e) {
							Log.i(TAG, "requestConnect - Error 1");
							e.printStackTrace();
							_isConnected = false;
							listener.OnConnected(false);
							return;
						}
					}
				}).start();
			}else{
				Log.i(TAG, "requestConnect - Error 2");
				_isConnected = false;
				listener.OnConnected(false);
				return;
			}

		} catch (IOException e) {
			Log.i(TAG, "requestConnect - Error 3");
			e.printStackTrace();
			_isConnected = false;
			listener.OnConnected(false);
			return;
		}
	}


}
