package me.joshuaemq.blockregenerator.events;

import org.bukkit.Material;

public class RegenOreMinedEvent extends CancellableEvent {

  private String rewardId;
  private String regionId;
  private Material minedMaterial;

  public String getRewardId() {
    return rewardId;
  }

  public void setRewardId(String rewardId) {
    this.rewardId = rewardId;
  }

  public String getRegionId() {
    return regionId;
  }

  public void setRegionId(String regionId) {
    this.regionId = regionId;
  }

  public Material getMinedMaterial() {
    return minedMaterial;
  }

  public void setMinedMaterial(Material minedMaterial) {
    this.minedMaterial = minedMaterial;
  }

}
