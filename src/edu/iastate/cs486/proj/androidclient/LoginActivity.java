package edu.iastate.cs486.proj.androidclient;

import edu.iastate.cs486.hw3.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.LOGIN_FAIL);
		filter.addAction(Constants.LOGIN_SUCCESS);
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent){
				Log.v("LoginActivity", "Received broadcast with action " + intent.getAction());
				if(Constants.LOGIN_SUCCESS.equals(intent.getAction())){
					
					Intent mainIntent = new Intent(context, MainActivity.class);
					startActivity(mainIntent);
				} else{
					Log.v("LoginActivity", "Login failed. Please check to see if you are using the same socket and IP as the server");
					Toast.makeText(context, "There was an error connecting to the server", Toast.LENGTH_SHORT);
					return;
				}
			}
		};
		registerReceiver(receiver, filter);
		
	}
	
	public void startLink(View view){
		Log.v("LoginActivity", "Starting connection");
		String ip =  ((EditText)findViewById(R.id.IPEdit)).getText().toString();
		int portNo;
		try{
			EditText p = (EditText)findViewById(R.id.SocketEdit);
			portNo = Integer.parseInt(p.getText().toString());
		} catch(NumberFormatException e){
			Log.v("LoginActivity", "Invalid socket");
			Toast.makeText(this, "Please enter a valid port number", Toast.LENGTH_SHORT);
			return;
		}
		if(!validIP(ip)){
			Log.v("LoginActivity", "Invalid IP");
			Toast.makeText(this, "Invalid IP entered. Please enter a valid IPv4 IP", Toast.LENGTH_SHORT);
			return;
		}
		Intent main = new Intent(this, MainActivity.class);
		main.putExtra(Constants.IP, ip);
		main.putExtra(Constants.SOCKET, portNo);
		Log.v("LoginActivity", "Starting main activity with given IP and socket");
		startActivity(main);
//		Intent connect = new Intent(this, ConnectionService.class);
//		connect.setAction(Constants.CONNECT);
//		connect.putExtra(Constants.IP, ip);
//		connect.putExtra(Constants.SOCKET, portNo);
//		Log.v("LoginActivity", String.format("Trying to get server at %s on socket %d", ip, portNo));
//		Toast.makeText(this, String.format("Trying connection for ip %s on port %d...", ip, portNo), Toast.LENGTH_SHORT);
//		startService(connect);
	}
	
	private boolean validIP(String ip){
		try{
			if(ip == null || ip.length() == 0) return false;
			String[] parts = ip.split("\\.");
			if(parts.length != 4){
				return false;
			}
			
			for (String s : parts){
				int i = Integer.parseInt(s);
				if((i < 0) || (i > 255)){
					return false;
				}
			}
			return true;
		} catch(NumberFormatException nfe){
			return false;
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
	}
}
