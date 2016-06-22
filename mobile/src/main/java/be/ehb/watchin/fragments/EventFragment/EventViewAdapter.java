package be.ehb.watchin.fragments.EventFragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.ehb.watchin.R;
import be.ehb.watchin.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventViewAdapter extends RecyclerView.Adapter<EventViewAdapter.ViewHolder> {

    private static final String TAG = "EventViewAdapter";
    private Map<Integer,Event> mEvents=null;
    private Integer[] mKeys;
    private final EventListFragment.OnListFragmentInteractionListener mListener;
    private int mID;

    public EventViewAdapter(int myID, Map<Integer,Event> events, EventListFragment.OnListFragmentInteractionListener listener) {
        mID= myID;
        mEvents =  events;
        mKeys = mEvents.keySet().toArray(new Integer[mEvents.size()]);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG,"onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mEvents.get(mKeys[position]);
        holder.mEventName.setText(holder.mItem.getName());
        Date date = holder.mItem.getStartTime();
        if (date != null){
            SimpleDateFormat ft =
                    new SimpleDateFormat("dd MMMM yy");

            holder.mDate.setText(ft.format(date));
        }
        holder.mAttendees.setText(String.valueOf(holder.mItem.Attendees().size()));
        holder.mLocation.setText(holder.mItem.getLocation());

    }

    public static Map<Integer, Event> sortByValue( Map<Integer, Event> map )
    {
        List<Map.Entry<Integer, Event>> list = new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<Integer, Event>>()
        {
            public int compare( Map.Entry<Integer, Event> o1, Map.Entry<Integer,Event> o2 )
            {
                if (o1.getValue().getStartTime() == null || o2.getValue().getStartTime() == null)
                    return 0;
                return (o1.getValue().getStartTime().compareTo(o2.getValue().getStartTime()));
            }
        } );

        Map<Integer, Event> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Event> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void refresh(int myID, Map<Integer, Event> events) {
        mID = myID;
        mEvents = events;
        mKeys = events.keySet().toArray(new Integer[events.size()]);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mEventName;
        public final TextView mDate;
        public final TextView mLocation;
        public final TextView mAttendees;
        public Event mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mEventName = (TextView) view.findViewById(R.id.txtEventListItemName);
            mDate = (TextView) view.findViewById(R.id.txtEventListItemDate);
            mAttendees = (TextView) view.findViewById(R.id.txtEventListItemAttendees);
            mLocation = (TextView) view.findViewById(R.id.txtEventListItemLocation);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mEventName.getText() + "'";
        }
    }
}
