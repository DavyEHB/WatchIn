package be.ehb.watchin.services.AttendeeDAO;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import be.ehb.watchin.WatchInApp;


/**
 * Created by davy.van.belle on 14/06/2016.
 */
public class AttendeeRestService extends IntentService{

    private static final String ACTION_GETBYID = "be.ehb.watchin.services.action.GET";
    private static final String ACTION_GETALL = "be.ehb.watchin.services.action.GETALL";
    private static final String ACTION_CREATE = "be.ehb.watchin.services.action.CREATE";
    private static final String ACTION_ADD = "be.ehb.watchin.services.action.ADD";
    private static final String ACTION_DELETE = "be.ehb.watchin.services.action.DELETE";
    private static final String ACTION_UPDATE = "be.ehb.watchin.services.action.UPDATE";

    private static final String EXTRA_ID = "be.ehb.watchin.services.extra.ID";
    private static final String EXTRA_RECEIVER = "be.ehb.watchin.services.extra.RECEIVER";
    private static final String EXTRA_PID = "be.ehb.watchin.services.extra.PID";
    private static final String EXTRA_EID = "be.ehb.watchin.services.extra.EID";
    private static final String EXTRA_LOGGED_IN = "be.ehb.watchin.services.extra.LOGGED_IN";

    private static final String TAG = "ContactRestService";
    private static final int MY_SOCKET_TIMEOUT_MS = 5000;



    private static String server = WatchInApp.server;
    private static String path = WatchInApp.path.attendees;

    public static final String JSON_ID = "id";
    public static final String JSON_PID = "pid";
    public static final String JSON_EID = "eid";
    public static final String JSON_LOGGED_IN = "loggedIn";

    public static final String BUN_ID = "BUN_ID";
    public static final String BUN_PID = "BUN_PID";
    public static final String BUN_EID = "BUN_EID";
    public static final String BUN_LOGGED_IN = "BUN_LOGGED_IN";


    private ResultReceiver resultReceiver;
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error handling");
            Log.e(TAG, error.toString());
            resultReceiver.send(AttendeeResultReceiver.ERROR_RECEIVING,null);
        }
    };


    public AttendeeRestService() {
        super("ContactRestService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionGetByID(Context context, int ID, AttendeeResultReceiver receiver) {
        Intent intent = new Intent(context, AttendeeRestService.class);
        intent.setAction(ACTION_GETBYID);
        intent.putExtra(EXTRA_ID, ID);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }

    public static void startActionGetAll(Context context,AttendeeResultReceiver receiver) {
        Intent intent = new Intent(context, AttendeeRestService.class);
        intent.setAction(ACTION_GETALL);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
        Log.d(TAG, "Start GetAll");
    }

    public static void startActionAdd(Context context,int PID, int EID,Boolean loggedIn, AttendeeResultReceiver receiver) {
        Intent intent = new Intent(context, AttendeeRestService.class);
        intent.setAction(ACTION_ADD);
        intent.putExtra(EXTRA_PID, PID);
        intent.putExtra(EXTRA_EID, EID);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        intent.putExtra(EXTRA_LOGGED_IN,loggedIn);
        context.startService(intent);
        Log.d(TAG, "Start Add");
    }

    public static void startActionUpdate(Context context,int PID, int EID,Boolean loggedIn, AttendeeResultReceiver receiver) {
        Intent intent = new Intent(context, AttendeeRestService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_PID, PID);
        intent.putExtra(EXTRA_EID, EID);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        intent.putExtra(EXTRA_LOGGED_IN,loggedIn);
        context.startService(intent);
        Log.d(TAG, "Start Update");
    }


    public static void startActionDelete(Context context,int PID, int EID, AttendeeResultReceiver receiver){
        Intent intent = new Intent(context, AttendeeRestService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_PID, PID);
        intent.putExtra(EXTRA_EID, EID);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            resultReceiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            Log.d(TAG,action);
            if (ACTION_GETBYID.equals(action)) {
                final int ID = intent.getIntExtra(EXTRA_ID,0);
                handleActionGetByID(ID);
            } else if (ACTION_GETALL.equals(action)) {
                handleActionGetAll();
            } else if (ACTION_ADD.equals(action)){
                final int PID = intent.getIntExtra(EXTRA_PID,0);
                final int EID = intent.getIntExtra(EXTRA_EID,0);
                final Boolean loggedIn = intent.getBooleanExtra(EXTRA_LOGGED_IN,false);
                handleActionAdd(PID,EID,loggedIn);
            } else if (ACTION_DELETE.equals(action)){
                final int PID = intent.getIntExtra(EXTRA_PID,0);
                final int EID = intent.getIntExtra(EXTRA_EID,0);
                handleActionDelete(PID,EID);
            } else if (ACTION_UPDATE.equals(action)){
                final int PID = intent.getIntExtra(EXTRA_PID,0);
                final int EID = intent.getIntExtra(EXTRA_EID,0);
                final Boolean loggedIn = intent.getBooleanExtra(EXTRA_LOGGED_IN,false);
                handleActionUpdate(PID,EID,loggedIn);
            }
        }
    }

    private void handleActionDelete(int pid, int eid) {
        URI uri = null;
        try {
            uri = new URI("http",server,path + pid + "/" + eid,null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Log.d(TAG,uri.toString());

       // JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DELETE,uri.toASCIIString(),null,volleyDeleteResponse,errorListener);
        StringRequest request = new StringRequest(Request.Method.DELETE,uri.toASCIIString(), volleyDeleteResponse,errorListener);

        addToQueue(request);
    }

    private void handleActionGetAll() {
        URI uri = null;
        try {
            uri = new URI("http",server,path,null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Log.d(TAG,uri.toString());

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

    private void handleActionAdd(int PID, int EID, boolean loggedIn) {
        URI uri = null;
        try {
            uri = new URI("http",server,path,null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //Log.d(TAG,uri.toString());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_ID,0);
            jsonObject.put(JSON_PID,PID);
            jsonObject.put(JSON_EID,EID);
            jsonObject.put(JSON_LOGGED_IN,loggedIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,uri.toASCIIString(),jsonObject,jsonPostResponse,errorListener){
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

    private void handleActionUpdate(int PID, int EID, boolean loggedIn) {
        Log.d(TAG, "Update");
        URI uri = null;
        try {
            uri = new URI("http",server,path + PID + "/" + EID,null,null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //Log.d(TAG,uri.toString());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_PID,PID);
            jsonObject.put(JSON_EID,EID);
            jsonObject.put(JSON_LOGGED_IN,loggedIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }


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

    private void addToQueue(Request request)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Log.d(TAG,request.toString());
        queue.add(request);
    }


    private Response.Listener<JSONObject> jsonGetResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            /*
            Log.d(TAG,"We have response");
            Log.d(TAG, jsonObject.toString());
            */

            Bundle bundle = new Bundle();
            try {
                bundle.putInt(BUN_ID,jsonObject.getInt(JSON_ID));
                bundle.putInt(BUN_PID,jsonObject.getInt(JSON_PID));
                bundle.putInt(BUN_EID,jsonObject.getInt(JSON_EID));
                bundle.putBoolean(BUN_LOGGED_IN,jsonObject.getBoolean(JSON_LOGGED_IN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d(TAG,bundle.toString());
            resultReceiver.send(AttendeeResultReceiver.RESULT_ONE,bundle);
        }
    };

    private Response.Listener<JSONArray> jsonGetAllResponse = new Response.Listener<JSONArray>(){
        @Override
        public void onResponse(JSONArray response) {
            /*
            Log.d(TAG,"We have array response");
            Log.d(TAG,response.toString());
            */

            for (int i =0 ; i < response.length();i++){
                JSONObject jsonObject = null;
                Bundle bundle = new Bundle();
                try {
                    jsonObject = response.getJSONObject(i);
                    bundle.putInt(BUN_ID,jsonObject.getInt(JSON_ID));
                    bundle.putInt(BUN_PID,jsonObject.getInt(JSON_PID));
                    bundle.putInt(BUN_EID,jsonObject.getInt(JSON_EID));
                    bundle.putBoolean(BUN_LOGGED_IN,jsonObject.getBoolean(JSON_LOGGED_IN));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG,bundle.toString());
                resultReceiver.send(AttendeeResultReceiver.RESULT_ONE,bundle);
            }
        }
    };

    private Response.Listener<JSONObject> jsonPostResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {

            Log.d(TAG,"We have response on POST");
            Log.d(TAG, jsonObject.toString());

        }
    };

    private Response.Listener<JSONObject> jsonPutResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {

            Log.d(TAG,"We have response on PUT");
            Log.d(TAG, jsonObject.toString());

        }
    };

    private Response.Listener<String> volleyDeleteResponse = new Response.Listener<String>() {
        @Override
        public void onResponse(String string) {

            Log.d(TAG,"We have response on Delete");
            Log.d(TAG, string);

        }
    };
}
