package com.github.hexa.pvpbot.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class JsonUtils {

    public static JsonObject parseURL(URL url) {
        try {
            InputStreamReader reader = new InputStreamReader(url.openStream());
            return parseStream(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonObject parseStream(InputStreamReader stream) {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(stream).getAsJsonObject();
        return object;
    }

}
