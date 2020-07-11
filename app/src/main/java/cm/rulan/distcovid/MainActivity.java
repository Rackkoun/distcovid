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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
// source: https://stackoverflow.com/questions/14228289/android-pair-devices-via-bluetooth-programmatically

    private static final String TAG = "MAIN_TAG";

    private static final int REQUEST_ENABLE_BT = 1;

    private Button launchScanBtn;
    private TextView numberOfDectectedDevices;
    private ListView deviceListView;

    private List<String> deviceNameList;
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

        deviceNameList = new ArrayList<String>();
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
                    arrayAdapter.add(device.getName());

                    Log.i(TAG, "Device: Name: ["+device.getName()+" - - "+device.getAddress()+"]");
                    bluetoothDeviceList.add(device);
                    arrayAdapter.notifyDataSetChanged();
                    numberOfDectectedDevices.setText("Number of devices found: [" + arrayAdapter.getCount()+"]");
                }else{
                    Log.i(TAG, "SIZE OF LIST: " + bluetoothDeviceList.size());
                    boolean deviceIsInTheList = false;
                    for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                        if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                            deviceIsInTheList = true;
                        }
                    }
                    if(!deviceIsInTheList){
                        arrayAdapter.add(device.getName());

                        bluetoothDeviceList.add(device);
                        for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                            deviceNameList.add(bluetoothDevice.getName());
                        }
                        arrayAdapter.notifyDataSetChanged();
                        numberOfDectectedDevices.setText("Number of devices found: [" + arrayAdapter.getCount()+"]");
                    }
                }
            }
        }
    };

    private void launchScan(){
        Log.i(TAG, "Scanning...");

        // enable BT
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            Log.i(TAG, "BT is enabled");
        }

        // Make BT discoverable
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "discovering is canceled...");
        }
        Log.i(TAG, "BT is discovering...");
        //make Bluetooth discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500);

        for(String b: deviceNameList)
            Log.i(TAG, "LIST: str"+ b);

        deviceListView.setAdapter(arrayAdapter);
        numberOfDectectedDevices.setText("Number of devices found: [" + arrayAdapter.getCount()+"]");
        arrayAdapter.notifyDataSetChanged();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);
        bluetoothAdapter.startDiscovery();
        startActivity(discoverableIntent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(broadcastReceiver);
    }

}
