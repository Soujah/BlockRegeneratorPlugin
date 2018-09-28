package me.joshuaemq.blockregenerator;

import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import info.faceland.strife.StrifePlugin;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration.VersionUpdateType;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import me.joshuaemq.blockregenerator.commands.BaseCommand;
import me.joshuaemq.blockregenerator.listeners.BlockBreakListener;
import me.joshuaemq.blockregenerator.managers.BlockManager;
import me.joshuaemq.blockregenerator.managers.MineRewardManager;
import me.joshuaemq.blockregenerator.managers.SQLManager;
import me.joshuaemq.blockregenerator.objects.RegenBlock;
import me.joshuaemq.blockregenerator.objects.MineReward;
import me.joshuaemq.blockregenerator.tasks.BlockRespawnTask;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.scheduler.BukkitTask;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

public class BlockRegeneratorPlugin extends FacePlugin {

  private CommandHandler commandHandler;

  private MineRewardManager mineRewardManager;
  private BlockManager blockManager;
  private SQLManager sqlManager;
  private BlockRespawnTask blockRespawnTask;
  private BukkitTask oreRespawnTask;

  private VersionedSmartYamlConfiguration configYAML;
  private VersionedSmartYamlConfiguration blocksYAML;
  private VersionedSmartYamlConfiguration itemsYAML;

  StrifePlugin strifePlugin;

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
    strifePlugin = (StrifePlugin) Bukkit.getPluginManager().getPlugin("Strife");

    String username = configYAML.getString("MySQL.user");
    String password = configYAML.getString("MySQL.password");
    String database = configYAML.getString("MySQL.database");
    String hostAndPort = configYAML.getString("MySQL.host") + ":" +
        configYAML.getString("MySQL.port");
    // configYAML.getString("MySQL.prefix")

    if (StringUtils.isBlank(username)) {
      getLogger().severe("Missing database username! Plugin will fail to work!");
    }
    if (StringUtils.isBlank(password)) {
      getLogger().severe("Missing database password! Plugin will fail to work!");
    }
    if (StringUtils.isBlank(database)) {
      getLogger().severe("Missing database field! Plugin will fail to work!");
    }

    DatabaseOptions options =
        DatabaseOptions.builder().mysql(username, password, database, hostAndPort).build();
    Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
    DB.setGlobalDatabase(db);

    mineRewardManager = new MineRewardManager(this);
    blockManager = new BlockManager();

    sqlManager = new SQLManager();

    blockRespawnTask = new BlockRespawnTask(this);
    oreRespawnTask = Bukkit.getScheduler().runTaskTimer(this, () ->
        blockRespawnTask.doOreRespawn(),600L, 300L); // Start after 30s Repeat every 15s

    loadBlocks();
    loadItems();

    Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Enabled!");
  }

  @Override
  public void disable() {
    HandlerList.unregisterAll(this);

    commandHandler = null;
    sqlManager = null;
    mineRewardManager = null;
    blockManager = null;

    oreRespawnTask.cancel();
    DB.close();
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
      String name = itemsYAML.getString(id + ".display-name");
      List<String> lore = itemsYAML.getStringList(id + ".lore");
      int amount = itemsYAML.getInt(id + ".amount", 1);
      ItemStack itemStack = new ItemStack(material);
      itemStack.setAmount(amount);
      ItemMeta meta = itemStack.getItemMeta();
      if (StringUtils.isNotBlank(name)) {
        meta.setDisplayName(TextUtils.color(name));
      }
      if (!lore.isEmpty()) {
        meta.setLore(TextUtils.color(lore));
      }
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

  public StrifePlugin getStrifePlugin() {
    return strifePlugin;
  }
}
