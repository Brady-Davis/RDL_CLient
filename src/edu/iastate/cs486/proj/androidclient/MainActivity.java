package edu.iastate.cs486.proj.androidclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.iastate.cs486.hw3.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple android client that connects to port 3077.
 * 
 * @author Brady Davis
 * 
 *         Special thanks to example code found at JavaCodeGeeks by Nikos
 *         Maravitsas
 */
public class MainActivity extends Activity {

	private static final int SELECT_PICTURE = 1;
	
	final Context c = this;
	private Socket socket;
	private String ImagePath;

	// ConnectionService mService;

	private static int SERVERPORT;
	private static String SERVER_IP;

	private BroadcastReceiver rec;

	public Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			String newMsg = msg.getData().getString(Constants.MSG);
			TextView t = (TextView) findViewById(R.id.ConnectedPrompt);
			t.setText(newMsg);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v("MainActivity", "OnCreate call");
		if (savedInstanceState != null) {
			String s = savedInstanceState.getString(Constants.MSG);
			if (s != null) {
				Log.v("MainActivity", "Setting text view to be " + s);
				TextView t = (TextView) findViewById(R.id.ConnectedPrompt);
				t.setText(s);
			}
		}
		rec = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.v(Constants.LOG, "MainActivity got message with intent " + intent.getAction());
				if (Constants.MSG.equals(intent.getAction())) {
					String reply = intent.getStringExtra(Constants.MSG);
					Intent newMain = new Intent(context, MainActivity.class);
					Bundle bun = new Bundle();
					bun.putString(Constants.MSG, reply);
					Log.v(Constants.LOG, "STARTING NEW MAIN");
					context.startActivity(newMain);
				}

			}
		};
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to ConnectionService

		Intent intent = getIntent();
		SERVER_IP = intent.getStringExtra(Constants.IP);
		SERVERPORT = intent.getIntExtra(Constants.SOCKET, 3077);
		new Thread(new ClientThread(this)).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void SendMessage(View view) {
		try {
			EditText et = (EditText) findViewById(R.id.Message2Send);
			String str = et.getText().toString();
			Log.v("MainActivity",
					String.format("Sending string {%s} : to server", str));
			OutputStreamWriter osw = new OutputStreamWriter(
					socket.getOutputStream());
			BufferedWriter buff = new BufferedWriter(osw);
			PrintWriter out = new PrintWriter(osw);
			// Put our message and flush it out to the server
			out.println(str);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Picture selection logic
	 * @param view
	 */
	public void PickPic(View view){
		//todo
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(intent.ACTION_GET_CONTENT);
		Log.v("MainActivity", "Choosing pic from gallery");
		startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_PICTURE);
	}
	
	public void onAcivityResult(int requestCode, int resultCode, Intent data){
		Log.v("MainActivity", "Result got");
		if(resultCode == RESULT_OK){
			if(requestCode == SELECT_PICTURE){
				//todo
				Uri selectedImageUri = data.getData();
				ImagePath = getPath(selectedImageUri);
				TextView t = (TextView)findViewById(R.id.FileText);
				t.setText(ImagePath);
			}
		}
	}
	
	/**
	 * Helper method to retrieve path of URI
	 * @param uri
	 * @return
	 */
	public String getPath(Uri uri){
		if(uri == null){
			return null;
		}
		String[] projection = { MediaStore.Images.Media.DATA};
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if(cursor != null){
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String ret = cursor.getString(column_index);
			Log.v("MainActivity", ret);
			return ret;
		}
		Log.v("MainActivity", uri.getPath());
		return uri.getPath();
	}

	class ClientThread implements Runnable {
		MainActivity activity;
		
		public ClientThread(MainActivity activity){
			this.activity = activity;
		}
		
		@Override
		public void run() {

			try {
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
				socket = new Socket(serverAddr, SERVERPORT);

				BufferedReader input = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));

				String st = input.readLine();

				if (st != null && st.length() != 0) {
					Log.v("MainActivity", "Connection thread got string " + st);
					Bundle bun = new Bundle();
					bun.putString(Constants.MSG, st);
					Message m = new Message();
					m.setData(bun);
					activity.handler.sendMessage(m);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
