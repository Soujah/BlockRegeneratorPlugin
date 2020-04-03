package me.joshuaemq.blockregenerator.objects;

import org.bukkit.Material;

import java.util.Map;
import java.util.Random;

public class RegenBlock {

  private final String id;
  private final double exhaustChance;
  private final double lootChance;
  private final Material depleteMaterial;
  private final int respawnTime;
  private final Map<String, Double> rewardMap;

  private static final Random random = new Random();

  public RegenBlock(String id, double exhaustChance, double lootChance, Material depleteMaterial,
      int respawnTime, Map<String, Double> rewardMap) {
    this.id = id;
    this.exhaustChance = exhaustChance;
    this.lootChance = lootChance;
    this.depleteMaterial = depleteMaterial;
    this.respawnTime = respawnTime;
    this.rewardMap = rewardMap;
  }

  public String getId() {
    return id;
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

  public static String getRandomReward(RegenBlock block) {
    double weightTotal = 0;
    for (double rewardWeight : block.rewardMap.values()) {
      weightTotal += rewardWeight;
    }
    double targetWeight = random.nextDouble() * weightTotal;
    while (targetWeight > 0) {
      for (String reward : block.rewardMap.keySet()) {
        targetWeight -= block.rewardMap.get(reward);
        if (targetWeight < 0) {
          return reward;
        }
      }
    }
    return null;
  }
}
