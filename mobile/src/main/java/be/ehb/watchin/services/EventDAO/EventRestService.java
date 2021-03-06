package be.ehb.watchin.services.EventDAO;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.builders.EventBuilder;
import be.ehb.watchin.model.Event;
import be.ehb.watchin.model.Person;

/**
 * Created by davy.van.belle on 14/06/2016.
 */
public class EventRestService extends IntentService {

    private static final String ACTION_GETBYID = "be.ehb.watchin.services.action.GET";
    private static final String ACTION_GETALL = "be.ehb.watchin.services.action.GETALL";
    private static final String ACTION_CREATE = "be.ehb.watchin.services.action.CREATE";

    private static final String EXTRA_ID = "be.ehb.watchin.services.extra.ID";
    private static final String EXTRA_RECEIVER = "be.ehb.watchin.services.extra.RECEIVER";

    private static final String TAG = "EventRestService";
    private static final int MY_SOCKET_TIMEOUT_MS = 5000;

    public static final String BUN_EVENT = "BUNDLE_EVENT";
    public static final String BUN_EVENT_LIST = "BUNDLE_EVENT_LIST";

    private static String server = WatchInApp.server;
    private static String path = WatchInApp.path.events;

    private static final String JSON_NAME = "name";
    private static final String JSON_LOC = "location";
    private static final String JSON_START_DATE = "startTime";
    private static final String JSON_END_DATE = "endTime";
    private static final String JSON_ID = "id";
    private static final String JSON_UUID = "uuid";

    private ResultReceiver resultReceiver;
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error handling");
            Log.e(TAG, error.toString());
            resultReceiver.send(EventResultReceiver.ERROR_RECEIVING,null);
        }
    };

    public EventRestService() {
        super("EventRestService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionGetByID(Context context, int ID, EventResultReceiver receiver) {
        Intent intent = new Intent(context, EventRestService.class);
        intent.setAction(ACTION_GETBYID);
        intent.putExtra(EXTRA_ID, ID);
        intent.putExtra(EXTRA_RECEIVER,receiver);
        context.startService(intent);
    }

    public static void startActionGetAll(Context context,EventResultReceiver receiver) {
        Intent intent = new Intent(context, EventRestService.class);
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
            bundle.putSerializable(BUN_EVENT,jsonToEvent(jsonObject));
            resultReceiver.send(EventResultReceiver.RESULT_ONE,bundle);
        }
    };


    private Response.Listener<JSONArray> jsonGetAllResponse = new Response.Listener<JSONArray>(){
        @Override
        public void onResponse(JSONArray response) {
            ArrayList<Event> eventList = new ArrayList<>();
            for (int i = 0; i< response.length(); i++){
                JSONObject jsonObject = null;
                try {
                    jsonObject = response.getJSONObject(i);
                    eventList.add(jsonToEvent(jsonObject));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Bundle  bundle = new Bundle();
            bundle.putSerializable(BUN_EVENT_LIST,eventList);
            resultReceiver.send(EventResultReceiver.RESULT_ALL,bundle);
        }
    };
    private Event jsonToEvent(JSONObject jsonObject) {
        Date start_date = null;
        Date end_date = null;
        Event event = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd,HH:00");
            start_date = sdf.parse(jsonObject.getString(JSON_START_DATE));
            end_date = sdf.parse(jsonObject.getString(JSON_END_DATE));

            event = new EventBuilder()
                    .ID(jsonObject.getInt(JSON_ID))
                    .Name(jsonObject.getString(JSON_NAME))
                    .Location(jsonObject.getString(JSON_LOC))
                    .StartTime(start_date)
                    .EndTime(end_date)
                    .UUID(UUID.fromString(jsonObject.getString(JSON_UUID)))
                    .create();

            Log.d(TAG,event.toString());
            return event;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
