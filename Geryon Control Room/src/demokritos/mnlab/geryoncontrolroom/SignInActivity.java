package demokritos.mnlab.geryoncontrolroom;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventTypes;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class SignInActivity extends Activity {

	private static String TAG = SignInActivity.class.getCanonicalName(); 

	/*
	private static String PUBLIC_IDENTITY = "";
	private static String PRIVATE_IDENTITY = "";
	private static String PASSWORD = "";
	private static String REALM = "";
	private static String PROXY_HOST = "";
	private static String PROXY_PORT = "";
	*/
	
	/*
	private static String PUBLIC_IDENTITY = "sip:psap@ims.geryon.gr";
	private static String PRIVATE_IDENTITY = "psap@ims.geryon.gr";
	private static String PASSWORD = "psap";
	private static String REALM = "ims.geryon.gr";
	private static String PROXY_HOST = "10.143.200.10";
	private static String PROXY_PORT = "4060";
	*/
	
	/*
	private static String PUBLIC_IDENTITY = "sip:alice@ims.geryon.gr";
	private static String PRIVATE_IDENTITY = "alice@ims.geryon.gr";
	private static String PASSWORD = "alice";
	private static String REALM = "ims.geryon.gr";
	private static String PROXY_HOST = "10.143.200.10";
	private static String PROXY_PORT = "4060";
	*/
	
	/*
	private static String PUBLIC_IDENTITY = "sip:psap@geryon.test";
	private static String PRIVATE_IDENTITY = "psap@geryon.test";
	private static String PASSWORD = "psap";
	private static String REALM = "geryon.test";
	private static String PROXY_HOST = "192.168.15.218";
	private static String PROXY_PORT = "4060";
	*/
	
	
	
	
	private static String PUBLIC_IDENTITY = "sip:psap@organization2.org";
	private static String PRIVATE_IDENTITY = "psap@organization2.org";
	private static String PASSWORD = "psap";
	private static String REALM = "sip:organization2.org";
	private static String PROXY_HOST = "192.168.12.53";
	private static String PROXY_PORT = "4060";
	
	
	
	
	
	
	
	
	
	/*
	private static String PUBLIC_IDENTITY = "sip:psap@sec-geryon.eu";
	private static String PRIVATE_IDENTITY = "psap@sec-geryon.eu";
	private static String PASSWORD = "psap";
	private static String REALM = "sip:sec-geryon.eu";
	private static String PROXY_HOST = "pcscf.sec-geryon.eu";
	private static String PROXY_PORT = "4060";
	*/
	
	
	
	private TextView mTvInfo;
	private Button mBtSignIn;
	private Button mBtExit;
    private BroadcastReceiver mSipBroadCastRecv;

    private  NgnEngine mEngine;
    private  INgnConfigurationService mConfigurationService;
    private  INgnSipService mSipService;

    public static Activity sia;
    

    public SignInActivity(){
    	mEngine = NgnEngine.getInstance();
    	mConfigurationService = mEngine.getConfigurationService();
    	mSipService = mEngine.getSipService();
    	sia = this;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sign_in);
		
		
		mTvInfo = (TextView)findViewById(R.id.textViewInfo);
		mBtSignIn = (Button) findViewById(R.id.bSignIn);
		mBtExit = (Button) findViewById(R.id.bExit);		

		// Subscribe for registration state changes
		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				// Registration Event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case REGISTRATION_NOK:
							mTvInfo.setText("Failed to register :(");
							break;
						case UNREGISTRATION_OK:
							mTvInfo.setText("You are now unregistered :)");
							break;
						case REGISTRATION_OK:
							mTvInfo.setText("You are now registered :)");
							break;
						case REGISTRATION_INPROGRESS:
							mTvInfo.setText("Trying to register...");
							break;
						case UNREGISTRATION_INPROGRESS:
							mTvInfo.setText("Trying to unregister...");
							break;
						case UNREGISTRATION_NOK:
							mTvInfo.setText("Failed to unregister :(");
							break;
					}
					mBtSignIn.setText(mSipService.isRegistered() ? "Enter" : "Sign In");
					if (mBtSignIn.getText()=="Enter"){
						mBtSignIn.setBackgroundResource(R.color.nicegreen);
					}
				}
				
				
				
			
				
				
				
			}
		};
		
		
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intentFilter.addAction(NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
		registerReceiver(mSipBroadCastRecv, intentFilter);
		
		
		mBtSignIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mEngine.isStarted()){
					if(!mSipService.isRegistered()){
						// Set credentials
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, PRIVATE_IDENTITY);
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, PUBLIC_IDENTITY);
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, PASSWORD);
						mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, PROXY_HOST);
						mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT, NgnStringUtils.parseInt(PROXY_PORT, 5060));
						mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, REALM);
						// VERY IMPORTANT: Commit changes
						mConfigurationService.commit();
						// register (log in)
						try{
							mSipService.register(SignInActivity.this);
							
							
							
							//signIn();
							//finish();
						}catch(NetworkOnMainThreadException nomt){
							Toast.makeText(getApplicationContext(), "Device is not connected to the internet.", Toast.LENGTH_LONG).show();
						}
					}else{
						// unregister (log out)
						//mSipService.unRegister();
						signIn();
					}
				}else{
					mTvInfo.setText("Engine not started yet");
				}
			}
		});

		
		mBtExit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				mSipService.unRegister();
				finish();
			}
		});
		
		
	}
	
	
	public void signIn(){
		Toast.makeText(getApplicationContext(), "Connected to IMS server.", Toast.LENGTH_LONG).show();
		Intent intent = new Intent(SignInActivity.this,ControlRoomActivity.class);
		startActivity(intent);
	}
	
	
	@Override
	protected void onDestroy() {
		// Stops the engine
		if(mEngine.isStarted()){
			mEngine.stop();
		}
		// release the listener
		mSipService.unRegister();
		if (mSipBroadCastRecv != null) {
			unregisterReceiver(mSipBroadCastRecv);
			mSipBroadCastRecv = null;
		}
		super.onDestroy();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		// Starts the engine
		if(!mEngine.isStarted()){
			if(mEngine.start()){
				mTvInfo.setText("Engine started :)");
			}else{
				mTvInfo.setText("Failed to start the engine :(");
			}
		}
	}   
	
	
}
