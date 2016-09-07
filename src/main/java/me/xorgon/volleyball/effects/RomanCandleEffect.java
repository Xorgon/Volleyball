package me.xorgon.volleyball.effects;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.ParticleEffect;
import de.slikey.effectlib.util.RandomUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Created by Elijah on 06/09/2016.
 */
public class RomanCandleEffect extends Effect{

    private double radius = 0.3;
    private int particles = 30;
    private double height;
    private double yIncr;
    private static int MAX_ITERATIONS = 10;

    public RomanCandleEffect(EffectManager effectManager, Location location, Color color, double height) {
        super(effectManager);
        type = EffectType.REPEATING;
        period = 1;
        iterations = MAX_ITERATIONS;

        this.color = color;
        this.height = height;

        yIncr = height/MAX_ITERATIONS;

        setLocation(location);
    }

    @Override
    public void onRun() {
        Location location = this.getLocation();
        int count = MAX_ITERATIONS - iterations;
        double yOff = count * yIncr;
        for (int i = 0; i < particles; i++) {
            Random r = new Random();
            double radFact = r.nextDouble() * radius;
            Vector vector = RandomUtils.getRandomCircleVector().multiply(radFact).add(new Vector(0, yOff + r.nextDouble() * yIncr, 0));
            location.add(vector);
            display(ParticleEffect.REDSTONE, location, color);

            if (r.nextDouble() < 0.002){ // Random low frequency.
                location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_LAUNCH, 1.5F, 1F);
            }
            if (r.nextDouble() < 0.0005) { // Really low random frequency.
                location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_TWINKLE, 1.5F, r.nextFloat() * 2);
            }
            location.subtract(vector);
        }
    }
}
