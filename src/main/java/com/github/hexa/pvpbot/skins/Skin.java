package com.github.hexa.pvpbot.skins;

import com.github.hexa.pvpbot.util.JsonUtils;
import com.github.hexa.pvpbot.util.LogUtils;
import com.github.hexa.pvpbot.v1_16_R3.SkinNMS;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

public class Skin {

    private String texture;
    private String signature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public static void setForGameProfile(GameProfile gameProfile, Skin skin) {
        PropertyMap propertyMap = gameProfile.getProperties();
        if (propertyMap.containsKey("textures")) {
            Property property = propertyMap.get("textures").iterator().next();
            propertyMap.remove("textures", property);
        }
        propertyMap.put("textures", new Property("textures", skin.getTexture(), skin.getSignature()));
    }

    public String getTexture() {
        return this.texture;
    }

    public String getSignature() {
        return this.signature;
    }

    public static Skin getFromPlayer(Player player) {
        GameProfile gameProfile = getGameProfile(player);
        Property property = gameProfile.getProperties().get("textures").iterator().next();
        if (property == null) {
            LogUtils.warn("Player " + player.getName() + " does not have skin properties.");
            LogUtils.warn("Probable causes: server is in offline mode or player is a NPC.");
            return null;
        }
        return new Skin(property.getValue(), property.getSignature());
    }

    public static Skin getFromPlayer(String name) throws IOException, ParseException {
        Player player = Bukkit.getOfflinePlayer(name).getPlayer();
        if (player != null) {
            return getFromUUID(player.getUniqueId());
        } else {
            return getFromUUID(getPlayerUUID(name));
        }
    }

    public static Skin getFromUUID(UUID uuid) throws IOException, ParseException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false");
        URLConnection urlConnection = getURLConnection(url);

        String json = new Scanner(urlConnection.getInputStream(), "UTF-8").useDelimiter("\\A").next();
        JSONParser parser = new JSONParser();
        JSONObject jsonObj = (JSONObject) parser.parse(json);

        JSONObject properties = (JSONObject) ((JSONArray) jsonObj.get("properties")).iterator().next();
        String value = (String) properties.get("value");
        String signature = properties.containsKey("signature") ? (String) properties.get("signature") : null;
        return new Skin(value, signature);
    }

    public static UUID getPlayerUUID(String name) throws IOException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        InputStreamReader stream = new InputStreamReader(url.openStream());
        BufferedReader in = new BufferedReader(stream);
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = in.readLine()) != null){
            result.append(line);
        }
        if (result.toString().equals("")) {
            // Username does not exist
            return null;
        }
        JsonObject json = JsonUtils.parseStream(stream);
        return UUID.fromString(json.get("id").getAsString());

    }

    public static GameProfile getGameProfile(Player player) {
        return SkinNMS.getGameProfile(player);
    }



    private static URLConnection getURLConnection(URL url) throws IOException {
        URLConnection uc = url.openConnection();
        uc.setUseCaches(false);
        uc.setDefaultUseCaches(false);
        uc.addRequestProperty("User-Agent", "Mozilla/5.0");
        uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        uc.addRequestProperty("Pragma", "no-cache");
        return uc;
    }

}
