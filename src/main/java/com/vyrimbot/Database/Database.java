package com.vyrimbot.Database;

import com.mongodb.*;
import com.vyrimbot.Main;
import com.vyrimbot.Giveaways.Giveaway;
import com.vyrimbot.Tickets.Ticket;
import com.vyrimbot.Tickets.TicketType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
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

        mongoClient = new MongoClient(new MongoClientURI("mongodb://"+user+":"+password+"@"+host+":"+port+"/?authSource="+dbName));
        database = mongoClient.getDB(config.getString("Database.DBName", "VyrimBot"));

        if(database.getCollection("Bans") == null) database.createCollection("Bans", null);
        if(database.getCollection("Invites") == null) database.createCollection("Invites", null);
        if(database.getCollection("Tickets") == null) database.createCollection("Tickets", null);
        if(database.getCollection("Infractions") == null) database.createCollection("Infractions", null);

        loadTickets();
    }

    public void saveInvite(Member member, String url) {
        DBCollection collection = database.getCollection("Invites");
        BasicDBObject query = new BasicDBObject();
        query.put("user", member.getIdLong());

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("url", url);

        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument);

        collection.update(query, updateObject);
    }

    public String getInvite(Member member) {
        DBCollection collection = database.getCollection("Invites");

        if(hasInvite(member)) {
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("user", member.getIdLong());
            DBCursor cursor = collection.find(searchQuery);

            if (cursor.hasNext()) {
                DBObject document = cursor.next();

                return (String) document.get("url");
            }
        }

        BasicDBObject document = new BasicDBObject();
        document.put("guild", member.getGuild().getIdLong());
        document.put("user", member.getUser().getIdLong());
        document.put("url", "none");

        collection.insert(document);

        return null;
    }

    public boolean hasInvite(Member member) {
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
    
    public void saveGiveaway(Giveaway giveaway) {
    	DBCollection collection = database.getCollection("Giveaways");
        
    	BasicDBObject document = new BasicDBObject();
    	
    	document.put("messageID", giveaway.getMessageId());
    	document.put("channelID", giveaway.getChannel().getId());
    	document.put("args", giveaway.getSerializedArgs());
    	
    	collection.insert(document);
    	
    }
    
    public void loadGiveaways() {
    	DBCollection collection = database.getCollection("Giveaways");
        DBCursor cursor = collection.find();
        
        for(int i = 0; cursor.hasNext(); i++) {
        	DBObject document = cursor.next();

            TextChannel c = Main.getInstance().getJda().getTextChannelById(String.valueOf(document.get("channelID")));
            
            new Giveaway(String.valueOf(document.get("messageID")), c, String.valueOf(document.get("args")).split(" "));
        }
        
    }
    
    public void deleteGiveaway(Giveaway giveaway) {
    	DBCollection collection = database.getCollection("Tickets");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("messageID", giveaway.getMessageId());

        collection.remove(searchQuery);
    }
}
