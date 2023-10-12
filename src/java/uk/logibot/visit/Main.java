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
        pm.addPermission(new Permission("novavisit"));
        pm.addPermission(new Permission("novavisit.create"));
        pm.addPermission(new Permission("novavisit.create.multiple.*"));
        pm.addPermission(new Permission("novavisit.delete"));
        pm.addPermission(new Permission("novavisit.delete.others"));
        pm.addPermission(new Permission("novavisit.open"));
        pm.addPermission(new Permission("novavisit.open.others"));
        pm.addPermission(new Permission("novavisit.close"));
        pm.addPermission(new Permission("novavisit.close.others"));
        pm.addPermission(new Permission("novavisit.move"));
        pm.addPermission(new Permission("novavisit.move.others"));
        pm.addPermission(new Permission("novavisit.rename"));
        pm.addPermission(new Permission("novavisit.rename.others"));
        pm.addPermission(new Permission("novavisit.vote"));
        pm.addPermission(new Permission("novavisit.votebonus"));
        pm.addPermission(new Permission("novavisit.list"));
        pm.addPermission(new Permission("novavisit.location.others"));
        pm.addPermission(new Permission("novavisit.location.wild"));
        pm.addPermission(new Permission("novavisit.location.admin"));

        pm.registerEvents(new VisitGUI(), this);
    }

    @Override
    public void onDisable() {
        plugin = null;

        PluginManager pm = Bukkit.getPluginManager();
        pm.removePermission(new Permission("novavisit"));
        pm.removePermission(new Permission("novavisit.create"));
        pm.removePermission(new Permission("novavisit.create.multiple.*"));
        pm.removePermission(new Permission("novavisit.delete"));
        pm.removePermission(new Permission("novavisit.delete.others"));
        pm.removePermission(new Permission("novavisit.open"));
        pm.removePermission(new Permission("novavisit.open.others"));
        pm.removePermission(new Permission("novavisit.close"));
        pm.removePermission(new Permission("novavisit.close.others"));
        pm.removePermission(new Permission("novavisit.move"));
        pm.removePermission(new Permission("novavisit.move.others"));
        pm.removePermission(new Permission("novavisit.rename"));
        pm.removePermission(new Permission("novavisit.rename.others"));
        pm.removePermission(new Permission("novavisit.vote"));
        pm.removePermission(new Permission("novavisit.votebonus"));
        pm.removePermission(new Permission("novavisit.list"));
        pm.removePermission(new Permission("novavisit.location.others"));
        pm.removePermission(new Permission("novavisit.location.wild"));
        pm.removePermission(new Permission("novavisit.location.admin"));
    }
}
