package me.joshuaemq.blockregenerator.strife;

import org.bukkit.entity.Player;

public class NoOpStrifeAdapter extends StrifeAdapter {
    @Override
    public void addMiningExperience(Player player, double amount, boolean exact) {
        // do nothing
    }
}
