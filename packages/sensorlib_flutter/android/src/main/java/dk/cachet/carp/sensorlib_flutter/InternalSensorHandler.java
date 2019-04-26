package dk.cachet.carp.sensorlib_flutter;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.fau.sensorlib.SensorDataProcessor;
import de.fau.sensorlib.SensorInfo;
import de.fau.sensorlib.dataframe.SensorDataFrame;
import de.fau.sensorlib.sensors.InternalSensor;
import io.flutter.plugin.common.EventChannel;

public class InternalSensorHandler implements SensorHandler {
    private InternalSensor sensor;
    private Activity activity;
    private EventChannel.EventSink eventSink;

    public InternalSensorHandler(Activity activity, EventChannel.EventSink eventSink) {
        this.activity = activity;
        this.eventSink = eventSink;
        Log.d("PermissionActivity", this.eventSink.toString());
    }

    @Override
    public void startService() {
        sensor = new InternalSensor(activity, new SensorInfo(InternalSensor.INTERNAL_SENSOR_NAME, InternalSensor.INTERNAL_SENSOR_ADDRESS), new SensorDataProcessor() {
            @Override
            public void onNewData(SensorDataFrame sensorDataFrame) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("timestamp", sensorDataFrame.getTimestamp());
                m.put("sensor", sensorDataFrame.getOriginatingSensor().toString());
                if (sensorDataFrame instanceof InternalSensor.InternalAccelDataFrame) {
                    m.put("accelX", ((InternalSensor.InternalAccelDataFrame) sensorDataFrame).getAccelX());
                    m.put("accelY", ((InternalSensor.InternalAccelDataFrame) sensorDataFrame).getAccelY());
                    m.put("accelZ", ((InternalSensor.InternalAccelDataFrame) sensorDataFrame).getAccelZ());
                }
                if (sensorDataFrame instanceof InternalSensor.InternalGyroDataFrame) {
                    m.put("gyroX", ((InternalSensor.InternalGyroDataFrame) sensorDataFrame).getGyroX());
                    m.put("gyroY", ((InternalSensor.InternalGyroDataFrame) sensorDataFrame).getGyroY());
                    m.put("gyroZ", ((InternalSensor.InternalGyroDataFrame) sensorDataFrame).getGyroZ());
                }
                if (sensorDataFrame instanceof InternalSensor.InternalOrientationDataFrame) {
                    m.put("pitch", ((InternalSensor.InternalOrientationDataFrame) sensorDataFrame).getPitch());
                    m.put("roll", ((InternalSensor.InternalOrientationDataFrame) sensorDataFrame).getRoll());
                    m.put("yaw", ((InternalSensor.InternalOrientationDataFrame) sensorDataFrame).getYaw());
                }
                eventSink.success(m);
            }
        });
        try {
            sensor.connect();
            sensor.startStreaming();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void stopService() {
        if (sensor != null) {
            sensor.stopStreaming();
            sensor.disconnect();
            sensor = null;
        }
    }
}
