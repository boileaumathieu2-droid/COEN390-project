package com.example.zone.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.example.zone.R;
import com.example.zone.model.HeartRateReading;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.TimerModel;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Keeps the Bluno BLE connection alive for the whole application.
 *
 * The connection used to be owned by HeartRateMonitorView, so Android closed
 * it when that screen was destroyed. This manager only stores the application
 * context and continues receiving readings while another screen is open.
 */
public final class HeartRateSensorManager {

    public interface Listener {
        void onConnectionStateChanged(String message, boolean connected, boolean busy);

        void onHeartRateReading(HeartRateReading rawReading, HeartRateReading stableReading);
    }

    private static final UUID BLUNO_SERVICE_UUID =
            UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUNO_SERIAL_UUID =
            UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUNO_COMMAND_UUID =
            UUID.fromString("0000dfb2-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CONFIGURATION_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String TARGET_BLUNO_ADDRESS = "D0:39:72:DF:D5:0E";
    private static final long[] RECONNECT_DELAYS_MS = {2_000L, 5_000L, 10_000L};

    private static volatile HeartRateSensorManager instance;

    private final Context applicationContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final HeartRatePacketParser packetParser = new HeartRatePacketParser();
    private final HeartRateStabilizer stabilizer = new HeartRateStabilizer();
    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();

    private volatile BluetoothGatt bluetoothGatt;
    private volatile boolean connected;
    private volatile boolean busy;
    private volatile String stateMessage;
    private volatile HeartRateReading lastRawReading;
    private volatile HeartRateReading lastStableReading;
    private BluetoothDevice reconnectDevice;
    private boolean autoReconnectEnabled;
    private boolean manualDisconnect = true;
    private int reconnectAttempt;

    private final Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            BluetoothDevice device;
            synchronized (HeartRateSensorManager.this) {
                if (!autoReconnectEnabled || manualDisconnect || connected) {
                    return;
                }
                device = reconnectDevice;
            }
            if (device != null) {
                beginConnection(device, false);
            }
        }
    };

    private HeartRateSensorManager(Context context) {
        applicationContext = context.getApplicationContext();
        stateMessage = applicationContext.getString(R.string.select_bluno_device);
    }

    public static HeartRateSensorManager getInstance(Context context) {
        if (instance == null) {
            synchronized (HeartRateSensorManager.class) {
                if (instance == null) {
                    instance = new HeartRateSensorManager(context);
                }
            }
        }
        return instance;
    }

    public void addListener(Listener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
        mainHandler.post(() -> {
            listener.onConnectionStateChanged(stateMessage, connected, busy);
            if (lastRawReading != null && lastStableReading != null) {
                listener.onHeartRateReading(lastRawReading, lastStableReading);
            }
        });
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isBusy() {
        return busy;
    }

    public HeartRateReading getLastStableReading() {
        return lastStableReading;
    }

    /**
     * Connects in the background when Bluetooth permission was granted before.
     * The connection page still handles the first-time permission request.
     */
    @SuppressLint("MissingPermission")
    public synchronized void autoConnectToSavedBluno() {
        if (connected || busy || !hasConnectPermission()) {
            return;
        }

        BluetoothManager manager =
                applicationContext.getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            return;
        }

        try {
            connect(adapter.getRemoteDevice(TARGET_BLUNO_ADDRESS), true);
        } catch (IllegalArgumentException ignored) {
            publishState(
                    applicationContext.getString(R.string.invalid_bluetooth_address),
                    false,
                    false
            );
        }
    }

    @SuppressLint("MissingPermission")
    public synchronized void connect(BluetoothDevice device) {
        connect(device, false);
    }

    @SuppressLint("MissingPermission")
    public synchronized void connect(BluetoothDevice device, boolean autoReconnect) {
        if (device == null) {
            publishState(applicationContext.getString(R.string.not_a_bluno_device), false, false);
            return;
        }
        if (!hasConnectPermission()) {
            publishState(
                    applicationContext.getString(R.string.bluetooth_permission_needed),
                    false,
                    false
            );
            return;
        }

        reconnectDevice = device;
        autoReconnectEnabled = autoReconnect;
        manualDisconnect = false;
        reconnectAttempt = 0;
        mainHandler.removeCallbacks(reconnectRunnable);
        beginConnection(device, true);
    }

    @SuppressLint("MissingPermission")
    private synchronized void beginConnection(BluetoothDevice device, boolean resetReadings) {
        if (!hasConnectPermission()) {
            publishState(
                    applicationContext.getString(R.string.bluetooth_permission_needed),
                    false,
                    false
            );
            return;
        }
        closeGatt();
        if (resetReadings) {
            packetParser.reset();
            stabilizer.reset();
            lastRawReading = null;
            lastStableReading = null;
            markSignalUnavailable();
        }

        String name = device.getName();
        if (name == null || name.trim().isEmpty()) {
            name = device.getAddress();
        }
        publishState(applicationContext.getString(R.string.connecting_to, name), false, true);
        bluetoothGatt = device.connectGatt(
                applicationContext,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
        );
        if (bluetoothGatt == null) {
            publishState(
                    applicationContext.getString(R.string.connection_failed, -1),
                    false,
                    false
            );
            scheduleReconnect();
        }
    }

    @SuppressLint("MissingPermission")
    public synchronized void disconnect() {
        manualDisconnect = true;
        autoReconnectEnabled = false;
        reconnectDevice = null;
        reconnectAttempt = 0;
        mainHandler.removeCallbacks(reconnectRunnable);
        BluetoothGatt gatt = bluetoothGatt;
        bluetoothGatt = null;
        connected = false;
        busy = false;
        packetParser.reset();
        stabilizer.reset();
        markSignalUnavailable();
        if (gatt != null) {
            if (hasConnectPermission()) {
                try {
                    gatt.disconnect();
                } catch (SecurityException ignored) {
                    // The permission can be revoked while the app is running.
                }
            }
            gatt.close();
        }
        publishState(applicationContext.getString(R.string.disconnected), false, false);
    }

    private boolean hasConnectPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void publishState(String message, boolean isConnected, boolean isBusy) {
        stateMessage = message;
        connected = isConnected;
        busy = isBusy;
        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onConnectionStateChanged(message, isConnected, isBusy);
            }
        });
    }

    private void publishReading(
            HeartRateReading rawReading,
            HeartRateReading stableReading
    ) {
        lastRawReading = rawReading;
        lastStableReading = stableReading;

        // NO_SIGNAL packets remain visible on the connection screen, but they
        // are not stored in a study session or used by Analytics.
        StudySessionModel.getInstance().setCurrentHeartRateReading(stableReading);
        StudySessionModel liveSession = TimerModel.getInstance().getLiveSession();
        if (liveSession != null) {
            // Passing NO_SIGNAL as BPM 0 clears the previous value. Otherwise
            // the timer would keep recording an old good value during signal loss.
            liveSession.setCurrentHeartRateReading(stableReading);
        }

        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onHeartRateReading(rawReading, stableReading);
            }
        });
    }

    private void markSignalUnavailable() {
        HeartRateReading unavailable = new HeartRateReading(0, 0, 0, "NO_SIGNAL");
        publishReading(unavailable, unavailable);
    }

    @SuppressLint("MissingPermission")
    private void finishBlunoSetup(BluetoothGatt gatt, BluetoothGattService service) {
        if (gatt != bluetoothGatt) {
            return;
        }

        BluetoothGattCharacteristic commandCharacteristic =
                service == null ? null : service.getCharacteristic(BLUNO_COMMAND_UUID);
        if (commandCharacteristic != null) {
            byte[] baudCommand =
                    "AT+CURRUART=115200\r\n".getBytes(StandardCharsets.US_ASCII);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                        commandCharacteristic,
                        baudCommand,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                );
            } else {
                commandCharacteristic.setWriteType(
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                );
                commandCharacteristic.setValue(baudCommand);
                gatt.writeCharacteristic(commandCharacteristic);
            }
        }

        publishState(
                applicationContext.getString(R.string.connected_waiting_for_data),
                true,
                false
        );
        synchronized (this) {
            reconnectAttempt = 0;
        }
    }

    private void handleIncomingData(byte[] value) {
        for (HeartRateReading rawReading : packetParser.append(value)) {
            HeartRateReading stableReading = stabilizer.filter(rawReading);
            if (stableReading != null) {
                publishReading(rawReading, stableReading);
                publishState(
                        applicationContext.getString(R.string.connected_receiving_data),
                        true,
                        false
                );
            }
        }
    }

    private void fail(String message, BluetoothGatt failedGatt) {
        if (failedGatt != null && failedGatt != bluetoothGatt) {
            return;
        }

        BluetoothGatt gatt = bluetoothGatt;
        bluetoothGatt = null;
        connected = false;
        busy = false;
        markSignalUnavailable();
        if (gatt != null) {
            if (hasConnectPermission()) {
                try {
                    gatt.close();
                } catch (SecurityException ignored) {
                    // Permission can be revoked between the callback and cleanup.
                }
            }
        }
        publishState(message, false, false);
        scheduleReconnect();
    }

    private synchronized void scheduleReconnect() {
        if (!autoReconnectEnabled || manualDisconnect || reconnectDevice == null) {
            return;
        }
        if (reconnectAttempt >= RECONNECT_DELAYS_MS.length) {
            publishState(
                    applicationContext.getString(R.string.bluno_reconnect_paused),
                    false,
                    false
            );
            return;
        }

        long delay = RECONNECT_DELAYS_MS[reconnectAttempt++];
        publishState(
                applicationContext.getString(
                        R.string.bluno_reconnecting,
                        Math.max(1L, delay / 1_000L)
                ),
                false,
                true
        );
        mainHandler.removeCallbacks(reconnectRunnable);
        mainHandler.postDelayed(reconnectRunnable, delay);
    }

    @SuppressLint("MissingPermission")
    private synchronized void closeGatt() {
        BluetoothGatt oldGatt = bluetoothGatt;
        bluetoothGatt = null;
        connected = false;
        busy = false;
        if (oldGatt != null) {
            if (hasConnectPermission()) {
                try {
                    oldGatt.disconnect();
                } catch (SecurityException ignored) {
                    // The permission can be revoked while the app is running.
                }
            }
            oldGatt.close();
        }
    }

    @SuppressLint("MissingPermission")
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (gatt != bluetoothGatt) {
                return;
            }

            if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_CONNECTED) {
                publishState(
                        applicationContext.getString(R.string.discovering_bluno_services),
                        false,
                        true
                );
                if (!gatt.discoverServices()) {
                    fail(
                            applicationContext.getString(R.string.service_discovery_failed),
                            gatt
                    );
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                packetParser.reset();
                stabilizer.reset();
                markSignalUnavailable();
                if (gatt == bluetoothGatt) {
                    bluetoothGatt = null;
                }
                gatt.close();
                publishState(applicationContext.getString(R.string.disconnected), false, false);
                scheduleReconnect();
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                fail(
                        applicationContext.getString(R.string.connection_failed, status),
                        gatt
                );
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (gatt != bluetoothGatt) {
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                fail(
                        applicationContext.getString(
                                R.string.service_discovery_error,
                                status
                        ),
                        gatt
                );
                return;
            }

            BluetoothGattService service = gatt.getService(BLUNO_SERVICE_UUID);
            BluetoothGattCharacteristic serialCharacteristic =
                    service == null ? null : service.getCharacteristic(BLUNO_SERIAL_UUID);
            if (serialCharacteristic == null) {
                fail(applicationContext.getString(R.string.not_a_bluno_device), gatt);
                return;
            }

            if (!gatt.setCharacteristicNotification(serialCharacteristic, true)) {
                fail(
                        applicationContext.getString(R.string.notification_setup_failed),
                        gatt
                );
                return;
            }

            BluetoothGattDescriptor descriptor =
                    serialCharacteristic.getDescriptor(CLIENT_CONFIGURATION_UUID);
            if (descriptor == null) {
                finishBlunoSetup(gatt, service);
                return;
            }

            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result = gatt.writeDescriptor(
                        descriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                );
            } else {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                result = gatt.writeDescriptor(descriptor)
                        ? BluetoothGatt.GATT_SUCCESS : -1;
            }

            if (result != BluetoothGatt.GATT_SUCCESS) {
                fail(
                        applicationContext.getString(R.string.notification_setup_failed),
                        gatt
                );
            }
        }

        @Override
        public void onDescriptorWrite(
                BluetoothGatt gatt,
                BluetoothGattDescriptor descriptor,
                int status
        ) {
            if (gatt != bluetoothGatt
                    || !CLIENT_CONFIGURATION_UUID.equals(descriptor.getUuid())) {
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                fail(
                        applicationContext.getString(R.string.notification_setup_failed),
                        gatt
                );
                return;
            }
            finishBlunoSetup(gatt, descriptor.getCharacteristic().getService());
        }

        @Override
        public void onCharacteristicChanged(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                byte[] value
        ) {
            if (gatt == bluetoothGatt
                    && BLUNO_SERIAL_UUID.equals(characteristic.getUuid())) {
                handleIncomingData(value);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public void onCharacteristicChanged(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    && gatt == bluetoothGatt
                    && BLUNO_SERIAL_UUID.equals(characteristic.getUuid())) {
                handleIncomingData(characteristic.getValue());
            }
        }
    };
}
