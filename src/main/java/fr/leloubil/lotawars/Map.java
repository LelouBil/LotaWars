package fr.leloubil.lotawars;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Map {
    @Getter
    private final String name;

    @Getter @Setter
    private Location blueSpawn;
    @Getter @Setter
    private Location redSpawn;

    @Getter @Setter
    private Location pnj1BSpawn;
    @Getter @Setter
    private Location pnj2BSpawn;
    @Getter @Setter
    private Location pnj3BSpawn;

    @Getter @Setter
    private Location pnj1RSpawn;
    @Getter @Setter
    private Location pnj2RSpawn;
    @Getter @Setter
    private Location pnj3RSpawn;

    @Getter @Setter
    private Location goldOne;

    @Getter @Setter
    private Location goldTwo;

    // 14 blocks
    @Getter @Setter
    private ArrayList<Location> drapeauB = new ArrayList<>();
    @Getter @Setter
    private ArrayList<Location> drapeauR = new ArrayList<>();

    @Getter @Setter
    private Location briquetSpawn;
    @Getter @Setter
    private boolean modified = false;

    public Map(String name) {
        this.name = name;
        this.setModified(true);
    }

    @Getter
    public static HashMap<UUID,String> editList = new HashMap<>();

    public Map(Map map) {
        this.modified = false;
        this.name = map.name;
        this.setBriquetSpawn(map.getBriquetSpawn().clone());
        this.setRedSpawn(map.getRedSpawn().clone());
        this.setBlueSpawn(map.getBlueSpawn().clone());

        this.setPnj1BSpawn(map.getPnj1BSpawn().clone());
        this.setPnj2BSpawn(map.getPnj2BSpawn().clone());
        this.setPnj3BSpawn(map.getPnj3BSpawn().clone());

        this.setPnj1RSpawn(map.getPnj1RSpawn().clone());
        this.setPnj2RSpawn(map.getPnj2RSpawn().clone());
        this.setPnj3RSpawn(map.getPnj3RSpawn().clone());

        this.setGoldOne(map.getGoldOne().clone());
        this.setGoldTwo(map.getGoldTwo().clone());

        map.getDrapeauR().forEach(e -> this.getDrapeauR().add(e.clone()));
        map.getDrapeauB().forEach(e -> this.getDrapeauB().add(e.clone()));
    }
    public Map GetNew(World w){
        Map n = new Map(this);
        n.blueSpawn.setWorld(w);
        n.redSpawn.setWorld(w);

        n.pnj1BSpawn.setWorld(w);
        n.pnj2BSpawn.setWorld(w);
        n.pnj3BSpawn.setWorld(w);

        n.pnj1RSpawn.setWorld(w);
        n.pnj2RSpawn.setWorld(w);
        n.pnj3RSpawn.setWorld(w);

        n.goldOne.setWorld(w);

        n.goldTwo.setWorld(w);

        n.drapeauB.forEach(l -> l.setWorld(w));
        n.drapeauR.forEach(l -> l.setWorld(w));

        n.briquetSpawn.setWorld(w);
        return n;
    }
}
