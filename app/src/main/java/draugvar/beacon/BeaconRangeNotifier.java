package draugvar.beacon;

import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BeaconRangeNotifier implements RangeNotifier{
    private static RangingActivity.BeaconHandler mBeaconHandler;
    protected static final String TAG = "BeaconRangeNotifier";
    protected static final int MESSAGE_READ = 1;
    private List<Beacon> alreadySeenInRegion=new LinkedList<>();

    public static void setBeaconHandler(RangingActivity.BeaconHandler beaconHandler){
        mBeaconHandler = beaconHandler;
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() > 0) {
            //BeaconItem beaconItem = null;

            for (Beacon beacon : beacons) {
                //checked implementation of equals in Beacon class: 2 beacons are equal if they share same three identifiers
                if(!(alreadySeenInRegion.contains(beacon))) {
                    alreadySeenInRegion.add(beacon);
                    String message = beacon.getId1() + "||" + beacon.getDistance();
                    mBeaconHandler.obtainMessage(MESSAGE_READ, message).sendToTarget();
                    Log.i(TAG, "Beacon " + beacon.getId1().toString());
                }
            }
        }
    }

}
