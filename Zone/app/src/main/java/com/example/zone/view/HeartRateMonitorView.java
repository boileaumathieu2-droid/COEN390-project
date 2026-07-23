package com.example.zone.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zone.R;
import com.example.zone.controller.HeartRatePacketParser;
import com.example.zone.model.HeartRateReading;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HeartRateMonitorView extends AppCompatActivity {
    // Official DFRobot Bluno BLE UART service and characteristics.
    private static final UUID BLUNO_SERVICE_UUID =
            UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUNO_SERIAL_UUID =
            UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUNO_COMMAND_UUID =
            UUID.fromString("0000dfb2-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CONFIGURATION_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String TARGET_BLUNO_ADDRESS = "D0:39:72:DF:D5:0E";
    private static final long SCAN_DURATION_MS = 15_000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final HeartRatePacketParser packetParser = new HeartRatePacketParser();
    private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private final List<String> deviceLabels = new ArrayList<>();
    private final Map<String, String> bestDeviceNames = new HashMap<>();
    private final Set<String> blunoCandidateAddresses = new HashSet<>();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private ArrayAdapter<String> deviceListAdapter;
    private boolean scanning;
    private boolean scanAfterPermission;
    private String pendingDirectAddress;

    private TextView connectionStatusText;
    private TextView bpmText;
    private TextView signalStatusText;
    private TextView rawValueText;
    private TextView signalRangeText;
    private TextView lastPacketText;
    private TextView emptyDeviceText;
    private EditText deviceIdInput;
    private Button scanButton;
    private Button connectByIdButton;
    private Button disconnectButton;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = true;
                for (String permission : requiredPermissions()) {
                    granted &= Boolean.TRUE.equals(result.get(permission));
                }

                if (granted && pendingDirectAddress != null) {
                    String address = pendingDirectAddress;
                    pendingDirectAddress = null;
                    connectUsingAddress(address);
                } else if (granted && scanAfterPermission) {
                    scanAfterPermission = false;
                    beginScan();
                } else if (!granted) {
                    scanAfterPermission = false;
                    pendingDirectAddress = null;
                    showConnectionState(getString(R.string.bluetooth_permission_needed), false);
                }
            });

    private final ActivityResultLauncher<Intent> enableBluetoothLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    if (pendingDirectAddress != null) {
                        String address = pendingDirectAddress;
                        pendingDirectAddress = null;
                        connectUsingAddress(address);
                    } else {
                        beginScan();
                    }
                } else {
                    showConnectionState(getString(R.string.bluetooth_disabled), false);
                }
            });

    private final Runnable scanTimeout = this::stopScan;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addDiscoveredDevice(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addDiscoveredDevice(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            runOnUiThread(() -> {
                stopScan();
                showConnectionState(getString(R.string.scan_failed, errorCode), false);
            });
        }
    };

    // Every GATT operation below is reached only after connectToDevice verifies the
    // version-appropriate runtime permissions. SuppressLint documents that invariant.
    @SuppressLint("MissingPermission")
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (gatt != bluetoothGatt) {
                return;
            }

            if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread(() -> showConnectionState(
                        getString(R.string.discovering_bluno_services), false));
                if (!gatt.discoverServices()) {
                    showGattError(getString(R.string.service_discovery_failed));
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                packetParser.reset();
                runOnUiThread(() -> {
                    showConnectionState(getString(R.string.disconnected), false);
                    disconnectButton.setEnabled(false);
                });
                gatt.close();
                if (gatt == bluetoothGatt) {
                    bluetoothGatt = null;
                }
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                showGattError(getString(R.string.connection_failed, status));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (gatt != bluetoothGatt) {
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                showGattError(getString(R.string.service_discovery_error, status));
                return;
            }

            BluetoothGattService service = gatt.getService(BLUNO_SERVICE_UUID);
            BluetoothGattCharacteristic serialCharacteristic = service == null
                    ? null : service.getCharacteristic(BLUNO_SERIAL_UUID);
            if (serialCharacteristic == null) {
                showGattError(getString(R.string.not_a_bluno_device));
                return;
            }

            boolean notificationStarted = gatt.setCharacteristicNotification(
                    serialCharacteristic, true);
            if (!notificationStarted) {
                showGattError(getString(R.string.notification_setup_failed));
                return;
            }

            BluetoothGattDescriptor descriptor =
                    serialCharacteristic.getDescriptor(CLIENT_CONFIGURATION_UUID);
            if (descriptor == null) {
                // Some Bluno firmware versions expose notification without a CCCD.
                finishBlunoSetup(gatt, service);
                return;
            }

            int writeResult;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                writeResult = gatt.writeDescriptor(
                        descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                writeResult = gatt.writeDescriptor(descriptor)
                        ? BluetoothGatt.GATT_SUCCESS : -1;
            }
            if (writeResult != BluetoothGatt.GATT_SUCCESS) {
                showGattError(getString(R.string.notification_setup_failed));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (gatt != bluetoothGatt || !CLIENT_CONFIGURATION_UUID.equals(descriptor.getUuid())) {
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                showGattError(getString(R.string.notification_setup_failed));
                return;
            }
            finishBlunoSetup(gatt, descriptor.getCharacteristic().getService());
        }

        @Override
        public void onCharacteristicChanged(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                byte[] value) {
            if (gatt == bluetoothGatt && BLUNO_SERIAL_UUID.equals(characteristic.getUuid())) {
                handleIncomingData(value);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public void onCharacteristicChanged(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    && gatt == bluetoothGatt
                    && BLUNO_SERIAL_UUID.equals(characteristic.getUuid())) {
                handleIncomingData(characteristic.getValue());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_monitor);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.heart_rate_monitor);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        connectionStatusText = findViewById(R.id.connectionStatusText);
        bpmText = findViewById(R.id.bpmText);
        signalStatusText = findViewById(R.id.signalStatusText);
        rawValueText = findViewById(R.id.rawValueText);
        signalRangeText = findViewById(R.id.signalRangeText);
        lastPacketText = findViewById(R.id.lastPacketText);
        emptyDeviceText = findViewById(R.id.emptyDeviceText);
        deviceIdInput = findViewById(R.id.deviceIdInput);
        scanButton = findViewById(R.id.scanButton);
        connectByIdButton = findViewById(R.id.connectByIdButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        ListView deviceList = findViewById(R.id.deviceList);

        deviceListAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, deviceLabels);
        deviceList.setAdapter(deviceListAdapter);
        deviceList.setEmptyView(emptyDeviceText);
        deviceList.setNestedScrollingEnabled(true);
        deviceList.setOnTouchListener((list, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                // The list is inside the page ScrollView. Keep vertical gestures in
                // the device list so every scan result remains reachable.
                list.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL) {
                list.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
        deviceList.setOnItemClickListener((parent, view, position, id) ->
                connectToDevice(discoveredDevices.get(position)));

        scanButton.setOnClickListener(view -> scanForDevices());
        connectByIdButton.setOnClickListener(view -> connectUsingEnteredDeviceId());
        disconnectButton.setOnClickListener(view -> disconnectFromDevice());
        disconnectButton.setEnabled(false);

        BluetoothManager manager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = manager == null ? null : manager.getAdapter();
        if (bluetoothAdapter == null
                || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            scanButton.setEnabled(false);
            connectByIdButton.setEnabled(false);
            showConnectionState(getString(R.string.ble_not_supported), false);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String[] requiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        }
        return new String[] {Manifest.permission.ACCESS_FINE_LOCATION};
    }

    private boolean hasRequiredPermissions() {
        for (String permission : requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void scanForDevices() {
        if (!hasRequiredPermissions()) {
            scanAfterPermission = true;
            permissionLauncher.launch(requiredPermissions());
            return;
        }
        beginScan();
    }

    private void connectUsingEnteredDeviceId() {
        String address = deviceIdInput.getText().toString()
                .trim()
                .toUpperCase(Locale.US);
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            deviceIdInput.setError(getString(R.string.invalid_bluetooth_address));
            return;
        }
        deviceIdInput.setError(null);

        if (!hasRequiredPermissions()) {
            scanAfterPermission = false;
            pendingDirectAddress = address;
            permissionLauncher.launch(requiredPermissions());
            return;
        }
        connectUsingAddress(address);
    }

    @SuppressLint("MissingPermission")
    private void connectUsingAddress(String address) {
        if (bluetoothAdapter == null) {
            showConnectionState(getString(R.string.ble_not_supported), false);
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            pendingDirectAddress = address;
            enableBluetoothLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            showConnectionState(getString(R.string.connecting_device_id, address), false);
            connectToDevice(device);
        } catch (IllegalArgumentException error) {
            deviceIdInput.setError(getString(R.string.invalid_bluetooth_address));
        }
    }

    @SuppressLint("MissingPermission")
    private void beginScan() {
        scanAfterPermission = false;
        if (bluetoothAdapter == null) {
            showConnectionState(getString(R.string.ble_not_supported), false);
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            enableBluetoothLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            showConnectionState(getString(R.string.scanner_unavailable), false);
            return;
        }

        if (scanning) {
            stopScan();
        }
        discoveredDevices.clear();
        deviceLabels.clear();
        bestDeviceNames.clear();
        blunoCandidateAddresses.clear();
        deviceListAdapter.notifyDataSetChanged();
        emptyDeviceText.setText(R.string.scanning_for_devices);

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .build();
        scanning = true;
        scanButton.setEnabled(false);
        showConnectionState(getString(R.string.scanning), false);
        try {
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback);
        } catch (SecurityException | IllegalStateException error) {
            scanning = false;
            scanButton.setEnabled(true);
            showConnectionState(getString(R.string.scan_start_failed), false);
            return;
        }
        mainHandler.removeCallbacks(scanTimeout);
        mainHandler.postDelayed(scanTimeout, SCAN_DURATION_MS);
    }

    @SuppressLint("MissingPermission")
    private void stopScan() {
        mainHandler.removeCallbacks(scanTimeout);
        if (scanning && bluetoothLeScanner != null && hasRequiredPermissions()) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
        scanning = false;
        scanButton.setEnabled(bluetoothAdapter != null);
        if (discoveredDevices.isEmpty()) {
            emptyDeviceText.setText(R.string.no_ble_devices_found);
        }
        if (bluetoothGatt == null) {
            showConnectionState(getString(R.string.select_bluno_device), false);
        }
    }

    @SuppressLint("MissingPermission")
    private void addDiscoveredDevice(ScanResult result) {
        if (!hasRequiredPermissions() || result == null || result.getDevice() == null) {
            return;
        }

        BluetoothDevice device = result.getDevice();
        String address = device.getAddress();
        String reportedName = device.getName();
        if ((reportedName == null || reportedName.trim().isEmpty())
                && result.getScanRecord() != null) {
            reportedName = result.getScanRecord().getDeviceName();
        }

        boolean advertisesBlunoService = false;
        if (result.getScanRecord() != null
                && result.getScanRecord().getServiceUuids() != null) {
            for (ParcelUuid serviceUuid : result.getScanRecord().getServiceUuids()) {
                if (BLUNO_SERVICE_UUID.equals(serviceUuid.getUuid())) {
                    advertisesBlunoService = true;
                    break;
                }
            }
        }

        String normalizedName = reportedName == null
                ? "" : reportedName.trim().toLowerCase(Locale.US);
        boolean nameLooksLikeBluno = normalizedName.contains("bluno")
                || normalizedName.contains("dfrobot")
                || normalizedName.contains("df robot");
        boolean isBlunoCandidate = address.equalsIgnoreCase(TARGET_BLUNO_ADDRESS)
                || advertisesBlunoService
                || nameLooksLikeBluno;
        String finalReportedName = reportedName;
        boolean finalIsBlunoCandidate = isBlunoCandidate;
        runOnUiThread(() -> {
            if (finalReportedName != null && !finalReportedName.trim().isEmpty()) {
                // A scan response often supplies the name after the first unnamed
                // advertisement. Keep the best name instead of discarding duplicates.
                bestDeviceNames.put(address, finalReportedName.trim());
            } else if (!bestDeviceNames.containsKey(address)) {
                bestDeviceNames.put(address, getString(R.string.unnamed_ble_device));
            }
            if (finalIsBlunoCandidate) {
                blunoCandidateAddresses.add(address);
            }

            boolean likelyBluno = blunoCandidateAddresses.contains(address);
            String bestName = bestDeviceNames.get(address);
            String label = getString(R.string.ble_device_label, bestName, address);
            if (likelyBluno) {
                label += "\n" + getString(R.string.likely_bluno_device);
            }

            int existingIndex = -1;
            for (int i = 0; i < discoveredDevices.size(); i++) {
                if (discoveredDevices.get(i).getAddress().equals(address)) {
                    existingIndex = i;
                    break;
                }
            }

            if (existingIndex >= 0) {
                BluetoothDevice existingDevice = discoveredDevices.remove(existingIndex);
                deviceLabels.remove(existingIndex);
                int insertAt = likelyBluno ? 0 : existingIndex;
                discoveredDevices.add(insertAt, existingDevice);
                deviceLabels.add(insertAt, label);
                deviceListAdapter.notifyDataSetChanged();
                if (likelyBluno) {
                    showConnectionState(getString(R.string.bluno_found), false);
                }
                return;
            }

            int insertAt = likelyBluno ? 0 : deviceLabels.size();
            discoveredDevices.add(insertAt, device);
            deviceLabels.add(insertAt, label);
            deviceListAdapter.notifyDataSetChanged();
            emptyDeviceText.setVisibility(View.GONE);
            if (likelyBluno) {
                showConnectionState(getString(R.string.bluno_found), false);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, R.string.bluetooth_permission_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        stopScan();
        closeGatt();
        packetParser.reset();

        String name = device.getName();
        if (name == null || name.trim().isEmpty()) {
            name = device.getAddress();
        }
        showConnectionState(getString(R.string.connecting_to, name), false);
        disconnectButton.setEnabled(true);
        bluetoothGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    @SuppressLint("MissingPermission")
    private void finishBlunoSetup(BluetoothGatt gatt, BluetoothGattService service) {
        // Match the Arduino sketch's Serial.begin(115200). This command is harmless
        // on firmware where 115200 is already stored and improves first-time setup.
        BluetoothGattCharacteristic commandCharacteristic =
                service == null ? null : service.getCharacteristic(BLUNO_COMMAND_UUID);
        if (commandCharacteristic != null) {
            byte[] baudCommand = "AT+CURRUART=115200\r\n".getBytes(StandardCharsets.US_ASCII);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                        commandCharacteristic,
                        baudCommand,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            } else {
                commandCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                commandCharacteristic.setValue(baudCommand);
                gatt.writeCharacteristic(commandCharacteristic);
            }
        }

        runOnUiThread(() -> {
            showConnectionState(getString(R.string.connected_waiting_for_data), true);
            disconnectButton.setEnabled(true);
        });
    }

    private void handleIncomingData(byte[] bytes) {
        for (HeartRateReading reading : packetParser.append(bytes)) {
            runOnUiThread(() -> displayReading(reading));
        }
    }

    private void displayReading(HeartRateReading reading) {
        rawValueText.setText(getString(R.string.raw_value, reading.getRawValue()));
        signalRangeText.setText(getString(R.string.signal_range, reading.getSignalRange()));
        lastPacketText.setText(reading.toPacketString());

        if (!reading.hasGoodSignal()) {
            bpmText.setText(R.string.bpm_placeholder);
            signalStatusText.setText(R.string.no_signal_instructions);
            signalStatusText.setTextColor(ContextCompat.getColor(this, R.color.zone_warning));
        } else if (reading.getBpm() == 0) {
            bpmText.setText(R.string.bpm_placeholder);
            signalStatusText.setText(R.string.calculating_bpm);
            signalStatusText.setTextColor(ContextCompat.getColor(this, R.color.zone_primary));
        } else {
            bpmText.setText(String.valueOf(reading.getBpm()));
            signalStatusText.setText(R.string.live_reading);
            signalStatusText.setTextColor(ContextCompat.getColor(this, R.color.zone_success));
        }
        showConnectionState(getString(R.string.connected_receiving_data), true);
    }

    private void showConnectionState(String message, boolean connected) {
        connectionStatusText.setText(message);
        int color = connected ? R.color.zone_success : R.color.zone_text_secondary;
        connectionStatusText.setTextColor(ContextCompat.getColor(this, color));
    }

    private void showGattError(String message) {
        runOnUiThread(() -> {
            showConnectionState(message, false);
            disconnectButton.setEnabled(false);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    @SuppressLint("MissingPermission")
    private void disconnectFromDevice() {
        if (bluetoothGatt != null && hasRequiredPermissions()) {
            bluetoothGatt.disconnect();
        } else {
            showConnectionState(getString(R.string.disconnected), false);
            disconnectButton.setEnabled(false);
        }
    }

    @SuppressLint("MissingPermission")
    private void closeGatt() {
        if (bluetoothGatt != null) {
            if (hasRequiredPermissions()) {
                bluetoothGatt.disconnect();
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopScan();
        closeGatt();
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
