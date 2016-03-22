package draugvar.beacon;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class beacon extends Application implements BootstrapNotifier {
    private static final String TAG = ".Beacon";
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App Started Up!");
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
              setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        // wake up the app when any beacon is seen (you can specify specific id filters in the parameters below)
        Region region = new Region("draugvar.beacon.boostrapRegion", null, null, null);
        RegionBootstrap regionBootstrap = new RegionBootstrap(this, region);
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "Got a didEnterRegion call");
        // This call to disable will make it so the activity below only gets launched the first time
        // a beacon is seen (until the next time the app is launched)
        // if you want the Activity to launch every single time beacons come into view, remove this call.
        //regionBootstrap.disable();

        Intent intent = new Intent(this, RangingActivity.class);
        // IMPORTANT: in the AndroidManifest.xml definition of this activity, you must set
        // android:launchMode="singleInstance" or you will get two instances
        // created when a user launches the activity manually and it gets launched from here.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        this.startActivity(intent);
    }

    @Override
    public void didExitRegion(Region arg0) {
        // Don't care
    }
}

