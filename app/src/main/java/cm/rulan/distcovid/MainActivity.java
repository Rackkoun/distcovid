package cm.rulan.distcovid;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Set;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "Home";
    private static final int REQUEST_ENABLE_BT = 1;
    //private static final int REQUEST_DISCOVERABLE_BT = 0;
    Set<BluetoothDevice> pairedDevices;
    //private HomeViewModel homeViewModel;
    private BluetoothAdapter bluethoothadapter;
    private Button listBtn, scanBtn;
    private ListView foundDevicesListView;
    private ArrayAdapter<String> deviceListAdapter;
    Context context = null;
    FloatingActionButton fab;

    private AppBarConfiguration mAppBarConfiguration;
    IntentFilter filter;
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (bluetoothDevice != null) {
                    String  devName = bluetoothDevice.getName();
                    String hardadresse = bluetoothDevice.getAddress();
                    int  bondstate = bluetoothDevice.getBondState();

                    //deviceList.add(devName);
                    System.out.println("DEVICE NAME BT: " + devName);
                    deviceListAdapter.add(devName);
                    deviceListAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "DEVICE NAME: "+ devName, Toast.LENGTH_LONG).show();

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

        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluethoothadapter.getBondedDevices();

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
        //listBtn = findViewById(R.id.listpairedbt);
        //listBtn.setEnabled(false);
        foundDevicesListView = findViewById(R.id.listView);
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
        foundDevicesListView.setAdapter(deviceListAdapter);

        //1-) set default manager
        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluethoothadapter.getBondedDevices();
        scanBtn = findViewById(R.id.listpd);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScanningBluetoothDevices();

            }
        });
    }
    private void initViews(View view){
        //scanBtn = view.findViewById(R.id.scan_btn);
        listBtn = view.findViewById(R.id.listpd);
        //listBtn.setEnabled(false);
        foundDevicesListView = view.findViewById(R.id.listView);
        deviceListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_expandable_list_item_1);
        foundDevicesListView.setAdapter(deviceListAdapter);

        //1-) set default manager
        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluethoothadapter.getBondedDevices();
    }
    public void onScanningBluetoothDevices(){

        if(pairedDevices.size() > 0){
            deviceListAdapter.clear();
            for (BluetoothDevice device: pairedDevices){
                deviceListAdapter.add(device.getName() + "\t" +"\t" +device.getAddress());
                deviceListAdapter.notifyDataSetChanged();
                foundDevicesListView.setAdapter(deviceListAdapter);
            }
        }
    }
    // ce button ci
    public void scanDevices(View view){
        listBtn = view.findViewById(R.id.listpd);
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScanningBluetoothDevices();
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister receiver
        try {
            //Objects.requireNonNull(getContext()).unregisterReceiver(mReceiver);

            // hat ohne fehler functioniert
            context.unregisterReceiver(mReceiver);

        }catch (NullPointerException e){
            Log.d("HOME FRAGEMENT: ", "onDestroy-Func");
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bluethoothadapter != null) {
            if (bluethoothadapter.isDiscovering()) {
                bluethoothadapter.cancelDiscovery();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bluethoothadapter.startDiscovery();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (bluethoothadapter != null && bluethoothadapter.isEnabled()) {
                // If the resultCode is 0, the user selected "No" when prompt to
                // allow the app to enable bluetooth.
                // You may want to display a dialog explaining what would happen if
                // the user doesn't enable bluetooth.
                bluethoothadapter.startDiscovery();
                Toast.makeText(this, "The user decided to deny bluetooth access", Toast.LENGTH_LONG).show();
            } else
                Log.i(TAG, "User allowed bluetooth access!");
        }
    }

}