package demokritos.mnlab.geryoncontrolroom;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.events.NgnMsrpEventArgs;
import org.doubango.ngn.events.NgnMsrpEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.sip.NgnSubscriptionSession.EventPackageType;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;



public class ControlRoomActivity extends Activity {

	private static String TAG = ControlRoomActivity.class.getCanonicalName();
	private  NgnEngine mEngine;
    private  INgnConfigurationService mConfigurationService;
    private  INgnSipService mSipService;
    private BroadcastReceiver mSipBroadCastRecv;
	
    private LayoutInflater mInflater;		//video
    private static int mLastRotation; // values: degrees
    private final NgnTimer mTimerInCall;
    private final NgnTimer mTimerBlankPacket;
    private final NgnTimer mTimerSuicide;
    private TextView mTvDuration;
    private RelativeLayout mMainLayout;		//video
    private RelativeLayout mCallInfoLayout;
    private RelativeLayout mPersonInfoLayout;
    private TextView mTvRemote;
    private Button mBtSubscribe;
 
    private final static int MENU_OPEN_CALL = 0;
    private final static int MENU_HANGUP_CALL = 1;
    private final static int MENU_HOLD_CALL = 2;
    private final static int MENU_RESUME_CALL = 3;
    
    private LocationManager locationManager;
    private LocationListener locationListener;

    static private NgnObservableHashMap<Long, NgnAVSession> sessions;

    private NgnAVSession mSession;
    private FrameLayout mViewLocalVideoPreview;
    private FrameLayout mViewRemoteVideoPreview;
    private View mViewInCallVideo;
    private View mViewCallInfo;
    private View mViewPersonInfo;
    private String logoPath = "file:///android_asset/glogosuper.gif";

    private WebView myWebView;
    
    private ListView mListView;
    private TextView mTvLog;
    private ScreenAVQueueAdapter mAdapter;
    private long avsid=-1;
    private String avsids="-1";

    public final static String EXTRAT_SIP_SESSION_ID = "SipSession";
    
      		
    private GoogleMap mMap;
    Marker baseLoc;
    private Timer mTimer;
    private static HashMap<String,Marker> mm = new HashMap<String,Marker>();
    
    
    
    private ProgressBar mProgressBar;
    private TextView mTvByteRange;
    public NgnMsrpSession msrpSession;
    private Button mBtAccept;
    private Button mBtAbort;
    private Button mBtSendFile;
    private String mStringFormat;
    private EditText mEtCompose;
    
    
    private TextView mChat;
    private ScrollView mScrollM;
    
    private Button mBtSendMessage;
    
    
    public static Activity cra;

    
	public ControlRoomActivity(){
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
		
		cra = this;
		
		mTimerInCall = new NgnTimer();			//video	
		mTimerSuicide = new NgnTimer();
		mTimerBlankPacket = new NgnTimer();	//video
		
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control_room);		
		
		
		
		mProgressBar = (ProgressBar) findViewById(R.id.screen_filetrans_view_progressBar);
		mTvByteRange = (TextView) findViewById(R.id.screen_filetrans_view_textView_byteRange);
		mBtAccept = (Button) findViewById(R.id.screen_filetrans_view_button_accept);
		mBtAbort = (Button) findViewById(R.id.screen_filetrans_view_button_abort);
		mBtSendFile = (Button) findViewById(R.id.screen_filetrans_view_button_sendfile);
		
		
		mChat  = (TextView) findViewById(R.id.screen_msrp_chat_show);
		mScrollM = (ScrollView) findViewById(R.id.screen_msrp_scroll);
		
		
		mBtAccept.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
				if (msrpSession!=null){
					msrpSession.accept();
				}
				}catch(Exception e666){
					Log.e(TAG,"Execute Order 666");
					e666.printStackTrace();
				}
			}
		});
		
		mBtAbort.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
				if(msrpSession!=null){
					msrpSession.hangUp();
					//msrpSession = null;
				}
				}catch(Exception e666){
					Log.e(TAG,"Execute Order 666.");
					e666.printStackTrace();
				}
			}
		});
		mBtSendFile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//sendFile("alice@ims.geryon.gr",mEngine.getStorageService().getContentShareDir()+"/savasa-chris.txt");
				//sendFile("alice@sec-geryon.eu",mEngine.getStorageService().getContentShareDir()+"/savasa-chris.txt");
				//Toast.makeText(getApplicationContext(), "Not yet functional", Toast.LENGTH_SHORT).show();
				Toast.makeText(getApplicationContext(), "not in ims.geryon.gr->"+msrpSession.getId(), Toast.LENGTH_SHORT).show();
				
				
			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		mEtCompose = (EditText) findViewById(R.id.screen_chat_editText_compose);
		mBtSendMessage = (Button) findViewById(R.id.screen_filetrans_view_button_sendmessage);
		
		mBtSendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//sendMessage("alice@ims.geryon.gr");
				Log.v(TAG,"Send message to alice@organization2.org");
				//sendMessage("alice@sec-geryon.eu");
				sendMessage("alice@organization2.org");
				//Toast.makeText(getApplicationContext(), "Not yet functional", Toast.LENGTH_SHORT).show();
				Log.v(TAG,"Send message to alice@organization2.org");
			}
		});
		
		
		
		
		
		
		
		
		
		 mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			    @Override
			    public void run() {

			    	Log.v(TAG,"Updating Map Markers.");
			    	runOnUiThread(new Runnable() {
			    	    public void run() {
			    	    	if (PresenceInfo.pi.size()>0){
					    		Set set = PresenceInfo.pi.entrySet(); 
					    		Iterator i = set.iterator(); 
					    		
					    		while(i.hasNext()) { 
					    			Map.Entry me = (Map.Entry)i.next(); 
					    			if ( mm.get( me.getKey().toString() ) == null){
					    				Log.v(TAG,"!!!!!PUTTING IN MAP->"+PresenceInfo.pi.get(me.getKey())+"   LAT->"+PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()+"     LONG->"+PresenceInfo.pi.get(me.getKey().toString()).getLatitude());
					    				
					    				if (PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()=="" || PresenceInfo.pi.get(me.getKey().toString()).getLatitude()==""){
					    				
					    					Log.e(TAG,"NO GPS DATA FOR THIS USER");
					    					
					    				}else{
					    					
					    					
					    				if ( PresenceInfo.pi.get(me.getKey().toString()).getOrganization().equals("organization1") ){
					    				mm.put(me.getKey().toString(), mMap.addMarker(new MarkerOptions()
					    					.position(new LatLng(Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()),Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLatitude())))
					    					.title(PresenceInfo.pi.get(me.getKey().toString()).getUser()+" - "+PresenceInfo.pi.get(me.getKey().toString()).getOrganization())
					    					.snippet(me.getKey().toString())
					    					.icon(BitmapDescriptorFactory.fromResource(R.drawable.firefightericon)) ));
					    				}else if ( PresenceInfo.pi.get(me.getKey().toString()).getOrganization().equals("organisation3") ){
					    					mm.put(me.getKey().toString(), mMap.addMarker(new MarkerOptions()
					    					.position(new LatLng(Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()),Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLatitude())))
					    					.title(PresenceInfo.pi.get(me.getKey().toString()).getUser()+" - "+PresenceInfo.pi.get(me.getKey().toString()).getOrganization())
					    					.snippet(me.getKey().toString())
					    					.icon(BitmapDescriptorFactory.fromResource(R.drawable.policeicon)) ));
					    				
					    				}else if ( PresenceInfo.pi.get(me.getKey().toString()).getOrganization().equals("organisation4") ){
					    					mm.put(me.getKey().toString(), mMap.addMarker(new MarkerOptions()
					    					.position(new LatLng(Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()),Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLatitude())))
					    					.title(PresenceInfo.pi.get(me.getKey().toString()).getUser()+" - "+PresenceInfo.pi.get(me.getKey().toString()).getOrganization())
					    					.snippet(me.getKey().toString())
					    					.icon(BitmapDescriptorFactory.fromResource(R.drawable.doctoricon)) ));
					    				
					    				}else{
					    					mm.put(me.getKey().toString(), mMap.addMarker(new MarkerOptions()
					    					.position(new LatLng(Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()),Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLatitude())))
					    					.title(PresenceInfo.pi.get(me.getKey().toString()).getUser()+" - "+PresenceInfo.pi.get(me.getKey().toString()).getOrganization())
					    					.snippet(me.getKey().toString()) ));
					    				}
					    				
					    				}
					    				
					    				
					    				//mm.put("test"+counter, mMap.addMarker(new MarkerOptions().position(new LatLng(37.7750+counter, 122.4183)).title("San Francisco")));
					    				
					    			}else{
					    				//update marker
					    				
					    				mm.get(me.getKey().toString()).setPosition(new LatLng(Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()),Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLatitude())));
					    				/*
					    				mm.get(me.getKey().toString()).remove();
					    				mm.remove(me.getKey().toString());
					    				mm.put(me.getKey().toString(), mMap.addMarker(new MarkerOptions()
				    					.position(new LatLng(Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLongtitude()),Double.parseDouble(PresenceInfo.pi.get(me.getKey().toString()).getLatitude())))
				    					.title(PresenceInfo.pi.get(me.getKey().toString()).getUser()+" - "+PresenceInfo.pi.get(me.getKey().toString()).getOrganization())
				    					.snippet(me.getKey().toString()) ));
					    				*/
					    				Log.v(TAG,"updating map marker");
					    			}
					    			
					    			//Log.v(TAG, "KONTEUEI:D-> "+me.getValue().toString());
					    			
					    		}
					    	}
			    	    }
			    	});
			    	
			    	
			    	
			         }
			    }, 0, 10000);
		
		
		
		
		mInflater = LayoutInflater.from(this);
		mMainLayout = (RelativeLayout)findViewById(R.id.screen_av_relativeLayout);
		mViewInCallVideo = mInflater.inflate(R.layout.logotop, null);
		mMainLayout.removeAllViews();
	    mMainLayout.addView(mViewInCallVideo);
		myWebView = (WebView) findViewById(R.id.logotopleft);
		myWebView.loadUrl(logoPath);
		
		mCallInfoLayout = (RelativeLayout)findViewById(R.id.second_column_top_left);
		
		mPersonInfoLayout = (RelativeLayout)findViewById(R.id.second_column_top_right);
		mViewPersonInfo = mInflater.inflate(R.layout.personinfo, mPersonInfoLayout, false);
		mPersonInfoLayout.removeAllViews();
		
		
		//animation of top right, whole thing
		AnimationSet set4 = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		set4.addAnimation(animation);
		animation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
		);
		animation.setDuration(3000);
		set4.addAnimation(animation);
		LayoutAnimationController controller3 = new LayoutAnimationController(set4,0.25f);
		RelativeLayout testrinefe3 = (RelativeLayout)findViewById(R.id.second_column_top);
		testrinefe3.setLayoutAnimation(controller3);
		
		
		
		//animation of left column
		AnimationSet set3 = new AnimationSet(true);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		set3.addAnimation(animation);
		animation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
		);
		animation.setDuration(5000);
		set3.addAnimation(animation);
		LayoutAnimationController controller = new LayoutAnimationController(set3,0.25f);
		RelativeLayout testrinefe = (RelativeLayout)findViewById(R.id.first_column_top);
		testrinefe.setLayoutAnimation(controller);
				
		//animation of the map
		AnimationSet set2 = new AnimationSet(true);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setStartOffset(5000);
		animation.setDuration(100);
		set2.addAnimation(animation);
		animation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
		);
		animation.setStartOffset(5000);
		animation.setDuration(4000);
		set2.addAnimation(animation);
		LayoutAnimationController controller2 = new LayoutAnimationController(set2,0.25f);
		RelativeLayout testrinefe2 = (RelativeLayout)findViewById(R.id.second_column_bottom);
		testrinefe2.setLayoutAnimation(controller2);
				
	  	mAdapter = new ScreenAVQueueAdapter(this);
	  	mListView = (ListView)findViewById(R.id.screen_av_queue_listView); 
	  	
	  	registerForContextMenu(mListView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		final NgnAVSession session = (NgnAVSession)mAdapter.getItem(position);
                if(session != null){
                	resumeAVSession(session);
                }
 			}
 		});
 	        
         
        //************************************ MAP INITIALIZATION**********************************************//
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
        	public void onLocationChanged(Location location) {
        		Log.v(TAG,"LocalLoc Update");
        		// Called when a new location is found by the network location provider.
        	    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 15));
        	    
        		//Toast.makeText(getApplicationContext(), "Refreshing location of control center.", Toast.LENGTH_SHORT).show();
        	    baseLoc.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        	}
            
        	public void onStatusChanged(String provider, int status, Bundle extras) {}
        	public void onProviderEnabled(String provider) {}
       	    public void onProviderDisabled(String provider) {}
        };
        	  
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, locationListener);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        // Or use LocationManager.GPS_PROVIDER
      	Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
       	
        setUpMapIfNeeded();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()), 10));
        // mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11), 5000, null);
        mMap.setOnMarkerClickListener(new OnMarkerClickListener(){

			@Override
			public boolean onMarkerClick(Marker marker) {
				Log.v(TAG,"Overriding default map marker click behaviour.");
				showPersonInfo(marker.getSnippet());
				String shelper = marker.getSnippet();
				marker.setSnippet("");
				marker.showInfoWindow();
				marker.setSnippet(shelper);
				return true;
			}
			
        });
        
        
        baseLoc = mMap.addMarker(new MarkerOptions()
        	.position(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()))
        	.title("GERYON TACTICAL BASE")
        	.icon(BitmapDescriptorFactory.fromResource(R.drawable.sfglogo)));
         
        //************************************ MAP INITIALIZATION END******************************************//
         
        
    
         
        
        
        mSipBroadCastRecv = new BroadcastReceiver() {
        	@Override
            public void onReceive(Context context, Intent intent) {
        		if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(intent.getAction())){
        			Log.d(TAG,"BroadcastReceiver: ACTION_INVITE_EVENT");
        			handleSipEvent(intent);
              	}else if(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(intent.getAction())){  //video
              		Log.d(TAG,"BroadcastReceiver: ACTION_MEDIA_PLUGIN_EVENT");
              		handleMediaEvent(intent);															//video
              	}else if(NgnMsrpEventArgs.ACTION_MSRP_EVENT.equals(intent.getAction())){
              		Log.d(TAG,"BroadcastReceiver: ACTION_MSRP_EVENT");
              		handleMsrpEvent(intent);
              	}
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
        intentFilter.addAction(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT);//video
        intentFilter.addAction(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
        registerReceiver(mSipBroadCastRecv, intentFilter);
	}
	
	//************************************************************************************************* CALL LIST QUEUE
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, MENU_OPEN_CALL, Menu.NONE, "Open");
		menu.add(0, MENU_HANGUP_CALL, Menu.NONE, "Hang Up");
		menu.add(0, MENU_HOLD_CALL, Menu.NONE, "Hold");
		menu.add(0, MENU_RESUME_CALL, Menu.NONE, "Resume");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	final NgnAVSession session;
		final int location = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		if ((session = (NgnAVSession) mAdapter.getItem(location)) == null) {
			return super.onContextItemSelected(item);
		}
		switch (item.getItemId()) {
			case MENU_HOLD_CALL:
				Toast.makeText(getApplicationContext(), "HOLD CALL", Toast.LENGTH_SHORT).show();
				session.holdCall();
				session.setLocalHold(true);
				return true;
			case MENU_RESUME_CALL:
				Toast.makeText(getApplicationContext(), "RESUME CALL", Toast.LENGTH_SHORT).show();
				final NgnAVSession activeSession = NgnAVSession.getFirstActiveCallAndNot(session.getId());
		        if(activeSession != null){
		        	activeSession.holdCall();
		            Toast.makeText(getApplicationContext(), "You have to hold the active call first.", Toast.LENGTH_SHORT).show();
		            if(session.isLocalHeld()){
		            	
		            	session.resumeCall();
		            	session.setLocalHold(false);
		        	}else{
		        		session.acceptCall();
		        	}
		        }else{
		        // Resume the selected call and display it to the screen
		        //mScreenService.show(ScreenAV.class, Long.toString(session.getId()));
		        	if(session.isLocalHeld()){
		            	session.resumeCall();
		            	session.setLocalHold(false);
		        	}else{
		        		session.acceptCall();
		        	}
		        }
				return true;
			case MENU_OPEN_CALL:
				resumeAVSession(session);
				return true;
			case ControlRoomActivity.MENU_HANGUP_CALL:
				session.hangUpCall();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
    }
    
    public boolean hasMenu(){
        return true;
    }
    
    public boolean hasBack(){
		return true;
	}
    
    private void resumeAVSession(NgnAVSession session){
		Log.v(TAG,"resumeAVSession, sessionID: "+session.getId());
		AnimationSet set5 = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		set5.addAnimation(animation);
		animation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
		);
		animation.setDuration(3000);
		set5.addAnimation(animation);  
		mViewCallInfo = mInflater.inflate(R.layout.callinfo,null);
  		mCallInfoLayout.removeAllViews();
  		mCallInfoLayout.setAnimation(set5);
  		mCallInfoLayout.startAnimation(set5);
		mCallInfoLayout.addView(mViewCallInfo);
  		
		Button mBtHangUpCallScreen = (Button)findViewById(R.id.callinfo_button_hangup);
        mBtHangUpCallScreen.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if(mSession != null){
        			Log.d(TAG,"HangUp Call"+ mSession.hangUpCall());
        		}
            }
        });
        
        Button mBtTransferCall = (Button)findViewById(R.id.callinfo_button_transfercall);
        mBtTransferCall.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if (mSession != null){
        			if (mSession.isLocalHeld()){
        				Log.d(TAG,"TRANSFERRRRROOOOOOOOCALLLLLLLLLLLLLLL1111111111111111"+"sip:bob@organization2.org"+"ID="+mSession.getSessionTransferId());
        				mSession.TransferCall("sip:bob@organization2.org");
        			}else{
        				// FIXME: for now doubango will not send Hold prior to transfer
        				setIsTransfering(true);
        				//mSession.holdCall();
        				mSession.TransferCall("sip:bob@organization2.org");
        				Log.v(TAG,"TRANSFERRRRROOOOOOOOCALLLLLLLLLLLLLLL22222222222222222"+"sip:bob@organization2.org"+"ID="+mSession.getSessionTransferId());
        			}
        			
        		}
        	}
        });
        
        mSession = session;
		final NgnAVSession activeSession = NgnAVSession.getFirstActiveCallAndNot(session.getId());
        if(activeSession != null){
        	activeSession.holdCall();
            Toast.makeText(getApplicationContext(), "You have to hold the active call first.", Toast.LENGTH_SHORT).show();
            if(session.isLocalHeld()){
            	session.resumeCall();
        	}else{
        		session.acceptCall();
        	}
        }else{
        // Resume the selected call and display it to the screen
        //mScreenService.show(ScreenAV.class, Long.toString(session.getId()));
        	if(session.isLocalHeld()){
            	session.resumeCall();
        	}else{
        		session.acceptCall();
        	}
        }
        
	}

    
    public class ScreenAVQueueAdapter extends BaseAdapter implements Observer { 
    	private NgnObservableHashMap<Long, NgnAVSession> mAVSessions;
        private final LayoutInflater mInflater;
        private final Handler mHandler;
        ScreenAVQueueAdapter(Context context) {
        	mHandler = new Handler();
            mInflater = LayoutInflater.from(context);
            mAVSessions = NgnAVSession.getSessions();
            mAVSessions.addObserver(this);
        }
        
        @Override
        public int getCount() {
        	return mAVSessions.size();
        }
        
        @Override
        public Object getItem(int position) {
        	return mAVSessions.getAt(position);
        }
        
        @Override
        public long getItemId(int position) {
        	return position;
        }
                
        @Override
        public void update(Observable observable, Object data) {
        	mAVSessions = NgnAVSession.getSessions();
            if(Thread.currentThread() == Looper.getMainLooper().getThread()){
            	notifyDataSetChanged();
            }else{
            	mHandler.post(new Runnable(){
            		@Override
                    public void run() {
            			notifyDataSetChanged();
                        Log.d(TAG,"CallList======First Visible:"+mListView.getFirstVisiblePosition()+"===========Count:"+mListView.getCount()+"=========");
                    }
                });
            }
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {    
        	View view = convertView;
            NgnAVSession session;
            if (view == null) {
            	view = mInflater.inflate(R.layout.screen_av_queue_item, null);
            }
            session = (NgnAVSession)getItem(position);
            if(session != null){
            	final ImageView imageView = (ImageView) view.findViewById(R.id.screen_av_queue_item_imageView);
                final TextView tvRemoteParty = (TextView) view.findViewById(R.id.screen_av_queue_item_textView_remote);
                final TextView tvInfo = (TextView) view.findViewById(R.id.screen_av_queue_item_textView_info);
                if(session.isLocalHeld() || session.isRemoteHeld()){
                	imageView.setImageResource(R.drawable.phone_hold_48);
                    tvInfo.setText("Held");
                }else{
                	imageView.setImageResource(R.drawable.phone_resume_48);
					switch (session.getState()) {
						case INCOMING:
							tvInfo.setText("Incoming");
							break;
						case INPROGRESS:
							tvInfo.setText("In Progress");
							break;
						case INCALL:
						default:
							tvInfo.setText("In Call");
							break;
						case TERMINATED:
							tvInfo.setText("Terminated");
							break;
					}
                }                               
                final String remoteParty = session.getRemotePartyDisplayName();
                if(remoteParty != null){
                    tvRemoteParty.setText(remoteParty);
                }else{
                	tvRemoteParty.setText("Unknown");
                }
            }
            return view;
        }
    }
	

	//************************************************************************************************* CALL LIST QUEUE END
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.controlroommenu, menu);
	    return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    	case R.id.menu_signout:
	    		Toast.makeText(getApplicationContext(), "Disconnected from IMS server.", Toast.LENGTH_LONG).show();
	            Intent intent = new Intent(ControlRoomActivity.this, SignInActivity.class);
	            SignInActivity.sia.finish();
	            startActivity(intent);
	            finish();
	            return true;
	        case R.id.menu_exit:
	        	SignInActivity.sia.finish();
	        	finish();
	        	return true;
	        case R.id.menu_hangupall:
				final NgnObservableHashMap<Long, NgnAVSession> sessions54 = NgnAVSession.getSessions();
				NgnAVSession session;
				for (Map.Entry<Long, NgnAVSession> entry : sessions54.entrySet()) {
					session = entry.getValue();
					if (session.isActive()) {
						session.hangUpCall();
					}
				}
				return true;
	        case R.id.menu_subscribe:
	        	Toast.makeText(getApplicationContext(), "Subscribing to user-groups.", Toast.LENGTH_LONG).show();
       		 	new Thread(new Runnable() {
       			    public void run() {
       			    	
       			    	//NgnSubscriptionSession subPres = new NgnSubscriptionSession(mSipService.getSipStack(),"sip:group1.organization1.org.org@sec-geryon.eu",NgnSubscriptionSession.EventPackageType.Presence,NgnConfigurationEntry.IDENTITY_IMPU);
       	                
       			    	String helpub = mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPU, "error@organization2.org");
       			    	Log.v(TAG,"CONFIGOUOOUOUOUOOU->"+helpub);
       			    	
       			    	
       			    	//NgnSubscriptionSession subPres = new NgnSubscriptionSession(mSipService.getSipStack(),"sip:GeryonGroup1.organization1.org.org@sec-geryon.eu",NgnSubscriptionSession.EventPackageType.Presence, helpub);

       			    	NgnSubscriptionSession subPres = new NgnSubscriptionSession(mSipService.getSipStack(),"GeryonGroup1.organization1.org.org@organization2.org",NgnSubscriptionSession.EventPackageType.Presence, helpub);
       			    	Log.v(TAG, "GERYON group1 organization1 trying to get presence information xmls");
       	                 subPres.subscribe();
       	                 
       	                 //Toast.makeText(getApplicationContext(), "Subscribing to groups...", Toast.LENGTH_SHORT).show();
       	               
       	                 //NgnSubscriptionSession subPres2 = new NgnSubscriptionSession(mSipService.getSipStack(),"sip:GeryonGroup1.organisation3.org.org@sec-geryon.eu",NgnSubscriptionSession.EventPackageType.Presence, helpub);

       	                 NgnSubscriptionSession subPres2 = new NgnSubscriptionSession(mSipService.getSipStack(),"GeryonGroup1.organisation3.org.org@organization2.org",NgnSubscriptionSession.EventPackageType.Presence, helpub);
       	                 Log.v(TAG, "GERYON2 group2 organization2 trying to get presence information xmls");
       	                 subPres2.subscribe();
       	                 
       	                 //Toast.makeText(getApplicationContext(), "Subscribing to group2org2...", Toast.LENGTH_SHORT).show();
       	                 
       	                // NgnSubscriptionSession subPres3 = new NgnSubscriptionSession(mSipService.getSipStack(),"sip:GeryonGroup1.organisation4.org.org@sec-geryon.eu",NgnSubscriptionSession.EventPackageType.Presence, helpub);

       	                 NgnSubscriptionSession subPres3 = new NgnSubscriptionSession(mSipService.getSipStack(),"GeryonGroup1.organisation4.org.org@organization2.org",NgnSubscriptionSession.EventPackageType.Presence, helpub);
       	                 Log.v(TAG, "GERYON3 group3 organization2 trying to get presence information xmls");
       	                 subPres3.subscribe();
       	                 
       	                 //Toast.makeText(getApplicationContext(), "Subscribing to group3org2...", Toast.LENGTH_SHORT).show();
       	                 
       	                 //NgnSubscriptionSession subPres4 = new NgnSubscriptionSession(mSipService.getSipStack(),"sip:group4@sec-geryon.eu",NgnSubscriptionSession.EventPackageType.Presence, helpub);
    	                 //Log.v(TAG, "GERYON4 group4 organization12 trying to get presence information xmls");
    	                 //subPres4.subscribe();
    	                 
    	                 //Toast.makeText(getApplicationContext(), "Subscribing to group3org2...", Toast.LENGTH_SHORT).show();
    	                 
    	                 
       			    }
       			  }).start();
       		 	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
 	    if (mMap == null) {
 	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 	        // Check if we were successful in obtaining the map.
 	        if (mMap != null) {
 	            // The Map is verified. It is now safe to manipulate the map.

 	        }
 	    }
 	}
  
	
	private String getStateDesc(InviteState state){
		switch(state){
        	case NONE:
         	default:
         		return "Unknown";
         	case INCOMING:
         		return "Incoming";
         	case INPROGRESS:
         		return "Inprogress";
            case REMOTE_RINGING:
            	return "Ringing";
            case EARLY_MEDIA:
            	return "Early media";
            case INCALL:
            	return "In Call";
            case TERMINATING:
            	return "Terminating";
            case TERMINATED:
            	return "terminated";
        }
	 }
	
	 
	 private void handleMediaEvent(Intent intent){
        final String action = intent.getAction();
        if(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(action)){
        	NgnMediaPluginEventArgs args = intent.getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
            if(args == null){
            	Log.e(TAG, "Invalid event args");
                	return;
                }
                switch(args.getEventType()){
                case STARTED_OK: //started or restarted (e.g. reINVITE)
                	createVideoFeeds();
                    break;
                case PREPARED_OK:
                case PREPARED_NOK:
                case STARTED_NOK:
                case STOPPED_OK:
                case STOPPED_NOK:
                case PAUSED_OK:
                case PAUSED_NOK:
                	Log.v(TAG,"HandleMediaEvent except STARTED_OK.");
                    break;
            }
        }
	}
	

	private void handleSipEvent(Intent intent){
		
        final String action = intent.getAction();
        Log.v(TAG,"handleSipEvent->Intent.action: "+action);
        if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
        	
        	NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
        	if(args == null){
        		Log.e(TAG, "handleSipEvent: Invalid event args");
        		return;
        	}
        	        	
        	Log.v(TAG, "handleSipEvent->MediaType: "+ args.getMediaType());
        	
        	if (NgnMediaType.isAudioVideoType(args.getMediaType())){
        		if(mSession == null){
        			Log.e(TAG, "handleSipEvent: Invalid session object");
        			return;
        		}
        		if(args.getSessionId() != mSession.getId()){
            		return;
            	}
            final InviteState callState = mSession.getState();
            Log.d(TAG,"handleSipEvent->State of Session: "+mSession.getState());
            switch(callState){
            	case REMOTE_RINGING:
            		mEngine.getSoundService().startRingBackTone();
                    break;
                case INCOMING:
                	mEngine.getSoundService().startRingTone();
                    break;
                case EARLY_MEDIA:
                case INCALL:
                	mEngine.getSoundService().stopRingTone();
                    mEngine.getSoundService().stopRingBackTone();
                    mSession.setSpeakerphoneOn(false);
                    if (NgnMediaType.isVideoType(mSession.getMediaType())){ 
                    	createVideoFeeds();
                        if(mSession != null){
                        	applyCamRotation(mSession.compensCamRotation(true)); 
                        }	
                    }
                    switch(args.getEventType()){
                    	case REMOTE_DEVICE_INFO_CHANGED:
                    		Log.d(TAG, String.format("Remote device info changed: orientation: %s", mSession.getRemoteDeviceInfo().getOrientation()));
                    		break;
                    	case MEDIA_UPDATED:
                    		createVideoFeeds();
                    		break;
                    	default:
                    	break;
                    }
                    break;
                case TERMINATING:
                case TERMINATED:
                	mEngine.getSoundService().stopRingTone();
                    mEngine.getSoundService().stopRingBackTone();
                    AnimationSet set6 = new AnimationSet(true);
                    Animation animation = new TranslateAnimation(
                    	Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    	Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f
                    );
                    animation.setDuration(3000);
                    set6.addAnimation(animation);
                    animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setStartOffset(2900);
                    animation.setDuration(100);
                    set6.addAnimation(animation);
                    mCallInfoLayout.setAnimation(set6);
                    mCallInfoLayout.startAnimation(set6);
                    if (NgnMediaType.isVideoType(mSession.getMediaType())){	  
                    	AnimationSet set7 = new AnimationSet(true);
                    	animation = new AlphaAnimation(0.0f, 1.0f);
                   		animation.setDuration(100);
                   		set7.addAnimation(animation);
                   		animation = new TranslateAnimation(
                   			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                   			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                   		);
                   		animation.setDuration(3000);
                   		set7.addAnimation(animation);
                     	mMainLayout.setAnimation(set7);
                        mMainLayout.startAnimation(set7);
                        mViewInCallVideo = mInflater.inflate(R.layout.logotop, null);
                      	mMainLayout.removeAllViews();
                      	mMainLayout.addView(mViewInCallVideo);
                      	myWebView = (WebView) findViewById(R.id.logotopleft);
                    	myWebView.loadUrl(logoPath);
                    }
                    
                    set6.setAnimationListener(new AnimationListener(){
                    	public void onAnimationEnd(Animation animation) {
                    		mCallInfoLayout.removeView(mViewCallInfo);
                     	}
                    	@Override
						public void onAnimationRepeat(Animation animation) {
                    		// do nothing
						}
						@Override
						public void onAnimationStart(Animation animation) {
							// do nothing
						}
                    });
                    Log.d(TAG,"Session was terminated.");
                    break;
                default:
                	break;
            }
        
          }else if (NgnMediaType.isMsrpType(args.getMediaType())){
        	  if(msrpSession == null){
      			Log.e(TAG, "Invalid session object");
      			return;
      		}
        	  if(args.getSessionId() != msrpSession.getId()){
          		return;
        	  }
        	  final InviteState state = msrpSession.getState();
        	  Log.d(TAG,"handleSipEvent->MSRP session state: "+msrpSession.getState());
        	  Log.v(TAG,"handleSipEvent->MSRP session phrase: "+args.getPhrase());
        	  switch(state){
				case NONE:
				default:
					break;
					
				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					mTvByteRange.setText("Trying...");
					break;
					
				case EARLY_MEDIA:
					break;
				case INCALL:
					mTvByteRange.setText("Connected!");
					break;
					
				case TERMINATING:
				case TERMINATED:
					msrpSession = null;
					mTvByteRange.setText("Terminated!");
					break;
			}
        	  
        	  
        	  
          }
        
        
        }
	}
	
	

private void handleMsrpEvent(Intent intent){
		
        final String action = intent.getAction();
        Log.v(TAG,"DEBUGMSRP ACTION-> "+action);
	
if(NgnMsrpEventArgs.ACTION_MSRP_EVENT.equals(action)){
        	
			final NgnMsrpEventArgs args = intent.getParcelableExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED);
			Log.v(TAG,"Got MSRP Message from remote: "+args.toString());
			Log.v(TAG,"DOKIMI MSRP: "+msrpSession.getMediaType());
			Log.v(TAG,"DOKIMI MSRP uri: "+msrpSession.getRemotePartyUri());

			final NgnMsrpEventTypes type;
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			if(args.getSessionId() != msrpSession.getId()){
				return;
			}

			if (msrpSession.getMediaType().equals("Chat")){
				Log.v(TAG,"EGINE MSRP");
				final byte[]content = intent.getByteArrayExtra(NgnMsrpEventArgs.EXTRA_DATA);
				String helpme="";
				for (int i=0;i<content.length;i++){
					helpme = new String(content,0,content.length);
				}	
				Log.v(TAG,"EGINE MSRP: "+helpme);
				
			}else{
				
				try{
				Log.v(TAG,"DEN EGINE MSRP");
				byte[]content = intent.getByteArrayExtra(NgnMsrpEventArgs.EXTRA_DATA);
				String helpme="";
				helpme = new String(content,0,content.length);
				Log.v(TAG,"DEN EGINE MSRP: "+helpme);
				
				if (!helpme.split(" ")[0].equals("<?xml")){
				
				
				//Log.v(TAG,helpme.split(" ")[0]);
				//Log.v(TAG,helpme.split(" ")[1]);
				mChat.setText(mChat.getText()+"\n"+msrpSession.getRemotePartyDisplayName()+": "+helpme);
				scrollToBottom(10);
				}
				
				
				}catch(Exception exc541){			
					Log.e(TAG,"TI LATHOS EXEI GINEI DIAOLE??? "+exc541.getStackTrace());
				}
				
				
			}
			
			if (msrpSession.isOutgoing()) {
				mStringFormat = "%d/%d Bytes sent";
				
			} else {
				mStringFormat = "%d/%d Bytes received";
				
			}
			//updateProgressBar(msrpSession.getStart(), msrpSession.getEnd(), msrpSession.getTotal());
			
			
			switch((type = args.getEventType())){
				case CONNECTED:
					//mBtAbort.setText("Abort");
                    //mBtAccept.setVisibility(View.GONE);
					break;
				case SUCCESS_2XX:
				case SUCCESS_REPORT:
				case DATA:
					if((msrpSession.isOutgoing() && type == NgnMsrpEventTypes.SUCCESS_2XX) || (!msrpSession.isOutgoing() && type == NgnMsrpEventTypes.DATA)){
                        //updateProgressBar(intent.getLongExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_START, -1L),intent.getLongExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_END, -1L),intent.getLongExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_TOTAL, -1L));
					}
					break;
				case ERROR:
					mTvByteRange.setText("ERROR!");
					break;
				case DISCONNECTED:
					mTvByteRange.setText("Terminated!");
					
					break;
			}
	}
        
        
}
	
	
	private void updateProgressBar(long start, long end, long total){
		if(end >= 0 && total>0 && end<=total){
            mProgressBar.setProgress((int)((100*end)/total));
            mTvByteRange.setText(String.format(mStringFormat, end, total));
            mProgressBar.setIndeterminate(false);
	    }
	    else{
	    	mProgressBar.setIndeterminate(true);
	    }
	}
	
	boolean sendFile(String remoteUri, String filePath){
		mTvByteRange.setText("Trying");
		final String validUri = NgnUriUtils.makeValidSipUri(remoteUri);
		if(validUri == null){
			Log.e(TAG, "failed to normalize sip uri '" + remoteUri + "'");
			return false;
		}
		final NgnMsrpSession msrpSes = NgnMsrpSession.createOutgoingSession(mSipService.getSipStack(), 
				NgnMediaType.FileTransfer, validUri);
		if(msrpSes == null){
			Log.e(TAG,"Failed to create MSRP session");
			return false;
		}
		msrpSession = msrpSes;
		if(msrpSes.sendFile(filePath)){
			Log.v(TAG,"FILE WAS SENT YAYAYYAAY:D");
			return true;
		}
		else{
			Log.e(TAG, "Failed to send file");
			return false;
		}
	}
	
	//TODO: Around here we have MSRP handling methods
	
	
	private boolean sendMessage(String remoteUri){
		boolean ret = false;
		try{
		
		final String content = mEtCompose.getText().toString();
			if(msrpSession != null){
				ret = msrpSession.SendMessage(content);
			}else{
				
				final String remotePartyUri = NgnUriUtils.makeValidSipUri(remoteUri);
				final NgnMsrpSession msrpSes = NgnMsrpSession.createOutgoingSession(mSipService.getSipStack(), 
						NgnMediaType.Chat, remotePartyUri);
				if(msrpSes == null){
					Log.e(TAG,"Failed to create MSRP session!chating!");
					return false;
				}
				msrpSession = msrpSes;
				if(msrpSes.SendMessage(content)){
					Log.v(TAG,"MESSAGEEEEEEEEEEEEEEEEEE WAS SENT YAYAYYAAY:D");
					
					
					
					
					ret = true;
				}else{
					Log.v(TAG,"Unexpected error in Sending MSRP Chat Message.");
					return false;
				}
		}
		
			mChat.setText(mChat.getText()+"\nMe: "+content);
			//Toast.makeText(getApplicationContext(), mChat.getText(), Toast.LENGTH_LONG).show();
			scrollToBottom(10);
			//mChat.append("\nMe: "+content);
		
			
		

		mEtCompose.setText(NgnStringUtils.emptyValue());
		return ret;
		}catch(Exception e666){
			Log.e(TAG,"Execute Order 666.");
			e666.printStackTrace();
			return false;
		}
		
		
		
	}
	
	
	
	private void scrollToBottom(int delay) {
	    // If we don't call fullScroll inside a Runnable, it doesn't scroll to
	    // the bottom but to the (bottom - 1)
	    mScrollM.postDelayed(new Runnable() {
	        public void run() {
	            mScrollM.fullScroll(ScrollView.FOCUS_DOWN);
	        }
	    }, delay);
	}
	
	

	boolean makeVideoCall(String phoneNumber){
		if (NgnAVSession.hasActiveSession() == false){
			final String validUri = NgnUriUtils.makeValidSipUri(String.format("sip:%s@%s", phoneNumber, "organization2.org"));
			Log.d(TAG,"ValidUri is: "+validUri);
			if(validUri == null){
				//mTvLog.setText("failed to normalize sip uri '" + phoneNumber + "'");
	            return false;
			}
			mSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.AudioVideo);
			mSession.setRemotePartyUri(validUri); 
			//mTvRemote.setText(mSession.getRemotePartyUri());
			boolean helper = mSession.makeCall(validUri);
			return helper;
		}else{
			Toast.makeText(getApplicationContext(), "There is already an ongoing call.", Toast.LENGTH_LONG).show();
			return false;
		}
	}

	
	boolean createVideoFeeds(){
		if (mSession != null){			mSession.incRef();
			mSession.setContext(this);;
			loadInCallVideoView();
		}
		return true;
	}

	
	private void loadInCallVideoView(){
		Log.d(TAG, "loadInCallVideoView()");
    	mViewInCallVideo = mInflater.inflate(R.layout.view_call_incall_video, null);
    	mViewLocalVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(R.id.view_call_incall_video_FrameLayout_local_video);
    	mViewRemoteVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(R.id.view_call_incall_video_FrameLayout_remote_video);
    	mMainLayout.removeAllViews();
    	startStopVideoConsumer(mSession.isSendingVideo());
	    
    	
    	//CONTROL ROOM SHOULD NOT SEND VIDEO TO OTHERS - if you want video from control room uncomment below
    	//startStopVideoProducer(mSession.isSendingVideo());
	    
	    
	    AnimationSet setVideo = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		setVideo.addAnimation(animation);
		animation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
		);
		animation.setDuration(3000);
		setVideo.addAnimation(animation);
		mMainLayout.setAnimation(setVideo);
		mMainLayout.startAnimation(setVideo);
	    mMainLayout.addView(mViewInCallVideo);   
	}

	
	private void startStopVideoProducer(boolean bStart){
		Log.d(TAG, "startStopVideoPRODUCER("+bStart+")");
		mSession.setSendingVideo(bStart);
		if(mViewLocalVideoPreview != null){
            mViewLocalVideoPreview.removeAllViews();
            if(bStart){
            	final View localPreview = mSession.startVideoProducerPreview();
                if(localPreview != null){
                	final ViewParent viewParent = localPreview.getParent();
                    if(viewParent != null && viewParent instanceof ViewGroup){
                    	((ViewGroup)(viewParent)).removeView(localPreview);
                    }
                    if(localPreview instanceof SurfaceView){
                    	((SurfaceView)localPreview).setZOrderOnTop(true);
                    }
                    AnimationSet setVideo = new AnimationSet(true);
                    Animation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(100);
                    setVideo.addAnimation(animation);
                    animation = new TranslateAnimation(
                    	Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    	Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    );
                    animation.setDuration(3000);
                    setVideo.addAnimation(animation);
                    mViewLocalVideoPreview.setAnimation(setVideo);
                    mViewLocalVideoPreview.startAnimation(setVideo);
                    mViewLocalVideoPreview.addView(localPreview);
                    mViewLocalVideoPreview.bringChildToFront(localPreview);
                }
            }
            mViewLocalVideoPreview.bringToFront();
		}
	}

	
	private void startStopVideoConsumer(boolean bStart){
		Log.d(TAG, "startStopVideoCONSUMER("+bStart+")");
    
		mSession.setSendingVideo(bStart);
    
		if(mViewRemoteVideoPreview != null){
		mViewRemoteVideoPreview.removeAllViews();
            if(bStart){
            	final View localPreview = mSession.startVideoConsumerPreview();
                if(localPreview != null){
                	final ViewParent viewParent = localPreview.getParent();
                    if(viewParent != null && viewParent instanceof ViewGroup){
                    	((ViewGroup)(viewParent)).removeView(localPreview);
                    }
                    AnimationSet setVideo = new AnimationSet(true);
                    Animation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(100);
                    setVideo.addAnimation(animation);
                    animation = new TranslateAnimation(
                    	Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    	Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    );
                    animation.setDuration(3000);
                    setVideo.addAnimation(animation);
                    mViewRemoteVideoPreview.setAnimation(setVideo);
                    mViewRemoteVideoPreview.startAnimation(setVideo);
                    mViewRemoteVideoPreview.addView(localPreview);
                    mViewRemoteVideoPreview.bringChildToFront(localPreview);
                }
            }
		}
	}


//Not sure if they are really needed somewhere in the future************************************************************************************

	private void applyCamRotation(int rotation){
		if(mSession != null){
			mLastRotation = rotation;
			mSession.setRotation(rotation);
        }
	}

	private boolean isTransfering = false;

	public boolean getIsTransfering(){
		return isTransfering;
	}
	public void setIsTransfering(boolean istra){
		isTransfering = istra;
	}
	
	//************************************************************************************Not sure if they are really needed somewhere in the future - END




	boolean makeVoiceCall(String phoneNumber){
		if (NgnAVSession.hasActiveSession() == false){
			final String validUri = NgnUriUtils.makeValidSipUri(String.format("sip:%s@%s", phoneNumber, "organization2.org"));
			Log.d(TAG,"ValidUri: "+validUri);
			if(validUri == null){
				//mTvLog.setText("failed to normalize sip uri '" + phoneNumber + "'");
				return false;
			}
			mSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.Audio);
    		mSession.incRef();
			mSession.setContext(this);
    		mSession.setRemotePartyUri(validUri);
			//mTvRemote.setText(mSession.getRemotePartyUri());
    		return mSession.makeCall(validUri);
		}else{
			Toast.makeText(getApplicationContext(), "There is already an ongoing call.", Toast.LENGTH_LONG).show();
			return false;
		}
	}
	
	
	
	private void showPersonInfo(final String pkey){

		mPersonInfoLayout.removeAllViews();	
		mPersonInfoLayout.addView(mViewPersonInfo);
		TextView tvUser = (TextView) findViewById(R.id.personinfo_title);

		TextView tvClass = (TextView) findViewById(R.id.personinfo_class);
		TextView tvNote = (TextView) findViewById(R.id.personinfo_note);
		//TextView tvAddress = (TextView) findViewById(R.id.personinfo_address);
		
		TextView tvTimestamp = (TextView) findViewById(R.id.personinfo_timestamp);
		
		if (PresenceInfo.pi.get(pkey) != null){
			tvUser.setText("<"+PresenceInfo.pi.get(pkey).getStatus()+"> "+PresenceInfo.pi.get(pkey).getUser()+" - "+PresenceInfo.pi.get(pkey).getOrganization());
			tvClass.setText("Class: "+PresenceInfo.pi.get(pkey).getOclass());
			tvNote.setText("Note:  "+PresenceInfo.pi.get(pkey).getNote());
			//tvAddress.setText(pkey);
			tvTimestamp.setText("Last Updated: "+PresenceInfo.pi.get(pkey).getTimestamp());
		}else{
			tvUser.setText("Geryon Control Room");
			tvClass.setText("");
			tvNote.setText("");
			//tvAddress.setText("");
			tvTimestamp.setText("");
		}
		
		
		Button mBtPesronCall = (Button) findViewById(R.id.personinfo_call);
		
		mBtPesronCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (PresenceInfo.pi.get(pkey) != null){
					Toast.makeText(getApplicationContext(), "Calling->"+pkey, Toast.LENGTH_LONG).show();
					makeVoiceCall(pkey.substring(4));
				}else{
					Toast.makeText(getApplicationContext(), "You cannot call yourself", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		
		
		Button mBtPesronVideoCall = (Button) findViewById(R.id.personinfo_videocall);
		
		mBtPesronVideoCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (PresenceInfo.pi.get(pkey) != null){
					makeVideoCall(pkey.substring(4));
				}else{
					Toast.makeText(getApplicationContext(), "You cannot call yourself", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		
		
		Button mBtPesronTransfer = (Button) findViewById(R.id.personinfo_transfer);
		
		mBtPesronTransfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (PresenceInfo.pi.get(pkey) != null){
					
					//makeVoiceCall(pkey);
					Toast.makeText(getApplicationContext(), "Transfer here", Toast.LENGTH_LONG).show();
				
				}else{
					Toast.makeText(getApplicationContext(), "You cannot transfer to yourself", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		
		
		
		
	}
	
	

	@Override
	protected void onDestroy() {
		if (mSipBroadCastRecv != null) {
			unregisterReceiver(mSipBroadCastRecv);
			mSipBroadCastRecv = null;
		}
		super.onDestroy();
		// Remove the listener you previously added
		mTimer.cancel();
		mm = new HashMap<String,Marker>();
		locationManager.removeUpdates(locationListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Starts the engine
		if(!mEngine.isStarted()){
			mEngine.start();
		}
		startService(new Intent(this,ListenerService.class));
		Log.d(TAG,"Starting ListenerService...");

	}
	
	protected void onPause() {
		super.onPause();
		stopService(new Intent(this,ListenerService.class));
		Log.d(TAG,"Stopping ListenerService...");

	}
	
	

	
}


