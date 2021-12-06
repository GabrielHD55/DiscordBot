package com.vyrimbot.Utils;

import com.vyrimbot.App;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigCreator {

    private static ConfigCreator instance;

    public static ConfigCreator get() {
        if (ConfigCreator.instance == null) {
            ConfigCreator.instance = new ConfigCreator();
        }
        return ConfigCreator.instance;
    }

    public void setup(App plugin, String configname) {
        File configFile = new File(plugin.getDataFolder(), configname);
        if (!configFile.exists()) {
            try {
                try (InputStream is = plugin.getResourceAsStream(configname)) {
                    Files.copy(is, configFile.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error on create " + configname + " please contact with Developer: GabrielHD55", e);
            }
        }
    }
}