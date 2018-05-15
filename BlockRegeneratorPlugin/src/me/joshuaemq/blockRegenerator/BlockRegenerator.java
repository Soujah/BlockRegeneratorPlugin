package me.joshuaemq.blockRegenerator;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.joshuaemq.blockRegenerator.RegenerationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockRegenerator extends JavaPlugin {


    WorldGuardPlugin worldGuardPlugin;
    RegenerationManager regenManager;

    List<Material> oresList = new ArrayList<Material>();

    public List<Material> getOresList() {
        return oresList;
    }

    private Material depletedOre = Material.BEDROCK;

    public Material getDepletedOre() {
        return depletedOre;
    }

    public void onEnable() {
        worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
        Bukkit.getPluginManager().registerEvents(new RegenerationManager(this), this);
        regenManager = new RegenerationManager(this);
        //calls method to load orelootmanager stuff
        
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

        Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Enabled!");
    }

    public void onDisable() {
    	//regenerate all blocks
        HandlerList.unregisterAll();
        worldGuardPlugin = null;
        regenManager = null;
        Bukkit.getServer().getLogger().info("Block Regenerator by Joshuaemq: Disabled!");

    }
    
    public WorldGuardPlugin getWorldGuard() {
        return worldGuardPlugin;
    }

    private FileConfiguration lootTableData = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/data", "lootTable.yml"));
    private FileConfiguration lootItemsData = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/data", "lootItems.yml"));

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

}
