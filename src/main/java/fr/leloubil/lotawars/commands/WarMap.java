package fr.leloubil.lotawars.commands;

import fr.leloubil.lotawars.LotaWars;
import fr.leloubil.lotawars.Map;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.UUID;

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
            case "setup":
                return setup(args);
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

    private boolean setup(String[] args) {
        if(args.length != 2){
            p.sendMessage("Usage : /warmap setup <NomMap>");
            return false;
        }
        String mapName = args[1];
        if(LotaWars.getMaplist().containsKey(mapName)){
            p.sendMessage("Cette map existe déja !");
            return false;
        }
        Map m = new Map(mapName);
        setUpMap.put(p.getUniqueId(),new AbstractMap.SimpleEntry<>(m,0));
        p.sendMessage("Vas a l'emplacement du briquet et fais /continue !");
        return true;
    }

    public static void doStep(Player p){
        Integer i = setUpMap.get(p.getUniqueId()).getValue();
        if( i == 0){
            doBriq(p);
        }
        else if(i < 5){
            doRed(p,i);
        }
        else if(i < 9){
            doBlue(p,i);
        }
        else{
            doGold(p,i);
        }
    }

    private static void doGold(Player p, Integer i) {
        Map m = getM(p);
        if(i == 9){
            val loc = lastClick.get(p);
            m.setGoldOne(loc);
            p.sendMessage("clique sur deuxieme block pour le gold et fais /continue !");
        }
        else if(i == 10){
            val loc = lastClick.get(p);
            m.setGoldTwo(loc);
            p.sendMessage("Voila, t'a fini !");
        }
        upStep(p);
        setM(m,p);
    }

    private static void doBlue(Player p, Integer i) {
            Map m = getM(p);
        switch (i) {
            case 5: {
                val loc = p.getLocation();
                m.setBlueSpawn(loc);
                p.sendMessage("Clique droit sur le spawn du pnj1 et fais /continue !");
                break;
            }
            case 6: {
                val loc = lastClick.get(p);
                m.setPnj1BSpawn(loc);
                p.sendMessage("Clique droit sur le spawn du pnj2 et fais /continue !");
                break;
            }
            case 7: {
                val loc = lastClick.get(p);
                m.setPnj2BSpawn(loc);
                p.sendMessage("Clique droit sur le spawn du pnj3 et fais /continue !");
                break;
            }
            case 8: {
                val loc = lastClick.get(p);
                m.setPnj3BSpawn(loc);
                p.sendMessage("Vas premier block pour le gold et fais /continue !");
                break;
            }
        }
            upStep(p);
            setM(m,p);
    }

    private static void doBriq(Player p) {
        Map m = getM(p);
        Location l = p.getLocation().clone().subtract(0,1,0);
        m.setBriquetSpawn(l);
        p.sendMessage("OK, maintenant vas au spawn des joueurs rouges (fais comme si tu venais de spawner)");
        setM(m,p);
        upStep(p);
    }

    private static void setM(Map m, Player p) {
        val entry  = setUpMap.get(p.getUniqueId());
        val i = entry.getValue();
        val oth = new AbstractMap.SimpleEntry<Map,Integer>(m,i);
        LotaWars.getMaplist().put(m.getName(),m);
        try {
            LotaWars.getInstance().saveMaps();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        setUpMap.put(p.getUniqueId(),oth);
    }

    private static Map getM(Player p) {
        return setUpMap.get(p.getUniqueId()).getKey();
    }

    public static final java.util.Map<Player,Location> lastClick = new HashMap<>();

    private static void doRed(Player p, Integer i){
        Map m = getM(p);
        if(i == 1){
            val loc = p.getLocation();
            m.setRedSpawn(loc);
            p.sendMessage("Clique droit sur le spawn du pnj1 et fais /continue !");
        }
        else if(i == 2){
            val loc = lastClick.get(p);
            m.setPnj1RSpawn(loc);
            p.sendMessage("Clique droit sur le spawn du pnj2 et fais /continue !");
        }
        else if(i == 3){
            val loc = lastClick.get(p);
            m.setPnj2RSpawn(loc);
            p.sendMessage("Clique droit sur le spawn du pnj3 et fais /continue !");
        }
        else if(i == 4){
            val loc = lastClick.get(p);
            m.setPnj3RSpawn(loc);
            p.sendMessage("Vas au spawn bleu et fais /continue !");
        }
        upStep(p);
        setM(m,p);
    }


    public static void upStep(Player p){
        val entry = setUpMap.get(p.getUniqueId());
        entry.setValue(entry.getValue() + 1);
        if(entry.getValue() >= 10) {
            setUpMap.remove(p.getUniqueId());
            try {
                LotaWars.getInstance().saveMaps();
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                p.sendMessage("Une erreure est survenue.. déso bruh");
            }
        }
        else setUpMap.replace(p.getUniqueId(),entry);
    }

    public static java.util.Map<UUID, AbstractMap.SimpleEntry<Map,Integer>> setUpMap = new HashMap<>();

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
                p.sendMessage("/warmap <add|edit|list|drap|setup>");
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
