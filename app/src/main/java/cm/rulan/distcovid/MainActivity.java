package cm.rulan.distcovid;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import cm.rulan.distcovid.database.StatsDataDB;
import cm.rulan.distcovid.measurements.BluetoothDistanceMeasurement;
import cm.rulan.distcovid.model.DistcovidModelObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
// source: https://stackoverflow.com/questions/14228289/android-pair-devices-via-bluetooth-programmatically
// source spannable Text: https://stackoverflow.com/questions/16335178/different-font-size-of-strings-in-the-same-textview/16335416

    private static final String TAG = "MAIN_TAG";

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf_time = new SimpleDateFormat("hh:mm:ss");

    private static final int REQUEST_ENABLE_BT = 1;

    private Switch bluetoothSwitch, scanSwitch;
    private TextView numberOfDectectedDevices;
    private TextView closestDeviceDistance, nextClosestDeviceDistance;

    private List<BluetoothDevice> bluetoothDeviceList;
    private ArrayAdapter<String> deviceNameList;

    private List<Double> closestDevicesList;

    private BluetoothAdapter bluetoothAdapter = null;

    private StatsDataDB dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setTransitionAnimation();
        setViewComponents();
    }

    private void initViews() {
        numberOfDectectedDevices = findViewById(R.id.number_of_devices_found_int_id);
        closestDeviceDistance = findViewById(R.id.closest_dist_id);
        nextClosestDeviceDistance = findViewById(R.id.next_closest_value_id);
        setAnimationPulse();

        bluetoothSwitch = findViewById(R.id.bluetooth_on_off_id);
        scanSwitch = findViewById(R.id.scan_start_stop_id);

        bluetoothDeviceList = new ArrayList<>();
        closestDevicesList = new ArrayList<>();
        deviceNameList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        dbHelper = new StatsDataDB(this);
        database = dbHelper.getWritableDatabase();
    }

    private void bluetoothOnOff() {
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // enable BT
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        bluetoothSwitch.setText(R.string.bluetooth_default1);
                        //isChecked = true;
                        bluetoothSwitch.setChecked(true);
                        Log.i(TAG, "BT is enabled");
                    }
                    Log.i(TAG, "BT on: [" + isChecked + "]");
                } else {
                    Log.i(TAG, "Disable BT");
                    bluetoothAdapter.disable();
                    bluetoothSwitch.setText(R.string.bluetooth_default);
                    bluetoothSwitch.setChecked(false);
                    Log.i(TAG, "BT off: [" + isChecked + "]");
                    bluetoothAdapter.cancelDiscovery();
                    scanSwitch.setActivated(false);
                    scanSwitch.setChecked(false);
                }
                Log.i(TAG, "Text: [" + bluetoothSwitch.getText().toString() + "]");
                Log.i(TAG, "Text: [" + bluetoothSwitch.getTextOn().toString() + "]");
                Log.i(TAG, "Text: [" + bluetoothSwitch.getTextOff().toString() + "]");
                Log.i(TAG, "Text: [" + bluetoothSwitch.isChecked() + "]");
                Log.i(TAG, "Text: [" + bluetoothSwitch.isActivated() + "]");
            }
        });
    }

    private void setViewComponents() {
        Log.i(TAG, "Set view components -- start");
        bluetoothOnOff();
        launchScan();
        String pulseResource = getResources().getString(R.string.default_pulse_msg);
        Log.i(TAG, "Content Pulse resource before: [" + pulseResource + "]");
        reformatTextSize(pulseResource);
        Log.i(TAG, "Set view components -- end");
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i(TAG, "ACTION: [" + action + "]");
                Log.i(TAG, "Discov status 1: [" + bluetoothAdapter.isDiscovering() + "]");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "SIZE OF LIST: " + bluetoothDeviceList.size());
                if (bluetoothDeviceList.size() < 1) {
                    Log.i(TAG, "-- scan mode 1 --" + bluetoothAdapter.getScanMode() + "  scan state" + bluetoothAdapter.getState());
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                    if (device.getName() != null) {
                        Log.i(TAG, "<----- Start: Name: " + device.getName() + " ---");
                        deviceNameList.add(device.getName());
                        Log.i(TAG, "Name: " + device.getName() + " ---- END --->");

                        Log.i(TAG, "Device: Name: [" + device.getName() + " - - " + device.getAddress() + "]");
                        bluetoothDeviceList.add(device);
                        deviceNameList.notifyDataSetChanged();
                        estimateSignalStrength(rssi);
                        numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                    }

                } else {
                    boolean deviceIsInTheList = false;
                    for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                        if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                            deviceIsInTheList = true;
                        }
                    }

                    if (!deviceIsInTheList) {
                        Log.i(TAG, "------- Discov status 3: [" + bluetoothAdapter.isDiscovering() + "]");
                        short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                        if (device.getName() != null) {
                            Log.i(TAG, "<----- Start: Name: " + device.getName() + " ---");
                            deviceNameList.add(device.getName());

                            bluetoothDeviceList.add(device);
                            deviceNameList.notifyDataSetChanged();
                            estimateSignalStrength(rssi);
                            numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                            Log.i(TAG, "Name: " + device.getName() + " ---- END --->");
                        }
                    }


                }// Restart discovering after it is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "--- -- Get count -- " + deviceNameList.getCount());
                Log.i(TAG, "ACTION: [" + action + "]");
                deviceNameList.clear();
                bluetoothDeviceList.clear();
                closestDevicesList.clear();
                numberOfDectectedDevices.setText(getResources().getText(R.string.init_value_devices_found));
                deviceNameList.notifyDataSetChanged();
                bluetoothAdapter.startDiscovery();
                Log.i(TAG, "----- End ----: ");
                if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter.isEnabled()) {
                    Log.i(TAG, "-- try to relaunch discovering");

                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);
                    Log.i(TAG, "-- try to relaunch discovering: End --");

                }
            }
        }
    };

    private void resetMemberVariables() {
        closestDevicesList.clear();
        bluetoothDeviceList.clear();
        deviceNameList.clear();
        String second = 0 + " " + "meter";
        nextClosestDeviceDistance.setText(second);
        numberOfDectectedDevices.setText(getResources().getText(R.string.init_value_devices_found));
        String pulseResource = getResources().getString(R.string.default_pulse_msg);
        Log.i(TAG, "Content Pulse resource re-init: [" + pulseResource + "]");
        reformatTextSize(pulseResource);
    }

    private void launchScan() {
        Log.i(TAG, "Scanning...");

        scanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!bluetoothSwitch.isChecked() || bluetoothSwitch.isActivated()) { //
                        Toast.makeText(MainActivity.this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "BT is not enabled");// Make BT discoverable
                        scanSwitch.setText(R.string.scan_devices_default);
                        scanSwitch.setChecked(false);
                        scanSwitch.setActivated(false);
                        Log.i(TAG, "bt on off: " + bluetoothSwitch.isActivated() + ", checked: " + bluetoothSwitch.isChecked());
                    } else {
                        scanSwitch.setText(R.string.scan_devices_default1);
                        Toast.makeText(MainActivity.this, "Discovering is activated", Toast.LENGTH_SHORT).show();
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
                            closestDevicesList.clear();
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
                } else {
                    Log.i(TAG, "Discovering stopped");
                    bluetoothAdapter.cancelDiscovery();
                    scanSwitch.setText(R.string.scan_devices_default);
                    scanSwitch.setChecked(false);
                    resetMemberVariables();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(broadcastReceiver);
    }

    private void reformatTextSize(String textPulseStr) {
        Log.i(TAG, "-- Set String [" + textPulseStr + "] start --");
        String[] splitedText = textPulseStr.split(" ");

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(textPulseStr);

        RelativeSizeSpan largeText = new RelativeSizeSpan(1.5f);
        RelativeSizeSpan midlleText = new RelativeSizeSpan(1.0f);
        RelativeSizeSpan smallText = new RelativeSizeSpan(.6f);

        stringBuilder.setSpan(largeText,
                textPulseStr.indexOf(splitedText[0]),
                textPulseStr.indexOf(splitedText[2]),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(smallText,
                textPulseStr.indexOf(splitedText[2]),
                textPulseStr.indexOf(splitedText[4]),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        stringBuilder.setSpan(largeText,
                textPulseStr.indexOf(splitedText[4]),
                textPulseStr.indexOf(splitedText[4]),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        stringBuilder.setSpan(midlleText,
                textPulseStr.indexOf(splitedText[5]),
                textPulseStr.indexOf(splitedText[5]),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // replace space with backslash in the string builder
        stringBuilder.replace(textPulseStr.indexOf(splitedText[1]) + (splitedText[1].length()),
                textPulseStr.indexOf(splitedText[1]) + (splitedText[1].length()), "\n");

        stringBuilder.replace(textPulseStr.indexOf(splitedText[3]) + (splitedText[3].length() + 1),
                textPulseStr.indexOf(splitedText[3]) + (splitedText[3].length() + 1), "\n");

        closestDeviceDistance.setText(stringBuilder);

        Log.i(TAG, "Str builder after: " + stringBuilder);
        Log.i(TAG, "Set String end ----");
    }

    private void setAnimationPulse() {
        closestDeviceDistance.setBackgroundResource(R.drawable.pulse_list);
        AnimationDrawable pulseAnim = (AnimationDrawable) closestDeviceDistance.getBackground();
        pulseAnim.start();
    }

    private void setTransitionAnimation() {
        if (Build.VERSION.SDK_INT > 20) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.TOP);
            slide.setDuration(350);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }

    public void goToStatisticActivity(View view) {
        Intent intent = new Intent(this, StatisticsActivity.class);

        if (Build.VERSION.SDK_INT > 20) {
            ActivityOptions activityOptions;
            activityOptions = ActivityOptions
                    .makeSceneTransitionAnimation(this);
            startActivity(intent, activityOptions.toBundle());
        } else {
            startActivity(intent);
        }
    }

    // add new value to the DB
    private void insertDistance(double distance) {
        Log.i(TAG, "Entry in insertDistance: [MainActivity]");
        long datetime = System.currentTimeMillis();
        Log.i(TAG, "Starting insertion: [MainActivity]");
        dbHelper.insertValue(distance, datetime);
        Log.i(TAG, "Insertion Successful");
        //database.endTransaction();
        //database.close();
        Log.i(TAG, "Closing DBHelper");
        dbHelper.close();
        Log.i(TAG, "DBHelper closed successfully");
        Log.d(TAG, "Value: ( " + distance + " ) saved in the DB");

        DistcovidModelObject warning = new DistcovidModelObject(distance, datetime);
        warning.setFormattedDate(sdf.format(new Date(datetime)));
        warning.setFormattedTime(sdf_time.format(new Date(datetime)));
        Log.i(TAG, "Exiting insertDistance [MainActivity]");
        //onUpdateGraph(warning);
    }

    private void estimateSignalStrength(short rssi) {
        Log.i(TAG, "Estimation started ------->");
        // use the range n = 10
        // Low signal approx in distance meter
        String estimation;

        if (((rssi >= -62) && (rssi <= -48))) {
            double distance = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 3);

            closestDevicesList.add(distance);
            Collections.sort(closestDevicesList);
            estimation = "High signal approx in " + closestDevicesList.get(0) + " meter";

            String second = 0 + " " + "meter";
            if (closestDevicesList.size() > 1) {
                second = closestDevicesList.get(1) + " " + "meter";
            }
            reformatTextSize(estimation);
            nextClosestDeviceDistance.setText(second);

            Log.i(TAG, " size of list for next device: ---- " + closestDevicesList.size() + " ----");
            insertDistance(distance);
            Log.i(TAG, "Starting vibration [estimateDistance]");
            // vibrate, notify and then update view
            //vibrate();
            Log.i(TAG, "Ending vibration [estimateDistance]");
            Log.i(TAG, "Starting alert");
            alertUser(estimation);
            Log.i(TAG, "Alert ended");

        } else if (((rssi >= -82) && (rssi <= -68))) {
            double distance = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 3);
            closestDevicesList.add(distance);
            Collections.sort(closestDevicesList);

            estimation = "Low signal approx in " + closestDevicesList.get(0) + " meter";
            //updateViews(closestDevicesDistAccurate);
            String second = 0 + " " + "meter";
            if (closestDevicesList.size() > 1) {
                second = closestDevicesList.get(1) + " " + "meter";
            }
            reformatTextSize(estimation);
            nextClosestDeviceDistance.setText(second);
            Log.i(TAG, "Estimation: " + estimation);
        } else {
            final double distance = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 3);
            Log.i(TAG, "Not to approximate: strength [" + rssi + " dbm] for distance [" + distance + " m]");
        }

        Log.i(TAG, "Estimation ended <-------");
    }

    // Push notification
    private void alertUser(String message){
        Log.i(TAG, "--- Notification called---");
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Log.i(TAG, "Config Notification--- ");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        Notification notification = new Notification();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        notificationBuilder.setDefaults(notification.defaults);
        notificationBuilder.setContentTitle("Alert: Someone is very close to you ")
                    .setContentText(message)
                .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.distcovid_launcher_round)
                    .setContentIntent(pendingIntent);
        Log.i(TAG, "Config Notification finish --- ");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null){
            Log.i(TAG, "Manager is not null");
            notificationManager.notify(0, notificationBuilder.build());
        }else{
            Log.i(TAG, "Manager is null");
        }
        //if (notificationManager != null){
           // Log.i(TAG, "--- Notification manager is not null ---");
//            Notification notification = new Notification();
//
//
//            notification.flags |= Notification.FLAG_AUTO_CANCEL;
//
//            notificationManager.notify(0, notification);
       // }else {
        //    Log.i(TAG, "--- Notification manager is null ---");
        //}
        Log.i(TAG, "--- Notification call ended ---");
    }
}