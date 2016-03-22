package draugvar.beacon;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    private RecyclerView recyclerView;
    private FastItemAdapter fastAdapter;
    private List<Beacon> list_beacons;
    private static Handler handler;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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

        recyclerView = (RecyclerView) findViewById(R.id.beacon_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //create our FastAdapter which will manage everything
        fastAdapter = new FastItemAdapter();

        //set our adapters to the RecyclerView
        //we wrap our FastAdapter inside the ItemAdapter -> This allows us to chain adapters for more complex useCases
        recyclerView.setAdapter(fastAdapter);
        list_beacons = new LinkedList<>();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    private void reload(BeaconItem beaconItem) {
        fastAdapter.add(beaconItem);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                //set the items to your ItemAdapter
                //fastAdapter.add((List<Beacon>)beacons);
                if (beacons.size() > 0) {
                    BeaconItem beaconItem = null;
                    for (Beacon beacon : beacons) {
                        beaconItem = new BeaconItem();
                        beaconItem.name = beacon.getId1().toString();
                        Log.i(TAG, "Beacon " + beacon.getId1().toString());
                    }
                    //b.name=beacons.iterator().next().getId1().toString();
                    reload(beaconItem);
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }
}
