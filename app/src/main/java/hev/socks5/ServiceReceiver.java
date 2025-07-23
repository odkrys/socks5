/*
 ============================================================================
 Name        : ServiceReceiver.java
 Author      : hev <r@hev.cc>
 Copyright   : Copyright (c) 2024 hev
 Description : ServiceReceiver
 ============================================================================
 */

package hev.socks5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action == null) {
			return;
		}
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Preferences prefs = new Preferences(context);

			/* Auto-start */
			if (prefs.getEnable()) {
				Intent i = new Intent(context, Socks5Service.class);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					context.startForegroundService(i.setAction(Socks5Service.ACTION_START));
				} else {
					context.startService(i.setAction(Socks5Service.ACTION_START));
				}
			}
		} else if (action.equals(Socks5Service.ACTION_SERVER_STATUS_CHANGED)) {
			Preferences prefs = new Preferences(context);
			boolean isServiceActuallyRunning = isServiceRunning(context, Socks5Service.class);
			if (prefs.getEnable() != isServiceActuallyRunning) {
				prefs.setEnable(isServiceActuallyRunning);
			}
		}
	}

	private boolean isServiceRunning(Context context, Class<?> serviceClass) {
		android.app.ActivityManager manager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (manager != null) {
			for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (serviceClass.getName().equals(service.service.getClassName())) {
					return true;
				}
			}
		}
		return false;
	}
}
