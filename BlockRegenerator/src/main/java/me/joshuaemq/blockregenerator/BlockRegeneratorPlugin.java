package me.joshuaemq.blockregenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.joshuaemq.blockregenerator.listeners.BlockBreakListener;
import me.joshuaemq.blockregenerator.managers.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class BlockRegeneratorPlugin extends JavaPlugin {

  private WorldGuardPlugin worldGuardPlugin;
  private SQLManager sqlManager;

  List<Material> oresList = new ArrayList<>();

  public List<Material> getOresList() {
    return oresList;
  }

  private Material depletedOre = Material.BEDROCK;

  public Material getDepletedOre() {
    return depletedOre;
  }

  public void onEnable() {
    worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");

    Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);

    sqlManager = new SQLManager(this);
    sqlManager.initDatabase();
    startCheck();

    getConfig().options().copyDefaults(true);
    saveConfig();
    reloadConfig();

    lootTable();
    lootItems();
    saveConfig();
    oresList.add(Material.IRON_ORE);
    oresList.add(Material.DIAMOND_ORE);
    oresList.add(Material.GOLD_ORE);
    oresList.add(Material.LAPIS_ORE);
    oresList.add(Material.EMERALD_ORE);
    oresList.add(Material.REDSTONE_ORE);
    oresList.add(Material.COAL_ORE);
    oresList.add(Material.QUARTZ_ORE);

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

  private FileConfiguration lootTableData = YamlConfiguration
      .loadConfiguration(new File(getDataFolder() + "/data", "lootTable.yml"));
  private FileConfiguration lootItemsData = YamlConfiguration
      .loadConfiguration(new File(getDataFolder() + "/data", "lootItems.yml"));

  public FileConfiguration getLootTable() {
    return lootTableData;
  }

  public FileConfiguration getLootItems() {
    return lootItemsData;
  }

  public void lootTable() { //creates data config for loot to be made
    File lootTableData = new File(getDataFolder() + "/data", "lootTable.yml");

    if (!lootTableData.exists()) {
      try {
        lootTableData.createNewFile();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      FileConfiguration lootConfig = YamlConfiguration.loadConfiguration(lootTableData);

      try {
        lootConfig.save(lootTableData);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }

  public void lootItems() { //creates data config for loot to be made
    File lootItemsData = new File(getDataFolder() + "/data", "lootItems.yml");

    if (!lootItemsData.exists()) {
      try {
        lootItemsData.createNewFile();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      FileConfiguration lootConfig = YamlConfiguration.loadConfiguration(lootItemsData);

      try {
        lootConfig.save(lootItemsData);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }

  public SQLManager getSQLManager() {
    return sqlManager;
  }
}
