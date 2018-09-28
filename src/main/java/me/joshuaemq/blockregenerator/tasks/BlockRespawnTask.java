package me.joshuaemq.blockregenerator.tasks;

import java.util.List;
import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.objects.BlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockRespawnTask {

  private BlockRegeneratorPlugin plugin;

  public BlockRespawnTask(BlockRegeneratorPlugin plugin) {
    this.plugin = plugin;
  }

  public interface BlockActionCallback {

    List<BlockData> onGetBlocksComplete();
  }

  public void doOreRespawn() {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      BlockActionCallback callback = () -> plugin.getSQLManager().getRespawnBlocks();
      Bukkit.getScheduler().runTask(plugin, () -> placeBlocks(callback.onGetBlocksComplete()));
    });
  }

  private void placeBlocks(List<BlockData> blockDataList) {
    for (BlockData b : blockDataList) {
      Location loc = new Location(Bukkit.getWorld(b.getWorld()), b.getX(), b.getY(), b.getZ());
      Block block = loc.getBlock();
      block.setType(Material.valueOf(b.getMaterial()));
      plugin.getSQLManager().removeItem(b.getId());
    }
  }
}
