package draugvar.beacon;

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

import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.util.regex.Pattern;

public class RangingActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    protected static FastItemAdapter fastAdapter;
    public BeaconHandler beaconHandler = new BeaconHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        Log.i(TAG, "RANGING ");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        beaconManager.setBackgroundScanPeriod(6000l);
        beaconManager.setForegroundScanPeriod(6000l);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.beacon_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //create our FastAdapter which will manage everything
        fastAdapter = new FastItemAdapter();

        //set our adapters to the RecyclerView
        //we wrap our FastAdapter inside the ItemAdapter -> This allows us to chain adapters for more complex useCases
        recyclerView.setAdapter(fastAdapter);

        // setting handler
        BeaconRangeNotifier.setBeaconHandler(beaconHandler);
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
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
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
            Log.d(TAG, "Here comes the message!");
            switch (msg.what) {
                case BeaconRangeNotifier.MESSAGE_READ:
                    String mMessage = (String) msg.obj;
                    String[] s = mMessage.split(Pattern.quote("||"));
                    BeaconItem beaconItem = new BeaconItem();
                    beaconItem.name = s[0];
                    beaconItem.distance = s[1];
                    Log.d(TAG, "Name "+beaconItem.name+" distance "+beaconItem.distance);
                    fastAdapter.clear();
                    fastAdapter.add(beaconItem);
            }
        }
    }
}
