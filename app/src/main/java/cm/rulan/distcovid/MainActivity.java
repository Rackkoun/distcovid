package cm.rulan.distcovid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import cm.rulan.distcovid.measurements.BluetoothDistanceMeasurement;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
// source: https://stackoverflow.com/questions/14228289/android-pair-devices-via-bluetooth-programmatically

    private static final String TAG = "MAIN_TAG";

    private static final int REQUEST_ENABLE_BT = 1;

    private Button launchScanBtn;
    private TextView numberOfDectectedDevices;
    private ListView deviceListView;

    private List<BluetoothDevice> bluetoothDeviceList;
    private ArrayAdapter<String> arrayAdapter;

    private BluetoothAdapter bluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews(){
        launchScanBtn = findViewById(R.id.scan_devices_btn_id);
        numberOfDectectedDevices = findViewById(R.id.number_of_devices_found_id);
        deviceListView = findViewById(R.id.device_list_id);

        bluetoothDeviceList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        launchScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScan();
            }
        });
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i(TAG, "ACTION: ["+action+"]");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(bluetoothDeviceList.size() < 1){
                    Log.i(TAG, "SIZE OF LIST: " + bluetoothDeviceList.size());
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    double distance = BluetoothDistanceMeasurement.convertRSSI2MeterWithAccuracy(rssi);
                    double distance2 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 0);
                    double distance3 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 1);
                    double distance4 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 2);
                    if (device.getName() != null){
                        arrayAdapter.add(device.getName()+"  --  "+distance +" meter");

                    }else {
                        arrayAdapter.add(device.getAddress()+"  --  "+distance +" meter");
                    }
                    Log.i(TAG, "D: ["+distance+"] m");
                    Log.i(TAG, "D0: ["+distance2+"] m");
                    Log.i(TAG, "D1: ["+distance3+"] m");
                    Log.i(TAG, "D2: ["+distance4+"] m");

                    Log.i(TAG, "Device: Name: ["+device.getName()+" - - "+device.getAddress()+"]");
                    bluetoothDeviceList.add(device);
                    arrayAdapter.notifyDataSetChanged();
                    numberOfDectectedDevices.setText("Number of devices found: [" + String.valueOf(arrayAdapter.getCount())+"]");
                }else{
                    Log.i(TAG, "SIZE OF LIST: " + bluetoothDeviceList.size());
                    boolean deviceIsInTheList = false;
                    for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                        if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                            deviceIsInTheList = true;
                        }
                    }
                    if(!deviceIsInTheList){
                        short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        double distance = BluetoothDistanceMeasurement.convertRSSI2MeterWithAccuracy(rssi);
                        double distance2 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 0);
                        double distance3 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 1);
                        double distance4 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 2);

                        if (device.getName() != null){
                            arrayAdapter.add(device.getName()+"  --  "+distance +" meter");
                        }else {
                            arrayAdapter.add(device.getAddress()+"  --  "+distance +" meter");
                        }
                        Log.i(TAG, "D: ["+distance+"] m");
                        Log.i(TAG, "D0: ["+distance2+"] m");
                        Log.i(TAG, "D1: ["+distance3+"] m");
                        Log.i(TAG, "D2: ["+distance4+"] m");

                        bluetoothDeviceList.add(device);
                        arrayAdapter.notifyDataSetChanged();
                        numberOfDectectedDevices.setText("Number of devices found: [" + arrayAdapter.getCount()+"]");
                    }
                }// Restart discovering after it is finished
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.i(TAG, "--- -- Get count -- "+ arrayAdapter.getCount());
                Log.i(TAG, "ACTION: ["+action+"]");
                Log.i(TAG, "ACTION: ["+bluetoothAdapter.isDiscovering()+"]");
                Log.i(TAG, "ACTION: ["+bluetoothAdapter.isEnabled()+"]");
                Log.i(TAG, "----- End ----: ");
                if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter.isEnabled()){
                    Log.i(TAG, "-- try to relaunch discovering");
                    arrayAdapter.clear();
                    bluetoothDeviceList.clear();
                    arrayAdapter.notifyDataSetChanged();
                    bluetoothAdapter.startDiscovery();

                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);
                    Log.i(TAG, "-- try to relaunch discovering: End --");

                }
            }
        }
    };

    private void launchScan(){
        Log.i(TAG, "Scanning...");

        // enable BT
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.i(TAG, "BT is enabled");
        }

        // Make BT discoverable
        if(bluetoothAdapter.isDiscovering()){
            arrayAdapter.clear();
            bluetoothDeviceList.clear();
            arrayAdapter.notifyDataSetChanged();
            Log.i(TAG, "discovering is canceled...");
        }
        else {
            Log.i(TAG, "-- Entry else: adapter cleared");
            bluetoothAdapter.startDiscovery();
            arrayAdapter.clear();
            bluetoothDeviceList.clear();
            arrayAdapter.notifyDataSetChanged();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);

            // action when discovery is finished
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);

            Log.i(TAG, "BT is discovering...");

            deviceListView.setAdapter(arrayAdapter);
            numberOfDectectedDevices.setText("Number of devices found: [" + arrayAdapter.getCount() + "]");
            arrayAdapter.notifyDataSetChanged();
            Log.i(TAG, "End of else--");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(broadcastReceiver);
    }

}
