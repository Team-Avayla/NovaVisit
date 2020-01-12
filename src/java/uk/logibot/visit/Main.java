package uk.logibot.visit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import uk.logibot.visit.commands.VisitCMD;
import uk.logibot.visit.events.Database;
import uk.logibot.visit.events.SQLite;
import uk.logibot.visit.gui.VisitGUI;

import java.util.Objects;

public class Main extends JavaPlugin {
    public static Plugin plugin = null;
    public static Database db;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        plugin = this;

        // Database
        db = new SQLite(this);
        db.load();

        // Commands
        Objects.requireNonNull(getCommand("visit")).setExecutor(new VisitCMD());
        PluginManager pm = Bukkit.getPluginManager();
        pm.addPermission(new Permission("rmc.visit"));
        pm.addPermission(new Permission("rmc.visit.create"));
        pm.addPermission(new Permission("rmc.visit.delete"));
        pm.addPermission(new Permission("rmc.visit.open"));
        pm.addPermission(new Permission("rmc.visit.close"));
        pm.addPermission(new Permission("rmc.visit.admin.delete"));
        pm.addPermission(new Permission("rmc.visit.admin.move"));
        pm.addPermission(new Permission("rmc.visit.admin.open"));
        pm.addPermission(new Permission("rmc.visit.admin.close"));

        pm.registerEvents(new VisitGUI(), this);
    }

    @Override
    public void onDisable() {
        plugin = null;

        PluginManager pm = Bukkit.getPluginManager();
        pm.removePermission(new Permission("rmc.visit"));
        pm.removePermission(new Permission("rmc.visit.create"));
        pm.removePermission(new Permission("rmc.visit.delete"));
        pm.removePermission(new Permission("rmc.visit.open"));
        pm.removePermission(new Permission("rmc.visit.close"));
        pm.removePermission(new Permission("rmc.visit.admin.delete"));
        pm.removePermission(new Permission("rmc.visit.admin.move"));
        pm.removePermission(new Permission("rmc.visit.admin.open"));
        pm.removePermission(new Permission("rmc.visit.admin.close"));
    }
}
