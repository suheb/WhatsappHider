package com.haloappstudio.lastseenhider;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.haloappstudio.lastseenhider.R;

public class Hider extends Activity {

	private AdView adView;
	private final String AD_UNIT_ID = "ca-app-pub-1187028475051204/5849048176";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hider);
		TextView txt = (TextView) findViewById(R.id.textView1);
		Typeface font = Typeface.createFromAsset(getAssets(),
				"fonts/Chantelli_Antiqua.ttf");
		txt.setTypeface(font);

		final CheckBox wcheckBox = (CheckBox) findViewById(R.id.wcheckBox);
		final CheckBox mcheckBox = (CheckBox) findViewById(R.id.mcheckBox);

		wcheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					wcheckBox.setError(null);
					mcheckBox.setError(null);
				}
			}
		});

		mcheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					mcheckBox.setError(null);
					wcheckBox.setError(null);
				}
			}
		});

		// Create the adView.
		adView = new AdView(this);
		adView.setAdUnitId(AD_UNIT_ID);
		adView.setAdSize(AdSize.SMART_BANNER);

		LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);

		layout.addView(adView);
		AdRequest adRequest = new AdRequest.Builder()
							.addTestDevice("FB5DE11AEA131AC62B8978FB62009488")
							.build();
		
		adView.loadAd(adRequest);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.hider, menu);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			MenuItem actionItem = menu.findItem(R.id.menu_share);
			ShareActionProvider actionProvider = (ShareActionProvider) actionItem
					.getActionProvider();
			actionProvider.setShareIntent(createShareIntent());
		}

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_about)
			showDialogBox();
		return true;
	}

	public Intent createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WhatsApp Last Seen Hider");
		shareIntent
				.putExtra(Intent.EXTRA_TEXT,
						"https://play.google.com/store/apps/details?id=com.suhaib.whatsapphider");
		return shareIntent;
	}

	public void StartService(View v1) {
		CheckBox wcheckBox = (CheckBox) findViewById(R.id.wcheckBox);
		CheckBox mcheckBox = (CheckBox) findViewById(R.id.mcheckBox);
		wcheckBox.setError(null);
		mcheckBox.setError(null);
		if ((wcheckBox.isChecked() == true || mcheckBox.isChecked() == true)
				&& !isServiceRunning(this)) {

			Intent intent = new Intent(this, LastSeenService.class);
			if (wcheckBox.isChecked()) {
				intent.putExtra("wifi", true);
			} else {
				intent.putExtra("wifi", false);
			}
			if (mcheckBox.isChecked()) {
				intent.putExtra("mData", true);
			} else {
				intent.putExtra("mData", false);
			}
			startService(intent);
		} else {
			if (isServiceRunning(this)) {
				Toast.makeText(this, "Already Hiding your last seen!",
						Toast.LENGTH_SHORT).show();
			} else {
				wcheckBox.setError("Check");
				mcheckBox.setError("Check");
				Toast.makeText(this, "Check either Wifi or Mobile data",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	public void StopService(View v2) {
		if (isServiceRunning(this)) {
			Intent intent = new Intent(this, LastSeenService.class);
			stopService(intent);
		} else {
			Toast.makeText(this, "Already Showing your last seen!",
					Toast.LENGTH_SHORT).show();
		}
	}

	

	public static boolean isServiceRunning(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager
				.getRunningServices(50);

		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(
					"com.haloappstudio.lastseenhider.LastSeenService")) {
				return true;
			}
		}
		return false;
	}

	public void showDialogBox() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// Chain together various setter methods to set the dialog
		// characteristics
		builder.setMessage(R.string.dialog_message).setTitle(
				R.string.dialog_title);
		// Add the buttons
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						// Do nothing
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse("https://www.facebook.com/suhebjerk"));
						startActivity(browserIntent);
					}
				});
		builder.show();

	}
}
