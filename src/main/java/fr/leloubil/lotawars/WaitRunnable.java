package fr.leloubil.lotawars;

import fr.leloubil.lotawars.matchmaking.Lobby;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.github.paperspigot.Title;

import java.util.HashMap;

public class WaitRunnable implements Runnable {

    @Getter
    private static HashMap<String,BukkitTask> waitRunnables = new HashMap<>();

    private final Lobby lobby;

    private Integer timeleft;

    public WaitRunnable(Lobby lobby) {
        timeleft = 10;
        this.lobby = lobby;
    }

    @Override
    public void run() {
        if(!lobby.isFull()){
            lobby.broadcastWait(ChatColor.DARK_GREEN + "Demarrage annulé, un joueur a quitté la partie");
            this.cancel();
        }
        if(timeleft == 0){
            Bukkit.getScheduler().runTask(LotaWars.getInstance(), lobby::PreStart);
            lobby.get1_7().forEach(p -> p.setLevel(timeleft));
            this.cancel();
        }
        lobby.broadcastWait(new Title(ChatColor.GREEN + timeleft.toString(),ChatColor.GREEN + "Démarrage dans",0,20,0));
        lobby.get1_7().forEach(p -> p.setLevel(timeleft));
        Bukkit.getScheduler().runTask(LotaWars.getInstance(),() -> lobby.playWait(Sound.ORB_PICKUP));
        timeleft--;
    }

    private void cancel() {
        WaitRunnable.getWaitRunnables().remove(lobby.getName()).cancel();
    }
}
