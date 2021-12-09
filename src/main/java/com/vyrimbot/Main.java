package com.vyrimbot;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.vyrimbot.Commands.GeneralCmds;
import com.vyrimbot.Commands.ModCmds;
import com.vyrimbot.Listeners.AntiBot;
import com.vyrimbot.Listeners.GiveawayListener;
import com.vyrimbot.Listeners.TicketListener;
import com.vyrimbot.Managers.GiveawaysManager;
import com.vyrimbot.Managers.TicketManager;
import com.vyrimbot.Utils.ConfigCreator;
import com.vyrimbot.Giveaways.Giveaway;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends App {

    @Getter private static Main instance;

    @Getter private static String prefix;
    @Getter private static String botName;
    @Getter private static TicketManager ticketManager;

    @Getter private JDA jda;
    @Getter private YamlFile lang;
    @Getter private YamlFile config;
    @Getter private YamlFile tickets;
    @Getter private MongoClient mongoClient;

    @Getter private static GiveawaysManager giveawayManager;

    @Override
    public void onEnable() {
        instance = this;

        ConfigCreator.get().setup(this, "Settings.yml");
        ConfigCreator.get().setup(this, "Lang.yml");

        config = new YamlFile(new File(this.getDataFolder(), "Settings.yml"));
        lang = new YamlFile(new File(this.getDataFolder(), "Lang.yml"));
        try {
            config.load();
            lang.load();
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

        ticketManager = new TicketManager();

        botName = config.getString("Settings.Name", "VyrimBot");
        prefix = config.getString("Settings.Prefix", "+");

        debug("INFO", "Starting "+botName+"...");

        connectBot();

        giveawayManager = new GiveawaysManager();
        //connectDatabase();
    }

    public void connectBot() {
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(config.getString("Settings.Token"));
            jdaBuilder.setAutoReconnect(config.getBoolean("Settings.AutoReconnect", true));
            if(!config.getString("Activity.Name").isEmpty()) {
                jdaBuilder.setActivity(Activity.of(Activity.ActivityType.valueOf(config.getString("Activity.Type", "DEFAULT")), config.getString("Activity.Name"), config.getString("Activity.URL")));
            }
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS);
            jdaBuilder.setChunkingFilter(ChunkingFilter.NONE);
            jdaBuilder.disableCache(CacheFlag.ACTIVITY);
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.addEventListeners(new AntiBot(), new GeneralCmds(), new ModCmds(), new TicketListener(), new GiveawayListener());

            jda = jdaBuilder.build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            debug("ERROR", e.getMessage());
            e.printStackTrace();
        }
    }

    public void connectDatabase() {
        mongoClient = new MongoClient(config.getString("Database.Host", "localhost"), config.getInt("Database.Port", 27017));
        DB database = mongoClient.getDB(config.getString("Database.DBName", "VyrimBot"));
    }

    public static void debug(String type, String message) {
        System.out.println("[" + botName + "] " + type + " " + message);
    }
}
