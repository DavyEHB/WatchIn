/**
 * Created by davy.van.belle on 30/08/2016.
 */

package be.ehb.watchin.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import be.ehb.watchin.R;

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

    private BluetoothAdapter mBluetoothAdapter;

    private Map<String, Long> detectedDeviceList = new ConcurrentHashMap<>();
    private int postedNotificationCount;

    private static final String ACTION_SCAN = "be.ehb.watchin.services.action.START";
    private static final String ACTION_STOP = "be.ehb.watchin.services.action.STOP";

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
        mScanFilterList.add(sf);


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
            bundle.putParcelable("Device" , result);
            mReceiver.send(15,bundle);
            if(result.getRssi()>RSSI_THRESHOLD){
                String time = DateFormat.getDateTimeInstance().format(new Date());
                Log.d(TAG, "Found device: " + result.getDevice().getAddress() + " @ " + time);
            }
        }
    };


    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()){
            case Constants.MAIN_START:
                startScanning();
                break;
            case Constants.MAIN_STOP:
                stopScanning();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }
    */
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler = new Handler();
        mMainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message message) {
                if (message.what == SHOW_TOAST){
                    Toast.makeText(getApplicationContext(), (CharSequence) message.obj, Toast.LENGTH_SHORT).show();
                } else if (message.what == SHOW_TOAST_VIBRATING){
                    Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);
                    Toast.makeText(getApplicationContext(), (CharSequence) message.obj, Toast.LENGTH_LONG).show();
                } else if (message.what == POST_NOTIFICATION) {
                    Task task = (Task) message.obj;
                    Log.d(TAG,"Post Notif " + task.toString());
                    if (task != null){
                        CardNotification cardNotification = new CardNotification(task);

                        Notification[] notifications = cardNotification.buildNotifications(getApplicationContext());

                        // Post new notifications
                        for (int i = 0; i < notifications.length; i++) {
                            NotificationManagerCompat.from(getApplicationContext()).notify(i, notifications[i]);
                        }

                        // Cancel any that are beyond the current count.
                        for (int i = notifications.length; i < postedNotificationCount; i++) {
                            NotificationManagerCompat.from(getApplicationContext()).cancel(i);
                        }
                        postedNotificationCount = notifications.length;
                    }
                } else if (message.what == CANCEL_NOTIFICATION){
                    NotificationManagerCompat.from(getApplicationContext()).cancelAll();
                }
            }
        };



        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        if (intent.getAction().equals(Constants.START_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");

            taskArrayList.addAll(TaskDAO.getInstance(this).getAll());
            if (taskArrayList != null) {
                for (Task t : taskArrayList) {
                    t.setCards(CardDAO.getInstance(this).getByTaskID(t.getID()));
                }
            }

            Intent notificationIntent = new Intent(this, SelectorActivity.class);
            notificationIntent.setAction(Constants.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Intent startIntent = new Intent(this, BeaconScanner.class);
            startIntent.setAction(Constants.STOP_ACTION);
            PendingIntent pStopIntent = PendingIntent.getService(this, 0,
                    startIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_media_play);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("TaskAID")
                    .setTicker("TaskAID")
                    .setContentText("Scanning")
                    .setSmallIcon(R.drawable.ic_media_play)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_pause,
                            "Stop scanning", pStopIntent)
                    .build();

            startLeScan();
            scanDetectedDeviceList();

            startForeground(Constants.NOTIFICATION_ID,
                    notification);
        } else if (intent.getAction().equals(Constants.STOP_ACTION)) {
            Log.i(TAG, "Clicked Stop");
            stopLeScan();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void startLeScan() {
        mScanning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"Scanning " + i++);
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                if (mScanning) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG,"Waiting");
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            if (mScanning) {
                                startLeScan();
                            }
                        }
                    },SCAN_INTERVAL);
                }
            }
        }, SCAN_PERIOD);
    }

    private void scanDetectedDeviceList(){
        long timeNow = System.currentTimeMillis();
        Log.d(TAG,"Checking detected list");
        for (String device : detectedDeviceList.keySet()){
            Log.d(TAG,"Removable?: " + device + " - TimeStamp: " + detectedDeviceList.get(device) + " Now: " + timeNow + " Difference: " + (timeNow -  detectedDeviceList.get(device)));
            if ((detectedDeviceList.get(device) + REMOVE_DELAY) <= timeNow ){

                Message message = mMainHandler.obtainMessage(SHOW_TOAST,"Removed: " + device);
                message.sendToTarget();
                message = mMainHandler.obtainMessage(CANCEL_NOTIFICATION);
                message.sendToTarget();
                detectedDeviceList.remove(device);
            }
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScanning) {
                    scanDetectedDeviceList();
                }
            }
        },REMOVE_INTERVAL);
    }

    private void stopLeScan(){
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private static int i;
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (rssi >= RSSI_THRESHOLD) {
                        if (!detectedDeviceList.containsKey(device.getAddress())) {
                            Task task = checkTaskList(device);
                            if (task != null) {
                                Log.d(TAG, "Task detected: " + task.toString());

                                //Message message = mMainHandler.obtainMessage(SHOW_TOAST_VIBRATING,"Task detected: " + task.toString());
                                Message message = mMainHandler.obtainMessage(POST_NOTIFICATION,task);
                                message.sendToTarget();
                                detectedDeviceList.put(device.getAddress(),System.currentTimeMillis());
                            }
                        }
                        detectedDeviceList.put(device.getAddress(), System.currentTimeMillis());
                    }
                }
            };


    private Task checkTaskList(BluetoothDevice device) {
        for (Task  task : taskArrayList){
            if (device.getAddress().equals(task.getBeaconAddress())){
                return task;
            }
        }
        return null;
    }
    */
}
