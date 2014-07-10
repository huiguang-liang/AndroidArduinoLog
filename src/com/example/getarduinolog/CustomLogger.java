package com.example.getarduinolog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;

public class CustomLogger {
	public static void appendLog(String fileName, String text)
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
}
