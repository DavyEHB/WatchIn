package be.ehb.watchin.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.fragments.EventFragment.EventViewAdapter;
import be.ehb.watchin.fragments.PersonsFragment.PersonListFragment;
import be.ehb.watchin.fragments.PersonsFragment.PersonViewAdapter;
import be.ehb.watchin.model.Event;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.services.AttendeeDAO.AttendeeResultReceiver;

public class EventDetailActivity extends AppCompatActivity implements PersonListFragment.OnPersonListInteractionListener, AttendeeResultReceiver.ReceiveAttendee {

    public static final String EVENT_ID = "be.ehb.be.watchin.EVENT_ID";
    private static final String TAG = "EventDetailActivity";
    private Event mEvent;
    private Person me;
    private PersonViewAdapter personViewAdapter;

    private RecyclerView.LayoutManager rvLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        int eventID = getIntent().getIntExtra(EVENT_ID,0);
        mEvent = ((WatchInApp) getApplication()).Events.get(eventID);
        Log.d(TAG,mEvent.toString());

        me = ((WatchInApp) getApplication()).Me();

        TextView txtEventName = (TextView) findViewById(R.id.txtEventName);
        TextView txtEventStart = (TextView) findViewById(R.id.txtEventStart);
        TextView txtEventEnd = (TextView) findViewById(R.id.txtEventEnd);
        TextView txtEventLocation = (TextView) findViewById(R.id.txtEventLocation);

        RecyclerView rvAttendees = (RecyclerView) findViewById(R.id.rvAttendees);

        Button btnAttend = (Button) findViewById(R.id.btnAttend);
        Button btnDismiss = (Button) findViewById(R.id.btnDismiss);

        if (mEvent != null){
            txtEventName.setText(mEvent.getName());
            txtEventLocation.setText("Place: " + mEvent.getLocation());

            Date startTime = mEvent.getStartTime();
            if (startTime != null){
                SimpleDateFormat ft =
                        new SimpleDateFormat("dd MMMM yy - hh:mm");
                txtEventStart.setText("From: " + ft.format(startTime));
            }

            Date endTime = mEvent.getEndTime();
            if (endTime != null){
                SimpleDateFormat ft =
                        new SimpleDateFormat("dd MMMM yy - hh:mm");
                txtEventEnd.setText("To: " + ft.format(endTime));
            }

            // use a linear layout manager
            rvLayoutManager = new LinearLayoutManager(this);
            rvAttendees.setLayoutManager(rvLayoutManager);

            personViewAdapter = new PersonViewAdapter(me,mEvent.Attendees(),this);
            rvAttendees.setAdapter(personViewAdapter);
            Log.d(TAG, "Number of attendees; " + String.valueOf(mEvent.Attendees().size()));

            if (mEvent.Attendees().containsValue(me)){
                Log.d(TAG,"Going to event");
                btnDismiss.setVisibility(View.VISIBLE);
                btnAttend.setVisibility(View.GONE);
            } else
            {
                btnDismiss.setVisibility(View.GONE);
                btnAttend.setVisibility(View.VISIBLE);
            }
        }
    }



    @Override
    public void onPersonListClick(Person item) {
        Log.d(TAG,"OnListClick");
    }

    public void onBtnAttendClick(View view) {
        int myID = ((WatchInApp) getApplication()).Me().getID();
        AttendeeResultReceiver attendeeResultReceiver = new AttendeeResultReceiver(this);
        be.ehb.watchin.services.AttendeeDAO.AttendeeRestService.startActionAdd(this,myID,mEvent.getID(),false,attendeeResultReceiver);
        this.recreate();
    }


    public void onBtnDismissClick(View view) {
        int myID = ((WatchInApp) getApplication()).Me().getID();
        AttendeeResultReceiver attendeeResultReceiver = new AttendeeResultReceiver(this);
        be.ehb.watchin.services.AttendeeDAO.AttendeeRestService.startActionDelete(this,myID,mEvent.getID(),attendeeResultReceiver);
        this.recreate();
    }

    @Override
    public void onReceiveAttendee(Bundle contact) {

    }

    @Override
    public void onError() {

    }

}
