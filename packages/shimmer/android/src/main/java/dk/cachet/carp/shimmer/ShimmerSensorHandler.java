package dk.cachet.carp.shimmer;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

import android.view.View;
import android.widget.TextView;

import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

import java.io.IOException;
import java.util.Collection;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER;

public class ShimmerSensorHandler implements SensorHandler
{
    private Shimmer sensor;
    private final Activity activity;
    private final EventChannel.EventSink eventSink;
    private final String macAddress;
    final static String LOG_TAG = "BluetoothManagerExample";
    private ShimmerBluetoothManagerAndroid btManager;

    public ShimmerSensorHandler(Activity activity, EventChannel.EventSink eventSink, String macAddress)
    {
        this.activity = activity;
        this.eventSink = eventSink;
        this.macAddress = macAddress;
        Log.d("PermissionActivity", this.eventSink.toString());
    }
    private ShimmerDevice shimmerDevice;
    public class ShimmerHandler2 extends Handler {

        private ShimmerBluetoothManager btManager;
        private final EventChannel.EventSink eventSink;

        public ShimmerHandler2(EventChannel.EventSink eventSink)
        {
            this.eventSink = eventSink;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                    if ((msg.obj instanceof ObjectCluster)) {
                        /*
                        ObjectCluster objectCluster = (ObjectCluster) msg.obj;

                        //Retrieve all possible formats for the current sensor device:
                        Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);
                        FormatCluster timeStampCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        double timeStampData = timeStampCluster.mData;
                        Log.i(LOG_TAG, "Time Stamp: " + timeStampData);
                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
                        FormatCluster accelXCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (accelXCluster != null) {
                            double accelXData = accelXCluster.mData;
                            Log.i(LOG_TAG, "Accel LN X: " + accelXData);
                        }
                         */
                        Map<String, Object> m = new HashMap<String, Object>();
                        ObjectCluster obj = (ObjectCluster) msg.obj;
                        if (obj != null)
                        {
                            m.put("timestamp", obj.mSystemTimeStamp);
                            m.put("sensor", obj.getShimmerName());
                            for (Map.Entry<String, FormatCluster> entry : obj.mPropertyCluster.entries())
                            {
                                m.put(entry.getKey(), entry.getValue().mData);
                            }
                        }
                        eventSink.success(m);

                    }
                    break;
                case Shimmer.MESSAGE_TOAST:
                    /** Toast messages sent from {@link Shimmer} are received here. E.g. device xxxx now streaming.
                     *  Note that display of these Toast messages is done automatically in the Handler in {@link com.shimmerresearch.android.shimmerService.ShimmerService} */

                    //Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    ShimmerBluetooth.BT_STATE state = null;
                    String macAddress = "";

                    if (msg.obj instanceof ObjectCluster) {
                        state = ((ObjectCluster) msg.obj).mState;
                        macAddress = ((ObjectCluster) msg.obj).getMacAddress();
                    } else if (msg.obj instanceof CallbackObject) {
                        state = ((CallbackObject) msg.obj).mState;
                        macAddress = ((CallbackObject) msg.obj).mBluetoothAddress;
                    }

                    Log.d(LOG_TAG, "Shimmer state changed! Shimmer = " + macAddress + ", new state = " + state);

                    switch (state) {
                        case CONNECTED:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now CONNECTED");
                            shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(macAddress); // TODO: Was (shimmerBtAdd);
                            if (shimmerDevice != null) {
                                Log.i(LOG_TAG, "Got the ShimmerDevice!");
                                shimmerDevice.startStreaming();
                            } else {
                                Log.i(LOG_TAG, "ShimmerDevice returned is NULL!");
                            }
                            break;
                        case CONNECTING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is CONNECTING");
                            break;
                        case STREAMING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now STREAMING");
                            break;
                        case STREAMING_AND_SDLOGGING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now STREAMING AND LOGGING");
                            break;
                        case SDLOGGING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now SDLOGGING");
                            break;
                        case DISCONNECTED:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            break;
                    }
                    break;
            }

            super.handleMessage(msg);
        }

        public void setBtManager(ShimmerBluetoothManager btManager)
        {
            this.btManager = btManager;
        }
    };

    /*
    public static class ShimmerHandler extends Handler
    {
        private final EventChannel.EventSink eventSink;

        public ShimmerHandler(EventChannel.EventSink eventSink)
        {
            this.eventSink = eventSink;
        }


        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {

                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    switch (msg.arg1)
                    {
                        case ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED:
                            Log.d("ShimmerActivity", "Message Fully Initialized Received from Shimmer driver");
                            try
                            {
                                Thread.sleep(300);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        case ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE:
                            Log.d("ShimmerActivity", "Driver is attempting to establish connection with Shimmer device");
                            break;
                        case ShimmerBluetooth.NOTIFICATION_SHIMMER_STOP_STREAMING:
                            Log.d("ShimmerActivity", "Shimmer No State");
                            break;
                    }
                    break;
                case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                    Map<String, Object> m = new HashMap<String, Object>();
                    ObjectCluster obj = (ObjectCluster) msg.obj;
                    if (obj != null)
                    {
                        m.put("timestamp", obj.mSystemTimeStamp);
                        m.put("sensor", obj.getShimmerName());
                        for (Map.Entry<String, FormatCluster> entry : obj.mPropertyCluster.entries())
                        {
                            m.put(entry.getKey(), entry.getValue().mData);
                        }
                    }
                    eventSink.success(m);

                    break;
            }


        }
    }

    ;
    
     */

    @Override
    public void startService()
    {
        try
        {
            // The Handler that gets information back from the BluetoothChatService
            //Handler mHandler = new ShimmerHandler(eventSink);
            //sensor = new Shimmer(mHandler);
            ShimmerHandler2 handler = new ShimmerHandler2(eventSink);
            btManager = new ShimmerBluetoothManagerAndroid(activity, handler);
            handler.setBtManager(btManager);

            btManager.connectShimmerThroughBTAddress(macAddress);
            //sensor.connect(macAddress, "default");
            //sensor.startStreaming();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void stopService()
    {
        /*if (sensor != null)
        {
            sensor.stopStreaming();
            sensor.stop();
        }
         */
        //Disconnect the Shimmer device when app is stopped
        if (shimmerDevice != null) {
            if (shimmerDevice.isSDLogging()) {
                shimmerDevice.stopSDLogging();
                Log.d(LOG_TAG, "Stopped Shimmer Logging");
            } else if (shimmerDevice.isStreaming()) {
                shimmerDevice.stopStreaming();
                Log.d(LOG_TAG, "Stopped Shimmer Streaming");
            } else {
                shimmerDevice.stopStreamingAndLogging();
                Log.d(LOG_TAG, "Stopped Shimmer Streaming and Logging");
            }
        }
        btManager.disconnectAllDevices();
        Log.i(LOG_TAG, "Shimmer DISCONNECTED");
    }

    public static class LogAndStreamExample extends Activity
    {


        static final int REQUEST_ENABLE_BT = 1;
        static final int REQUEST_CONNECT_SHIMMER = 2;
        static String deviceState = "Disconnected";

        static Shimmer shimmer;

        // Local Bluetooth adapter
        private BluetoothAdapter mBluetoothAdapter = null;
        // Name of the connected device
        private static String mBluetoothAddress = null;


        /**
         * Called when the activity is first created.
         */
        public void onCreateaaaaa(Bundle savedInstanceState)
        {
            Configuration.setTooLegacyObjectClusterSensorNames();

            shimmer.startStreaming();

            shimmer.startDataLogAndStreaming();
            shimmer.stopStreaming();
            shimmer.readDirectoryName();
            //wait the directory name from the Shimmer
            try
            {
                Thread.sleep(300);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //get the directory name
            String directory = shimmer.getDirectoryName();
            shimmer.readStatusLogAndStream();
            //wait the directory name from the Shimmer
            try
            {
                Thread.sleep(300);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //get the current status
            boolean docked = shimmer.isDocked();
            boolean sensing = shimmer.isSensing();

            if (deviceState.equals("Disconnected"))
            {
                //Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                //startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
            } else
                shimmer.stop();


            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null)
            {
                Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
                finish();
            }

        }


        @Override
        public void onStart()
        {
            super.onStart();

            if (!mBluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        }


        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {

            switch (requestCode)
            {
                case REQUEST_ENABLE_BT:
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK)
                    {

                        //setMessage("\nBluetooth is now enabled");
                        Toast.makeText(this, "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
                    } else
                    {
                        // User did not enable Bluetooth or an error occured
                        Toast.makeText(this, "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case REQUEST_CONNECT_SHIMMER:
                    // When DeviceListActivity returns with a device to connect
                    if (resultCode == Activity.RESULT_OK)
                    {
                        //String address = data.getExtras()
                        //        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        //Log.d("ShimmerActivity", address);
                        //mBluetoothAddress = address;


                    }
                    break;

            }
        }

    }



    /**
     * This example demonstrates the use of the {@link ShimmerBluetoothManagerAndroid} to:
     * <ul>
     * <li>Connect to a Shimmer device</li>
     * <li>Stream data from the Shimmer device</li>
     * <li>Enable and disable sensors</li>
     * <li>Modify individual sensor configurations</li>
     * </ul>
     */
    public class MainActivity extends Activity {

        ShimmerBluetoothManagerAndroid btManager;
        ShimmerDevice shimmerDevice;
        String shimmerBtAdd = "00:00:00:00:00:00";  //Put the address of the Shimmer device you want to connect here

        TextView textView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            try {
                ShimmerHandler2 handler = new ShimmerHandler2(eventSink);
                btManager = new ShimmerBluetoothManagerAndroid(this, handler);
                handler.setBtManager(btManager);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
            }

        }

        @Override
        protected void onStart() {
            //Connect the Shimmer using its Bluetooth Address
            try {
                btManager.connectShimmerThroughBTAddress(shimmerBtAdd);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
                Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
                        "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
            }
            super.onStart();
        }

        @Override
        protected void onStop() {
            //Disconnect the Shimmer device when app is stopped
            if (shimmerDevice != null) {
                if (shimmerDevice.isSDLogging()) {
                    shimmerDevice.stopSDLogging();
                    Log.d(LOG_TAG, "Stopped Shimmer Logging");
                } else if (shimmerDevice.isStreaming()) {
                    shimmerDevice.stopStreaming();
                    Log.d(LOG_TAG, "Stopped Shimmer Streaming");
                } else {
                    shimmerDevice.stopStreamingAndLogging();
                    Log.d(LOG_TAG, "Stopped Shimmer Streaming and Logging");
                }
            }
            btManager.disconnectAllDevices();
            Log.i(LOG_TAG, "Shimmer DISCONNECTED");
            super.onStop();
        }

        /**
         * Messages from the Shimmer device including sensor data are received here
         */
        public void stopStreaming(View v) {
            shimmerDevice.stopStreaming();
        }

        public void startStreaming(View v) {
            shimmerDevice.startStreaming();
        }

        /**
         * Called when the configurations button is clicked
         *
         * @param v
         */
        public void openConfigMenu(View v) {
            if (shimmerDevice != null) {
                if (!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
                    ShimmerDialogConfigurations.buildShimmerConfigOptions(shimmerDevice, MainActivity.this, btManager);
                } else {
                    Log.e(LOG_TAG, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING");
                    Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(LOG_TAG, "Cannot open menu! Shimmer device is not connected");
                Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is not connected", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Called when the menu button is clicked
         *
         * @param v
         * @throws IOException
         */
        public void openMenu(View v) throws IOException {

            if (shimmerDevice != null) {
                if (!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
                    //ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(shimmerDevice, MainActivity.this);
                    ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(shimmerDevice, MainActivity.this, btManager);
                } else {
                    Log.e(LOG_TAG, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING");
                    Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(LOG_TAG, "Cannot open menu! Shimmer device is not connected");
                Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is not connected", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Called when the connect button is clicked
         *
         * @param v
         */
        public void connectDevice(View v) {
            Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
            startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
        }

        public void startSDLogging(View v) {
            ((ShimmerBluetooth) shimmerDevice).writeConfigTime(System.currentTimeMillis());
            shimmerDevice.startSDLogging();
        }

        public void stopSDLogging(View v) {
            shimmerDevice.stopSDLogging();
        }


        /**
         * Get the result from the paired devices dialog
         *
         * @param requestCode
         * @param resultCode
         * @param data
         */
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CONNECT_SHIMMER) {
                if (resultCode == Activity.RESULT_OK) {
                    btManager.disconnectAllDevices();   //Disconnect all devices first
                    //Get the Bluetooth mac address of the selected device:
                    String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                    btManager.connectShimmerThroughBTAddress(macAdd);   //Connect to the selected device
                    shimmerBtAdd = macAdd;
                }

            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
