package com.vyrimbot.Database;

import com.mongodb.*;
import com.vyrimbot.Main;
import com.vyrimbot.Tickets.Ticket;
import com.vyrimbot.Tickets.TicketType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.simpleyaml.configuration.file.YamlFile;

import java.text.SimpleDateFormat;
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

        mongoClient = new MongoClient(new MongoClientURI("mongodb://"+user+":"+password+"@"+host+":"+port+"/?authSource="+dbName+"&authMechanism=SCRAM-SHA-1"));
        database = mongoClient.getDB(config.getString("Database.DBName", "VyrimBot"));

        if(!database.collectionExists("Bans")) database.createCollection("Bans", null);
        if(!database.collectionExists("Invites")) database.createCollection("Invites", null);
        if(!database.collectionExists("Tickets")) database.createCollection("Tickets", null);
        if(!database.collectionExists("Infractions")) database.createCollection("Infractions", null);

        new Thread(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            loadTickets();
        }).start();
    }

    public void saveInvite(User member, String url) {
        DBCollection collection = database.getCollection("Invites");
        BasicDBObject query = new BasicDBObject();
        query.put("user", member.getIdLong());

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("url", url);

        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument);

        collection.update(query, updateObject);
    }

    public void createInvite(User user) {
        if(!hasInvite(user)) {
            DBCollection collection = database.getCollection("Invites");
            BasicDBObject document = new BasicDBObject();
            document.put("user", user.getIdLong());
            document.put("url", "none");

            collection.insert(document);
        }
    }

    public String getInvite(User member) {
        DBCollection collection = database.getCollection("Invites");

        if(hasInvite(member)) {
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("user", member.getIdLong());
            DBCursor cursor = collection.find(searchQuery);

            if (cursor.hasNext()) {
                DBObject document = cursor.next();

                if(!String.valueOf(document.get("url")).equalsIgnoreCase("none")) {
                    return (String) document.get("url");
                }
            }
        }

        return null;
    }

    public boolean hasInvite(User member) {
        DBCollection collection = database.getCollection("Invites");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("user", member.getIdLong());
        DBCursor cursor = collection.find(searchQuery);

        while (cursor.hasNext()) {
            DBObject document = cursor.next();

            return !String.valueOf(document.get("url")).equalsIgnoreCase("none");
        }

        return false;
    }

    public void unbanUser(long idLong, Member admin) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        DBCollection collection = database.getCollection("Bans");
        BasicDBObject query = new BasicDBObject();
        query.put("user", idLong);

        collection.remove(query);
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
        document.put("reason", reason);

        collection.insert(document);
        collection.save(document);
    }

    public void saveTicket(Ticket ticket) {
        DBCollection collection = database.getCollection("Tickets");
        BasicDBObject document = new BasicDBObject();
        document.put("guild", ticket.getTextChannel().getGuild().getIdLong());
        document.put("owner", ticket.getMember().getIdLong());
        document.put("channel", ticket.getTextChannel().getIdLong());
        document.put("type", ticket.getType().name().toLowerCase());

        collection.insert(document);
        collection.save(document);

        Main.debug("DATABASE", "Ticket saved "+ticket.getMember().getEffectiveName());
    }

    public void deleteTicket(Ticket ticket) {
        DBCollection collection = database.getCollection("Tickets");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("owner", ticket.getMember().getIdLong());

        collection.remove(searchQuery);

        Main.debug("DATABASE", "Ticket deleted "+ticket.getMember().getEffectiveName());
    }

    public void loadTickets() {
        DBCollection collection = database.getCollection("Tickets");
        DBCursor cursor = collection.find();

        int ticketsAmount = 0;

        while (cursor.hasNext()) {
            DBObject document = cursor.next();

            long guildId = Long.parseLong(String.valueOf(document.get("guild")));
            long memberId = Long.parseLong(String.valueOf(document.get("owner")));
            long channelId = Long.parseLong(String.valueOf(document.get("channel")));
            String sType = String.valueOf(document.get("type"));

            Guild guild = Main.getInstance().getJda().getGuildById(guildId);
            Member member = guild.getMemberById(memberId);
            TextChannel textChannel = guild.getTextChannelById(channelId);
            TicketType type = TicketType.getType(sType);

            if (member != null && textChannel != null) {
                Main.getTicketManager().loadExistTicket(member, type, textChannel);

                Main.debug("INFO", "Ticket loaded");
                ticketsAmount += 1;
            }
        }

        Main.debug("INFO", ticketsAmount+" tickets successfully uploaded");
    }
}
