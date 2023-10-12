package com.github.hexa.pvpbot.util;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.events.PropertySetEvent;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PropertyMap {

    private HashMap<String, Object> map;
    private HashMap<String, Class<?>> types;
    private Bot bot;

    public PropertyMap(Bot bot) {
        this.bot = bot;
        map = new HashMap<>();
        types = new HashMap<>();
    }

    public void init(String property, Object initialValue, Class<?> type) {
        PropertySetEvent event = new PropertySetEvent(bot, property, initialValue);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        map.put(event.getProperty(), event.getValue());
        types.put(event.getProperty(), type);
    }

    public boolean set(String property, Object value) {
        if (!map.containsKey(property)) {
            return false;
        }
        PropertySetEvent event = new PropertySetEvent(bot, property, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        map.put(event.getProperty(), event.getValue());
        return true;
    }

    public boolean set(String property, String value) {
        if (!types.containsKey(property)) {
            return false;
        }
        Class<?> propertyType = types.get(property);
        Object parsedValue = parseString(value, propertyType);
        return set(property, parsedValue);
    }

    public Object get(String property) {
        return map.get(property);
    }

    public int getInt(String property) {
        return (int) get(property);
    }

    public String getString(String property) {
        return (String) get(property);
    }

    public boolean getBoolean(String property) {
        return (boolean) get(property);
    }

    public static <T> T parseString(String s, Class<T> clazz) {
        try {
            return clazz.getConstructor(new Class[] {String.class }).newInstance(s);
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
