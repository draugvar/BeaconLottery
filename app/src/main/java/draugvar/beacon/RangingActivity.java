package draugvar.beacon;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    protected static FastItemAdapter fastAdapter;
    public BeaconHandler beaconHandler = new BeaconHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        Log.i(TAG, "RANGING ");
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        beaconManager.setBackgroundScanPeriod(1000l);
        beaconManager.setForegroundScanPeriod(1000l);

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
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BeaconRangeNotifier.MESSAGE_READ:
                    String[] s = msg.toString().split("||");
                    BeaconItem beaconItem = new BeaconItem();
                    beaconItem.name = s[0];
                    beaconItem.description = s[1];
                    fastAdapter.add(beaconItem);
            }
        }
    }
}
