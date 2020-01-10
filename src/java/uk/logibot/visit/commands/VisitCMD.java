package uk.logibot.visit.commands;

import com.google.common.collect.Lists;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import uk.logibot.visit.Main;
import uk.logibot.visit.gui.VisitGUI;

import java.util.*;

public class VisitCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("visit")) {
            if(sender instanceof Player) {
                String prefix = "§8[§bTRVisit§8] ";

                if(!sender.hasPermission("rmc.visit")) {
                    sender.sendMessage(prefix + "§4You cannot use this command!");
                    return true;
                }

                Player player = (Player) sender;
                if(args.length == 0) {
                    VisitGUI.openGUI(player, 1);
                } else if(args[0].equalsIgnoreCase("create")) {
                    if(!sender.hasPermission("rmc.visit.create")) {
                        sender.sendMessage(prefix + "§4You cannot use this command!");
                        return true;
                    }

                    if(args.length == 1) {
                        sender.sendMessage(prefix + "§cYou must specific a warp name!");
                        return true;
                    }
                    String visitName = args[1];
                    visitName = visitName.replaceAll("[^a-zA-Z0-9_]", "");
                    visitName = StringUtils.left(visitName, 16);
                    if(visitName.isEmpty()) {
                        sender.sendMessage(prefix + "§cWarp names must be alphanumeric!");
                        return true;
                    }

                    int warpCount = Main.db.getWarps(player.getUniqueId(), false).size();
                    if(warpCount > 0) {
                        int maxWarps = 1;
                        for (int i = 10; i-- > 0; ) {
                            if(sender.hasPermission("rmc.visit.create.multiple." + i) && i > maxWarps) {
                                maxWarps = i;
                            }
                        }
                        if(warpCount >= maxWarps) {
                            sender.sendMessage(prefix + "§cYou have reached your warp limit!");
                            return true;
                        }
                    }

                    HashMap<String, String> warp = Main.db.getWarp(visitName);
                    if(warp == null) {
                        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
                        if (claim != null) {
                            if (claim.ownerID.equals(player.getUniqueId())) {
                                if (isSafeLocation(player.getLocation())) {
                                    Main.db.createWarp(player.getUniqueId(), visitName, player.getLocation());
                                    sender.sendMessage(prefix + "§aYou created a warp named §f" + visitName);
                                } else {
                                    sender.sendMessage(prefix + "§cFailed to set warp due to unsafe location!");
                                }
                            } else {
                                sender.sendMessage(prefix + "§cYou can only set a warp in your claim!");
                            }
                        } else {
                            sender.sendMessage(prefix + "§cYou can only set a warp in your claim!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cA warp named §f" + warp.get("name") + " §calready exists!");
                    }
                } else if(args[0].equalsIgnoreCase("delete")) {
                    if(!sender.hasPermission("rmc.visit.delete")) {
                        sender.sendMessage(prefix + "§4You cannot use this command!");
                        return true;
                    }

                    if(args.length == 1) {
                        sender.sendMessage(prefix + "§cYou must specific a warp name!");
                        return true;
                    }
                    String visitName = args[1];

                    HashMap<String, String> warp = Main.db.getWarp(visitName);
                    if(warp != null) {
                        if(warp.get("uuid").equals(player.getUniqueId().toString()) || player.hasPermission("rmc.visit.admin.delete")) {
                            sender.sendMessage(prefix + "§aYou deleted warp named §f" + warp.get("name"));
                            Main.db.deleteWarp(warp.get("name"));
                        } else {
                            sender.sendMessage(prefix + "§cYou cannot delete someone else's warp!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cCould not find a warp named §f" + visitName + "§c!");
                    }
                } else if(args[0].equalsIgnoreCase("move")) {
                    if(!sender.hasPermission("rmc.visit.move")) {
                        sender.sendMessage(prefix + "§4You cannot use this command!");
                        return true;
                    }

                    if(args.length == 1) {
                        sender.sendMessage(prefix + "§cYou must specific a warp name!");
                        return true;
                    }
                    String visitName = args[1];

                    HashMap<String, String> warp = Main.db.getWarp(visitName);
                    if(warp != null) {
                        if(warp.get("uuid").equals(player.getUniqueId().toString()) || player.hasPermission("rmc.visit.admin.move")) {
                            Main.db.setLocation(warp.get("name"), player.getLocation());
                            sender.sendMessage(prefix + "§aYou moved warp named §f" + warp.get("name"));
                        } else {
                            sender.sendMessage(prefix + "§cYou cannot move someone else's warp!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cCould not find a warp named §f" + visitName + "§c!");
                    }
                } else if(args[0].equalsIgnoreCase("rename")) {
                    if(!sender.hasPermission("rmc.visit.rename")) {
                        sender.sendMessage(prefix + "§4You cannot use this command!");
                        return true;
                    }

                    if(args.length < 3) {
                        sender.sendMessage(prefix + "§cYou must specific the old and new names!");
                        return true;
                    }
                    String visitName = args[1];

                    String newName = args[2];
                    newName = newName.replaceAll("[^a-zA-Z0-9_]", "");
                    newName = StringUtils.left(newName, 16);
                    if(newName.isEmpty()) {
                        sender.sendMessage(prefix + "§cWarp names must be alphanumeric!");
                        return true;
                    }

                    HashMap<String, String> warp = Main.db.getWarp(visitName);
                    if(warp != null) {
                        if(warp.get("uuid").equals(player.getUniqueId().toString())) {
                            Main.db.setName(warp.get("name"), newName);
                            sender.sendMessage(prefix + "§aYou renamed warp §f" + warp.get("name") + " §ato §f" + newName);
                        } else {
                            sender.sendMessage(prefix + "§cYou cannot rename someone else's warp!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cCould not find a warp named §f" + visitName + "§c!");
                    }
                } else if(args[0].equalsIgnoreCase("open")) {
                    if(!sender.hasPermission("rmc.visit.open")) {
                        sender.sendMessage(prefix + "§4You cannot use this command!");
                        return true;
                    }

                    if(args.length == 1) {
                        sender.sendMessage(prefix + "§cYou must specific a warp name!");
                        return true;
                    }
                    String visitName = args[1];

                    HashMap<String, String> warp = Main.db.getWarp(visitName);
                    if(warp != null) {
                        if(warp.get("uuid").equals(player.getUniqueId().toString()) || player.hasPermission("rmc.visit.admin.open")) {
                            if(warp.get("open").equals("false")) {
                                Main.db.setOpen(warp.get("name"), true);
                                sender.sendMessage(prefix + "§aYou opened warp named §f" + warp.get("name"));
                            } else {
                                sender.sendMessage(prefix + "§cThis warp is already open!");
                            }
                        } else {
                            sender.sendMessage(prefix + "§cYou cannot open someone else's warp!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cCould not find a warp named §f" + visitName + "§c!");
                    }
                } else if(args[0].equalsIgnoreCase("close")) {
                    if(!sender.hasPermission("rmc.visit.close")) {
                        sender.sendMessage(prefix + "§4You cannot use this command!");
                        return true;
                    }

                    if(args.length == 1) {
                        sender.sendMessage(prefix + "§cYou must specific a warp name!");
                        return true;
                    }
                    String visitName = args[1];

                    HashMap<String, String> warp = Main.db.getWarp(visitName);
                    if(warp != null) {
                        if(warp.get("uuid").equals(player.getUniqueId().toString()) || player.hasPermission("rmc.visit.admin.close")) {
                            if (warp.get("open").equals("true")) {
                                Main.db.setOpen(warp.get("name"), false);
                                sender.sendMessage(prefix + "§aYou closed warp named §f" + warp.get("name"));
                            } else {
                                sender.sendMessage(prefix + "§cThis warp is already closed!");
                            }
                        } else {
                            sender.sendMessage(prefix + "§cYou cannot close someone else's warp!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cCould not find a warp named §f" + visitName + "§c!");
                    }
                } else if(args[0].equalsIgnoreCase("vote")) {
                    if(!sender.hasPermission("rmc.visit.vote")) {
                        sender.sendMessage(prefix + "§4You cannot use this command!");
                        return true;
                    }

                    if(args.length == 1) {
                        sender.sendMessage(prefix + "§cYou must specific a warp name!");
                        return true;
                    }
                    String visitName = args[1];

                    HashMap<String, String> warp = Main.db.getWarp(visitName);
                    if(warp != null) {
                        if(!warp.get("uuid").equals(player.getUniqueId().toString())) {
                            if (warp.get("open").equals("true")) {
                                Long lastVote = Main.db.getLastVote(player.getUniqueId(), warp.get("name"));
                                if(lastVote == null || lastVote > System.currentTimeMillis() + (24 * 60 * 60 * 1000)) {
                                    Main.db.addNewVote(player.getUniqueId(), warp.get("name"));
                                    sender.sendMessage(prefix + "§aYou voted for warp named §f" + warp.get("name"));
                                } else {
                                    sender.sendMessage(prefix + "§eYou can only vote once every 24 hours!");
                                }
                            } else {
                                sender.sendMessage(prefix + "§cThis warp is currently closed!");
                            }
                        } else {
                            sender.sendMessage(prefix + "§cYou cannot vote on your own warp!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cCould not find a warp named §f" + visitName + "§c!");
                    }
                } else if(args[0].equalsIgnoreCase("list")) {
                    int page = 1;
                    if(args.length > 2 && args[2].matches("^[0-9]*$")) {
                        page = Integer.parseInt(args[2]);
                    }
                    ArrayList<HashMap<String, String>> warps = null;

                    if(args.length > 1 && args[1].equalsIgnoreCase("mine")) {
                        warps = Main.db.getWarps(player.getUniqueId(), true);
                    } else if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
                        warps = Main.db.getWarps(null, true);
                    } else {
                        sender.sendMessage(prefix + "§c/visit list <all|mine> [page]");
                        return true;
                    }
                    if(warps.size() > 0) {
                        warps.sort(Collections.reverseOrder(Comparator.comparingInt(w -> Main.db.getVotes(w.get("name")))));

                        List<List<HashMap<String, String>>> pages = Lists.partition(warps, 10);
                        if (page < 1) page = 1;
                        else if (page > pages.size()) page = pages.size();

                        sender.sendMessage("§7Visit Warps (Page §b" + page + "/" + pages.size() + "§7):");
                        for (int i = 0; i < pages.get(page - 1).size(); i++) {
                            HashMap<String, String> value = pages.get(page - 1).get(i);

                            sender.sendMessage("§7" + (i+1) + ". §a" + value.get("name") + " §7- §a" + Main.db.getVotes(value.get("name")) + " votes");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cThere are no avaliable warps.");
                    }
                } else if(args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(
                    "§6/visit§f: Opens the Visit GUI.\n" +
                        "§6/visit §7help§f: Opens the Visit GUI.\n" +
                        "§6/visit §7create §e<name>§f: Creates a new warp.\n" +
                        "§6/visit §7delete §e<name>§f: Deletes an existing warp.\n" +
                        "§6/visit §7open §e<name>§f: Opens the warp for visitors.\n" +
                        "§6/visit §7close §e<name>§f: Closes the warp from visitors.\n" +
                        "§6/visit §7move §e<name>§f: Moves the warp to your location.\n" +
                        "§6/visit §7rename §e<name> <name>§f: Renames the warp to something else.\n" +
                        "§6/visit §7vote §e<name>§f: Votes for a warp." +
                        "§6/visit §7list §e<all|mine> [page]§f: View a list of warps."
                    );
                } else if(Main.db.getWarp(args[0]) != null) {
                    HashMap<String, String> warp = Main.db.getWarp(args[0]);
                    Location warploc = Main.db.getLocation(warp.get("name"));

                    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(warploc, true, null);
                    if(claim != null && warp.get("uuid").equals(claim.ownerID.toString())) {
                        if(warp.get("open").equals("true")) {
                            if (isSafeLocation(warploc)) {
                                warploc.setYaw(Float.parseFloat(warp.get("locYaw")));
                                warploc.setPitch(Float.parseFloat(warp.get("locPitch")));
                                player.teleport(warploc, PlayerTeleportEvent.TeleportCause.COMMAND);
                                sender.sendMessage(prefix + "§aYou are now visiting warp §f" + warp.get("name") + "§a!");
                            } else {
                                sender.sendMessage(prefix + "§eCould not teleport due to the warp being unsafe!");
                            }
                        } else {
                            sender.sendMessage(prefix + "§cThe warp §f" + warp.get("name") + " §cis currently closed!");
                        }
                    } else {
                        sender.sendMessage(prefix + "§cCould not find a warp named §f" + warp.get("name") + "§c!");
                        Main.db.deleteWarp(warp.get("name"));
                    }
                } else {
                    sender.sendMessage(prefix + "§cUse /visit help for the command list!");
                }
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Only players can use /visit");
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        if (!feet.getType().isTransparent() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block head = feet.getRelative(BlockFace.UP);
        if (!head.getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block ground = feet.getRelative(BlockFace.DOWN);
        if (!ground.getType().isSolid()) {
            return false; // not solid
        }
        return true;
    }
}
