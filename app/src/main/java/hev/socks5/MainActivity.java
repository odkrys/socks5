/*
 ============================================================================
 Name        : MainActivity.java
 Author      : hev <r@hev.cc>
 Copyright   : Copyright (c) 2024 hev
 Description : Main Activity
 ============================================================================
 */

package hev.socks5;

import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity implements View.OnClickListener {
	private Preferences prefs;
	private EditText edittext_workers;
	private EditText edittext_listen_addr;
	private EditText edittext_listen_port;
	private EditText edittext_udp_listen_addr;
	private EditText edittext_udp_listen_port;
	private EditText edittext_bind_ipv4_addr;
	private EditText edittext_bind_ipv6_addr;
	private EditText edittext_bind_iface;
	private EditText edittext_auth_user;
	private EditText edittext_auth_pass;
	private CheckBox checkbox_listen_ipv6_only;
	private Button button_save;
	private Button button_control;

	private BroadcastReceiver serverStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Socks5Service.ACTION_SERVER_STATUS_CHANGED.equals(intent.getAction())) {
					checkAndSyncSocks5ServiceState();
			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = new Preferences(this);
		setContentView(R.layout.main);

		edittext_workers = (EditText) findViewById(R.id.workers);
		edittext_listen_addr = (EditText) findViewById(R.id.listen_addr);
		edittext_listen_port = (EditText) findViewById(R.id.listen_port);
		edittext_udp_listen_addr = (EditText) findViewById(R.id.udp_listen_addr);
		edittext_udp_listen_port = (EditText) findViewById(R.id.udp_listen_port);
		edittext_bind_ipv4_addr = (EditText) findViewById(R.id.bind_ipv4_addr);
		edittext_bind_ipv6_addr = (EditText) findViewById(R.id.bind_ipv6_addr);
		edittext_bind_iface = (EditText) findViewById(R.id.bind_iface);
		edittext_auth_user = (EditText) findViewById(R.id.auth_user);
		edittext_auth_pass = (EditText) findViewById(R.id.auth_pass);
		checkbox_listen_ipv6_only = (CheckBox) findViewById(R.id.listen_ipv6_only);
		button_save = (Button) findViewById(R.id.save);
		button_control = (Button) findViewById(R.id.control);

		checkbox_listen_ipv6_only.setOnClickListener(this);
		button_save.setOnClickListener(this);
		button_control.setOnClickListener(this);

		requestNotificationPermission();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(Socks5Service.ACTION_SERVER_STATUS_CHANGED);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			registerReceiver(serverStatusReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
		} else {
			registerReceiver(serverStatusReceiver, filter);
		}
		checkAndSyncSocks5ServiceState();
	}


	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(serverStatusReceiver);
	}

	@Override
	public void onClick(View view) {
		if (view == button_save) {
			savePrefs();
			Context context = getApplicationContext();
			Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
		} else if (view == button_control) {
			boolean isEnable = prefs.getEnable();
			prefs.setEnable(!isEnable);
			savePrefs();
			updateUI();
			Intent intent = new Intent(this, Socks5Service.class);
			if (isEnable)
			  startService(intent.setAction(Socks5Service.ACTION_STOP));
			else
			  startService(intent.setAction(Socks5Service.ACTION_START));
		}
	}

	private void checkAndSyncSocks5ServiceState() {
		boolean isSocks5ServiceActuallyRunning = isServiceRunning(Socks5Service.class);

		if (prefs.getEnable() != isSocks5ServiceActuallyRunning) {
			prefs.setEnable(isSocks5ServiceActuallyRunning);
			savePrefs();
		}
		updateUI();
	}

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if (manager != null) {
			for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (serviceClass.getName().equals(service.service.getClassName())) {
					return true;
				}
			}
		}
		return false;
	}

	private void requestNotificationPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this,
						new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
						1001);
			}
		}
	}

	private void updateUI() {
		edittext_workers.setText(Integer.toString(prefs.getWorkers()));
		edittext_listen_addr.setText(prefs.getListenAddress());
		edittext_listen_port.setText(Integer.toString(prefs.getListenPort()));
		edittext_udp_listen_addr.setText(prefs.getUDPListenAddress());
		edittext_udp_listen_port.setText(Integer.toString(prefs.getUDPListenPort()));
		edittext_bind_ipv4_addr.setText(prefs.getBindIPv4Address());
		edittext_bind_ipv6_addr.setText(prefs.getBindIPv6Address());
		edittext_bind_iface.setText(prefs.getBindInterface());
		edittext_auth_user.setText(prefs.getAuthUsername());
		edittext_auth_pass.setText(prefs.getAuthPassword());
		checkbox_listen_ipv6_only.setChecked(prefs.getListenIPv6Only());

		boolean isServerEnabled = prefs.getEnable();
		boolean editable = !isServerEnabled;

		edittext_workers.setEnabled(editable);
		edittext_listen_addr.setEnabled(editable);
		edittext_listen_port.setEnabled(editable);
		edittext_udp_listen_addr.setEnabled(editable);
		edittext_udp_listen_port.setEnabled(editable);
		edittext_bind_ipv4_addr.setEnabled(editable);
		edittext_bind_ipv6_addr.setEnabled(editable);
		edittext_bind_iface.setEnabled(editable);
		edittext_auth_user.setEnabled(editable);
		edittext_auth_pass.setEnabled(editable);
		checkbox_listen_ipv6_only.setEnabled(editable);
		button_save.setEnabled(editable);

		if (editable)
		  button_control.setText(R.string.control_start);
		else
		  button_control.setText(R.string.control_stop);
	}

	private void savePrefs() {
		prefs.setWorkers(Integer.parseInt(edittext_workers.getText().toString()));
		prefs.setListenAddress(edittext_listen_addr.getText().toString());
		prefs.setListenPort(Integer.parseInt(edittext_listen_port.getText().toString()));
		prefs.setUDPListenAddress(edittext_udp_listen_addr.getText().toString());
		prefs.setUDPListenPort(Integer.parseInt(edittext_udp_listen_port.getText().toString()));
		prefs.setBindIPv4Address(edittext_bind_ipv4_addr.getText().toString());
		prefs.setBindIPv6Address(edittext_bind_ipv6_addr.getText().toString());
		prefs.setBindInterface(edittext_bind_iface.getText().toString());
		prefs.setAuthUsername(edittext_auth_user.getText().toString());
		prefs.setAuthPassword(edittext_auth_pass.getText().toString());
		prefs.setListenIPv6Only(checkbox_listen_ipv6_only.isChecked());
	}
}
