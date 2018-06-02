package me.joshuaemq.blockregenerator.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import java.util.Set;
import me.joshuaemq.blockregenerator.BlockRegenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class BlockBreakListener implements Listener {

  private BlockRegenerator plugin;

  public BlockBreakListener(BlockRegenerator plugin) {
    this.plugin = plugin;
  }

  //if block broken's material type is equal to x and location of the broken block is in a world guarded area
  //set event cancelled and spawn that block type as an item at the players location


  HashMap<String, Integer> lootSelectorMap = new HashMap<String, Integer>();
  String minedOreName = "";

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    Block brokenBlock = event.getBlock();
    ProtectedRegion region = null;
    Material blockMaterial = event.getBlock().getType();


    if (plugin.getOresList().contains(event.getBlock().getType())) {

      event.setCancelled(true);

      ApplicableRegionSet regionSet =
          plugin.getWorldGuard().getRegionManager(player.getWorld()).getApplicableRegions(brokenBlock.getLocation());

      int priority = -9999;
      for (ProtectedRegion p : regionSet.getRegions()) {
        if (p.getPriority() > priority) {
          region = p;
        }
      }

      if (region == null) {
        plugin.getLogger().severe("No applicable regions? Are all regions below -9999 priority?");
        return;
      }

      //player.sendMessage(region.getId());

      //select drop from lootTable.yml
      //create that drop from lootItems.yml

      Set<String> rewardStrings =
          plugin.getLootTable().getConfigurationSection(region.getId() + "." + blockMaterial.toString() + ".rewards").getKeys(false);

      for (String r : rewardStrings) {
        int rewardWeight = plugin.getLootTable().getInt(region.getId() + "." + blockMaterial.toString() + ".rewards." + r);
        //p.sendMessage("String: " + rewardz);
        //p.sendMessage("Weight: " + possibleRewardWeight);
        lootSelectorMap.put(r, rewardWeight);
      }
      minedOreName = blockMaterial.toString();
      //p.sendMessage("map: " + lootSelectorMap);
      int weightTotal = 0;
      for (int rewardWeight : lootSelectorMap.values()) {
        weightTotal += rewardWeight;
      }
      //p.sendMessage("TOTAL WEIGHT: " + weightTotal);

      String reward = "";
      Random randomNumber = new Random();

      double randomizer = randomNumber.nextDouble() * weightTotal + Math.random();
      //player.sendMessage("" + randomizer);
      while (randomizer > 0) {
        for (String rewardSelector : lootSelectorMap.keySet()) {
          //player.sendMessage("reward sel: " + rewardSelector);
          randomizer -= lootSelectorMap.get(rewardSelector);
          if (randomizer < 0) {
            //player.sendMessage("IF STATEMENT: " + randomizer);
            reward = rewardSelector;
            break;
          }
        }
      }

      //player.sendMessage("REWARD CHOSEN: " + reward);
      //p.sendMessage("MAT " + plugin.getLootItems().getString(reward + ".material"));
      Material rewardMaterial = Material.matchMaterial(plugin.getLootItems().getString(reward + ".material"));
      ItemStack minedReward = new ItemStack(rewardMaterial, 1); //makes new item stack for mined reward
      ItemMeta meta = minedReward.getItemMeta();

      String rewardsName = plugin.getLootItems().getString(reward + ".display-name"); //set display name for reward
      meta.setDisplayName(rewardsName);

      List<String> loreList = new ArrayList<String>();

      int levelRequirement = plugin.getLootItems().getInt(reward + ".level-requirement"); //sets item level requirement
      loreList.add(ChatColor.WHITE + "Item Level: " + levelRequirement);

      for (String lore : plugin.getLootItems().getConfigurationSection(reward)
          .getStringList(".lore")) { //sets additional lore lines
        loreList.add(lore);
      }
      meta.setLore(loreList);
      minedReward.setItemMeta(meta);
      //p.sendMessage(loreList.toString());
      //give player xp

      //give item to player
      Bukkit.getWorld(player.getLocation().getWorld().getName()).dropItemNaturally(player.getLocation(), minedReward);
      //player.sendMessage(ChatColor.RED + "reward given");
      //clear map
      lootSelectorMap.clear();

      //set broken block to bedrock

      //information as a new sql entry:
      //blocktype
      Material brokenBlockType = brokenBlock.getType();
      //block location/world
      Location brokenBlockLocation = brokenBlock.getLocation();
      //block respawn timer
      int respawnDelay = plugin.getLootTable()
          .getInt(region.getId() + "." + minedOreName + ".respawn"); //sets respawn delay for that ore
      int x = (int) brokenBlockLocation.getX();
      int y = (int) brokenBlockLocation.getY();
      int z = (int) brokenBlockLocation.getZ();

      long currentTime = System.currentTimeMillis();

      plugin.getSQLManager().insertBlock(brokenBlockType.toString(), x, y, z, brokenBlock.getWorld().getName(),
          currentTime + respawnDelay);

      //set broken block to bedrock
      brokenBlock.setType(plugin.getDepletedOre());
    }
  }
}

