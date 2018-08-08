package fr.leloubil.lotawars;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public enum Teams {

    RED("Les Rouges"),
    BLUE("Les Bleus");
    private final String text;

    Teams( final String s){
        text = s;
    }

    @Override
    public String toString() {
        return text;
    }

    public Teams other(){
        if(this == Teams.BLUE) return Teams.RED;
        else return Teams.BLUE;
    }

    public ChatColor chatColor(){
        if(this == Teams.BLUE) return ChatColor.BLUE;
        else return ChatColor.RED;
    }

    public DyeColor dyeColor(){
        if(this == Teams.BLUE) return DyeColor.BLUE;
        else return DyeColor.RED;
    }

    public Color color(){
        if(this == Teams.BLUE) return Color.BLUE;
        else return Color.RED;
    }

}
