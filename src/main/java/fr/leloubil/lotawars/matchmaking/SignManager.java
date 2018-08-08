package fr.leloubil.lotawars.matchmaking;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class SignManager {

    //Format
    // [LotaWar]
    //  nomMap + chiffre
    //  Joueurs/joueurs
    //  Etat

    public static void UpdateSign(Sign sign){
        sign.setLine(0,"[LotaWar]");
        String name = sign.getLine(1);

        Lobby lobby = Lobby.getLobbies().get(name);

        sign.setLine(2,(lobby.isStarted() ? lobby.maxPlayers : lobby.getPlayersIn()) + "/" + lobby.maxPlayers);
        sign.setLine(3,lobby.isStarted() ? "En cours" : "En attente");
        sign.update(true);
        System.out.println("Updated " + name );
        System.out.println("started " + lobby.isStarted());
        System.out.println("in " + lobby.getPlayersIn());
    }

    public static boolean isGameSign(Block b){
        if(b == null) return false;
        if(b.getType() != Material.SIGN && b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) return false;
        return ((Sign) b.getState()).getLine(0).equals("[LotaWar]");
    }

    public static void OnClick(Player p, Sign s){
        if(!s.getLine(0).equals("[LotaWar]")) return;
        String name = s.getLine(1);

        Lobby lobby = Lobby.getLobbySafe(s);
        if(lobby.isStarted()){
            p.sendMessage("La partie a déja commencée !");
            return;
        }

        if(lobby.isFull()){
            p.sendMessage("Il y a déja le maximum de joueurs dans cette partie !");
            return;
        }
        lobby.join(p);
        UpdateSign(s);
    }

    public static HashMap<UUID,String> SignList = new HashMap<>();
}
