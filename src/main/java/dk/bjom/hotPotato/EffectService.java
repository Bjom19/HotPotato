package dk.bjom.hotPotato;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;

public class EffectService {
    public static void endGame(Player loser) {
        if (HotPotato.getInstance().getConfig().getBoolean("clearInvOnExplode", true)) {
            loser.getInventory().clear();
        } else {
            loser.getInventory().forEach(item -> {
                if (PotatoItem.isPotato(item)) loser.getInventory().remove(item);
            });
        }

        GameMode mode = loser.getGameMode();
        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
            loser.damage(100000000);
        } else {
            Title title = Title.title(
                    Component.text("You cannot escape.", NamedTextColor.RED),
                    Component.text(""),
                    Title.Times.times(Duration.ofMillis(10), Duration.ofSeconds(5), Duration.ofSeconds(1))
            );
            loser.showTitle(title);

            Location loc = loser.getLocation();
            loc.setY(loc.getY() - 500);
            loser.teleport(loc);
        }

        Explosion.explode(loser.getLocation());
    }

    public static void tagPlayer(Player tagger, Player tagged) {
        Title title = Title.title(
                Component.text("The curse is yours."),
                Component.text("Pass on the burden... before the flames consume you."),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofSeconds(1))
        );
        tagged.showTitle(title);

        Sound tagSound = Sound.sound(Key.key("entity.ender_dragon.shoot"), Sound.Source.PLAYER, 1.0f, 1.0f);
        tagger.playSound(tagSound);
        tagged.playSound(tagSound);

        tagged.setFireTicks(30);

    }
}
