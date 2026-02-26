package dk.bjom.hotPotato;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public class EffectService {
    public static void endGame(Player loser) {
        loser.getInventory().clear();
        loser.damage(100000000);
        Explosion.explode(loser.getLocation());
    }

    public static void tagPlayer(Player tagger, Player tagged) {
        Title title = Title.title(
                Component.text("You've got the hot potato!"),
                Component.text("Quickly find someone else to give it to before you burn!"),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofSeconds(1))
        );
        tagged.showTitle(title);

        Sound tagSound = Sound.sound(Key.key("entity.ender_dragon.shoot"), Sound.Source.PLAYER, 1.0f, 1.0f);
        tagger.playSound(tagSound);
        tagged.playSound(tagSound);

        tagged.setFireTicks(30);

    }
}
