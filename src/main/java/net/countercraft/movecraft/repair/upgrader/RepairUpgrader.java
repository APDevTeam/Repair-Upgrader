package net.countercraft.movecraft.repair.upgrader;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public final class RepairUpgrader extends JavaPlugin {
    private static RepairUpgrader instance;

    public static RepairUpgrader getInstance() {
        return instance;
    }

    private WorldEditPlugin worldEditPlugin = null;

    @Override
    public void onEnable() {
        instance = this;

        Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (!(plugin instanceof WorldEditPlugin)) {
            getLogger().severe(
                    "Movecraft-Repair did not find a compatible version of WorldEdit. Disabling WorldEdit integration.");
            return;
        }
        getLogger().info("Found a compatible version of WorldEdit. Enabling WorldEdit integration.");
        worldEditPlugin = (WorldEditPlugin) plugin;

        // Run bootstrap task, this will handle setting up everything else
        new BootstrapTask(this);
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }
}
