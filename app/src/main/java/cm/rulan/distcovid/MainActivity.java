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
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
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

        initViews(); // initialize views
        setTransitionAnimation(); // set animation
        setViewComponents(); // set event listener to switch buttons
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

        dbHelper = new StatsDataDB(this); // initialize the DB-helper for database transaction
        database = dbHelper.getWritableDatabase();
    }

    private void bluetoothOnOff() {
        // Enable and disable bluetooth when the check state is true
        bluetoothSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // enable BT
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    bluetoothSwitch.setText(R.string.bluetooth_default1);

                    bluetoothSwitch.setChecked(true);
                    Log.i(TAG, "BT is enabled");
                }
            } else {
                Log.i(TAG, "Disable BT");
                bluetoothAdapter.disable(); // bluetooth disabling here
                bluetoothSwitch.setText(R.string.bluetooth_default);
                bluetoothSwitch.setChecked(false);
                bluetoothAdapter.cancelDiscovery();
                scanSwitch.setActivated(false); // be sure to disable the switch button for device discovery
                scanSwitch.setChecked(false);
            }
        });
    }

    private void setViewComponents() {
        // Set value for all views in the main screen
        bluetoothOnOff();
        launchScan();
        String pulseResource = getResources().getString(R.string.default_pulse_msg);
        reformatTextSize(pulseResource);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // get the bluetooth action

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothDeviceList.size() < 1) {
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                    if (device.getName() != null) {// the app will crash if the device name is null
                        Log.i(TAG, "Device is not null");
                        deviceNameList.add(device.getName());
                        bluetoothDeviceList.add(device);
                        deviceNameList.notifyDataSetChanged();
                        estimateSignalStrength(rssi); // estimate the device distance through its emitted signal
                        numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                    }

                } else {
                    boolean deviceIsInTheList = false;
                    for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                        if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                            deviceIsInTheList = true;
                        }
                    }

                    if (!deviceIsInTheList) { // be sure to do not add the same device in the list more than one
                        short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE); // get signal strength

                        if (device.getName() != null) {
                            Log.i(TAG, "Device is not null [else]");
                            deviceNameList.add(device.getName());

                            bluetoothDeviceList.add(device);
                            deviceNameList.notifyDataSetChanged();
                            estimateSignalStrength(rssi);
                            numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                        }
                    }


                }// Restart discovering after it is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                deviceNameList.clear();
                bluetoothDeviceList.clear();
                closestDevicesList.clear();
                numberOfDectectedDevices.setText(getResources().getText(R.string.init_value_devices_found));
                deviceNameList.notifyDataSetChanged();
                bluetoothAdapter.startDiscovery();

                if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter.isEnabled()) {
                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);
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
        scanSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!bluetoothSwitch.isChecked() || bluetoothSwitch.isActivated()) { //
                    Toast.makeText(MainActivity.this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                    scanSwitch.setText(R.string.scan_devices_default);
                    scanSwitch.setChecked(false);
                    scanSwitch.setActivated(false);
                } else {
                    scanSwitch.setText(R.string.scan_devices_default1);
                    Toast.makeText(MainActivity.this, "Discovering is activated", Toast.LENGTH_SHORT).show();
                    if (bluetoothAdapter.isDiscovering()) {

                        deviceNameList.clear();
                        bluetoothDeviceList.clear();
                        deviceNameList.notifyDataSetChanged();
                    } else {
                        bluetoothAdapter.startDiscovery();
                        deviceNameList.clear();
                        bluetoothDeviceList.clear();
                        deviceNameList.notifyDataSetChanged();
                        closestDevicesList.clear();
                        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);

                        // notify the broadcast when the discovery is finished
                        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        MainActivity.this.registerReceiver(broadcastReceiver, intentFilter);

                        numberOfDectectedDevices.setText(String.valueOf(deviceNameList.getCount()));
                    }

                }
            } else {
                Log.i(TAG, "Discovering stopped");
                bluetoothAdapter.cancelDiscovery();
                scanSwitch.setText(R.string.scan_devices_default);
                scanSwitch.setChecked(false);
                resetMemberVariables();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(broadcastReceiver);
    }

    private void reformatTextSize(String textPulseStr) {
        // Format the text to fit into the pulse animation
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
        // Insert the notified distance value to the DB
        long datetime = System.currentTimeMillis();

        dbHelper.insertValue(distance, datetime);
        dbHelper.close();
        Log.d(TAG, "Value: ( " + distance + " ) saved in the DB");

        DistcovidModelObject warning = new DistcovidModelObject(distance, datetime);
        warning.setFormattedDate(sdf.format(new Date(datetime)));
        warning.setFormattedTime(sdf_time.format(new Date(datetime)));
    }

    private void estimateSignalStrength(short rssi) {
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
            insertDistance(distance); // insert value in the DB
            alertUser(estimation); // then notify the user

        } else if (((rssi >= -82) && (rssi <= -68))) {
            double distance = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 3);
            closestDevicesList.add(distance);
            Collections.sort(closestDevicesList);

            estimation = "Low signal approx in " + closestDevicesList.get(0) + " meter";
            String second = 0 + " " + "meter";

            if (closestDevicesList.size() > 1) {
                second = closestDevicesList.get(1) + " " + "meter";
            }
            reformatTextSize(estimation);
            nextClosestDeviceDistance.setText(second);
        } else {
            final double distance = BluetoothDistanceMeasurement.convertRSSI2Meter(rssi, 3);
            Log.i(TAG, "Not to approximate: strength [" + rssi + " dbm] for distance [" + distance + " m]");
        }
    }

    // Push notification
    private void alertUser(String message) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // Config Notification
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        } else {
            Log.i(TAG, "Manager is null");
        }
    }
}