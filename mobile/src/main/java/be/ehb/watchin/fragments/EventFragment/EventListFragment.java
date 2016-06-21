package be.ehb.watchin.fragments.EventFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.fragments.FragmentTemplate;
import be.ehb.watchin.model.Event;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.model.dummy.DummyEventList;


public class EventListFragment extends FragmentTemplate {

    private static final String ARG_TITLE = "title";
    private static final String TAG = "EventListFragment";
    private Map<Integer,Event> mEvents = new HashMap<>();
    private static final String title = "Events";

    private OnListFragmentInteractionListener mListener;
    private int myID = 0;

    private EventViewAdapter eventViewAdapter = new EventViewAdapter(myID,mEvents, mListener);

    /**
     * fragment (e.g. upon screen orientation changes).
     */
    public EventListFragment() {
    }

    @SuppressWarnings("unused")
    public static EventListFragment newInstance() {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        fragment.mTitle = title;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
        }
        mEvents = ((WatchInApp) getActivity().getApplication()).Events;
        myID = ((WatchInApp) getActivity().getApplication()).MyID();
        Log.d("SYSTEM_ID", "Global Events: " + String.valueOf(System.identityHashCode(mEvents)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(eventViewAdapter);
        }
        return view;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Person item);
    }

    public void notifyDataSetChanged()
    {
        Log.d(TAG,"data changed: " + mEvents.toString());
        Log.d("SYSTEM_ID", "Notify Events: " + String.valueOf(System.identityHashCode(mEvents)));
        eventViewAdapter.refresh(myID,mEvents);
        eventViewAdapter.notifyDataSetChanged();
    }
}
