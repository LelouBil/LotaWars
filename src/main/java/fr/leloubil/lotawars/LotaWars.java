package fr.leloubil.lotawars;

import fr.leloubil.lotawars.commands.WarMap;
import fr.leloubil.lotawars.commands.WarMatch;
import fr.leloubil.lotawars.matchmaking.Lobby;
import fr.leloubil.lotawars.scoreboard.LotaPNJ;
import fr.leloubil.lotawars.scoreboard.ScoreboardSign;
import fr.leloubil.minihub.MiniHub;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityVillager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public final class LotaWars extends JavaPlugin {

    @Getter
    public static HashMap<String,Map> maplist = new HashMap<>();

    @Getter
    public static HashMap<Player,ScoreboardSign> scoreboardSignHashMap = new HashMap<>();

    @Getter
    private static YamlConfiguration mapsConf = new YamlConfiguration();

    @Getter
    private static YamlConfiguration pnjsconf = new YamlConfiguration();

    private static Logger logger;

    @Getter
    private static LotaWars instance;

    @Getter @Setter
    private static Location waitZone;

    @Getter
    private static Database db = new Database();

    @Getter
    private static ArrayList<ItemStack> stuff = new ArrayList<>();

    @Getter
    private static ArrayList<ArrayList<VillagerTrade>> recipeItems = new ArrayList<>();


    @Getter
    private static HashMap<String,ArrayList<HashMap<Location,Teams>>> pnjcache = new HashMap<>();

    @Override
    public void onEnable() {
        logger = this.getLogger();
        instance = this;
        File mapsfile = new File(getDataFolder(),"maps.yml");
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        loadConf(mapsfile, mapsConf);
        parseMaps();
        parseLobbies();
        parsePnjs();
        parseStuff();
        registerThings();
        Bukkit.createWorld(new WorldCreator("lobby"));
        info("Plugin Demarré !");
    }

    private void saveLobbies(){
        File lobbyfile = new File(getDataFolder(), "lobbies.yml");
        YamlConfiguration lobbiesConf = new YamlConfiguration();
        loadConf(lobbyfile,lobbiesConf);
        List<String> stringArrayList = new ArrayList<>();
        lobbiesConf.set("waitzone",MiniHub.LocToString(waitZone));
        Lobby.lobbies.values().forEach(s -> {
            String data = s.getName() + ";;" + s.getMap().getName() + ";;" + s.maxPlayers + ";;" + MiniHub.LocToString(s.getSign().getLocation());
            stringArrayList.add(data);
        });
        lobbiesConf.set("lobbies",stringArrayList);
        try {
            lobbiesConf.save(lobbyfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseLobbies() {
        File lobbyfile = new File(getDataFolder(), "lobbies.yml");
        YamlConfiguration lobbiesConf = new YamlConfiguration();
        loadConf(lobbyfile,lobbiesConf);
        ArrayList<String> LobbiesStrings = (ArrayList<String>) lobbiesConf.getStringList("lobbies");
        LotaWars.waitZone = MiniHub.StrToLocation(lobbiesConf.getString("waitzone"));
        LobbiesStrings.forEach(s -> {
            //Format : Chinese1;;Chinese2;;SignLocation
            String[] data = s.split(";;");
            String lobbyname = data[0];
            Bukkit.createWorld(new WorldCreator(lobbyname));
            String mapname = data[1];
            Map m = LotaWars.getMaplist().get(mapname);
            int maxPlayers = Integer.parseInt(data[2]);
            Location signloc = MiniHub.StrToLocation(data[3]);
            assert signloc != null;
            Sign sign = (Sign) signloc.getBlock().getState();
            Lobby l = new Lobby(m,lobbyname,maxPlayers,Bukkit.getWorld(lobbyname),sign);
            l.waitZone = LotaWars.getWaitZone();

            Lobby.lobbies.put(lobbyname,l);
        });
        LotaPNJ.registerEntity("Villager", 120, EntityVillager.class, LotaPNJ.class);
    }

    @Override
    public void onDisable() {
        info("Sauvegarde des maps en cours");
        try {
            saveMaps();
            saveLobbies();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Bukkit.getScheduler().cancelTasks(this);
        info("Plugin Eteint !");
    }


    public static void info(String txt){
        logger.info(txt);
    }

    public void parseMaps(){
        ArrayList<String> mapNames = (ArrayList<String>) mapsConf.getStringList("Maps");
        File mapsDir = new File(getDataFolder(),"Maps");
        if(!mapsDir.exists()) mapsDir.mkdir();

        mapNames.forEach((name) -> {
            try {
                File mapFile = new File(mapsDir, name + ".yml");
                Map m = new Map(name);
                YamlConfiguration mapConfig = new YamlConfiguration();
                mapConfig.load(mapFile);

                m.setBlueSpawn(MiniHub.StrToLocation(mapConfig.getString("blueSpawn")));
                m.setRedSpawn(MiniHub.StrToLocation(mapConfig.getString("redSpawn")));

                m.setPnj1BSpawn(MiniHub.StrToLocation(mapConfig.getString("pnj1BSpawn")));
                m.setPnj2BSpawn(MiniHub.StrToLocation(mapConfig.getString("pnj2BSpawn")));
                m.setPnj3BSpawn(MiniHub.StrToLocation(mapConfig.getString("pnj3BSpawn")));


                m.setPnj1RSpawn(MiniHub.StrToLocation(mapConfig.getString("pnj1RSpawn")));
                m.setPnj2RSpawn(MiniHub.StrToLocation(mapConfig.getString("pnj2RSpawn")));
                m.setPnj3RSpawn(MiniHub.StrToLocation(mapConfig.getString("pnj3RSpawn")));

                mapConfig.getStringList("drapeauB").forEach(s -> m.getDrapeauB().add(MiniHub.StrToLocation(s)));
                mapConfig.getStringList("drapeauR").forEach(s -> m.getDrapeauR().add(MiniHub.StrToLocation(s)));

                m.setBriquetSpawn(MiniHub.StrToLocation(mapConfig.getString("briquetSpawn")));

                m.setGoldOne(MiniHub.StrToLocation(mapConfig.getString("goldOne")));
                m.setGoldTwo(MiniHub.StrToLocation(mapConfig.getString("goldTwo")));

                maplist.put(name,m);
            }    catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            info("Map \"" + name + "\" chargée !");
        });
    }

    public static void removeMaps(String s){
        LotaWars.maplist.remove(s);
        List<String> d = LotaWars.mapsConf.getStringList("Maps");
        d.remove(s);
        mapsConf.set("Maps",d);
    }
    public void saveMaps() throws IOException, InvalidConfigurationException {
        mapsConf.set("Maps",new ArrayList<>(maplist.keySet()));
        saveMapsConfig();
    }

    public void registerThings(){
        //getCommand("wardebug").setExecutor(new WarDebug());
        getCommand("warmap").setExecutor(new WarMap());
        //getCommand("wargame").setExecutor(new WarGame());
        getCommand("warmatch").setExecutor(new WarMatch());
        getServer().getPluginManager().registerEvents(new Listener(),this);
    }


    public void saveMapsConfig() throws IOException {
        File mapsfile = new File(getDataFolder(),"maps.yml");
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        if(!mapsfile.exists()) mapsfile.createNewFile();
        mapsConf.save(mapsfile);
        File mapsDir = new File(getDataFolder(),"Maps");
        if(!mapsDir.exists()) mapsDir.mkdir();
        maplist.forEach((name,map) -> {
            if(!map.isModified()) return;
            try {
            File mapFile = new File(mapsDir, name + ".yml");
            if(!mapFile.exists()) {
                mapFile.createNewFile();
            }
            YamlConfiguration mapConfig = new YamlConfiguration();
            mapConfig.load(mapFile);
            mapConfig.set("blueSpawn", MiniHub.LocToString(map.getBlueSpawn()));
            mapConfig.set("redSpawn", MiniHub.LocToString(map.getRedSpawn()));

            mapConfig.set("pnj1BSpawn", MiniHub.LocToString(map.getPnj1BSpawn()));
            mapConfig.set("pnj2BSpawn", MiniHub.LocToString(map.getPnj2BSpawn()));
            mapConfig.set("pnj3BSpawn", MiniHub.LocToString(map.getPnj3BSpawn()));

            mapConfig.set("pnj1RSpawn", MiniHub.LocToString(map.getPnj1RSpawn()));
            mapConfig.set("pnj2RSpawn", MiniHub.LocToString(map.getPnj2RSpawn()));
            mapConfig.set("pnj3RSpawn", MiniHub.LocToString(map.getPnj3RSpawn()));

            ArrayList<String> drapB = new ArrayList<>();
            map.getDrapeauB().forEach(loc -> drapB.add(MiniHub.LocToString(loc)));

            ArrayList<String> drapR = new ArrayList<>();
            map.getDrapeauR().forEach(loc -> drapR.add(MiniHub.LocToString(loc)));

            mapConfig.set("drapeauB",drapB);
            mapConfig.set("drapeauR",drapR);

            mapConfig.set("briquetSpawn", MiniHub.LocToString(map.getBriquetSpawn()));

            mapConfig.set("goldOne", MiniHub.LocToString(map.getGoldOne()));
            mapConfig.set("goldTwo", MiniHub.LocToString(map.getGoldTwo()));

            mapConfig.save(mapFile);

            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            info("Map \"" + name + "\" sauvegardée !");
        });
    }


    public void parsePnjs(){
        File pnjsfile = new File(getDataFolder(),"pnjs.yml");
        loadConf(pnjsfile, pnjsconf);

        List<String> first = pnjsconf.getStringList("first");
        List<String> second = pnjsconf.getStringList("second");
        List<String> third = pnjsconf.getStringList("third");
        ArrayList<List<String>> all = new ArrayList<>();
        all.add(first);
        all.add(second);
        all.add(third);
        for (int i = 0; i <= 2; i++) {
            LotaWars.getRecipeItems().add(new ArrayList<>());
        }
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            all.get(i).forEach(s -> {String[] data = s.split("-->");
            VillagerTrade tr;
            if(data.length == 2) tr = new VillagerTrade(MiniHub.StringToItemStack(data[0]),null, MiniHub.StringToItemStack(data[1]),9999);
            else if(data.length == 3) tr = new VillagerTrade(MiniHub.StringToItemStack(data[0]), MiniHub.StringToItemStack(data[1]), MiniHub.StringToItemStack(data[2]),9999);
            else return;
            LotaWars.getRecipeItems().get(finalI).add(tr);
            });
        }
    }

    private static void loadConf(File file, YamlConfiguration confToLoad) {
        try {
        if(!file.exists()) file.createNewFile();
            confToLoad.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void parseStuff(){
        File stuffile = new File(getDataFolder(),"stuff.yml");
        YamlConfiguration configuration = new YamlConfiguration();
        loadConf(stuffile, configuration);

        List<String> items = configuration.getStringList("stuff");
        items.forEach(s -> stuff.add(MiniHub.StringToItemStack(s)));
    }


}
