package com.pms.util;

public enum LocatorType {
	
	ID(1) ,
	CLASS_NAME(2) ,
	LINK_TEXT(3),
    XPATH(4),
	CSS(5);
    
private int type;

private LocatorType(int type) {
	this.type = type;
}


public int getType() {
	return type;
}

}
