package fr.leloubil.lotawars.commands;

import fr.leloubil.lotawars.Game;
import fr.leloubil.lotawars.LotaWars;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarGame implements CommandExecutor {

    private Player p;
    //Desactivé
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if(!sender.hasPermission("lotawar.admin")) return true;
        p = (Player) sender;
        if (args.length == 0) return error("usagemain");
        switch (args[0]) {
            case "stop":
                return stop(args);
            case "delete":
                return delete(args);
            case "start":
                return start(args);
            case "create":
                return create(args);
            case "addto":
                return addto(args);
            default:
                return error("usagemain");
        }
    }

    private boolean delete(String[] args) {
        if(args.length != 4) return error("usagedelete");
        if(!Game.gameList.containsKey(args[1])) return error("nogamename");
        if(Game.gameList.get(args[1]).isStarted()) return error("gameinprogress");
        Game.gameList.remove(args[1]);
        p.sendMessage("La partie \"" + args[1] + "\" a bien été supprimée.");
        return true;
    }

    private boolean stop(String[] args) {
        if(args.length != 2) return error("usagestop");
        if(!Game.gameList.containsKey(args[1])) return error("nogamename");
        if(!Game.gameList.get(args[1]).isStarted()) return error("gamestopped");
        Game.gameList.get(args[1]).end();
        p.sendMessage("La partie \"" + args[1] + "\" a bien été arrétée");
        return true;
    }

    private boolean addto(String[] args) {
        if(args.length != 4) return error("usageaddto");
        if(!Game.gameList.containsKey(args[1])) return error("nogamename");
        if(Bukkit.getPlayer(args[2]) == null) return error("noplayer");
        if(!args[3].equals("red") && !args[3].equals("blue")) return error("usageaddto");
        Game.gameList.get(args[1]).getRedteam().remove(Bukkit.getPlayer(args[2]));
        Game.gameList.get(args[1]).getBlueteam().remove(Bukkit.getPlayer(args[2]));
        switch (args[3]) {
            case "red":
                Game.gameList.get(args[1]).getRedteam().add(Bukkit.getPlayer(args[2]));
                break;
            case "blue":
                Game.gameList.get(args[1]).getBlueteam().add(Bukkit.getPlayer(args[2]));
                break;
        }
        p.sendMessage("Le joueur " + args[2] + " a bien été ajouté a l'équipe " + args[3] + " de la partie " + args[1]);
        Bukkit.getPlayer(args[2]).sendMessage("Tu as été ajouté a l'équipe " + args[3] + " de la partie " + args[1]);
        return true;
    }

    private boolean create(String[] args) {
        if(args.length != 3) return error("usagecreate");
        if(Game.gameList.containsKey(args[1])) return error("gamenameexists");
        if(!LotaWars.getMaplist().containsKey(args[2])) return error("mapnoexists");
        Game.gameList.put(args[1],new Game(LotaWars.getMaplist().get(args[2]),args[1]));
        p.sendMessage("la partie " + args[1] + " a été créée");
        return true;
    }

    private boolean start(String[] args) {
        if(args.length != 2) return error("usagestart");
        if(!Game.gameList.containsKey(args[1])) return error("nogamename");
        if(Game.gameList.get(args[1]).isStarted()) return error("gameinprogress");
        Game.gameList.get(args[1]).start();
        p.sendMessage("la partie " + args[1] + " a été lancée");
        return true;
    }
    public boolean error(String reason) {
        switch (reason) {
            case "mapnoexists":
                p.sendMessage("Cette map n'existe pas !");
                break;
            case "usagestart":
                p.sendMessage("/wargame start <LobbyName>");
                break;
            case "usagecreate":
                p.sendMessage("/wargame create <LobbyName> <MapName>");
                break;
            case "nogamename":
                p.sendMessage("Ce nom de partie n'existe pas");
                break;
            case "gamenameexists":
                p.sendMessage("Ce nom de partie est déja pris");
                break;
            case "usagemain":
                p.sendMessage("/wargame <create|start|stop|delete|addto>");
                break;
            case "noplayer":
                p.sendMessage("Ce jouer n'est pas connecté !");
                break;
            case "usageaddto":
                p.sendMessage("/wargame addto <LobbyName> <Player> <Team>");
                break;
            case "gameinprogress":
                p.sendMessage("La partie est en cours !");
                break;
            case "gamestopped":
                p.sendMessage("la partie est déja arrétée !");
                break;
            case "usagedel":
                p.sendMessage("/wargame del <LobbyName>");
                break;
            case "usagestop":
                p.sendMessage("/wargame stop <LobbyName>");
                break;
        }
        return false;
    }
}
