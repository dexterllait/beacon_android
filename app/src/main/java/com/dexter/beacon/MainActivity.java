package com.dexter.beacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private BeaconManager beaconManager;

    private List<BeaconModel> mBeacons;
    private Map<String, BeaconModel> beaconModelMap;
    private CustomBeaconAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVariable();
        initUI();

        checkBluetoothStatus();
    }

    private void initVariable() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBeacons = new ArrayList<>();
        beaconModelMap = new HashMap<>();
        mAdapter = new CustomBeaconAdapter(this, mBeacons);
    }

    private void initUI() {
        ListView beaconList = (ListView) findViewById(R.id.beacon_list);
        beaconList.setAdapter(mAdapter);
    }

    private void checkBluetoothStatus() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this, "Turning bluetooth...", Toast.LENGTH_SHORT).show();
                    mBluetoothAdapter.enable();
                }
                checkLocationPermission();
            }
        }, 1000);
    }

    private void checkLocationPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int rc = ActivityCompat.checkSelfPermission(this, permission);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int numOfRequest = grantResults.length;
        boolean isGranted = numOfRequest == 1 && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
        String permission;
        if (requestCode == 1000) {
            permission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (isGranted) {
                onPermissionGranted();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    checkLocationPermission();
                } else {
                    showOpenPermissionSetting();
                }
            }
        }
    }

    void showOpenPermissionSetting() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Location Permission Required")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1001);
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            checkLocationPermission();
        }
    }


    private void onPermissionGranted() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.e(TAG, "Scan Beacon: " + beacons.size());
                    for (Beacon beacon : beacons) {
                        BeaconModel model = new BeaconModel();
                        model.setBleAddress(beacon.getBluetoothAddress());
                        model.setNamespaceID(beacon.getId1().toString());
                        model.setInstanceIDs(beacon.getId2().toString());
                        model.setDistance(beacon.getDistance());
                        beaconModelMap.put(model.getBleAddress(), model);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBeacons.clear();
                            mBeacons.addAll(beaconModelMap.values());
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.dexter.beacon", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
