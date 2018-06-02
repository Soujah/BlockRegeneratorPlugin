package me.joshuaemq.blockregenerator.managers;

import com.sk89q.worldedit.regions.Region;
import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.objects.BlockData;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    private final BlockRegeneratorPlugin plugin;
    private final Map<String, BlockData> blockMap;

    public BlockManager(BlockRegeneratorPlugin plugin) {
        this.plugin = plugin;
        this.blockMap = new HashMap<>();
    }

    public void addBlock(String identifier, BlockData block) {
        blockMap.put(identifier, block);
    }

    public BlockData getBlock(String region, Material blockMaterial) {
        if (blockMap.containsKey(region)) {
            return blockMap.get(region);
        }
        plugin.getLogger().severe("No blocks within " + region + " exists!");
        return null;
    }
}
