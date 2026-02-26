package dk.bjom.hotPotato;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PotatoItem {
    private static GameDataTracker tracker = GameDataTracker.getInstance();
    private static HotPotato plugin = HotPotato.getInstance();

    public static final NamespacedKey KEY = new NamespacedKey("hotpotato", "is_potato");

    private PotatoItem() {
    }

    public static boolean isPotato(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return Boolean.TRUE.equals(item.getPersistentDataContainer().get(KEY, PersistentDataType.BOOLEAN));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("hotpotato")
                .requires(source -> source.getSender().hasPermission("hotpotato.commands"))
                .then(Commands.literal("give")
                        .executes(PotatoItem::runGiveCommand))
                .then(Commands.literal("status")
                        .executes(PotatoItem::runStatusCommand))
                .then(Commands.literal("engage")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes(PotatoItem::runEngageCommand)))
                .then(Commands.literal("disengage")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes(PotatoItem::runDisengageCommand)))
                .then(Commands.literal("reset")
                        .executes(PotatoItem::runStopCommand))
                .then(Commands.literal("arm")
                        .executes(PotatoItem::runArmRoundCommand))
                .then(Commands.literal("unarm")
                        .executes(PotatoItem::runUnarmRoundCommand));
    }

    private static int runStopCommand(CommandContext<CommandSourceStack> ctx) {
        tracker.resetGameData();
        return Command.SINGLE_SUCCESS;
    }

    private static int runArmRoundCommand(CommandContext<CommandSourceStack> ctx) {
        tracker.setRoundArmed(true);

        Entity executor = ctx.getSource().getExecutor();
        if (executor != null) executor.sendActionBar(Component.text("The potato has been armed", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }

    private static int runUnarmRoundCommand(CommandContext<CommandSourceStack> ctx) {
        tracker.setRoundArmed(false);

        Entity executor = ctx.getSource().getExecutor();
        if (executor != null) executor.sendActionBar(Component.text("The potato has been unarmed", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }

    private static int runGiveCommand(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();
        if (!(executor instanceof Player player)) return Command.SINGLE_SUCCESS;

        ItemStack potato = new ItemStack(Material.BAKED_POTATO);
        setPotatoMeta(potato);
        player.give(potato);

        return Command.SINGLE_SUCCESS;
    }

    private static int runDisengageCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(ctx.getSource()).getFirst();

        tracker.makeLoser(target);

        Entity executor = ctx.getSource().getExecutor();
        if (executor != null) executor.sendMessage(Component.text("Removed " + target.getName() + " with UUID" + target.getUniqueId() + " from the game", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }

    private static int runEngageCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(ctx.getSource()).getFirst();

        tracker.removeLoser(target);

        Entity executor = ctx.getSource().getExecutor();
        if (executor != null) executor.sendMessage(Component.text("Added " + target.getName() + " with UUID" + target.getUniqueId() + " to the game", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }

    private static int runStatusCommand(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();
        if (executor == null) return Command.SINGLE_SUCCESS;

        Player holder = tracker.getCurrentHolder();
        if (holder != null) {
            executor.sendMessage(Component.text("Current potato holder: " + holder.getName(), NamedTextColor.YELLOW));
        } else {
            executor.sendMessage(Component.text("The potato is currently not held by anyone.", NamedTextColor.YELLOW));
        }

        boolean roundArmed = tracker.isRoundArmed();
        executor.sendMessage(Component.text("Round armed: " + (roundArmed ? "Yes" : "No"), NamedTextColor.YELLOW));

        long roundStart = tracker.getRoundStartTime();
        long roundDuration = plugin.getConfig().getLong("roundDuration", 12000) / 1000; // Convert from milliseconds to seconds
        if (roundStart > 0) {
            long elapsedSeconds = (System.currentTimeMillis() - roundStart) / 1000;
            executor.sendMessage(Component.text("Round has been active for: " + elapsedSeconds + " seconds, and will end in: " + (roundDuration - elapsedSeconds) + " seconds.", NamedTextColor.YELLOW));
        } else {
            executor.sendMessage(Component.text("The round is currently not active.", NamedTextColor.YELLOW));
        }

        List<Player> losers = tracker.getLosers();
        if (losers.isEmpty()) {
            executor.sendMessage(Component.text("No players have been eliminated yet.", NamedTextColor.YELLOW));
        } else {
            executor.sendMessage(Component.text("Eliminated players:", NamedTextColor.YELLOW));
            tracker.getLosers().forEach(player -> {
                executor.sendMessage(Component.text("- " + player.getName(), NamedTextColor.RED));
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void setPotatoMeta(ItemStack potato) {
        ItemMeta meta = potato.getItemMeta();
        meta.customName(Component.text("Incredibly hot potato", NamedTextColor.GOLD));
        meta.lore(List.of(Component.text("It would be a bad idea to hold this for too long...", NamedTextColor.DARK_RED)));
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);
        potato.setItemMeta(meta);
    }
}
