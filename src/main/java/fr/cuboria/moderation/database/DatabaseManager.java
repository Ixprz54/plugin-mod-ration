package fr.cuboria.moderation.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Report;
import fr.cuboria.moderation.models.Sanction;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final ModerationPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(ModerationPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        setupConnectionPool();
        createTables();
    }

    private void setupConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://" +
                plugin.getConfig().getString("database.host") + ":" +
                plugin.getConfig().getInt("database.port") + "/" +
                plugin.getConfig().getString("database.database"));
        config.setUsername(plugin.getConfig().getString("database.username"));
        config.setPassword(plugin.getConfig().getString("database.password"));
        config.setMaximumPoolSize(plugin.getConfig().getInt("database.pool-size", 10));

        // Explicitly set the driver class name to use the relocated MariaDB driver
        config.setDriverClassName("fr.cuboria.moderation.shaded.mariadb.jdbc.Driver");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Pool de connexions HikariCP initialisé avec succès");
    }

    private void createTables() {
        String sanctionsTable = "CREATE TABLE IF NOT EXISTS cuboria_sanctions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "target_uuid VARCHAR(36) NOT NULL," +
                "target_name VARCHAR(16) NOT NULL," +
                "staff_uuid VARCHAR(36) NOT NULL," +
                "staff_name VARCHAR(16) NOT NULL," +
                "type VARCHAR(16) NOT NULL," +
                "reason TEXT NOT NULL," +
                "duration BIGINT NOT NULL," +
                "timestamp BIGINT NOT NULL," +
                "active BOOLEAN DEFAULT TRUE," +
                "INDEX idx_target_uuid (target_uuid)," +
                "INDEX idx_staff_uuid (staff_uuid)," +
                "INDEX idx_timestamp (timestamp)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        String reportsTable = "CREATE TABLE IF NOT EXISTS cuboria_reports (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "reporter_uuid VARCHAR(36) NOT NULL," +
                "reporter_name VARCHAR(16) NOT NULL," +
                "target_uuid VARCHAR(36) NOT NULL," +
                "target_name VARCHAR(16) NOT NULL," +
                "reason TEXT NOT NULL," +
                "timestamp BIGINT NOT NULL," +
                "handled BOOLEAN DEFAULT FALSE," +
                "handled_by_uuid VARCHAR(36)," +
                "handled_by_name VARCHAR(16)," +
                "INDEX idx_target_uuid (target_uuid)," +
                "INDEX idx_handled (handled)," +
                "INDEX idx_timestamp (timestamp)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        String staffInventoriesTable = "CREATE TABLE IF NOT EXISTS cuboria_staff_inventories (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "inventory MEDIUMBLOB NOT NULL," +
                "armor BLOB," +
                "timestamp BIGINT NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sanctionsTable);
            stmt.executeUpdate(reportsTable);
            stmt.executeUpdate(staffInventoriesTable);
            plugin.getLogger().info("Tables de la base de données créées avec succès");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la création des tables", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Connexion à la base de données fermée");
        }
    }

    // ==================== SANCTIONS ====================

    public void saveSanction(Sanction sanction) {
        String query = "INSERT INTO cuboria_sanctions (target_uuid, target_name, staff_uuid, staff_name, type, reason, duration, timestamp, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sanction.getTargetUuid().toString());
            stmt.setString(2, sanction.getTargetName());
            stmt.setString(3, sanction.getStaffUuid().toString());
            stmt.setString(4, sanction.getStaffName());
            stmt.setString(5, sanction.getType().name());
            stmt.setString(6, sanction.getReason());
            stmt.setLong(7, sanction.getDuration());
            stmt.setLong(8, sanction.getTimestamp());
            stmt.setBoolean(9, sanction.isActive());

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

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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
        String query = "SELECT * FROM cuboria_sanctions WHERE target_uuid = ? AND type = 'BAN' AND active = TRUE ORDER BY timestamp DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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
        String query = "SELECT * FROM cuboria_sanctions WHERE target_uuid = ? AND type = 'MUTE' AND active = TRUE ORDER BY timestamp DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, sanction.isActive());
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
                rs.getBoolean("active")
        );
    }

    // ==================== REPORTS ====================

    public void saveReport(Report report) {
        String query = "INSERT INTO cuboria_reports (reporter_uuid, reporter_name, target_uuid, target_name, reason, timestamp, handled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, report.getReporterUuid().toString());
            stmt.setString(2, report.getReporterName());
            stmt.setString(3, report.getTargetUuid().toString());
            stmt.setString(4, report.getTargetName());
            stmt.setString(5, report.getReason());
            stmt.setLong(6, report.getTimestamp());
            stmt.setBoolean(7, report.isHandled());

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
        String query = "SELECT * FROM cuboria_reports WHERE handled = FALSE ORDER BY timestamp DESC LIMIT 50";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
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
        String query = "UPDATE cuboria_reports SET handled = TRUE, handled_by_uuid = ?, handled_by_name = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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
                rs.getBoolean("handled"),
                rs.getString("handled_by_uuid") != null ? UUID.fromString(rs.getString("handled_by_uuid")) : null,
                rs.getString("handled_by_name")
        );
    }

    // ==================== STAFF INVENTORIES ====================

    public void saveStaffInventory(UUID playerUuid, ItemStack[] inventory, ItemStack[] armor) {
        String query = "REPLACE INTO cuboria_staff_inventories (uuid, inventory, armor, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la suppression de l'inventaire staff", e);
        }
    }
}
