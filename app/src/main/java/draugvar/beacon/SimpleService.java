package draugvar.beacon;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangingData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SimpleService extends Service implements BeaconConsumer
{
    protected static final String TAG = "MyBeaconService";
    private BeaconManager beaconManager;
    RangingData rangingData = null;
    Beacon beacon = null;
    int  mapKey = 0;

    ArrayList<String> sBeacons = new ArrayList<String>();
    Hashtable<Integer, String> source = new Hashtable<Integer,String>();

    final HashMap<Integer, String> map = new HashMap(source);
    final HashMap<Integer, String>  maptime = new HashMap(source);

    String[] parts = null;


    public class SimpleServiceBinder extends Binder
    {
        public SimpleService getService()
        {

            return SimpleService.this;
        }
    }




    @Override
    public Binder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(TAG, "Service created ...");

        beaconManager = BeaconManager.getInstanceForApplication(getBaseContext());

        //   beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make  it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb
        //
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.getBeaconParsers().
                add(new BeaconParser().
                        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));


        beaconManager.bind(this);

        beaconManager.setForegroundBetweenScanPeriod(5000); //ok faccio come dici tu! :P


    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        Log.i(TAG,"onStart called");

        beaconManager.setBackgroundMode(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG,"onStartCommand called");

        return START_STICKY; //START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "Service destroyed ...");
        //Toast.makeText(this, "Service destroyed ...", Toast.LENGTH_LONG).show();
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect()
    {
        Log.i(TAG, "<<< onBeaconServiceConnect  >>>");

        beaconManager.setMonitorNotifier(new MonitorNotifier()
        {


            @Override
            public void didEnterRegion(Region region)
            {

                Log.i(TAG, "onBeaconServiceConnect \ngetId1: "+region.getId1()+"\ngetId2: "+region.getId2()+"\ngetId3: "+region.getId3());
                Log.i(TAG, "**************-------------****************");


                logBeaconData(true);
            }

            @Override
            public void didExitRegion(Region region)
            {
                //logToDisplay("Exit Region "+ region.getUniqueId(), true);
                Log.i(TAG, "********!!!!!!!!! didExitRegion !!!!!!!!!!*******");
                mapKey = 0;
                logBeaconData(false);
                try
                {
                    beaconManager.stopRangingBeaconsInRegion(new Region("sBeacon", null, null, null));
                } catch (RemoteException e) { e.printStackTrace();}

                Iterator<Integer> keySetIterator = map.keySet().iterator();

                while(keySetIterator.hasNext())
                {
                    Integer key = keySetIterator.next();

                    Log.i(TAG, "****DELETE key: " + mapKey + " value: " + map.get(key));
                    keySetIterator.remove();
                    maptime.remove(key);

                }
                printtoscreen();


            }

            @Override
            public void didDetermineStateForRegion(int state, Region region)
            {

                Log.i(TAG, "didDetermineStateForRegion \ngetId1: "+region.getId1()+"\ngetId2: "+region.getId2()+"\ngetId3: "+region.getId3());
            }


        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("sBeacon", null, null, null));

        } catch (RemoteException e) {  Log.i(TAG, "RemoteException: "+e);   }
    }


    private void logBeaconData(final boolean enter)
    {
        final Service thisService=this;
        beaconManager.setRangeNotifier(new RangeNotifier()
        {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region)
            {

                if (beacons.size() > 0)
                {

                    //  sBeacons.clear();

                    beacon = beacons.iterator().next();

                    Log.i(TAG, " UUID: " + beacon.getId1());
                    Log.i(TAG, " Major: " + beacon.getId2());
                    Log.i(TAG, " Minor: " + beacon.getId3());
                    Log.i(TAG, " RSSI: " + beacon.getRssi());
                    Log.i(TAG, " Power: "+ beacon.getTxPower());
                    Log.i(TAG, " Distance: "+ beacon.getDistance());



                    if (map.values().contains(beacon.getIdentifiers().toString()))
                    {
                        Log.i(TAG, "<<< Already there >>> "+beacon.getIdentifiers().toString());

                    }
                    else
                    {
                        mapKey = mapKey + 1;
                        map.put(mapKey, beacon.getIdentifiers().toString());
                        maptime.put(mapKey, ""+System.currentTimeMillis());

                        //NOtifications each time a beacon is seen for first time
                        Intent intent = new Intent(thisService, RangingActivity.class);
                        PendingIntent pIntent = PendingIntent.getActivity(thisService, 0, intent, 0);

                        Notification noti = new Notification.Builder(thisService)
                                .setContentTitle("A new beacon has been found!")
                                .setContentText(beacon.getId1().toString()).setSmallIcon(R.drawable.b_icon)
                                .setAutoCancel(true)
                                .setContentIntent(pIntent)
                                .build();
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(0, noti);

                    }

                    Iterator<Integer> keySetIterator = map.keySet().iterator();

                    final int seconds = 10;

                    while(keySetIterator.hasNext())
                    {
                        Integer key = keySetIterator.next();


                        if (map.get(key).contains(beacon.getIdentifiers().toString()))
                        {
                            maptime.put(key, ""+System.currentTimeMillis());
                        }

                        long diff = System.currentTimeMillis() - (Long.parseLong(maptime.get(key)));

                        int second = (int) ((diff / 1000) % 60);
                        Log.i(TAG, "key: " + key + " value: " + map.get(key)+" diff: "+second);

                        if(second >= seconds )
                        {
                            Log.i(TAG, "****DELETE key: " + mapKey + " value: " + map.get(key)+" diff: "+second);
                            keySetIterator.remove();
                            maptime.remove(key);
                            //logToDisplay("\n** Beacon not longer reporting **"+ map.get(key), true);
                        }
                        //printtoscreen();

                    }

                }

            }

        });

        try
        {
            beaconManager.startRangingBeaconsInRegion(new Region("sBeacon", null, null, null));
            Log.i(TAG, "*** startRangingBeaconsInRegion ***");
        } catch (RemoteException e) {  Log.i(TAG, "RemoteException: "+e);   }

    }
    public void printtoscreen()
    {
        Iterator<Integer> keySetIterator = map.keySet().iterator();
        //   logToDisplay("", false);
        while(keySetIterator.hasNext())
        {
            Integer key = keySetIterator.next();

            Log.i(TAG, "---------------------------------------------------");
            if (map.get(key) != null)
            {
                parts = map.get(key).toString().substring(1, map.get(key).toString().length()-1).split("\\,");

                Log.i(TAG, "---------------------------------------------------");
                Log.i(TAG, "UUID:  " + parts[0]);
                Log.i(TAG, "Major: " + parts[1]);
                Log.i(TAG, "Minor: " + parts[2]);
                Log.i(TAG, "RSSI: "+ beacon.getRssi());
                Log.i(TAG, "Power: "+ beacon.getTxPower());
                Log.i(TAG, "Distance: "+ beacon.getDistance());
            }


        }

    }

}