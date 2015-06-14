package com.etnclp.samsun;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.etnclp.samsun.R;

import java.util.ArrayList;

public class RouteDialog extends Dialog {

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    public RouteDialog(final Context context, final ArrayList<String> list, final OnItemSelectedListener listener) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.route_dialog);

        TextView ok = (TextView) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();

                if (listener != null)
                    listener.onItemSelected(position);
            }
        });

        ListRouteAdapter adapter = new ListRouteAdapter(context, list);
        listView.setAdapter(adapter);
    }
}
