package com.vyrimbot;

import com.vyrimbot.Commands.GeneralCmds;
import com.vyrimbot.Commands.ModCmds;
import com.vyrimbot.Database.Database;
import com.vyrimbot.Listeners.*;
import com.vyrimbot.Managers.GiveawaysManager;
import com.vyrimbot.Managers.LogsManager;
import com.vyrimbot.Managers.ReactionRoleManager;
import com.vyrimbot.Managers.TicketManager;
import com.vyrimbot.Utils.ConfigCreator;
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
import java.time.ZoneId;

public class Main extends App {

    @Getter private static Main instance;

    @Getter private static String prefix;
    @Getter private static String botName;

    @Getter private JDA jda;
    @Getter private ZoneId zoneId;
    @Getter private YamlFile lang;
    @Getter private YamlFile config;
    @Getter private YamlFile tickets;
    @Getter private Database database;

    @Getter private static LogsManager logsManager;
    @Getter private static TicketManager ticketManager;
    @Getter private static GiveawaysManager giveawayManager;
    @Getter private static ReactionRoleManager rrManager;

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

        botName = config.getString("Settings.Name", "VyrimBot");
        prefix = config.getString("Settings.Prefix", "+");

        debug("INFO", "Starting "+botName+"...");
        connectBot();

        ticketManager = new TicketManager();
        giveawayManager = new GiveawaysManager();
        rrManager = new ReactionRoleManager();
        logsManager = new LogsManager();

        database = new Database();
    }

    public void connectBot() {
        String token = config.getString("Settings.Token");

        if(token == null || token.isEmpty()) {
            debug("ERROR", "Please set the token in the configuration!");

            System.exit(0);
            return;
        }

        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(token);
            jdaBuilder.setAutoReconnect(config.getBoolean("Settings.AutoReconnect", true));
            if(!config.getString("Activity.Name").isEmpty()) {
                jdaBuilder.setActivity(Activity.of(Activity.ActivityType.valueOf(config.getString("Activity.Type", "DEFAULT")), config.getString("Activity.Name"), config.getString("Activity.URL")));
            }
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS);
            jdaBuilder.setChunkingFilter(ChunkingFilter.NONE);
            jdaBuilder.disableCache(CacheFlag.ACTIVITY);
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.addEventListeners(new AntiBot(), new GeneralCmds(), new ModCmds(), new TicketListener(), new GiveawayListener(), new ReactionRoleListener(), new InviteTracker(), new LogsListener());

            jda = jdaBuilder.build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            debug("ERROR", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void debug(String type, String message) {
        System.out.println("[" + botName + "] " + type + " " + message);
    }
}
