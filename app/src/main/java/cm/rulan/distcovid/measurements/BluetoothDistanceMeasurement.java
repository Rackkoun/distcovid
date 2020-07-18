package cm.rulan.distcovid.measurements;

// sources:
// RSSI to Discance conversion:
//  https://gist.github.com/eklimcz/446b56c0cb9cfe61d575
// How to calculate distance from RSSI value of the BLE Beacon
//  https://iotandelectronics.wordpress.com/2016/10/07/how-to-calculate-distance-from-the-rssi-value-of-the-ble-beacon/

import android.util.Log;

public class BluetoothDistanceMeasurement {
    // RAW Coded
    private static final short txPower = -60;

    /*Power range:
    * from [-26, -100]
    * -26 a few inches
    *
    * formal: Distance = 10^((txPower - rssi)/(10*Ni))
    * with i = 2, 3, 4*/

    private static final int[] N = new int[]{2, 3, 4, 10, 15, 20, 25, 30, 50};
    private static final String TAG = "BTDistance";

    public static double convertRSSI2MeterWithAccuracy(short rssi){
        double distance;
        if (rssi == 0){
            return -1.0;
        }
        double ratio = rssi * 1.0/txPower;

        if (ratio < 1.0){
            Log.i(TAG, "-- Accuracy ratio < 1.0 -- ratio: ["+ratio+"]");
            distance = Math.pow(ratio, 10);
            distance = Math.round(distance * 100.0) / 100.0; // to get two decimal digits
        }else {
            Log.i(TAG, "-- Accuracy ration > 1.0 -- ratio: ["+ratio+"]");
            distance = (0.89976)*Math.pow(ratio, 7.7095) + 0.111;
            distance = Math.round(distance * 100.0) / 100.0;
            Log.i(TAG, "-- Accuracy finish --");
        }
        return distance;
    }

    public static double convertRSSI2Meter(short rssi, int rangeIdx){
        /*
        * rangeIdx between 0 and 2
        * */
        Log.i(TAG, "-- no Accuracy --");
        double distance = Math.pow(10, ((txPower - (rssi)) / (10f * N[rangeIdx])));
        distance = Math.round(distance * 100.0) / 100.0;
        Log.i(TAG, "-- no Accuracy end --");
        return distance;
    }
}
