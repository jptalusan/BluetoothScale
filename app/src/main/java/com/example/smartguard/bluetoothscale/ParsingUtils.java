package com.example.smartguard.bluetoothscale;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class ParsingUtils {
	private static String TAG = "Youten.ParseUtils";
	private boolean isShow = false;

	public void setIsShow(boolean isShown) {
		this.isShow = isShown;
	}
	/**
	 * Convert hex string to byte[]
	 *
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase(Locale.ENGLISH);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;

			Log.d(TAG, "charToByte[" + pos + "]:" + hexChars[pos] + ":" + charToByte(hexChars[pos]));
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			Log.d(TAG, "bytes[" + i + "]:" + d[i]);
		}
		return d;
	}


	/**
	 * Convert char to byte
	 *
	 * @param c
	 *            char
	 * @return byte
	 */
	private byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public String parse(String content) {
		Log.e(TAG, "Parse: " + content);
		byte[] data = hexStringToBytes(content);

		// weight
		int v = data[0] & 0xFF;
		int weight = (data[4] << 8) | (data[5] & 0xff) ;
		float scale = (float) 0.1;
		if (v == 0xcf) {
			scale = (float) 0.1;
		} else if (v == 0xce) {
			scale = (float) 0.1;
		} else if (v == 0xcb) {
			scale = (float) 0.01;
		} else if (v == 0xca) {
			scale = (float) 0.001;
		}

		float weightRec = scale * weight;

		if (weightRec < 0)
		{
			weightRec *= -1;
		}


		java.text.DecimalFormat dfc = new java.text.DecimalFormat("#.#");

		String rec = "body weight:" + dfc.format(weightRec < 0 ? -weightRec: weightRec) + "Kg";
		if (!isShow)
		{
			isShow = true;
			return rec;
		}
		return "";
	}
}

