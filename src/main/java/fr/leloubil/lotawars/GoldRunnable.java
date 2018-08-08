package fr.leloubil.lotawars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class GoldRunnable implements Runnable {

    private final Location one;

    private final Location two;

    private static int time = 0;

    private static int nexttime = 0;


    public GoldRunnable(Location one, Location two) {
        this.one = one;
        this.two = two;
    }
    private boolean good = false;

    @Override
    public void run() {
        if(time == 0 ) {
            nexttime = new Random().nextInt(2) + 1;
        }
        if(time == nexttime){
            spawn(search(),new Random().nextInt(2) + 1);
            time = 0;
        }
        else {
            time++;
        }
    }

    public Location search(){
        Random rx = new Random();
        Random rz = new Random();
        Random ry = new Random();

        int smallx = (int) (one.getX() < two.getX() ? one.getX() : two.getX());
        int bigx = (int) (one.getX() > two.getX() ? one.getX() : two.getX());

        int smallz = (int) (one.getZ() < two.getZ() ? one.getZ() : two.getZ());
        int bigz = (int) (one.getZ() > two.getZ() ? one.getZ() : two.getZ());

        int smally = (int) (one.getY() < two.getY() ? one.getY() : two.getY());
        int bigy = (int) (one.getY() > two.getY() ? one.getY() : two.getY());
        int x = rx.nextInt(bigx + 1 - smallx) + smallx;
        int z = rz.nextInt(bigz + 1 - smallz) + smallz;
        int y = ry.nextInt(bigy + 1 - smally) + smally;
        getAt(new Location(one.getWorld(),x,y,z));
        if((last == null  || last.getType() == Material.AIR) || (last.getType() == Material.STATIONARY_WATER) || last.getType() == Material.WATER) {
            getAt(new Location(one.getWorld(), (double) x, y - 1, (double) z));
            if (last != null) good = true;
        }

        return new Location(one.getWorld(),x,y,z);

    }



    public void spawn(Location l,int amount){
        if(!good) return;
        //Bukkit.broadcastMessage("SPAWNED");
        //Bukkit.broadcastMessage(l.toString());
        good = false;
        Bukkit.getScheduler().runTask(LotaWars.getInstance(), () -> l.getWorld().dropItem(l,new ItemStack(Material.GOLD_INGOT,amount)));
    }


    private Block last;
    public void getAt(Location l){
        Bukkit.getScheduler().runTask(LotaWars.getInstance(), () -> last = l.getBlock());
    }
}
