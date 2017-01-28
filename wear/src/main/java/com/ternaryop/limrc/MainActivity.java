package com.ternaryop.limrc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import static com.ternaryop.limrc.VLCRemoteControllerIntentService.DEFAULT_HOST_PORT;
import static com.ternaryop.limrc.VLCRemoteControllerIntentService.PREF_HOST_PORT;

public class MainActivity extends WearableActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {
    private static final String MSG_HOST_PORT = "/hostPort";

    private BoxInsetLayout mContainerView;
    private GoogleApiClient apiClient;
    private TextView ipValue;
    private OnClickListener onClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.cmd_pause:
                    VLCRemoteControllerIntentService.pause(MainActivity.this);
                    break;
                case R.id.cmd_forward:
                    VLCRemoteControllerIntentService.seek(MainActivity.this, 10);
                    break;
                case R.id.cmd_rewind:
                    VLCRemoteControllerIntentService.seek(MainActivity.this, -10);
                    break;
            }
        }
    };
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setAmbientEnabled();


        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        initUI();
        initGoogleApiClient();
    }

    private void initUI() {
        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        ipValue = (TextView) findViewById(R.id.ip_value);
        ipValue.setText(preferences.getString(PREF_HOST_PORT, DEFAULT_HOST_PORT));

        findViewById(R.id.cmd_pause).setOnClickListener(onClick);
        findViewById(R.id.cmd_forward).setOnClickListener(onClick);
        findViewById(R.id.cmd_rewind).setOnClickListener(onClick);
    }

    private void initGoogleApiClient() {
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        if (!(apiClient.isConnected() || apiClient.isConnecting()))
            apiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiClient != null && !( apiClient.isConnected() || apiClient.isConnecting()))
            apiClient.connect();
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(MSG_HOST_PORT)) {
                    final String ipAddr = new String(messageEvent.getData());
                    ipValue.setText(ipAddr);
                    preferences
                            .edit()
                            .putString(PREF_HOST_PORT, ipAddr.trim())
                            .apply();

                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(apiClient, this);
    }

    @Override
    protected void onStop() {
        if (apiClient != null) {
            Wearable.MessageApi.removeListener(apiClient, this);
            if (apiClient.isConnected()) {
                apiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (apiClient != null)
            apiClient.unregisterConnectionCallbacks(this);
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
}
