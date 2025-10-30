package me.xorgon.volleyball.effects;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.RandomUtils;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import java.util.Random;

public class BallLandEffect extends Effect {

    private Court court;
    private int count;
    private int particles = 50;
    private Court.Team scoringTeam;

    public BallLandEffect(EffectManager effectManager, Court court, Court.Team scoringTeam) {
        super(effectManager);
        type = EffectType.REPEATING;
        period = 1;
        iterations = 30;
        count = 0;
        this.court = court;
        this.scoringTeam = scoringTeam;
        setEntity(court.getBall());
    }

    @Override
    public void onRun() {
        Color color = scoringTeam == Court.Team.RED ? Color.RED : Color.BLUE;
        Location location = this.getLocation();
        for (int i = 0; i < particles; i++) {
            Vector vector = RandomUtils.getRandomVector().multiply(count/7.5);
            location.add(vector);
            display(Particle.DUST, location, color);
            location.subtract(vector);
        }
        if (new Random().nextDouble() < 0.2){ // Random low frequency.
            if (Math.pow(new Random().nextDouble(), 2) * iterations/30 > 0.25) {
                location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2F, 0.5F);
            } else {
                location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2F, 0F);
            }
        }
        count++;
    }
}
