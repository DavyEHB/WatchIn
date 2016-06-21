package be.ehb.watchin.fragments.EventFragment;

import android.support.v7.widget.RecyclerView;
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
import java.util.List;

public class EventViewAdapter extends RecyclerView.Adapter<EventViewAdapter.ViewHolder> {

    private final List<Event> mValues;

    public EventViewAdapter(List<Event> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        List<Event> sortedList = new ArrayList<>(mValues);
        Collections.sort(sortedList, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                if (lhs.getDate() == null || rhs.getDate() == null)
                    return 0;
                return lhs.getDate().compareTo(rhs.getDate());
            }
        });

        holder.mItem = sortedList.get(position);
        holder.mEventName.setText(sortedList.get(position).getName());
        Date date = sortedList.get(position).getDate();
        if (date != null){
            SimpleDateFormat ft =
                    new SimpleDateFormat("dd MMMM yy");

            holder.mDate.setText(ft.format(date));
        }
        holder.mAttendees.setText(String.valueOf(sortedList.get(position).Attendees().size()));
        holder.mLocation.setText(sortedList.get(position).getLocation());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
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
