package fr.leloubil.lotawars.commands;

import fr.leloubil.lotawars.Game;
import fr.leloubil.lotawars.Teams;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarDebug implements CommandExecutor {


    private Player p;
    //Desactivé
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if( sender instanceof Player) p = (Player) sender;
        switch (args[0]){
            case "score":
                if(args[2].equals("BLUE")) Game.gameList.get(args[1]).point(Teams.BLUE);
                if(args[2].equals("RED")) Game.gameList.get(args[1]).point(Teams.RED);
                break;
            case "yaw":
                p.getLocation().setYaw(Float.parseFloat(args[1]));
                break;
            case "sound":
                p.playSound(p.getLocation(),Sound.valueOf(args[1]),Float.parseFloat(args[2]),Float.parseFloat(args[3]));
        }
        return true;
    }

}
