package com.vyrimbot.Logs;

public enum LogType {
	
	Root,
	Punishment;
	
	public static LogType getType(String name) {
		for(LogType type : values()) {
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
	}

}
