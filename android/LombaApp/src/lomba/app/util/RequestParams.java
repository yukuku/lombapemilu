package lomba.app.util;

import android.net.Uri;

import java.util.concurrent.ConcurrentHashMap;

public class RequestParams {
	private ConcurrentHashMap<String, String> urlParams;

	/**
     * Constructs a new RequestParams instance and populate it with multiple initial key/value
     * string param.
     *
     * @param keysAndValues a sequence of keys and values. Objects are automatically converted to
     *                      Strings (including the value {@code null}).
     * @throws IllegalArgumentException if the number of arguments isn't even.
     */
    public RequestParams(Object... keysAndValues) {
        init();
        int len = keysAndValues.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException("Supplied arguments must be even");
        for (int i = 0; i < len; i += 2) {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
    }

    /**
     * Adds a key/value string pair to the request.
     *
     * @param key   the key name for the new param.
     * @param value the value string for the new param.
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");

	        result.append(Uri.encode(entry.getKey()));
	        result.append("=");
	        result.append(Uri.encode(entry.getValue()));
        }

        return result.toString();
    }

    private void init() {
        urlParams = new ConcurrentHashMap<>();
    }
}
