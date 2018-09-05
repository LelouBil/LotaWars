package fr.leloubil.lotawars.commands;

import fr.leloubil.lotawars.LotaWars;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class SignUpdate implements CommandExecutor {


    private static final List<String> AFTER = Arrays.asList();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("lotawar.admin")) return false;
        if(args.length != 3){
            sender.sendMessage("usage: /signupdate <x> <y> <z>");
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            World w = LotaWars.getWaitZone().getWorld();
            Block b = w.getBlockAt(x,y,z);
            if(b.getType() != Material.SIGN_POST ||b.getType() != Material.WALL_SIGN){
                sender.sendMessage("Il n'y as pas de panneau a cet endroit !");
                return false;
            }
            Sign s = (Sign) b.getState();
            List<String> lines = Arrays.asList(s.getLines());
            if(lines.containsAll(AFTER)){
                sender.sendMessage("Le panneau a déja été modifié !");
                return false;
            }
            for (int i = 0; i < AFTER.size(); i++) {
                s.setLine(i,AFTER.get(i));
            }
            sender.sendMessage("Panneau modifié !");

        }

        return true;
    }
}
