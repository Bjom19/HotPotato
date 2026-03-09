package dk.bjom.hotPotato;

import dk.bjom.hotPotato.listeners.GameFlowListener;
import dk.bjom.hotPotato.listeners.PotatoGuardListener;
import dk.bjom.hotPotato.webhook.WebhookHandler;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class HotPotato extends JavaPlugin {
    WebhookHandler webhookHandler;
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

        initializeWebhook();

        GameService gameService = new GameService(webhookHandler);
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

        if (webhookHandler != null) {
            try {
                webhookHandler.SendMessage("🔴 Hot potato plugin is now disabled!");
            } catch (Exception e) {
                getLogger().severe("Failed to send shutdown message to webhook: " + e.getMessage());
            }
        }
    }

    private void initializeWebhook() {
        String maintenanceUsername = getConfig().getString("webhook.username", "Maintenance Bot");
        String maintenanceAvatarUrl = getConfig().getString("webhook.avatar_url", "https://i.imgur.com/4M34hi2.png");
        boolean webhookEnabled = getConfig().getBoolean("webhook.enabled", false);
        String url = getConfig().getString("webhook.url", "");

        if (!url.isEmpty() && webhookEnabled) {
            webhookHandler = new WebhookHandler(url, this);
            webhookHandler.SetUsername(maintenanceUsername);
            webhookHandler.SetAvatarUrl(maintenanceAvatarUrl);

            try {
                webhookHandler.SendMessage("🟢 Hot potato plugin is now enabled!");
            } catch (Exception e) {
                getLogger().severe("Failed to send test message to webhook: " + e.getMessage());
            }
        }
    }
}
