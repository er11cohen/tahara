package com.eran.tahara;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    ArrayList<Halach> alHalach;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, ArrayList<Halach> alHalach) {
        this.alHalach = alHalach;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return alHalach.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.listview_content, null);
        TextView tv = view.findViewById(R.id.textView);
        tv.setText(alHalach.get(i).toString());

        // Halach and Agada gate
        if (alHalach.get(i).getHalachIndex() == 0 || alHalach.get(i).getHalachIndex() == 11) {
            tv.setBackgroundColor(Color.parseColor("#C1E7F6"));
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(20);
        }
        return view;
    }
}