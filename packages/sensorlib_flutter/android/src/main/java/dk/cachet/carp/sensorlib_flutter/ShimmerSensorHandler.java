package dk.cachet.carp.sensorlib_flutter;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.fau.sensorlib.SensorDataProcessor;
import de.fau.sensorlib.SensorInfo;
import de.fau.sensorlib.dataframe.SensorDataFrame;
import de.fau.sensorlib.sensors.InternalSensor;
import de.fau.sensorlib.sensors.ShimmerSensor;
import io.flutter.plugin.common.EventChannel;

public class ShimmerSensorHandler implements SensorHandler
{
    private ShimmerSensor sensor;
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

    @Override
    public void startService()
    {
        try
        {
            sensor = new ShimmerSensor(activity, macAddress, new SensorDataProcessor()
            {
                @Override
                public void onNewData(SensorDataFrame sensorDataFrame)
                {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put("timestamp", sensorDataFrame.getTimestamp());
                    m.put("sensor", sensorDataFrame.getOriginatingSensor().toString());
                    if (sensorDataFrame instanceof ShimmerSensor.ShimmerDataFrame)
                    {
                        ShimmerSensor.ShimmerDataFrame shimmerDataFrame = (ShimmerSensor.ShimmerDataFrame) sensorDataFrame; 
                        m.put("accelX", shimmerDataFrame.getAccelX());
                        m.put("accelY", shimmerDataFrame.getAccelY());
                        m.put("accelZ", shimmerDataFrame.getAccelZ());
                        m.put("gyroX", shimmerDataFrame.getGyroX());
                        m.put("gyroY", shimmerDataFrame.getGyroY());
                        m.put("gyroZ", shimmerDataFrame.getGyroZ());
                    }
                    eventSink.success(m);
                }
            });
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        try
        {
            sensor.connect();
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
            sensor.disconnect();
        }
    }
}
