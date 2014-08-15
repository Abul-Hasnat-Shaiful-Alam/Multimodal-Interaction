package com.example.buildingguide;



import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;


public class MainActivity extends Activity implements OnInitListener {
	/* User profiles */
	private final int USER_NORMAL = 0;
	private final int USER_DEAF = 1;
	private final int USER_BLIND = 2;
	/*  Type of Environment */
	private final int ENV_QUIET = 3;
	private final int ENV_LESSNOISY = 4;
	private final int ENV_VERYNOISY = 5;
	public OntModel model;
	private final int MENU_SEARCH = 0;
	private final int MENU_SELECTUSER = 2;
	private final int MENU_SELECTENVIRONMENT = 3;
	private final int MENU_SHOWSCHEDULE = 1;
	private SpeechRecognizer sr;
	public int usertype = USER_NORMAL,envtype = ENV_LESSNOISY;
	private int MY_DATA_CHECK_CODE=0;
	public TextToSpeech myTTS;
	private Resources res;
	public DrawMap m;
	private int screenWidth,screenHeight,selectedRoom = -1;
	boolean highlight = false;
	public Room[] Rooms;
	//AlertDialog.Builder search_dialog,user_dialog,env_dialog;
	View search_layout,user_layout,env_layout;
	EditText savedText;
	Intent intent;
	public String[] user_and_env = {"Normal_User", "Deaf_User", "Blind_User", "Quiet_Env","LessNoisy_Env","VeryNosiy_Env"}; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    res=getResources();
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		boolean[][] roomGRID = { {false, true, true, true, true, false, true, true, true ,true, false},
								 {true, false, false, false, false, false, false, false, false ,false, true},
								 {true, false, true, true, true, false, true, true, true ,false, true},
								 {true, false, true, true, true, false, true, true, true ,false, true},
								 {false, false, false, false, false, false, false, false, false ,false, false},
								 {true, false, true, true, true, false, true, true, true ,false, true},
								 {true, false, true, true, true, false, true, true, true ,false, true},
								 {true, false, false, false, false, false, false, false, false ,false, true},
								 {false, true, true, true, true, false, true, true, true ,true, false}, };
		String[][] roomNAMES = { {"", "Printer Room", "Neal Lawrence", "Crystal Powers", "Alex May", "", "Franklin Vick", "Marcia Walsh", "Lecture Room", "Tim Watts", ""},
								 {"Jerome Johnston", "", "", "", "", "", "", "", "" ,"", "Meeting Room"},
								 {"Douglas Ross", "", "Paul Woods", "Harvey Underwood", "Computer Lab", "", "William Jones", "Jason Cross", "Lecture Room" ,"", "Pat Allen"},
								 {"Michelle Rich", "", "Computer Lab", "Melinda Proctor", "Jessica Rich", "", "Natalie Walton", "Kyle Watts", "Sara Lucas" ,"", "Mike Whitehead"},
								 {"", "", "", "", "", "", "", "", "" ,"", ""},
								 {"Lecture Room", "", "Diana Moore", "Kelly Bowman", "Jennifer Christian", "", "Brett Lamb", "Brandon James", "Johnny Campbell" ,"", "Thomas Brandon"},
								 {"Computer Lab", "", "Anna Bruce", "Robin Oliver", "Edward Dickinson", "", "Nicole Moss", "Ricky Neal", "Patrick Fields", "", "Billy Wade"},
								 {"Marc Pollard", "", "", "", "", "", "", "", "","", "Monica Brown"},
								 {"", "Meeting Room", "Linda Wilder", "Sam Bishop", "John Dickerson", "", "Pam Adcock", "Wendy Blanton", "Ben Miller" ,"Printer Room", ""} };
		loadRoomInfo(roomGRID,roomNAMES);
		m = new DrawMap(this);
		setContentView(m);
		intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);        
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.example.buildingguide");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1); 
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, 0);
        model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
	    String inputFileName = "MMI.owl";
	    InputStream in = FileManager.get().openNoMap(inputFileName);
	    if (in == null) {
	        throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	    }
	    model.read(in, null);
		sr = SpeechRecognizer.createSpeechRecognizer(this);
		//SpeechRecognition dd = new SpeechRecognition(sr,intent);
        sr.setRecognitionListener(new SpeechRecognition(this, sr,intent));
        sr.cancel();
		sr.startListening(intent);
	}
	
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (requestCode == MY_DATA_CHECK_CODE) {
	            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	                myTTS = new TextToSpeech(this,this);
	                myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener(){
	                public void	 onDone(String utteranceId)
	                {
	                	sr.cancel();
	            		sr.startListening(intent);
	                }
	                public	void onError(String utteranceId)
	                {
	                	
	                }
	                public	void onStart(String utteranceId)
	                {
	                	sr.stopListening();
	                	sr.cancel();
	                }
	                });
	            }
	            else {
	                Intent installTTSIntent = new Intent();
	                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	                startActivity(installTTSIntent);
	            }
	            }
	    }
	    
	    public void onInit(int initStatus) {
	        if (initStatus == TextToSpeech.SUCCESS) {
	            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE) myTTS.setLanguage(Locale.US);
	        }
	        else if (initStatus == TextToSpeech.ERROR) {
	            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
	        }
	    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_SEARCH, 0, res.getText(R.string.search))
    		.setIcon(0);
    	 menu.add(0, MENU_SHOWSCHEDULE, 0, res.getText(R.string.show_schedule))
         .setIcon(0);
    	menu.add(0, MENU_SELECTUSER, 0, res.getText(R.string.select_user))
    	.setIcon(0);
        menu.add(0, MENU_SELECTENVIRONMENT, 0, res.getText(R.string.select_env))
        .setIcon(0);
    	//	.setIcon(R.drawable.screenshoticon); 
       // menu.add(0, MENU_SHOWMOVES, 0, res.getText(R.string.show_moves));
		//.setIcon(R.drawable.screenshoticon);*/
        return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        	case MENU_SHOWSCHEDULE:
        		 String queryString =
   	            "prefix table:<http://www.semanticweb.org/ontologies/2013/1/Ontology1361806230149.owl#>"+     
   	            "select ?output where {table:" + user_and_env[envtype] + " table:hasOutputModality ?output. table:" + user_and_env[usertype] + " table:hasOutputModality ?output}";
        		Query query = QueryFactory.create(queryString);
        		QueryExecution qe = QueryExecutionFactory.create(query, model);
        		ResultSet resul =  qe.execSelect();
        		ByteArrayOutputStream sos = new ByteArrayOutputStream();
        		ResultSetFormatter.out(sos, resul, query);
      	    	String str = sos.toString();
	      	    TextView showschedule= new TextView(this);
	      		showschedule.setText("");
	      		for(int k=0;k<Rooms.length;k++)
	      		{
	      			if(Rooms[k].selection==true)
	      			{
	      				showschedule.append(Rooms[k].returnSchedule());
	      			}
	      		}
	      		if(showschedule.getText().equals(""))
        			showschedule.setText("Please select a room first.");
      	    	if(str.contains("HighlightedText_Output"))
      	    	{
      	    		AlertDialog.Builder schedule_dialog = new AlertDialog.Builder(this);
            		//schedule_dialog.setPositiveButton("Close", new Dialog.OnClickListener() {
            		schedule_dialog.setView(showschedule);
            		schedule_dialog.setCancelable(true);
            		schedule_dialog.show();
      	    	}
      	    	else
      	    	{
      	    		if(str.contains("LoudSpeech_Output"))
      	    		{
	      	    		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	      	    		int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	      	    		am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
	      	    		myTTS.speak(showschedule.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
	      	    	}
	      	    	if(str.contains("NormalSpeech_Output"))
	      	    	{
	      	    		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	      	    		int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	      	    		am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol/3, 0);
	      	    		myTTS.speak(showschedule.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
	      	    	}
      	    	}
      	    	if(str.contains("Vibration_Output"))
      	    	{
      	    		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
     	    		 
      	    		// Vibrate for 300 milliseconds
      	    		v.vibrate(800);
      	    	}
        		//}
        		/*public void onClick(DialogInterface dialog, int which) {
    	        	String search_text = ((EditText) search_layout.findViewById(R.id.txt_note)).getText().toString().trim().toLowerCase();
    	        	if(!search_text.equals(""))
    	        	{
    	        		for(int i = 0;i<Rooms.length;i++)
    	        			Rooms[i].selection = false;
    	        		selectedRoom = -1;
    	        		for (int i=0;i<Rooms.length;i++)
    	        		{
    	        			if(Rooms[i].belongsTo.toLowerCase().contains(search_text))
    	        				Rooms[i].selection = true;
    	        		}
    	        		m.invalidate();
    	        	}
    	        }
        		
        		});*/
        		break;
	        case MENU_SEARCH:
	        	AlertDialog.Builder search_dialog = new AlertDialog.Builder(this);
	        	search_layout = View.inflate(this, R.layout.search, null);
	    	    search_dialog.setPositiveButton("Search", new Dialog.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) {
	    	        	String search_text = ((EditText) search_layout.findViewById(R.id.txt_note)).getText().toString().trim().toLowerCase();
	    	        	if(!search_text.equals(""))
	    	        	{
	    	        		for(int i = 0;i<Rooms.length;i++)
	    	        			Rooms[i].selection = false;
	    	        		selectedRoom = -1;
	    	        		for (int i=0;i<Rooms.length;i++)
	    	        		{
	    	        			if(Rooms[i].belongsTo.toLowerCase().contains(search_text))
	    	        				Rooms[i].selection = true;
	    	        		}
	    	        		m.invalidate();
	    	        	}
	    	        }
	    	    });
	    	    search_dialog.setNegativeButton("Cancel", new Dialog.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) {	        	           
	    	        }
	    	        
	    	    });
	    	    //savedText = ((EditText) layout.findViewById(R.id.txt_note));
	        	search_dialog.setView(search_layout);
	        	search_dialog.show();
	        	break;
	        case MENU_SELECTUSER:
	        	AlertDialog.Builder user_dialog = new AlertDialog.Builder(this);
	        	user_layout = View.inflate(this, R.layout.userprofile, null);
	        	switch (usertype) {
	        	  case USER_BLIND:
	        	  ((RadioButton)user_layout.findViewById(R.id.blinduser)).setChecked(true);
	        	                   	              break;
	        	  case USER_DEAF:
	        	  ((RadioButton)user_layout.findViewById(R.id.deafuser)).setChecked(true);
	        			                      break;
	        	  case USER_NORMAL:
	        	  ((RadioButton)user_layout.findViewById(R.id.normaluser)).setChecked(true);
	        			                      break;
	        	}
	        	user_dialog.setPositiveButton("Select", new Dialog.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) {
	    	        	int checkedRadioButton = ((RadioGroup) user_layout.findViewById(R.id.user_profile)).getCheckedRadioButtonId();
	    	        	 
	    	        	
	    	        	if(checkedRadioButton==R.id.blinduser) 
	    	        	{
	    	        		usertype = USER_BLIND;
	    	        	  ((RadioButton)user_layout.findViewById(R.id.blinduser)).setChecked(true);
	    	        	}
	    	        	    
	    	        	  if(checkedRadioButton==R.id.deafuser)
	    	        	  {
	    	        		  usertype = USER_DEAF;
	    	        	  ((RadioButton)user_layout.findViewById(R.id.deafuser)).setChecked(true);
	    	        	  }
	    	        		
	    	        	  if(checkedRadioButton==R.id.normaluser)
	    	        	  {
	    	        		  usertype = USER_NORMAL;
	    	        	  ((RadioButton)user_layout.findViewById(R.id.normaluser)).setChecked(true);
	    	        	  }
	    	        		
	    	        	
	    	           
	    	        }
	    	    });
	        	user_dialog.setView(user_layout);
	        	user_dialog.show();
	        	break;
	        case MENU_SELECTENVIRONMENT:
	        	AlertDialog.Builder env_dialog = new AlertDialog.Builder(this);
	        	env_layout = View.inflate(this, R.layout.environmenttype, null);
	        	switch (envtype) {
	        	  case ENV_LESSNOISY:
	        	  ((RadioButton)env_layout.findViewById(R.id.less_noisy)).setChecked(true);
	        	                   	              break;
	        	  case ENV_QUIET:
	        	  ((RadioButton)env_layout.findViewById(R.id.quiet)).setChecked(true);
	        			                      break;
	        	  case ENV_VERYNOISY:
	        	  ((RadioButton)env_layout.findViewById(R.id.very_noisy)).setChecked(true);
	        			                      break;
	        	}
        	    env_dialog.setPositiveButton("Select", new Dialog.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) {
	    	        	int checkedRadioButton = ((RadioGroup) env_layout.findViewById(R.id.environment_type)).getCheckedRadioButtonId();
	    	        	if(checkedRadioButton==R.id.less_noisy)
	    	        	{
	    	        	  envtype = ENV_LESSNOISY;
	    	        	  ((RadioButton)env_layout.findViewById(R.id.less_noisy)).setChecked(true);
	    	        	}
	    	        	if(checkedRadioButton==R.id.quiet)
	    	        	{
	    	        		envtype = ENV_QUIET;
	    	        		((RadioButton)env_layout.findViewById(R.id.quiet)).setChecked(true);
	    	        	}
	    	        	if(checkedRadioButton==R.id.very_noisy)
	    	        	{
	    	        		envtype = ENV_VERYNOISY;
	    	        		((RadioButton)env_layout.findViewById(R.id.very_noisy)).setChecked(true);
	    	        	}

	    	        }
	    	    });
	        	env_dialog.setView(env_layout);
	        	env_dialog.show();
	        	break;

        }
        return true;
	}
	
	boolean loadRoomInfo(boolean[][] roomGRID,String[][] roomNames)
	{
		//Log.d("End", screenWidth + " "  + screenHeight);
		int gap = 15; //pixels
		int height = (screenHeight - 2*gap)/9;
		int width = (screenWidth- 2*gap)/11;
		int noOfRooms= 0;
		for (int i=0;i<9;i++)
		{
			for (int j=0;j<11;j++)
			{
				if(roomGRID[i][j]==true)
					noOfRooms++;
			}
		}
		Rooms = new Room[noOfRooms];
		noOfRooms = 0;
		int left = gap;
		for (int i=0;i<9;i++)
		{
			int top = gap;
			for (int j=0;j<11;j++)
			{
				if(roomGRID[i][j]==true)
				{
					Rooms[noOfRooms] = new Room(noOfRooms,roomNames[i][j],true,top,left,top+width,left+height);
					Rooms[noOfRooms++].loadSchedule();
				}
				top = top + width;
			}
			left = left + height;
		}
		return true; 
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}*/

	public class DrawMap extends View {

		Toast toast;
	    Paint paint = new Paint();
	    
	    public DrawMap(Context context) {
	        super(context); 
	        toast = new Toast(context);
	        toast.setGravity(Gravity.CENTER, screenWidth/2,screenHeight/2);
	        
	    }

	    public boolean onTouchEvent(MotionEvent event) {
			float lastX = event.getX();
			float lastY = event.getY();
			//Log.d("End", selectedRoom + " " + lastY);
			//Log.d("End", lastX + " " + lastY);
			switch(event.getAction()) {
				//Action started
				case MotionEvent.ACTION_DOWN:
				//case MotionEvent.ACTION_MOVE:
					int i;
					for(i = 0;i<Rooms.length;i++)
					{
						if(Rooms[i].contains((int)lastX, (int)lastY))
						{
							if(selectedRoom!=-1)
								Rooms[selectedRoom].selection = false;
							Rooms[i].selection = true;
							selectedRoom = i;
							invalidate();
							 String queryString =
			   	            "prefix table:<http://www.semanticweb.org/ontologies/2013/1/Ontology1361806230149.owl#>"+     
			   	            "select ?output where {table:" + user_and_env[envtype] + " table:hasOutputModality ?output. table:" + user_and_env[usertype] + " table:hasOutputModality ?output}";
							 Query query = QueryFactory.create(queryString);
			        		QueryExecution qe = QueryExecutionFactory.create(query, model);
			        		ResultSet resul =  qe.execSelect();
			        		ByteArrayOutputStream sos = new ByteArrayOutputStream();
			        		ResultSetFormatter.out(sos, resul, query);
			      	    	String str = sos.toString();
			      	    	if(str.contains("HighlightedText_Output"))
			      	    	{
			      	    		paint.setColor(Color.WHITE);
								toast.setGravity(Gravity.CENTER, screenWidth/2,screenHeight/2);
								toast = Toast.makeText(getApplicationContext(), Rooms[i].belongsTo,Toast.LENGTH_SHORT);
								toast.show();
			      	    	}
			      	    	else
			      	    	{
				      	    	if(str.contains("LoudSpeech_Output"))
				      	    	{
				      	    		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				      	    		int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				      	    		am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
				      	    		myTTS.speak( "Room " + Rooms[i].no  + "belongs to " + Rooms[i].belongsTo + "\n", TextToSpeech.QUEUE_FLUSH, null);
				      	    	}
				      	    	if(str.contains("NormalSpeech_Output"))
				      	    	{
				      	    		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				      	    		int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				      	    		am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol/3, 0);
				      	    		myTTS.speak( "Room " + Rooms[i].no  + "belongs to " + Rooms[i].belongsTo + "\n", TextToSpeech.QUEUE_FLUSH, null);
				      	    	}
			      	    	}
			      	    	if(str.contains("Vibration_Output"))
			      	    	{
			      	    		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			      	    		 
			      	    		// Vibrate for 300 milliseconds
			      	    		v.vibrate(800);
			      	    		
			      	    	}
							
							//Log.d("End", selectedRoom + " " );
							break;
						}
					}
					if(i==Rooms.length)
					{
						for(i = 0;i<Rooms.length;i++)
							Rooms[i].selection = false;
						invalidate();
						selectedRoom = -1;
					}
					//highlight = true;
					break;
				case MotionEvent.ACTION_UP:
					//highlight = false;
				//case MotionEvent.ACTION_CANCEL:	
			}
		return true;
	}
	    @Override
	    public void onDraw(Canvas canvas) {
	    	paint.setStyle(Style.FILL_AND_STROKE);
	        paint.setColor(Color.WHITE);
	        paint.setStrokeWidth(2);
	        canvas.drawRect(0.0f,0.0f,screenWidth,screenHeight,paint);
	        paint.setStyle(Style.STROKE);
	        paint.setColor(Color.BLACK);
	        paint.setStrokeWidth(2);
	        for (int i = 0; i<Rooms.length;i++)
	        {
	        	if(Rooms[i].selection==true)
	        	{
	        		paint.setStyle(Style.FILL_AND_STROKE);
	    	        paint.setColor(Color.BLACK);
	        	}
	        	canvas.drawRect(Rooms[i].dimensions, paint);
	        	if(Rooms[i].selection==true)
	        	{
	        		paint.setStyle(Style.FILL_AND_STROKE);
	    	        paint.setColor(Color.WHITE);
	        	}
	        	paint.setStrokeWidth(1);
	        	canvas.drawText(Integer.toString(Rooms[i].no), Rooms[i].dimensions.centerX(),Rooms[i].dimensions.centerY(),paint);
	        	//canvas.drawRect(i*screenWidth/noOfRooms, (i+1)*screenWidth/noOfRooms, screenWidth/noOfRooms, 60, paint);
	        	paint.setStyle(Style.STROKE);
		        paint.setColor(Color.BLACK);
		        paint.setStrokeWidth(2);
	        }
	        	
	        /*canvas.drawRect(30, 30, 80, 80, paint);
	        paint.setStrokeWidth(0);
	        paint.setColor(Color.CYAN);
	        canvas.drawRect(33, 60, 77, 77, paint );
	        paint.setColor(Color.YELLOW);
	        canvas.drawRect(33, 33, 77, 60, paint );*/

	    }

	};
}