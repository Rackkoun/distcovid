package cm.rulan.distcovid.measurements;

// sources:
// RSSI to Discance conversion:
//  https://gist.github.com/eklimcz/446b56c0cb9cfe61d575
// How to calculate distance from RSSI value of the BLE Beacon
//  https://iotandelectronics.wordpress.com/2016/10/07/how-to-calculate-distance-from-the-rssi-value-of-the-ble-beacon/

public class BluetoothDistanceMeasurement {
    // RAW Coded
    private static final short REFERENCE_SIGNAL = -60;

    /*Power range:
    * from [-26, -100]
    * -26 a few inches
    *
    * formal: Distance = 10^((txPower - rssi)/(10*Ni))
    * with i = 2, 3, 4*/

    private static final int[] constExponent = new int[]{2, 3, 4, 10, 15, 20, 25, 30, 50};

    public BluetoothDistanceMeasurement(){/* explicit declaration of constructor*/}

    public static double convertRSSI2Meter(short rssi, int rangeIdx){
        /*
        * rangeIdx between 0 and 2
        * */
        double distance = Math.pow(10, ((REFERENCE_SIGNAL - (rssi)) / (10f * constExponent[rangeIdx])));
        distance = Math.round(distance * 100.0) / 100.0;
        return distance;
    }
}
