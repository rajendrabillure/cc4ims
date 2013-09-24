package demokritos.mnlab.geryoncontrolroom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.util.Log;

class PresenceObject{
	private String user;
	private String longtitude;
	private String latitude;
	private String organization;
	private String oclass;
	private String note;
	private String status;
	private String timestamp;
	private String radius;
	
	public PresenceObject(String u,String l1,String l2, String orga, String oc, String n, String s, String ts, String ra){
		user=u;
		longtitude=l1;
		latitude=l2;
		organization=orga;
		oclass=oc;
		note=n;
		status=s;
		timestamp=ts;
		radius=ra;
	}
	
	public String getUser(){
		return user;
	}
	public String getLongtitude(){
		return longtitude;
	}
	public String getLatitude(){
		return latitude;
	}
	public String getOrganization(){
		return organization;
	}
	public String getOclass(){
		return oclass;
	}
	public String getNote(){
		return note;
	}
	public String getStatus(){
		return status;
	}
	public String getTimestamp(){
		return timestamp;
	}
	public String getRadius(){
		return radius;
	}
	
	public String toString(){
		return user+" - "+longtitude+" - "+latitude+" - "+organization+" - "+oclass+" - "+note+" - "+status+" - "+timestamp+" - "+radius; 
	}
	
}

public class PresenceInfo{
	
	public static HashMap<String,PresenceObject> pi = new HashMap<String,PresenceObject>();

	public static void printAllPresenceData(){
		Set set = pi.entrySet(); 
		Iterator i = set.iterator();  
		while(i.hasNext()) { 
		Map.Entry me = (Map.Entry)i.next(); 
		Log.v("Entry:\n",me.getKey() + " -> "+me.getValue().toString());
		} 
	}
	

	
}