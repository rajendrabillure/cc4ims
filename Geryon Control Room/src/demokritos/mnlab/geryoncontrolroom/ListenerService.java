package demokritos.mnlab.geryoncontrolroom;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMsrpEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;






public class ListenerService extends NgnNativeService {
	private final static String TAG = ListenerService.class.getCanonicalName();
	public static final String ACTION_STATE_EVENT = TAG + ".ACTION_STATE_EVENT";
	
	private PowerManager.WakeLock mWakeLock;
	private BroadcastReceiver mBroadcastReceiver;
	private NgnEngine mEngine;
	
	public ListenerService(){
		super();
		mEngine = NgnEngine.getInstance();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate() Listener Service");
		
		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if(powerManager != null && mWakeLock == null){
			mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE 
					| PowerManager.SCREEN_BRIGHT_WAKE_LOCK 
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart() Listener Service");
		
		// register()
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				 // Registration Events
                if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
                        NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
                        final NgnRegistrationEventTypes type;
                        if(args == null){
                                Log.e(TAG, "Invalid event args");
                                return;
                        }
                        switch((type = args.getEventType())){
                                case REGISTRATION_OK:
                                case REGISTRATION_NOK:
                                case REGISTRATION_INPROGRESS:
                                case UNREGISTRATION_INPROGRESS:
                                case UNREGISTRATION_OK:
                                case UNREGISTRATION_NOK:
                                default:
                                        Log.d(TAG,"Registration Event in Listener Service");
                        }
                }
                
                
                if(NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT.equals(action)){
                	NgnSubscriptionEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
                    final NgnSubscriptionEventTypes type;
                    if(args == null){
                            Log.e(TAG, "Invalid NgnSubscriptionEventArgs event args");
                            return;
                    }
                   
                    
                    Log.v(TAG,"Presence Information are coming.");
                    
                  
                    //String spliter = args.getContentType();
                    final byte[] content = args.getContent();
                  if (content!=null){
                	  
                	  
                	  
                    Log.v(TAG,"\n\nATTENTION:\n");
                    Log.v(TAG,"LENGTH: "+content.length+" WHAT: "+content.toString());
                    
                    String helper="";
                    
                    /*
                    new Thread(new Runnable() {
                        public void run() {
                        	 String helperthread="";
                        	
                        	 
                        	 int len = content.length;
                        	 
                        	 for (int ids=0;ids<len;ids++){
                               	 //helper= helper+(char)content[ids];
                               	 helperthread=helperthread+(char)content[ids];
                            }
                       		
                        	helperthread = new String(content,0,len);
                        	
                        }
                    }).start();
                    */
                    
                    helper = new String(content,0,content.length);
                    
                    
                    Scanner scanner = new Scanner(helper);
                    String spliter = scanner.nextLine();
                    Log.v(TAG, "Spliting with: "+spliter);
                    
                    String [] helperarray = helper.split(spliter);
                    Log.v(TAG, "Lenght of split: "+helperarray.length);
                    
                    
                    
                    
                    /*
                    for (int jj=0;jj<helperarray.length;jj++){
                    	Log.v(TAG,helperarray[jj]);
                    }*/
                    	//Log.v(TAG,"To prwto einai: "+helperarray[0]);
                    	//Log.v(TAG,"To teleutaio einai: "+helperarray[helperarray.length-1]);
                 	
                    try{
                    	
                    	
                    	
                    	if (helperarray.length>=4){

                    		scanner = new Scanner(helperarray[1]);
                    		int helpcounter=2;
                    		while (scanner.hasNextLine()) {
                              String line = scanner.nextLine();
                              
                              
                              if (line.length()>8 && line.substring(0,9).equals("<resource")){
                            	  String poKey="";
                            	  String poUser="";
                            	  String poLat="";
                            	  String poLong="";
                            	  String poOrg="";
                            	  String poClass="";
                            	  String poNote="";
                            	  String poTimestamp="";
                            	  String poStatus="";
                            	  String poRadius="";
                            	  
                            	  boolean flagger=false;
                            	  
                            	  String [] linehelper = line.split("\"");
                            	  poKey = linehelper[1];
                            	  String [] poOrghelper = poKey.split("\\.");
                            	  String [] poOrghelper2 = poOrghelper[1].split("@");
                            	  if (poOrghelper2[0].equals("geryon")){
                            		  poOrg = poOrghelper[2];
                            	  }else{
                            		  poOrg = poOrghelper2[0];
                            	  }
                            	  poUser = poOrghelper[0].substring(4);
                            	  /*
                            	  for (int i=0;i<poOrghelper.length;i++){
                            		  Log.v(TAG,"ORG HELPER"+i+"->"+poOrghelper[i]);
                            	  }
                            	  for (int i=0;i<poOrghelper2.length;i++){
                            		  Log.v(TAG,"ORG2 HELPER"+i+"->"+poOrghelper2[i]);
                            	  }
                            	  */
                            	  Scanner scanner2 = new Scanner(helperarray[helpcounter]);
                                  while (scanner2.hasNextLine()) {
                                    String line2 = scanner2.nextLine();
                                    line2 = line2.trim();
                                    if (line2.length()>11 && line2.substring(0,12).equals("<rpid:class>")){
                                    	final Pattern pattern = Pattern.compile("<rpid:class>(.+?)</rpid:class>");
                                    	final Matcher matcher = pattern.matcher(line2);
                                    	matcher.find();
                                    	poClass = matcher.group(1);
                                    	//Log.v(TAG,"grammh2->"+matcher.group(1));
                                    }
                                    if (line2.length()>16 && line2.substring(0,17).equals("<rpid:activities>")){
                                    	final Pattern pattern = Pattern.compile("<rpid:activities><rpid:(.+?)/></rpid:activities>");
                                    	final Matcher matcher = pattern.matcher(line2);
                                    	matcher.find();
                                    	poStatus = matcher.group(1);
                                    	//Log.v(TAG,"grammh2->"+matcher.group(1));
                                    }
                                    if (line2.length()>8 && line2.substring(0,9).equals("<pdm:note")){
                                    	final Pattern pattern = Pattern.compile("<pdm:note xml:lang=\"en\">(.+?)</pdm:note>");
                                    	final Matcher matcher = pattern.matcher(line2);
                                    	matcher.find();
                                    	poNote = matcher.group(1);
                                    	//Log.v(TAG,"grammh2->"+matcher.group(1));
                                    }
                                    if (line2.length()>14 && line2.substring(0,15).equals("<pdm:timestamp>") && flagger==false){
                                    	
                                    	final Pattern pattern = Pattern.compile("<pdm:timestamp>(.+?)</pdm:timestamp>");
                                    	final Matcher matcher = pattern.matcher(line2);
                                    	matcher.find();
                                    	poTimestamp = matcher.group(1);
                                    	//Log.v(TAG,"grammh2->"+matcher.group(1));
                                    	
                                    	flagger=true;
                                    }
                                    if (line2.length()>16 && line2.substring(0,17).equals("<gml:coordinates>")){
                                    	final Pattern pattern = Pattern.compile("<gml:coordinates>(.+?)</gml:coordinates>");
                                    	final Matcher matcher = pattern.matcher(line2);
                                    	matcher.find();
                                    	Log.v(TAG,"grammh2->"+matcher.group(1));
                                    	String [] linehelper2 = matcher.group(1).split(" ");
                                    	poLat = linehelper2[0];
                                    	if (poLat!=""){
                                    		String endSign = poLat.substring(poLat.length()-1);
                                    		if (endSign.equals("N")){
                                    			endSign = "";
                                    		}else{
                                    			endSign = "-";
                                    		}
                                    		String [] poLathelper = poLat.split(":");
                                    		poLat = endSign + poLathelper[0]+"."+poLathelper[1]+poLathelper[2];
                                    		poLat = poLat.substring(0,poLat.length()-1);
                                    		
                                    	}
                                    	poLong = linehelper2[1];
                                    	if (poLong!=""){
                                    		String endSign = poLong.substring(poLong.length()-1);
                                    		if (endSign.equals("E")){
                                    			endSign = "";
                                    		}else{
                                    			endSign = "-";
                                    		}
                                    		String [] poLonghelper = poLong.split(":");
                                    		poLong = endSign + poLonghelper[0]+"."+poLonghelper[1]+poLonghelper[2];
                                    		poLong = poLong.substring(0,poLong.length()-1);
                                    		
                                    	}
                                    	
                                    }
                                    if (line2.length()>8 && line2.substring(0,9).equals("<gml:pos>")){
                                    	final Pattern pattern = Pattern.compile("<gml:pos>(.+?)</gml:pos>");
                                    	final Matcher matcher = pattern.matcher(line2);
                                    	matcher.find();
                                    	
                                    	String [] linehelper2 = matcher.group(1).split(" ");
                                    	poLat = linehelper2[0];
                                    	
                                    	poLong = linehelper2[1];
                                    	
                                    	
                                    	Log.d(TAG,"EDW EDW EDW-> "+poUser+"    ->    "+poLat+" "+poLong);
                                    	
                                    	
                                    }
                                    if (line2.length()>9 && line2.substring(0,10).equals("<gs:radius")){
                                    	final Pattern pattern = Pattern.compile("<gs:radius uom=\"urn:ogc:def:uom:EPSG::9001\">(.+?)</gs:radius>");
                                    	final Matcher matcher = pattern.matcher(line2);
                                    	matcher.find();
                                    	
                                    	poRadius = matcher.group(1);
                                    	
                                    	
                                    }
                                    
                                    
                                    
                                    
                                  }
                            	  
                            	   
                                  PresenceInfo.pi.put(poKey, new PresenceObject(poUser,poLat,poLong,poOrg,poClass,poNote,poStatus,poTimestamp,poRadius));
                            	  helpcounter++;
                              }
                              
                              
                            
                              
                            }
                    		
                    		
                    	}else{
                    		Log.e(TAG,"Error in xml, it doesnt have data inside.");
                    	}
                    	
                    	
                    	
                    }catch(Exception e51){
                    	Log.e(TAG,"Error in reading the xml. Differenet format than the expected.");
                    	e51.printStackTrace();
                    }
                    
              
                    
                    Log.v(TAG, "Xml Presence parsing was successful.");
                    
                  }
                    
                }
                
                
				// PagerMode Messaging Events
				if(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT.equals(action)){
					NgnMessagingEventArgs args = intent.getParcelableExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case INCOMING:
							String dateString = intent.getStringExtra(NgnMessagingEventArgs.EXTRA_DATE);
							String remoteParty = intent.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY);
							if(NgnStringUtils.isNullOrEmpty(remoteParty)){
								remoteParty = NgnStringUtils.nullValue();
							}
							remoteParty = NgnUriUtils.getUserName(remoteParty);
							NgnHistorySMSEvent event = new NgnHistorySMSEvent(remoteParty, StatusType.Incoming);
							event.setContent(new String(args.getPayload()));
							event.setStartTime(NgnDateTimeUtils.parseDate(dateString).getTime());
							mEngine.getHistoryService().addEvent(event);
							break;
					}
				}
				
				
				
				
				// MSRP chat Events
				// For performance reasons, file transfer events will be handled by the owner of the context
				if(NgnMsrpEventArgs.ACTION_MSRP_EVENT.equals(action)){
					NgnMsrpEventArgs args = intent.getParcelableExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					Log.e(TAG,"MSRP event,  eventType: "+args.getEventType());
					switch(args.getEventType()){
						case DATA:
							final NgnMsrpSession session = NgnMsrpSession.getSession(args.getSessionId());
							if(session == null){
								Log.e(TAG, "Failed to find MSRP session with id="+args.getSessionId());
								return;
							}
							
							/*
							final byte[]content = intent.getByteArrayExtra(NgnMsrpEventArgs.EXTRA_DATA);
							String helpme="";
							helpme = new String(content,0,content.length);
							Log.v(TAG,"ISWS EGINE MSRP: "+helpme);
							*/
						
						
						
						
							break;
					}
				}
				
				
				
				
				
				
				
				// Invite Events
				else if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
					try{
					NgnInviteEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					
					final NgnMediaType mediaType = args.getMediaType();
					Log.d(TAG,"Action Invite Event, EventType: "+args.getEventType()+"\n MediaType:"+args.getMediaType()+"\n ID: "+args.getSessionId() );//Chat - FileTransfer
					switch(args.getEventType()){	
						
						
						case TERMWAIT:
						case TERMINATED:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.getSoundService().stopRingBackTone();
								mEngine.getSoundService().stopRingTone();	
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
							}
							else if(NgnMediaType.isChat(mediaType)){
							}
							break;
							
						case INCOMING:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								final NgnAVSession avSession = NgnAVSession.getSession(args.getSessionId());
								if(avSession != null){
									
									Log.d(TAG,"Incoming avSession ID: "+avSession.getId());
									
										
									
									if(mWakeLock != null && !mWakeLock.isHeld()){
										mWakeLock.acquire(10);
									}
									mEngine.getSoundService().startRingTone();
								}
								else{
									//Log.e(TAG, String.format("Failed to find session with id=%ld", args.getSessionId()));
								}
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
								//mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
								if(mWakeLock != null && !mWakeLock.isHeld()){
									mWakeLock.acquire(10);
								}
								final NgnMsrpSession msrpSession2 = NgnMsrpSession.getSession(args.getSessionId());
								if (msrpSession2 != null){
									Log.d(TAG,"MSRP Session ID: "+msrpSession2.getId());
									Log.d(TAG,"MSRP Session RemotePartyDisplayName "+msrpSession2.getRemotePartyDisplayName());
									((ControlRoomActivity) ControlRoomActivity.cra).msrpSession = msrpSession2;
								}
							}
							else if(NgnMediaType.isChat(mediaType)){
								//mEngine.refreshChatNotif(R.drawable.chat_25);
								if(mWakeLock != null && !mWakeLock.isHeld()){
									mWakeLock.acquire(10);
								}
								final NgnMsrpSession msrpSession2 = NgnMsrpSession.getSession(args.getSessionId());
								if (msrpSession2 != null){
									Log.d(TAG,"MSRP Session ID: "+msrpSession2.getId());
									msrpSession2.setRemotePartyUri("sip:alice@organization2.org");
									Log.d(TAG,"MSRP Session RemotePartyUri: "+msrpSession2.getRemotePartyUri());
									((ControlRoomActivity) ControlRoomActivity.cra).msrpSession = msrpSession2;
									//msrpSession2.accept();
									if (((ControlRoomActivity) ControlRoomActivity.cra).msrpSession!=null){
										((ControlRoomActivity) ControlRoomActivity.cra).msrpSession.incRef();
										//((ControlRoomActivity) ControlRoomActivity.cra).msrpSession.accept();
									}
								}
							}
							break;
							
						case INPROGRESS:
							if(NgnMediaType.isAudioVideoType(mediaType)){
							
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
							
							}
							else if(NgnMediaType.isChat(mediaType)){

							}
							break;
							
						case RINGING:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.getSoundService().startRingBackTone();
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
								
							}
							else if(NgnMediaType.isChat(mediaType)){
								
							}
							break;
						
						case CONNECTED:
						case EARLY_MEDIA:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								
								mEngine.getSoundService().stopRingBackTone();
								mEngine.getSoundService().stopRingTone();
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
								
							}
							else if(NgnMediaType.isChat(mediaType)){
								
							}
							break;
						default: break;
					}
					
					}catch(Exception e666){
						Log.e(TAG,"Execute Order 666.");
						e666.printStackTrace();
					}
					
					
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intentFilter.addAction(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
		intentFilter.addAction(NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
		intentFilter.addAction(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
		registerReceiver(mBroadcastReceiver, intentFilter);
		
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if (bundle != null && bundle.getBoolean("autostarted")) {
				if (mEngine.start()) {
					mEngine.getSipService().register(null);
				}
			}
		}
		
		// alert()
		final Intent i = new Intent(ACTION_STATE_EVENT);
		i.putExtra("started", true);
		sendBroadcast(i);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if(mBroadcastReceiver != null){
			unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}
		if(mWakeLock != null){
			if(mWakeLock.isHeld()){
				mWakeLock.release();
				mWakeLock = null;
			}
		}
		super.onDestroy();
	}
	

	
}
