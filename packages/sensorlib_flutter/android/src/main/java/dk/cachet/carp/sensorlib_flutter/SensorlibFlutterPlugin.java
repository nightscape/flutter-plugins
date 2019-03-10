package dk.cachet.carp.sensorlib_flutter;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * SensorlibFlutterPlugin
 * public class SensorlibFlutterPlugin implements MethodCallHandler {
 * /** Plugin registration. *

 *
 * @Override public void onMethodCall(MethodCall call, Result result) {
 * if (call.method.equals("getPlatformVersion")) {
 * result.success("Android " + android.os.Build.VERSION.RELEASE);
 * } else {
 * result.notImplemented();
 * }
 * }
 * }
 */


/**
 * SensorlibFlutterPlugin
 */
public class SensorlibFlutterPlugin implements EventChannel.StreamHandler, MethodChannel.MethodCallHandler
{

    private EventChannel.EventSink eventSink;
    private Registrar registrar;
    static String USER_DATA_KEY = "user_data";
    static String USER_DATA_METHOD = "userData";

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar)
    {
        // Set up plugin instance
        SensorlibFlutterPlugin plugin = new SensorlibFlutterPlugin(registrar);

        // Set up method channel
        final MethodChannel methodChannel = new MethodChannel(registrar.messenger(), "sensorlib.method_channel");
        methodChannel.setMethodCallHandler(plugin);

        // Set up event channel
        final EventChannel eventChannel = new EventChannel(registrar.messenger(), "sensorlib.event_channel");
        eventChannel.setStreamHandler(plugin);
    }

    public SensorlibFlutterPlugin(Registrar registrar)
    {
        this.registrar = registrar;
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink)
    {
        this.eventSink = eventSink;
    }

    @Override
    public void onCancel(Object o)
    {
        this.eventSink = null;

    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result)
    {
        if (methodCall.method.equals(USER_DATA_METHOD))
        {
            PermissionManager manager = new PermissionManager(registrar.activity(), this.eventSink);
            manager.startSensorlibService();
        } else
        {
            result.notImplemented();
        }
    }

}
