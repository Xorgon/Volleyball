package me.xorgon.volleyball.schedulers;

import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.objects.Court;

public class SetBallFacing implements Runnable {

    private VManager manager;

    public SetBallFacing(VManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        for (Court court : manager.getCourts().values())
            if (court.getBall() != null) {
                court.getBall().setRotation(0, 0);
            }
    }
}

