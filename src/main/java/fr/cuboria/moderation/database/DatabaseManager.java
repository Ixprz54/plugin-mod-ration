package fr.cuboria.moderation.database;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Report;
import fr.cuboria.moderation.models.Sanction;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final ModerationPlugin plugin;
    private Connection connection;
    private final File databaseFile;

    public DatabaseManager(ModerationPlugin plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "cuboria_moderation.db");
    }

    public void initialize() {
        try {
            // Créer le dossier du plugin si nécessaire
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            // Charger le driver SQLite
            Class.forName("org.sqlite.JDBC");

            // Établir la connexion
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            plugin.getLogger().info("Connexion SQLite établie avec succès: " + databaseFile.getName());

            // Créer les tables
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation de la base de données SQLite", e);
        }
    }

    private void createTables() {
        String sanctionsTable = "CREATE TABLE IF NOT EXISTS cuboria_sanctions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "target_uuid TEXT NOT NULL," +
                "target_name TEXT NOT NULL," +
                "staff_uuid TEXT NOT NULL," +
                "staff_name TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "reason TEXT NOT NULL," +
                "duration INTEGER NOT NULL," +
                "timestamp INTEGER NOT NULL," +
                "active INTEGER DEFAULT 1" +
                ");";

        String reportsTable = "CREATE TABLE IF NOT EXISTS cuboria_reports (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "reporter_uuid TEXT NOT NULL," +
                "reporter_name TEXT NOT NULL," +
                "target_uuid TEXT NOT NULL," +
                "target_name TEXT NOT NULL," +
                "reason TEXT NOT NULL," +
                "timestamp INTEGER NOT NULL," +
                "handled INTEGER DEFAULT 0," +
                "handled_by_uuid TEXT," +
                "handled_by_name TEXT" +
                ");";

        String staffInventoriesTable = "CREATE TABLE IF NOT EXISTS cuboria_staff_inventories (" +
                "uuid TEXT PRIMARY KEY," +
                "inventory BLOB NOT NULL," +
                "armor BLOB," +
                "timestamp INTEGER NOT NULL" +
                ");";

        // Créer les index pour améliorer les performances
        String indexSanctionsTarget = "CREATE INDEX IF NOT EXISTS idx_sanctions_target ON cuboria_sanctions(target_uuid);";
        String indexSanctionsStaff = "CREATE INDEX IF NOT EXISTS idx_sanctions_staff ON cuboria_sanctions(staff_uuid);";
        String indexSanctionsTimestamp = "CREATE INDEX IF NOT EXISTS idx_sanctions_timestamp ON cuboria_sanctions(timestamp);";
        String indexReportsTarget = "CREATE INDEX IF NOT EXISTS idx_reports_target ON cuboria_reports(target_uuid);";
        String indexReportsHandled = "CREATE INDEX IF NOT EXISTS idx_reports_handled ON cuboria_reports(handled);";
        String indexReportsTimestamp = "CREATE INDEX IF NOT EXISTS idx_reports_timestamp ON cuboria_reports(timestamp);";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sanctionsTable);
            stmt.executeUpdate(reportsTable);
            stmt.executeUpdate(staffInventoriesTable);
            stmt.executeUpdate(indexSanctionsTarget);
            stmt.executeUpdate(indexSanctionsStaff);
            stmt.executeUpdate(indexSanctionsTimestamp);
            stmt.executeUpdate(indexReportsTarget);
            stmt.executeUpdate(indexReportsHandled);
            stmt.executeUpdate(indexReportsTimestamp);
            plugin.getLogger().info("Tables SQLite créées avec succès");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la création des tables", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Connexion SQLite fermée");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
        }
    }

    // ==================== SANCTIONS ====================

    public void saveSanction(Sanction sanction) {
        String query = "INSERT INTO cuboria_sanctions (target_uuid, target_name, staff_uuid, staff_name, type, reason, duration, timestamp, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sanction.getTargetUuid().toString());
            stmt.setString(2, sanction.getTargetName());
            stmt.setString(3, sanction.getStaffUuid().toString());
            stmt.setString(4, sanction.getStaffName());
            stmt.setString(5, sanction.getType().name());
            stmt.setString(6, sanction.getReason());
            stmt.setLong(7, sanction.getDuration());
            stmt.setLong(8, sanction.getTimestamp());
            stmt.setInt(9, sanction.isActive() ? 1 : 0);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                sanction.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la sauvegarde de la sanction", e);
        }
    }

    public List<Sanction> getPlayerSanctions(UUID playerUuid) {
        List<Sanction> sanctions = new ArrayList<>();
        String query = "SELECT * FROM cuboria_sanctions WHERE target_uuid = ? ORDER BY timestamp DESC";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sanctions.add(buildSanctionFromResultSet(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la récupération des sanctions", e);
        }

        return sanctions;
    }

    public Sanction getActiveBan(UUID playerUuid) {
        String query = "SELECT * FROM cuboria_sanctions WHERE target_uuid = ? AND type = 'BAN' AND active = 1 ORDER BY timestamp DESC LIMIT 1";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Sanction sanction = buildSanctionFromResultSet(rs);
                if (!sanction.isExpired()) {
                    return sanction;
                } else {
                    // Marquer comme inactif si expiré
                    sanction.setActive(false);
                    updateSanction(sanction);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la récupération du ban actif", e);
        }

        return null;
    }

    public Sanction getActiveMute(UUID playerUuid) {
        String query = "SELECT * FROM cuboria_sanctions WHERE target_uuid = ? AND type = 'MUTE' AND active = 1 ORDER BY timestamp DESC LIMIT 1";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Sanction sanction = buildSanctionFromResultSet(rs);
                if (!sanction.isExpired()) {
                    return sanction;
                } else {
                    // Marquer comme inactif si expiré
                    sanction.setActive(false);
                    updateSanction(sanction);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la récupération du mute actif", e);
        }

        return null;
    }

    public void updateSanction(Sanction sanction) {
        String query = "UPDATE cuboria_sanctions SET active = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, sanction.isActive() ? 1 : 0);
            stmt.setInt(2, sanction.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la mise à jour de la sanction", e);
        }
    }

    private Sanction buildSanctionFromResultSet(ResultSet rs) throws SQLException {
        return new Sanction(
                rs.getInt("id"),
                UUID.fromString(rs.getString("target_uuid")),
                rs.getString("target_name"),
                UUID.fromString(rs.getString("staff_uuid")),
                rs.getString("staff_name"),
                Sanction.SanctionType.valueOf(rs.getString("type")),
                rs.getString("reason"),
                rs.getLong("duration"),
                rs.getLong("timestamp"),
                rs.getInt("active") == 1
        );
    }

    // ==================== REPORTS ====================

    public void saveReport(Report report) {
        String query = "INSERT INTO cuboria_reports (reporter_uuid, reporter_name, target_uuid, target_name, reason, timestamp, handled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, report.getReporterUuid().toString());
            stmt.setString(2, report.getReporterName());
            stmt.setString(3, report.getTargetUuid().toString());
            stmt.setString(4, report.getTargetName());
            stmt.setString(5, report.getReason());
            stmt.setLong(6, report.getTimestamp());
            stmt.setInt(7, report.isHandled() ? 1 : 0);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                report.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la sauvegarde du report", e);
        }
    }

    public List<Report> getPendingReports() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM cuboria_reports WHERE handled = 0 ORDER BY timestamp DESC LIMIT 50";

        try (PreparedStatement stmt = getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reports.add(buildReportFromResultSet(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la récupération des reports", e);
        }

        return reports;
    }

    public void markReportAsHandled(Report report, UUID handlerUuid, String handlerName) {
        String query = "UPDATE cuboria_reports SET handled = 1, handled_by_uuid = ?, handled_by_name = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, handlerUuid.toString());
            stmt.setString(2, handlerName);
            stmt.setInt(3, report.getId());
            stmt.executeUpdate();

            report.setHandled(true);
            report.setHandledByUuid(handlerUuid);
            report.setHandledByName(handlerName);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du marquage du report", e);
        }
    }

    private Report buildReportFromResultSet(ResultSet rs) throws SQLException {
        return new Report(
                rs.getInt("id"),
                UUID.fromString(rs.getString("reporter_uuid")),
                rs.getString("reporter_name"),
                UUID.fromString(rs.getString("target_uuid")),
                rs.getString("target_name"),
                rs.getString("reason"),
                rs.getLong("timestamp"),
                rs.getInt("handled") == 1,
                rs.getString("handled_by_uuid") != null ? UUID.fromString(rs.getString("handled_by_uuid")) : null,
                rs.getString("handled_by_name")
        );
    }

    // ==================== STAFF INVENTORIES ====================

    public void saveStaffInventory(UUID playerUuid, ItemStack[] inventory, ItemStack[] armor) {
        String query = "INSERT OR REPLACE INTO cuboria_staff_inventories (uuid, inventory, armor, timestamp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setBytes(2, InventorySerializer.serializeItemStacks(inventory));
            stmt.setBytes(3, InventorySerializer.serializeItemStacks(armor));
            stmt.setLong(4, System.currentTimeMillis());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la sauvegarde de l'inventaire staff", e);
        }
    }

    public ItemStack[][] loadStaffInventory(UUID playerUuid) {
        String query = "SELECT inventory, armor FROM cuboria_staff_inventories WHERE uuid = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ItemStack[] inventory = InventorySerializer.deserializeItemStacks(rs.getBytes("inventory"));
                ItemStack[] armor = InventorySerializer.deserializeItemStacks(rs.getBytes("armor"));
                return new ItemStack[][]{inventory, armor};
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du chargement de l'inventaire staff", e);
        }

        return null;
    }

    public void deleteStaffInventory(UUID playerUuid) {
        String query = "DELETE FROM cuboria_staff_inventories WHERE uuid = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la suppression de l'inventaire staff", e);
        }
    }
}
