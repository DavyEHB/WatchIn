package be.ehb.watchin.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.model.Event;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.services.AttendeeDAO.AttendeeRestService;
import be.ehb.watchin.services.AttendeeDAO.AttendeeResultReceiver;
import be.ehb.watchin.services.BeaconScannerService;
import be.ehb.watchin.services.ContactDAO.ContactRestService;
import be.ehb.watchin.services.ContactDAO.ContactResultReceiver;
import be.ehb.watchin.services.EventDAO.EventResultReceiver;
import be.ehb.watchin.services.PersonDAO.PersonRestService;
import be.ehb.watchin.services.PersonDAO.PersonResultReceiver;

public class TempLauncher extends Activity implements ContactResultReceiver.ReceiveContact, EventResultReceiver.ReceiveEvent, AttendeeResultReceiver.ReceiveAttendee {

    private static final String TAG = "TempLauncher";

    private TextView tv1;


    @Override
    protected void onResume() {
        super.onResume();
        tv1 = (TextView) findViewById(R.id.tv1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_launcher);
        tv1 = (TextView) findViewById(R.id.tv1);
     }

    public void onClickSwipe(View view)
    {
        Intent intent = new Intent(this, WatchInMain.class);
        startActivity(intent);
    }

    public void onClickLogin(View view)
    {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    public void onClickCheckEmail(View view)
    {
        Log.d(TAG,"Creating person");

        Person p = Person.makePerson()
                .ID(1)
                .firstName("Davy")
                .create();
        Log.d(TAG,p.toString());

    }

    public void onClickGetByID(View view)
    {
        Intent intent = new Intent(this,PersonDetailActivity.class);
        intent.putExtra(PersonDetailActivity.USER_ID,2);
        startActivity(intent);
    }



    /*
    @Override
    public void onReceiveSkill(Bundle skill) {
        Log.d(TAG,"Receiving skill");
        Log.d(TAG,skill.toString());
        int ID = skill.getInt(SkillRestService.BUN_ID);
        int PID = skill.getInt(SkillRestService.BUN_PID);
        String mSkill = skill.getString(SkillRestService.BUN_SKILL);
        Person p = ((WatchInApp) getApplication()).Persons().get(PID);
        p.Skills().add(mSkill);
        Log.d(TAG,p.toString());
        Log.d(TAG,mSkill);

    }
    */

    /*
    @Override
    public void onReceiveAllPersons(Bundle skills) {
        Log.d(TAG,"Receiving all skills");
        Log.d(TAG,skills.toString());
        ArrayList<Bundle> skillsArray = (ArrayList<Bundle>) skills.getSerializable(SkillRestService.BUN_SKILL);
        for (Bundle b: skillsArray)
        {
            Log.d(TAG,b.toString());
            onReceiveSkill(b);
        }
    }
*/

    @Override
    public void onReceiveContact(Bundle contact) {
        Log.d(TAG,"Receiving contact");
        Log.d(TAG,contact.toString());
        int ID = contact.getInt(ContactRestService.BUN_ID);
        int PID = contact.getInt(ContactRestService.BUN_PID);
        int CID = contact.getInt(ContactRestService.BUN_CID);
        /*Person p = ((WatchInApp) getApplication()).Persons().get(PID);
        p.Skills().add(mSkill);
        Log.d(TAG,p.toString());
        */
        Person p = ((WatchInApp) getApplication()).Persons.get(PID);
        Person c = ((WatchInApp) getApplication()).Persons.get(CID);
        p.Contacts().put(c.getID(),c);
    }

    @Override
    public void onReceiveEvent(Event event) {
        Log.d(TAG,"Receiving events in temp");
        Log.d(TAG,event.toString());
    }

    @Override
    public void onReceiveAllEvents(Map<Integer, Event> eventMap) {
        Log.d(TAG,"Receiving all events in temp");
        Log.d(TAG,eventMap.toString());
    }


    @Override
    public void onReceiveAttendee(Bundle attendee) {
        Log.d(TAG,"Receiving Attendee");
        Log.d(TAG,attendee.toString());
        int ID = attendee.getInt(AttendeeRestService.BUN_ID);
        int PID = attendee.getInt(AttendeeRestService.BUN_PID);
        int EID = attendee.getInt(AttendeeRestService.BUN_EID);

        Person p = ((WatchInApp) getApplication()).Persons.get(PID);
        Event e = ((WatchInApp) getApplication()).Events.get(EID);
        p.Events().put(e.getID(),e);

        Log.d(TAG,p.toString() +"Goes to: " +p.Events().toString());
        Log.d(TAG,e.toString() + "Has guests: "+ e.Attendees().toString());
    }

    @Override
    public void onError() {

    }


    public void onClickBtn2(View view) {
        ResultReceiver rec = new ResultReceiver(new Handler())
        {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                int RSSI = ((ScanResult) resultData.getParcelable("Device")).getRssi();
                Log.d(TAG, "onReceiveResult: " + resultCode);
                Log.d(TAG, "onReceiveResult: " + RSSI);
                TextView tv1 = (TextView) findViewById(R.id.tv1);
                tv1.setText(String.valueOf(RSSI));
            }
        };
        BeaconScannerService.startScanning(this,rec);
    }

    public void onClickBtn1(View view) {
        BeaconScannerService.stopScanning(this);
    }


    public void onClickBtn3(View view) {
        Log.d(TAG, "btn3 Clicked");





    }


    /*DAO Call to log in to venue
    AttendeeResultReceiver attendeeResultReceiver = new AttendeeResultReceiver(new AttendeeResultReceiver.ReceiveAttendee() {
        @Override
        public void onReceiveAttendee(Bundle attendee) {
            Log.d(TAG, "onReceiveAttendee: ");
        }

        @Override
        public void onError() {

        }
    });
    AttendeeRestService.startActionUpdate(this,4,1,false,attendeeResultReceiver);
    */
}
