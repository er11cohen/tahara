package com.eran.tahara;

import android.content.Context;

import com.eran.utils.Utils;

import java.util.ArrayList;

public class UtilTahara {

    public static ArrayList<Halach> getHalachArray(Context context) {
        String text = Utils.ReadTxtFile("files/tahara.csv", context);
        ArrayList<Halach> alHalach = new ArrayList<Halach>();
        try {
            String[] items = text.split("\\r");
            for (int i = 0; i < items.length; i++) {
                String[] halachStr = items[i].split(",");
                Halach halach = new Halach(Integer.parseInt(halachStr[0].trim()), halachStr[1].trim(),
                        halachStr[2].trim(), Integer.parseInt(halachStr[3].trim()));
                alHalach.add(halach);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return alHalach;
    }

}
