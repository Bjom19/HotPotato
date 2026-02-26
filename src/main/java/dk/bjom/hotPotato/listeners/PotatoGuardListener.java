package dk.bjom.hotPotato.listeners;

import dk.bjom.hotPotato.GameService;
import dk.bjom.hotPotato.HotPotato;
import dk.bjom.hotPotato.PotatoItem;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class PotatoGuardListener implements Listener {

    private final GameService gameService;

    public PotatoGuardListener(GameService gameService, HotPotato plugin) {
        this.gameService = gameService;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void cancelIfPotato(ItemStack item, Cancellable event) {
        if (PotatoItem.isPotato(item) && gameService.isRoundRunning()) {
            event.setCancelled(true);
        }
    }

    private boolean cancelIsPlayerIsLoser(Player player, Cancellable event) {
        if (gameService.isLoser(player)) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        cancelIfPotato(event.getItem().getItemStack(), event);
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        cancelIfPotato(event.getItem(), event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!gameService.isRoundRunning()) return;
        if (PotatoItem.isPotato(event.getCurrentItem()) || PotatoItem.isPotato(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        cancelIfPotato(event.getOldCursor(), event);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        cancelIfPotato(event.getItemDrop().getItemStack(), event);
    }

    @EventHandler
    public void onEntityDrop(EntityDropItemEvent event) {
        cancelIfPotato(event.getItemDrop().getItemStack(), event);
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        cancelIfPotato(event.getMainHandItem(), event);
        cancelIfPotato(event.getOffHandItem(), event);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        cancelIfPotato(event.getItem(), event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameService.isRoundRunning()) return;
        Player player = event.getEntity();
        if (gameService.isLoser(player)) return;

        for (ItemStack item : event.getDrops()) {
            if (PotatoItem.isPotato(item)) {
                event.getDrops().remove(item);
                event.getItemsToKeep().add(item);
            }
        }
    }

    @EventHandler
    public void onItemFrameChange(PlayerItemFrameChangeEvent event) {
        cancelIfPotato(event.getItemStack(), event);
    }

    @EventHandler
    public void onItemInteractEvent(PlayerInteractEvent event) {
        cancelIfPotato(event.getItem(), event);
    }
}
