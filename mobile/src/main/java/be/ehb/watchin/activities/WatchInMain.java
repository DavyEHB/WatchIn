package be.ehb.watchin.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.fragments.EventFragment.EventListFragment;
import be.ehb.watchin.fragments.FragmentTemplate;
import be.ehb.watchin.fragments.PersonalDetail;
import be.ehb.watchin.fragments.PersonsFragment.PersonListFragment;
import be.ehb.watchin.model.Event;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.services.AttendeeDAO.AttendeeRestService;
import be.ehb.watchin.services.AttendeeDAO.AttendeeResultReceiver;
import be.ehb.watchin.services.ContactDAO.ContactRestService;
import be.ehb.watchin.services.ContactDAO.ContactResultReceiver;
import be.ehb.watchin.services.EventDAO.EventRestService;
import be.ehb.watchin.services.EventDAO.EventResultReceiver;
import be.ehb.watchin.services.PersonDAO.PersonRestService;
import be.ehb.watchin.services.PersonDAO.PersonResultReceiver;
import be.ehb.watchin.services.SkillDAO.SkillRestService;
import be.ehb.watchin.services.SkillDAO.SkillResultReceiver;

public class WatchInMain extends AppCompatActivity implements PersonListFragment.OnListFragmentInteractionListener, PersonResultReceiver.ReceivePerson,
        SkillResultReceiver.ReceiveSkill,ContactResultReceiver.ReceiveContact, EventResultReceiver.ReceiveEvent, AttendeeResultReceiver.ReceiveAttendee {

    private static final String TAG = "WatchinMain";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ProgressDialog progress;

    private FragmentTemplate personList = PersonListFragment.newInstance();
    private FragmentTemplate eventList = EventListFragment.newInstance();
    private FragmentTemplate personalDetail = PersonalDetail.newInstance("Me");

    public static final String PREFS_NAME = "WatchInPrefs";
    public static final String PREFS_ID = "UserID";
    public static final String PREFS_EMAIL = "Email";
    public static final String PREFS_LOGIN = "Login";



    //private EventList mEvents = new EventList();


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int progressCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int userID = settings.getInt(WatchInMain.PREFS_ID,0);
        String email = settings.getString(WatchInMain.PREFS_EMAIL,"");
        boolean isLoggedIn = settings.getBoolean(WatchInMain.PREFS_LOGIN,false);

        if ((userID != 0)&&(isLoggedIn)){
            ((WatchInApp) getApplication()).MyID(userID);
            ((WatchInApp) getApplication()).MyEmail(email);
            ((WatchInApp) getApplication()).Login();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_in_main);

        progress = new ProgressDialog(this);

        if (!progress.isShowing()) {
            progress.setTitle("Loading data");
            progress.setMessage("Please Wait..");
            progress.show();
        }

        /*
        if (((WatchInApp) getApplication()).Me()==null)
        {
            getMeFromServer();
        }
        */

        getPersons();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    protected void onLogout(){
        ((WatchInApp)getApplication()).Logout();

        SharedPreferences settings = getSharedPreferences(WatchInMain.PREFS_NAME, 0);
        SharedPreferences.Editor edit = settings.edit();

        edit.putInt(WatchInMain.PREFS_ID,0);
        edit.putBoolean(WatchInMain.PREFS_LOGIN,false);

        edit.commit();

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onReceive(Person person) {
        /*
        progressCount--;
        ((WatchInApp) getApplication()).Me(person);
        if (progressCount == 0) {
            progress.dismiss();
        }
        */
    }

    public void onReceiveAllPersons(Map<Integer,Person> persons) {
        progressCount--;

        ((WatchInApp) getApplication()).Persons.clear();
        ((WatchInApp) getApplication()).Persons.putAll(persons);

        ((PersonListFragment) personList).notifyDataSetChanged();

        getEvents();
        getSkills();
        getContacts();


        if (progressCount == 0) {
            progress.dismiss();
        }
    }

    @Override
    public void onReceive(int PID,String skill) {
        progressCount--;
        Person p = ((WatchInApp) getApplication()).Persons.get(PID);
        p.Skills().add(skill);

        ((PersonListFragment) personList).notifyDataSetChanged();

        if (progressCount == 0) {
            progress.dismiss();
        }
    }

    @Override
    public void onReceiveContact(Bundle contact) {
        progressCount--;
        int ID = contact.getInt(ContactRestService.BUN_ID);
        int PID = contact.getInt(ContactRestService.BUN_PID);
        int CID = contact.getInt(ContactRestService.BUN_CID);

        Person p = ((WatchInApp) getApplication()).Persons.get(PID);
        Person c = ((WatchInApp) getApplication()).Persons.get(CID);
        p.Contacts().put(c.getID(),c);

        ((PersonListFragment) personList).notifyDataSetChanged();

        if (progressCount == 0) {
            progress.dismiss();
        }
    }

    @Override
    public void onReceiveEvent(Event event) {

    }

    public void onReceiveAllEvents(Map<Integer, Event> eventMap) {
        progressCount--;

        ((WatchInApp) getApplication()).Events.clear();
        ((WatchInApp) getApplication()).Events.putAll(eventMap);

        ((EventListFragment) eventList).notifyDataSetChanged();

        Map<Integer,Event> integerEventMap = ((WatchInApp)getApplication()).Events;

        getAttendees();

        if (progressCount == 0) {
            progress.dismiss();
        }
    }


    @Override
    public void onReceiveAttendee(Bundle attendee) {
      //  Log.d(TAG,"Receiving Attendee");
      //  Log.d(TAG,attendee.toString());

        progressCount--;

        int ID = attendee.getInt(AttendeeRestService.BUN_ID);
        int PID = attendee.getInt(AttendeeRestService.BUN_PID);
        int EID = attendee.getInt(AttendeeRestService.BUN_EID);

        Person p = ((WatchInApp) getApplication()).Persons.get(PID);
        Event e = ((WatchInApp) getApplication()).Events.get(EID);
        p.Events().put(e.getID(),e);
        e.Attendees().put(p.getID(),p);

        ((PersonListFragment) personList).notifyDataSetChanged();

        ((PersonalDetail) personalDetail).notifyDataChange(((WatchInApp) getApplication()).Me());

        if (progressCount == 0) {
            progress.dismiss();
        }
    }

    @Override
    public void onError() {
        progressCount--;
        Toast toast = Toast.makeText(this,"Could not load data.\nCheck connection",Toast.LENGTH_SHORT);
        toast.show();
        if (progressCount == 0) {
            progress.dismiss();
        }
    }

    private void getEvents()
    {
        Log.d(TAG,"Loading Events");
        EventResultReceiver eventResultReceiver = new EventResultReceiver(this);
        EventRestService.startActionGetAll(this,eventResultReceiver);
        progressCount++;
    }

    private void getPersons()
    {
        Log.d(TAG,"Loading Persons");
        PersonResultReceiver personResultReceiver = new PersonResultReceiver(this);
        PersonRestService.startActionGetAll(this,personResultReceiver);
        progressCount++;
    }

    public void getSkills()
    {
        Log.d(TAG,"Loading Skill");
        SkillResultReceiver skillResultReceiver = new SkillResultReceiver();
        skillResultReceiver.setReceiver(this);
        SkillRestService.startActionGetAll(this,skillResultReceiver);
        progressCount++;
    }

    public void getContacts()
    {
        Log.d(TAG,"Loading contacts");
        ContactResultReceiver contactResultReceiver = new ContactResultReceiver(this);
        ContactRestService.startActionGetAll(this,contactResultReceiver);
        progressCount++;
    }

    public void getAttendees()
    {
        AttendeeResultReceiver attendeeResultReceiver = new AttendeeResultReceiver(this);
        be.ehb.watchin.services.AttendeeDAO.AttendeeRestService.startActionGetAll(this,attendeeResultReceiver);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_watchin_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your are about to logout, are you sure")
                    .setTitle("Logout");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    onLogout();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onListFragmentInteraction(Person item) {

    }

    public void onAddEventClick(View view) {
        Log.d(TAG,"add persons");
        Person p2 = Person.makePerson()
                .age(14)
                .beaconID("36")
                .company("EveryWhere")
                .email("ding@ske.be")
                .ID(15)
                .firstName("Jan")
                .lastName("Uytebroeck")
                .skill("Electronics development")
                .skill("Networking")
                .skill("Doinig everything")
                .create();
        ((WatchInApp)getApplication()).Persons.put(p2.getID(),p2);
        ((PersonListFragment) personList).notifyDataSetChanged();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter  {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public FragmentTemplate getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return eventList;
                case 1:
                    return personList;
                case 2:
                    return personalDetail;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getItem(position).getTitle();
        }

    }

    public void onListEventItemClick(View view){
        Log.d(TAG,"event Item click");
    }
}
