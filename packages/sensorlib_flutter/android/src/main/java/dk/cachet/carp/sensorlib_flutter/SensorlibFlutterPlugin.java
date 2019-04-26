package dk.cachet.carp.sensorlib_flutter;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/**
 * SensorlibFlutterPlugin
 */
public class SensorlibFlutterPlugin implements EventChannel.StreamHandler, MethodChannel.MethodCallHandler
{

    private EventChannel.EventSink eventSink;
    private PermissionManager manager;
    private Registrar registrar;
    static String CONNECT_DEVICE = "connectDevice";

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
        manager.stopService();
        this.eventSink = null;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result)
    {
        if (methodCall.method.equals(CONNECT_DEVICE))
        {
            String deviceName = methodCall.argument("deviceName");
            connectToDevice(deviceName, methodCall);
        } else
        {
            result.notImplemented();
        }
    }

    private void connectToDevice(String deviceName, MethodCall methodCall)
    {
        switch (deviceName)
        {
            case "shimmer":
            {
                String macAddress = methodCall.argument("macAddress");
                SensorHandler sh = new ShimmerSensorHandler(registrar.activity(), this.eventSink, macAddress);
                manager = new PermissionManager(registrar.activity(), sh);
                break;
            }
            case "internal":
            {
                SensorHandler sh = new InternalSensorHandler(registrar.activity(), this.eventSink);
                manager = new PermissionManager(registrar.activity(), sh);
                break;
            }
            default:
                throw new RuntimeException("Unknown device: " + deviceName);
        }
        manager.startService();
    }

}
