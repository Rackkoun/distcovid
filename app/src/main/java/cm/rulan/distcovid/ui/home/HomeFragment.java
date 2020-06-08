package cm.rulan.distcovid.ui.home;

import android.app.AlertDialog;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Set;

import cm.rulan.distcovid.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private static final String TAG = "Home";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    private BluetoothAdapter bluethoothadapter;
    private Button btnpairedDev, scan;
    private ListView pairedDev, newDeviceList;
    private ArrayList list;
    private IntentFilter filter;
    private ArrayAdapter adapter;
    private TextView tview;
    private Context context;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                bluethoothadapter.startDiscovery();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                bluethoothadapter.cancelDiscovery();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                Toast.makeText(context.getApplicationContext(), "Found device " + deviceName, Toast.LENGTH_SHORT).show();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("Device Name: " , "device " + deviceName);
                list.add(deviceName + "\t" + "\t" + deviceHardwareAddress);

            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
               new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        pairedDev = root.findViewById(R.id.listView);
        list = new ArrayList<>();
        tview = root.findViewById(R.id.textView);
        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        btnpairedDev = root.findViewById(R.id.list);
        scan = root.findViewById(R.id.scan);

        if (bluethoothadapter == null) {
            // Device doesn't support Bluetooth
            new AlertDialog.Builder(this.context)
                    .setTitle("Nicht Kompatibel")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if (!bluethoothadapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            Toast.makeText(this.getContext(), "Bluetooth is already On", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnpairedDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listDevices();
            }
        });
        scan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ScanBluetooth();
            }
        });
        /*view.findViewById(R.id.button_home).setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View view) {
                HomeFragmentDirections.ActionHomeFragmentToHomeSecondFragment action =
                        HomeFragmentDirections.actionHomeFragmentToHomeSecondFragment
                                ("From HomeFragment");
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(action);
            }
//        });*/
    }

    public void listDevices() {
        Set<BluetoothDevice> pairedDevices = bluethoothadapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Es gibt gekoppelte Geräte. Gib den name und adresse
            // von jedem gekoppelten Gerät.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();  // device Name
                String deviceHardwareAddress = device.getAddress(); // MAC address
                list.add(deviceName + "\t" + "\t" + deviceHardwareAddress);
                Log.d("Device Name: " , "device " + deviceName);
                Toast.makeText(this.getContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
                adapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, list);
                pairedDev.setAdapter(adapter);

            }
        }
    }

    private void ScanBluetooth(){
        if (bluethoothadapter.isDiscovering()) {
            bluethoothadapter.cancelDiscovery();
            Log.d(TAG, "Cancelling discover");
            // Für Broadcasts registrieren, wenn ein Gerät entdeckt wird.
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //registerReceiver(receiver, filter);
        }
        else{
            bluethoothadapter.startDiscovery();
            Toast.makeText(this.getContext(), "Start Scanning.........", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Looking for unpaired devices");
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //context.registerReceiver(receiver, filter);
        }
    }
}
