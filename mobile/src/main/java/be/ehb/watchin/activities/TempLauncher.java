package be.ehb.watchin.activities;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.model.Event;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.model.dummy.DummyEventList;
import be.ehb.watchin.services.ContactDAO.ContactRestService;
import be.ehb.watchin.services.ContactDAO.ContactResultReceiver;
import be.ehb.watchin.services.EventDAO.EventRestService;
import be.ehb.watchin.services.EventDAO.EventResultReceiver;
import be.ehb.watchin.services.PersonDAO.PersonRestService;
import be.ehb.watchin.services.PersonDAO.PersonResultReceiver;
import be.ehb.watchin.services.SkillDAO.SkillRestService;
import be.ehb.watchin.services.SkillDAO.SkillResultReceiver;

public class TempLauncher extends AppCompatActivity implements ContactResultReceiver.ReceiveContact, EventResultReceiver.ReceiveEvent {

    private static final String TAG = "TempLauncher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_launcher);
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

        Map<Integer,Event> integerEventMap = ((WatchInApp)getApplication()).Events;
        Log.d(TAG,"Global Map: " +integerEventMap.toString());
        Log.d("SYSTEM_ID", "Persons: " + String.valueOf(System.identityHashCode(((WatchInApp) getApplication()).Persons)));
        Log.d("SYSTEM_ID", "Events: "+ String.valueOf(System.identityHashCode(((WatchInApp) getApplication()).Events)));

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
        p.Contacts().add(c);
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
    public void onError() {

    }

    public void onClickBtn2(View view) {

        Log.d(TAG,"===BTN 2 Click ===");

        Map<Integer,Person> personList =((WatchInApp) getApplication()).Persons;
        List<Person> mutual = null;
        Person me = ((WatchInApp) getApplication()).Me();

        Log.d(TAG,me.toString());

        Log.d(TAG,"--== friend list ==--");

        for(Person c: me.Contacts())
        {
            mutual = c.findMutualContacts(me);
            Log.d(TAG,c.toString() + " #mutual: " + mutual.size());
        }

        Log.d(TAG,"--== No friend list ==--");

        for (Person p : personList.values()) {
            mutual = p.findMutualContacts(me);
            Log.d(TAG, p.toString() + " #mutual: " + mutual.size());
        }
    }
}
