package me.joshuaemq.blockregenerator.strife;

import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RealStrifeAdapter extends StrifeAdapter {
  private final StrifePlugin strifePlugin;

  public RealStrifeAdapter(Plugin strifePlugin) {
    this.strifePlugin = (StrifePlugin) strifePlugin;
  }

  @Override
  public void addMiningExperience(Player player, double amount, boolean exact) {
    strifePlugin
        .getSkillExperienceManager()
        .addExperience(player, LifeSkillType.MINING, amount, exact);
  }
}
