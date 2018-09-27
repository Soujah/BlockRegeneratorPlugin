package me.joshuaemq.blockregenerator.managers;

import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.objects.MineReward;

import java.util.HashMap;
import java.util.Map;

public class MineRewardManager {

    private final BlockRegeneratorPlugin plugin;
    private final Map<String, MineReward> rewardMap;

    public MineRewardManager(BlockRegeneratorPlugin plugin) {
        this.plugin = plugin;
        this.rewardMap = new HashMap<>();
    }

    public void addReward(String identifier, MineReward reward) {
        rewardMap.put(identifier, reward);
    }

    public MineReward getReward(String identifier) {
        if (rewardMap.containsKey(identifier)) {
            return rewardMap.get(identifier);
        }
        plugin.getLogger().warning("No reward with the identifier " + identifier + " exists!");
        return null;
    }
}
