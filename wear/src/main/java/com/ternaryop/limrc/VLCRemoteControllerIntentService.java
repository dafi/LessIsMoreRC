package com.ternaryop.limrc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

/**
 * Created by dave on 03/01/17.
 * The service used to send commands to VLC remote
 */

public class VLCRemoteControllerIntentService extends IntentService {
    public static final String PREF_HOST_PORT = "hostPort";
    public static final String DEFAULT_HOST_PORT = "192.168.0.2:8080";
    private static final String COMMAND = "command";

    public VLCRemoteControllerIntentService() {
        super("VLCRemoteControllerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String command = intent.getStringExtra(COMMAND);

        if (command != null) {
            try {
                execute(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void seek(Context context, int position) {
        final String encondedPosition = Uri.encode(position > 0 ? "+" + position : "" + position);
        final String command = "command=seek&val=" + encondedPosition;

        startService(context, command);
    }

    public static void pause(Context context) {
        final String command = "command=pl_pause";

        startService(context, command);
    }

    private static void startService(Context context, String command) {
        Intent intent = new Intent(context, VLCRemoteControllerIntentService.class);
        intent.putExtra(COMMAND, command);

        context.startService(intent);
    }

    private String execute(String command) throws IOException {
        HttpURLConnection http = null;
        try {
            http = (HttpURLConnection) getStatusUrl(command).openConnection();
            System.out.println("VLCRemoteControllerIntentService.execute sent command ");

            String username = ""; // always empty for VLC
            String password = "vlcremote";
            http.setConnectTimeout(2000);
            http.setUseCaches(false);
            http.setRequestProperty("Authorization", "basic " +
                    Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));
            http.connect();
            int responseCode = http.getResponseCode();
            String responseMessage = http.getResponseMessage();
            if (responseCode != HttpURLConnection.HTTP_OK) {
            }
            return http.getResponseMessage();
        } catch (Exception ex) {
            showError(ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
        return null;
    }

    private void showError(final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        text,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private URL getStatusUrl(String command) throws MalformedURLException {
//        URL url = new URL("http://10.0.3.2:8080/requests/status.xml?command=pl_pause"); // genymotion
//        URL url = new URL("http://10.0.2.2:8080/requests/status.xml?" + command); // emulator
//        URL url = new URL("http://192.168.0.2:8080/requests/status.xml?" + command);
//        return url;
        final String host = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PREF_HOST_PORT, DEFAULT_HOST_PORT);
        System.out.println("host = " + host + " url " + "http://" + host + "/requests/status.xml?" + command);
        return new URL("http://" + host + "/requests/status.xml?" + command);
    }
}
