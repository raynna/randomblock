package com.raynna.blockroulette.config;

import com.google.gson.JsonObject;
import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.raynna.blockroulette.BlockRoulette;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RaynnaServerConfig extends AutoInitConfigContainer {

    public static final RaynnaServerConfig INSTANCE = new RaynnaServerConfig();
    public static final int CURRENT_VERSION = 1;
    public static final String PATH = "./config/raynna/common";

    String VERSION_KEY = "version";

    public ServerConfig server = new ServerConfig();

    public static ServerConfig getServerConfig() {
        return INSTANCE.server;
    }


    public RaynnaServerConfig() {
        super(ResourceLocation.fromNamespaceAndPath(BlockRoulette.MOD_ID, "common"), "screen.raynna.common.title", PATH + ".json");
    }

    @Override
    protected boolean shouldLoad(JsonObject obj) {
        if (!obj.has(VERSION_KEY)) return true;
        int version = obj.get(VERSION_KEY).getAsInt();
        if (version != CURRENT_VERSION) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                FileUtils.copyFile(new File(this.path), new File(PATH + "-"+ sdf.format(new Date()) + ".json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BlockRoulette.LOGGER.info("Wrong server config version {} for mod {}! Automatically use version {} and backup old one.", version, BlockRoulette.MOD_NAME, CURRENT_VERSION);
            return false;
        } else BlockRoulette.LOGGER.info("{} server config version match.", BlockRoulette.MOD_NAME);
        return true;
    }

    @Override
    protected void writeCustomData(JsonObject obj) {
        obj.addProperty("version", CURRENT_VERSION);
    }
}