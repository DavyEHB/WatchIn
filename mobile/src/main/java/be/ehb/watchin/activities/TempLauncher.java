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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.services.ContactDAO.ContactRestService;
import be.ehb.watchin.services.ContactDAO.ContactResultReceiver;
import be.ehb.watchin.services.PersonDAO.PersonRestService;
import be.ehb.watchin.services.PersonDAO.PersonResultReceiver;
import be.ehb.watchin.services.SkillDAO.SkillRestService;
import be.ehb.watchin.services.SkillDAO.SkillResultReceiver;

public class TempLauncher extends AppCompatActivity implements ContactResultReceiver.ReceiveContact{

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
        /*
        Log.d(TAG,"loading skill");
        SkillResultReceiver skillResultReceiver = new SkillResultReceiver();
        skillResultReceiver.setReceiver(this);
        SkillRestService.startActionGetAll(this,skillResultReceiver);
        */

        /*
        Log.d(TAG,"Loading contacts");
        ContactResultReceiver contactResultReceiver = new ContactResultReceiver();
        contactResultReceiver.setReceiver(this);
        ContactRestService.startActionGetAll(this,contactResultReceiver);
        */

        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            date = sdf.parse("2016-05-19T00:00:00+02:00");

        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG,date.toString());
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
    public void onReceiveAll(Bundle skills) {
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
        Person p = ((WatchInApp) getApplication()).Persons().get(PID);
        Person c = ((WatchInApp) getApplication()).Persons().get(CID);
        p.Contacts().add(c);
    }

    @Override
    public void onError() {

    }

    public void onClickBtn2(View view) {

        Log.d(TAG,"===BTN 2 Click ===");

        Map<Integer,Person> personList =((WatchInApp) getApplication()).Persons();
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
