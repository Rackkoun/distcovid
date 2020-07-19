package cm.rulan.distcovid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import cm.rulan.distcovid.measurements.BluetoothDistanceMeasurement;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
// source: https://stackoverflow.com/questions/14228289/android-pair-devices-via-bluetooth-programmatically
// source spannable Text: https://stackoverflow.com/questions/16335178/different-font-size-of-strings-in-the-same-textview/16335416

    private static final String TAG = "MAIN_TAG";
    private static int detectionCountDown = 4;

    private static final int REQUEST_ENABLE_BT = 1;

    private Switch bluetoothSwitch, scanSwitch;
    private TextView numberOfDectectedDevices;
    private TextView closestDeviceDistance;
    //private ListView deviceListView;

    private List<BluetoothDevice> bluetoothDeviceList;
    private ArrayAdapter<String> deviceNameList;

    private List<Double> closestDevicesDistAccurate;
    private List<Double> closestDevicesDist;
    private BluetoothAdapter bluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews(){
        numberOfDectectedDevices = findViewById(R.id.number_of_devices_found_int_id);
        closestDeviceDistance = findViewById(R.id.closest_dist_id);

        setAnimationPulse();
        //deviceListView = findViewById(R.id.device_list_id);

        bluetoothSwitch = findViewById(R.id.bluetooth_on_off_id);
        scanSwitch = findViewById(R.id.scan_start_stop_id);

        bluetoothDeviceList = new ArrayList<>();
        closestDevicesDist = new ArrayList<>();
        closestDevicesDistAccurate = new ArrayList<>();
        deviceNameList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

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
                    bluetoothAdapter.cancelDiscovery();
                    scanSwitch.setActivated(false);
                    scanSwitch.setChecked(false);
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
        String pulseResource = getResources().getString(R.string.dist_init_val);
        Log.i(TAG, "Content Pulse resource before: ["+pulseResource+"]");
        reformatTextSize(pulseResource);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i(TAG, "ACTION: ["+action+"]");
                Log.i(TAG, "Discov status 1: ["+bluetoothAdapter.isDiscovering()+"]");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "SIZE OF LIST: " + bluetoothDeviceList.size());
                if(bluetoothDeviceList.size() < 1){
                    Log.i(TAG, "-- scan mode 1 --"+ bluetoothAdapter.getScanMode()+ "  scan state"+ bluetoothAdapter.getState());
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    double distance = BluetoothDistanceMeasurement.convertRSSI2MeterWithAccuracy(rssi);
                    double distance2 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 0);
                    double distance3 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 1);
                    double distance4 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 2);
                    if (device.getName() != null){
                        deviceNameList.add(device.getName()+"  --  "+distance +" meter");
                        closestDevicesDistAccurate.add(distance);

                    }else {
                        deviceNameList.add(device.getAddress()+"  --  "+distance +" meter");
                        closestDevicesDistAccurate.add(distance);
                    }
                    Log.i(TAG, "D: ["+distance+"] m");
                    Log.i(TAG, "D0: ["+distance2+"] m");
                    Log.i(TAG, "D1: ["+distance3+"] m");
                    Log.i(TAG, "D2: ["+distance4+"] m");

                    Log.i(TAG, "Device: Name: ["+device.getName()+" - - "+device.getAddress()+"]");
                    bluetoothDeviceList.add(device);
                    deviceNameList.notifyDataSetChanged();
                    final List<Double> minDist = BluetoothDistanceMeasurement.getClosetDevicesAscOrdered(closestDevicesDistAccurate);
                    String pulseMsg = minDist.get(0) + " " + "meter";
                    reformatTextSize(pulseMsg);
                    Log.i(TAG, "size of double list: ["+closestDevicesDistAccurate.size()+"]");
                    numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                }else{
                    boolean deviceIsInTheList = false;
                    for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                        if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                            deviceIsInTheList = true;
                        }
                    }

                    if(!deviceIsInTheList){
                        Log.i(TAG, "------- Discov status 3: ["+bluetoothAdapter.isDiscovering()+"]");
                        short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        double distance = BluetoothDistanceMeasurement.convertRSSI2MeterWithAccuracy(rssi);
                        double distance2 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 0);
                        double distance3 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 1);
                        double distance4 = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 2);

                        if (device.getName() != null){
                            deviceNameList.add(device.getName()+"  --  "+distance +" meter");
                            closestDevicesDistAccurate.add(distance);
                        }else {
                            deviceNameList.add(device.getAddress()+"  --  "+distance +" meter");
                            closestDevicesDistAccurate.add(distance);
                        }
                        Log.i(TAG, "D: ["+distance+"] m");
                        Log.i(TAG, "D0: ["+distance2+"] m");
                        Log.i(TAG, "D1: ["+distance3+"] m");
                        Log.i(TAG, "D2: ["+distance4+"] m");
                        Log.i(TAG, "size of double list: ["+closestDevicesDistAccurate.size()+"]");
                        final List<Double> minDist = BluetoothDistanceMeasurement.getClosetDevicesAscOrdered(closestDevicesDistAccurate);
                        String pulseMsg = minDist.get(0) + " " + "meter";
                        reformatTextSize(pulseMsg);
                        bluetoothDeviceList.add(device);
                        deviceNameList.notifyDataSetChanged();
                        numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                    }else {
                        Log.i(TAG, "------ Discov status 4: ["+bluetoothAdapter.isDiscovering()+"]");
                        Log.i(TAG, "ACTION enabled: ["+bluetoothAdapter.isEnabled()+"]");
                        Log.i(TAG, "ACTION: ["+action+"]");
                        detectionCountDown--;
                        Log.i(TAG, "Block here Device always in the list---- Count down [-- "+detectionCountDown+" --]");
                        if(detectionCountDown == 0){
                            Log.i(TAG, "Retry to set scan as finished");
                            //bluetoothAdapter.cancelDiscovery();
                            //MainActivity.this.unregisterReceiver(broadcastReceiver);
                            //IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            //MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);
                            Log.i(TAG, "size of double list: ["+closestDevicesDistAccurate.size()+"]");
                            Log.i(TAG, "Re init countDown");
                            detectionCountDown = 4;
                            Log.i(TAG, "-- scan mode 2 --"+ bluetoothAdapter.getScanMode()+ "  scan state"+ bluetoothAdapter.getState());
                            Log.i(TAG, "------ Discov status 4: ["+bluetoothAdapter.isDiscovering()+"]");
                            Log.i(TAG, "ACTION enabled: ["+bluetoothAdapter.isEnabled()+"]");
                            Log.i(TAG, "ACTION: ["+action+"]");
                            //bluetoothAdapter.startDiscovery();
                            Log.i(TAG, "-- try to relaunch discovering: re-yo --");
                            Log.i(TAG, "------ Discov status 4: ["+bluetoothAdapter.isDiscovering()+"]");
                            Log.i(TAG, "ACTION enabled: ["+bluetoothAdapter.isEnabled()+"]");
                            Log.i(TAG, "ACTION: ["+action+"]");
                        }
                    }

                }// Restart discovering after it is finished
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.i(TAG, "--- -- Get count -- "+ deviceNameList.getCount());
                Log.i(TAG, "ACTION: ["+action+"]");
                Log.i(TAG, "ACTION: ["+bluetoothAdapter.isDiscovering()+"]");
                Log.i(TAG, "ACTION: ["+bluetoothAdapter.isEnabled()+"]");
                deviceNameList.clear();
                bluetoothDeviceList.clear();
                closestDevicesDistAccurate.clear();
                numberOfDectectedDevices.setText(getResources().getText(R.string.init_value_devices_found));
                deviceNameList.notifyDataSetChanged();
                bluetoothAdapter.startDiscovery();
                Log.i(TAG, "----- End ----: ");
                if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter.isEnabled()){
                    Log.i(TAG, "-- try to relaunch discovering");

                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);
                    Log.i(TAG, "-- try to relaunch discovering: End --");

                }
            }else {
                Log.i(TAG, "-- scan mode 3 --"+ bluetoothAdapter.getScanMode()+ "  scan state"+ bluetoothAdapter.getState());
                Log.i(TAG, "-- try to relaunch discovering: re-yo --");
                Log.i(TAG, "------ Discov status 4: ["+bluetoothAdapter.isDiscovering()+"]");
                Log.i(TAG, "ACTION enabled: ["+bluetoothAdapter.isEnabled()+"]");
                Log.i(TAG, "ACTION: ["+action+"]");
            }
        }
    };

    private void resetMemberVariables(){
        closestDevicesDistAccurate.clear();
        closestDevicesDist.clear();
        bluetoothDeviceList.clear();
        deviceNameList.clear();

        numberOfDectectedDevices.setText(getResources().getText(R.string.init_value_devices_found));
        String pulseResource = getResources().getString(R.string.dist_init_val);
        Log.i(TAG, "Content Pulse resource re-init: ["+pulseResource+"]");
        reformatTextSize(pulseResource);
    }
    private void launchScan(){
        Log.i(TAG, "Scanning...");

        scanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if(!bluetoothAdapter.isEnabled()) { //
                        Toast.makeText(MainActivity.this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "BT is not enabled");// Make BT discoverable
                        scanSwitch.setText(R.string.scan_devices_default);
                        Log.i(TAG,"bt on off: "+bluetoothSwitch.isActivated()+", checked: "+bluetoothSwitch.isChecked());
                    }else {
                        scanSwitch.setText(R.string.scan_devices_default1);
                        if (bluetoothAdapter.isDiscovering()) {
                            //resetMemberVariables();
                            deviceNameList.clear();
                            bluetoothDeviceList.clear();
                            deviceNameList.notifyDataSetChanged();
                            Log.i(TAG, "discovering is canceled...");
                        } else {
                            Log.i(TAG, "-- Entry else: adapter cleared");
                            bluetoothAdapter.startDiscovery();
                            //resetMemberVariables();
                            deviceNameList.clear();
                            bluetoothDeviceList.clear();
                            deviceNameList.notifyDataSetChanged();
                            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                            MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);

                            // action when discovery is finished
                            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);

                            Log.i(TAG, "BT is discovering...");

                            //deviceListView.setAdapter(arrayAdapter);
                            numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                            //deviceNameList.notifyDataSetChanged();
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

    private void reformatTextSize(String textPulseStr){
        Log.i(TAG, "-- Set String start --");
        String[] splitedText = textPulseStr.split(" ");
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(textPulseStr);

        RelativeSizeSpan largeText = new RelativeSizeSpan(3.0f);
        RelativeSizeSpan smallText = new RelativeSizeSpan(.1f);

        stringBuilder.setSpan(largeText,
                textPulseStr.indexOf(splitedText[0]),
                textPulseStr.indexOf(splitedText[1]),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(smallText,
                textPulseStr.indexOf(splitedText[1]),
                textPulseStr.indexOf(splitedText[1]),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        closestDeviceDistance.setText(stringBuilder);
        Log.i(TAG, "Str 1: "+splitedText[0]);
        Log.i(TAG, "Str 2: "+splitedText[1]);
        Log.i(TAG, "Set String end ----");
    }

    private void setAnimationPulse(){
        closestDeviceDistance.setBackgroundResource(R.drawable.pulse_list);
        AnimationDrawable pulseAnim = (AnimationDrawable) closestDeviceDistance.getBackground();
        pulseAnim.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "-- onResume entry");
//        String pulseResource = getResources().getString(R.string.dist_init_val);
//        Log.i(TAG, "Content Pulse resource before: ["+pulseResource+"]");
//        reformatTextSize(pulseResource);
        Log.i(TAG, "onResume end ----");
    }
}
