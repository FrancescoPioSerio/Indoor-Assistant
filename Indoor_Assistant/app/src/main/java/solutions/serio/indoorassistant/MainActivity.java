package solutions.serio.indoorassistant;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.support.design.widget.Snackbar;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;


    /**
     * Called when the activity is first created.
     */

    //Context context;
    BluetoothAdapter bluetoothAdapter;
    Button noteButton;
    Button deviceButton;
    Button pageButton;
    Button msgButton;
    String note;
    String page;
    String msg;
    String device;
    Switch mySwitch;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    MyService servicePrefs;
    Set<String> devicesBeacon;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicesBeacon = new HashSet<>();

        final ArrayList<String> bluetoothdev = new ArrayList<>();

        //inizializzo le mie shared preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit(); //istanzio l'editor per modificare le mie preferenze

        servicePrefs = new MyService(); //istanzio il mio service

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //bluetoothadapter per l'intent di attivazione bluetooth

        mySwitch = (Switch) findViewById(R.id.switchButton);

        //dichiaro le preferenze con la propria chiave del memo, pagina web, messaggio e anche del device bluetooth (beacon)
        prefs = getSharedPreferences("preftemp", MODE_PRIVATE);
        final String note_pref = (prefs.getString("note_id",""));
        final String page_pref = (prefs.getString("page_id",""));
        final String msg_pref = (prefs.getString("msg_id",""));
        final String device_pref = (prefs.getString("device_id",""));

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(((note == null) && (page == null) && (msg == null) || (device == null ))){
                        Snackbar.make(mySwitch, "Insert more settings", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        mySwitch.setChecked(false); //setto lo switch a false nel caso in cui non ci siano informazioni
                    }else{ //nel caso in cui siano state inserite delle informazioni parte il service abilitando lo switch
                        Log.d("memo", note.toString());
                        Log.d("device", device.toString());
                        startService(mySwitch);
                    }
                }else{
                    stopService(mySwitch);
                }
            }
        });

        noteButton = (Button) findViewById(R.id.noteButton);
        deviceButton = (Button) findViewById(R.id.deviceButton);
        pageButton = (Button) findViewById(R.id.pageButton);
        msgButton = (Button) findViewById(R.id.msgButton);

        CheckBlueToothState(); // nell'onCreate eseguo il metodo per verificare l'attivazione del bluetooth sullo smartphone

        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Memo");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        note = input.getText().toString(); //Stringa di input dall'EditText
                        Log.d("Promemoria",note);

                        editor.putString("note_id", note); //inserimento coppia chiave/valore
                        editor.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        deviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Beacon");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        device = input.getText().toString();
                        bluetoothdev.add(device);
                        Log.d("Lista:",bluetoothdev.toString());

                        devicesBeacon.addAll(bluetoothdev);

                        editor.putStringSet("device_id", devicesBeacon); //InputString: from the EditText
                        editor.commit();

                        Snackbar.make(deviceButton, "Beacon Saved", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        pageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Web Page: http://www...");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        page = input.getText().toString();
                        Log.d("Pagina",page);
                        if (!page.startsWith("http://") && !page.startsWith("https://")) {
                            Snackbar.make(pageButton, "Wrong URL typing", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }else {

                            editor.putString("page_id", page); //InputString: from the EditText
                            editor.commit();

                            Snackbar.make(pageButton, "Web Page Saved", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        msgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("WhatsApp");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        msg = input.getText().toString();
                        Log.d("Messaggio",msg);

                        editor.putString("msg_id", msg); //InputString: from the EditText
                        editor.commit();

                        Snackbar.make(msgButton, "Message Saved", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    // Start the  service
    public void startService(View view) {
        Snackbar.make(view, "Service Started", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        startService(new Intent(this, MyService.class));

    }

    // Stop the  service
    public void stopService(View view) {
        Snackbar.make(view, "Service Interrupted", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        stopService(new Intent(this, MyService.class));
    }

    public void CheckBlueToothState() {

        if (!(bluetoothAdapter.isEnabled())) { //se non è abilitato

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE_BT) {
            CheckBlueToothState();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        editor.remove("note_id").commit();
        editor.remove("page_id").commit();
        editor.remove("device_id").commit();
        editor.remove("msg_id").commit();

        Snackbar.make(noteButton, "Preferences Deleted", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        return false;
    }
}