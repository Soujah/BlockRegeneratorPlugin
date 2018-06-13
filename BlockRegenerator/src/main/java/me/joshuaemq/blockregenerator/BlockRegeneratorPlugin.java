package me.joshuaemq.blockregenerator;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;

import me.joshuaemq.blockregenerator.commands.BaseCommand;
import me.joshuaemq.blockregenerator.listeners.BlockBreakListener;
import me.joshuaemq.blockregenerator.managers.BlockManager;
import me.joshuaemq.blockregenerator.managers.MineRewardManager;
import me.joshuaemq.blockregenerator.managers.SQLManager;
import me.joshuaemq.blockregenerator.objects.BlockData;
import me.joshuaemq.blockregenerator.objects.MineReward;
import me.joshuaemq.blockregenerator.tasks.BlockRespawnTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

public class BlockRegeneratorPlugin extends JavaPlugin {

  private CommandHandler commandHandler;

  private WorldGuardPlugin worldGuardPlugin;
  private MineRewardManager mineRewardManager;
  private BlockManager blockManager;
  private SQLManager sqlManager;
  private BlockRespawnTask blockRespawnTask;

  private FileConfiguration blockTableData;
  private FileConfiguration lootItemsData;

  public void onEnable() {
    commandHandler = new CommandHandler(this);
    commandHandler.registerCommands(new BaseCommand(this));

    worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");

    blockTableData = YamlConfiguration
        .loadConfiguration(new File(getDataFolder(), "blocks.yml"));
    lootItemsData = YamlConfiguration
        .loadConfiguration(new File(getDataFolder(), "items.yml"));

    Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);

    mineRewardManager = new MineRewardManager(this);
    blockManager = new BlockManager();

    sqlManager = new SQLManager(this);
    sqlManager.initDatabase();

    blockRespawnTask = new BlockRespawnTask(sqlManager);
    blockRespawnTask.runTaskTimer(this, 200L, 4 * 20L);

    getConfig().options().copyDefaults(true);
    saveConfig();
    reloadConfig();

    buildConfig("items.yml");
    buildConfig("blocks.yml");
    saveConfig();

    loadBlocks();
    loadItems();

    Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Enabled!");
  }

  public void onDisable() {
    HandlerList.unregisterAll();

    commandHandler = null;

    sqlManager = null;
    mineRewardManager = null;
    blockManager = null;

    blockRespawnTask.cancel();
    Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Disabled!");
  }

  public WorldGuardPlugin getWorldGuard() {
    return worldGuardPlugin;
  }

  public FileConfiguration getLootTable() {
    return blockTableData;
  }

  public FileConfiguration getLootItems() {
    return lootItemsData;
  }

  private void buildConfig(String fileName) { //creates data config for loot to be made
    File file = new File(getDataFolder(), fileName);

    if (file.exists()) {
      return;
    }
    try {
      file.createNewFile();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    try {
      configuration.save(file);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  private void loadItems() {
    for (String id : lootItemsData.getKeys(false)) {
      Material material;
      try {
        material = Material.valueOf(lootItemsData.getString(id + ".material"));
      } catch (Exception e) {
        getLogger().severe("Invalid material name! Failed to load!");
        continue;
      }
      String name = TextUtils.color(lootItemsData.getString(id + ".display-name", "Item"));
      List<String> lore = TextUtils.color(lootItemsData.getStringList(id + ".lore"));
      ItemStack itemStack = new ItemStack(material);
      ItemMeta meta = itemStack.getItemMeta();
      meta.setDisplayName(name);
      meta.setLore(lore);
      itemStack.setItemMeta(meta);

      int levelRequirement = lootItemsData.getInt(id + ".level-requirement", 0);
      float experience = (float) lootItemsData.getDouble(id + ".experience", 0);

      MineReward mineReward = new MineReward(itemStack, experience, levelRequirement);

      mineRewardManager.addReward(id, mineReward);
    }
  }

  private void loadBlocks() {
    Map<String, Map<Material, BlockData>> blockMap = new HashMap<>();
    for (String regionId : blockTableData.getKeys(false)) {
      Map<Material, BlockData> materialBlockMap = new HashMap<>();
      ConfigurationSection oreSection = blockTableData.getConfigurationSection(regionId);
      for (String oreType : oreSection.getKeys(false)) {
        Material oreMaterial;
        try {
          oreMaterial = Material.getMaterial(oreType);
        } catch (Exception e) {
          getLogger().warning("Skipping bad material type " + oreType);
          continue;
        }
        Material replacementMaterial;
        try {
          String replaceOre = oreSection.getString(oreType + ".replace-material", "BEDROCK");
          replacementMaterial = Material.getMaterial(replaceOre);
        } catch (Exception e) {
          getLogger().warning("Bad replacement material for " + regionId + "+" + oreType);
          replacementMaterial = Material.BEDROCK;
        }
        int oreRespawn = oreSection.getInt(oreType + ".respawn-millis");
        double exhaust = oreSection.getDouble(oreType + ".exhaust-chance");
        double lootChance = oreSection.getDouble(oreType + ".loot-chance");

        ConfigurationSection rewardSection = oreSection.getConfigurationSection(oreType + ".rewards");
        Map<String, Double> rewardAndWeightMap = new HashMap<>();
        for (String rewardName : rewardSection.getKeys(false)) {
          rewardAndWeightMap.put(rewardName, rewardSection.getDouble(rewardName));
        }
        BlockData blockData = new BlockData(exhaust, lootChance, replacementMaterial, oreRespawn,
            rewardAndWeightMap);

        materialBlockMap.put(oreMaterial, blockData);
      }
      blockMap.put(regionId, materialBlockMap);
    }
    blockManager.setBlockMap(blockMap);
  }

  public SQLManager getSQLManager() {
    return sqlManager;
  }

  public MineRewardManager getMineRewardManager() {
    return mineRewardManager;
  }

  public BlockManager getBlockManager() {
    return blockManager;
  }

}
