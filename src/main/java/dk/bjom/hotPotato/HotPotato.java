package dk.bjom.hotPotato;

import dk.bjom.hotPotato.listeners.GameFlowListener;
import dk.bjom.hotPotato.listeners.PotatoGuardListener;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class HotPotato extends JavaPlugin {
    private static HotPotato instance;

    public static HotPotato getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveResource("config.yml", false);
        saveResource("game-data.yml", false);
        saveDefaultConfig();

        GameService gameService = new GameService();
        if (gameService.isRoundRunning()) {
            gameService.checkTimers(20); // Resume timer for round that was active before server restart
        }

        new PotatoGuardListener(gameService, this);
        new GameFlowListener(gameService, this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(PotatoItem.getCommand().build());
        });
    }

    @Override
    public void onDisable() {
        GameDataTracker.getInstance().save();
    }
}
