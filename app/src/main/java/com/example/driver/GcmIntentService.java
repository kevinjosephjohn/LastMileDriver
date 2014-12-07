package com.example.driver;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.JsonReader;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by Nishanth on 24-11-2014.
 */
public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	static final String TAG = "Driver";
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// This loop represents the service doing some work.

				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				// Post notification of received message.
				sendNotification(intent.getStringExtra("price"));
				Log.i(TAG, intent.getStringExtra("price"));
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
//		mNotificationManager = (NotificationManager)
//		this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//		new Intent(this, MainActivity.class), 0);
//
//		NotificationCompat.Builder mBuilder =
//		new NotificationCompat.Builder(this)
//		.setSmallIcon(R.drawable.ic_launcher)
//		.setContentTitle("GCM Notification")
//		.setStyle(new NotificationCompat.BigTextStyle()
//		.bigText(msg))
//		.setContentText(msg);
//
//		mBuilder.setContentIntent(contentIntent);
//		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        if(msg.equalsIgnoreCase("your trip has been cancelled"))
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
		try {
			JSONObject data = new JSONObject(msg);
			Intent intent = new Intent(this, RequestActivity.class);
			intent.putExtra("lat", data.getString("lat"));
            intent.putExtra("id", data.getString("id"));
			intent.putExtra("lng", data.getString("lng"));
			intent.putExtra("name", data.getString("name"));
			intent.putExtra("phone", data.getString("phone"));
            intent.putExtra("eta", data.getString("eta"));
            intent.putExtra("address", data.getString("address"));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}