package me.joshuaemq.blockRegenerator;

import java.sql.Timestamp;

import org.bukkit.scheduler.BukkitRunnable;

public class RespawnTask extends BukkitRunnable{
	
	private BlockRegenerator plugin;

    public RespawnTask(BlockRegenerator plugin) {
        this.plugin = plugin;
    }
    
    private Timestamp currentTime = null;
    
    public Timestamp getCurrentTime() {
    	return currentTime;
    }
    
    //loop through table respawning any blocks that are ready to be respawned
    public void run() {
		
    	
    	for (ENTRY IN SQL (blockRespawnTime) : RESPAWN TIMESTAMP) {
    		
    		if (blockRespawnTime == this.getCurrentTime()) {
        		//RESPAWN BLOCK
        		//REMOVE ENTRY FROM SQL
        	}
    	}
    	
	}
	
	

}
