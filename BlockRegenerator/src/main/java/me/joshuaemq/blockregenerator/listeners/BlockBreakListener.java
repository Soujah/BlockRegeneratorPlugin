package me.joshuaemq.blockregenerator.listeners;

import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.objects.BlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.swing.plaf.synth.Region;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private BlockRegeneratorPlugin plugin;

    public BlockBreakListener(BlockRegeneratorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block brokenBlock = event.getBlock();
        ProtectedRegion region = null;
        Material blockMaterial = event.getBlock().getType();


        if (blockMaterial.toString().contains("_ORE")) {
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

            BlockData blockData = plugin.getBlockManager().getBlock(region.toString(), blockMaterial);
            String reward = blockData.getRandomReward();

            ItemStack minedReward = plugin.getMineRewardManager().getReward(reward).getItemStack();

            // TODO: Give player exp

            Bukkit.getWorld(player.getLocation().getWorld().getName()).dropItemNaturally(player.getLocation(), minedReward);

            Location brokenBlockLocation = brokenBlock.getLocation().clone();

            int respawnDelay = blockData.getRespawnTime();
            int x = (int) brokenBlockLocation.getX();
            int y = (int) brokenBlockLocation.getY();
            int z = (int) brokenBlockLocation.getZ();

            long currentTime = System.currentTimeMillis();

            //if random number between 1 and 100 > block exhaust chance, perform normal logic
            //if random number between 1 and 100 < block exhaust chance, set broken block to the original material

            Random random = new Random();
            int randomNumber = random.nextInt(100);


            double exhaust = plugin.getLootTable().getInt(region.getId() + blockMaterial + ".exhaust-chance");

            if (randomNumber > exhaust) {
                plugin.getSQLManager().insertBlock(blockMaterial.toString(), x, y, z,
                        brokenBlock.getWorld().getName(), currentTime + respawnDelay);
                brokenBlock.setType(blockData.getDepleteMaterial());
            } else {
                brokenBlock.setType(blockMaterial);
            }


        }
    }
}

