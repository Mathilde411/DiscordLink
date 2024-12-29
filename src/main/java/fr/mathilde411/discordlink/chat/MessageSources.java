package fr.mathilde411.discordlink.chat;

import java.awt.*;


public enum MessageSources {
    DISCORD(new Color(0x5865F2), "Discord"),
    SERVER(new Color(0x00AA00), "Server"),
    MINECRAFT(null, "");

    private final Color color;
    private final String name;

    MessageSources(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    public Color color() {
        return color;
    }

    public String getName() {
        return name;
    }
}
