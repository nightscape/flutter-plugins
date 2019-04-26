package dk.cachet.carp.sensorlib_flutter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class PermissionManager implements SensorHandler
{


    private static String[] permissions = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final SensorHandler wrappedSensorHandler;

    private Activity activity;


    public PermissionManager(Activity activity, SensorHandler wrappedSensorHandler)
    {
        this.activity = activity;
        this.wrappedSensorHandler = wrappedSensorHandler;
        Log.d("PermissionActivity", this.wrappedSensorHandler.toString());
    }

    public void startService()
    {
        if (!arePermissionsGranted())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                activity.requestPermissions(permissions, 0);
            }
            checkDelayed();
        } else
        {
            wrappedSensorHandler.startService();
        }
    }

    public void stopService()
    {
        wrappedSensorHandler.stopService();
    }

    private Boolean arePermissionsGranted()
    {
        for (String permission : permissions)
        {
            if (!isPermissionGranted(permission))
                return false;
        }

        return true;
    }

    private Boolean isPermissionGranted(String permission)
    {
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    private void checkDelayed()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                if (arePermissionsGranted())
                    wrappedSensorHandler.startService();
                else
                    checkDelayed();
            }
        }, 1000);
    }

}
