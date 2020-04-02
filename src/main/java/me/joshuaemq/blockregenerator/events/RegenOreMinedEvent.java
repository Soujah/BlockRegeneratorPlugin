package me.joshuaemq.blockregenerator.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RegenOreMinedEvent extends CancellableEvent {

  private Player player;
  private String rewardId;
  private String regionId;
  private Material minedMaterial;

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

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
