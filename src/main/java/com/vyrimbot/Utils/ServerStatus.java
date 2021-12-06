package com.vyrimbot.Utils;

import com.vyrimbot.Utils.Json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ServerStatus {

    public static boolean checkOnline(String ip) {
        JSONObject json = null;
        try {
            json = new JSONObject(IOUtils.toString(new URL("https://api.mcsrvstat.us/2/"+ip), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (json.has("online") && Boolean.parseBoolean(json.get("online").toString()));
    }

    public static String checkCount(String ip) {
        JSONObject json = null;
        try {
            json = new JSONObject(IOUtils.toString(new URL("https://api.mcsrvstat.us/2/"+ip), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (json.has("players") ? String.valueOf(json.getJSONObject("players").get("online").toString()) : "0");
    }

    public static String checkMaxOnline(String ip) {
        JSONObject json = null;
        try {
            json = new JSONObject(IOUtils.toString(new URL("https://api.mcsrvstat.us/2/"+ip), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (json.has("players") ? String.valueOf(json.getJSONObject("players").get("max").toString()) : "0");
    }
}
