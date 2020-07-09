package cm.rulan.distcovid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Home";
    private static final int REQUEST_ENABLE_BT = 1;
    Set<BluetoothDevice> pairedDevices;
    //private BluetoothAdapter bluethoothadapter;
    private Button listBtn, scanBtn;
    private ListView listViewPaired;
    private ArrayAdapter<String> adapter;
    ProgressDialog mProgressDlg;
    ////////////////////////
    private ArrayList<String> arrayOfFoundBTDevices;
    BluetoothAdapter bluethoothadapterscan ;
    private static final int SELECT_DEVICE_REQUEST_CODE = 42;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(context, "ACTION_FOUND", Toast.LENGTH_SHORT).show();

                if (bluetoothDevice != null) {
                    String devName = bluetoothDevice.getName();
                    String devadress = bluetoothDevice.getAddress();
                    Log.d(TAG, "DEVICE NAME BT: " + devName + devadress);
                    adapter.add(devName + "\t" + devadress);
                    //adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "DEVICE NAME: " + devName, Toast.LENGTH_LONG).show();

                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StatisticActivity.class);
                startActivity(intent);
            }
        });
        mProgressDlg = new ProgressDialog(getApplicationContext());

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getApplicationContext().registerReceiver(mReceiver, filter);

        // 1)
        activateBluetooth();
        // 2- Initialisierungen von Views
        initViews();
        // 3- List of paired Devices
        listPairedDevices();
        // 4- Scan devices
        scandevices();
    }

    private void activateBluetooth() {
        BluetoothAdapter bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        if (bluethoothadapter == null) {
            AlertDialog.Builder dialbuild = new AlertDialog.Builder(this);
            dialbuild.setTitle("Nicht Kompatibel")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        //3-) if supported then enables it
        if (!bluethoothadapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void initViews() {
        scanBtn = findViewById(R.id.btnscan);
        listBtn = findViewById(R.id.listpd);
        listViewPaired = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
        listViewPaired.setAdapter(adapter);
        BluetoothAdapter bluethoothadapter = BluetoothAdapter.getDefaultAdapter();

        //1-) set default manager
        pairedDevices = bluethoothadapter.getBondedDevices();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        listPairedDevices();
    }

    public void onListingBluetoothDevices() {

        if (pairedDevices.size() > 0) {
            adapter.clear();
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device.getName() + "\t" + "\t" + device.getAddress());
                Log.d(TAG, device.getName() + "\t" + device.getAddress());
                adapter.notifyDataSetChanged();
            }
        }
    }

    // ce button ci
    public void listPairedDevices() {
        listBtn = findViewById(R.id.listpd);
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListingBluetoothDevices();
            }
        });
    }
    private void scandevices(){
        scanBtn = findViewById(R.id.btnscan);
        scanBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                    arrayOfFoundBTDevices = new ArrayList<String>();
                     // start looking for bluetooth devices
                bluethoothadapterscan = BluetoothAdapter.getDefaultAdapter();
                    bluethoothadapterscan.startDiscovery();

                    // Discover new devices
                    // Create a BroadcastReceiver for ACTION_FOUND
                    /*final BroadcastReceiver mReceiver = new BroadcastReceiver()
                    {
                        @Override
                        public void onReceive(Context context, Intent intent) {

                                String action = intent.getAction();
                                // When discovery finds a device
                                if (BluetoothDevice.ACTION_FOUND.equals(action))
                                {
                                    // Get the bluetoothDevice object from the Intent
                                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                    String a = device.getName();
                                    String b = device.getAddress();
                                    adapter.add(a + "\t" + b);
                                    Log.d(TAG, a + "\t" + b);
                                    adapter.notifyDataSetChanged();

                                }
                    }
                    };*/


            }

        });
    }


    @Override
    public void onResume() {
        super.onResume();
        final BluetoothAdapter bluethoothadapter = BluetoothAdapter.getDefaultAdapter();

        bluethoothadapter.startDiscovery();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_DEVICE_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK) {
            // User has chosen to pair with the Bluetooth device.
            BluetoothDevice deviceToPair = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            deviceToPair.createBond();
        }
    }

}