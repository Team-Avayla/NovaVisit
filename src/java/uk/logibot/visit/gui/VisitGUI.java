package uk.logibot.visit.gui;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import uk.logibot.visit.Main;

import java.util.*;

public class VisitGUI implements Listener {
    private static final HashMap<UUID, Integer> cPage = new HashMap<>();

    public static void openGUI(Player player, Integer page) {
        cPage.put(player.getUniqueId(), page);

        Inventory GUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&3&lVisit GUI"));

        // Back Page
        ItemStack tItem = new ItemStack(Material.ARROW, 1);
        ItemMeta tMeta = tItem.getItemMeta();
        tMeta.setDisplayName(ChatColor.GREEN + "Back");
        tItem.setItemMeta(tMeta);
        GUI.setItem(45, tItem);
        // Next Page
        tItem = new ItemStack(Material.ARROW, 1);
        tMeta = tItem.getItemMeta();
        tMeta.setDisplayName(ChatColor.GREEN + "Next");
        tItem.setItemMeta(tMeta);
        GUI.setItem(53, tItem);

        ArrayList<HashMap<String, String>> warps = Main.db.getWarps(null, true);

        if(warps.size() > 0) {
            warps.sort(Collections.reverseOrder(Comparator.comparingInt(w -> Main.db.getVotes(w.get("name")))));

            List<List<HashMap<String, String>>> pages = Lists.partition(warps, 45);
            if (page < 1) page = 1;
            if (page > pages.size()) page = pages.size();

            for (int i = 0; i < pages.get(page - 1).size(); i++) {
                HashMap<String, String> value = pages.get(page - 1).get(i);

                ItemStack skull = null;
                if (Bukkit.getVersion().contains("1.13.") || Bukkit.getVersion().contains("1.14.") || Bukkit.getVersion().contains("1.15.") || Bukkit.getVersion().contains("1.16.")) {
                    skull = new ItemStack(Material.PLAYER_HEAD);
                } else {
                    skull = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal());
                }
                SkullMeta skMeta = (SkullMeta) skull.getItemMeta();
                skMeta.setDisplayName(ChatColor.GREEN + value.get("name"));
                OfflinePlayer skplayer = Bukkit.getOfflinePlayer(UUID.fromString(value.get("uuid")));
                skMeta.setLore(Arrays.asList(
                        "§fOwner: §e" + skplayer.getName(),
                        "§fVotes: §e" + Main.db.getVotes(value.get("name"))
                ));
                skMeta.setOwningPlayer(skplayer);
                skull.setItemMeta(skMeta);
                GUI.setItem(GUI.firstEmpty(), skull);
            }
        }

        player.openInventory(GUI);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getSlotType() != InventoryType.SlotType.OUTSIDE) {
            if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&3&lVisit GUI"))) {
                event.setCancelled(true);

                ItemStack clicked = event.getCurrentItem();
                Material clickedType = clicked.getType();
                String clickedName = clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : "";

                if (clickedType == Material.ARROW) {
                    if (clickedName.equals(ChatColor.GREEN + "Back")) {
                        openGUI(player, cPage.get(player.getUniqueId()) - 1);
                    } else if (clickedName.equals(ChatColor.GREEN + "Next")) {
                        openGUI(player, cPage.get(player.getUniqueId()) + 1);
                    }
                } else if(clickedType == Material.PLAYER_HEAD || clickedType == Material.getMaterial("SKULL_ITEM")) {
                    player.closeInventory();
                    player.chat("/visit " + ChatColor.stripColor(clickedName));
                }
            }
        }
    }
}
