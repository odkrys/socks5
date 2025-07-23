/*
 ============================================================================
 Name        : Socks5Service.java
 Author      : hev <r@hev.cc>
 Copyright   : Copyright (c) 2024 hev
 Description : Socks5 Service
 ============================================================================
 */

package hev.socks5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Binder;
import android.os.IBinder;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;

import androidx.core.app.NotificationCompat;

public class Socks5Service extends Service {
	private static native void Socks5StartService(String config_path);
	private static native void Socks5StopService();

	public static final String ACTION_START = "hev.socks5.START";
	public static final String ACTION_STOP = "hev.socks5.STOP";
	public static final String ACTION_SERVER_STATUS_CHANGED = "hev.socks5.SERVER_STATUS_CHANGED";

	static {
		System.loadLibrary("hev-socks5-server");
	}

	public class Socks5Binder extends Binder {
		Socks5Service getService() {
			return Socks5Service.this;
		}
	}

	private final IBinder mBinder = new Socks5Binder();
	private boolean started = false;
	private Preferences prefs;

	@Override
	public void onCreate() {
		super.onCreate();
		prefs = new Preferences(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && ACTION_STOP.equals(intent.getAction())) {
			stopService();
			return START_NOT_STICKY;
		}
		startService();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		prefs.setEnable(false);
		sendServerStatusChangedBroadcast();
		super.onDestroy();
	}

	public void startService() {
		if (started)
		  return;

		File file = new File(getCacheDir(), "socks5.conf");
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file, false);

			String conf = "main:\n" +
				"  workers: " + prefs.getWorkers() + "\n" +
				"  port: " + prefs.getListenPort() + "\n" +
				"  listen-address: '" + prefs.getListenAddress() + "'\n" +
				"  udp-port: " + prefs.getUDPListenPort() + "\n" +
				"  udp-listen-address: '" + prefs.getUDPListenAddress() + "'\n" +
				"  listen-ipv6-only: " + prefs.getListenIPv6Only() + "\n" +
				"  bind-address-v4: '" + prefs.getBindIPv4Address() + "'\n" +
				"  bind-address-v6: '" + prefs.getBindIPv6Address() + "'\n" +
				"  bind-interface: '" + prefs.getBindInterface() + "'\n";

			conf += "misc:\n" +
				"  task-stack-size: " + prefs.getTaskStackSize() + "\n";

			if (!prefs.getAuthUsername().isEmpty() &&
				!prefs.getAuthPassword().isEmpty()) {
				conf += "auth:\n" +
					"  username: '" + prefs.getAuthUsername() + "'\n" +
					"  password: '" + prefs.getAuthPassword() + "'\n";
			}

			fos.write(conf.getBytes());
			fos.close();
		} catch (IOException e) {
			return;
		}
		Socks5StartService(file.getAbsolutePath());
		prefs.setEnable(true);
		started = true;

		String channelName = "socks5";
		initNotificationChannel(channelName);
		createNotification(channelName);

		sendServerStatusChangedBroadcast();
	}

	public void stopService() {
		if (!started)
		  return;

		stopForeground(true);
		Socks5StopService();
		prefs.setEnable(false);
		started = false;

		sendServerStatusChangedBroadcast();
		stopSelf();
	}

	private void createNotification(String channelName) {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

		Intent stopIntent = new Intent(this, Socks5Service.class);
		stopIntent.setAction(ACTION_STOP);
		PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelName);
		notificationBuilder
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Server is ON")
				.setSmallIcon(android.R.drawable.sym_def_app_icon)
				.setContentIntent(contentPendingIntent)
				.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent);

		Notification notify = notificationBuilder.build();

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			startForeground(1, notify);
		} else {
			startForeground(1, notify, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
		}
	}

	private void initNotificationChannel(String channelName) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = getString(R.string.app_name);
			NotificationChannel channel = new NotificationChannel(channelName, name, NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
		}
	}

	private void sendServerStatusChangedBroadcast() {
		Intent intent = new Intent(ACTION_SERVER_STATUS_CHANGED);
		intent.setPackage(getPackageName());
		sendBroadcast(intent);
	}
}
