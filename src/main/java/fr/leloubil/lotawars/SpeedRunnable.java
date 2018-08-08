package fr.leloubil.lotawars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class SpeedRunnable implements Runnable {

    private final Location r;

    private final Location b;

    private final String gameName;

    public SpeedRunnable(final Location r,final Location b,final String gameName) {
        this.r = r;
        this.b = b;
        this.gameName = gameName;
    }

    @Override
    public void run() {
        Game game = Game.gameList.get(gameName);
        ArrayList<Player> red = (ArrayList<Player>) game.getRedteam().clone();
        ArrayList<Player> blue = (ArrayList<Player>) game.getBlueteam().clone();
        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED,2 * 20,0,true,false);
        int dist = 0;
        int players = game.getInGame();
        if(players <= 4) dist = 15;
        else if(players <= 8) dist = 25;
        else if(players <= 16) dist = 45;
        int finalDist = dist;

        red.removeIf(p -> r.getWorld() != p.getLocation().getWorld() || r.distance(p.getLocation()) > finalDist);
        blue.removeIf(p -> b.getWorld() != p.getLocation().getWorld() || b.distance(p.getLocation()) > finalDist);

        Bukkit.getScheduler().runTask(LotaWars.getInstance(),() -> {
            red.forEach(p -> p.addPotionEffect(speed,true));
            blue.forEach(p -> p.addPotionEffect(speed,true));
        });

    }
}
