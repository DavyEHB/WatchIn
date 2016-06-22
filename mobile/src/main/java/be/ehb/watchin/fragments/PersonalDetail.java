package be.ehb.watchin.fragments;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.fragments.EventFragment.EventViewAdapter;
import be.ehb.watchin.model.Event;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.services.PersonDAO.PersonRestService;
import be.ehb.watchin.services.PersonDAO.PersonResultReceiver;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonalDetail.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PersonalDetail#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalDetail extends FragmentTemplate {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "FragmentPersonalDetail";

    private String title;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Person me = new Person();
    private EventAdapter eventAdapter = new EventAdapter(me, mListener);

    public PersonalDetail() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *     * @param title Parameter 1.
     * @return A new instance of fragment PersonalDetailActivity.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalDetail newInstance(String title) {
        PersonalDetail fragment = new PersonalDetail();
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
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        int myID = ((WatchInApp) getActivity().getApplication()).MyID();
        me = ((WatchInApp) getActivity().getApplication()).Me();
        //me = ((WatchInApp) getActivity().getApplication()).Persons.get(myID);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_personal_detail, container, false);

        boolean login = ((WatchInApp) getActivity().getApplication()).isLoggedIn();
        if ((me!=null)&&(login)){
            TextView txtFullname = (TextView) view.findViewById(R.id.txtFullName);
            TextView txtCompany = (TextView) view.findViewById(R.id.txtCompany);
            TextView txtEmail = (TextView) view.findViewById(R.id.txtEmail);
            TextView txtContacts = (TextView) view.findViewById(R.id.txtContacts);

            RecyclerView lvEvents = (RecyclerView) view.findViewById(R.id.lvMyEvents);

            txtFullname.setText(me.getFullname());
            txtEmail.setText(me.getEmail());
            txtCompany.setText(me.getCompany());
            txtContacts.setText(String.valueOf(me.Contacts().size()));

            if (lvEvents != null) {
                Context context = lvEvents.getContext();
                lvEvents.setLayoutManager(new LinearLayoutManager(context));
                lvEvents.setAdapter(eventAdapter);
            }

        }

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void notifyDataChange(Person Me){
           this.me = Me;
        eventAdapter.refresh(me,me.Events());
        eventAdapter.notifyDataSetChanged();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(View uri);
    }

    public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

        private static final String TAG = "EventAdapter";
         private Integer[] mKeys;
        private final PersonalDetail.OnFragmentInteractionListener mListener;
        private Person me;

        public EventAdapter(Person Me,PersonalDetail.OnFragmentInteractionListener listener) {
            super();
            this.me = Me;
            mKeys = me.Events().keySet().toArray(new Integer[me.Events().size()]);
            mListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_event, parent, false);

            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = me.Events().get(mKeys[position]);
            holder.mEventName.setText(holder.mItem.getName());
            Date date = holder.mItem.getStartTime();
            if (date != null) {
                SimpleDateFormat ft =
                        new SimpleDateFormat("dd MMMM yy");

                holder.mDate.setText(ft.format(date));
            }
            holder.mAttendees.setText(String.valueOf(holder.mItem.Attendees().size()));
            holder.mLocation.setText(holder.mItem.getLocation());

        }

        public Map<Integer, Event> sortByValue(Map<Integer, Event> map) {
            List<Map.Entry<Integer, Event>> list = new LinkedList<>(map.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Event>>() {
                public int compare(Map.Entry<Integer, Event> o1, Map.Entry<Integer, Event> o2) {
                    if (o1.getValue().getStartTime() == null || o2.getValue().getStartTime() == null)
                        return 0;
                    return (o1.getValue().getStartTime().compareTo(o2.getValue().getStartTime()));
                }
            });

            Map<Integer, Event> result = new LinkedHashMap<>();
            for (Map.Entry<Integer, Event> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        @Override
        public int getItemCount() {
            return me.Events().size();
        }

        public void refresh(Person Me, Map<Integer, Event> events) {
            this.me = Me;
            mKeys = me.Events().keySet().toArray(new Integer[me.Events().size()]);
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
}
