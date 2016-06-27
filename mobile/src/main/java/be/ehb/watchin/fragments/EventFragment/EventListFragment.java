package be.ehb.watchin.fragments.EventFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.fragments.FragmentTemplate;
import be.ehb.watchin.model.Event;


public class EventListFragment extends FragmentTemplate {

    private static final String ARG_TITLE = "title";
    private static final String TAG = "EventListFragment";
    private Map<Integer,Event> mEvents = new HashMap<>();
    private static final String title = "Events";

    private OnEventListInteractionListener mListener;
    private int myID = 0;

    private EventViewAdapter eventViewAdapter;

    public interface OnEventListInteractionListener {
        // TODO: Update argument type and name
        void onEventListClick(Event event);
    }

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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEventListInteractionListener) {
            mListener = (OnEventListInteractionListener) context;
            Log.d(TAG, "System hash: " + System.identityHashCode(mListener));
            eventViewAdapter = new EventViewAdapter(myID,mEvents, mListener);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventListFragmentInteractionListener");
        }
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

    public void notifyDataSetChanged()
    {
        eventViewAdapter.refresh(myID,mEvents);
        eventViewAdapter.notifyDataSetChanged();
    }
}
