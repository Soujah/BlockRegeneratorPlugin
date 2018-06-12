package me.joshuaemq.blockregenerator.objects;

import me.joshuaemq.blockregenerator.managers.BlockManager;
import me.joshuaemq.blockregenerator.managers.MineRewardManager;
import org.bukkit.inventory.ItemStack;

public class MineReward {

    private MineRewardManager mineRewardManager;
    private BlockManager blockManager;

    private final ItemStack itemStack;
    private final float experience;
    private final int levelRequirement;

    public MineReward(ItemStack item, float experience, int levelRequirement) {
        this.itemStack = item;
        this.experience = experience;
        this.levelRequirement = levelRequirement;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public float getExperience() {
        return experience;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }
}
