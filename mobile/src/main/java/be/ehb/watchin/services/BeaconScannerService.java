/**
 * Created by davy.van.belle on 30/08/2016.
 */

package be.ehb.watchin.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import be.ehb.watchin.R;
import be.ehb.watchin.activities.WatchInMain;

public class BeaconScannerService extends IntentService {
    private static final String TAG = "BEACON_SCANNER";
    private static final long SCAN_PERIOD = 5000;
    private static final long SCAN_INTERVAL = 10000;
   // private static final int RSSI_THRESHOLD = -90;
    private static final long REMOVE_DELAY = 10000;
    private static final int SHOW_TOAST = 99;
    private static final long REMOVE_INTERVAL = 5000;
    private static final int SHOW_TOAST_VIBRATING = 90;
    private static final int POST_NOTIFICATION = 80;
    private static final int CANCEL_NOTIFICATION = 70;
    private static final String PREF_SCANNING = "isScanning";
    private static final String PREFS = "WatchInPrefs";
    private static final String EXTRA_RECEIVER = "be.ehb.watchin.services.extra.RECEIVER";

    private static final int FOUND_BEACON = 2;

    private BluetoothAdapter mBluetoothAdapter;

    private Map<String, Long> detectedDeviceList = new ConcurrentHashMap<>();
    private int postedNotificationCount;

    public static final String ACTION_SCAN = "be.ehb.watchin.services.action.START";
    public static final String ACTION_STOP = "be.ehb.watchin.services.action.STOP";


    public static final String BUN_BEACON = "be.ehb.watchin.BUN_BEACON";

    private BluetoothLeScanner mBluetoothLeScanner;
    private static ScanSettings mScanSettings;
    private static List<ScanFilter> mScanFilterList = new ArrayList<>();

    public static int RSSI_THRESHOLD = 100;

    private static ResultReceiver mReceiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public BeaconScannerService() {
        super("BeaconScanner");
    }

    public static void startScanning(Context context,ResultReceiver receiver) {
        Log.d(TAG,"StartingService");
        Intent intent = new Intent(context, BeaconScannerService.class);
        intent.setAction(ACTION_SCAN);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }

    public static void stopScanning(Context context) {
        Log.d(TAG,"StoppingService");
        Intent intent = new Intent(context, BeaconScannerService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(TAG, action);
            if (ACTION_SCAN.equals(action)) {
                ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
                handleActionScan(receiver);
            } else if (ACTION_STOP.equals(action)) {
                handleActionStop();
            }/* else if (ACTION_ADD.equals(action)){
                final int PID = intent.getIntExtra(EXTRA_PID,0);
                final int EID = intent.getIntExtra(EXTRA_EID,0);
                handleActionAdd(PID,EID);
            } else if (ACTION_DELETE.equals(action)){
                final int PID = intent.getIntExtra(EXTRA_PID,0);
                final int EID = intent.getIntExtra(EXTRA_EID,0);
                handleActionDelete(PID,EID);
            }*/
        }
    }

    private void handleActionStop() {
        Log.d(TAG,"Stopping...");
        if (initBLE()) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    private void handleActionScan(ResultReceiver receiver) {
        Log.d(TAG,"Initializing");
        mReceiver = receiver;
        if (initBLE()) {
            mBluetoothLeScanner.startScan(mScanFilterList,mScanSettings,mScanCallback);
            showNotification();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Destoying Service");
    }

    private boolean initBLE() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        mBluetoothAdapter = getBluetoothAdapter();
        if(mBluetoothAdapter == null){
            Log.d(TAG,"BluetoothAdapter Error");
            return false;
        }


        mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        String addr = "E4:7D:89:7A:AA:8C";
        //String addr = "";
        ScanFilter sf = new ScanFilter.Builder()
                .setDeviceAddress(addr)
                .build();
        //mScanFilterList.add(sf);


        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        return true;
    }

    private BluetoothAdapter getBluetoothAdapter(){
        BluetoothAdapter adapter = null;
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();

        if (adapter == null || !adapter.isEnabled()) {
            adapter.enable();
            Log.d(TAG,"enabling BLE");
        }
        return adapter;
    }


    //Scan call back function
    private static ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BUN_BEACON, result);
            mReceiver.send(FOUND_BEACON,bundle);
            /*
            if(result.getRssi()>RSSI_THRESHOLD){
                String time = DateFormat.getDateTimeInstance().format(new Date());
                Log.d(TAG, "Found device: " + result.getDevice().getAddress() + " @ " + time);
            }
            */
        }
    };


    private void showNotification(){

        Intent notificationIntent = new Intent(this, WatchInMain.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent stopIntent = new Intent(this, WatchInMain.class);
        stopIntent.setAction(WatchInMain.ACTION_STOP_SCANNING);
        PendingIntent pendingStopIntent = PendingIntent.getActivity(this, 0,
                stopIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("WatchIn Scanning")
                .setTicker("WatchIn - scanning people")
                .setSmallIcon(R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .setShowWhen(false)
                .addAction(android.R.drawable.ic_media_pause,
                        "Stop scanning", pendingStopIntent)
                .build();


        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(1, notification);




    }

}
