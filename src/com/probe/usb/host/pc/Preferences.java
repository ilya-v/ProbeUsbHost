package com.probe.usb.host.pc;


public class Preferences {

    private final String PREF_OUTPUTDIR_NAME = "preference_outputdir";
    private final String PREF_LASTFILE_NAME = "preference_lastfile";
    private final String PREF_DATAFILEDIR = "preference_datafiledir";
    private final String PREF_CONFIGFILEDIR = "preference_configfiledir";

    public String getOutputDirectory() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ProbeGUI.class);
        String defaultValue = System.getProperty("user.home");
        return prefs.get(PREF_OUTPUTDIR_NAME, defaultValue);
    }

    public void saveOutputDirectory(String outputDirectory) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ProbeGUI.class);
        prefs.put(PREF_OUTPUTDIR_NAME, outputDirectory);
    }

    public String getLastFileName() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ProbeGUI.class);
        String defaultValue = System.getProperty("none");
        return prefs.get(PREF_LASTFILE_NAME, defaultValue);
    }

    public void saveLastFileName(String fileName) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ProbeGUI.class);
        prefs.put(PREF_LASTFILE_NAME, fileName);
    }

    public String getConfigFileDirectory() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ProbeGUI.class);
        String defaultValue = System.getProperty("user.home");
        return prefs.get(PREF_CONFIGFILEDIR, defaultValue);
    }

    public void saveConfigFileDirectory(String dir) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ProbeGUI.class);
        prefs.put(PREF_CONFIGFILEDIR, dir);
    }
}
