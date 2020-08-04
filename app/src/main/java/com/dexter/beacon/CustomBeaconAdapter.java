package com.dexter.beacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class CustomBeaconAdapter extends BaseAdapter {
    List<BeaconModel> list;
    Context context;

    public CustomBeaconAdapter(Context context, List<BeaconModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_beacon, null);

        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        TextView tvInstance = (TextView) view.findViewById(R.id.tv_instance);
        TextView tvDist = (TextView) view.findViewById(R.id.tv_dist);


        tvName.setText(list.get(position).getNamespaceID());
        tvInstance.setText(list.get(position).getInstanceIDs());
        tvDist.setText(String.valueOf(list.get(position).getDistance()));

        return view;
    }
}
