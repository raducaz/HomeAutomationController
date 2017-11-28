package com.gmail.raducaz.arduinomate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Radu.Cazacu on 11/26/2017.
 */

public class ConfigurationGridAdapter extends BaseAdapter {

    List<String> list;
    Context context;

    public ConfigurationGridAdapter(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.configuration_grid_item, parent, false);
        }
        TextView textView = view.findViewById(R.id.configurationGridItemTextView);
        LinearLayout linearLayout = view.findViewById(R.id.configurationGridItemLinearLayout);
        textView.setText(list.get(position));
        if (list.size() - 1 == position) {
            linearLayout.addView(addButton());
        }
        return view;
    }

    private Button addButton() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Button button = new Button(context);
        button.setLayoutParams(params);
        button.setText("Testing");
        return button;
    }

}
