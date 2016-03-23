package draugvar.beacon;

import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BeaconRangeNotifier implements RangeNotifier{
    private static RangingActivity.BeaconHandler mBeaconHandler;
    protected static final String TAG = "BeaconRangeNotifier";
    protected static final int MESSAGE_READ = 1;
    //private HashMap<Beacon, String> beaconsHM = new HashMap<>();

    public static void setBeaconHandler(RangingActivity.BeaconHandler beaconHandler){
        mBeaconHandler = beaconHandler;
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() > 0) {
            String[] beacon_array = new String[beacons.size()];
            int i = 0;
            for (Beacon beacon : beacons) {
                //checked implementation of equals in Beacon class: 2 beacons are equal if they share same three identifiers
                beacon_array[i] = beacon.getId1() + "||" + beacon.getDistance();
                i++;
                Log.i(TAG, "Beacon " + beacon.getId1().toString());
            }
            mBeaconHandler.obtainMessage(MESSAGE_READ, beacon_array).sendToTarget();
        }
    }

}
