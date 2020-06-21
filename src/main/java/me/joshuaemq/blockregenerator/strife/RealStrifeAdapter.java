package me.joshuaemq.blockregenerator.strife;

import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RealStrifeAdapter extends StrifeAdapter {
  private final land.face.strife.StrifePlugin strifePlugin;

  public RealStrifeAdapter(Plugin strifePlugin) {
    this.strifePlugin = (land.face.strife.StrifePlugin) strifePlugin;
  }

  @Override
  public void addMiningExperience(Player player, double amount, boolean exact) {
    strifePlugin
        .getSkillExperienceManager()
        .addExperience(player, land.face.strife.data.champion.LifeSkillType.MINING, amount, exact, false);
  }

  @Override
  public int getMiningSkillLevel(Player player) {
    return PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.MINING);
  }

  @Override
  public double getEffectiveMiningSkillLevel(Player player) {
    return PlayerDataUtil.getEffectiveLifeSkill(player, LifeSkillType.MINING, true);
  }
}
