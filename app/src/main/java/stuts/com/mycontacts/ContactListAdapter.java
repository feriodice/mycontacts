package stuts.com.mycontacts;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import stuts.com.mycontacts.data.StutsContact;

public class ContactListAdapter  extends ArrayAdapter<StutsContact> {
    public static StutsContact LOADING = new StutsContact();
    public static StutsContact ERROR = new StutsContact();

    private GoogleApiClient mGoogleClient;
    private Activity mActivity;
    private boolean mIsWaitingResponse = false;
    private boolean mIsFinished = false;

    private String mNextPageToken = null;

    private ResultCallback<People.LoadPeopleResult> mResultCallback = new ResultCallback<People.LoadPeopleResult>() {
        @Override
        public void onResult(People.LoadPeopleResult loadPeopleResult) {
            Log.d(S.TAG, "contact callback");
            mIsWaitingResponse = false;
            mNextPageToken = loadPeopleResult.getNextPageToken();

            if (loadPeopleResult.getPersonBuffer().getCount() == 0) {
                remove(LOADING);
            }


            for (Person person : loadPeopleResult.getPersonBuffer()) {
                StutsContact contact = new StutsContact();
                contact.name = person.getDisplayName();
                contact.nickname = person.getNickname();

                insert(contact, getCount() - 1);
            }

            loadPeopleResult.getPersonBuffer().close();

            if (mNextPageToken == null) {
                remove(LOADING);
                mIsFinished = true;
            }
        }
    };

    private final View.OnClickListener mErrorClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            remove(ERROR);
            add(LOADING);
            init();
        }
    };

    public ContactListAdapter(Activity activity, GoogleApiClient apiClient) {
        super(activity, R.layout.row_loading);

        mGoogleClient = apiClient;
        mActivity = activity;

        add(LOADING);
    }

    public void init() {
        if (!mGoogleClient.isConnected() || mIsWaitingResponse || mIsFinished) return;

        mIsWaitingResponse = true;
        Plus.PeopleApi.loadVisible(mGoogleClient, mNextPageToken)
                .setResultCallback(mResultCallback);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StutsContact contact = getItem(position);

        if (contact == LOADING) return getLoadingView();

        if (contact == ERROR) return getErrorView();

        if (convertView == null || convertView.getTag() == LOADING || convertView.getTag() == ERROR) {
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_contact, null);
        }

        TextView streetText = (TextView) convertView.findViewById(R.id.name);
        streetText.setText(contact.name );

        return convertView;
    }

    private View getLoadingView() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_loading, null);
        view.setTag(LOADING);
        return view;
    }

    private View getErrorView() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_error, null);
        Button retryButton = (Button) view.findViewById(R.id.btRetry);
        retryButton.setOnClickListener(mErrorClickListener);
        view.setTag(ERROR);
        return view;
    }
}
