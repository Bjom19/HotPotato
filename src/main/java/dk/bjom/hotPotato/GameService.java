package dk.bjom.hotPotato;

import dk.bjom.hotPotato.webhook.WebhookHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class GameService {
    private final GameDataTracker tracker = GameDataTracker.getInstance();
    private final HotPotato plugin = HotPotato.getInstance();

    private final WebhookHandler wh;

    private long gracePeriodStart = 0;

    private final List<BukkitTask> tasks = new ArrayList<>();

    public GameService(
            final WebhookHandler webhookHandler
    ) {
        this.wh = webhookHandler;
    }

    // --- State access ---

    public boolean isRoundRunning() {
        return tracker.isRoundStarted();
    }

    public boolean isRoundArmed() {
        return tracker.isRoundArmed();
    }

    public boolean isLoser(Player player) {
        return tracker.isLoser(player);
    }

    // --- Game actions ---

    public void startRound() {
        tracker.setRoundArmed(false);
        tracker.setRoundStarted(true);
        tracker.setRoundStartTime(System.currentTimeMillis());
        checkTimers(20); // Check timers every second (20 ticks)
    }

    public void endRound() {
        Player holder = tracker.getCurrentHolder();
        java.util.UUID holderUUID = tracker.getCurrentHolderUUID();
        tracker.setRoundStarted(false);
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();

        if (holderUUID == null) return;
        boolean explodeMakesLoser = plugin.getConfig().getBoolean("explodeMakesLoser", true);
        if (explodeMakesLoser) tracker.makeLoser(holderUUID);

        plugin.getServer().broadcast(Component.text("A life was claimed by the inferno of the hot potato...", NamedTextColor.DARK_RED));
        if (holder != null) {
            EffectService.endGame(holder);
        } else {
            tracker.addPendingEndGame(holderUUID);
        }

        trySendMessage("Round ended! " + (holder != null ? holder.getName() : "An unknown player") + " was holding the potato.");

        tracker.setCurrentHolder(null);
        tracker.setRoundStartTime(0);
        tracker.setHolderStartTime(0);
    }

    public boolean withinGracePeriod() {
        long elapsed = System.currentTimeMillis() - gracePeriodStart;
        long graceDuration = plugin.getConfig().getLong("graceTime");
        return elapsed < graceDuration;
    }

    public void tagPlayer(Player tagger, Player tagged, ItemStack potato) {
        gracePeriodStart = System.currentTimeMillis();
        tagger.getInventory().remove(potato);
        tracker.setCurrentHolder(tagged);
        givePotatoToPlayer(tagged, potato);
        resetTimer();
        trySendMessage(tagger.getName() + " tagged " + tagged.getName() + "!");
        EffectService.tagPlayer(tagger, tagged);
    }

    public void eliminateCurrentHolder() {
        Player holder = tracker.getCurrentHolder();
        if (holder == null) return;
        holder.setFireTicks(60);
    }

    public void triggerArmedStart(Player holder) {
        if (!isRoundArmed()) return;

        startRound();
        tracker.setCurrentHolder(holder);
        resetTimer();
        movePotatoToHotbar(holder);
        trySendMessage("Game started! " + holder.getName() + " is the first holder of the potato!");
    }

    public void resetTimer() {
        tracker.setHolderStartTime(System.currentTimeMillis());
    }

    // --- Timer scheduling ---

    public void checkTimers(int ticks) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!tracker.isRoundStarted()) return; // Avoid infinite timer loops if game ended
            handlePotatoTimer();
            handleGameTimer();
            checkTimers(ticks);
        }, ticks);
        tasks.add(task);
    }

    private void handlePotatoTimer() {
        Player holder = tracker.getCurrentHolder();
        if (holder == null) return;

        // Safety: if the potato disappeared (e.g. /clear, creative clear button), restore it
        boolean hasPotato = false;
        for (ItemStack item : holder.getInventory().getStorageContents()) {
            if (PotatoItem.isPotato(item)) {
                hasPotato = true;
                break;
            }
        }
        if (!hasPotato) {
            givePotatoToPlayer(holder, PotatoItem.create());
            holder.sendMessage(Component.text("It finds its way back. You cannot escape it.", NamedTextColor.DARK_RED));
        }

        long elapsed = System.currentTimeMillis() - tracker.getHolderStartTime();
        long timeLimit = plugin.getConfig().getLong("holdLimit");
        if (elapsed >= timeLimit) {
            eliminateCurrentHolder();
        }
    }

    private void handleGameTimer() {
        long elapsed = System.currentTimeMillis() - tracker.getRoundStartTime();
        long gameDuration = plugin.getConfig().getLong("roundDuration");
        if (elapsed >= gameDuration) {
            endRound();
        }
    }

    // --- Private inventory helpers ---

    private void givePotatoToPlayer(Player player, ItemStack potato) {
        // Find an empty hotbar slot (0-8) to place the potato without displacing any item
        int targetSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                targetSlot = i;
                break;
            }
        }

        if (targetSlot != -1) {
            player.getInventory().setItem(targetSlot, potato);
        } else {
            // All hotbar slots occupied — swap held item into first free inventory slot
            int heldSlot = player.getInventory().getHeldItemSlot();
            ItemStack displaced = player.getInventory().getItem(heldSlot);
            int freeSlot = -1;
            for (int i = 9; i < 36; i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if (slot == null || slot.getType() == Material.AIR) {
                    freeSlot = i;
                    break;
                }
            }
            player.getInventory().setItem(heldSlot, potato);
            if (freeSlot != -1) {
                player.getInventory().setItem(freeSlot, displaced);
            } else if (displaced != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), displaced);
            }
        }
    }

    private void movePotatoToHotbar(Player player) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (PotatoItem.isPotato(contents[i])) {
                if (i < 9) return; // Already in hotbar
                ItemStack potato = contents[i];
                player.getInventory().setItem(i, null);
                givePotatoToPlayer(player, potato);
                return;
            }
        }
    }

    // Other stuff
    private void trySendMessage(String message) {
        try {
            wh.SendMessage(message);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send message to webhook: " + e.getMessage());
        }
    }
}
