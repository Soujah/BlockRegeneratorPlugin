package me.joshuaemq.blockregenerator.objects;

import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockData {

    private final Random random;
    private final double exhaustChance;
    private final double lootChance;
    private final Material depleteMaterial;
    private final int respawnTime;
    private final Map<String, Double> rewardMap;

    public BlockData(double exhaustChance, double lootChance, Material depleteMaterial, int respawnTime, Map rewardMap) {
        this.random = new Random();
        this.exhaustChance = exhaustChance;
        this.lootChance = lootChance;
        this.depleteMaterial = depleteMaterial;
        this.respawnTime = respawnTime;
        this.rewardMap = rewardMap;
    }

    public double getExhaustChance() {
        return exhaustChance;
    }

    public double getLootChance() {
        return lootChance;
    }

    public Material getDepleteMaterial() {
        return depleteMaterial;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public Map<String, Double> getRewardMap() {
        return rewardMap;
    }

    public String getRandomReward() {
        double weightTotal = 0;
        Bukkit.getServer().getLogger().info("REWARD MAP: " + this.getRewardMap().toString());
        for (double rewardWeight : this.getRewardMap().values()) {
            weightTotal += rewardWeight;
        }
        double targetWeight = random.nextDouble() * weightTotal;
        while (targetWeight > 0) {
            for (String reward : rewardMap.keySet()) {
                targetWeight -= rewardMap.get(reward);
                if (targetWeight < 0) {
                    return reward;
                }
            }
        }
        return null;
    }


}
