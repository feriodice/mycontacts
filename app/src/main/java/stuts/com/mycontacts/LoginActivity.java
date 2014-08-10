package stuts.com.mycontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.a;
import com.google.android.gms.plus.Plus;

import java.util.concurrent.TimeUnit;


public class LoginActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int SIGNIN_REQUEST_CODE = 1;

    private GoogleApiClient mGoogleClient;
    private ConnectionResult mConnectionResult;
    private boolean mIsSignInRequested = false;

    private View.OnClickListener mSignInListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mIsSignInRequested) return;

            mIsSignInRequested = true;
            requestGooglePermission();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(mSignInListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int googleServicesStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (googleServicesStatus != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(googleServicesStatus, this, 0);
            errorDialog.show();
        }
    }

    protected void onStart() {
        super.onStart();
        mGoogleClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleClient.isConnected()) {
            mGoogleClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(S.TAG, "connected");

        Intent i = new Intent();
        i.setClass(this, ContactListActivity.class);

        startActivity(i);
        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(S.TAG, "suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(S.TAG, "failed" + connectionResult.toString());
        mConnectionResult = connectionResult;
    }

    private void requestGooglePermission() {
        Log.d(S.TAG, "resolveSignInError");
        try {
            mConnectionResult.startResolutionForResult(this, SIGNIN_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            mGoogleClient.connect();
        }

    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        Log.d(S.TAG, "resolveSignInError" + requestCode + " " + responseCode);

        if (requestCode != SIGNIN_REQUEST_CODE) return;

        mIsSignInRequested = false;

        if (responseCode == Activity.RESULT_OK) {
            mGoogleClient.connect();
        }
    }
}
