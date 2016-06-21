package be.ehb.watchin.fragments.PersonsFragment;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.model.Person;

import java.util.List;
import java.util.Map;


public class PersonViewAdapter extends RecyclerView.Adapter<PersonViewAdapter.ViewHolder> {

    private static final String TAG = "PersonViewAdapter";
    private Map<Integer,Person> mPersons;
    private Integer[] mKeys;
    private final PersonListFragment.OnListFragmentInteractionListener mListener;
    private int mID;

    public PersonViewAdapter(int myID,Map<Integer,Person> persons, PersonListFragment.OnListFragmentInteractionListener listener) {
         mListener = listener;
        mPersons = persons;
        mKeys = mPersons.keySet().toArray(new Integer[mPersons.size()]);
        mID = myID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_people, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mPerson = mPersons.get(mKeys[position]);
        holder.txtFullName.setText(holder.mPerson.getFullname());
        holder.txtCompany.setText(holder.mPerson.getCompany());
        if (mPersons.containsKey(mID)) {
            Person me = mPersons.get(mID);
            holder.txtMutualFriends.setText(String.valueOf(holder.mPerson.findMutualContacts(me).size()));
        } else
        {
            holder.txtMutualFriends.setText("0");
        }
        holder.txtMutualEvents.setText("2");
        List<String> skillList = holder.mPerson.Skills();
        String skills = "";
        if (skillList.size()>0) {
            skills = skillList.get(0);

            for (int i = 1; i < skillList.size(); i++) {
                skills = skills + ", " + skillList.get(i);
            }


        }
        holder.txtSkills.setText(skills);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPersons.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView txtFullName;
        public final TextView txtCompany;
        public final TextView txtSkills;
        public final TextView txtMutualFriends;
        public final TextView txtMutualEvents;
        public Person mPerson;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            txtFullName = (TextView) view.findViewById(R.id.txtFullName);
            txtCompany = (TextView) view.findViewById(R.id.txtCompany);
            txtMutualEvents = (TextView) view.findViewById(R.id.txtMutualEvents);
            txtMutualFriends =(TextView) view.findViewById(R.id.txtMutualFriends);
            txtSkills = (TextView) view.findViewById(R.id.txtSkills);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + txtFullName.getText() + "'";
        }
    }

    public void refresh(int myID,Map<Integer,Person> persons)
    {
        mID = myID;
        mPersons = persons;
        mKeys = mPersons.keySet().toArray(new Integer[mPersons.size()]);
    }
}
