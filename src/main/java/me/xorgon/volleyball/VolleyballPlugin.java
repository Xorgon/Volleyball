package me.xorgon.volleyball;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.supaham.commons.bukkit.SimpleCommonPlugin;
import de.slikey.effectlib.EffectManager;
import me.xorgon.volleyball.commands.VolleyballCommand;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

public class VolleyballPlugin extends SimpleCommonPlugin<VolleyballPlugin> {

    private static VolleyballPlugin instance;
    private VManager manager;
    private static final String COMMAND_PREFIX = "vb";
    private EffectManager effectManager;
    private CommandsManager<CommandSender> commands;


    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        manager = new VManager();
        registerEvents(new VListener());
        setupCommands();
        effectManager = new EffectManager(this);
        effectManager.enableDebug(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        manager.getCourts().values().forEach(Court::resetCourt);
        manager.getCourtsConfig().save();
        manager.getMessagesConfig().save();
        effectManager.dispose();
    }

    public static VolleyballPlugin getInstance() {
        return instance;
    }

    public VManager getManager() {
        return manager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    public void setupCommands() {
        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender commandSender, String s) {
                return commandSender.hasPermission(s);
            }
        };

        CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);

        reg.register(VolleyballCommand.VolleyballRootCommand.class);

    }


}
