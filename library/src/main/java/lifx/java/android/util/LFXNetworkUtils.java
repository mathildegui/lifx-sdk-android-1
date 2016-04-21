//
//  LFXNetworkUtils.java
//  LIFX
//
//  Created by Jarrod Boyes on 24/03/14.
//  Copyright (c) 2014 LIFX Labs. All rights reserved.
//

package lifx.java.android.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class LFXNetworkUtils {
    private final static String TAG = LFXNetworkUtils.class.getSimpleName();
    public static final int EXPIRE_TIME = 60000; //ms
    public static String sLocalHostAddress = null;
    public static long sLocalHostAddressExpires;
    public static String sBroadcastAddress = null;
    public static long sBroadcastAddressExpires;

    @SuppressLint("DefaultLocale")
    public static String getLocalHostAddress() {
        if (sLocalHostAddress != null && sLocalHostAddressExpires > System.currentTimeMillis()) {
            return sLocalHostAddress;
        }

        boolean useIPv4 = true;

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = sAddr.split("\\.").length == 4;
                        if (useIPv4) {
                            if (isIPv4) {
                                sLocalHostAddress = sAddr;
                                sLocalHostAddressExpires = System.currentTimeMillis() + EXPIRE_TIME;
                                return sAddr;
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                sLocalHostAddress = delim < 0 ? sAddr : sAddr.substring(0, delim);
                                sLocalHostAddressExpires = System.currentTimeMillis() + EXPIRE_TIME;
                                return sLocalHostAddress;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }


//	public static String getBroadcastAddress()
//	{
//		return "255.255.255.255";
//	}

//	public static String getBroadcastAddress()
//	{
//	    String found_bcast_address = null;
//	    System.setProperty( "java.net.preferIPv4Stack", "true"); 
//	    
//	    try
//	    {
//	         Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces();
//	         while( niEnum.hasMoreElements())
//	         {
//	             NetworkInterface ni = niEnum.nextElement();
//	             
//	             Enumeration<InetAddress> iNetAddresses = ni.getInetAddresses();
//	             while( iNetAddresses.hasMoreElements())
//	             {
//	            	 InetAddress i = iNetAddresses.nextElement();
//	            	 System.out.println( "Address: " + i.getHostAddress());
//	             }
//	             
//	             if( !ni.isLoopback())
//	             {
//	                for( InterfaceAddress interfaceAddress : ni.getInterfaceAddresses())
//	                {
//	                	if( interfaceAddress.getBroadcast() != null)
//	                	{
//	                		found_bcast_address = interfaceAddress.getBroadcast().toString();
//	                		found_bcast_address = found_bcast_address.substring( 1);
//	                	}
//	                	else
//	                	{
//	                		System.out.println( "Found bcast address: " + interfaceAddress.toString());
//	                	}
//	                }
//	             }
//	          }
//	     }
//	     catch( SocketException e)
//	     {
//	          e.printStackTrace();
//	     }
//
//	     return found_bcast_address;
//	}

    public static String getBroadcastAddress(Context context) {
        if (sBroadcastAddress != null && sBroadcastAddressExpires > System.currentTimeMillis()) {
            return sBroadcastAddress;
        }

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));
        try {
            sBroadcastAddress = InetAddress.getByAddress(quads).getHostAddress();
            sBroadcastAddressExpires = System.currentTimeMillis() + EXPIRE_TIME;
            return sBroadcastAddress;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        sBroadcastAddress = "255.255.255.255";
        sBroadcastAddressExpires = System.currentTimeMillis() + EXPIRE_TIME;
        return sBroadcastAddress;
    }

    public static String getIPv4StringByStrippingIPv6Prefix(String in) {
        String ipv6Prefix = "::ffff:";

        if (in.startsWith(ipv6Prefix)) {
            return in.substring(ipv6Prefix.length(), in.length());
        }

        return in;
    }
}
