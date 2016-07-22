package com.example.smartguard.bluetoothscale;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
	private static HashMap<String, String> attributes = new HashMap();
	public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	public static String CHAR_READ_STRING = "0000fff4-0000-1000-8000-00805f9b34fb";

	static {
		// Sample Services.
		attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
		// Sample Characteristics.
		attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");

		attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "SERVICE_DEVICE_INFORMATION");
		attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "CHAR_MANUFACTURER_NAME_STRING");
		attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "CHAR_MODEL_NUMBER_STRING");
		attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "CHAR_SERIAL_NUMBEAR_STRING");

		attributes.put("00001802-0000-1000-8000-00805f9b34fb", "SERVICE_IMMEDIATE_ALERT");
		attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "CHAR_ALERT_LEVEL");

		attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "SERVICE_WRITE_STRING");
		attributes.put("0000fff1-0000-1000-8000-00805f9b34fb", "CHAR_WRITE_STRING");
		attributes.put("0000fff4-0000-1000-8000-00805f9b34fb", "CHAR_READ_STRING");

		attributes.put("0000180F-0000-1000-8000-00805f9b34fb", "SERVICE_BATTERY_SERVICE");
		attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "CHAR_BATTERY_LEVEL");

		attributes.put("00002902-0000-1000-8000-00805f9b34fb", "CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID");
	}

	public static String lookup(String uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}
}