package be.ehb.watchin.activities;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

import be.ehb.watchin.R;
import be.ehb.watchin.WatchInApp;
import be.ehb.watchin.model.Person;
import be.ehb.watchin.services.PersonDAO.CheckEmailReceiver;

public class LoginActivity extends AppCompatActivity implements CheckEmailReceiver.ResultCallback {

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (((WatchInApp) getApplication()).isLoggedIn())
        {
            Intent intent = new Intent(this, WatchInMain.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences settings = getSharedPreferences(WatchInMain.PREFS_NAME, 0);
        String email = settings.getString(WatchInMain.PREFS_EMAIL,"");
        EditText edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtEmail.setText(email);
    }

    public void onClickRegister(View view)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity( intent);
    }

    public void onClickLogin(View view)
    {
        progress = new ProgressDialog(this);
        progress.setTitle("Logging in");
        progress.setMessage("Wait while checking email...");
        progress.show();

        EditText edtEmail = (EditText) findViewById(R.id.edtEmail);
        String email = String.valueOf(edtEmail.getText());

        if (!isValidEmail(email))
        {
            edtEmail.setError("Not a valid email");
            return;
        }

        CheckEmailReceiver checkEmailReceiver = new CheckEmailReceiver();
        checkEmailReceiver.setResultCallback(this);
        checkEmailReceiver.checkEmail(this,email);

    }

    private boolean isValidEmail(String email)
    {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    @Override
    public void found(int id, String email) {
        progress.dismiss();
        ((WatchInApp) this.getApplication()).MyID(id);
        ((WatchInApp) this.getApplication()).MyEmail(email);
        ((WatchInApp) this.getApplication()).Login();

        SharedPreferences settings = getSharedPreferences(WatchInMain.PREFS_NAME, 0);
        SharedPreferences.Editor edit = settings.edit();

        edit.putInt(WatchInMain.PREFS_ID,id);
        edit.putString(WatchInMain.PREFS_EMAIL,email);
        edit.putBoolean(WatchInMain.PREFS_LOGIN,true);

        edit.commit();


        Toast toast = Toast.makeText(this,"Login succesfull",Toast.LENGTH_SHORT);
        toast.show();
        Intent intent = new Intent(this, WatchInMain.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void notFound() {
        progress.dismiss();
        EditText edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtEmail.setError("Email not registered");
    }
}
