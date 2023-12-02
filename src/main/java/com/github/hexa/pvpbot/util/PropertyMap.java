package com.github.hexa.pvpbot.util;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.events.PropertySetEvent;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PropertyMap {

    private HashMap<String, Object> properties;
    private HashMap<String, Class<?>> types;
    private Bot bot;

    public PropertyMap(Bot bot) {
        this.bot = bot;
        properties = new HashMap<>();
        types = new HashMap<>();
    }

    public void set(String property, Object initialValue, Class<?> type) {
        PropertySetEvent event = new PropertySetEvent(bot, property, initialValue);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        properties.put(event.getProperty(), event.getValue());
        types.put(event.getProperty(), type);
    }

    public boolean set(String property, Object value) {
        if (!properties.containsKey(property)) {
            return false;
        }
        PropertySetEvent event = new PropertySetEvent(bot, property, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        properties.put(event.getProperty(), event.getValue());
        return true;
    }

    public boolean set(String property, String value) {
        if (!types.containsKey(property)) {
            return false;
        }
        Class<?> propertyType = types.get(property);
        return set(property, parseString(value, propertyType));
    }

    public Object get(String property) {
        return properties.get(property);
    }

    public int getInt(String property) {
        return (int) get(property);
    }

    public float getFloat(String property) {
        return (float) get(property);
    }

    public String getString(String property) {
        return (String) get(property);
    }

    public boolean getBoolean(String property) {
        return (boolean) get(property);
    }

    public static <T> T parseString(String s, Class<T> clazz) {
        try {
            return clazz.getConstructor(new Class[] {String.class}).newInstance(s);
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
