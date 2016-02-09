package com.triplelift.sdk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.view.WindowManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static java.util.Collections.list;

public class Utils {

    public static String getIpAddress() throws SocketException {
        for (final NetworkInterface networkInterface : list(NetworkInterface.getNetworkInterfaces())) {
            for (final InetAddress address : list(networkInterface.getInetAddresses())) {
                if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                    String hostAddress = address.getHostAddress();
                    return hostAddress;
                }
            }
        }

        return null;
    }

    public static boolean isNetworkAvailable(Context context) {

        final int internetPermission = context.checkCallingOrSelfPermission(INTERNET);
        if (internetPermission == PackageManager.PERMISSION_DENIED) {
            return false;
        }

        final int networkAccessPermission = context.checkCallingOrSelfPermission(ACCESS_NETWORK_STATE);
        if (networkAccessPermission == PackageManager.PERMISSION_DENIED) {
            return true;
        }
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    public static int getWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        return width;
    }

    public static String getStringElseNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
