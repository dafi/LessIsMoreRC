package com.ternaryop.limrc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity {
    private static final String MSG_HOST_PORT = "/hostPort";

    private GoogleApiClient apiClient;
    private EditText ipText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initGoogleApiClient();
    }

    private void initUI() {
        ipText = (EditText) findViewById(R.id.ip_address);
        findViewById(R.id.send_button).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = ipText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    sendMessage(MSG_HOST_PORT, text);
                    System.out.println("MainActivity.onClick " + text);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        apiClient.disconnect();
    }

    private void initGoogleApiClient() {
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        apiClient.connect();
    }

    private void sendMessage(final String path, final String text) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                System.out.println("MainActivity.run ");
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(apiClient).await();
                for(Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(apiClient, node.getId(), path, text.getBytes()).await();
                    System.out.println("MainActivity.run sent " + path + " " + text);
                }
            }
        }).start();
    }}
