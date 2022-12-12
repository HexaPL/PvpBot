package com.github.hexa.pvpbot.skins;

import com.github.hexa.pvpbot.util.JsonUtils;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class Skin {

    private String texture;
    private String signature;

    public Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public String getTexture() {
        return this.texture;
    }

    public String getSignature() {
        return this.signature;
    }

    public static Skin getFromPlayer(Player player) {
        return null; // TODO
    }

    public static Skin getFromUUID(UUID uuid) {
        return null;
    }

    public static Skin getFromPlayerName(String name) {
        return null;
    }

    private static UUID getUUID(String name) {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }





    /* TODO
    URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");

    JSONArray properties = (JSONArray) ((JSONObject) obj).get("properties");
    for (int i = 0; i < properties.size(); i++) {
        try {
            JSONObject property = (JSONObject) properties.get(i);
            String name = (String) property.get("name");
            String value = (String) property.get("value");
            String signature = property.containsKey("signature") ? (String) property.get("signature") : null;


            this.name = name;
            this.value = value;
            this.signatur = signature;


        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to apply auth property", e);
        }
    }

     */

}
