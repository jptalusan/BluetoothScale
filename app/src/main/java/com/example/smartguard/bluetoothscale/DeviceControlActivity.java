package com.example.smartguard.bluetoothscale;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by talusan on 7/22/2016.
 */
public class DeviceControlActivity extends AppCompatActivity {
	private final static String TAG = DeviceControlActivity.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	private String mDeviceName;
	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
			new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	private TextView weightTV;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				Log.d(TAG, "ACTION_GATT_CONNECTED");
				mConnected = true;
				//updateConnectionState("connected");
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Log.d(TAG, "ACTION_GATT_DISCONNECTED");
				mConnected = false;
				//updateConnectionState("disconnected");
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
				Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				Log.d(TAG, "ACTION_DATA_AVAILABLE");
				//displayData();
				final byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				StringBuilder stringBuilder = new StringBuilder("");
				for (int i = 0; i < value.length; ++i) {
					int v = value[i] & 0xFF;
					String hv = Integer.toHexString(v);
					if (2 > hv.length())
						stringBuilder.append(0);
					stringBuilder.append(hv);
				}
				final String result = stringBuilder.toString();
				ParsingUtils pUtils = new ParsingUtils();
				String weight = pUtils.parse(result);
//				Toast.makeText(getApplicationContext(), weight, Toast.LENGTH_SHORT).show();
				weightTV.setText(weight);
				Log.d(TAG, "Weight: " + weight);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);

		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		weightTV = (TextView) findViewById(R.id.weight);

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//mConnectionState.setText(resourceId);
			}
		});
	}


	private void displayData(String data) {
		if (data != null) {
			//mDataField.setText(data);
		}
	}

	// Demonstrates how to iterate through the supported GATT Services/Characteristics.
// In this sample, we populate the data structure that is bound to the ExpandableListView
// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null) return;
		String uuid = null;
		String unknownServiceString = "unknown service";
		String unknownCharaString = "unknown char";
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
				= new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			currentServiceData.put(
					LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
					new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics =
					gattService.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas =
					new ArrayList<BluetoothGattCharacteristic>();

			Log.d(TAG, "Service Name: " + SampleGattAttributes.lookup(uuid, uuid));

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
				Log.d(TAG, "\tCharacteristic Name: " + SampleGattAttributes.lookup(uuid, uuid) +
						", value: " + gattCharacteristic.getValue());
				currentCharaData.put(
						LIST_NAME, SampleGattAttributes.lookup(uuid, uuid));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);
			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
		}
		readDataFromBlueToothDevice();
	}

	//Calls Read charas
	private void readDataFromBlueToothDevice() {
		Log.d(TAG, "readDataFromBlueToothDevice");
		if (mGattCharacteristics != null) {
			final BluetoothGattCharacteristic characteristic =
					mGattCharacteristics.get(3).get(0); //Hard coded for particular UUID
			Log.d(TAG, "Service: "
					+ SampleGattAttributes.lookup(characteristic.getService().getUuid().toString(), characteristic.getService().getUuid().toString()) + ", uuid: "
					+ SampleGattAttributes.lookup(characteristic.getUuid().toString(), characteristic.getUuid().toString()) + ", "
					+ characteristic.getProperties());
			final int charaProp = characteristic.getProperties();
			if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
				// If there is an active notification on a characteristic, clear
				// it first so it doesn't update the data field on the user interface.
				if (mNotifyCharacteristic != null) {
					mBluetoothLeService.setCharacteristicNotification(
							mNotifyCharacteristic, false);
					mNotifyCharacteristic = null;
				}
				mBluetoothLeService.readCharacteristic(characteristic);
			}
			if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
				mNotifyCharacteristic = characteristic;
				mBluetoothLeService.setCharacteristicNotification(
						characteristic, true);
			}
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
}
