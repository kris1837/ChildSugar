package com.example.childsugar.nfc_app_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by yth on 29/08/2016.
 */
public class NetworkStatusReceiver extends BroadcastReceiver {
	public static final String NETWORK_STATUS_EVENT = "netstatev";
	public static final String NETWORK_STATUS = "netstat";
	public static final String NETWORK_STATUS_API = "netstatapi";

	public interface NetworkStatus {
		void onNetworkStatus(boolean error);

		void onNetworkStatus(boolean error, String api);
	}

	private NetworkStatus networkStatus;

	// ctor
	public NetworkStatusReceiver(NetworkStatus status) {
		this.networkStatus = status;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (networkStatus != null) {
			// Invoke interface callback
			if (intent.getAction().compareTo(NETWORK_STATUS_EVENT) == 0) {
				if (intent.hasExtra(NETWORK_STATUS)) {
					boolean networkError = intent.getBooleanExtra(NETWORK_STATUS, false);
					if (intent.hasExtra(NETWORK_STATUS_API)) {
						String api = intent.getStringExtra(NETWORK_STATUS_API);
						networkStatus.onNetworkStatus(networkError, api);
					} else {
						networkStatus.onNetworkStatus(networkError);
					}
				}
			}
		}
	}

	public static void showError(boolean error) {
		Intent intent = new Intent(NETWORK_STATUS_EVENT);
		intent.putExtra(NETWORK_STATUS, error);
		LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
	}


}
