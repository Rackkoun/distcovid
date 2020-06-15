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
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Set;

import cm.rulan.distcovid.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private static final String TAG = "Home";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluethoothadapter;
    private Button btnpairedDev, scan;
    private ListView pairedDev, Deviceslist;
    private ArrayList list, btfundlist;
    private ArrayAdapter adapter, newArradapter;
    private TextView tview;
    private Context context;
    Set<BluetoothDevice> pairedDevices;

    private final BroadcastReceiver receiver  = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "ON RECEIVE");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                list.add(deviceName + "\t" + "\t" + deviceHardwareAddress);
                adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,list);
                pairedDev.setAdapter(adapter);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                Toast.makeText(getContext(), "Showing nearby Devices via BT", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        pairedDev = root.findViewById(R.id.listView);
        list = new ArrayList<>();
        btfundlist = new ArrayList();
        tview = root.findViewById(R.id.textView);
        btnpairedDev = root.findViewById(R.id.list);
        scan = root.findViewById(R.id.scan);
        Deviceslist = root.findViewById(R.id.listView1);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(receiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(receiver, filter);

        newArradapter = new ArrayAdapter(getContext(), R.layout.fragment_home);
        Deviceslist.setAdapter(newArradapter);

        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        if (bluethoothadapter == null) {
            // Device doesn't support Bluetooth
            AlertDialog.Builder dialbuild = new AlertDialog.Builder(getContext());
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

        if (!bluethoothadapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(this.getContext(), "Bluetooth is already On", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnpairedDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPairedDevices();
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanBluetooth();
                //v.setVisibility(View.GONE);
            }
        });
    }



    public void listPairedDevices() {
        pairedDevices = bluethoothadapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Es gibt gekoppelte Geräte. Gib den name und adresse
            // von jedem gekoppelten Gerät.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();  // device Name
                String deviceHardwareAddress = device.getAddress(); // MAC address
                list.add(deviceName + "\t" + "\t" + deviceHardwareAddress);
                Log.d("Device Name: ", "deviceName " + deviceName);
                Log.d("Device Hardwareaddr: ", "deviceHardwareAdresse " + deviceHardwareAddress);
                Toast.makeText(this.getContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
                adapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, list);
                pairedDev.setAdapter(adapter);

            }
        }
    }

    public void ScanBluetooth() {
        // Indicate scanning in the title
        this.getActivity().setProgressBarIndeterminateVisibility(true);
        this.getActivity().setTitle(R.string.scanning);
        //view.findViewById(R.id.scan).setVisibility(View.VISIBLE);
        if (bluethoothadapter.isDiscovering()) {
            bluethoothadapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");
        }
            bluethoothadapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getContext().registerReceiver(receiver, discoverDevicesIntent);

            if (!bluethoothadapter.isDiscovering()) {

                //check BT permissions in manifest
                //checkBTPermissions();

                bluethoothadapter.startDiscovery();
                discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                getContext().registerReceiver(receiver, discoverDevicesIntent);
            }
            /*bluethoothadapter.startDiscovery();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getContext().registerReceiver(receiver, filter);
            // Register for broadcasts when a device is discovered.
            receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "ON RECEIVE");
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    list.add(deviceName + "\t" + "\t" + deviceHardwareAddress);
                    adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,list);
                    pairedDev.setAdapter(adapter);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    Toast.makeText(getContext(), "Showing nearby Devices via BT", Toast.LENGTH_SHORT).show();
                }
            }*/
        }

    }


