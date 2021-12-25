package org.akadia.ath.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ConfigManager {
    final String CONFIG_FILENAME = "config.yml";
    final String TAG = ChatColor.translateAlternateColorCodes('&', "&f[&6阿影&f]");

    public FileConfiguration config;
    public File configFile;

    public File logFile;

    String diskLogging;
    String consoleLogging;
    String notify;
    String reloading;
    String reloaded;
    String noPerm;
    String unknownCommand;
    String outdated;
    String upToDate;

    public ConfigManager() {
        if (!Main.getMain().getDataFolder().exists()) {
            Main.getMain().getDataFolder().mkdir();
        }

        // Config file.
        configFile = new File(Main.getMain().getDataFolder(), CONFIG_FILENAME);
        // Copy default.
        backupConfig();
        if (!configFile.exists()) {
            Main.getMain().saveResource(CONFIG_FILENAME, true);
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            // Generating missing configuration.
            config = YamlConfiguration.loadConfiguration(configFile);
            try {
                generateConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // Language File.
        logFile = new File(Main.getMain().getDataFolder(), "log.yml");
        // Make an empty file.
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED
                        + "Could not create lang.yml!");
            }
        }

        consoleLogging = getMsg("logs.console", false);
        diskLogging = getMsg("logs.disk", false);
        notify = getMsg("msg.notify");
        reloading = getMsg("msg.reloading");
        reloaded = getMsg("msg.reloaded");
        noPerm = getMsg("msg.noPerm");
        unknownCommand = getMsg("msg.unknownCommand");
        outdated = getMsg("logs.outdated");
        upToDate = getMsg("logs.upToDate");
    }

    public String getMsg(String path, boolean isColor) {
        String str = config.getString(path);
        if (str == null) {
            return "";
        }
        if (isColor) {
            return ChatColor.translateAlternateColorCodes('&', str);
        } else {
            return str;
        }
    }

    public String getMsg(String path) {
        return getMsg(path, true);
    }

    private void backupConfig() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
        String timestamp = formatter.format(date);
        File dataDirectory = Main.getMain().getDataFolder();
        File playerDataDirectory = new File(dataDirectory, "config_backup");

        if (!playerDataDirectory.exists() && !playerDataDirectory.mkdirs()) {
            return;
        }

        FileUtil.copy(configFile, new File(playerDataDirectory, timestamp + "_" + configFile.getName()));
    }

    public void generateConfig() throws IOException {

        Logger logger = Main.getMain().getLogger();
        logger.info("Updating config to the latest...");
        int settings = 0;
        int addedSettings = 0;

        InputStream defConfigStream = Main.getMain().getResource(CONFIG_FILENAME);

        backupConfig();
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            for (String string : defConfig.getKeys(true)) {
                if (!config.contains(string)) {
                    config.set(string, defConfig.get(string));
                    addedSettings++;
                }
                if (!config.isConfigurationSection(string))
                    settings++;
            }
        }


        logger.info("Found " + settings + " config settings");
        logger.info("Added " + addedSettings + " new config settings");
        if (addedSettings > 0) {
            config.save(configFile);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED
                    + "Could not save CONFIG_FILENAME!");
        }
    }
}
