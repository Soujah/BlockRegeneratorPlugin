package me.joshuaemq.blockregenerator;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration.VersionUpdateType;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.util.*;

import me.joshuaemq.blockregenerator.commands.BaseCommand;
import me.joshuaemq.blockregenerator.listeners.BlockBreakListener;
import me.joshuaemq.blockregenerator.managers.BlockManager;
import me.joshuaemq.blockregenerator.managers.MineRewardManager;
import me.joshuaemq.blockregenerator.managers.SQLManager;
import me.joshuaemq.blockregenerator.objects.RegenBlock;
import me.joshuaemq.blockregenerator.objects.MineReward;
import me.joshuaemq.blockregenerator.tasks.BlockRespawnTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import se.ranzdo.bukkit.methodcommand.CommandHandler;

public class BlockRegeneratorPlugin extends FacePlugin {

  private CommandHandler commandHandler;

  private MineRewardManager mineRewardManager;
  private BlockManager blockManager;
  private SQLManager sqlManager;
  private BlockRespawnTask blockRespawnTask;

  private VersionedSmartYamlConfiguration configYAML;
  private VersionedSmartYamlConfiguration blocksYAML;
  private VersionedSmartYamlConfiguration itemsYAML;

  @Override
  public void enable() {
    commandHandler = new CommandHandler(this);
    commandHandler.registerCommands(new BaseCommand(this));

    configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
        getResource("config.yml"),
        VersionUpdateType.BACKUP_AND_NEW);
    blocksYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "blocks.yml"),
        getResource("blocks.yml"),
        VersionUpdateType.BACKUP_AND_NEW);
    itemsYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "items.yml"),
        getResource("items.yml"),
        VersionUpdateType.BACKUP_AND_NEW);

    Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);

    mineRewardManager = new MineRewardManager(this);
    blockManager = new BlockManager();

    sqlManager = new SQLManager(configYAML);

    blockRespawnTask = new BlockRespawnTask(sqlManager);
    blockRespawnTask.runTaskTimer(this, 30 * 20L, 5 * 20L);

    loadBlocks();
    loadItems();

    Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Enabled!");
  }

  @Override
  public void disable() {
    HandlerList.unregisterAll(this);

    commandHandler = null;

    sqlManager.closeConnection();
    sqlManager = null;
    mineRewardManager = null;
    blockManager = null;

    blockRespawnTask.cancel();
    Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Disabled!");
  }

  private void loadItems() {
    for (String id : itemsYAML.getKeys(false)) {
      if (!itemsYAML.isConfigurationSection(id)) {
        continue;
      }
      Material material;
      try {
        material = Material.valueOf(itemsYAML.getString(id + ".material"));
      } catch (Exception e) {
        getLogger().severe("Invalid material name! Failed to load!");
        continue;
      }
      String name = TextUtils.color(itemsYAML.getString(id + ".display-name", "Item"));
      List<String> lore = TextUtils.color(itemsYAML.getStringList(id + ".lore"));
      ItemStack itemStack = new ItemStack(material);
      ItemMeta meta = itemStack.getItemMeta();
      meta.setDisplayName(name);
      meta.setLore(lore);
      itemStack.setItemMeta(meta);

      int levelRequirement = itemsYAML.getInt(id + ".level-requirement", 0);
      float experience = (float) itemsYAML.getDouble(id + ".experience", 0);

      MineReward mineReward = new MineReward(itemStack, experience, levelRequirement);

      mineRewardManager.addReward(id, mineReward);
    }
  }

  private void loadBlocks() {
    Map<String, Map<Material, RegenBlock>> blockMap = new HashMap<>();
    for (String regionId : blocksYAML.getKeys(false)) {
      if (!blocksYAML.isConfigurationSection(regionId)) {
        continue;
      }
      Map<Material, RegenBlock> materialBlockMap = new HashMap<>();
      ConfigurationSection oreSection = blocksYAML.getConfigurationSection(regionId);
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

        ConfigurationSection rewardSection = oreSection
            .getConfigurationSection(oreType + ".rewards");
        Map<String, Double> rewardAndWeightMap = new HashMap<>();
        for (String rewardName : rewardSection.getKeys(false)) {
          rewardAndWeightMap.put(rewardName, rewardSection.getDouble(rewardName));
        }
        RegenBlock regenBlock = new RegenBlock(exhaust, lootChance, replacementMaterial, oreRespawn,
            rewardAndWeightMap);

        materialBlockMap.put(oreMaterial, regenBlock);
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
