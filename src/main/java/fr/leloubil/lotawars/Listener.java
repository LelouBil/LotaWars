package fr.leloubil.lotawars;

import fr.leloubil.lotawars.commands.WarMap;
import fr.leloubil.lotawars.matchmaking.Lobby;
import fr.leloubil.lotawars.matchmaking.SignManager;
import fr.leloubil.minihub.MiniHub;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.github.paperspigot.Title;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class Listener implements org.bukkit.event.Listener {


    public void CreateLobby(PlayerInteractEvent e){
        //Format = UUID->(mapname#maxPlayers)
        String[] data = SignManager.SignList.get(e.getPlayer().getUniqueId()).split("#");
        SignManager.SignList.remove(e.getPlayer().getUniqueId());
        Player p = e.getPlayer();
        String mapname = data[0];
        int maxPlayers = 0;
        try {
            maxPlayers = Integer.parseInt(data[1]);
        }
        catch (NumberFormatException ignored){}
        Location waitZone = LotaWars.getWaitZone();

        if(e.getClickedBlock().getType() != Material.SIGN && e.getClickedBlock().getType() != Material.SIGN_POST && e.getClickedBlock().getType() != Material.WALL_SIGN) return;
        String worldname = mapname + Lobby.getFree(mapname);
        if(Bukkit.getWorld(worldname) == null){
            File f = new File(LotaWars.getInstance().getDataFolder(),"Maps/" + mapname);
            if(!f.exists()) return;
            File f2 = new File(Bukkit.getWorldContainer(),worldname);
            try {
                FileUtils.copyDirectory(f,f2);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            Bukkit.createWorld(new WorldCreator(worldname));
            if(Bukkit.getWorld(worldname) == null) return;
        }

        Sign s = (Sign) e.getClickedBlock().getState();
        Lobby l = new Lobby(LotaWars.getMaplist().get(mapname),worldname,maxPlayers,Bukkit.getWorld(worldname),s);
        l.waitZone = waitZone;
        Lobby.getLobbies().put(worldname,l);
        p.sendMessage("Le lobby \"" + worldname + "\" a été créé");
    }



    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        if(WarMap.setUpMap.containsKey(e.getPlayer().getUniqueId())){
            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                WarMap.lastClick.put(e.getPlayer(), e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage("Block cliqué, " + e.getClickedBlock().getLocation().toVector().toString());
                return;
            }
        }
        if(SignManager.SignList.containsKey(e.getPlayer().getUniqueId())){
            CreateLobby(e);
            return;
        }
        if(SignManager.isGameSign(e.getClickedBlock())){
            SignManager.OnClick(e.getPlayer(),(Sign)e.getClickedBlock().getState());
            return;
        }
        Player p = e.getPlayer();
        if(MiniHub.games.containsKey(p.getUniqueId())) {
            if (!MiniHub.games.get(p.getUniqueId()).getMiniGameName().equals("LotaWar")) return;
            fr.leloubil.minihub.interfaces.Game minigame = MiniHub.games.get(p.getUniqueId());
            if (p.getWorld().getName().equals("lobby") && minigame.isLobby()) {
                Lobby l = (Lobby) minigame;
                if (e.getItem() == null) return;
                if (e.getItem().isSimilar(l.getBluewool())) {
                    if (l.getPreblue().size() == l.maxPerTeam) {
                        p.sendMessage(ChatColor.BLUE + "L'équipe bleue est pleine !");
                        return;
                    }
                    if (l.getPreblue().contains(p)) {
                        p.sendMessage(ChatColor.BLUE + "Tu es déja dans l'équipe bleue !");
                        return;
                    } else {
                        p.sendMessage(ChatColor.BLUE + "Tu as bien rejoint l'équipe bleue !");
                    }
                    l.getPreblue().add(p);
                    l.getPrered().remove(p);
                    l.setTab(p, ChatColor.BLUE);
                    l.updateTeams();
                } else if (e.getItem().isSimilar(l.getRedwool())) {
                    if (l.getPrered().size() == l.maxPerTeam) {
                        p.sendMessage(ChatColor.RED + "L'équipe rouge est pleine !");
                        return;
                    }
                    if (l.getPrered().contains(p)) {
                        p.sendMessage(ChatColor.RED + "Tu es déja dans l'équipe rouge !");
                        return;
                    } else {
                        p.sendMessage(ChatColor.RED + "Tu as bien rejoint l'équipe rouge !");
                    }
                    l.getPrered().add(p);
                    l.getPreblue().remove(p);
                    l.setTab(p, ChatColor.RED);
                    l.updateTeams();
                } else if (e.getItem().isSimilar(Lobby.getGreywool())) {
                    if (l.getPreblue().contains(p)) {
                        p.sendMessage(ChatColor.GRAY + "Tu as quitté l'équipe" + ChatColor.BLUE + ChatColor.ITALIC + " bleue " + ChatColor.GRAY + " !");
                        l.getPreblue().remove(p);
                        l.setTab(p, ChatColor.GRAY);
                        l.updateTeams();
                        return;
                    }
                    if (l.getPrered().contains(p)) {
                        p.sendMessage(ChatColor.GRAY + "Tu as quitté l'équipe" + ChatColor.RED + ChatColor.ITALIC + " rouge " + ChatColor.GRAY + " !");
                        l.getPrered().remove(p);
                        l.setTab(p, ChatColor.GRAY);
                        l.updateTeams();
                        return;
                    }
                }
                return;
            }
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(p.getGameMode() == GameMode.ADVENTURE){
            GameListener(e);
            return;
        }
        if (!Map.getEditList().containsKey(p.getUniqueId())) return;

        e.setCancelled(true);
        Location pos = e.getClickedBlock().getLocation();

        String[] parsed = Map.getEditList().get(p.getUniqueId()).split("#");
        Map m = LotaWars.getMaplist().get(parsed[0]);

        if(parsed[1].equals("briq")){
            m.setBriquetSpawn(pos);
        }
        else if(parsed[1].equals("goldone")){
            m.setGoldOne(pos);
        }
        else if(parsed[1].equals("goldtwo")){
            m.setGoldTwo(pos);
        }
        else if (parsed[2].equals("red")) {
            switch (parsed[1]) {
                case "spawn":
                    pos.setYaw(0f);
                    m.setRedSpawn(pos);
                    break;
                case "pnj1":
                    pos.setYaw(270f);
                    pos.add(0.5,0,0.5);
                    m.setPnj1RSpawn(pos);
                    break;
                case "pnj2":
                    pos.setYaw(270f);
                    pos.add(0.5,0,0.5);
                    m.setPnj2RSpawn(pos);
                    break;
                case "pnj3":
                    pos.setYaw(270f);
                    pos.add(0.5,0,0.5);
                    m.setPnj3RSpawn(pos);
                    break;
                case "drapadd":
                    m.getDrapeauR().add(pos);
                    break;
                case "drapdel":
                    m.getDrapeauR().remove(pos);
                    break;
            }
        } else if (parsed[2].equals("blue")) {
            switch (parsed[1]) {
                case "spawn":
                    pos.setYaw(180f);
                    m.setBlueSpawn(pos);
                    break;
                case "pnj1":
                    pos.setYaw(270f);
                    pos.add(0.5,0,0.5);
                    m.setPnj1BSpawn(pos);
                    break;
                case "pnj2":
                    pos.setYaw(270f);
                    pos.add(0.5,0,0.5);
                    m.setPnj2BSpawn(pos);
                    break;
                case "pnj3":
                    pos.setYaw(270f);
                    pos.add(0.5,0,0.5);
                    m.setPnj3BSpawn(pos);
                    break;
                case "drapadd":
                    m.getDrapeauB().add(pos);
                    break;
                case "drapdel":
                    m.getDrapeauB().remove(pos);
                    break;

            }
        }
            m.setModified(true);
        Map.getEditList().remove(p.getUniqueId());
        p.sendMessage("Le " + parsed[1] + " " + (parsed.length < 3 ? "" : parsed[2])  + " de la map " + parsed[0] + " a bien été modifié !");
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        if(!MiniHub.games.containsKey(e.getPlayer().getUniqueId())) return;
        if(!MiniHub.games.get(e.getPlayer().getUniqueId()).getMiniGameName().equals("LotaWar")) return;
        Player player = e.getPlayer();
        addStuff(player);
    }

    static void addStuff(Player player) {
        LotaWars.getStuff().forEach(i -> {
            if(i.getType().toString().startsWith("LEATHER")){
                Game g = Game.gameList.get(MiniHub.games.get(player.getUniqueId()).getName());
                LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
                if(g.getRedteam().contains(player))meta.setColor(Color.RED);
                else meta.setColor(Color.BLUE);
                i.setItemMeta(meta);
            }
            if(i.getType().toString().endsWith("HELMET")){
                player.getInventory().setHelmet(i);
            }
            else if(i.getType().toString().endsWith("CHESTPLATE")){
                player.getInventory().setChestplate(i);
            }
            else if(i.getType().toString().endsWith("LEGGINGS")){
                player.getInventory().setLeggings(i);
            }
            else if(i.getType().toString().endsWith("BOOTS")){
                player.getInventory().setBoots(i);
            }
            else player.getInventory().addItem(i);
        });
        Bukkit.getScheduler().runTaskLater(LotaWars.getInstance(),() -> {
            if(!player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,6 * 20,255,true,false))){
                player.sendMessage("bruh");
            }
        },5);
    }


    @EventHandler
    public void OnPnjHit(EntityDamageByEntityEvent e){
        if(!Arrays.asList(Game.pnjNames).contains(e.getEntity().getName())) return;
        if(e.getEntity().getMetadata("game").isEmpty()) return;
        if(!(e.getDamager() instanceof Player)) return;
        Villager v = (Villager) e.getEntity();
        Game g = Game.gameList.get(v.getMetadata("game").get(0).asString());
        if(!g.isStarted()) return;
        Teams t = (Teams) v.getMetadata("team").get(0).value();
        Player p = (Player) e.getDamager();
        if(!MiniHub.games.containsKey(p.getUniqueId())) return;
        if(!MiniHub.games.get(p.getUniqueId()).getName().equals(g.getName())){
            e.setCancelled(true);
            return;
        }
        if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(g.getName() + "All") != null){
            if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(g.getName() + "All").hasEntry(p.getName())){
                e.setCancelled(true);
                return;
            }
        }
        if(g.getTeam(p) == t){
            p.sendMessage("Tu ne peut pas attaquer tes propres pnjs !");
            e.setCancelled(true);
            return;
        }
        g.play(t,Sound.VILLAGER_HIT);
        g.broadcast(t,new Title(ChatColor.DARK_RED + "A L'AIDE !",ChatColor.RED + " -les pnjs",0,2 * 20,0));
    }

    @EventHandler
    public void PNJListener(EntityDeathEvent e){
        if(!Arrays.asList(Game.pnjNames).contains(e.getEntity().getName())) return;
        if(e.getEntity().getMetadata("game").isEmpty()) return;
        Villager v = (Villager) e.getEntity();
        Game g = Game.gameList.get(v.getMetadata("game").get(0).asString());
        if(!g.isStarted()) return;
        Teams t = (Teams) v.getMetadata("team").get(0).value();
        Firework fw = (Firework) v.getWorld().spawnEntity(e.getEntity().getKiller().getLocation().clone().add(0,1,0),EntityType.FIREWORK);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(0);
        meta.clearEffects();
        meta.addEffect(FireworkEffect.builder().withColor(t.color()).withFade(Color.GRAY).flicker(false).trail(false).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(meta);
        switch (t){
            case BLUE:
                g.point(Teams.RED);
                e.getEntity().getKiller().teleport(g.getMap().getRedSpawn());
                e.getEntity().getKiller().setHealth(20);
                e.getEntity().getKiller().setFoodLevel(20);
                g.play(Teams.RED,Sound.SUCCESSFUL_HIT);
                g.play(Teams.BLUE,Sound.BAT_DEATH);
                break;
            case RED:
                g.point(Teams.BLUE);
                e.getEntity().getKiller().teleport(g.getMap().getBlueSpawn());
                e.getEntity().getKiller().setHealth(20);
                e.getEntity().getKiller().setFoodLevel(20);
                g.play(Teams.BLUE,Sound.SUCCESSFUL_HIT);
                g.play(Teams.RED,Sound.BAT_DEATH);
        }
        Bukkit.getScheduler().runTaskLater(LotaWars.getInstance(), fw::detonate,3);

    }

    @EventHandler
    public void onFlintPickup(PlayerPickupItemEvent e){
        Player p = e.getPlayer();
        if(!MiniHub.games.containsKey(p.getUniqueId())) return;
        if(!MiniHub.games.get(p.getUniqueId()).getMiniGameName().equals("LotaWar")) return;
        Game g = Game.gameList.get(MiniHub.games.get(p.getUniqueId()).getName());
        if(e.getItem().getItemStack().getType() != Material.FLINT_AND_STEEL)return;
        g.setFlintHolder(p,false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        if(!MiniHub.games.containsKey(p.getUniqueId())) return;
        if(!MiniHub.games.get(p.getUniqueId()).getMiniGameName().equals("LotaWar")) return;
        Game g = Game.gameList.get(MiniHub.games.get(p.getUniqueId()).getName());
        g.deaths.replace(p,g.deaths.get(p) + 1);
        if(p.getKiller() != null && !p.getKiller().getName().equals(p.getName())){
            g.kills.replace(p.getKiller(),g.kills.get(p.getKiller()) + 1);
            g.broadcast(p.getMetadata("colorname").get(0).asString() + ChatColor.RESET +  " a été tué par " + ChatColor.RESET +  p.getKiller().getMetadata("colorname").get(0).asString() + ".");
        }
        else {
            g.broadcast(p.getMetadata("colorname").get(0).asString()+ ChatColor.RESET + " est mort.");
        }
        e.setDroppedExp(0);
        e.setDeathMessage("");

        LotaWars.getStuff().forEach(b -> e.getDrops().removeIf(i -> i.getType().equals(b.getType())));
        if(e.getDrops().removeIf(i -> i.getType() == Material.FLINT_AND_STEEL)){
            g.setFlintHolder(null,true);
        }
        Bukkit.getScheduler().runTaskLater(LotaWars.getInstance(),() -> p.spigot().respawn(),10);
    }


    @EventHandler
    public void DropListener(PlayerDropItemEvent e){
        Player p = e.getPlayer();
        if(!MiniHub.games.containsKey(p.getUniqueId())) return;
        if(!MiniHub.games.get(p.getUniqueId()).getMiniGameName().equals("LotaWar")) return;
        if(e.getItemDrop().getItemStack().getType() == Material.FLINT_AND_STEEL || e.getItemDrop().getItemStack().getType().toString().contains("MUSHROOM")) e.setCancelled(true);
    }




    public void GameListener(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(!MiniHub.games.containsKey(p.getUniqueId())) return;
        if(!MiniHub.games.get(p.getUniqueId()).getMiniGameName().equals("LotaWar")) return;
        fr.leloubil.minihub.interfaces.Game minigame = MiniHub.games.get(p.getUniqueId());
        if(e.getClickedBlock().getType().toString().contains("CHEST") || e.getClickedBlock().getType().toString().contains("DOOR")){
            e.setCancelled(true);
            return;
        }
        if(minigame.isLobby()) return;
        Game g = (Game) minigame;
        Map m = g.getMap();
        Teams t = Teams.BLUE;
        if(!p.getItemInHand().getType().equals(Material.FLINT_AND_STEEL)) return;
        if(g.getRedteam().contains(p)) t = Teams.RED;
        if(t == Teams.BLUE){
           if(m.getDrapeauB().contains(e.getClickedBlock().getLocation())){
                e.setCancelled(true);
                p.sendMessage("Tu ne peut pas bruler ton propre drapeau !");
            }
            else if(m.getDrapeauR().contains(e.getClickedBlock().getLocation())){
                e.setCancelled(true);
                g.point(Teams.BLUE);
                if(p.getItemInHand().getAmount() == 1) p.getInventory().clear(p.getInventory().getHeldItemSlot());
                else p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                p.teleport(g.getMap().getBlueSpawn());
                g.play(Teams.BLUE,Sound.SUCCESSFUL_HIT);
                g.play(Teams.RED,Sound.BAT_DEATH);
                p.setHealth(20);
                p.setFoodLevel(20);
                g.setFlintHolder(null,false);
            }
        }else {
            if(m.getDrapeauR().contains(e.getClickedBlock().getLocation())){
                e.setCancelled(true);
                p.sendMessage("Tu ne peut pas bruler ton propre drapeau !");
            }
            else if(m.getDrapeauB().contains(e.getClickedBlock().getLocation())){
                e.setCancelled(true);
                p.teleport(g.getMap().getRedSpawn());
                g.point(Teams.RED);
                if(p.getItemInHand().getAmount() == 1) p.getInventory().clear(p.getInventory().getHeldItemSlot());
                else p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                g.play(Teams.RED,Sound.SUCCESSFUL_HIT);
                g.play(Teams.BLUE,Sound.BAT_DEATH);
                p.setHealth(20);
                p.setFoodLevel(20);
                g.setFlintHolder(null,false);
            }
        }

    }
}
