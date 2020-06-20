package cm.rulan.distcovid;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import cm.rulan.distcovid.ui.home.HomeFragment;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private ProgressDialog progressDialog;
    private HomeFragment homeFragment;
    private Context ctx;

    public BluetoothBroadcastReceiver(HomeFragment activity) {
        this.homeFragment = activity;
        this.ctx = activity.getContext();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        handleBluetoothState(intent);
        handleAction(intent);
    }
    private void handleBluetoothState(Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                showToast(R.string.bluetooth_off);
                break;
            case BluetoothAdapter.STATE_ON:
                showToast(R.string.bluetooth_on);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                showToast(R.string.bluetooth_turning_on);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                showToast(R.string.bluetooth_turning_off);
                break;
        }
    }

    private void showToast(int stringResId) {
        Toast.makeText(ctx, ctx.getString(stringResId), Toast.LENGTH_LONG).show();
    }

    private void handleAction(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                progressDialog = new ProgressDialog(this.ctx);
                progressDialog.setTitle(this.ctx.getString(R.string.searchForNewBluetoothDevices_titel));
                progressDialog.setMessage(this.ctx.getString(R.string.searchForNewBluetoothDevices_msg));
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                progressDialog.cancel();
                //homefragment.setTargetFragment();
                break;
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //XBluetoothDevice xBluetoothDevice = activity.createXBluetoothDevice(device);
                //activity.getDeviceList().add(xBluetoothDevice);
                break;
        }
    }
}
