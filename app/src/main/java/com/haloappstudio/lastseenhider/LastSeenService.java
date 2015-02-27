package com.haloappstudio.lastseenhider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

public class LastSeenService extends Service {

	private Handler handler;
	private Thread thread;
	private NotificationCompat.Builder mBuilder;
	private WifiManager wifiManager;
	private String packageName = "com.whatsapp";

	@Override
	public void onCreate() {
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("WhatsApp Hider")
				.setContentText("is hiding your last seen timestamp!")
				.setDefaults(Notification.DEFAULT_SOUND).setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, Hider.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(Hider.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		startForeground(1, mBuilder.build());
		final Boolean wifi = intent.getBooleanExtra("wifi", true);
		final Boolean mData = intent.getBooleanExtra("mData", true);
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (isAppOnForeground(LastSeenService.this)) {
					if (mData)
						setMobileDataEnabled(LastSeenService.this, false);
					if (wifi)
						wifiManager.setWifiEnabled(false);
				} else {
					if (mData)
						setMobileDataEnabled(LastSeenService.this, true);
					if (wifi)
						wifiManager.setWifiEnabled(true);
				}
			}

		};

		thread = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						sleep(1000);
						handler.sendEmptyMessage(0);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		thread.start();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		thread.interrupt();
		stopForeground(true);
	}

	private boolean isAppOnForeground(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null) {
			return false;
		}

		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
					&& appProcess.processName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	private void setMobileDataEnabled(Context context, boolean enabled) {

		ConnectivityManager conman = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		Method setMobileDataEnabledMethod = null;
		try {
			setMobileDataEnabledMethod = ConnectivityManager.class
					.getDeclaredMethod("setMobileDataEnabled", boolean.class);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setMobileDataEnabledMethod.setAccessible(true);
		try {
			setMobileDataEnabledMethod.invoke(conman, enabled);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
