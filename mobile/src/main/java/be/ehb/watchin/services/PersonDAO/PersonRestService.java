package be.ehb.watchin.services.PersonDAO;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.activities.WatchInMain;
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
    private static final String ACTION_GETBYID = "be.ehb.personrest.services.action.GET";
    private static final String ACTION_GETALL = "be.ehb.personrest.services.action.GETALL";
    private static final String ACTION_CREATE = "be.ehb.personrest.services.action.CREATE";
    private static final String ACTION_CHECK_EMAIL = "be.ehb.personrest.services.action.CHECK_EMAIL";
    private static final String ACTION_UPDATE = "be.ehb.personrest.services.action.UPDATE";

    private static final String EXTRA_RECEIVER = "be.ehb.personrest.services.extra.RECEIVER";
    private static final String EXTRA_PERSON = "be.ehb.personrest.services.extra.PERSON";
    private static final String EXTRA_ID = "be.ehb.personrest.services.extra.ID";
    private static final String EXTRA_EMAIL = "be.ehb.personrest.services.extra.EMAIL";


    private static final String JSON_ID = "id";
    private static final String JSON_FIRST_NAME = "firstName";
    private static final String JSON_LAST_NAME = "lastName";
    private static final String JSON_AGE = "age";
    private static final String JSON_BEACON_ID = "beaconID";
    private static final String JSON_COMPANY = "company";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_VISIBLE = "visible";
    private static final String JSON_PHOTO = "photo";
    private static final String JSON_CURRENT_EVENT_ID = "currentEventID";





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

    public static void startActionUpdate(Context context,Person person,PersonResultReceiver receiver){
        Intent intent = new Intent(context, PersonRestService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        intent.putExtra(EXTRA_PERSON, person);
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
            } else if (ACTION_UPDATE.equals(action)) {
                final Person person = (Person) intent.getSerializableExtra(EXTRA_PERSON);
                handleActionUpdate(person);
            }
        }
    }

    private void handleActionUpdate(Person person) {
        Log.d(TAG, "Update");
        URI uri = null;
        try {
            uri = new URI("http",server,path + person.getID(),null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        JSONObject jsonObject = personToJSON(person);
        Log.d(TAG, "handleActionUpdate: " + jsonObject.toString());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PUT,uri.toASCIIString(),jsonObject,jsonPutResponse,errorListener){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<String, String>();
                header.put("Accept", "application/json;");
                return header;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

        };

        addToQueue(jsonRequest);
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
            int id = jsonObject.getInt(JSON_ID);
            Person person = new Person();
            person.setID(id);
            person.setFirstName(jsonObject.getString(JSON_FIRST_NAME));
            person.setLastName(jsonObject.getString(JSON_LAST_NAME));
            person.setAge(jsonObject.getInt(JSON_AGE));
            person.setBeaconID(jsonObject.getString(JSON_BEACON_ID));
            person.setCompany(jsonObject.getString(JSON_COMPANY));
            person.setEmail(jsonObject.getString(JSON_EMAIL));
            person.setVisible(jsonObject.getBoolean(JSON_VISIBLE));
            person.setPhoto(getBitmapFromString(jsonObject.getString(JSON_PHOTO)));
            person.setCurrentEventID(jsonObject.getInt(JSON_CURRENT_EVENT_ID));
            return person;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject personToJSON(Person person)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_ID,person.getID());
            jsonObject.put(JSON_FIRST_NAME,person.getFirstName());
            jsonObject.put(JSON_LAST_NAME,person.getLastName());
            jsonObject.put(JSON_AGE,person.getAge());
            jsonObject.put(JSON_BEACON_ID,person.getBeaconID());
            jsonObject.put(JSON_COMPANY,person.getCompany());
            jsonObject.put(JSON_EMAIL,person.getEmail());
            jsonObject.put(JSON_VISIBLE,person.isVisible());
            jsonObject.put(JSON_PHOTO, getStringFromBitmap(person.getPhoto()));
            jsonObject.put(JSON_CURRENT_EVENT_ID,person.getCurrentEventID());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
 /*
 * This functions converts Bitmap picture to a string which can be
 * JSONified.
 * */
        if (bitmapPicture != null) {
            final int COMPRESSION_QUALITY = 100;
            String encodedImage;
            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
            bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                    byteArrayBitmapStream);
            byte[] b = byteArrayBitmapStream.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            return encodedImage;
        } else
        {
            return "";
        }
    }

    private Bitmap getBitmapFromString(String jsonString) {
/*
* This Function converts the String back to Bitmap
* */
        if ((jsonString != "") && (jsonString != null)) {
            byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            return decodedByte;
        } else
        {
            return null;
        }
    }

    private Response.Listener<JSONObject> jsonPutResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {

            Log.d(TAG,"We have response on PUT");
            Log.d(TAG, jsonObject.toString());

        }
    };
}
