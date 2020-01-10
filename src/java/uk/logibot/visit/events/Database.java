package uk.logibot.visit.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import uk.logibot.visit.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("LoopStatementThatDoesntLoop")
public abstract class Database {
    Main plugin;
    Connection connection;
    private String table = "warpdata";
    private String vtable = "votedata";
    Database(Main instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    void initialize(){
        connection = getSQLConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM " + table + ";");
            rs = ps.executeQuery();
            rs.next();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
    }


    public void createWarp(UUID uuid, String name, Location loc) {
        if(getWarp(name) == null) {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement("INSERT INTO " + table + " (uuid, name) VALUES (?,?);");
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
            } finally {
                try {
                    if (ps != null)
                        ps.close();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
                }
            }
            setLocation(name, loc);
        }
    }

    public HashMap<String, String> getWarp(String name) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE LOWER(name)=LOWER(?);");
            ps.setString(1, name);
            rs = ps.executeQuery();
            while(rs.next()){
                HashMap<String, String> wp = new HashMap<>();
                wp.put("name", rs.getString("name"));
                wp.put("open", String.valueOf(rs.getInt("open") == 1));
                wp.put("uuid", rs.getString("uuid"));
                wp.put("locYaw", rs.getString("locYaw"));
                wp.put("locPitch", rs.getString("locYaw"));
                return wp;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
        return null;
    }

    public ArrayList<HashMap<String, String>> getWarps(UUID uuid, Boolean openOnly) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if(uuid == null) {
                ps = connection.prepareStatement("SELECT * FROM " + table + (openOnly ? " WHERE open=1 ORDER by name ASC" : "") + ";");
            } else {
                ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE uuid=?" + (openOnly ? " AND open=1" : "") + ";");
                ps.setString(1, uuid.toString());
            }
            rs = ps.executeQuery();
            ArrayList<HashMap<String, String>> warps = new ArrayList<>();
            while(rs.next()) {
                HashMap<String, String> wp = new HashMap<>();
                wp.put("name", rs.getString("name"));
                wp.put("open", String.valueOf(rs.getInt("open") == 1));
                wp.put("uuid", rs.getString("uuid"));
                warps.add(wp);
            }
            return warps;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
        return null;
    }

    public Location getLocation(String name) {
        if(getWarp(name) != null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = connection.prepareStatement("SELECT locWorld,locX,locY,locZ,locPitch,locYaw FROM " + table + " WHERE name=?;");
                ps.setString(1, name);
                rs = ps.executeQuery();
                while (rs.next()) {
                    return new Location(Bukkit.getWorld(rs.getString("locWorld")), rs.getDouble("locX"), rs.getDouble("locY"), rs.getDouble("locZ"));
                }
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
            } finally {
                try {
                    if (ps != null)
                        ps.close();
                    if (rs != null)
                        rs.close();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
                }
            }
        }
        return null;
    }

    public void deleteWarp(String name) {
        if(getWarp(name) != null) {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement("DELETE FROM " + table + " WHERE name=?;");
                ps.setString(1, name);
                ps.executeUpdate();
                ps = connection.prepareStatement("DELETE FROM " + vtable + " WHERE name=?;");
                ps.setString(1, name);
                ps.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
            } finally {
                try {
                    if (ps != null)
                        ps.close();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
                }
            }
        }
    }

    public void setName(String name, String newname) {
        if(getWarp(newname) == null) {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement("UPDATE " + table + " SET name=? WHERE name=?;");
                ps.setString(1, newname);
                ps.setString(2, name);
                ps.executeUpdate();
                ps = connection.prepareStatement("UPDATE " + vtable + " SET name=? WHERE name=?;");
                ps.setString(1, newname);
                ps.setString(2, name);
                ps.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
            } finally {
                try {
                    if (ps != null)
                        ps.close();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
                }
            }
        }
    }

    public void setLocation(String name, Location l) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE " + table + " SET locWorld=?,locX=?,locY=?,locZ=?,locPitch=?,locYaw=? WHERE name=?;");
            ps.setString(1, Objects.requireNonNull(l.getWorld()).getName());
            ps.setDouble(2, l.getX());
            ps.setDouble(3, l.getY());
            ps.setDouble(4, l.getZ());
            ps.setFloat(6, l.getPitch());
            ps.setFloat(5, l.getYaw());
            ps.setString(7, name);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
    }

    public void setOpen(String name, Boolean open) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE " + table + " SET open=? WHERE name=?;");
            ps.setInt(1, open ? 1 : 0);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
    }

    public void addNewVote(UUID uuid, String name) {
        PreparedStatement ps = null;
        try {
            if(getLastVote(uuid, name) != null) {
                ps = connection.prepareStatement("UPDATE " + vtable + " SET last_vote=?, votes=votes+1 WHERE name=? AND uuid=?;");
                ps.setLong(1, System.currentTimeMillis());
                ps.setString(2, name);
                ps.setString(3, uuid.toString());
            } else {
                ps = connection.prepareStatement("INSERT INTO " + vtable + " (name, uuid, last_vote, votes) VALUES (?, ?, ?, ?);");
                ps.setString(1, name);
                ps.setString(2, uuid.toString());
                ps.setLong(3, System.currentTimeMillis());
                ps.setInt(4, 1);
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
    }

    public Long getLastVote(UUID uuid, String name) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT last_vote FROM " + vtable + " WHERE name=? AND uuid=?;");
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getLong("last_vote");
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
        return null;
    }

    public Integer getVotes(String name) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT votes FROM " + vtable + " WHERE name=?;");
            ps.setString(1, name);
            rs = ps.executeQuery();
            int votes = 0;
            while (rs.next()) {
                votes = votes + rs.getInt("votes");
            }
            return votes;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
        return null;
    }
}