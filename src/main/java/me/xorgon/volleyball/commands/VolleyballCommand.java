package me.xorgon.volleyball.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.supaham.commons.bukkit.utils.ChatUtils;
import de.slikey.effectlib.EffectManager;
import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.VolleyballPlugin;
import me.xorgon.volleyball.effects.RomanCandleEffect;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VolleyballCommand {

    public static class VolleyballRootCommand {

        @Command(aliases = {"volleyball", "vb"}, desc = "The root Volleyball command.")
        @NestedCommand(value = {VolleyballCommand.class, VBSetCommand.VBSetRootCommand.class})
        public static void volleyball(CommandContext args, CommandSender sender) {
        }
    }

    @Command(aliases = {"addcourt", "ac", "createcourt", "add"},
            desc = "Create a court to be set up.",
            usage = "<court name>",
            min = 1,
            max = 1)
    @CommandPermissions("vb.admin")
    public static void addCourt(CommandContext args, CommandSender sender) {
        VolleyballPlugin.getInstance().getManager().addCourt(args.getString(0).toLowerCase());
        sender.sendMessage(ChatColor.YELLOW + "Created court.");
    }

    @Command(aliases = {"removecourt", "remove"},
            desc = "Remove a court from the config.",
            usage = "<court name>",
            min = 1,
            max = 1)
    @CommandPermissions("vb.admin")
    public static void removeCourt(CommandContext args, CommandSender sender) {
        VolleyballPlugin.getInstance().getManager().removeCourt(args.getString(0).toLowerCase());
        sender.sendMessage(ChatColor.YELLOW + "Removed court.");
    }

    @Command(aliases = {"listcourts", "list"}, desc = "")
    @CommandPermissions("vb.admin")
    public static void listcourts(CommandContext args, CommandSender sender) {
        String message = ChatColor.YELLOW + "Courts: ";
        for (Court court : VolleyballPlugin.getInstance().getManager().getCourts().values()) {
            message = message.concat(court.getName() + ", ");
        }
        sender.sendMessage(message.substring(0, message.length() - 2));
    }

    @Command(aliases = {"help"}, desc = "Basic instructions on how to play volleyball.")
    @CommandPermissions("vb.user")
    public static void help(CommandContext args, CommandSender sender) {
        String helpMessage = VolleyballPlugin.getInstance().getManager().messages.getHelpMessage();
        if (!helpMessage.isEmpty()) {
            sender.sendMessage(helpMessage);
        }
    }

    @Command(aliases = {"join"}, desc = "Join the specified volleyball court.", usage = "<court name> ", min = 1, max = 1)
    @CommandPermissions("vb.tp")
    public static void join(CommandContext args, CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            VManager manager = VolleyballPlugin.getInstance().getManager();
            if (manager.getCourts().containsKey(args.getString(0))) {
                Court court = manager.getCourt(args.getString(0));
                int redSize = court.getRedPlayers().size();
                int blueSize = court.getBluePlayers().size();
                if (redSize < blueSize && redSize < court.getMaxTeamSize()) {
                    player.teleport(court.getCenter(Court.Team.RED));
                } else if (blueSize < court.getMaxTeamSize()) {
                    player.teleport(court.getCenter(Court.Team.BLUE));
                } else {
                    Vector redVec = court.getCenter(Court.Team.RED).toVector();
                    Vector blueVec = court.getCenter(Court.Team.BLUE).toVector();
                    Vector mid = redVec.midpoint(blueVec);
                    Vector across = redVec.clone().subtract(blueVec);
                    mid.add(new Vector(0, 1, 0).crossProduct(across.clone().multiply(1 / across.length())).multiply(across.length()));
                    String fullGameMessage = manager.messages.getFullGameMessage();
                    if (!fullGameMessage.isEmpty()) {
                        player.sendMessage(fullGameMessage);
                    }
                    player.teleport(mid.toLocation(court.getWorld()));
                }
            }
        }
    }

    @Command(aliases = {"start"},
            desc = "Start a match on specified court.",
            usage = "<court name>",
            max = 1)
    @CommandPermissions("vb.admin")
    public static void start(CommandContext args, CommandSender sender) {
        if (sender instanceof Player) {
            if (args.argsLength() == 0) {
                Court court = VolleyballPlugin.getInstance().getManager().getCourt((Player) sender);
                if (court != null) {
                    court.startGame(true);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not within a court and you have not specified a court.");
                }
            } else {
                VolleyballPlugin.getInstance().getManager().getCourt(args.getString(0)).startGame(true);
            }
        }
    }

    @Command(aliases = {"end"},
            desc = "End a match on specified court.",
            usage = "<court name>",
            max = 1)
    @CommandPermissions("vb.admin")
    public static void end(CommandContext args, CommandSender sender) {
        if (sender instanceof Player) {
            VManager manager = VolleyballPlugin.getInstance().getManager();
            if (args.argsLength() == 0) {
                Player player = (Player) sender;
                if (manager.isInCourt(player)) {
                    manager.getCourt(player).endGame();
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "You aren't in a court.");
                }
            } else {
                manager.getCourt(args.getString(0)).endGame();
            }

        }
    }

    @Command(aliases = {"spawn"}, desc = "Spawn a volleyball.")
    @CommandPermissions("vb.admin")
    public static void spawn(CommandContext args, CommandSender sender) {
        if (sender instanceof Player) {
            VManager manager = VolleyballPlugin.getInstance().getManager();
            Player player = (Player) sender;
            if (manager.getCourt(player) != null) {
                Location loc = player.getLocation().add(0, 1.25, 0);
                manager.getCourt(player).spawnBall(loc);
            } else {
                player.sendMessage(ChatColor.YELLOW + "You aren't in a court.");
            }
        }
    }

    @Command(aliases = {"testbound"}, desc = "Tells you if you're inside a court.")
    @CommandPermissions("vb.admin")
    public static void testBound(CommandContext args, CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            VManager manager = VolleyballPlugin.getInstance().getManager();
            if (manager.isInCourt(player)) {
                player.sendMessage(ChatColor.YELLOW + "You are in court " + ChatColor.LIGHT_PURPLE + manager.getCourt(player).getName());
            } else {
                player.sendMessage(ChatColor.YELLOW + "You are not in a court.");
            }
        }
    }

    @Command(aliases = {"romancandle", "rc"}, desc = "Launch a roman candle.")
    @CommandPermissions("vb.admin")
    public static void romanCandle(CommandContext args, CommandSender sender) {
        if (sender instanceof Player) {
            EffectManager effectManager = VolleyballPlugin.getInstance().getEffectManager();
            VManager manager = VolleyballPlugin.getInstance().getManager();
            Player player = (Player) sender;
            Color color;
            if (manager.isInCourt(player)) {
                color = manager.getCourt(player).getSide(player.getLocation()) == Court.Team.RED ? Color.RED : Color.BLUE;
            } else {
                color = Color.PURPLE;
            }
            double height;
            if (args.argsLength() > 0) {
                height = args.getDouble(0);
            } else {
                height = 3.;
            }
            RomanCandleEffect effect = new RomanCandleEffect(effectManager, player.getLocation(), color, height);
            effect.start();
        }
    }

    @Command(aliases = {"clear"}, desc = "Remove all volleyballs.")
    @CommandPermissions("vb.admin")
    public static void clear(CommandContext args, CommandSender sender) {
        VolleyballPlugin.getInstance().getManager().clearVolleyballs();
        sender.sendMessage(ChatColor.YELLOW + "Removed volleyballs.");
    }

    public static class VBSetCommand {

        public static class VBSetRootCommand {
            @Command(aliases = {"set"}, desc = "Define a setting for a court.")
            @CommandPermissions("vb.admin")
            @NestedCommand(value = {VBSetCommand.class})
            public static void set(CommandContext args, CommandSender sender) {
            }
        }

        @Command(aliases = {"red"}, desc = "Set the red side of the court.", usage = "<court name>", min = 1)
        public static void red(CommandContext args, CommandSender sender) {
            if (!(sender instanceof Player)) {
                return;
            }
            Player player = (Player) sender;
            VManager manager = VolleyballPlugin.getInstance().getManager();
            Court court = manager.getCourt(args.getString(0));
            if (court != null) {
                WorldEditPlugin worldedit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                LocalSession playerSession = worldedit.getSession(player);
                if (playerSession != null) {
                    Region selection = null;
                    try {
                        selection = playerSession.getSelection(playerSession.getSelectionWorld());
                    } catch (IncompleteRegionException e) {
                        player.sendMessage(ChatColor.RED + "Your selection is incomplete.");
                    }
                    if (selection != null) {
                        court.setRed(selection.getMinimumPoint(), selection.getMaximumPoint().add(1, 0, 1));
                        court.setWorld(player.getWorld());
                        player.sendMessage(ChatColor.YELLOW + "Red side set.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "WorldEdit could not find your session.");
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "That court does not exist.");
            }
        }

        @Command(aliases = {"blue"}, desc = "Set the blue side of the court.", usage = "<court name>", min = 1)
        public static void blue(CommandContext args, CommandSender sender) {
            if (!(sender instanceof Player)) {
                return;
            }
            Player player = (Player) sender;
            VManager manager = VolleyballPlugin.getInstance().getManager();
            Court court = manager.getCourt(args.getString(0));
            if (court != null) {
                WorldEditPlugin worldedit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                LocalSession playerSession = worldedit.getSession(player);
                if (playerSession != null) {
                    Region selection = null;
                    try {
                        selection = playerSession.getSelection(playerSession.getSelectionWorld());
                    } catch (IncompleteRegionException e) {
                        player.sendMessage(ChatColor.RED + "Your selection is incomplete.");
                    }
                    if (selection != null) {
                        court.setBlue(selection.getMinimumPoint(), selection.getMaximumPoint().add(1, 0, 1));
                        court.setWorld(player.getWorld());
                        player.sendMessage(ChatColor.YELLOW + "Blue side set.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "WorldEdit could not find your session.");
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "That court does not exist.");
            }
        }

        @Command(aliases = {"displayname", "dn"},
                desc = "Set the display name of the court,",
                usage = "<court name> <display name>",
                min = 1)
        public static void displayName(CommandContext args, CommandSender sender) {
            VManager manager = VolleyballPlugin.getInstance().getManager();
            if (manager.getCourt(args.getString(0)) != null) {
                if (args.argsLength() > 1) {
                    manager.getCourt(args.getString(0)).setDisplayName(args.getJoinedStrings(1));
                    sender.sendMessage(ChatColor.YELLOW + "Set court display name.");
                } else {
                    manager.getCourt(args.getString(0)).setDisplayName(null);
                    sender.sendMessage(ChatColor.YELLOW + "Unset court display name.");
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "That court doesn't exist.");
            }
        }

        @Command(aliases = {"ballsize"},
                desc = "Set the ball size for the court,",
                usage = "<court name> <ball size>",
                min = 1)
        public static void ballSize(CommandContext args, CommandSender sender) {
            VManager manager = VolleyballPlugin.getInstance().getManager();
            if (manager.getCourt(args.getString(0)) != null) {
                if (args.argsLength() > 1) {
                    manager.getCourt(args.getString(0)).setBallSize(args.getInteger(1));
                    sender.sendMessage(ChatColor.YELLOW + "Set ball size.");
                } else {
                    manager.getCourt(args.getString(0)).setBallSize(3);
                    sender.sendMessage(ChatColor.YELLOW + "Reset ball size to default (3).");
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "That court doesn't exist.");
            }
        }

        @Command(aliases = {"minteamsize"},
                desc = "Set the minimum team size for the court,",
                usage = "<court name> <team size>",
                min = 1)
        public static void minTeamSize(CommandContext args, CommandSender sender) {
            VManager manager = VolleyballPlugin.getInstance().getManager();
            if (manager.getCourt(args.getString(0)) != null) {
                if (args.argsLength() > 1) {
                    manager.getCourt(args.getString(0)).setMinTeamSize(args.getInteger(1));
                    sender.sendMessage(ChatColor.YELLOW + "Set minimum team size.");
                } else {
                    manager.getCourt(args.getString(0)).setMinTeamSize(1);
                    sender.sendMessage(ChatColor.YELLOW + "Reset minimum team size to default (1).");
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "That court doesn't exist.");
            }
        }

        @Command(aliases = {"maxteamsize"},
                desc = "Set the maximum team size for the court,",
                usage = "<court name> <team size>",
                min = 1)
        public static void maxTeamSize(CommandContext args, CommandSender sender) {
            VManager manager = VolleyballPlugin.getInstance().getManager();
            if (manager.getCourt(args.getString(0)) != null) {
                if (args.argsLength() > 1) {
                    manager.getCourt(args.getString(0)).setMaxTeamSize(args.getInteger(1));
                    sender.sendMessage(ChatColor.YELLOW + "Set maximum team size.");
                } else {
                    manager.getCourt(args.getString(0)).setMaxTeamSize(6);
                    sender.sendMessage(ChatColor.YELLOW + "Reset maximum team size to default (6).");
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "That court doesn't exist.");
            }
        }

    }

}
