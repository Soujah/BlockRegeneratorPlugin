package me.joshuaemq.blockregenerator.tasks;

import me.joshuaemq.blockregenerator.managers.SQLManager;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockRespawnTask extends BukkitRunnable {

  private SQLManager sqlManager;

  public BlockRespawnTask(SQLManager sqlManager) {
    this.sqlManager = sqlManager;
  }

  @Override
  public void run() {
    sqlManager.check();
  }
}
