package be.ehb.watchin.activities;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
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
import be.ehb.watchin.services.BeaconScannerService;
import be.ehb.watchin.services.ContactDAO.ContactRestService;
import be.ehb.watchin.services.ContactDAO.ContactResultReceiver;
import be.ehb.watchin.services.EventDAO.EventRestService;
import be.ehb.watchin.services.EventDAO.EventResultReceiver;
import be.ehb.watchin.services.MeetingDAO.MeetingRestService;
import be.ehb.watchin.services.MeetingDAO.MeetingResultReceiver;
import be.ehb.watchin.services.PersonDAO.PersonRestService;
import be.ehb.watchin.services.PersonDAO.PersonResultReceiver;
import be.ehb.watchin.services.SkillDAO.SkillRestService;
import be.ehb.watchin.services.SkillDAO.SkillResultReceiver;

public class WatchInMain extends AppCompatActivity implements EventListFragment.OnEventListInteractionListener, PersonListFragment.OnPersonListInteractionListener, PersonResultReceiver.ReceivePerson,
        SkillResultReceiver.ReceiveSkill,ContactResultReceiver.ReceiveContact, EventResultReceiver.ReceiveEvent, AttendeeResultReceiver.ReceiveAttendee, PersonalDetail.OnPersonDetailInteractionListener, MeetingResultReceiver.ReceiveMeeting {

    private static final String TAG = "WatchinMain";
    private static final long DELAYED_DELETE = 30000;


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
    private FragmentTemplate personalDetail;

    public static final String PREFS_NAME = "WatchInPrefs";
    public static final String PREFS_ID = "UserID";
    public static final String PREFS_EMAIL = "Email";
    public static final String PREFS_LOGIN = "Login";
    private static final String PREFS_IS_SCANNING = "IS_SCANNING";

    public static final String ACTION_STOP_SCANNING = "be.ehb.watchin.STOP_SCANNING";

    private static Map<Person,Runnable> detectedPerson = new HashMap<>();


    private boolean isScanning = false;



    //private EventList mEvents = new EventList();


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int progressCount = 0;
    private static final Handler hDelayedDelete = new Handler();


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
            personalDetail = PersonalDetail.newInstance(userID,"Me");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_in_main);

        progress = new ProgressDialog(this);

        if (!progress.isShowing()) {
            progress.setTitle("Loading data");
            progress.setMessage("Please Wait..");
            progress.show();
        }

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

        String action = getIntent().getAction();

        if (ACTION_STOP_SCANNING.equals(action)){
            isScanning = false;
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREFS_IS_SCANNING,isScanning);
            editor.apply();
            invalidateOptionsMenu();
            NotificationManagerCompat.from(getApplicationContext()).cancelAll();
            this.onResume();
            Toast toast = Toast.makeText(this,"Stopped scanning for people",Toast.LENGTH_SHORT);
            toast.show();
        }

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
        getMeetings();


        if (progressCount == 0) {
            progress.dismiss();
        }

        invalidateOptionsMenu();
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
        Log.d(TAG,"Receiving Attendee");
        Log.d(TAG,attendee.toString());

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
    public void onReceiveMeeting(Bundle meeting) {
        progressCount--;
        int ID = meeting.getInt(MeetingRestService.BUN_ID);
        int PID = meeting.getInt(MeetingRestService.BUN_PID);
        int MID = meeting.getInt(MeetingRestService.BUN_MID);

        Person p = ((WatchInApp) getApplication()).Persons.get(PID);
        Person m = ((WatchInApp) getApplication()).Persons.get(MID);
        p.Meetings().put(m.getID(),m);

        ((PersonListFragment) personList).notifyDataSetChanged();

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

    public void getEvents()
    {
        Log.d(TAG,"Loading Events");
        EventResultReceiver eventResultReceiver = new EventResultReceiver(this);
        EventRestService.startActionGetAll(this,eventResultReceiver);
        progressCount++;
    }

    public void getPersons()
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

    public void getMeetings()
    {
        Log.d(TAG,"Loading meetings");
        MeetingResultReceiver meetingResultReceiver = new MeetingResultReceiver(this);
        MeetingRestService.startActionGetAll(this,meetingResultReceiver);
        progressCount++;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_watchin_main, menu);
        MenuItem miScanning = menu.findItem(R.id.action_scanning);
        MenuItem miEnterEvent = menu.findItem(R.id.action_enter_event);
        MenuItem miLeaveEvent = menu.findItem(R.id.action_leave_event);

        miScanning.setChecked(isScanning);

        Person Me = ((WatchInApp) getApplication()).Me();
        if (Me != null) {
            if (Me.getCurrentEventID() != 0) {
                miLeaveEvent.setVisible(true);
                miEnterEvent.setVisible(false);
            } else {
                miLeaveEvent.setVisible(false);
                miEnterEvent.setVisible(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
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
        else if (id == R.id.action_scanning) {
            isScanning = !item.isChecked();
            item.setChecked(isScanning);
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREFS_IS_SCANNING,isScanning);
            editor.apply();
            invalidateOptionsMenu();
            this.onResume();
            return true;
        } else if (id == R.id.action_enter_event){
            try {

                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "AZTEC_MODE"); // "PRODUCT_MODE for bar codes

                startActivityForResult(intent, 0);

            } catch (Exception e) {

                Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                startActivity(marketIntent);

            }
        } else if (id == R.id.action_leave_event) {
            ((WatchInApp) getApplication()).Me().setCurrentEventID(0);
            PersonResultReceiver personResultReceiver = new PersonResultReceiver(new PersonResultReceiver.ReceivePerson() {
                @Override
                public void onReceive(Person person) {

                }

                @Override
                public void onReceiveAllPersons(Map<Integer, Person> persons) {

                }

                @Override
                public void onError() {

                }
            });
            PersonRestService.startActionUpdate(this,((WatchInApp) getApplication()).Me(),personResultReceiver);
            invalidateOptionsMenu();
            Toast toast = Toast.makeText(this,"You left the event",Toast.LENGTH_SHORT);
            toast.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        isScanning = sharedPref.getBoolean(PREFS_IS_SCANNING,false);

        if (isScanning) {
            ResultReceiver rec = new ResultReceiver(new Handler())
            {
                //TODO Complete on receive handler
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    super.onReceiveResult(resultCode, resultData);
                    ScanResult result = resultData.getParcelable(BeaconScannerService.BUN_BEACON);
                    Log.d(TAG, "BeaconAddress: " + result.getDevice().getAddress());
                    Person Me = ((WatchInApp) getApplication()).Me();
                    for (Person p : Me.Meetings().values()){
                        Log.d(TAG, "PersonBeacon: " + p.getBeaconID());
                        if (p.isVisible()) {
                            Log.d(TAG, "onReceiveResult: Visible");
                            if ((p.getCurrentEventID() != 0)  && (p.getCurrentEventID() == Me.getCurrentEventID())) {
                                Log.d(TAG, "onReceiveResult: Same event");
                                if (p.getBeaconID().equals(result.getDevice().getAddress())) {
                                    addToList(p);
                                }
                            }
                        }
                    }
                }
            };
            BeaconScannerService.startScanning(this,rec);
        }
        else
        {
            BeaconScannerService.stopScanning(this);
        }

        super.onResume();
    }

    private void addToList(final Person person){
        if (!detectedPerson.containsKey(person)){
            Log.d(TAG, "addToList: not in detected list");
            Runnable delayedRun = new Runnable() {
                @Override
                public void run() {
                    detectedPerson.remove(person);
                    Log.d(TAG, "run: Removed person");
                    Log.d(TAG, "run: " + detectedPerson.toString());
                }
            };
            hDelayedDelete.postDelayed(delayedRun, DELAYED_DELETE);
            detectedPerson.put(person,delayedRun);
            Log.d(TAG, "addToList: Added");
            Log.d(TAG, "addToList: " + detectedPerson.toString());
            showWatchNotification(person);
        }
        else
        {
            Log.d(TAG, "addToList: Already in list");
            Log.d(TAG, "addToList: " + detectedPerson.toString());
            Runnable rOldRun = detectedPerson.get(person);
            hDelayedDelete.removeCallbacks(rOldRun);
            Runnable delayedRun = new Runnable() {
                @Override
                public void run() {
                    detectedPerson.remove(person);
                    Log.d(TAG, "run: Removed person");
                    Log.d(TAG, "run: " + detectedPerson.toString());
                }
            };
            hDelayedDelete.postDelayed(delayedRun,DELAYED_DELETE);
            detectedPerson.put(person,delayedRun);
        }
    }


    private void showWatchNotification(Person person){

        int notificationId = 001;
        // Build intent for notification content
        Intent viewIntent = new Intent(this, TempLauncher.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_people_black_24dp
                        )
                        .setContentTitle(person.getFullname())
                        .setContentText(person.getFirstName() + " is within range")
                        .setContentIntent(viewPendingIntent)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000})
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(R.drawable.mr_ic_play_light,"action", viewPendingIntent);

// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }



    @Override
    public void onPersonListClick(Person item) {
        Intent intent = new Intent(this,PersonDetailActivity.class);
        intent.putExtra(PersonDetailActivity.USER_ID,item.getID());
        startActivity(intent);
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

    @Override
    public void onEventListClick(Event event) {
        Intent intent = new Intent(this,EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EVENT_ID,event.getID());
        startActivity(intent);
    }

    public void onVisibleClick(View view) {
        Person me = ((WatchInApp) getApplication()).Me();
        me.setVisible(!me.isVisible());
        Log.d(TAG, "onVisibleClick: " + me.isVisible());
        PersonResultReceiver personResultReceiver = new PersonResultReceiver(new PersonResultReceiver.ReceivePerson() {
            @Override
            public void onReceive(Person person) {

            }

            @Override
            public void onReceiveAllPersons(Map<Integer, Person> persons) {

            }

            @Override
            public void onError() {

            }
        });
        PersonRestService.startActionUpdate(this,me,personResultReceiver);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                String UUID = data.getStringExtra("SCAN_RESULT");
                Log.d(TAG, "onActivityResult: " + UUID);
                for (Event e: ((WatchInApp) getApplication()).Events.values()){
                    if (e.getUuid().toString().equals(UUID)){
                        ((WatchInApp) getApplication()).Me().setCurrentEventID(e.getID());
                        PersonResultReceiver personResultReceiver = new PersonResultReceiver(new PersonResultReceiver.ReceivePerson() {
                            @Override
                            public void onReceive(Person person) {

                            }

                            @Override
                            public void onReceiveAllPersons(Map<Integer, Person> persons) {

                            }

                            @Override
                            public void onError() {

                            }
                        });
                        PersonRestService.startActionUpdate(this,((WatchInApp) getApplication()).Me(),personResultReceiver);
                        invalidateOptionsMenu();
                        Toast toast = Toast.makeText(this,"You can enter the event",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }
}
