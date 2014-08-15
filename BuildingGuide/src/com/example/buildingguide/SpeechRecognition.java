package com.example.buildingguide;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;


class SpeechRecognition implements RecognitionListener          
{
	private final String TAG = "MyRecTag";
	private SpeechRecognizer sr;
	private Intent intent;
	private String result = "";
	private MainActivity reference;
	SpeechRecognition(MainActivity m,SpeechRecognizer s, Intent i)
	{
		sr = s;
		intent = i;
		reference = m;
	}
         public void onReadyForSpeech(Bundle params)
         {
                  //Log.d(TAG, "onReadyForSpeech");
         }
         public void onBeginningOfSpeech()
         {
                  //Log.d(TAG, "onBeginningOfSpeech");
         }
         public void onRmsChanged(float rmsdB)
         {
                  //Log.d(TAG, "onRmsChanged");
         }
         public void onBufferReceived(byte[] buffer)
         {
                  //Log.d(TAG, "onBufferReceived");
         }
         public void onEndOfSpeech()
         {
                  //Log.d(TAG, "onEndofSpeech");
         }
         public void onError(int error)
         {
                  Log.d(TAG,  "error " +  error);
                  sr.cancel();
                  //sr.stopListening();
                  sr.startListening(intent);
         }
         public void onResults(Bundle results)                   
         {
              result = "";
             // Log.d(TAG, "onResults " + results);
              ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
              for (int i = 0; i < 1; i++)
              {
            	  Log.d(TAG, "Result: " + data.get(i));
            	  result += data.get(i);
              } 
              sr.stopListening();
              //Analyze result and find if there is a query match
              // Either highlight the room (selection = true) or set the text to be displayed/spoken
              //Query 1 : 'looking for', "Show me" + room, "belongs to" + room
              //Query 2 : "Show me" + schedule, "Give me" + schedule, "what" + "schedule"
              result = result.toLowerCase();
              result = " " + result + " ";
              int [] selections = new int [reference.Rooms.length];
              int [] count = new int [4];
              //Finding match
        	  for (int i = 0;i<reference.Rooms.length;i++)
        	  {
        		  
        		  selections[i] = -1;
        		  //if found exact matches for room no AND name
        		  if(result.contains(reference.Rooms[i].belongsTo.toLowerCase()) && (result.contains(Integer.toString(reference.Rooms[i].no)) && result.contains("room")))
        		  {
        			  selections[i] = 0;
        			  count[0]++;
        		  }
        		  else
        		  {
        			  if((result.contains(" this ") || result.contains(" that ") || result.contains(" selected ") || result.contains(" highlighted ")) && reference.Rooms[i].selection==true)
        			  {
        				  selections[i] = 1;
        				  count[1]++;
        			  }
        			  else
        			  {
	        			  //if found exact matches for room no OR name
	        			  if(result.contains(reference.Rooms[i].belongsTo.toLowerCase()) || (result.contains(" " +Integer.toString(reference.Rooms[i].no) + " ") && result.contains("room")))
	        			  {
	        				  selections[i] = 2;
	        				  count[2]++;
	        			  }
	        			  else
	        			  {
	        				  //partial match
	        				  StringTokenizer st = new StringTokenizer(reference.Rooms[i].belongsTo.toLowerCase()," ");
	        				  for (int j = 0;j<st.countTokens();j++)
	        				  {
	        					  String temp = st.nextToken();
	        					  if(!temp.equals("room") && result.contains(" " +temp + " "))
	        					  {
	        						  count[3]++;
	        						  selections[i] =3;
	        					  }
	        				  }
	        			  }	
        			  }
        		  }
        	  }
        	  String output_string = "";
        	  int j;
        	  for(j=0;j<4;j++)
        	  {
        		  if(count[j]>0)
        		  {
        			  for(int i=0;i<reference.Rooms.length;i++)
        			  {
        				  if(selections[i]==j)
        				  {
	        				  if(result.contains("schedule")) //Query 2
	        				  {
	        					  output_string = output_string + reference.Rooms[i].returnSchedule();
	        				  }
	        				  else
	        				  { // Query 1 
	        					  output_string = output_string + "Room " + reference.Rooms[i].no  + "belongs to " + reference.Rooms[i].belongsTo + "\n";
	        				  }
        				  }
        			  }
        			  break;
        		  }
        	  }
        	  String queryString =
      	            "prefix table:<http://www.semanticweb.org/ontologies/2013/1/Ontology1361806230149.owl#>"+     
      	            "select ?output where {table:" + reference.user_and_env[reference.envtype] + " table:hasOutputModality ?output. table:" + reference.user_and_env[reference.usertype] + " table:hasOutputModality ?output}";
            if(!output_string.equals(""))
            {
            	Query query = QueryFactory.create(queryString);
        		QueryExecution qe = QueryExecutionFactory.create(query, reference.model);
        		ResultSet resul =  qe.execSelect();
        		ByteArrayOutputStream sos = new ByteArrayOutputStream();
        		ResultSetFormatter.out(sos, resul, query);
      	    	String str = sos.toString();
      	    	if(str.contains("HighlightedText_Output"))
      	    	{
      	    		if(result.contains("schedule")) //Query 2
      	    		{
      	    			AlertDialog.Builder schedule_dialog = new AlertDialog.Builder(reference);
      	        		//schedule_dialog.setPositiveButton("Close", new Dialog.OnClickListener() {
      	        		TextView showschedule= new TextView(reference);
      	        		showschedule.setText(output_string);
      	        		schedule_dialog.setView(showschedule);
      	        		schedule_dialog.setCancelable(true);
      	        		schedule_dialog.show();
      	    		}
      	    		else
      	    		{
      	    			for(int k=0;k<reference.Rooms.length;k++)
      	    			{
      	    				reference.Rooms[k].selection = false;
          				  if(selections[k]==j)
          				  {
          					reference.Rooms[k].selection = true;
          				  }
      	    			}
      	    			reference.m.invalidate();
      	    		}
      	    		
      	    	}
      	    	if(str.contains("LoudSpeech_Output"))
      	    	{
      	    		AudioManager am = (AudioManager)reference.getSystemService(Context.AUDIO_SERVICE);
      	    		int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      	    		am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
      	    		reference.myTTS.speak(output_string, TextToSpeech.QUEUE_FLUSH, null);
      	    	}
      	    	if(str.contains("NormalSpeech_Output"))
      	    	{
      	    		AudioManager am = (AudioManager)reference.getSystemService(Context.AUDIO_SERVICE);
      	    		int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      	    		am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol/3, 0);
      	    		reference.myTTS.speak(output_string, TextToSpeech.QUEUE_FLUSH, null);
      	    	}
      	    	
      	    	if(str.contains("Vibration_Output"))
      	    	{
      	    		Vibrator v = (Vibrator) reference.getSystemService(Context.VIBRATOR_SERVICE);
      	    		 
      	    		// Vibrate for 300 milliseconds
      	    		v.vibrate(800);

      	    	}
            }
              sr.cancel();              
              sr.startListening(intent);
         }
         
         public void onPartialResults(Bundle partialResults)
         {
                  //Log.d(TAG, "onPartialResults");
         }
         
         public void onEvent(int eventType, Bundle params)
         {
                  //Log.d(TAG, "onEvent " + eventType);
         }
         
         public String getResult()
         {
        	 
        	 return result;
         }
}