package me.joshuaemq.blockregenerator.tasks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.joshuaemq.blockregenerator.managers.SQLManager;
import me.joshuaemq.blockregenerator.objects.BlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockRespawnTask extends BukkitRunnable {

  private SQLManager sqlManager;

  public BlockRespawnTask(SQLManager sqlManager) {
    this.sqlManager = sqlManager;
  }

  @Override
  public void run() {
    CompletableFuture.supplyAsync(() -> sqlManager.getRespawnBlocks()).thenAccept(this::placeBlocks);
  }

  private void placeBlocks(List<BlockData> blockDataList) {
    for (BlockData b : blockDataList) {
      Location loc = new Location(Bukkit.getWorld(b.getWorld()), b.getX(), b.getY(), b.getZ());
      Block block = loc.getBlock();
      block.setType(Material.valueOf(b.getMaterial()));
      sqlManager.removeItem(b.getId());
    }
  }
}
