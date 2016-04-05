package draugvar.beacon;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

public class RangingActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    protected static FastItemAdapter fastAdapter;
    public BeaconHandler beaconHandler = new BeaconHandler();
    private final static int REQUEST_ENABLE_BT = 1;
    private static int FLAG = 0;
    private static Handler runnableHandler = null;
    private static Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        Log.i(TAG, "RANGING ");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //estimote layout
        beaconManager.getBeaconParsers().
                add(new BeaconParser().
                        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        beaconManager.setBackgroundScanPeriod(5000l); //fatti i fatti tuoi! :D
        beaconManager.setForegroundScanPeriod(5000l);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.beacon_recycler_view);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //create our FastAdapter which will manage everything
        fastAdapter = new FastItemAdapter();
        fastAdapter.withOnClickListener(new FastAdapter.OnClickListener() {
            @Override
            public boolean onClick(View v, IAdapter adapter, IItem item, int position) {
                Double bet = new Random().nextDouble();
                if(bet >= 0.5){
                    Intent i = new Intent(RangingActivity.this, WinActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(RangingActivity.this, LoseActivity.class);
                    startActivity(i);
                }
                return false;
            }
        });

        //set our adapters to the RecyclerView
        //we wrap our FastAdapter inside the ItemAdapter -> This allows us to chain adapters for more complex useCases
        recyclerView.setAdapter(fastAdapter);

        // setting handler
        BeaconRangeNotifier.setBeaconHandler(beaconHandler);

        runnableHandler = new Handler();
        runnable = new Runnable() {
            public void run() {
                Log.d(TAG,"runnable started!");
                if (FLAG == 1) {
                    FLAG = 0;
                } else {
                    fastAdapter.clear();
                }
                runnableHandler.postDelayed(this, 50000);
            }
        };
        runnable.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        beaconManager.unbind(this);
        Intent mServiceIntent = new Intent(this, SimpleService.class);
        this.startService(mServiceIntent);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new BeaconRangeNotifier());
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("RangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    public static class BeaconHandler extends Handler{
        protected static final String TAG = "BeaconHandler";

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.d(TAG, "Here comes the message!");
            switch (msg.what) {
                case BeaconRangeNotifier.MESSAGE_READ:
                    fastAdapter.clear();
                    FLAG = 1;
                    String[] beacons = (String[]) msg.obj;
                    for(String beacon: beacons){
                        String[] s = beacon.split(Pattern.quote("||"));
                        BeaconItem beaconItem = new BeaconItem();
                        beaconItem.name = s[0];
                        beaconItem.distance = s[1];
                        fastAdapter.add(beaconItem);
                    }
                    /*String mMessage = (String) msg.obj;
                    Log.d(TAG, "Name "+beaconItem.name+" distance "+beaconItem.distance);*/
                    break;
                default:
                    break;
            }
        }
    }

}
