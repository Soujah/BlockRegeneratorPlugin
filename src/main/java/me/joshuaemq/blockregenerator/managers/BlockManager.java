package me.joshuaemq.blockregenerator.managers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.objects.BlockData;
import me.joshuaemq.blockregenerator.objects.RegenBlock;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockManager {

  private final BlockRegeneratorPlugin plugin;
  private final Map<String, Map<Material, RegenBlock>> blockMap;

  public BlockManager(BlockRegeneratorPlugin plugin) {
    this.plugin = plugin;
    this.blockMap = new HashMap<>();
  }

  public interface BlockActionCallback {

    List<BlockData> onGetBlocksComplete();
  }

  public interface InsertActionCallback {

    Integer onInsertComplete();
  }

  public void doOreRespawn() {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      BlockActionCallback callback = () -> plugin.getSQLManager().getRespawnBlocks();
      Bukkit.getScheduler().runTask(plugin, () -> placeBlocks(callback.onGetBlocksComplete()));
    });
  }

  public void respawnAllOres() {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      BlockActionCallback callback = () -> plugin.getSQLManager().getAllDespawnedBlocks();
      Bukkit.getScheduler().runTask(plugin, () -> placeBlocks(callback.onGetBlocksComplete()));
    });
  }

  public void insertBlock(String material, int x, int y, int z, String world, long respawnTime,
      Material depleteMaterial, Location depleteLocation) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      InsertActionCallback callback = () -> plugin.getSQLManager().insertBlock(material, x, y, z, world, respawnTime);
      Bukkit.getScheduler().runTask(plugin, () -> depleteBlock(callback.onInsertComplete(), depleteLocation, depleteMaterial));
    });
  }

  private void placeBlocks(List<BlockData> blockDataList) {
    for (BlockData b : blockDataList) {
      placeBlock(b);
    }
  }

  private void placeBlock(BlockData b) {
    Location loc = new Location(Bukkit.getWorld(b.getWorld()), b.getX(), b.getY(), b.getZ());
    Block block = loc.getBlock();
    block.setType(Material.valueOf(b.getMaterial()));
    plugin.getSQLManager().removeItem(b.getId());
  }

  private void depleteBlock(int complete, Location location, Material material) {
    if (complete > 0) {
      location.getBlock().setType(material);
    }
  }

  public void setBlockMap(Map<String, Map<Material, RegenBlock>> blockMap) {
    this.blockMap.clear();
    this.blockMap.putAll(blockMap);
  }

  public void addBlock(String region, Material material, RegenBlock block) {
    if (blockMap.containsKey(region)) {
      blockMap.get(region).put(material, block);
      return;
    }
    Map<Material, RegenBlock> newData = new HashMap<>();
    blockMap.put(region, newData);
  }

  public RegenBlock getBlock(String region, Material material) {
    if (!blockMap.containsKey(region)) {
      return null;
    }
    return blockMap.get(region).getOrDefault(material, null);
  }

  public Set<Material> getValidMaterials(String region) {
    if (!blockMap.containsKey(region)) {
      return new HashSet<>();
    }
    return blockMap.get(region).keySet();
  }

  public boolean containsRegion(String region) {
    return blockMap.containsKey(region);
  }
}
