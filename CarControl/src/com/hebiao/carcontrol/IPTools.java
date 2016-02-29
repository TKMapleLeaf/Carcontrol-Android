package com.hebiao.carcontrol;

public class IPTools {
	
	public static final String CONNECT=":8080/carcontrol/control/connect.action";
	public static final String OTHERCMD=":8080/carcontrol/control/sendCmd.action";
	public static final String HEAD="http://";
	
	public static String connectUrl(String ip){
		return HEAD+ip+CONNECT;
	}
	public static String cmdUrl(String ip){
		return HEAD+ip+OTHERCMD;
	}
}
