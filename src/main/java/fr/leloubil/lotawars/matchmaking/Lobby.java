package fr.leloubil.lotawars.matchmaking;

import fr.leloubil.lotawars.Game;
import fr.leloubil.lotawars.LotaWars;
import fr.leloubil.lotawars.Map;
import fr.leloubil.lotawars.WaitRunnable;
import fr.leloubil.lotawars.scoreboard.ScoreboardSign;
import fr.leloubil.minihub.Listeners;
import fr.leloubil.minihub.MiniHub;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.paperspigot.Title;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Lobby extends Game {
    public Lobby(Map map, String name, int maxPlayers, World w, Sign s) {
        super(map.GetNew(w), name);
        ItemMeta meta = bluewool.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Rejoindre les bleus !");
        bluewool.setItemMeta(meta);
        meta.setDisplayName(ChatColor.RED + "Rejoindre les rouges !");
        redwool.setItemMeta(meta);
        LotaWars.info("map : " + map.getName() + ":: name :" + name + " ::: world :" + map.getBlueSpawn().getWorld().getName());
        this.maxPlayers = maxPlayers;
        maxPerTeam = maxPlayers / 2;
        this.waitZone = LotaWars.getWaitZone();
        this.sign = s;
        s.setLine(0,"[LotaWar]");
        s.setLine(1,w.getName());
        s.setLine(2,"0/" + maxPlayers);
        s.setLine(3,"En attente");
        s.update(true);
    }

    public int maxPlayers;

    @Getter
    public Location waitZone;

    @Getter
    private final ArrayList<Player> waiting = new ArrayList<>();


    @Getter
    private Sign sign;


    @Override
    public boolean isLobby(){
        return !isStarted();
    }

    public int getPlayersIn(){
        return waiting.size();
    }

    @Override
    public ArrayList<Player> getPlayers(){
        if(this.isStarted()) return super.getPlayers();
        else return new ArrayList<>(waiting);
    }

    @Getter
    private ItemStack bluewool = new ItemStack(Material.WOOL,1,(short)11);
    @Getter
    private ItemStack redwool = new ItemStack(Material.WOOL,1,(short)14);
    @Getter
    private static ItemStack greywool = new ItemStack(Material.WOOL,1,(short)8);

    static {
        ItemMeta meta = greywool.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Quitter l'équipe");
        greywool.setItemMeta(meta);
    }
    @Getter
    private ArrayList<Player> prered = new ArrayList<>();

    @Getter
    private ArrayList<Player> preblue = new ArrayList<>();

    public void updateTeams(){

        ItemMeta meta = bluewool.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Rejoindre les bleus !" + " (" + preblue.size() + "/" + maxPerTeam + ")");
        bluewool.setItemMeta(meta);
        meta.setDisplayName(ChatColor.RED + "Rejoindre les rouges !" + " (" + prered.size() + "/" + maxPerTeam + ")");
        redwool.setItemMeta(meta);
        waiting.forEach(p -> {
            p.getInventory().setItem(2,getBluewool());
            p.getInventory().setItem(3,getRedwool());
        });
        preblue.forEach(p -> {
            p.getInventory().setItem(2,getBluewool());
            p.getInventory().setItem(3,getRedwool());
        });
        prered.forEach(p -> {
            p.getInventory().setItem(2,getBluewool());
            p.getInventory().setItem(3,getRedwool());
        });
    }

    public void join(Player p){
        p.teleport(waitZone);
        p.setGameMode(GameMode.ADVENTURE);
        setTab(p,ChatColor.GRAY);
        waiting.add(p);
        MiniHub.games.put(p.getUniqueId(),this);
        ScoreboardSign scoreboard = new ScoreboardSign(p,ChatColor.GOLD + "LotaWars");
        scoreboard.create();
        scoreboard.setLine(0,"Lobby : " + this.getName());
        scoreboard.setLine(1,"Joueurs : " + this.waiting.size() + "/" + this.maxPlayers);
        LotaWars.getScoreboardSignHashMap().put(p,scoreboard);
        setTab(p,ChatColor.GRAY);
        MiniHub.giveItems(p);
        p.getInventory().setItem(2,getBluewool());
        p.getInventory().setItem(3,getRedwool());
        p.getInventory().setItem(4,getGreywool());
        updateTeams();
        p.updateInventory();
        Listeners.updateHideShow();
        updateScoreboards();
        if(this.isFull()){
            WaitPre();
        }
    }


    public void WaitPre(){
        broadcastWait(ChatColor.GOLD + "Le lobby est plein !");
        get1_7().forEach(p -> p.sendMessage(ChatColor.GREEN + "La partie commence dans : "));
        WaitRunnable.getWaitRunnables().put(getName(),Bukkit.getScheduler().runTaskTimerAsynchronously(LotaWars.getInstance(),new WaitRunnable(this),0,20));
    }



    public void broadcastWait(Title t){
        getWaiting().forEach(p -> {
            ViaAPI api = Via.getAPI();
            if(api.getPlayerVersion(p.getUniqueId()) < 47){
                //p.sendMessage(t.getTitle()[0]);
                //p.sendMessage(t.getSubtitle()[0]);
            }
            else p.sendTitle(t);
        });
    }

    public void playWait(Sound s){
        synchronized (waiting) {
            waiting.forEach(p -> p.playSound(p.getLocation(), s, 1.0F, 1.0F));
        }
    }

    public ArrayList<Player> get1_7(){
        ArrayList<Player> temp = new ArrayList<>();
        ViaAPI api = Via.getAPI();
        getWaiting().forEach(p -> {
            if(api.getPlayerVersion(p.getUniqueId()) < 47) temp.add(p);
        });
        return temp;
    }
    public void broadcastWait(String s){
        getWaiting().forEach(p -> {
            p.sendMessage(s);
        });
    }


    public void updateScoreboards(){
        waiting.forEach(p -> {
            LotaWars.getScoreboardSignHashMap().get(p).setLine(0,"Lobby : " + this.getName());
            LotaWars.getScoreboardSignHashMap().get(p).setLine(1,"Joueurs : " + this.waiting.size() + "/" + this.maxPlayers);
        });
    }

    @Override
    public void leaveLobby(Player p){
        if(!waiting.contains(p)) return;
        waiting.remove(p);
        prered.remove(p);
        preblue.remove(p);
        updateTeams();
        setTab(p,ChatColor.getByChar(p.getPlayerListName().substring(1,2)));
        LotaWars.getScoreboardSignHashMap().get(p).destroy();
        LotaWars.getScoreboardSignHashMap().remove(p);
        p.getInventory().clear();
        MiniHub.games.remove(p.getUniqueId());
        p.teleport(MiniHub.getLobby());
        MiniHub.giveItems(p);
        Listeners.updateHideShow();
        SignManager.UpdateSign(sign);
        updateScoreboards();
    }

    public void PreStart(){
        LotaWars.info("MAP : " + this.getMap().getName() + " :: " + this.getMap().getBlueSpawn().getWorld());
        redteam.addAll(prered);
        blueteam.addAll(preblue);
        waiting.removeAll(prered);
        waiting.removeAll(preblue);
        Collections.shuffle(waiting);
        if(!waiting.isEmpty()){
            waiting.forEach(p -> {
                if(redteam.size() < blueteam.size()) redteam.add(p);
                else blueteam.add(p);
            });
        }

        fr.leloubil.lotawars.Game.gameList.put(this.getName(),this);
        redteam.forEach(w -> {
            LotaWars.getScoreboardSignHashMap().get(w).destroy();
        });
        blueteam.forEach(w -> {
            LotaWars.getScoreboardSignHashMap().get(w).destroy();
        });
        prered.clear();
        preblue.clear();
        waiting.clear();
        loadChunks();
        start();
    }

    private void loadChunks() {
        getMap().getBlueSpawn().getChunk().load();
        getMap().getRedSpawn().getChunk().load();
    }


    public boolean isFull(){
        return maxPlayers == waiting.size();
    }
    @Getter
    public static HashMap<String,Lobby> lobbies = new HashMap<>();

    public static Lobby getLobbySafe(Sign s){
        if(lobbies.containsKey(s.getLine(1))) return lobbies.get(s.getLine(1));
        String map = s.getLine(1).substring(0,s.getLine(1).length() - 1);
        String world = s.getLine(1);
        int maxPlayers = Integer.parseInt(s.getLine(2).split("/")[1]);
        Lobby l = new Lobby(LotaWars.getMaplist().get(map),world,maxPlayers,Bukkit.getWorld(world),s);
        lobbies.put(world,l);
        return l;
    }

    public static int getFree(String mapName){
        int free = 0;
        for (String s : lobbies.keySet()) {
            if(s.startsWith(mapName)){
                free++;
            }
        }
        return free + 1;
    }


    public void reset() {
        updateScoreboards();
        waiting.forEach(p -> {
            LotaWars.getScoreboardSignHashMap().get(p).destroy();
            LotaWars.getScoreboardSignHashMap().remove(p);
        });
        waiting.clear();
        Listeners.updateHideShow();
        SignManager.UpdateSign(sign);

    }
}
