package be.ehb.watchin.services.PersonDAO;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.model.Person;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class PersonRestService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GETBYID = "be.ehb.restclient.services.action.GET";
    private static final String ACTION_GETALL = "be.ehb.restclient.services.action.GETALL";
    private static final String ACTION_CREATE = "be.ehb.restclient.services.action.CREATE";
    private static final String ACTION_CHECK_EMAIL = "be.ehb.restclient.services.action.CHECK_EMAIL";

    // TODO: Rename parameters
    private static final String EXTRA_ID = "be.ehb.restclient.services.extra.ID";
    private static final String EXTRA_RECEIVER = "be.ehb.restclient.services.extra.RECEIVER";
    private static final String EXTRA_EMAIL = "be.ehb.restclient.services.extra.EMAIL";

    private static final String TAG = "SkillRestService";
    private static final int MY_SOCKET_TIMEOUT_MS = 5000;
    public static final String BUN_EMAIL = "email";
    public static final String BUN_ID = "id";

    private static String server = WatchInApp.server;
    private static String path = WatchInApp.path.persons;

    private ResultReceiver resultReceiver;
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error handling");
            Log.e(TAG, error.toString());
            resultReceiver.send(PersonResultReceiver.ERROR_RECEIVING,null);
        }
    };

    private Response.ErrorListener checkEmailError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            resultReceiver.send(CheckEmailReceiver.EMAIL_NOT_FOUND,null);
        }
    };

    public PersonRestService() {
        super("SkillRestService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionCheckEmail(Context context, String email, CheckEmailReceiver receiver)
    {
        Intent intent = new Intent(context,PersonRestService.class);
        intent.setAction(ACTION_CHECK_EMAIL);
        intent.putExtra(EXTRA_EMAIL,email);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }

    public static void startActionGetByID(Context context, int ID, PersonResultReceiver receiver) {
        Intent intent = new Intent(context, PersonRestService.class);
        intent.setAction(ACTION_GETBYID);
        intent.putExtra(EXTRA_ID, ID);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }

    public static void startActionGetAll(Context context,PersonResultReceiver receiver) {
        Intent intent = new Intent(context, PersonRestService.class);
        intent.setAction(ACTION_GETALL);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            resultReceiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            //Log.d(TAG,action);
            if (ACTION_GETBYID.equals(action)) {
                final int ID = intent.getIntExtra(EXTRA_ID,0);
                handleActionGetByID(ID);
            } else if (ACTION_GETALL.equals(action)) {
                handleActionGetAll();
            } else if (ACTION_CHECK_EMAIL.equals(action)) {
                final String email = intent.getStringExtra(EXTRA_EMAIL);
                handleActionCheckEmail(email);
            }
        }
    }

    private void handleActionCheckEmail(String email) {

        URI uri = null;
        try {
            uri = new URI("http",server,path +"email/" + email ,null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Log.d(TAG,uri.toString());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,uri.toASCIIString(),null,jsonCheckEmail,checkEmailError){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<String, String>();
                header.put("Accept", "application/json;");
                return header;
            }
        };
        addToQueue(jsonRequest);
    }

    private void handleActionGetAll() {
        URI uri = null;
        try {
            uri = new URI("http",server,path,null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //Log.d(TAG,uri.toString());

        //JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,uri.toASCIIString(),null,jsonGetAllResponse,errorListener);
        JsonArrayRequest jsonRequest = new JsonArrayRequest(uri.toASCIIString(),jsonGetAllResponse,errorListener){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<String, String>();
                header.put("Accept", "application/json;");
                return header;
            }
        };
        addToQueue(jsonRequest);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetByID(int ID){
        RequestQueue queue = Volley.newRequestQueue(this);
        URI uri = null;
        try {
            uri = new URI("http",server,path + ID,null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //Log.d(TAG,uri.toString());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,uri.toASCIIString(),null,jsonGetResponse,errorListener){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<String, String>();
                header.put("Accept", "application/json;");
                return header;
            }
        };

        addToQueue(jsonRequest);
    }

    private void addToQueue(JsonRequest request)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private Response.Listener<JSONObject> jsonCheckEmail = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            Bundle bundle = new Bundle();
            try {
                int id = jsonObject.getInt("ID");
                String email = jsonObject.getString("email");

                bundle.putString(BUN_EMAIL,email);
                bundle.putInt(BUN_ID,id);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            resultReceiver.send(CheckEmailReceiver.EMAIL_OK,bundle);
        }
    };


    private Response.Listener<JSONObject> jsonGetResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            //Log.d(TAG,"We have response");
            //Log.d(TAG, jsonObject.toString());

            Person person = jsonToPerson(jsonObject);
            Bundle bundle = new Bundle();
            bundle.putSerializable("person",person);
            resultReceiver.send(PersonResultReceiver.RESULT_ONE,bundle);
        }
    };

    private Response.Listener<JSONArray> jsonGetAllResponse = new Response.Listener<JSONArray>(){
        @Override
        public void onResponse(JSONArray response) {
            //Log.d(TAG,"We have array response");
            //Log.d(TAG,response.toString());
            ArrayList<Person> userList = new ArrayList<>();
            for (int i =0 ; i < response.length();i++){
                JSONObject jsonObject = null;
                try {
                    jsonObject = response.getJSONObject(i);
                    Person person = jsonToPerson(jsonObject);
                    userList.add(person);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("persons",userList);
            resultReceiver.send(PersonResultReceiver.RESULT_ALL,bundle);
        }
    };

    private Person jsonToPerson(JSONObject jsonObject)
    {
        try {
            int id = jsonObject.getInt("id");
            Person person = new Person();
            person.setID(id);
            person.setFirstName(jsonObject.getString("firstName"));
            person.setLastName(jsonObject.getString("lastName"));
            person.setAge(jsonObject.getInt("age"));
            person.setBeaconID(jsonObject.getString("beaconID"));
            person.setCompany(jsonObject.getString("company"));
            person.setEmail(jsonObject.getString("email"));
            byte[] data = jsonObject.getString("photo").getBytes();
            person.setPhoto(BitmapFactory.decodeByteArray(data,0,data.length));
            return person;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
