package me.joshuaemq.blockregenerator.objects;

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

    public BlockData(double exhaustChance, double lootChance, Material depleteMaterial, int respawnTime) {
        this.random = new Random();
        this.exhaustChance = exhaustChance;
        this.lootChance = lootChance;
        this.depleteMaterial = depleteMaterial;
        this.respawnTime = respawnTime;
        this.rewardMap = new HashMap<>();
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
        for (double rewardWeight : rewardMap.values()) {
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
