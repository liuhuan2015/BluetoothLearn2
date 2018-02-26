package com.liuh.bluetoothlearn2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Android 4.3(Api 18)开始引入Bluetooth Low Energy(BLE,低功耗蓝牙)的核心功能并提供了相应的api.
 * 应用程序通过这些api可以:扫描蓝牙设备,查询services,读写设备的characteristics(属性特征)等操作.
 * <p>
 * Android BLE使用的蓝牙协议是GATT协议.
 * <p>
 * 一些专业名词解释:
 * Service:一个低功耗蓝牙设备可以定义许多Service,Service可以理解为一个功能的集合.
 * 设备中每一个不同的Service都有一个128bit的UUID作为这个Service的独立标识.
 * Characteristic:在Service下面,又包括了许多的数据独立项,我们把这些独立的数据项称作Characteristic.
 * 同样的,每一个Characteristic也有一个唯一的UUID作为标识符.在Android开发中,建立蓝牙连接后,我们说的通过蓝牙发送数据
 * 给外围设备就是往这些Characteristic中的Value字段写入数据
 * <p>
 * --------------------------------分割线---------------------------------------
 * BluetoothAdapter的startDiscovery()在大多数手机上是可以同时发现经典蓝牙和Ble的,但是startDiscovery的回调无法返回Ble的广播,
 * 所以无法通过广播识别设备,而且startDiscovery扫描Ble的效率比startLeScan低很多.所以在实际应用中,还是startDiscovery和startLeScan
 * 分开扫,前者扫描传统蓝牙,后者扫描低功耗蓝牙.
 * 如果Ble设备比较多,可以先扫Ble 10s,再扫传统蓝牙 10s.
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.rv_bluetooth_device)
    RecyclerView rvBluetoothDevice;

    private static final String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 100;

    private List<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<BluetoothDevice>();

    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000L;//扫描时间

    public PermissionListener mPermissionListener;
    private AlertDialog.Builder builder;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        myAdapter = new MyAdapter(this);
        myAdapter.setDevices(bluetoothDeviceArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvBluetoothDevice.setLayoutManager(linearLayoutManager);
        rvBluetoothDevice.setAdapter(myAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        //如果检测到蓝牙没有开启,则开启蓝牙
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //
            requestRuntimePermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, new MyRequestPermission());
        } else {
//            mBluetoothAdapter.startDiscovery();
            scanLeDevice(true);
        }


    }

    /**
     * @param stopDelay 是否延时结束
     */
    private void scanLeDevice(boolean stopDelay) {
        if (stopDelay) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(callback);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(callback);
        } else {
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.stopLeScan(callback);
        }
    }


    BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //扫描会扫描出重复设备,在这里进行一下过滤
            if (!bluetoothDeviceArrayList.contains(device)) {
                Log.e("**********", "bluetooth device name: " + device.getName() + ",address: " + device.getAddress());
                bluetoothDeviceArrayList.add(device);
                myAdapter.notifyDataSetChanged();

            }
        }
    };

    public void requestRuntimePermission(String[] permissions, PermissionListener permissionListener) {

        mPermissionListener = permissionListener;
        List<String> permissionList = new ArrayList<>();


        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {
            permissionListener.onGranted();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    List<String> deniedPermission = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        String permission = permissions[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            deniedPermission.add(permission);
                        }
                    }

                    if (deniedPermission.isEmpty()) {
                        mPermissionListener.onGranted();
                    } else {
                        mPermissionListener.onDenied(deniedPermission);
                    }
                }
                break;

        }
    }

    class MyRequestPermission implements PermissionListener {

        @Override
        public void onGranted() {
            Log.e(TAG, "获取到了获取位置信息的权限");
//            mBluetoothAdapter.startDiscovery();
            scanLeDevice(true);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onDenied(List<String> deniedPermissions) {
            for (String str : deniedPermissions) {
                Log.e(TAG, "-----deniedPermission : " + str);
            }

            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                //如果用户勾选了不再提醒,shouldShowRequestPermissionRationale(...)会返回false
                //但国内定制的ROM，比如小米是永久返回false的。
                //下次进来的时候在这里做一些处理,一般是引导用户到设置里面去设置
                builder = new AlertDialog.Builder(MainActivity.this);


                builder.setMessage("应用需要获取用户位置信息,是否前往设置?");

                builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //这段跳转到应用权限设置界面的代码在不同的手机上需要进行适配(小米5X是可以的)
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        MainActivity.this.startActivity(intent);
                    }
                });

                builder.setNegativeButton("不了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        UIUtils.showToast("应用需要获取地理位置信息,否则定位功能可能无法正常使用");
                    }
                });
                builder.setCancelable(false);

                builder.show();
            } else {
//                UIUtils.showToast("应用需要获取地理位置信息,否则定位功能可能无法正常使用");
            }


        }
    }


}
