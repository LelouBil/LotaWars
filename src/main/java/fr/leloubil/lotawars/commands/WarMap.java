package fr.leloubil.lotawars.commands;

import fr.leloubil.lotawars.LotaWars;
import fr.leloubil.lotawars.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarMap implements CommandExecutor {
    private Player p;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        p = (Player) sender;
        if(!p.hasPermission("lotawar.admin"))return error("permission");
        if (args.length == 0) return error("usagemain");
        switch (args[0]) {
            case "add":
                return addmap(args);
            case "waitzone":
                LotaWars.setWaitZone(p.getLocation());
                p.sendMessage("WaitZone changée !");
                return true;
            case "edit":
                return editMap(args);
            case "delete":
                return deleteMap(args);
            case "list":
                p.sendMessage("Maps : ");
                LotaWars.getMaplist().forEach((s, v) -> p.sendMessage(" - " + s));
                return true;
            case "drap":
                if (args.length != 4) return error("usagedrap");
                if (!LotaWars.maplist.containsKey(args[2])) return error("mapnoexists");
                if (!args[3].equals("red") && !args[3].equals("blue")) return error("usagedadd");
                if(args[1].equals("add")) {
                    Map.getEditList().put(p.getUniqueId(), args[2] + "#" + "drapadd" + "#" + args[3]);
                    p.sendMessage("Clique sur le block que tu veux ajouter au drapeau");
                    return true;
                }
                else if(args[1].equals("del")) {
                    Map.getEditList().put(p.getUniqueId(), args[2] + "#" + "drapdel" + "#" + args[3]);
                    p.sendMessage("Clique sur le block que tu veux enlever au drapeau");
                    return true;
                }
                else {
                    return error("usagedrap");
                }
            default:
                return error("usagemain");
        }
    }
    private boolean deleteMap(String[] args) {
        if(args.length > 3) return error("usagedelete");
        if(args.length == 2){
            if(!LotaWars.getMaplist().containsKey(args[1])) return error("nomapexists");
            p.sendMessage("Pour confirmer, fais la commande : /lotawars delete " + args[1] + " confirm" );
            return true;
        }
        if(!args[2].equals("confirm")) return error("usagedelete");
        LotaWars.removeMaps(args[1]);
        p.sendMessage("La map " + args[1] + " a bien été supprimée.");
        return true;
    }
    private boolean editMap(String[] args) {
        if(args.length < 3) return error("usageedit");
        if(args.length > 4) return error("usageedit");
        Map.getEditList().remove(p.getUniqueId());
        if(!LotaWars.getMaplist().containsKey(args[1])) return error("mapnoexists");

        if(args[2].equals("briq")){
            if(args.length != 3) return error("usageedit");
            Map.getEditList().put(p.getUniqueId(),args[1] + "#" + args[2]);
            p.sendMessage("Clique droit sur le " + args[2] + " de la map " + args[1]);
            return true;
        }
        if(args[2].equals("goldone")){
            if(args.length != 3) return error("usageedit");
            Map.getEditList().put(p.getUniqueId(),args[1] + "#" + args[2]);
            p.sendMessage("Clique droit sur le " + args[2] + " de la map " + args[1]);
            return true;
        }
        if(args[2].equals("goldtwo")){
            if(args.length != 3) return error("usageedit");
            Map.getEditList().put(p.getUniqueId(),args[1] + "#" + args[2]);
            p.sendMessage("Clique droit sur le " + args[2] + " de la map " + args[1]);
            return true;
        }
        if(!args[3].equals("blue") && !args[3].equals("red")) return error("usageedit");
        switch (args[2]){
            case "spawn":
                Map.getEditList().put(p.getUniqueId(),args[1] + "#" + args[2]+ "#" + args[3]);
                p.sendMessage("Clique droit sur le " + args[2] +  " " + args[3] + " de la map " + args[1]);
                return true;
            case "pnj1":
                Map.getEditList().put(p.getUniqueId(),args[1] + "#" + args[2]+ "#" + args[3]);
                p.sendMessage("Clique droit sur le " + args[2] +  " " + args[3] + " de la map " + args[1]);
                return true;
            case "pnj2":
                Map.getEditList().put(p.getUniqueId(),args[1] + "#" + args[2]+ "#" + args[3]);
                p.sendMessage("Clique droit sur le " + args[2] +  " " + args[3] + " de la map " + args[1]);
                return true;
            case "pnj3":
                Map.getEditList().put(p.getUniqueId(),args[1] + "#" + args[2]+ "#" + args[3]);
                p.sendMessage("Clique droit sur le " + args[2] +  " " + args[3] + " de la map " + args[1]);
                return true;
        }
        return error("usageedit");
    }

    private Boolean addmap(String[] args) {
        if(args.length != 2) return error("usageadd");
        if(LotaWars.getMaplist().containsKey(args[1])) return error("mapexists");
        Map m = new Map(args[1]);
        LotaWars.getMaplist().put(args[1],m);
        p.sendMessage("La map " + args[1] + " a biens été créée");
        return true;
    }

    public boolean error(String reason) {
        switch (reason) {
            case "mapnoexists":
                p.sendMessage("Cette map n'existe pas !");
                break;
            case "usageadd":
                p.sendMessage("/warmap add <MapName>");
                break;
            case "usageedit":
                p.sendMessage("/warmap edit <MapName> <spawn|pnj1|pnj2|pnj3|briq|goldone|goldtwo> <red|blue>");
                break;
            case "mapexists":
                p.sendMessage("Ce nom de Map existe déja !");
                break;
            case "usagemain":
                p.sendMessage("/warmap <add|edit|list|drap>");
                break;
            case "usagedrap":
                p.sendMessage("/warmap drap <add|del> <MapName> <Team>");
                break;
            case "permission":
                break;
        }
        return false;
    }
}
