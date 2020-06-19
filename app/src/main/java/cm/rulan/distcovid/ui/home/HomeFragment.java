package cm.rulan.distcovid.ui.home;

 import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

    private static final String TAG = "Home";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    Set<BluetoothDevice> pairedDevices;
    private HomeViewModel homeViewModel;
    private BluetoothAdapter bluethoothadapter;
    private Button btnpairedDev, btnscan;
    private ListView pairedDev;
    private ArrayList list;
    private ArrayList btdevnew;
    private TextView tview;
    private ProgressDialog mProgressDlg;
    IntentFilter filter ;
    BroadcastReceiver mReceiver ;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        pairedDev = root.findViewById(R.id.listView);
        list = new ArrayList<>();
        btdevnew = new ArrayList<>();
        tview = root.findViewById(R.id.textView);
        btnpairedDev = root.findViewById(R.id.list);
        btnscan = root.findViewById(R.id.scanBT);
        mProgressDlg = new ProgressDialog(getContext());

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        mProgressDlg = new ProgressDialog(getContext());
                        mProgressDlg.setTitle(getContext().getString(R.string.searchForNewBluetoothDevices_titel));
                        mProgressDlg.setMessage(getContext().getString(R.string.searchForNewBluetoothDevices_msg));
                        mProgressDlg.setIndeterminate(true);
                        mProgressDlg.show();
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        mProgressDlg.cancel();
                        //activity.setFoundLabel();
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        final ArrayAdapter mNewDevicesArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.device_adapter_view);

                        mNewDevicesArrayAdapter.add(device);
                        pairedDev.setAdapter(mNewDevicesArrayAdapter);
                        Log.d(TAG, "device" +device);

                        break;
                }
            }
        };

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
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        return root;
    }

    @Override
    public void onPause() {
        if (bluethoothadapter != null) {
            if (bluethoothadapter.isDiscovering()) {
                bluethoothadapter.cancelDiscovery();
            }
        }
        super.onPause();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == 0)
            {
                // If the resultCode is 0, the user selected "No" when prompt to
                // allow the app to enable bluetooth.
                // You may want to display a dialog explaining what would happen if
                // the user doesn't enable bluetooth.
                Toast.makeText(getContext(), "The user decided to deny bluetooth access", Toast.LENGTH_LONG).show();
            }
            else
                Log.i(TAG, "User allowed bluetooth access!");
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //listPairedDevices();
        btnpairedDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPairedDevices();
            }
        });
        btnscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanBluetoothDevices();
                //scanLeDevice(true);
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
                final ArrayAdapter adapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, list);
                pairedDev.setAdapter(adapter);

            }
        }
    }

    private void ScanBluetoothDevices() {
        // If we're already discovering, stop it
        if (bluethoothadapter.isDiscovering()) {
            bluethoothadapter.cancelDiscovery();
        } else {

            // Request discover from BluetoothAdapter
            // Create a BroadcastReceiver for ACTION_FOUND.
            //discovery starts, we can show progress dialog or perform other tasks
            filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.getActivity().registerReceiver(mReceiver, filter);
            bluethoothadapter.startDiscovery();
        }
    }

    }



