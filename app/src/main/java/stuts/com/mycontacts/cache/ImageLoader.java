package stuts.com.mycontacts.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import stuts.com.mycontacts.S;
import stuts.com.mycontacts.data.StutsContact;

/**
 * Created by feriodice on 18/08/14.
 */
public class ImageLoader {

    private class ImageLoaderTask extends AsyncTask<StutsContact, StutsContact, Void> {

        @Override
        protected Void doInBackground(StutsContact... params) {
            StutsContact contact = params[0];
            try {
                Drawable drawable = loadFromFile(contact.imageUrl);

                if (drawable != null) {
                    mLoadingUrlState.put(contact.imageUrl, false);
                    return null;
                }

                byte[] imageDownload = downloadImageFrom(contact.imageUrl);

                if (imageDownload != null) saveOnCache(contact.imageUrl, imageDownload);

                mLoadingUrlState.put(contact.imageUrl, false);
                return null;
            } catch (Exception e) {
                mLoadingUrlState.put(contact.imageUrl, false);
                Log.e(S.TAG, "Error downloading");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void obj) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private BaseAdapter mAdapter;
    private Context mContext;

    private Map<String, Boolean> mLoadingUrlState = new HashMap<String, Boolean>();
    private ImageCache mMemoryCache = new ImageCache();

    public ImageLoader(BaseAdapter adapter, Context context) {
        mAdapter = adapter;
        mContext = context;
    }

    public Drawable getImage(StutsContact contact) {
        if (contact.imageUrl == null) return null;

        Drawable drawable = mMemoryCache.get(contact.imageUrl);

        if (drawable != null) return drawable;

        if ( mLoadingUrlState.get(contact.imageUrl) == Boolean.TRUE) return null;

        mLoadingUrlState.put(contact.imageUrl, true);

        StutsContact[] param = new StutsContact[] {contact};
        new ImageLoaderTask().execute(param);

        return null;
    }

    private Drawable loadFromFile(String url) {
        File f = getFileName(url);

        if (!f.exists()) return null;

        FileInputStream in = null;

        try {
            in = new FileInputStream(f);

            long len = f.length();
            byte[] bytes = new byte[(int) len];
            in.read(bytes);

            ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
            byte[] b =  (byte[]) objIn.readObject();

            ByteArrayInputStream in2 = new ByteArrayInputStream(b);
            Drawable drawable = Drawable.createFromStream(in2, url);

            mMemoryCache.put(url, drawable);

            return drawable;
        } catch (Exception e) {
            Log.e(S.TAG, "could not read cache", e);
            return null;
        } finally {
            silentClose(in);
        }
    }

    private byte[] downloadImageFrom(String imageUrl)  {
        InputStream input = null;
        try {
            java.net.URL url = new java.net.URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            return getBytes(input);

        } catch (Exception e) {
            Log.e(S.TAG, "could not download image");
        } finally {
            silentClose(input);
        }
        return null;
    }


    public void saveOnCache(String url, byte[] obj) {
        File f = getFileName(url);

        if (f == null) return;

        ObjectOutputStream objOut = null;
        FileOutputStream out = null;

        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            objOut.flush();

            out = new FileOutputStream(f);
            out.write(byteOut.toByteArray());
            out.flush();

            ByteArrayInputStream in2 = new ByteArrayInputStream(obj);
            Drawable drawable = Drawable.createFromStream(in2, url);

            mMemoryCache.put(url, drawable);

            Log.d(S.TAG, "wrote cache " + f.getAbsolutePath());
        } catch (Exception e) {
            Log.e(S.TAG, "exception while saving thumbnail", e);
        } finally {
            silentClose(objOut);
            silentClose(out);
        }
    }

    private File getFileName(String url) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
            md.update(url.getBytes("UTF-8"));
            byte[] digest = md.digest();

            StringBuffer buf = new StringBuffer();

            for (int i = 0; i < digest.length; i++) {
                buf.append(Integer.toHexString(0xff & digest[i]));
            }

            File f = new File(mContext.getCacheDir(), buf.toString());

            Log.d(S.TAG, "cache path: " + f.getAbsolutePath());

            return f;
        } catch (Exception e) {
            Log.e(S.TAG, "could not digest", e);
            return null;
        }
    }

    private void silentClose(OutputStream out) {
        try {
            if (out != null) out.close();
        } catch (IOException e) {
            Log.e(S.TAG, "could not close stream");
        }
    }

    private void silentClose(InputStream in) {
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            Log.e(S.TAG, "could not close stream", e);
        }
    }

    private byte[] getBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (true) {
            byte[] data = new byte[1024];
            int read = in.read(data);

            if (read == -1) break;

            out.write(data, 0, read);
        }

        return out.toByteArray();
    }

}
