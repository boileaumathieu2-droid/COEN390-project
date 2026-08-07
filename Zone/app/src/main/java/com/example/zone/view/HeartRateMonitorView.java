package com.example.zone.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zone.R;
import com.example.zone.controller.HeartRateSensorManager;
import com.example.zone.model.HeartRateReading;
import com.example.zone.model.HeartRateRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HeartRateMonitorView extends AppCompatActivity {

    private static final UUID BLUNO_SERVICE_UUID =
            UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private static final String TARGET_BLUNO_ADDRESS = "D0:39:72:DF:D5:0E";
    private static final long SCAN_DURATION_MS = 20_000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private final List<String> deviceLabels = new ArrayList<>();
    private final Map<String, String> bestDeviceNames = new HashMap<>();
    private final Map<String, Integer> deviceSignalStrengths = new HashMap<>();
    private final Set<String> blunoCandidateAddresses = new HashSet<>();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private HeartRateSensorManager sensorManager;
    private ArrayAdapter<String> deviceListAdapter;
    private boolean scanning;
    private boolean scanAfterPermission;
    private boolean automaticConnectionRequested;
    private boolean bluetoothReceiverRegistered;
    private String pendingDirectAddress;

    private TextView connectionStatusText;
    private TextView bpmText;
    private TextView signalStatusText;
    private TextView rawValueText;
    private TextView signalRangeText;
    private TextView lastPacketText;
    private TextView emptyDeviceText;
    private ImageView heartIcon;
    private EditText deviceIdInput;
    private Button scanButton;
    private Button connectByIdButton;
    private Button disconnectButton;

    private final BroadcastReceiver bluetoothDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = getBluetoothDeviceExtra(intent);
                if (device != null) {
                    addClassicDevice(device, getString(R.string.classic_device_source));
                }
            }
        }
    };

    private final HeartRateSensorManager.Listener sensorListener =
            new HeartRateSensorManager.Listener() {
                @Override
                public void onConnectionStateChanged(
                        String message,
                        boolean connected,
                        boolean busy
                ) {
                    showConnectionState(message, connected);
                    disconnectButton.setEnabled(connected || busy);
                }

                @Override
                public void onHeartRateReading(
                        HeartRateReading rawReading,
                        HeartRateReading stableReading
                ) {
                    displayReading(rawReading, stableReading);
                }
            };
    private final HeartRateSensorManager.WellnessListener wellnessListener =
            this::showWellnessSuggestion;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
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
                            showConnectionState(
                                    getString(R.string.bluetooth_permission_needed),
                                    false
                            );
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> enableBluetoothLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                            if (pendingDirectAddress != null) {
                                String address = pendingDirectAddress;
                                pendingDirectAddress = null;
                                connectUsingAddress(address);
                            } else {
                                beginScan();
                            }
                        } else {
                            showConnectionState(
                                    getString(R.string.bluetooth_disabled),
                                    false
                            );
                        }
                    }
            );

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
        heartIcon = findViewById(R.id.heartIcon);
        deviceIdInput = findViewById(R.id.deviceIdInput);
        scanButton = findViewById(R.id.scanButton);
        connectByIdButton = findViewById(R.id.connectByIdButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        ListView deviceList = findViewById(R.id.deviceList);

        sensorManager = HeartRateSensorManager.getInstance(getApplicationContext());

        deviceListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                deviceLabels
        );
        deviceList.setAdapter(deviceListAdapter);
        deviceList.setEmptyView(emptyDeviceText);
        deviceList.setNestedScrollingEnabled(true);
        deviceList.setOnTouchListener((list, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
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
        disconnectButton.setEnabled(sensorManager.isConnected() || sensorManager.isBusy());

        BluetoothManager manager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = manager == null ? null : manager.getAdapter();
        registerBluetoothDiscoveryReceiver();
        if (bluetoothAdapter == null) {
            scanButton.setEnabled(false);
            connectByIdButton.setEnabled(false);
            showConnectionState(getString(R.string.ble_not_supported), false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.addListener(sensorListener);
        sensorManager.addWellnessListener(wellnessListener);
        requestAutomaticBlunoConnection();
    }

    @Override
    protected void onStop() {
        sensorManager.removeListener(sensorListener);
        sensorManager.removeWellnessListener(wellnessListener);
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String[] requiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        }
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
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

    private void requestAutomaticBlunoConnection() {
        if (automaticConnectionRequested
                || sensorManager.isConnected()
                || sensorManager.isBusy()
                || bluetoothAdapter == null) {
            return;
        }
        automaticConnectionRequested = true;

        if (!hasRequiredPermissions()) {
            scanAfterPermission = false;
            pendingDirectAddress = TARGET_BLUNO_ADDRESS;
            permissionLauncher.launch(requiredPermissions());
            return;
        }
        connectUsingAddress(TARGET_BLUNO_ADDRESS);
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
            enableBluetoothLauncher.launch(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            );
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            showConnectionState(
                    getString(R.string.connecting_device_id, address),
                    false
            );
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
            enableBluetoothLauncher.launch(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            );
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (scanning) {
            stopScan();
        }
        discoveredDevices.clear();
        deviceLabels.clear();
        bestDeviceNames.clear();
        deviceSignalStrengths.clear();
        blunoCandidateAddresses.clear();
        deviceListAdapter.notifyDataSetChanged();
        emptyDeviceText.setText(R.string.scanning_for_nearby_devices);
        emptyDeviceText.setVisibility(
                discoveredDevices.isEmpty() ? View.VISIBLE : View.GONE
        );

        scanning = true;
        scanButton.setEnabled(false);
        showConnectionState(getString(R.string.scanning), false);
        boolean discoveryStarted = false;
        try {
            if (bluetoothLeScanner != null) {
                // No filter: show all BLE advertisers, not only the Bluno.
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build();
                bluetoothLeScanner.startScan(null, settings, scanCallback);
                discoveryStarted = true;
            }
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            discoveryStarted = bluetoothAdapter.startDiscovery() || discoveryStarted;
        } catch (SecurityException | IllegalStateException error) {
            scanning = false;
            scanButton.setEnabled(true);
            showConnectionState(getString(R.string.scan_start_failed), false);
            return;
        }
        if (!discoveryStarted) {
            scanning = false;
            scanButton.setEnabled(true);
            showConnectionState(getString(R.string.scanner_unavailable), false);
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
        if (bluetoothAdapter != null
                && hasRequiredPermissions()
                && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        scanning = false;
        scanButton.setEnabled(bluetoothAdapter != null);
        if (discoveredDevices.isEmpty()) {
            emptyDeviceText.setText(R.string.no_bluetooth_devices_found);
        }
        if (!sensorManager.isConnected() && !sensorManager.isBusy()) {
            showConnectionState(getString(R.string.select_bluno_device), false);
        }
    }

    @SuppressLint("MissingPermission")
    private void addClassicDevice(BluetoothDevice device, String source) {
        if (!hasRequiredPermissions() || device == null || isPreviouslyPaired(device)) {
            return;
        }
        String address = device.getAddress();
        for (int index = 0; index < discoveredDevices.size(); index++) {
            if (discoveredDevices.get(index).getAddress().equalsIgnoreCase(address)) {
                return;
            }
        }

        String name = device.getName();
        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.unnamed_ble_device);
        }
        boolean likelyBluno = address.equalsIgnoreCase(TARGET_BLUNO_ADDRESS)
                || name.toLowerCase(Locale.US).contains("bluno")
                || name.toLowerCase(Locale.US).contains("dfrobot");
        String label = getString(R.string.bluetooth_device_label, name, address, source);
        if (likelyBluno) {
            label += "\n" + getString(R.string.likely_bluno_device);
            blunoCandidateAddresses.add(address);
        }

        int insertAt = likelyBluno ? 0 : discoveredDevices.size();
        discoveredDevices.add(insertAt, device);
        deviceLabels.add(insertAt, label);
        deviceListAdapter.notifyDataSetChanged();
        emptyDeviceText.setVisibility(View.GONE);
    }

    private void registerBluetoothDiscoveryReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // ACTION_FOUND is sent by Android's Bluetooth service (outside this
        // app), so the receiver must accept system broadcasts.
        ContextCompat.registerReceiver(
                this,
                bluetoothDiscoveryReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
        );
        bluetoothReceiverRegistered = true;
    }

    @SuppressWarnings("deprecation")
    private BluetoothDevice getBluetoothDeviceExtra(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice.class
            );
        }
        return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    }

    @SuppressLint("MissingPermission")
    private void addDiscoveredDevice(ScanResult result) {
        if (!hasRequiredPermissions() || result == null || result.getDevice() == null) {
            return;
        }

        BluetoothDevice device = result.getDevice();
        if (isPreviouslyPaired(device)) {
            return;
        }
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
        int signalStrength = result.getRssi();

        runOnUiThread(() -> {
            if (finalReportedName != null && !finalReportedName.trim().isEmpty()) {
                bestDeviceNames.put(address, finalReportedName.trim());
            } else if (!bestDeviceNames.containsKey(address)) {
                bestDeviceNames.put(address, getString(R.string.unnamed_ble_device));
            }
            if (finalIsBlunoCandidate) {
                blunoCandidateAddresses.add(address);
            }
            deviceSignalStrengths.put(address, signalStrength);

            boolean likelyBluno = blunoCandidateAddresses.contains(address);
            String label = getString(
                    R.string.ble_device_label_rssi,
                    bestDeviceNames.get(address),
                    address,
                    deviceSignalStrengths.get(address)
            );
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
            } else {
                int insertAt = likelyBluno ? 0 : deviceLabels.size();
                discoveredDevices.add(insertAt, device);
                deviceLabels.add(insertAt, label);
            }

            deviceListAdapter.notifyDataSetChanged();
            emptyDeviceText.setVisibility(View.GONE);
            if (likelyBluno) {
                showConnectionState(getString(R.string.bluno_found), false);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private boolean isPreviouslyPaired(BluetoothDevice device) {
        if (device == null || !hasRequiredPermissions()) {
            return false;
        }
        try {
            return device.getBondState() == BluetoothDevice.BOND_BONDED;
        } catch (SecurityException ignored) {
            return false;
        }
    }

    private void showWellnessSuggestion(String message) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.heart_rate_check_in_title)
                .setMessage(message + "\n\n" + getString(R.string.wellness_not_medical_advice))
                .setPositiveButton(R.string.dismiss, null)
                .show();
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        if (!hasRequiredPermissions()) {
            Toast.makeText(
                    this,
                    R.string.bluetooth_permission_needed,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        stopScan();
        disconnectButton.setEnabled(true);
        sensorManager.connect(
                device,
                TARGET_BLUNO_ADDRESS.equalsIgnoreCase(device.getAddress())
        );
    }

    private void displayReading(
            HeartRateReading rawReading,
            HeartRateReading stableReading
    ) {
        rawValueText.setText(
                getString(R.string.raw_value, rawReading.getRawValue())
        );
        signalRangeText.setText(
                getString(R.string.signal_range, rawReading.getSignalRange())
        );
        lastPacketText.setText(rawReading.toPacketString());

        if (!stableReading.hasGoodSignal()) {
            bpmText.setText(R.string.bpm_placeholder);
            setHeartRateColour(R.color.zone_text_secondary);
            signalStatusText.setText(R.string.no_signal_instructions);
            signalStatusText.setTextColor(
                    ContextCompat.getColor(this, R.color.zone_warning)
            );
        } else if (stableReading.getBpm() == 0) {
            bpmText.setText(R.string.bpm_placeholder);
            setHeartRateColour(R.color.zone_primary);
            signalStatusText.setText(R.string.calculating_bpm);
            signalStatusText.setTextColor(
                    ContextCompat.getColor(this, R.color.zone_primary)
            );
        } else {
            bpmText.setText(String.valueOf(stableReading.getBpm()));
            if (stableReading.isHeldReading()) {
                signalStatusText.setText(R.string.heart_rate_holding);
                setHeartRateColour(R.color.zone_caution);
            } else {
                showHeartRateRange(stableReading.getBpm());
            }
        }
    }

    private void showHeartRateRange(int bpm) {
        HeartRateRange.Level level = HeartRateRange.classify(bpm);
        if (level == HeartRateRange.Level.TYPICAL) {
            signalStatusText.setText(R.string.heart_rate_typical);
            setHeartRateColour(R.color.zone_success);
        } else if (level == HeartRateRange.Level.CAUTION) {
            signalStatusText.setText(R.string.heart_rate_caution);
            setHeartRateColour(R.color.zone_caution);
        } else {
            signalStatusText.setText(R.string.heart_rate_alert);
            setHeartRateColour(R.color.zone_alert);
        }
    }

    private void setHeartRateColour(int colourId) {
        int colour = ContextCompat.getColor(this, colourId);
        bpmText.setTextColor(colour);
        signalStatusText.setTextColor(colour);
        heartIcon.setColorFilter(colour);
    }

    private void showConnectionState(String message, boolean connected) {
        connectionStatusText.setText(message);
        int color = connected
                ? R.color.zone_success : R.color.zone_text_secondary;
        connectionStatusText.setTextColor(ContextCompat.getColor(this, color));
    }

    private void disconnectFromDevice() {
        sensorManager.disconnect();
    }

    @Override
    protected void onDestroy() {
        stopScan();
        mainHandler.removeCallbacksAndMessages(null);
        if (bluetoothReceiverRegistered) {
            unregisterReceiver(bluetoothDiscoveryReceiver);
            bluetoothReceiverRegistered = false;
        }
        // Do not disconnect here. The manager keeps Bluno connected while the
        // user starts a study session or opens Analytics.
        super.onDestroy();
    }
}
