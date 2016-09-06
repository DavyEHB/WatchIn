package be.ehb.watchin.services.ContactDAO;

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
import com.android.volley.toolbox.JsonRequest;
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
public class ContactRestService extends IntentService{

    private static final String ACTION_GETBYID = "be.ehb.watchin.services.action.GET";
    private static final String ACTION_GETALL = "be.ehb.watchin.services.action.GETALL";
    private static final String ACTION_CREATE = "be.ehb.watchin.services.action.CREATE";

    private static final String EXTRA_ID = "be.ehb.watchin.services.extra.ID";
    private static final String EXTRA_RECEIVER = "be.ehb.watchin.services.extra.RECEIVER";

    private static final String TAG = "ContactRestService";
    private static final int MY_SOCKET_TIMEOUT_MS = 5000;

    private static String server = WatchInApp.server;
    private static String path = WatchInApp.path.contacts;

    public static final String JSON_CID = "cid";
    public static final String BUN_CID = "BUN_CID";
    public static final String JSON_PID = "pid";
    public static final String BUN_PID = "BUN_PID";
    public static final String JSON_ID = "id";
    public static final String BUN_ID = "BUN_ID";

    private ResultReceiver resultReceiver;
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error handling");
            Log.e(TAG, error.toString());
            resultReceiver.send(ContactResultReceiver.ERROR_RECEIVING,null);
        }
    };


    public ContactRestService() {
        super("ContactRestService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionGetByID(Context context, int ID, ContactResultReceiver receiver) {
        Intent intent = new Intent(context, ContactRestService.class);
        intent.setAction(ACTION_GETBYID);
        intent.putExtra(EXTRA_ID, ID);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }

    public static void startActionGetAll(Context context,ContactResultReceiver receiver) {
        Intent intent = new Intent(context, ContactRestService.class);
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
            }
        }
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

    private void addToQueue(JsonRequest request)
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
                bundle.putInt(BUN_CID,jsonObject.getInt(JSON_CID));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d(TAG,bundle.toString());
            resultReceiver.send(ContactResultReceiver.RESULT_ONE,bundle);
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
                    bundle.putInt(BUN_CID,jsonObject.getInt(JSON_CID));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG,bundle.toString());
                resultReceiver.send(ContactResultReceiver.RESULT_ONE,bundle);
            }
        }
    };
}
