package com.finalproj.finalproject.desktopmodule;

import com.finalproj.finalproject.game.FinalProject; // or whatever your main game class import is
import com.jme3.system.AppSettings;

public class DesktopLauncher {
    public static void main(String[] args) {
        // 1. Instantiate your game application class
        FinalProject app = new FinalProject(); 
        
        // 2. Create custom application settings
        AppSettings settings = new AppSettings(true);
        
        // 3. FORCE the standard desktop rendering backend, bypassing ANGLE GLES
        settings.setRenderer(AppSettings.LWJGL_OPENGL3); 
        
        // 4. (Optional) Set a title or window dimensions if you like
        settings.setTitle("My APCSA Final Project");
        
        // 5. Apply the settings and launch
        app.setSettings(settings);
        app.start();
    }
}