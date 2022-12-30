package net.countercraft.movecraft.repair.upgrader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;

public class WEUtils {
    public static final ClipboardFormat LEGACY_FORMAT = BuiltInClipboardFormat.MCEDIT_SCHEMATIC;
    public static final ClipboardFormat MODERN_FORMAT = BuiltInClipboardFormat.SPONGE_SCHEMATIC;

    @Nullable
    public static Clipboard loadSchematic(File file) throws IOException {
        ClipboardReader reader = LEGACY_FORMAT.getReader(new FileInputStream(file));
        return reader.read();
    }

    public static void saveSchematic(File file, Clipboard clipboard) throws IOException {
        ClipboardWriter writer = MODERN_FORMAT.getWriter(new FileOutputStream(file));
        writer.write(clipboard);
        writer.close();
    }
}
