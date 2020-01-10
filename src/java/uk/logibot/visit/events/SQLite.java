package uk.logibot.visit.events;

import uk.logibot.visit.Main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {
    public SQLite(Main instance){
        super(instance);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Connection getSQLConnection() {
        String dbname = "warps";

        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
        File dataFolder = new File(plugin.getDataFolder(), dbname +".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+ dbname +".db");
            }
        }
        try {
            if(connection!=null && !connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate("CREATE TABLE IF NOT EXISTS warpdata (" +
                    "`name` varchar(16) PRIMARY KEY," +
                    "`uuid` varchar(36)," +
                    "`open` integer DEFAULT 1," +
                    "`locWorld` string NULL," +
                    "`locX` double NULL," +
                    "`locY` double NULL," +
                    "`locZ` double NULL," +
                    "`locPitch` float NULL," +
                    "`locYaw` float NULL" +
                    ");");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS votedata (" +
                    "`name` varchar(16)," +
                    "`uuid` varchar(36)," +
                    "`last_vote` long," +
                    "`votes` integer DEFAULT 0" +
                    ");");
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
