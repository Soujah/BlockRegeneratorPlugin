package me.joshuaemq.blockregenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.joshuaemq.blockregenerator.listeners.BlockBreakListener;
import me.joshuaemq.blockregenerator.managers.MineRewardManager;
import me.joshuaemq.blockregenerator.managers.SQLManager;
import me.joshuaemq.blockregenerator.objects.MineReward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class BlockRegeneratorPlugin extends JavaPlugin {

  private WorldGuardPlugin worldGuardPlugin;
  private MineRewardManager mineRewardManager;
  private SQLManager sqlManager;

  private FileConfiguration lootTableData;
  private FileConfiguration lootItemsData;

  public void onEnable() {
    worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");

    lootTableData = YamlConfiguration
            .loadConfiguration(new File(getDataFolder(), "blocks.yml"));
    lootItemsData = YamlConfiguration
            .loadConfiguration(new File(getDataFolder(), "items.yml"));

    Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);

    sqlManager = new SQLManager(this);
    sqlManager.initDatabase();
    startCheck();

    getConfig().options().copyDefaults(true);
    saveConfig();
    reloadConfig();

    buildConfig("items.yml");
    buildConfig("blocks.yml");
    saveConfig();

    loadItems();

    Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Enabled!");
  }

  private void startCheck() {
    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        sqlManager.check();
      }
    }, 0L, 4 * 20L);
  }

  public void onDisable() {
    HandlerList.unregisterAll();

    worldGuardPlugin = null;
    sqlManager = null;

    Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Disabled!");
  }

  public WorldGuardPlugin getWorldGuard() {
    return worldGuardPlugin;
  }

  public FileConfiguration getLootTable() {
    return lootTableData;
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
    List<String> lootItems = lootItemsData.getStringList("");
    for (String id : lootItems) {
      Material material;
      try {
        material = Material.valueOf(lootItemsData.getString(id + ".material"));
      } catch (Exception e) {
        getLogger().severe("Invalid material name! Failed to load!");
        continue;
      }
      String name = lootItemsData.getString(id + ".display-name", "Item");
      List<String> lore = lootItemsData.getStringList(id + ".lore");
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

  public SQLManager getSQLManager() {
    return sqlManager;
  }

  public MineRewardManager getMineRewardManager() {
    return mineRewardManager;
  }

}
