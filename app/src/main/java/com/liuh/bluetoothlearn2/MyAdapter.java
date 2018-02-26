package com.liuh.bluetoothlearn2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Date: 2018/2/25 13:44
 * Description:
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context mContext;
    private List<BluetoothDevice> devices;

    public void setDevices(List<BluetoothDevice> devices) {
        this.devices = devices;
    }

    public MyAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout_btdevice, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        if (device != null) {
            holder.itemBtdeviceName.setText(device.getName());
            holder.itemBtdeviceAddress.setText(device.getAddress());
        }
    }

    @Override
    public int getItemCount() {
        if (devices != null && devices.size() > 0) {
            return devices.size();
        } else {
            return 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_btdevice_img)
        ImageView itemBtdeviceImg;
        @BindView(R.id.item_btdevice_name)
        TextView itemBtdeviceName;
        @BindView(R.id.item_btdevice_address)
        TextView itemBtdeviceAddress;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
