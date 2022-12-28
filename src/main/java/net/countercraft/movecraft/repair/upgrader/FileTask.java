package net.countercraft.movecraft.repair.upgrader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.extent.clipboard.Clipboard;

import net.countercraft.movecraft.repair.upgrader.util.WEUtils;

public class FileTask implements Task {
    private final Plugin plugin;
    private final File oldState;
    private final File newState;

    public FileTask(Plugin plugin, File oldState, File newState) {
        this.plugin = plugin;
        this.oldState = oldState;
        this.newState = newState;
    }

    @Override
    public void run() {
        Clipboard clipboard;
        try {
            clipboard = WEUtils.loadSchematic(oldState);
        } catch (IOException e) {
            handle(e, "load", oldState);
            return;
        }

        try {
            WEUtils.saveSchematic(newState, clipboard);
        } catch (IOException e) {
            handle(e, "save", newState);
        }
    }

    private void handle(IOException e, String type, File file) {
        plugin.getLogger().severe(() -> "Failed to " + type + " " + file.getParentFile().getName() + "/" + file.getName());
        plugin.getLogger().warning(() -> getStackTrace(e));
    }

    private String getStackTrace(IOException e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @Override
    public boolean isDone() {
        return true;
    }
}
