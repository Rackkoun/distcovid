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
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluethoothadapter;
    private Button btnpairedDev, scan;
    private ListView pairedDev, newDeviceList;
    private ArrayList list;
    private IntentFilter filter;
    private ArrayAdapter adapter;
    private TextView tview;
    private Context context;
    Set<BluetoothDevice> pairedDevices;


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
        btnpairedDev = root.findViewById(R.id.list);
        scan = root.findViewById(R.id.scan);
        //listDevices();
        bluethoothadapter = BluetoothAdapter.getDefaultAdapter();
        if (bluethoothadapter == null) {
            // Device doesn't support Bluetooth
            AlertDialog.Builder  dialbuild =new AlertDialog.Builder(getContext());
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

         /*if (!bluethoothadapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            Toast.makeText(this.getContext(), "Bluetooth is already On", Toast.LENGTH_SHORT).show();
        }*/
        return root;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bluethoothadapter.cancelDiscovery();
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
                Toast.makeText(this.getContext(), "The user decided to deny bluetooth access", Toast.LENGTH_LONG).show();
            }
            else
                Log.i(TAG, "User allowed bluetooth access!");
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnpairedDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                listDevices();
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

    /*public void listDevices() {
//       pairedDevices = bluethoothadapter.getBondedDevices();
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
    }*/

    private void ScanBluetooth(){
        Intent intent = new Intent(this.getContext(), FoundBTDevices.class);
        startActivity(intent);
    }
}
