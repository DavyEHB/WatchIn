package be.ehb.watchin.services.PersonDAO;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import be.ehb.watchin.model.Person;


/**
 * Created by davy.van.belle on 2/06/2016.
 */
public class CheckEmailReceiver extends ResultReceiver{

    public static final int EMAIL_OK = 1;
    public static final int EMAIL_NOT_FOUND = 0;

    private ResultCallback resultCallback;

    private static final String TAG = "CheckEmailReceiver";
    private static Creator CREATOR;

    public interface ResultCallback {
        void found(int id, String email);

        void notFound();
    }


    private CheckEmailReceiver() {
        super(new Handler());
    }

    public CheckEmailReceiver(ResultCallback callback) {
        super(new Handler());
        resultCallback = callback;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCallback != null) {
            if (EMAIL_OK == resultCode) {
                //Call result ok function
                resultCallback.found(resultData.getInt(PersonRestService.BUN_ID),resultData.getString(PersonRestService.BUN_EMAIL));
            } else if (EMAIL_NOT_FOUND == resultCode) {
                //Call no valid email address
                resultCallback.notFound();
            }
        }
    }

    public void checkEmail(Context context, String email)
    {
        PersonRestService.startActionCheckEmail(context,email,this);
    }

}
