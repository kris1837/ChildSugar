package com.example.childsugar.nfc_app_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by yth on 22/02/2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

	private boolean isNetworkAvail = false, isWiFiAvail = false;

	public interface ConnectivityEvent {
		void onConnect();
		void onDisconnect();
	}

	ConnectivityEvent m_ConnectivityEvent = null;

	// ctor
	public NetworkChangeReceiver() {
	}

	// ctor
	public NetworkChangeReceiver(ConnectivityEvent ev) {
		m_ConnectivityEvent = ev;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager cm =
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		isNetworkAvail = isConnected;
		isWiFiAvail = isConnected && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

		if (isConnected) {
			// Tell all interested parties the network is available
			// Call isNetworkAvailable() and isWiFiAvailable() to determine what type is available
			if (m_ConnectivityEvent != null) {
				m_ConnectivityEvent.onConnect();
			}
		}
		else {
			// Tell all interested parties the network is not available
			if (m_ConnectivityEvent != null) {
				m_ConnectivityEvent.onDisconnect();
			}
		}
	}

	public boolean isNetworkAvailable() {
		return isNetworkAvail;
	}

	public boolean isWiFiAvailable() {
		return isWiFiAvail;
	}
}
