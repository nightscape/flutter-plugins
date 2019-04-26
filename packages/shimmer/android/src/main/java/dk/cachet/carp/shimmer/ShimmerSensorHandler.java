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

public class ShimmerSensorHandler implements SensorHandler
{
    private Shimmer sensor;
    private final Activity activity;
    private final EventChannel.EventSink eventSink;
    private final String macAddress;

    public ShimmerSensorHandler(Activity activity, EventChannel.EventSink eventSink, String macAddress)
    {
        this.activity = activity;
        this.eventSink = eventSink;
        this.macAddress = macAddress;
        Log.d("PermissionActivity", this.eventSink.toString());
    }

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

    @Override
    public void startService()
    {
        try
        {
            // The Handler that gets information back from the BluetoothChatService
            Handler mHandler = new ShimmerHandler(eventSink);
            sensor = new Shimmer(mHandler);
            sensor.connect(macAddress, "default");
            sensor.startStreaming();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void stopService()
    {
        if (sensor != null)
        {
            sensor.stopStreaming();
            sensor.stop();
        }
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

}
