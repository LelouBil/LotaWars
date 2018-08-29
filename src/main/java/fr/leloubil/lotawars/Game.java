package fr.leloubil.lotawars;

import com.sun.media.sound.InvalidFormatException;
import fr.leloubil.lotawars.matchmaking.Lobby;
import fr.leloubil.lotawars.scoreboard.LotaPNJ;
import fr.leloubil.lotawars.scoreboard.ScoreboardSign;
import fr.leloubil.minihub.Listeners;
import fr.leloubil.minihub.MiniHub;
import lombok.Getter;
import lombok.Setter;
import net.lotary.lotaryapi.utils.CustomPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.MerchantRecipe;
import net.minecraft.server.v1_8_R3.MerchantRecipeList;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.github.paperspigot.Title;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class Game extends fr.leloubil.minihub.interfaces.Game {


    public static final String[] pnjNames = {"§b➡ §7Forgeron §b⬅","§b➡ §7Armurier §b⬅","§b➡ §7Alchimiste §b⬅"};

    public static final long perPoints = 15;

    public static final long perWin = 50;

    @Getter
    private final String name;

    public static int maxScore = 3;

    @Getter
    public HashMap<Player,Integer> kills = new HashMap<>();
    @Getter
    public HashMap<Player,Integer> deaths = new HashMap<>();


    @Getter
    boolean started = false;

    @Getter
    protected   ArrayList<Player> blueteam = new ArrayList<>();
    @Getter
    protected   ArrayList<Player> redteam = new ArrayList<>();


    @Getter
    private final Map map;

    @Getter
    private Player flintHolder;

    private String reds;

    private String blues;

    private BukkitTask goldRunnable;
    private BukkitTask speedRunnable;

    private ArrayList<Villager> villagers = new ArrayList<>();


    @Getter
    private int blueScore;

    @Getter @Setter
    protected int maxPerTeam;

    @Getter
    private int redScore;

    public Game(Map map, String name) {
        LotaWars.info("GAMEMAPWORLD : " + map.getBlueSpawn().getWorld());
        this.map = map;
        this.name = name;
        reds = name + "Rouges";
        blues = name + "Bleus";
    }

    public void makeScoreboard(){
        // Lotawar
        // Map : Nomdelamap
        // Briquet : Machin||-----
        // Rouges : 0
        // Bleus : 0
        Consumer<? super Player> consumer = p -> {
            ScoreboardSign sb;
            if(LotaWars.getScoreboardSignHashMap().containsKey(p)) sb = LotaWars.getScoreboardSignHashMap().get(p);
            else{
                if(LotaWars.getScoreboardSignHashMap().containsKey(p)) sb = LotaWars.getScoreboardSignHashMap().get(p);
                else sb = new ScoreboardSign(p,ChatColor.GOLD + "LOTAWARS");
                LotaWars.getScoreboardSignHashMap().replace(p,sb);
            }
            sb.create();
            sb.setLine(0,ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "            ");
            sb.setLine(1,"            ");
            sb.setLine(2,ChatColor.DARK_AQUA + "Map: " + map.getName());
            if(flintHolder == null) sb.setLine(3,ChatColor.GOLD + "Briquet : " + ChatColor.DARK_GRAY + "------");
            else sb.setLine(3,ChatColor.GOLD + "Briquet : " + flintHolder.getMetadata("colorname").get(0).asString());
            sb.setLine(4,"            ");
            sb.setLine(5,ChatColor.RED + "Rouges : " + redScore);
            sb.setLine(6,ChatColor.BLUE + "Bleus : " + blueScore);
            sb.setLine(7,ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "            ");
            sb.setLine(8,ChatColor.YELLOW + "play.lotary.net");
        };

        blueteam.forEach(consumer);
        redteam.forEach(consumer);

    }


    @Override
    public Location getWaitZone() {
        return null;
    }

    @Override
    public String getMiniGameName() {
        return "LotaWar";
    }

    @Override
    public boolean leave(Player p){
        kills.remove(p);
        deaths.remove(p);
        Teams t = getTeam(p);
        Bukkit.getScoreboardManager().getMainScoreboard().getTeam(reds).removeEntry(p.getName());
        Bukkit.getScoreboardManager().getMainScoreboard().getTeam(blues).removeEntry(p.getName());
        if(LotaWars.getScoreboardSignHashMap().containsKey(p)) {
            LotaWars.getScoreboardSignHashMap().get(p).destroy();
        }
        setTab(p,ChatColor.getByChar(p.getPlayerListName().substring(1,2)));
        redteam.remove(p);
        blueteam.remove(p);
        MiniHub.games.remove(p.getUniqueId());
        p.teleport(MiniHub.getLobby());
        MiniHub.giveItems(p);
        broadcast(ChatColor.GOLD + p.getMetadata("colorname").get(0).asString() + " a quitté la partie.");
        if(flintHolder == p) setFlintHolder(null,false);
        play(Sound.CREEPER_HISS);
        p.getInventory().clear();
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
        if(t == Teams.RED && redteam.isEmpty()){
            redScore = 3;
            end();
        }
        if(t == Teams.BLUE && blueteam.isEmpty()){
            blueScore = 3;
            end();
        }
        return true;
    }

    @Override
    public boolean isLobby() {
        return false;
    }

    @Override
    public ArrayList<Player> getPlayers() {
        ArrayList<Player> temp = new ArrayList<>(blueteam);
        temp.addAll(redteam);
        return temp;
    }


    public void start(){
        redteam.forEach(p -> {
            kills.put(p,0);
            deaths.put(p,0);
            LotaWars.getScoreboardSignHashMap().get(p).create();
        });
        blueteam.forEach(p -> {
            kills.put(p,0);
            deaths.put(p,0);
            LotaWars.getScoreboardSignHashMap().get(p).create();
        });
        map.getBriquetSpawn().getWorld().getEntities().forEach(e -> {
            if(e.getType() == EntityType.VILLAGER || e.getType() == EntityType.DROPPED_ITEM) e.remove();
        });
        if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(reds) != null)  Bukkit.getScoreboardManager().getMainScoreboard().getTeam(reds).unregister();
        if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(blues) != null)  Bukkit.getScoreboardManager().getMainScoreboard().getTeam(blues).unregister();
        spawnFlint();
        goldRunnable = Bukkit.getScheduler().runTaskTimer(LotaWars.getInstance(),new GoldRunnable(map.getGoldOne(),map.getGoldTwo()),0,26 / (blueteam.size() + redteam.size()));
        speedRunnable = Bukkit.getScheduler().runTaskTimerAsynchronously(LotaWars.getInstance(),new SpeedRunnable(map.getPnj1RSpawn(),map.getBlueSpawn(),this.name),0,2 * 20);
        getMap().getDrapeauB().forEach(l ->{
            l.getBlock().setType(Material.WOOL);
            l.getBlock().setData((byte) 11);
        });
        getMap().getDrapeauR().forEach(l ->{
            l.getBlock().setType(Material.WOOL);
            l.getBlock().setData((byte) 14);
        });
        redteam.forEach(p ->{
            p.setGameMode(GameMode.ADVENTURE);
            MiniHub.games.put(p.getUniqueId(),this);
        });
        blueteam.forEach(p -> {
            p.setGameMode(GameMode.ADVENTURE);
            MiniHub.games.put(p.getUniqueId(),this);
        });
        Listeners.updateHideShow();
        ToSpawn();
        SpawnPnj();
        started = true;
    }

    public void point(Teams who){
        if(who == Teams.RED){
            if(redScore + 1 >= maxScore){
                redScore++;
                drapUpdate(Teams.BLUE);
                end();
            }
            else{
                redScore++;
                blueteam.forEach(p -> p.sendMessage(ChatColor.RED + "Les rouges ont marqués un point !"));
                redteam.forEach(p -> p.sendMessage(ChatColor.RED + "Votre équipe a marquée un point !"));
                drapUpdate(Teams.BLUE);
            }
        }else if(who == Teams.BLUE) {
            if (blueScore + 1 >= maxScore) {
                blueScore++;
                drapUpdate(Teams.RED);
                end();
            } else {
                blueScore++;
                redteam.forEach(p -> p.sendMessage(ChatColor.BLUE + "Les bleus ont marqués un point !"));
                blueteam.forEach(p -> p.sendMessage(ChatColor.BLUE + "Votre équipe a marquée un point !"));
                drapUpdate(Teams.RED);
            }
        }
        makeScoreboard();
    }

    public void end(){
        goldRunnable.cancel();
        speedRunnable.cancel();
        ArrayList<Player> players = new ArrayList<>();
        map.getBriquetSpawn().getWorld().getEntities().forEach(e -> {
            if(e.getType() == EntityType.DROPPED_ITEM) e.remove();
        });
        players.addAll(blueteam);
        players.addAll(redteam);
        broadcast(new Title(new TextComponent(ChatColor.GOLD + "La partie est terminée !"),new TextComponent((redScore > blueScore ? ChatColor.RED + "Les Rouges " : ChatColor.BLUE + "Les Bleus ") + "ont gagnés !"),1 * 20,4 * 20,1 * 20));
        Teams whoWon = blueScore > redScore ? Teams.BLUE : Teams.RED;
        play(whoWon,Sound.FIREWORK_TWINKLE);
        play(whoWon.other(),Sound.WITHER_DEATH);
        players.forEach(p -> {
            setStats(p,getTeam(p) == whoWon);
            makeWin(p,whoWon);
        });
        if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name + "All") != null) Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name + "All").unregister();
        Team t = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(name + "All");
        t.setAllowFriendlyFire(false);
        players.forEach(p -> t.addEntry(p.getName()));
        villagers.forEach(v -> t.addEntry(v.getUniqueId().toString()));
        players.forEach(p -> {
            LotaWars.getScoreboardSignHashMap().get(p).destroy();
        });
        Bukkit.getScheduler().runTaskLaterAsynchronously(LotaWars.getInstance(),() -> toSpawn(players),6 * 20);
    }

    private void toSpawn(ArrayList<Player> team) {
        redteam.forEach(p -> {
            MiniHub.games.remove(p.getUniqueId());
        });
        blueteam.forEach(p -> {
            MiniHub.games.remove(p.getUniqueId());
        });
        redteam = new ArrayList<>();
        blueteam = new ArrayList<>();
        redScore = 0;
        blueScore = 0;
        Bukkit.getScheduler().runTask(LotaWars.getInstance(),() -> {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam(reds).unregister();
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam(blues).unregister();
            map.getBriquetSpawn().getWorld().getEntities().forEach(e -> {
                if(e.getType() == EntityType.VILLAGER || e.getType() == EntityType.DROPPED_ITEM) e.remove();
            });
            Team t = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name + "All");
            t.unregister();
            for (Player player : team) {
                try {
                    player.sendMessage("Tp vers le lobby en cours");
                    LotaWars.getScoreboardSignHashMap().get(player).create();
                    LotaWars.getScoreboardSignHashMap().get(player).destroy();
                    LotaWars.getScoreboardSignHashMap().remove(player);
                    player.getInventory().clear();
                    player.getInventory().setHelmet(null);
                    player.getInventory().setChestplate(null);
                    player.getInventory().setLeggings(null);
                    player.getInventory().setBoots(null);
                    setTab(player,ChatColor.getByChar(player.getPlayerListName().substring(1,2)));
                    player.teleport(MiniHub.getLobby());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        started = false;
        Lobby.getLobbies().get(this.name).reset();
    }

    public void broadcast(String s){
        broadcast(Teams.BLUE,s);
        broadcast(Teams.RED,s);
    }
    public void broadcast(Title t){
        ViaAPI api = Via.getAPI();
        broadcast(Teams.BLUE,t);
        broadcast(Teams.RED,t);
    }
    public void broadcast(Teams t,String s){
        if(t == Teams.BLUE)blueteam.forEach(p -> p.sendMessage(s));
        if(t == Teams.RED)redteam.forEach(p -> p.sendMessage(s));
    }

    public void broadcast(Teams t,Title s){
       ViaAPI api = Via.getAPI();
       ArrayList<Player> team = t == Teams.BLUE ? blueteam : redteam;
        team.forEach(p -> {
            if(api.getPlayerVersion(p.getUniqueId()) < 47){
                p.sendMessage(s.getTitle()[0]);
                p.sendMessage(s.getSubtitle()[0]);
            }
            else p.sendTitle(s);
        });
    }

    public void play(Sound s){
        blueteam.forEach(p -> p.playSound(p.getLocation(),s,1.0F,1.0F));
        redteam.forEach(p -> p.playSound(p.getLocation(),s,1.0F,1.0F));
    }
    public void play(Teams t, Sound s){
        if(t == Teams.BLUE)blueteam.forEach(p -> p.playSound(p.getLocation(),s,1.0F,1.0F));
        if(t == Teams.RED)redteam.forEach(p -> p.playSound(p.getLocation(),s,1.0F,1.0F));
    }

    public void setFlintHolder(@Nullable Player p,boolean death){
        if(p == null){
            if(death) broadcast(ChatColor.GOLD + "Un nouveau briquet est apparu apres la mort de " + flintHolder.getMetadata("colorname").get(0).asString() + ChatColor.GOLD + " !");
            else broadcast(ChatColor.GOLD + "Un nouveau briquet est apparu !");
            spawnFlint();
            play(Sound.ANVIL_LAND);
        }
        if(p != null){
            broadcast(p.getMetadata("colorname").get(0).asString() + ChatColor.GOLD + " a récupéré le briquet !");
            play(getTeam(p), Sound.FIREWORK_LAUNCH);
            play(getTeam(p).other(),Sound.BLAZE_HIT);
        }
        flintHolder = p;
        makeScoreboard();
    }

    public void setTab(Player p,ChatColor t){
        p.setPlayerListName(p.getPlayerListName().replace(p.getName(),t + p.getName()));
        p.setDisplayName(p.getPlayerListName().replace(p.getName(),t + p.getName()));
    }

    public void spawnFlint(){
        map.getBriquetSpawn().getWorld().dropItem(map.getBriquetSpawn(),new ItemStack(Material.FLINT_AND_STEEL));
    }

    public int getInGame(){
        return blueteam.size() + redteam.size();
    }

    public void ToSpawn(){
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();
        Team blues = scoreboard.registerNewTeam(this.blues);
        Team reds = scoreboard.registerNewTeam(this.reds);
        blues.setPrefix(ChatColor.BLUE.toString());
        reds.setPrefix(ChatColor.RED.toString());
        blues.setAllowFriendlyFire(false);
        reds.setAllowFriendlyFire(false);
        for (Player player : blueteam) {
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
            Listener.addStuff(player);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setMetadata("colorname",new FixedMetadataValue(LotaWars.getInstance(),ChatColor.BLUE + player.getName()));
            player.setBedSpawnLocation(map.getBlueSpawn(),true);
            player.getInventory().setHeldItemSlot(0);
            blues.addEntry(player.getName());
            player.teleport(map.getBlueSpawn());
            setTab(player,ChatColor.BLUE);
            Bukkit.getScheduler().runTaskLater(LotaWars.getInstance(),() -> player.setGameMode(GameMode.ADVENTURE),5);
        }
        for (Player player : redteam) {
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
            Listener.addStuff(player);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setMetadata("colorname",new FixedMetadataValue(LotaWars.getInstance(),ChatColor.RED + player.getName()));
            player.setBedSpawnLocation(map.getRedSpawn(),true);
            player.getInventory().setHeldItemSlot(0);
            reds.addEntry(player.getName());
            player.teleport(map.getRedSpawn());
            setTab(player,ChatColor.RED);
            Bukkit.getScheduler().runTaskLater(LotaWars.getInstance(),() -> player.setGameMode(GameMode.ADVENTURE),5);
        }
        broadcast(new Title(new TextComponent(ChatColor.GOLD + "C'est parti !"),new TextComponent(ChatColor.YELLOW + "la partie commence sur la map \"" + map.getName() + "\""),1 * 20,2 * 20,1 * 20));
        makeScoreboard();
    }

    public Teams getTeam(Player p){
        if(redteam.contains(p)) return Teams.RED;
        else return Teams.BLUE;
    }

    public void SpawnPnj(){
        villagers = new ArrayList<>();

        villagers.add(LotaPNJ.spawn(map.getPnj1RSpawn()));
        villagers.add(LotaPNJ.spawn(map.getPnj2RSpawn()));
        villagers.add(LotaPNJ.spawn(map.getPnj3RSpawn()));
        villagers.forEach(v -> v.setMetadata("team",new FixedMetadataValue(LotaWars.getInstance(),Teams.RED)));

        villagers.add(LotaPNJ.spawn(map.getPnj1BSpawn()));
        villagers.add(LotaPNJ.spawn(map.getPnj2BSpawn()));
        villagers.add(LotaPNJ.spawn(map.getPnj3BSpawn()));
        for (int i = 3; i < villagers.size(); i++) {
            villagers.get(i).setMetadata("team",new FixedMetadataValue(LotaWars.getInstance(),Teams.BLUE));
        }
        villagers.forEach(v -> v.setMetadata("game",new FixedMetadataValue(LotaWars.getInstance(),this.name)));
        villagers.forEach(this::disableAI);
        Bukkit.getScheduler().runTaskLater(LotaWars.getInstance(),() -> villagers.forEach(v -> v.setMaxHealth(80.0)),20);
        Bukkit.getScheduler().runTaskLater(LotaWars.getInstance(),() -> villagers.forEach(v -> v.setHealth(80.0)),40);
        for (int i = 0; i < villagers.size(); i++) {
            int use = ( i <= 2 ? i : i - 3);
            Villager v = villagers.get(i);
            ArrayList<VillagerTrade> trades = LotaWars.getRecipeItems().get(use);
            trades.forEach(trade -> addTrade(v,trade.getFirst(),trade.getSec(),trade.getThird(),trade.getMaxuses()));
        }
        for (int i = 0; i < villagers.size(); i++) {
            if( i > 2) villagers.get(i).setCustomName(pnjNames[i - 3]);
            else villagers.get(i).setCustomName(pnjNames[i]);
        }

    }

    public static HashMap<String,Game> gameList = new HashMap<>();


    public void drapUpdate(Teams t){
        ArrayList<Location> toremove = new ArrayList<>();
        switch (t){
            case RED:
                int i = 0;
                int mod = 4 - blueScore;
                for (Location location : getMap().getDrapeauR()) {
                    if(i % mod == 0){
                        toremove.add(location);
                    }
                    i++;
                }
                break;
            case BLUE:
                i = 0;
                mod = 4 - redScore;
                for (Location location : getMap().getDrapeauB()) {
                    if(i % mod == 0){
                        toremove.add(location);
                    }
                    i++;
                }
                break;
        }
        toremove.forEach(l -> l.getBlock().setType(Material.AIR));
    }
    private void disableAI(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEnt = ((CraftEntity) entity).getHandle();
        ((LotaPNJ) nmsEnt).NoAI = true;
        /*NBTTagCompound tag = nmsEnt.getNBTTag();

        if(tag == null) {
            tag = new NBTTagCompound();
        }

        nmsEnt.c(tag);
        tag.setInt("NoAI", 1);
        nmsEnt.f(tag); */
    }

    public void addTrade(Villager villager, org.bukkit.inventory.ItemStack stack1, org.bukkit.inventory.ItemStack stack2, org.bukkit.inventory.ItemStack stack3,int Maxuses) {
        LotaPNJ entityVillager = (LotaPNJ) ((CraftVillager) villager).getHandle();

        try {
            MerchantRecipeList list = (MerchantRecipeList) entityVillager.getTrades();
            if(list == null) list = new MerchantRecipeList();
            if (stack2 != null) {
                net.minecraft.server.v1_8_R3.ItemStack item1 = CraftItemStack.asNMSCopy(stack1);
                net.minecraft.server.v1_8_R3.ItemStack item2 = CraftItemStack.asNMSCopy(stack2);
                net.minecraft.server.v1_8_R3.ItemStack rewardItem = CraftItemStack.asNMSCopy(stack3);
                MerchantRecipe recipe = new MerchantRecipe(item1, item2, rewardItem);
                Field MaxUses = recipe.getClass().getDeclaredField("maxUses");
                MaxUses.setAccessible(true);
                MaxUses.setInt(recipe,Maxuses);
                list.add(recipe);
            } else {
                net.minecraft.server.v1_8_R3.ItemStack item1 = CraftItemStack.asNMSCopy(stack1);
                net.minecraft.server.v1_8_R3.ItemStack rewardItem = CraftItemStack.asNMSCopy(stack3);
                MerchantRecipe recipe = new MerchantRecipe(item1, rewardItem);
                Field MaxUses = recipe.getClass().getDeclaredField("maxUses");
                MaxUses.setAccessible(true);
                MaxUses.setInt(recipe,Maxuses);
                list.add(recipe);
            }
            entityVillager.setTrades(list);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public int getScore(Teams t){
        return t == Teams.BLUE ? getBlueScore() : getRedScore();
    }

    public void setStats(Player p,boolean won){
        HashMap<String,String> towrite = new HashMap<>();
        boolean update = false;
        int kills = getKills().get(p);
        int morts = getDeaths().get(p);
        int wins = won ? 1 : 0;
        int loss = won ? 0 : 1;
        try {
            ResultSet data = LotaWars.getDb().getFromKey("LotaWar_Stats","UUID",p.getUniqueId().toString());
            if(data.next()){
                update = true;
                kills+= data.getInt("kills");
                morts+= data.getInt("deaths");
                wins+= data.getInt("wins");
                loss+= data.getInt("loss");
            }
            else {
                towrite.put("UUID",p.getUniqueId().toString());
                towrite.put("kills", String.valueOf(kills));
                towrite.put("deaths", String.valueOf(morts));
                towrite.put("wins", String.valueOf(wins));
                towrite.put("loss", String.valueOf(loss));
            }

            if(update){
                LotaWars.getDb().ExecuteQuery("UPDATE `LotaWar_Stats` SET kills='" + kills + "', deaths='" + morts +"', wins='" + wins + "', loss='" + loss + "' WHERE UUID='" + p.getUniqueId() + "'");
            }
            else {
                LotaWars.getDb().AddValues("LotaWar_Stats",towrite);
            }
        } catch (InvalidFormatException | SQLException e) {
            e.printStackTrace();
        }
        getKills().remove(p);
        getDeaths().remove(p);
    }


    public void makeWin(Player p,Teams whoWon){
        CustomPlayer player = CustomPlayer.get(p);
        int score = getScore(getTeam(p)) ;
        if(getTeam(p) == whoWon){
            player.winLotas(perWin,"pour avoir gagné la partie");
        }
        if(score < 0) return;
        player.winLotas(perPoints * score,"pour avoir marqué " + score + " point"  + (score > 1 ? "s" : ""));
    }
}
