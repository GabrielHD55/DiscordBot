package com.vyrimbot.Database;

import com.mongodb.*;
import com.vyrimbot.Main;
import com.vyrimbot.Tickets.Ticket;
import com.vyrimbot.Tickets.TicketType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.simpleyaml.configuration.file.YamlFile;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Database {

    @Getter private final MongoClient mongoClient;
    @Getter private final DB database;

    public Database() {
        YamlFile config = Main.getInstance().getConfig();

        String host = config.getString("Database.Host");
        String user = config.getString("Database.Username");
        String dbName = config.getString("Database.DBName");
        String password = config.getString("Database.Password");
        String port = config.getString("Database.Port");

        mongoClient = new MongoClient(new MongoClientURI("mongodb://"+user+":"+password+"@"+host+":"+port+"/?authSource="+dbName));
        database = mongoClient.getDB(config.getString("Database.DBName", "VyrimBot"));

        if(database.getCollection("Bans") == null) database.createCollection("Bans", null);
        if(database.getCollection("Kicks") == null) database.createCollection("Kicks", null);
        if(database.getCollection("Mutes") == null) database.createCollection("Mutes", null);
        if(database.getCollection("Tickets") == null) database.createCollection("Tickets", null);

        loadTickets();
    }

    public void banUser(Member member, Member admin, String reason) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        DBCollection collection = database.getCollection("Bans");
        BasicDBObject document = new BasicDBObject();
        document.put("date", formatter.format(date));
        document.put("guild", member.getGuild().getIdLong());
        document.put("user", member.getUser().getIdLong());
        document.put("admin", admin.getUser().getIdLong());
        document.put("unbanned", false);
        document.put("reason", reason);

        collection.insert(document);
    }

    public void saveTicket(Ticket ticket) {
        DBCollection collection = database.getCollection("Tickets");
        BasicDBObject document = new BasicDBObject();
        document.put("guild", ticket.getTextChannel().getGuild().getIdLong());
        document.put("owner", ticket.getMember().getIdLong());
        document.put("channel", ticket.getTextChannel().getIdLong());
        document.put("type", ticket.getType().name().toLowerCase());

        collection.insert(document);
    }

    public void deleteTicket(Ticket ticket) {
        DBCollection collection = database.getCollection("Tickets");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("owner", ticket.getMember().getIdLong());

        collection.remove(searchQuery);
    }

    public void loadTickets() {
        DBCollection collection = database.getCollection("Tickets");
        DBCursor cursor = collection.find();

        int ticketsAmount = 0;

        while (cursor.hasNext()) {
            DBObject document = cursor.next();

            Guild guild = Main.getInstance().getJda().getGuildById(Long.parseLong(String.valueOf(document.get("guild"))));

            Member member = guild.getMemberById(Long.parseLong(String.valueOf(document.get("owner"))));
            TextChannel textChannel = guild.getTextChannelById(Long.parseLong(String.valueOf(document.get("channel"))));
            TicketType type = TicketType.getType(String.valueOf(document.get("type")));

            if(member != null && textChannel != null) {
                Main.getTicketManager().loadExistTicket(member, type, textChannel);

                ticketsAmount++;
            }
        }

        Main.debug("INFO", ticketsAmount+" tickets successfully uploaded");
    }
}
