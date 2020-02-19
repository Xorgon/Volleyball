package me.xorgon.volleyball.util;

import com.supaham.commons.bukkit.title.Title;
import net.kyori.text.TextComponent;
import org.bukkit.entity.Player;

/**
 * Created by Elijah on 13/09/2016.
 */
public class TitleUtil {

    public static void sendTitle(Player player, String title, String subtitle) {
        Title.sendTimes(player, 0, 30, 10);
        Title.sendSubtitle(player, TextComponent.of(title), TextComponent.of(subtitle));
    }

    public static void sendTitle(Player player, String title, String subtitle, boolean overrideTimes) {
        if (!overrideTimes) {
            Title.sendTimes(player, 0, 30, 10);
        }
        Title.sendSubtitle(player, TextComponent.of(title), TextComponent.of(subtitle));
    }

    public static void sendTitle(Player player, String title) {
        Title.sendTimes(player, 0, 30, 10);
        Title.sendTitle(player, TextComponent.of(title));
    }

    public static void sendTitle(Player player, String title, boolean overrideTimes) {
        if (!overrideTimes) {
            Title.sendTimes(player, 0, 30, 10);
        }
        Title.sendTitle(player, TextComponent.of(title));
    }

}

