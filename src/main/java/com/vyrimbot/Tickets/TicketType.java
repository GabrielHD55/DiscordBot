package com.vyrimbot.Tickets;

public enum TicketType {

    GeneralSupport,
    BanAppeal,
    BugReport,
    PlayerReport,
    StoreIssues,
    StaffApplication;

    public static TicketType getType(String name) {
        for(TicketType type : values()) {
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
