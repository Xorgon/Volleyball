package me.xorgon.volleyball.schedulers;

import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.objects.Court;
import net.minecraft.server.v1_13_R2.EntitySlime;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftSlime;

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

