package me.joshuaemq.blockregenerator.strife;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class StrifeAdapter {
    private static StrifeAdapter adapter = null;

    public static StrifeAdapter getAdapter() {
        if (adapter == null) {
            Plugin strifePlugin = Bukkit.getPluginManager().getPlugin("Strife");
            if (strifePlugin != null) {
                adapter = new RealStrifeAdapter(strifePlugin);
            } else {
                adapter = new NoOpStrifeAdapter();
            }
        }
        return adapter;
    }

    public abstract void addMiningExperience(Player player, double amount, boolean exact);

    public abstract int getMiningSkillLevel(Player player);

    public abstract double getEffectiveMiningSkillLevel(Player player);
}
