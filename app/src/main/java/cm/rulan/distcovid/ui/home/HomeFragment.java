package cm.rulan.distcovid.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothManager;
 import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
 import androidx.recyclerview.widget.RecyclerView;

 import java.util.ArrayList;
import cm.rulan.distcovid.R;

import java.util.Objects;
import java.util.Set;

public class HomeFragment extends Fragment {

   /* private static final String TAG = "Home";
    private static final int REQUEST_ENABLE_BT = 1;
    //private static final int REQUEST_DISCOVERABLE_BT = 0;
    Set<BluetoothDevice> pairedDevices;
    //private HomeViewModel homeViewModel;
    private BluetoothAdapter bluethoothadapter;
    private Button btnpairedDev, btnscan;
    private ListView pairedDev;
    private ArrayList list;
    private ArrayList btdevnew;
    private TextView tview;
    private ProgressDialog mProgressDlg;
    IntentFilter filter ;
    BroadcastReceiver mReceiver ;
    private boolean mScanning;
    private Handler mHandler;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private Button listBtn, scanBtn;
    private ListView foundDevicesListView;
    //private List<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    //private List btdevnew;
    //private TextView tview;
    //private ProgressDialog mProgressDlg;
    IntentFilter filter;
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (bluetoothDevice != null) {
                    String  devName = bluetoothDevice.getName();
                    //deviceList.add(devName);
                    System.out.println("DEVICE NAME BT: " + devName);
                    deviceListAdapter.add(devName);
                    deviceListAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "DEVICE NAME: "+ devName, Toast.LENGTH_LONG).show();

                }
            }
        }
    };
*/
    // Stops scanning after 10 seconds.
    //private static final long SCAN_PERIOD = 10000;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /*View root = inflater.inflate(R.layout.fragment_home, container, false);

        mHandler = new Handler();
        pairedDev = root.findViewById(R.id.listView);
        list = new ArrayList<>();
        btdevnew = new ArrayList<>();
        tview = root.findViewById(R.id.textView);
        btnpairedDev = root.findViewById(R.id.list);
        btnscan = root.findViewById(R.id.scanBT);
        mProgressDlg = new ProgressDialog(getContext());
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluethoothadapter = bluetoothManager.getAdapter();
       /* mReceiver = new BroadcastReceiver() {
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

        //bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        if (bluethoothadapter == null) {
            // Device doesn't support Bluetooth

        // 0-) init views
        initViews(root);

        scanDevices(root);
        return root;
    }

    private void initViews(View view){
        scanBtn = view.findViewById(R.id.scan_btn);
        foundDevicesListView = view.findViewById(R.id.listView);
        deviceListAdapter = new ArrayAdapter<>(
                Objects.requireNonNull(getActivity()),
                android.R.layout.simple_expandable_list_item_1);
        foundDevicesListView.setAdapter(deviceListAdapter);

        //1-) set default manager
        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluethoothadapter.getBondedDevices();
    }*/

    /*check permissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = Objects.requireNonNull(getActivity()).checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += getActivity().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        2020); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*2-) check if device is not supported
        if(bluethoothadapter == null){
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
        //3-) if supported then enables it
        if (!bluethoothadapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            //scanDevices();

            Toast.makeText(getContext(), "Bluetooth is on", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getContext(), "Bluetooth is already on on", Toast.LENGTH_LONG).show();
        }
    }*/
    /*
    public void onScanningBluetoothDevices(){

        if(pairedDevices.size() > 0){
            deviceListAdapter.clear();
            for (BluetoothDevice device: pairedDevices){
                deviceListAdapter.add(device.getName());
                deviceListAdapter.notifyDataSetChanged();
                foundDevicesListView.setAdapter(deviceListAdapter);
            }
        }
    }
// ce button ci
    public void scanDevices(View view){
        scanBtn = view.findViewById(R.id.scan_btn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScanningBluetoothDevices();
            }
        });
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
                //ScanBluetoothDevices();

                mLeDeviceListAdapter = new LeDeviceListAdapter();
                pairedDev.setAdapter(mLeDeviceListAdapter);
                scanLeDevice(true);
                //scanLeDevice(true);
            }
        });
    }*/
    /*
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

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluethoothadapter.stopLeScan(mLeScanCallback);
                    getActivity().invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluethoothadapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluethoothadapter.stopLeScan(mLeScanCallback);
        }
        getActivity().invalidateOptionsMenu();
    }
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        private int  mViewResourceId;


        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = HomeFragment.this.getLayoutInflater();
            mViewResourceId = 1;
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup convertView) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
        }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    }*/
        return null;
    }}
