package be.ehb.watchin.fragments;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
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

    private Person me;

    public PersonalDetail() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
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
        me = ((WatchInApp) getActivity().getApplication()).Persons.get(myID);
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

            txtFullname.setText(me.getFullname());
            txtEmail.setText(me.getEmail());
            txtCompany.setText(me.getCompany());
            txtContacts.setText(String.valueOf(me.Contacts().size()));

        }

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
}
