package stuts.com.mycontacts;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import stuts.com.mycontacts.cache.ImageLoader;
import stuts.com.mycontacts.data.StutsContact;

public class ContactListAdapter  extends ArrayAdapter<StutsContact> {
    public static StutsContact LOADING = new StutsContact();
    public static StutsContact ERROR = new StutsContact();

    private GoogleApiClient mGoogleClient;
    private Activity mActivity;
    private boolean mIsWaitingResponse = false;
    private boolean mIsFinished = false;

    private String mNextPageToken = null;

    private final ImageLoader mImageLoader;


    private ResultCallback<People.LoadPeopleResult> mResultCallback = new ResultCallback<People.LoadPeopleResult>() {
        @Override
        public void onResult(People.LoadPeopleResult loadPeopleResult) {
            Log.d(S.TAG, "contact callback");

            if (!loadPeopleResult.getStatus().isSuccess()) {
                add(ERROR);
                remove(LOADING);
                return;
            }

            mIsWaitingResponse = false;
            mNextPageToken = loadPeopleResult.getNextPageToken();
            Log.d(S.TAG, "contact callback nextPageToken " + mNextPageToken);

            if (loadPeopleResult.getPersonBuffer().getCount() == 0) {
                remove(LOADING);
            }


            for (Person person : loadPeopleResult.getPersonBuffer()) {
                StutsContact contact = new StutsContact();
                contact.name = person.getDisplayName();
                contact.nickname = person.getNickname();
                if (person.hasImage() && person.getImage().getUrl() != null) {
                    contact.imageUrl = person.getImage().getUrl();
                }

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
            getNextPage();
        }
    };

    public ContactListAdapter(Activity activity, GoogleApiClient apiClient) {
        super(activity, R.layout.row_loading);

        mGoogleClient = apiClient;
        mActivity = activity;
        mImageLoader = new ImageLoader(this, activity);

        add(LOADING);
    }

    public void getNextPage() {
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
        streetText.setText(contact.name);

        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        Drawable drawable = mImageLoader.getImage(contact);
        if (drawable != null) {
            image.setImageDrawable(drawable);
            image.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private View getLoadingView() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_loading, null);
        view.setTag(LOADING);

        getNextPage();
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
