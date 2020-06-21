package cm.rulan.distcovid.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import cm.rulan.distcovid.R;

import java.util.Objects;
import java.util.Set;

public class HomeFragment extends Fragment {

    private static final String TAG = "Home";
    private static final int REQUEST_ENABLE_BT = 1;
    //private static final int REQUEST_DISCOVERABLE_BT = 0;
    Set<BluetoothDevice> pairedDevices;
    //private HomeViewModel homeViewModel;
    private BluetoothAdapter bluethoothadapter;
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

    // Stops scanning after 10 seconds.
    //private static final long SCAN_PERIOD = 10000;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 0-) init views
        initViews(root);

        scanDevices(root);
        return root;
    }

    private void initViews(View view){
        scanBtn = view.findViewById(R.id.scan_btn);
        foundDevicesListView = view.findViewById(R.id.listView);
        deviceListAdapter = new ArrayAdapter<>(
                requireActivity(),
                android.R.layout.simple_expandable_list_item_1);
        foundDevicesListView.setAdapter(deviceListAdapter);

        //1-) set default manager
        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluethoothadapter.getBondedDevices();
    }

    // check permissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = requireActivity().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
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

        //2-) check if device is not supported
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
    }

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

    @SuppressLint("UseRequireInsteadOfGet")
    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister receiver
        try {
            Objects.requireNonNull(getContext()).unregisterReceiver(mReceiver);
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
                Toast.makeText(getContext(), "The user decided to deny bluetooth access", Toast.LENGTH_LONG).show();
            } else
                Log.i(TAG, "User allowed bluetooth access!");
        }
    }

}