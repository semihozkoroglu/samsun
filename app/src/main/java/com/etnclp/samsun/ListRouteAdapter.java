package com.etnclp.samsun;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.etnclp.samsun.R;

import java.util.ArrayList;

public class ListRouteAdapter extends BaseAdapter {

    private ArrayList<String> mList;
    private LayoutInflater inflater;

    public ListRouteAdapter(Context context, ArrayList<String> list) {
        mList = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_route_row, null);

        TextView routeText = (TextView) convertView.findViewById(R.id.routeText);
        TextView routeCount = (TextView) convertView.findViewById(R.id.routeCount);

        routeText.setText(getItem(position));
        routeCount.setText(position + "");

        return convertView;
    }
}
