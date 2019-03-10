package dk.cachet.carp.sensorlib_flutter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import de.fau.sensorlib.SensorDataProcessor;
import de.fau.sensorlib.SensorInfo;
import de.fau.sensorlib.dataframe.SensorDataFrame;
import de.fau.sensorlib.sensors.InternalSensor;
import io.flutter.plugin.common.EventChannel;

public class PermissionManager {

    InternalSensor sensor;
    private EventChannel.EventSink eventSink;

    private static String[] permissions = new String[]{
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Activity activity;


    public PermissionManager(Activity activity, EventChannel.EventSink eventSink) {
        Log.d("PermissionActivity", this.eventSink.toString());
        this.activity = activity;
        this.eventSink = eventSink;
    }

    public void startSensorlibService() {
        if (!arePermissionsGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                activity.requestPermissions(permissions, 0);
            }
            checkDelayed();
        } else {
            startService();
        }
    }

    private Boolean arePermissionsGranted() {
        for (String permission : permissions) {
            if (!isPermissionGranted(permission))
                return false;
        }

        return true;
    }

    private Boolean isPermissionGranted(String permission) {
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    private void checkDelayed() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (arePermissionsGranted())
                    startService();
                else
                    checkDelayed();
            }
        }, 1000);
    }

    private void startService()
    {
        sensor = new InternalSensor(activity, new SensorInfo("foo", "bar"), new SensorDataProcessor() {
            @Override
            public void onNewData(SensorDataFrame sensorDataFrame) {
                eventSink.success(sensorDataFrame);
            }
        });
        try
        {
            sensor.connect();
            sensor.startStreaming();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
