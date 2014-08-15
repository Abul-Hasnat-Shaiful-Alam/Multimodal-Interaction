package com.example.buildingguide;

import android.graphics.Rect;
import android.text.format.Time;
import android.util.Log;

public class Room {
	int no;
	String belongsTo;
	boolean access;
	Schedule[] todaySchedule;
	Rect dimensions;
	boolean selection;
	public Room(int a, String b, boolean c,int left,int top,int right, int bottom)
	{
		no = a;
		belongsTo = b;
		access = c;
		dimensions = new Rect(left, top ,right, bottom);
		selection=false;
		todaySchedule = null;
	}
	//find if a point is inside the rectangle
	public boolean contains(int x, int y)
	{
		return dimensions.contains(x, y);
	}
	
	public String currentActivity(Time t)
	{
		if(todaySchedule==null)
			return "";
		for(int i = 0; i<todaySchedule.length;i++)
		{
			if(t.before(todaySchedule[i].end) && t.after(todaySchedule[i].start))
				return todaySchedule[i].description; 
		}
		return "";
	}
	
	public boolean loadSchedule()
	{
		double rand;
		if(belongsTo.equals("Printer Room"))
		{
			//Log.d("ed", "Printer Room");
			todaySchedule = new Schedule[1];
			todaySchedule[0] = new Schedule(8,20,"Open"); 
			return true;
		}
		if(belongsTo.equals("Lecture Room") || belongsTo.equals("Computer Lab"))
		{
			//Log.d("ed", "Class");
			int i = -1;
			rand = Math.random();
			for(double j = (1.0/3.0);j<1.0;j= j+(1.0/3.0))
				i++;
			if(i>0)
			{
				todaySchedule = new Schedule[i];
				for (int j=0;j<i;j++)
				{
					char name = (char)('A' + ((int)(26*Math.random())));
					todaySchedule[j] = new Schedule((9 + j*5),12 + j*5,"Course " + name);			
				}
			}
			return true;
		}
		if(belongsTo.equals("Meeting Room"))
		{
			//Log.d("ed", "Meeting Room");
			int i = -1;
			rand = Math.random();
			for(double j = (1.0/5.0);j<1.0;j= j+(1.0/5.0))
				i++;
			if(i>0)
			{
				todaySchedule = new Schedule[i];
				int s = 8;
				for (int j=0;j<i;j++)
				{
					char name = (char)('A' + ((int)(26*Math.random())));
					todaySchedule[j] = new Schedule(s, s + 2,"Meeting: Project - " + name);
					if(s==10)
						s = s + 4;
					else
						s = s + 2;
				}
			}
			return true;
		}
		rand = Math.random();
		if(rand<0.8)
		{
			//Log.d("ed", "Else");
			todaySchedule = new Schedule[2];
			todaySchedule[0] = new Schedule(9 ,12 ,"In the Office");
			todaySchedule[1] = new Schedule(14 ,18 ,"In the Office");
		}
		return true;
	}
	
	public String returnSchedule()
	{
		String schedule = "";
		if(todaySchedule!=null)
		{
			for (int i=0;i<todaySchedule.length;i++)
				schedule = schedule + "From " + todaySchedule[i].start.hour + "H00 to " + todaySchedule[i].end.hour + "H00        " + todaySchedule[i].description + "\n";
		}
		if(schedule.equals(""))
			schedule = "Schedule not available";
		schedule = "Room number " + no + "\n" + schedule;
		return schedule;
	}
	public class Schedule
	{
		Time start;
		Time end;
		String description;
		public Schedule(int s,int e,String d)
		{
			start = new Time();
			start.hour = s;
			end = new Time();
			end.hour = e;
			description = d;
		}
	}

}
