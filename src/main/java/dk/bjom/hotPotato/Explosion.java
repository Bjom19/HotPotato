package dk.bjom.hotPotato;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Explosion {
    private static final ParticleBuilder explosion = Particle.EXPLOSION.builder()
            .count(5)
            .extra(0.5);
    private static final ParticleBuilder flame = Particle.FLAME.builder()
            .count(10)
            .extra(0.2);
    private static final ParticleBuilder largeSmoke = Particle.LARGE_SMOKE.builder()
            .count(10)
            .extra(0.5);
    private static final ParticleBuilder smoke =  Particle.SMOKE.builder()
            .count(20)
            .extra(0.5);
    private static final ParticleBuilder lava = Particle.LAVA.builder()
            .count(5)
            .extra(0.5);
    private static final ParticleBuilder poof = Particle.POOF.builder()
            .count(10)
            .extra(0.5);

    private static final Sound explosionSound = Sound.sound(Key.key("entity.dragon_fireball.explode"), Sound.Source.MASTER, 1.0f, 1.0f);
    private static final Sound fireSound = Sound.sound(Key.key("entity.ender_dragon.shoot"), Sound.Source.PLAYER, 1.0f, 1.0f);
    private static final Sound extinguishSound = Sound.sound(Key.key("block.fire.extinguish"), Sound.Source.PLAYER, 0.5f, 1.0f);

    public static void explode(Location location) {
        explosion.location(location).spawn();
        flame.location(location).spawn();
        largeSmoke.location(location).spawn();
        smoke.location(location).spawn();
        lava.location(location).spawn();
        poof.location(location).spawn();

        ForwardingAudience audience = HotPotato.getInstance().getServer();
        audience.playSound(explosionSound, location.x(), location.y(), location.z());
        audience.playSound(fireSound, location.x(), location.y(), location.z());
        audience.playSound(extinguishSound, location.x(), location.y(), location.z());
    }
}
