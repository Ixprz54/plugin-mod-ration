package fr.cuboria.moderation;

import fr.cuboria.moderation.commands.ReportCommand;
import fr.cuboria.moderation.commands.SanctionCommand;
import fr.cuboria.moderation.commands.StaffCommand;
import fr.cuboria.moderation.database.DatabaseManager;
import fr.cuboria.moderation.gui.GUIManager;
import fr.cuboria.moderation.listeners.FreezeListener;
import fr.cuboria.moderation.listeners.StaffItemListener;
import fr.cuboria.moderation.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ModerationPlugin extends JavaPlugin {

    private static ModerationPlugin instance;

    // Managers
    private DatabaseManager databaseManager;
    private StaffModeManager staffModeManager;
    private FreezeManager freezeManager;
    private SanctionManager sanctionManager;
    private ReportManager reportManager;
    private DiscordWebhookManager discordWebhookManager;
    private ConfigurationManager configurationManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("═══════════════════════════════════════");
        getLogger().info("  Cuboria Moderation Plugin");
        getLogger().info("  Version: " + getDescription().getVersion());
        getLogger().info("  Développeur: EmyXtrm");
        getLogger().info("═══════════════════════════════════════");

        // Chargement de la configuration
        saveDefaultConfig();
        saveResource("sanctions.yml", false);

        // Initialisation des managers
        initializeManagers();

        // Enregistrement des commandes
        registerCommands();

        // Enregistrement des listeners
        registerListeners();

        getLogger().info("Plugin de modération chargé avec succès!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Désactivation du plugin de modération...");

        // Restauration de tous les inventaires staff
        if (staffModeManager != null) {
            staffModeManager.disableAllStaffModes();
        }

        // Fermeture de la connexion à la base de données
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("Plugin de modération désactivé!");
    }

    private void initializeManagers() {
        try {
            // Configuration Manager
            this.configurationManager = new ConfigurationManager(this);
            getLogger().info("✓ Configuration Manager initialisé");

            // Database Manager
            if (getConfig().getBoolean("database.enabled", true)) {
                this.databaseManager = new DatabaseManager(this);
                this.databaseManager.initialize();
                getLogger().info("✓ Database Manager initialisé");
            }

            // Discord Webhook Manager
            this.discordWebhookManager = new DiscordWebhookManager(this);
            getLogger().info("✓ Discord Webhook Manager initialisé");

            // Staff Mode Manager
            this.staffModeManager = new StaffModeManager(this);
            getLogger().info("✓ Staff Mode Manager initialisé");

            // Freeze Manager
            this.freezeManager = new FreezeManager(this);
            getLogger().info("✓ Freeze Manager initialisé");

            // Sanction Manager
            this.sanctionManager = new SanctionManager(this);
            getLogger().info("✓ Sanction Manager initialisé");

            // Report Manager
            this.reportManager = new ReportManager(this);
            getLogger().info("✓ Report Manager initialisé");

            // GUI Manager
            this.guiManager = new GUIManager();
            getLogger().info("✓ GUI Manager initialisé");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation des managers!", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommands() {
        StaffCommand staffCommand = new StaffCommand(this);
        getCommand("staffco").setExecutor(staffCommand);
        getCommand("staffdeco").setExecutor(staffCommand);
        getCommand("sanction").setExecutor(new SanctionCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));

        getLogger().info("✓ Commandes enregistrées");
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new StaffItemListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);

        getLogger().info("✓ Listeners enregistrés");
    }

    // Getters
    public static ModerationPlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StaffModeManager getStaffModeManager() {
        return staffModeManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public SanctionManager getSanctionManager() {
        return sanctionManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public DiscordWebhookManager getDiscordWebhookManager() {
        return discordWebhookManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }
}
