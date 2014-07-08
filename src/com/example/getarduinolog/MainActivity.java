package com.example.getarduinolog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button sendButton;
	TextView logView;
	EditText command;
	List<String> cmds;
	String fileName  = "arduinoLog";
	
	boolean isThreadRunning = false;
	boolean savelog = false;
	boolean isFileNameSet = false;
	
	
	
	public void appendLog(String fileName, String text)
	{       
		File external = Environment.getExternalStorageDirectory();
	    String sdcardPath = external.getPath();
		File logFile = new File(sdcardPath+"/"+fileName+".txt");
  	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	    	  //threadMsg(e.getMessage());
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			
			String aResponse = msg.getData().getString("message");
			if ((null != aResponse)) {
				// Log.i("MAIN", aResponse);
				
				long unixTime = System.currentTimeMillis() / 1000L;
				
				if(savelog) {
					logView.append(unixTime+" "+aResponse + "\n");
					appendLog(fileName, unixTime+" "+aResponse + "\n");
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
		
		cmds = new ArrayList<String>();
		cmds.add("cat /dev/ttyACM0");
		
		appendLog(fileName, "New Experiment"+new Date().getTime());
	}
	
	Thread background = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Process process = Runtime.getRuntime().exec("su");
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				
				DataOutputStream os = new DataOutputStream(process.getOutputStream());
				
				//run command cat /dev/ttyACM0 to get log
			    for (String tmpCmd : cmds) {
			            os.writeBytes(tmpCmd+"\n");
			    }
			    os.flush();
			    os.close();
			    
				String message = null;
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
					
					if(isFileNameSet == false) {
						if(command.getText().toString().replaceAll("\\s+","").length()>0) {
							fileName = command.getText().toString();
							isFileNameSet = true;
						}
					}
					
					Toast.makeText(MainActivity.this, "Data will be save to file " + fileName +  "in sdcard", Toast.LENGTH_SHORT).show();
					
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
