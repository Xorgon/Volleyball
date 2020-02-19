package me.xorgon.volleyball.effects;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Slime;

public class BallTrailEffect extends Effect {

    private Court court;

    public BallTrailEffect(EffectManager effectManager, Court court) {
        super(effectManager);
        type = EffectType.REPEATING;
        setEntity(court.getBall());
        period = 1;
        infinite();
        this.court = court;
    }

    @Override
    public void onRun() {
        Color color = court.getLastHitBy() == Court.Team.RED ? Color.RED : Color.BLUE;
        display(Particle.REDSTONE, ((Slime) getEntity()).getEyeLocation(), color);
    }
}
