package com.example.getarduinolog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button sendButton;
	TextView logView;
	EditText command;
	List<String> cmds;
	
	String fileName  = "arduinoLog";
	String baudRate = "57600";
	
	EditText currentTime;
	TextView NTPTime;
	TextView localTime;
	Spinner spBaudrate;
	
	boolean isThreadRunning = false;
	
	boolean isBaudRateSet = false;
	
	boolean savelog = false;
	boolean isFileNameSet = false;
	public static Date dateNTPTime;
	
	
	
	private Handler customHandler = new Handler();
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			
			String aResponse = msg.getData().getString("message");
			if ((null != aResponse)) {
				
				//long unixTime = System.currentTimeMillis() / 1000L;
				
				long unixTime;
				if(dateNTPTime != null) {
					unixTime = dateNTPTime.getTime() / 1000;
				} else {
					unixTime = new Date().getTime() / 1000;
				}
				
				if(savelog) {
					logView.append(unixTime+" "+aResponse + "\n");
					CustomLogger.appendLog(fileName, unixTime+" "+aResponse + "\n");
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sendButton = (Button) findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new MyClickListener());

		logView = (TextView) findViewById(R.id.logView);
		logView.setEnabled(false);
		command = (EditText) findViewById(R.id.command);
		NTPTime = (TextView) findViewById(R.id.ntpTime);
		localTime = (TextView) findViewById(R.id.localTime);
		spBaudrate = (Spinner) findViewById(R.id.spinnerBaudRate);
		
		cmds = new ArrayList<String>();
		
		
		cmds.add("stty -F /dev/ttyACM0 "+baudRate+";" +"cat /dev/ttyACM0");
		
		CustomLogger.appendLog(fileName, "\nNew Experiment\n");
		
		customHandler.postDelayed(updateTimerThread, 0);
		Spinner sp = (Spinner)findViewById(R.id.spinnerBaudRate); 
		sp.setSelection(10);
		/*sp.setOnItemSelectedListener(new OnItemSelectedListener() {
			
		});*/
		
	}
	
	class NTPTime extends AsyncTask<String , Void, Long> {
		Date current;
		public static final String TIME_SERVER = "time-a.nist.gov";
		
		protected void onPreExecute() {
			dateNTPTime = null;
		}
		
		@Override
		protected Long doInBackground(String... params) {
			//NTP server list: http://tf.nist.gov/tf-cgi/servers.cgi
			try {
				NTPUDPClient timeClient = new NTPUDPClient();
				InetAddress inetAddress;
				inetAddress = InetAddress.getByName(TIME_SERVER);
				TimeInfo timeInfo = timeClient.getTime(inetAddress);
				//long returnTime = timeInfo.getReturnTime();   //local device time
				long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();   //server time
			//	current = new Date(returnTime);
				dateNTPTime = new Date(returnTime);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		
		protected void onPostExecute(Long d) {
//			Log.i("NTP", "ON POST EXCUTE");
			localTime.setTextColor(Color.BLACK);
			NTPTime.setTextColor(Color.BLACK);
			
			if( (dateNTPTime!=null) && (!dateNTPTime.toString().equals(""))) {
				NTPTime.setText("Current (NTP) Time: "+dateNTPTime.toString());
				NTPTime.setTextColor(Color.BLUE);
			} else {
				localTime.setTextColor(Color.BLUE);
				NTPTime.setText("Current (NTP) Time: Can not get time from server! use local time");
			}
			
			localTime.setText("Local time (phone):"+new Date().toGMTString()); 
	    }
	}
	
	private Runnable updateTimerThread = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			new NTPTime().execute();
			localTime.setText("Local time (phone):"+new Date().toGMTString()); 
			customHandler.postDelayed(this, 1000);
		}
		
	};
	
	Thread background = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try {
				Process process = Runtime.getRuntime().exec("su");
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				
				DataOutputStream os = new DataOutputStream(process.getOutputStream());
				String message = null;
				
				/*if(isBaudRateSet==false) {
					 os.writeBytes("stty -F /dev/ttyACM0"+baudRate+";\n");
					 isBaudRateSet = true;
					 if((message = in.readLine()) != null){
						 threadMsg(message);
					 }
				}*/
				
				//run command cat /dev/ttyACM0 to get log
			    /*for (String tmpCmd : cmds) {
			            os.writeBytes(tmpCmd+"\n");
			    }*/
				
				os.writeBytes("stty -F /dev/ttyACM0 "+baudRate+";" +"cat /dev/ttyACM0");
			    
			    os.flush();
			    os.close();
			    
				
				while ((message = in.readLine()) != null) {
					//Display on device
					threadMsg(message);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				threadMsg(e.getMessage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				threadMsg(e.getMessage());
			}
		}
		
	});
	
	private void threadMsg(String msg) {
		if (!msg.equals(null) && !msg.equals("")) {
			Message msgObj = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("message", msg);
			msgObj.setData(b);
			handler.sendMessage(msgObj);
		}
	}
	
	public class MyClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.sendButton:
				
				if(sendButton.getText().toString().equals("START")) {
					if(command.getText().toString().replaceAll("\\s+","").length()>0) {
						fileName = command.getText().toString().replaceAll("\\s+","");
					}
					baudRate=spBaudrate.getSelectedItem().toString();

					Toast.makeText(MainActivity.this, "Baud rate:"+baudRate+" Data file"+fileName, Toast.LENGTH_SHORT).show();
					
					if(isThreadRunning == false) {
						background.start();
						isThreadRunning = true;
					}
					
					savelog  = true;
					sendButton.setText("STOP");
				} else  if(sendButton.getText().toString().equals("STOP")) {
					//background.stop();
					savelog = false;
					sendButton.setText("START");
				}
				break;

			default:
				break;
			}
		}

	}

}
