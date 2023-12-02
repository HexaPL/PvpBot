package com.github.hexa.pvpbot.events;

import com.github.hexa.pvpbot.Bot;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PropertySetEvent extends Event implements Cancellable {

    private String property;
    private static final HandlerList handlers = new HandlerList();


    private Bot bot;
    private Object value;
    private boolean isCancelled;

    public PropertySetEvent(Bot bot, String property, Object value) {
        this.bot = bot;
        this.property = property;
        this.value = value;
        this.isCancelled = false;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Bot getBot() {
        return bot;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}
