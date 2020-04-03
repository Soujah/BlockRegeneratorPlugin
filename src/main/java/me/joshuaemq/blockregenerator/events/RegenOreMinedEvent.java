package me.joshuaemq.blockregenerator.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RegenOreMinedEvent extends CancellableEvent {

  private final Player player;
  private final String rewardId;
  private final String blockId;
  private final String regionId;
  private final Material minedMaterial;

  public RegenOreMinedEvent(Player player, String rewardId, String blockId, String regionId,
      Material minedMaterial) {
    this.player = player;
    this.rewardId = rewardId;
    this.blockId = blockId;
    this.regionId = regionId;
    this.minedMaterial = minedMaterial;
  }

  public Player getPlayer() {
    return player;
  }

  public String getRewardId() {
    return rewardId;
  }

  public String getBlockId() {
    return blockId;
  }

  public String getRegionId() {
    return regionId;
  }

  public Material getMinedMaterial() {
    return minedMaterial;
  }

}
