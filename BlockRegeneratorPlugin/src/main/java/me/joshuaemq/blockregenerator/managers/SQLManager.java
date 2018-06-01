package me.joshuaemq.blockregenerator.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import me.joshuaemq.blockregenerator.BlockRegenerator;

public class SQLManager {
    public Connection connection;
    private BlockRegenerator plugin;
 
    public SQLManager(final BlockRegenerator plugin) {
        this.plugin = plugin;
    }
    private void openConnection(){
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQL.host") + ":" + this.plugin.getConfig().getString("MySQL.port") + "/" + this.plugin.getConfig().getString("MySQL.database"), new StringBuilder().append(this.plugin.getConfig().getString("MySQL.user")).toString(), new StringBuilder().append(this.plugin.getConfig().getString("MySQL.password")).toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try {
            this.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initDatabase(){
        this.openConnection();
        try {
            final PreparedStatement sql = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `ms_blocks` (`ID` INT NOT NULL UNIQUE, `material` varchar(100), `respawntime` LONG, `x` INT, `y` INT, `z` INT, `world` VARCHAR(255), PRIMARY KEY (`ID`)) ;");
            sql.execute();
            sql.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            closeConnection();
        }
    }

    public void removeItem(int id){
        try{
            PreparedStatement sql = connection.prepareStatement("DELETE FROM `ms_blocks` WHERE `ID`=?;");
            sql.setInt(1, id);
            sql.execute();
            sql.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void insertBlock(String material, int x, int y, int z, String world, long respawntime){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("INSERT INTO `ms_blocks` (`id`, `material`, `respawntime`, `x`, `y`, `z`, `world`) VALUES (?,?,?,?,?,?,?);");
            sql.setInt(1, nextID());
            sql.setString(2, material);
            sql.setLong(3, respawntime);
            sql.setInt(4, x);
            sql.setInt(5, y);
            sql.setInt(6, z);
            sql.setString(7, world);
            sql.execute();
            sql.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
   
    public void check(){
        openConnection();
        try{
            PreparedStatement sql1 = connection.prepareStatement("SELECT * FROM `ms_blocks`;");
            ResultSet rs1 = sql1.executeQuery();
            while(rs1.next()){
                PreparedStatement sql3 = connection.prepareStatement("UPDATE `ms_blocks` SET `respawntime`=? WHERE `id`=?;");
                sql3.setLong(1, rs1.getLong("respawntime") - 4);
                sql3.setInt(2, rs1.getInt("id"));
                sql3.executeUpdate();
                if(System.currentTimeMillis() >= rs1.getLong("respawntime")){
                    Location loc = new Location(Bukkit.getWorld(rs1.getString("world")), rs1.getInt("x"), rs1.getInt("y"), rs1.getInt("z"));
                    Block block = loc.getBlock();
                    String material =rs1.getString("material");
                    block.setType(Material.valueOf(material));
                    this.removeItem(rs1.getInt("id"));
                }
                sql3.close();
            }
            rs1.close();
            sql1.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    public int nextID(){
        int data = 0;
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT `ID` FROM `ms_blocks` ORDER BY `ID` DESC LIMIT 1;");
            ResultSet rs = sql.executeQuery();
            if(rs.next()){
                data = rs.getInt("id") + 1;
            }
            rs.close();
            sql.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }
}