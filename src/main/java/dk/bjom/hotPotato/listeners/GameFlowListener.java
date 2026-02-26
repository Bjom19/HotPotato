package dk.bjom.hotPotato.listeners;

import dk.bjom.hotPotato.GameService;
import dk.bjom.hotPotato.HotPotato;
import dk.bjom.hotPotato.PotatoItem;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class GameFlowListener implements Listener {

    private final GameService gameService;
    private final HotPotato plugin;

    public GameFlowListener(GameService gameService, HotPotato plugin) {
        this.gameService = gameService;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
        if (!(event.getAttacked() instanceof Player attacked)) return;
        if (!gameService.isRoundRunning()) return;

        Player attacker = event.getPlayer();
        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (!PotatoItem.isPotato(item)) return;

        if (gameService.isLoser(attacked)) {
            event.setCancelled(!gameService.withinGracePeriod());
            return;
        }

        gameService.tagPlayer(attacker, attacked, item);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (gameService.isLoser(player)) return;

        // Delay one tick so cursor items are fully returned to inventory before we check
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (ItemStack item : player.getInventory().getStorageContents()) {
                if (PotatoItem.isPotato(item)) {
                    gameService.triggerArmedStart(player);
                    return;
                }
            }
        });
    }

    @EventHandler
    public void onPotatoGroundPickup(EntityPickupItemEvent event) {
        if (!gameService.isRoundArmed()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (gameService.isLoser(player)) return;
        if (!PotatoItem.isPotato(event.getItem().getItemStack())) return;

        // Delay one tick so the item is fully added to inventory before we reposition it
        plugin.getServer().getScheduler().runTask(plugin, () -> gameService.triggerArmedStart(player));
    }
}
