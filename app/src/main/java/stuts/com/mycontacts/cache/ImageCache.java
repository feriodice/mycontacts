package stuts.com.mycontacts.cache;

import android.graphics.drawable.Drawable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by feriodice on 20/08/14.
 */
public class ImageCache extends LinkedHashMap<String, Drawable> {
    private final static int MAX_SIZE = 15;

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Drawable> eldest) {
        return size() > MAX_SIZE;
    }
}
