package draugvar.beacon;

import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class BeaconRangeNotifier implements RangeNotifier{
    private static RangingActivity.BeaconHandler mBeaconHandler;
    protected static final String TAG = "BeaconRangeNotifier";
    protected static final int MESSAGE_READ = 1;

    public static void setBeaconHandler(RangingActivity.BeaconHandler beaconHandler){
        mBeaconHandler = beaconHandler;
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() > 0) {
            BeaconItem beaconItem = null;
            for (Beacon beacon : beacons) {
                String message = beacon.getId1() + "||" + beacon.getDistance();
                mBeaconHandler.obtainMessage(MESSAGE_READ, message).sendToTarget();
                Log.i(TAG, "Beacon " + beacon.getId1().toString());
            }
        }
    }
}
