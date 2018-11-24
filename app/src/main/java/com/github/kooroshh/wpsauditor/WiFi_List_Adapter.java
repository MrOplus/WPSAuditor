package com.github.kooroshh.wpsauditor;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Oplus on 28/02/2016.
 */
public class WiFi_List_Adapter extends BaseAdapter {
    private Context context ;
    private List<ScanResult> networks ;
    private LayoutInflater inflater;
    SQLiteClient sqlClient;
    public WiFi_List_Adapter(Context context, List<ScanResult> networks){
        this.networks = networks;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sqlClient = new SQLiteClient(context);
    }
    @Override
    public int getCount() {
        return networks.size();
    }

    @Override
    public Object getItem(int position) {
        return networks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null){
            v = inflater.inflate(R.layout.list_row,null);
        }
        ImageView HasSecurity = (ImageView) v.findViewById(R.id.img_security_state);
        ImageView SignalPower = (ImageView) v.findViewById(R.id.img_icon);
        TextView SSID = (TextView) v.findViewById(R.id.txt_SSID);
        TextView MAC = (TextView) v.findViewById(R.id.txt_bssid);
        TextView Vendor = (TextView) v.findViewById(R.id.txt_Vendor);
        ScanResult r = networks.get(position);
        SSID.setText(r.SSID);
        MAC.setText(r.BSSID.toUpperCase());
        Vendor.setText(sqlClient.Mac2Vendor(getMacFirst6Bytes(r.BSSID)));
        if (r.capabilities.toLowerCase().contains("wps")){
            HasSecurity.setImageResource(R.mipmap.ic_firewall_off);
        }else{
            HasSecurity.setImageResource(R.mipmap.ic_firewall_on);
        }
        SignalPower.setImageResource(getIconDrawable(r.level));
        return v;
    }
    private int getIconDrawable(int level){
        if (level >= -50){
            return R.mipmap.ic_wifi_4_4;
        }else if (level < -50 && level >= -75){
            return R.mipmap.ic_wifi_3_4;
        }else if (level < -75 && level >=90){
            return R.mipmap.ic_wifi_2_4 ;
        }else{
            return R.mipmap.ic_wifi_1_4;
        }
    }
    private String getMacFirst6Bytes(String bssid){
        return bssid.substring(0,8);
    }
}
