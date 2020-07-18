package cm.rulan.distcovid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import cm.rulan.distcovid.measurements.BluetoothDistanceMeasurement;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
// source: https://stackoverflow.com/questions/14228289/android-pair-devices-via-bluetooth-programmatically

    private static final String TAG = "MAIN_TAG";

    private static final int REQUEST_ENABLE_BT = 1;

    private Switch bluetoothSwitch, scanSwitch;
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
        numberOfDectectedDevices = findViewById(R.id.number_of_devices_found_int_id);
        deviceListView = findViewById(R.id.device_list_id);

        bluetoothSwitch = findViewById(R.id.bluetooth_on_off_id);
        scanSwitch = findViewById(R.id.scan_start_stop_id);

        bluetoothDeviceList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void bluetoothOnOff(){
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    // enable BT
                    if(!bluetoothAdapter.isEnabled()){
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        bluetoothSwitch.setText(R.string.bluetooth_default1);
                        //isChecked = true;
                        bluetoothSwitch.setChecked(true);
                        Log.i(TAG, "BT is enabled");
                    }
                    Log.i(TAG, "BT on: ["+isChecked+"]");
                }else{
                    Log.i(TAG, "Disable BT");
                    bluetoothAdapter.disable();
                    bluetoothSwitch.setText(R.string.bluetooth_default);
                    bluetoothSwitch.setChecked(false);
                    Log.i(TAG, "BT off: ["+isChecked+"]");
                }
                Log.i(TAG, "Text: ["+bluetoothSwitch.getText().toString()+"]");
                Log.i(TAG, "Text: ["+bluetoothSwitch.getTextOn().toString()+"]");
                Log.i(TAG, "Text: ["+bluetoothSwitch.getTextOff().toString()+"]");
                Log.i(TAG, "Text: ["+bluetoothSwitch.isChecked()+"]");
                Log.i(TAG, "Text: ["+bluetoothSwitch.isActivated()+"]");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothOnOff();
        launchScan();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i(TAG, "ACTION: ["+action+"]");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "SIZE OF LIST: " + bluetoothDeviceList.size());
                if(bluetoothDeviceList.size() < 1){
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
                    numberOfDectectedDevices.setText(String.valueOf(arrayAdapter.getCount()));
                }else{
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
                        numberOfDectectedDevices.setText(String.valueOf(arrayAdapter.getCount()));
                    }
                }// Restart discovering after it is finished
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && scanSwitch.isActivated()){
                Log.i(TAG, "--- -- Get count -- "+ arrayAdapter.getCount());
                Log.i(TAG, "ACTION: ["+action+"]");
                Log.i(TAG, "ACTION: ["+bluetoothAdapter.isDiscovering()+"]");
                Log.i(TAG, "ACTION: ["+bluetoothAdapter.isEnabled()+"]");
                Log.i(TAG, "----- End ----: ");
                if (!bluetoothAdapter.isDiscovering()){// && bluetoothAdapter.isEnabled()){
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

        scanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if(!bluetoothAdapter.isEnabled()) {
                        Toast.makeText(MainActivity.this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "BT is not enabled");// Make BT discoverable
                        scanSwitch.setText(R.string.scan_devices_default);
                        scanSwitch.setChecked(false);
                    }else {
                        scanSwitch.setText(R.string.scan_devices_default1);
                        if (bluetoothAdapter.isDiscovering()) {
                            arrayAdapter.clear();
                            bluetoothDeviceList.clear();
                            arrayAdapter.notifyDataSetChanged();
                            Log.i(TAG, "discovering is canceled...");
                        } else {
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
                            numberOfDectectedDevices.setText(String.valueOf(arrayAdapter.getCount()));
                            arrayAdapter.notifyDataSetChanged();
                            Log.i(TAG, "End of else--");
                        }

                    }
                }else {
                    Log.i(TAG, "Discovering stopped");
                    bluetoothAdapter.cancelDiscovery();
                    scanSwitch.setText(R.string.scan_devices_default);
                    scanSwitch.setChecked(false);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(broadcastReceiver);
    }

}
