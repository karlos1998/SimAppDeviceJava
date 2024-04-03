package it.letscode.simappdevice;

import org.json.JSONException;
import org.json.JSONObject;

public class VersionManager {

    public static int getVersionCode() {
        return ApplicationContextProvider.getPackageInfo().versionCode;
    }

    public static String getVersionName() {
        return ApplicationContextProvider.getPackageInfo().versionName;
    }

    public static JSONObject getVersionJson() {
        return new JSONObject() {{
            try {
                put("name", getVersionName());
                put("code", getVersionCode());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }};
    }

}
