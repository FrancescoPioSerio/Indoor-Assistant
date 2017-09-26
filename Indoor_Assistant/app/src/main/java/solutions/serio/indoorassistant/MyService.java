package solutions.serio.indoorassistant;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by utente on 28/04/2017.
 */

public class MyService extends Service {

    private SharedPreferences prefsPrivate;

    BluetoothAdapter bluetoothAdapter;
    BroadcastReceiver mBroadcastReceiver2;

    public MyService() {

    }

    String page;
    String note;
    String msg;
    Intent broswserIntent;
    Intent waIntent;
    SharedPreferences prefsDevice;
    String device1;

    NotificationManager notificationManager;
    NotificationCompat.Builder n;
    NotificationCompat.Builder other;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {

        // Visualizzo un Toast su schermo per avvisare l'utente dell'avvenuta
        // creazione del servizio
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        msg = pref.getString("msg_id", "default_id");
        //Log.d("Messaggio sul service",msg);
        page = pref.getString("page_id", "default_id");
        //Log.d("Pagina web sul service",page);
        note = pref.getString("note_id", "default_id");
        //Log.d("Nota sul service:",note);

        broswserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(page));
        broswserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if(note != null && !(note.equals("default_id"))){
            n  = new NotificationCompat.Builder(this)
                    .setContentTitle("BeaconApp")
                    .setContentText(note)
                    .setSmallIcon(R.drawable.icona)
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setSound(uri);
        }

        other = new NotificationCompat.Builder(this)
                .setSound(uri);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        prefsDevice = PreferenceManager.getDefaultSharedPreferences(this);
        //device1 = prefsDevice.getString("device_id", "default_id");
        final Set<String> set = prefsDevice.getStringSet("device_id", null);
        // Visualizzo un Toast su schermo per avvisare l'utente dell'avvenuta
        // inizializzazione del servizio.
        if(set == null) {
            Toast.makeText(this, "There are no settings", Toast.LENGTH_SHORT).show();
            Log.d("Device sul service", "nullo");
        }
        final ArrayList<String> devices = new ArrayList<>();
        final Boolean[] temp = new Boolean[1];
        temp[0] = false;
        //temp[1] = false;
        bluetoothAdapter.startDiscovery();
        mBroadcastReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    temp[0] = true;
                    if (devices.size() == 0) {
                        devices.add(device.getAddress());
                        if (set.containsAll(devices)) {
                            Log.d("NOTIFICA", "TROVATO");
                            if (n != null) {
                                notificationManager.notify(0, n.build());
                            }
                            if (page != null && !(page.equals("default_id"))) {
                                Log.d("Mostro la", "pagina web");
                                notificationManager.notify(0, other.build());
                                startActivity(broswserIntent);
                            }
                            if (msg != null && !(msg.equals("default_id"))) {
                                Log.d("Whatsapp", "Messaggio");
                                notificationManager.notify(0, other.build());
                                sendWhatsappMsg();
                            }
                        }
                    }
                }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                    if((devices.size() != 0) && (!temp[0])){
                        Log.d("NOTIFICA", "");

                        if(n != null){
                            notificationManager.notify(0, n.build());
                        }
                        if (page != null && !(page.equals("default_id"))){
                            Log.d("Mostro la","pagina web");
                            notificationManager.notify(0, other.build());
                            startActivity(broswserIntent);
                        }
                        if(msg != null && !(msg.equals("default_id"))){
                            Log.d("Whatsapp","Messaggio");
                            notificationManager.notify(0, other.build());
                            sendWhatsappMsg();
                        }

                        devices.clear();
                        //temp[0] = true;
                        bluetoothAdapter.startDiscovery();

                    }

                    bluetoothAdapter.startDiscovery();
                    Log.d("Rincomincio","Rincomincio");
                    temp[0] = false;

                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(bluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(bluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mBroadcastReceiver2, filter);
        return START_STICKY;
    }

    public void startForeground() {
        startForeground(17, null); // Because it can't be zero...
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(this, "Service Interrotto", Toast.LENGTH_LONG).show();
        Log.d("STATO","Service distrutto");
        unregisterReceiver(mBroadcastReceiver2);

    }

    public void sendWhatsappMsg (){

        PackageManager pm = getPackageManager();

        try {
            waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            waIntent.setType("text/plain");
            String text = msg;

            PackageInfo info= pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);

            Intent new_intent = Intent.createChooser(waIntent, "Share via");
            new_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(new_intent);

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
