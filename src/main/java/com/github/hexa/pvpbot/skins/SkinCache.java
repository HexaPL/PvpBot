package com.github.hexa.pvpbot.skins;

import java.util.HashMap;

public class SkinCache {

    private static HashMap<String, Skin> cache = new HashMap<>();

    public static void save(String name, Skin skin) {
        cache.put(name, skin);
    }

    public static Skin get(String name) {
        return cache.getOrDefault(name, null);
    }

}
