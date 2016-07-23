package com.example.smartguard.bluetoothscale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
	private static String TAG = "bluetoothscale.MainActivity";
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 60000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHandler = new Handler();
		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "BLE not supported.", Toast.LENGTH_SHORT).show();
			finish();
		}

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (null == mBluetoothAdapter) {
			//Device does not support Bluetooth
		} else {
			if (!mBluetoothAdapter.isEnabled()) {

				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}

			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

			if (0 < pairedDevices.size()) {
				for (BluetoothDevice device : pairedDevices) {
					Log.d(TAG, "Name:" + device.getName() + ", Address:" + device.getAddress());
				}
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
		// fire an intent to display a dialog asking the user to grant permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		scanLeDevice(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
//		mLeDeviceListAdapter.clear();
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {

				@Override
				public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//Checks if the device detected is the electronic scale
							if (device == null) return;
							if (device.getName().toLowerCase().contains("scale")) {
								final Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
								intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
								intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
								Log.d(TAG, "name:" + device.getName());
								Log.d(TAG, "address:" + device.getAddress());
								if (mScanning) {
									mBluetoothAdapter.stopLeScan(mLeScanCallback);
									mScanning = false;
								}
								startActivity(intent);
							} else {
								Toast.makeText(MainActivity.this, "No electronic scale present.", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			};
}
