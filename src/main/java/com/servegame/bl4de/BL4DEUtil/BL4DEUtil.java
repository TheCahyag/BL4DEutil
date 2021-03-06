package com.servegame.bl4de.BL4DEUtil;

import com.google.inject.Inject;
import com.servegame.bl4de.BL4DEUtil.modules.lastonline.commands.LastOnline;
import com.servegame.bl4de.BL4DEUtil.modules.blade.commands.Blade;
import com.servegame.bl4de.BL4DEUtil.modules.blade.commands.BladeHelp;
import com.servegame.bl4de.BL4DEUtil.modules.blade.commands.BladeToggleDebug;
import com.servegame.bl4de.BL4DEUtil.modules.gamemode.commands.GMC;
import com.servegame.bl4de.BL4DEUtil.modules.gamemode.commands.GMS;
import com.servegame.bl4de.BL4DEUtil.modules.getclw.commands.GetCLW;
import com.servegame.bl4de.BL4DEUtil.modules.getclw.commands.GetCLWConfirm;
import com.servegame.bl4de.BL4DEUtil.modules.ranks.commands.LabRat;
import com.servegame.bl4de.BL4DEUtil.modules.ranks.commands.Ranks;
import com.servegame.bl4de.BL4DEUtil.modules.ranks.commands.Scientist;
import com.servegame.bl4de.BL4DEUtil.modules.ranks.commands.Technician;
import com.servegame.bl4de.BL4DEUtil.util.BL4DEListenerHandler;
import com.servegame.bl4de.BL4DEUtil.modules.getclw.fileparser.CLWLimitFileParser;
import com.servegame.bl4de.BL4DEUtil.modules.lastonline.fileparser.LastOnlineFileParser;
import com.servegame.bl4de.BL4DEUtil.util.Permissions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * File: BL4DEUtil.java
 * @author Brandon Bires-Navel (brandonnavel@outlook.com)
 */
@Plugin(id = "bl4deutil", name = "BL4DEUtil", version = "0.0.1",
    authors = {"TheCahyag"},
    url = "https://github.com/TheCahyag/BL4DEUtil")
public class BL4DEUtil {

    private BL4DEListenerHandler eventHandler;
    private SqlService sql;

    private final String configDir = "./config/bl4deutil";
    private final String recentPlayersDataDir = configDir + "/recent_player_logins.dat";
    private final String CLWCounterDataDir = configDir + "/clw_count.dat";

    @Inject private Game game;
    @Inject private Logger logger;
    public static boolean debug = false;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path privateConfigDir;

    public Logger getLogger(){
        return this.logger;
    }

    public String getCLWCounterDataDir() {
        return this.CLWCounterDataDir;
    }

    public String getRecentPlayersDataDir(){
        return this.recentPlayersDataDir;
    }

    public Game getGame() {
        return game;
    }

    public SqlService getSql() {
        return sql;
    }

    @Listener
    public void onLoad(GameLoadCompleteEvent event){
        this.logger.info("BL4DEUtil has loaded.");
    }

    @Listener
    public void onInit(GameInitializationEvent event){
        // Check for config directory and do file stuff
        if (!new File(configDir).exists()){
            //noinspection ResultOfMethodCallIgnored
            new File(configDir).mkdir();
        }
        if (!new File(recentPlayersDataDir).exists()){
            try {
                //noinspection ResultOfMethodCallIgnored
                new File(recentPlayersDataDir).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                this.logger.info("Could not create/load player_info.dat.");
                this.logger.info("Disabling plugin.");
                return;
            }
        }
        if (!new File(CLWCounterDataDir).exists()){
            try {
                //noinspection ResultOfMethodCallIgnored
                new File(CLWCounterDataDir).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                this.logger.info("Could not create/load clw_count.dat.");
                this.logger.info("Disabling plugin.");
                return;
            }
        }

        /* Command Register START */

        // Register /GetCLW
        // /getclw confirm
        CommandSpec getCLWConfirm = CommandSpec.builder()
                .description(Text.of("Get a Chunk Loading Ward in exchange for 3 emeralds."))
                .permission(Permissions.COMMAND_GETCLW_CONFIRM)
                .executor(new GetCLWConfirm(this))
                .build();
        // /getclw
        CommandSpec getCLW = CommandSpec.builder()
                .description(Text.of("Show instructions to get Chunk Loading Ward"))
                .permission(Permissions.COMMAND_GETCLW)
                .child(getCLWConfirm, "confirm", "c")
                .executor(new GetCLW())
                .build();
        this.game.getCommandManager().register(this, getCLW, "getclw", "getchunkloadingward");
        this.logger.info("/GetCLW registered");

        // Register /blade
        // /blade debug
        CommandSpec bladeDebug = CommandSpec.builder()
                .description(Text.of("Toggles the debug mode for BL4DEUtil"))
                .permission(Permissions.COMMAND_BLADE_DEBUG)
                .executor(new BladeToggleDebug())
                .build();
        // /blade help
        CommandSpec bladeHelp = CommandSpec.builder()
                .description(Text.of("View commands provide with BL4DEUtil"))
                .permission(Permissions.COMMAND_BLADE_HELP)
                .executor(new BladeHelp())
                .build();
        // /blade
        CommandSpec blade = CommandSpec.builder()
                .description(Text.of("Information regarding the BL4DEUtil plugin"))
                .permission(Permissions.COMMAND_BLADE)
                .child(bladeHelp, "help", "?", "commands")
                .child(bladeDebug, "debug", "d")
                .executor(new Blade())
                .build();
        this.game.getCommandManager().register(this, blade, "blade", "bl4de");
        this.logger.info("/blade registered");

        // Register /ranks
        // /ranks LabRat
        CommandSpec labRat = CommandSpec.builder()
                .description(Text.of("Show information about the LabRat rank"))
                .permission(Permissions.COMMAND_RANK)
                .executor(new LabRat())
                .build();

        // /ranks Technician
        CommandSpec technician = CommandSpec.builder()
                .description(Text.of("Show information about the Technician rank"))
                .permission(Permissions.COMMAND_RANK)
                .executor(new Technician())
                .build();

        // /ranks Scientist
        CommandSpec scientist = CommandSpec.builder()
                .description(Text.of("Show information about the Scientist rank"))
                .permission(Permissions.COMMAND_RANK)
                .executor(new Scientist())
                .build();
        // /ranks
        CommandSpec ranks = CommandSpec.builder()
                .description(Text.of("View current ranks of the server and how they can be achieved."))
                .permission(Permissions.COMMAND_RANK)
                .child(labRat, "labrat")
                .child(technician, "technician")
                .child(scientist, "scientist")
                .executor(new Ranks())
                .build();
        this.game.getCommandManager().register(this, ranks, "ranks", "rank");
        this.logger.info("/ranks registered");

        // Register /GMC
        CommandSpec gmc = CommandSpec.builder()
                .description(Text.of("Sets the gamemode of the player to creative."))
                .permission(Permissions.COMMAND_GMC)
                .executor(new GMC())
                .build();
        this.game.getCommandManager().register(this, gmc, "gmc");
        this.logger.info("/GMC registered");

        // Register /GMS
        CommandSpec gms = CommandSpec.builder()
                .description(Text.of("Sets the gamemode of the player to survival."))
                .permission(Permissions.COMMAND_GMS)
                .executor(new GMS())
                .build();
        this.game.getCommandManager().register(this, gms, "gms");
        this.logger.info("/GMS registered");

        // Register /LastOnline
        CommandSpec lastOnline = CommandSpec.builder()
                .description(Text.of("Show the last 10 players who have logged on."))
                .permission(Permissions.COMMAND_LASTONLINE)
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("player"))))
                .executor(new LastOnline())
                .build();
        this.game.getCommandManager().register(this, lastOnline, "lastonline", "lo");
        this.logger.info("/LastOnline registered");
        /* Command Register END */

        // EventHandler
        this.eventHandler = new BL4DEListenerHandler(this);
        // Give the file parse plugin objects for logger and such
        new LastOnlineFileParser(this);
        new CLWLimitFileParser(this);
    }

    @Listener
    public void onChangeBlockPlaceEvent(ChangeBlockEvent.Place event, @Root Player player){
        this.eventHandler.handleEvent(event);
    }

    @Listener
    public void onClientConnectionEvent(ClientConnectionEvent.Login event){
        this.eventHandler.handleEvent(event);
    }
}
