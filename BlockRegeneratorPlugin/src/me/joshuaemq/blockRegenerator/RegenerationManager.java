package me.joshuaemq.blockRegenerator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class RegenerationManager implements Listener {

    private BlockRegenerator plugin;

    public RegenerationManager(BlockRegenerator plugin) {
        this.plugin = plugin;
    }

    //if block broken's material type is equal to x and location of the broken block is in a world guarded area
    //set event cancelled and spawn that block type as an item at the players location


    HashMap<String, Integer> lootSelectorMap = new HashMap<String, Integer>();
    String minedOreName = "";
    
	@EventHandler(priority= EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Block brokenBlock = event.getBlock();
        String regionName = "";
        Material brokenBlockMaterial = event.getBlock().getType();

        Boolean playerCanBuild = plugin.getWorldGuard().canBuild(p, p.getLocation());
        if (playerCanBuild == false) {
        	event.setCancelled(true);
            if (plugin.getOresList().contains(event.getBlock().getType())) {
                //get region
            	
                LocalPlayer localPlayer = plugin.getWorldGuard().wrapPlayer(p);
                Vector playerVector = localPlayer.getPosition();
                ApplicableRegionSet applicableRedionSet = plugin.getWorldGuard().getRegionManager(p.getWorld()).getApplicableRegions(playerVector);
                
                for (ProtectedRegion regions : applicableRedionSet) {
                	if (regions.contains(playerVector)) {
                		regionName = regions.getId().toString();
                	}
                }
                
                
                //select drop from lootTable.yml
                //create that drop from lootItems.yml
                
                for (String rewardz : plugin.getLootTable().getConfigurationSection(regionName + "." + brokenBlockMaterial.toString() + ".rewards").getKeys(false)) {
                	
                	String possibleRewardName = rewardz;
                    int possibleRewardWeight = plugin.getLootTable().getInt(regionName + "." + brokenBlockMaterial.toString() + ".rewards." + rewardz);
                    //p.sendMessage("String: " + rewardz);
                    //p.sendMessage("Weight: " + possibleRewardWeight);
                	lootSelectorMap.put(possibleRewardName, possibleRewardWeight);
                }
                minedOreName = brokenBlockMaterial.toString();
                //p.sendMessage("map: " + lootSelectorMap);
                int weightTotal = 0;
                for (int rewardWeight : lootSelectorMap.values()) {
                    weightTotal += rewardWeight;
                }
                //p.sendMessage("TOTAL WEIGHT: " + weightTotal);

                String reward = "";
                Random randomNumber = new Random();

                double randomizer = randomNumber.nextDouble() * weightTotal + Math.random();
                p.sendMessage("" + randomizer);
                while (randomizer > 0) {
                	for (String rewardSelector : lootSelectorMap.keySet()) {
                    	p.sendMessage("reward sel: " + rewardSelector);
                        randomizer -= lootSelectorMap.get(rewardSelector);
                        if (randomizer < 0) {
                        	p.sendMessage("IF STATEMENT: " + randomizer);
                            reward = rewardSelector;
                            break;
                        }
                    }
                }
                
                
                p.sendMessage("REWARD CHOSEN: " + reward);
                //p.sendMessage("MAT " + plugin.getLootItems().getString(reward + ".material"));
                Material rewardMaterial = Material.matchMaterial(plugin.getLootItems().getString(reward + ".material"));
                ItemStack minedReward = new ItemStack(rewardMaterial, 1); //makes new item stack for mined reward
                ItemMeta meta = minedReward.getItemMeta();
                
                String rewardsName = plugin.getLootItems().getString(reward + ".display-name"); //set display name for reward
                meta.setDisplayName(rewardsName);

                List<String> loreList = new ArrayList<String>();

                int levelRequirement = plugin.getLootItems().getInt(reward + ".level"); //sets item level requirement
                loreList.add(ChatColor.WHITE + "Item Level: " + levelRequirement);

                for (String lore : plugin.getLootItems().getConfigurationSection(reward).getStringList(".lore")) { //sets additional lore lines
                	loreList.add(lore);
                }
                meta.setLore(loreList);
                minedReward.setItemMeta(meta);
                //p.sendMessage(loreList.toString());
                //give player xp
                
                //give item to player
                Bukkit.getWorld(p.getLocation().getWorld().getName()).dropItemNaturally(p.getLocation(), minedReward);
                p.sendMessage(ChatColor.RED + "reward given");
                //clear map
                lootSelectorMap.clear();
                                
                //set broken block to bedrock
                
                //information as a new sql entry:
                //blocktype
                Material brokenBlockType = brokenBlock.getType();
                //block location/world
                Location brokenBlockLocation = brokenBlock.getLocation();
                //block respawn timer
                int respawnDelay = plugin.getLootTable().getInt(regionName + "." + minedOreName + ".respawn"); //sets respawn delay for that ore
                
                Timestamp time = null; //GET TIME BLOCK IS BROKEN
				int timeInt = time.getMinutes();
                int respawnTime = respawnDelay + timeInt;
                
                //get TimeStamp for when the block was broken
                //add respawnDelay to timestamp for time the block should respawn 
                
                
                //set broken block to bedrock
                brokenBlock.setType(plugin.getDepletedOre());
                
            }
        }
    }	
}

