package me.xorgon.volleyball.schedulers;

import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.events.BallLandEvent;
import me.xorgon.volleyball.objects.Court;
import net.minecraft.server.v1_11_R1.EntitySlime;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftSlime;
import org.bukkit.entity.Arrow;

public class SetBallFacing implements Runnable {

    private VManager manager;

    public SetBallFacing(VManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        for (Court court : manager.getCourts().values())
            if (court.getBall() != null) {
                EntitySlime handle = ((CraftSlime) court.getBall()).getHandle();
                handle.yaw = 0;
                handle.velocityChanged = true;
            }
    }
}

