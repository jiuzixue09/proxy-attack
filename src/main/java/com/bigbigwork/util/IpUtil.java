package com.bigbigwork.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpUtil {

    public static String getLocalIpv4Address() throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        String siteLocalAddress = null;
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                String hostAddress = addr.getHostAddress();
                if (addr instanceof Inet4Address
                        && !addr.isLoopbackAddress()
//                        && !hostAddress.startsWith("192.168")
                        && !hostAddress.startsWith("172.")
                        && !hostAddress.startsWith("169.")
                ) {
                    if (addr.isSiteLocalAddress()) {
                        siteLocalAddress = hostAddress;
                    } else {
                        return hostAddress;
                    }
                }
            }
        }
        return siteLocalAddress == null ? "" : siteLocalAddress;
    }
}
