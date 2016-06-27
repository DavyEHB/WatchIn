package be.ehb.watchin.fragments.PersonsFragment;

import android.content.Context;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.fragments.FragmentTemplate;
import be.ehb.watchin.model.Person;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPersonListInteractionListener}
 * interface.
 */
public class PersonListFragment extends FragmentTemplate {

    private static final String ARG_TITLE = "TITLE";
    private static final String title = "Persons";
    private static final String ARG_PERSONS = "PERSONS";
    private static final String TAG = "PersonListFragment";

    private OnPersonListInteractionListener mListener;
    private Map<Integer,Person> mPersons = new HashMap<>();
    private Person me;

    private PersonViewAdapter personViewAdapter;

    public interface OnPersonListInteractionListener {
        // TODO: Update argument type and name
        void onPersonListClick(Person item);
    }

    public PersonListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PersonListFragment newInstance() {
        PersonListFragment fragment = new PersonListFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_TITLE, title);
        //args.putParcelableArray(ARG_PERSONS,persons);
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
        mPersons.putAll(((WatchInApp) getActivity().getApplication()).Persons);

        me = ((WatchInApp) getActivity().getApplication()).Me();
        if (me != null) {
            mPersons.remove(me.getID());
        }
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(personViewAdapter);
                    }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPersonListInteractionListener) {
            mListener = (OnPersonListInteractionListener) context;
            personViewAdapter = new PersonViewAdapter(me,mPersons, mListener);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPersonListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void notifyDataSetChanged()
    {
        //Log.d(TAG,"data changed: " + mPersons.toString());
        mPersons.putAll(((WatchInApp) getActivity().getApplication()).Persons);

        me = ((WatchInApp) getActivity().getApplication()).Me();
        if (me != null) {
            mPersons.remove(me.getID());
        }
        personViewAdapter.refresh(me,mPersons);
        personViewAdapter.notifyDataSetChanged();
    }
}
