package stuts.com.mycontacts;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

public class ContactListActivity extends ListActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    private GoogleApiClient mGoogleClient;

    private ContactListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        ListView list = (ListView) findViewById(android.R.id.list);
        mAdapter = new ContactListAdapter(this, mGoogleClient);
        list.setAdapter(mAdapter);

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

        mAdapter.getNextPage();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(S.TAG, "suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(S.TAG, "failed" + connectionResult.toString());
        Intent i = new Intent();
        i.setClass(this, LoginActivity.class);

        startActivity(i);
        finish();
    }

}
