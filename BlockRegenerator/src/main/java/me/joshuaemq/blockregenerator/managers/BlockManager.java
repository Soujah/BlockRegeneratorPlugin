package me.joshuaemq.blockregenerator.managers;

import java.util.HashSet;
import java.util.Set;
import me.joshuaemq.blockregenerator.objects.BlockData;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class BlockManager {

  private final Map<String, Map<Material, BlockData>> blockMap;

  public BlockManager() {
    this.blockMap = new HashMap<>();
  }

  public void setBlockMap(Map<String, Map<Material, BlockData>> blockMap) {
    this.blockMap.clear();
    this.blockMap.putAll(blockMap);
  }

  public void addBlock(String region, Material material, BlockData block) {
    if (blockMap.containsKey(region)) {
      blockMap.get(region).put(material, block);
      return;
    }
    Map<Material, BlockData> newData = new HashMap<>();
    blockMap.put(region, newData);
  }

  public BlockData getBlock(String region, Material material) {
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
