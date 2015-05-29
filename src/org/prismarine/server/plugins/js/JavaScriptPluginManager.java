package org.prismarine.server.plugins.js;

import org.prismarine.server.util.Logger;
import org.prismarine.api.plugin.JavascriptPluginBase;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class JavaScriptPluginManager {

    private File jsPluginDir;
    private ArrayList<JavascriptPlugin> jsPlugins;
    private ScriptEngineManager factory;

    public JavaScriptPluginManager(File pluginsDir){
        Logger.write("Loading plugins...");
        this.jsPluginDir = new File("./plugins/javascript");
        this.factory = new ScriptEngineManager();
        //put them all in an awesome list
        this.jsPlugins = new ArrayList<JavascriptPlugin>();

        try {
            if(!this.jsPluginDir.exists()){
                this.jsPluginDir.mkdirs();
            }
            //list all files in directory
            final File[] jsPlugins = jsPluginDir.listFiles();

            if (jsPlugins != null) {
                for (final File f : jsPlugins) {
                    final String name = f.getName(); //TODO: initialize server var or json in zip
                    final FileReader reader = new FileReader(f);
                    this.addPlugin(reader, name);
                    reader.close();
                }
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void addPlugin(FileReader pluginFile, String name){
        String extension = "js";
        ScriptEngine engine = factory.getEngineByExtension(extension);

        try {
            //create a new plugin
            JavascriptPlugin plugin = new JavascriptPlugin(pluginFile, name);

            //eval the plugin js
            engine.eval(pluginFile);

            //set the default javascript PluginBase to the script, and require it to use it that way.
            JavascriptPluginBase scriptPlugin = ((Invocable) engine).getInterface(JavascriptPluginBase.class);
            if (scriptPlugin == null) {
                Logger.error("No method onEnable() and/or onDisable() found for plugin " + name);
            } else {
                try {
                    //run the onEnable script
                    scriptPlugin.onEnable();
                } catch (final Throwable t) {
                    Logger.error("Plugin Initialization Error " + t.getMessage());
                }
            }

            //add it to the ArrayList
            jsPlugins.add(plugin);
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            Logger.write("[PluginManager] Loaded: " + name + "!");
        }
    }

    public File getPluginDir(){
        return jsPluginDir;
    }

    public int getJsPluginCount(){
        return jsPlugins.size();
    }

}