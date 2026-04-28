package com.example.aiclient;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private ArrayList<String> messages = null;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		setContentView(R.layout.activity_main);
		
		String notifMessage = getIntent().getStringExtra("message");
		if (notifMessage != null) {
		    advertise(notifMessage);
		}
		
		if(messages == null) messages = new ArrayList<String>();
		
		final Button btn = (Button)findViewById(R.id.button1);
		final Button btn2 = (Button)findViewById(R.id.button2);
		final EditText edit = (EditText)findViewById(R.id.editText1);
		
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				btn.setEnabled(false);
				
				Toast.makeText(getApplicationContext(), "Please wait for processing...", Toast.LENGTH_SHORT).show();
				
				String msg = edit.getText().toString();
				
				messages.add("User: " + msg);
				
				String finalRequest = buildHistory();
				
				// local api key for server, leave the same if using my python proxy
				new PostTask("my-secret-key", finalRequest, new PostTask.TaskListener() {
				    public void onResult(int responseCode, final String body) {
				        Log.d("POST", "code=" + responseCode + " body=" + body);
				        
				        messages.add("You (AI): " + body);
				        
				        runOnUiThread(new Runnable() {
				            public void run() {
				                advertise(body);
				                publishNotification(body);
				                edit.setText("");
				                btn.setEnabled(true);
				            }
				        });
				    }
				    public void onError(String error) {
				        Log.e("POST", "error=" + error);
				        
				        Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_SHORT).show();
				        
				        btn.setEnabled(true);
				    }
				}).execute();
			}
			
		});
		
		btn2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				confirmClear();
			}
			
		});
	}
	
	private String buildHistory() {
		StringBuilder logBuilder = new StringBuilder();
		for(String message : messages) {
			logBuilder.append(message);
			logBuilder.append('\n');
		}
		
		return logBuilder.toString();
	}
	
	private void advertise(String message) {
		new AlertDialog.Builder(this)
	        .setTitle("Reply from AI")
	        .setMessage(message)
	        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
	        	@Override
	            public void onClick(DialogInterface dialog, int which) { 
	                
	            }
	        })
	    .show();
	}
	
	private void reviewHistory() {
		String message = buildHistory();
		
		new AlertDialog.Builder(this)
        .setTitle("Chat history")
        .setMessage(message)
        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) { 
                
            }
        })
    .show();
	}
	
	private void confirmClear() {
		new AlertDialog.Builder(this)
	        .setTitle("Are you sure?")
	        .setMessage("This is going to reset history of this chat!")
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        	@Override
	            public void onClick(DialogInterface dialog, int which) { 
	                messages.clear();
	                Toast.makeText(getApplicationContext(), "History is reset!", Toast.LENGTH_SHORT).show();
	            }
	        })
	        .setNegativeButton("No", new DialogInterface.OnClickListener() {
	        	@Override
	            public void onClick(DialogInterface dialog, int which) { 
	                
	            }
	        })
	    .show();
	}
	
	private void publishNotification(String message) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		int icon = R.drawable.ic_stat_name;
		CharSequence tickerText = "AI finished thinking!"; // Text that flashes in the status bar
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		// Define the color
		notification.ledARGB = 0xFF00FFFF; 

		// How long the LED stays on and off (in ms)
		notification.ledOnMS = 2000; 
		notification.ledOffMS = 1000; 

		notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "New response";
		CharSequence contentText = message;

		Intent intent = new Intent();

		PendingIntent contentIntent = PendingIntent.getActivity(
		    getApplicationContext(), 
		    0, 
		    intent, 
		    PendingIntent.FLAG_ONE_SHOT
		);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(1, notification);
	}
}
