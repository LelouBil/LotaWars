package fr.leloubil.lotawars.commands;

import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Continue implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        val player = ((Player) sender);
        if(!WarMap.setUpMap.containsKey(player.getUniqueId()))return false;
        WarMap.doStep(player);
        return true;
    }
}
