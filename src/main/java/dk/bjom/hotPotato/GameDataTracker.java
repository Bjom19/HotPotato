package dk.bjom.hotPotato;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameDataTracker {
    private static GameDataTracker instance;

    private GameDataTracker() {
        this.playerDataFile = new File(HotPotato.getInstance().getDataFolder(), "game-data.yml");
        load();
    }

    public static GameDataTracker getInstance() {
        if (instance == null) {
            instance = new GameDataTracker();
        }
        return instance;
    }

    private final File playerDataFile;

    private Player currentHolder;
    private final Set<UUID> losers = new HashSet<>();
    private boolean roundArmed = false;
    private boolean roundStarted = false;
    private long roundStartTime = 0L;
    private long holderStartTime = 0L;

    public Player getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(Player holder) {
        this.currentHolder = holder;
        save();
    }

    public void setRoundArmed(boolean roundArmed) {
        this.roundArmed = roundArmed;
        save();
    }

    public void setRoundStarted(boolean roundStarted) {
        this.roundStarted = roundStarted;
        save();
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
        save();
    }

    public void setHolderStartTime(long holderStartTime) {
        this.holderStartTime = holderStartTime;
        save();
    }

    public boolean isRoundStarted() {
        return roundStarted;
    }

    public void makeLoser(Player player) {
        if (losers.add(player.getUniqueId())) save();
    }

    public void removeLoser(Player target) {
        if (losers.remove(target.getUniqueId())) save();
    }

    public boolean isLoser(Player player) {
        return losers.contains(player.getUniqueId());
    }

    // We load this from the file every time because we want live file update without having to restart the server
    public boolean isRoundArmed() {
        if (!playerDataFile.exists()) return false;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerDataFile);
        this.roundArmed = config.getBoolean("roundArmed");
        return roundArmed;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public long getHolderStartTime() {
        return holderStartTime;
    }

    private void load() {
        if (!playerDataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerDataFile);

        // Potato holder
        String currentHolderIdS = config.getString("currentHolder", null);
        if (currentHolderIdS != null && !currentHolderIdS.equals("~")) {
            currentHolder = Bukkit.getPlayer(UUID.fromString(currentHolderIdS));
        }

        // Losers
        for (String uuidS : config.getStringList("losers")) {
            if (uuidS.equals("~")) continue;
            losers.add(UUID.fromString(uuidS));
        }

        roundArmed = config.getBoolean("roundArmed", false);
        roundStarted = config.getBoolean("roundStarted", false);
        roundStartTime = config.getLong("roundStartTime", 0L);
        holderStartTime = config.getLong("holderStartTime", 0L);
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        List<String> loserIds = new ArrayList<>();
        for (UUID uuid : losers) {
            loserIds.add(uuid.toString());
        }

        config.set("currentHolder", currentHolder == null ? "~" : currentHolder.getUniqueId().toString());
        config.set("losers", loserIds);
        config.set("roundArmed", roundArmed);
        config.set("roundStarted", roundStarted);
        config.set("roundStartTime", roundStartTime);
        config.set("holderStartTime", holderStartTime);

        // Save
        try {
            config.save(playerDataFile);
        } catch (Exception e) {
            HotPotato.getInstance().getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    public void resetGameData() {
        currentHolder = null;
        roundArmed = false;
        roundStarted = false;
        roundStartTime = 0L;
        holderStartTime = 0L;
        save();
    }

    public List<Player> getLosers() {
        List<Player> loserPlayers = new ArrayList<>();
        for (UUID uuid : losers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                loserPlayers.add(player);
            }
        }
        return loserPlayers;
    }
}
