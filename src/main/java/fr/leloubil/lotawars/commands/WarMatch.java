package fr.leloubil.lotawars.commands;

import fr.leloubil.lotawars.LotaWars;
import fr.leloubil.lotawars.matchmaking.Lobby;
import fr.leloubil.lotawars.matchmaking.SignManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarMatch implements CommandExecutor {

    private Player p;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if( sender instanceof Player) p = (Player) sender;
        if(!p.hasPermission("lotawar.admin"))return error("permission");
        if(args.length == 0) return error("matchusage");
        switch (args[0]){
            case "create":
                return create(args);
            case "delete":
                return delete(args);
            case "list":
                return list(args);
            default:
                return error("matchusage");
        }
    }


    private boolean create(String[] args) {
        //Format = UUID->(mapname#maxPlayers#WaitZone)
        if(args.length != 3) return error("createusage");
        String mapname = args[1];
        if(!LotaWars.getMaplist().containsKey(mapname)) return error("mapnoexist");
        Integer maxPlayers;
        try {
            maxPlayers = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e){
            return error("createusage");
        }
        String data = mapname + "#" + maxPlayers.toString();
        SignManager.SignList.put(p.getUniqueId(),data);
        return true;
    }

    private boolean delete(String[] args) {
        if(args.length != 2) return error("deleteusage");
        String lobbyname = args[1];
        if(!Lobby.getLobbies().containsKey(lobbyname)) return error("matchnoexists");
        Lobby.getLobbies().remove(lobbyname);
        return true;
    }

    private boolean list(String[] args){
        p.sendMessage("Lobbies :");
        Lobby.getLobbies().values().forEach(l -> p.sendMessage(" - " + l.getName() + " : " + l.getPlayersIn() + "/" + l.maxPlayers ));
        return true;
    }

    public boolean error(String reason) {
        switch (reason) {
            case "mapnoexist":
                p.sendMessage("Cette map n'existe pas !");
                break;
            case "createusage":
                p.sendMessage("/warmatch create MapName maxPlayers");
                break;
           case "mapprob":
                p.sendMessage("Il y as un probleme avec la structure des maps.");
                break;
            case "matchnoexists":
                p.sendMessage("Ce lobby n'existe pas !");
                break;
            case "deleteusage":
                p.sendMessage("/warmatch delete MatchName");
                break;
            case "matchusage":
                p.sendMessage("/warmatch <create|delete|list>");
                break;
            case "permission":
                break;
        }
        return false;
    }
}
