package be.ehb.watchin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import be.ehb.watchin.model.Event;

public class EventDetailActivity extends AppCompatActivity {

    public static final String EVENT_ID = "be.ehb.be.watchin.EVENT_ID";
    private static final String TAG = "EventDetailActivity";
    private Event mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        int eventID = getIntent().getIntExtra(EVENT_ID,0);
        mEvent = ((WatchInApp) getApplication()).Events.get(eventID);
        Log.d(TAG,mEvent.toString());

        TextView txtEventName = (TextView) findViewById(R.id.txtEventName);
        TextView txtEventStart = (TextView) findViewById(R.id.txtEventStart);
        TextView txtEventEnd = (TextView) findViewById(R.id.txtEventEnd);
        TextView txtEventLocation = (TextView) findViewById(R.id.txtEventLocation);
        ListView lvAttendees = (ListView) findViewById(R.id.lvAttendees);

        if (mEvent != null){
            txtEventName.setText(mEvent.getName());
            txtEventLocation.setText(mEvent.getLocation());

            Date startTime = mEvent.getStartTime();
            if (startTime != null){
                SimpleDateFormat ft =
                        new SimpleDateFormat("dd MMMM yy - hh:mm");
                txtEventStart.setText(ft.format(startTime));
            }

            Date endTime = mEvent.getEndTime();
            if (endTime != null){
                SimpleDateFormat ft =
                        new SimpleDateFormat("dd MMMM yy - hh:mm");
                txtEventEnd.setText(ft.format(endTime));
            }

        }
    }
}
