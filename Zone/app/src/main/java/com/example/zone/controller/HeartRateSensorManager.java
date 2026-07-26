package com.example.zone.controller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.zone.R;
import com.example.zone.model.HeartRateReading;
import com.example.zone.model.StudySessionModel;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HeartRateSensorManager {

    public interface Listener {
        void onConnectionStateChanged(String message, boolean connected);

        void onHeartRateReading(HeartRateReading reading);
    }

    private static final UUID BLUNO_SERVICE_UUID =
            UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");

    private static final UUID BLUNO_SERIAL_UUID =
            UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");

    private static final UUID BLUNO_COMMAND_UUID =
            UUID.fromString("0000dfb2-0000-1000-8000-00805f9b34fb");

    private static final UUID CLIENT_CONFIGURATION_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static HeartRateSensorManager instance;

    private final Context applicationContext;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private final HeartRatePacketParser packetParser =
            new HeartRatePacketParser();

    private final Set<Listener> listeners =
            new CopyOnWriteArraySet<>();

    private BluetoothGatt bluetoothGatt;
    private HeartRateReading latestReading;
    private String connectionMessage;

    private boolean connected;
    private boolean connecting;

    private long lastSessionSampleTime;

    private HeartRateSensorManager(Context context) {
        applicationContext = context.getApplicationContext();

        connectionMessage = applicationContext.getString(
                R.string.select_bluno_device
        );
    }

    public static synchronized HeartRateSensorManager getInstance(
            Context context
    ) {
        if (instance == null) {
            instance = new HeartRateSensorManager(context);
        }

        return instance;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);

        mainHandler.post(() -> {
            listener.onConnectionStateChanged(
                    connectionMessage,
                    connected
            );

            if (latestReading != null) {
                listener.onHeartRateReading(latestReading);
            }
        });
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isConnecting() {
        return connecting;
    }

    @SuppressLint("MissingPermission")
    public synchronized void connect(BluetoothDevice device) {
        closeCurrentGatt();

        packetParser.reset();

        connected = false;
        connecting = true;

        String deviceName = device.getName();

        if (deviceName == null || deviceName.trim().isEmpty()) {
            deviceName = device.getAddress();
        }

        publishState(
                applicationContext.getString(
                        R.string.connecting_to,
                        deviceName
                ),
                false
        );

        bluetoothGatt = device.connectGatt(
                applicationContext,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
        );
    }

    @SuppressLint("MissingPermission")
    public synchronized void disconnect() {
        connecting = false;
        connected = false;

        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        } else {
            publishState(
                    applicationContext.getString(
                            R.string.disconnected
                    ),
                    false
            );
        }
    }

    @SuppressLint("MissingPermission")
    private synchronized void closeCurrentGatt() {
        if (bluetoothGatt == null) {
            return;
        }

        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private void publishState(
            String message,
            boolean isConnected
    ) {
        connectionMessage = message;
        connected = isConnected;

        if (isConnected) {
            connecting = false;
        }

        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onConnectionStateChanged(
                        message,
                        isConnected
                );
            }
        });
    }

    private void publishReading(HeartRateReading reading) {
        latestReading = reading;

        StudySessionModel studySession =
                StudySessionModel.getInstance();

        studySession.setCurrentHeartRateReading(reading);

        saveStudySessionReading(studySession, reading);

        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onHeartRateReading(reading);
            }
        });
    }

    private void saveStudySessionReading(
            StudySessionModel studySession,
            HeartRateReading reading
    ) {
        if (!studySession.isActive()) {
            lastSessionSampleTime = 0L;
            return;
        }

        if (!reading.hasGoodSignal()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (lastSessionSampleTime == 0L) {
            lastSessionSampleTime = currentTime;

            if (studySession.getHeartRateData().length == 0) {
                studySession.addHeartRateReading();
            }

            return;
        }

        if (currentTime - lastSessionSampleTime >= 5_000L) {
            studySession.addHeartRateReading();
            lastSessionSampleTime = currentTime;
        }
    }

    private void handleIncomingData(byte[] data) {
        for (HeartRateReading reading : packetParser.append(data)) {
            publishReading(reading);
        }
    }

    @SuppressLint("MissingPermission")
    private void finishBlunoSetup(
            BluetoothGatt gatt,
            BluetoothGattService service
    ) {
        BluetoothGattCharacteristic commandCharacteristic =
                service == null
                        ? null
                        : service.getCharacteristic(
                        BLUNO_COMMAND_UUID
                );

        if (commandCharacteristic != null) {
            byte[] command =
                    "AT+CURRUART=115200\r\n"
                            .getBytes(StandardCharsets.US_ASCII);

            if (Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.TIRAMISU) {

                gatt.writeCharacteristic(
                        commandCharacteristic,
                        command,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                );

            } else {
                commandCharacteristic.setWriteType(
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                );

                commandCharacteristic.setValue(command);

                gatt.writeCharacteristic(commandCharacteristic);
            }
        }

        publishState(
                applicationContext.getString(
                        R.string.connected_waiting_for_data
                ),
                true
        );
    }

    @SuppressLint("MissingPermission")
    private void fail(
            String message,
            BluetoothGatt gatt
    ) {
        connecting = false;
        connected = false;

        if (gatt != null) {
            gatt.close();
        }

        synchronized (this) {
            if (gatt == bluetoothGatt) {
                bluetoothGatt = null;
            }
        }

        publishState(message, false);
    }

    @SuppressLint("MissingPermission")
    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(
                        @NonNull BluetoothGatt gatt,
                        int status,
                        int newState
                ) {
                    if (gatt != bluetoothGatt) {
                        return;
                    }

                    if (status == BluetoothGatt.GATT_SUCCESS
                            && newState
                            == BluetoothProfile.STATE_CONNECTED) {

                        publishState(
                                applicationContext.getString(
                                        R.string.discovering_bluno_services
                                ),
                                false
                        );

                        if (!gatt.discoverServices()) {
                            fail(
                                    applicationContext.getString(
                                            R.string.service_discovery_failed
                                    ),
                                    gatt
                            );
                        }

                        return;
                    }

                    if (newState
                            == BluetoothProfile.STATE_DISCONNECTED) {

                        packetParser.reset();

                        connecting = false;
                        connected = false;

                        gatt.close();

                        synchronized (HeartRateSensorManager.this) {
                            if (gatt == bluetoothGatt) {
                                bluetoothGatt = null;
                            }
                        }

                        publishState(
                                applicationContext.getString(
                                        R.string.disconnected
                                ),
                                false
                        );

                        return;
                    }

                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        fail(
                                applicationContext.getString(
                                        R.string.connection_failed,
                                        status
                                ),
                                gatt
                        );
                    }
                }

                @Override
                public void onServicesDiscovered(
                        @NonNull BluetoothGatt gatt,
                        int status
                ) {
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

                    BluetoothGattService blunoService =
                            gatt.getService(BLUNO_SERVICE_UUID);

                    BluetoothGattCharacteristic serialCharacteristic =
                            blunoService == null
                                    ? null
                                    : blunoService.getCharacteristic(
                                    BLUNO_SERIAL_UUID
                            );

                    if (serialCharacteristic == null) {
                        fail(
                                applicationContext.getString(
                                        R.string.not_a_bluno_device
                                ),
                                gatt
                        );

                        return;
                    }

                    boolean notificationsEnabled =
                            gatt.setCharacteristicNotification(
                                    serialCharacteristic,
                                    true
                            );

                    if (!notificationsEnabled) {
                        fail(
                                applicationContext.getString(
                                        R.string.notification_setup_failed
                                ),
                                gatt
                        );

                        return;
                    }

                    BluetoothGattDescriptor descriptor =
                            serialCharacteristic.getDescriptor(
                                    CLIENT_CONFIGURATION_UUID
                            );

                    if (descriptor == null) {
                        finishBlunoSetup(gatt, blunoService);
                        return;
                    }

                    int result;

                    if (Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.TIRAMISU) {

                        result = gatt.writeDescriptor(
                                descriptor,
                                BluetoothGattDescriptor
                                        .ENABLE_NOTIFICATION_VALUE
                        );

                    } else {
                        descriptor.setValue(
                                BluetoothGattDescriptor
                                        .ENABLE_NOTIFICATION_VALUE
                        );

                        result = gatt.writeDescriptor(descriptor)
                                ? BluetoothGatt.GATT_SUCCESS
                                : -1;
                    }

                    if (result != BluetoothGatt.GATT_SUCCESS) {
                        fail(
                                applicationContext.getString(
                                        R.string.notification_setup_failed
                                ),
                                gatt
                        );
                    }
                }

                @Override
                public void onDescriptorWrite(
                        @NonNull BluetoothGatt gatt,
                        @NonNull BluetoothGattDescriptor descriptor,
                        int status
                ) {
                    if (gatt != bluetoothGatt) {
                        return;
                    }

                    if (!CLIENT_CONFIGURATION_UUID.equals(
                            descriptor.getUuid()
                    )) {
                        return;
                    }

                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        fail(
                                applicationContext.getString(
                                        R.string.notification_setup_failed
                                ),
                                gatt
                        );

                        return;
                    }

                    finishBlunoSetup(
                            gatt,
                            descriptor
                                    .getCharacteristic()
                                    .getService()
                    );
                }

                @Override
                public void onCharacteristicChanged(
                        @NonNull BluetoothGatt gatt,
                        @NonNull BluetoothGattCharacteristic characteristic,
                        @NonNull byte[] value
                ) {
                    if (gatt == bluetoothGatt
                            && BLUNO_SERIAL_UUID.equals(
                            characteristic.getUuid()
                    )) {

                        handleIncomingData(value);
                    }
                }

                @Override
                @SuppressWarnings("deprecation")
                public void onCharacteristicChanged(
                        @NonNull BluetoothGatt gatt,
                        @NonNull BluetoothGattCharacteristic characteristic
                ) {
                    if (Build.VERSION.SDK_INT
                            < Build.VERSION_CODES.TIRAMISU
                            && gatt == bluetoothGatt
                            && BLUNO_SERIAL_UUID.equals(
                            characteristic.getUuid()
                    )) {

                        handleIncomingData(
                                characteristic.getValue()
                        );
                    }
                }
            };
}