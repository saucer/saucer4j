package com.example.saucer4j.experiments;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import app.saucer.Saucer;
import app.saucer.SaucerApp;
import app.saucer.SaucerPreferences;
import app.saucer.util.SaucerSize;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

public class Win32NestedWebview {

    public static void main(String[] args) {
        if (Platform.osDistribution != OSDistribution.WINDOWS_NT) {
            throw new RuntimeException("This sample is only available on Windows.");
        }

        SaucerApp.initialize("com.example.saucer4j");

        Saucer parentWindow = Saucer.create(
            SaucerPreferences.create()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );
        parentWindow.webview().setContextMenuAllowed(true); // Allow the right-click menu.
        parentWindow.webview().setUrl("https://duckduckgo.com");
        parentWindow.window().setTitle("ParentWindow");
        parentWindow.window().show();

        Saucer childWindow = Saucer.create(
            SaucerPreferences.create()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );
        childWindow.webview().setContextMenuAllowed(true); // Allow the right-click menu.
        childWindow.webview().setUrl("https://example.com");
        childWindow.window().setTitle("ChildWindow");
        childWindow.window().show();

        // Replace these with native calls to saucer :D
        HWND parentWindowHandle = User32.INSTANCE.FindWindow(null, "ParentWindow");
        HWND childWindowHandle = User32.INSTANCE.FindWindow(null, "ChildWindow");

        User32.INSTANCE.SetParent(childWindowHandle, parentWindowHandle);
        childWindow.window().showDecorations(false);
        childWindow.window().setSize(new SaucerSize(400, 400));

        SaucerApp.run(); // This blocks until the last window is closed.
    }

}
