package dev.dejvokep.clickspersecond.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import dev.dejvokep.clickspersecond.ClicksPerSecond;
import dev.dejvokep.clickspersecond.UUIDFactory;
import dev.dejvokep.clickspersecond.handler.sampler.Sampler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class StatsCommand extends PluginCommand {

    public StatsCommand(ClicksPerSecond plugin, CommandManager<CommandSender> manager) {
        super(plugin);

        manager.command(manager.commandBuilder("cps", "clickspersecond").literal("stats").permission("cps.stats")
                .argument(StringArgument.single("id")).handler(context -> {
                    // Parse UUID
                    UUID uuid = UUIDFactory.fromArgument(context.get("id"));
                    if (uuid == null) {
                        send(context, MESSAGE_INVALID_NAME);
                        return;
                    }

                    // Sampler
                    Sampler sampler = plugin.getClickHandler().getSampler(uuid);
                    // Online
                    if (sampler != null) {
                        // Loading
                        if (sampler.getInfo().isLoading()) {
                            send(context, MESSAGE_REQUEST_PENDING);
                            return;
                        }

                        // Empty
                        if (sampler.getInfo().isEmpty()) {
                            send(context, MESSAGE_PREFIX + "stats.not-found");
                            return;
                        }

                        // Success
                        send(context, MESSAGE_PREFIX + "stats.message", message -> plugin.getPlaceholderReplacer().replace(sampler, message));
                        return;
                    }

                    // If not instant fetch and no permission
                    if (!plugin.getDataStorage().isInstantFetch() && !context.hasPermission("cps.stats.fetch")) {
                        send(context, MESSAGE_NO_PERMISSION);
                        return;
                    }

                    // Fetch
                    send(context, MESSAGE_REQUEST_SENT);
                    plugin.getDataStorage().fetchSingle(uuid).whenComplete((info, exception) -> Bukkit.getScheduler().runTask(plugin, () -> {
                        // If an error
                        if (info == null) {
                            send(context, MESSAGE_REQUEST_ERROR);
                            return;
                        }

                        // Empty
                        if (info.isEmpty()) {
                            send(context, MESSAGE_PREFIX + "stats.not-found");
                            return;
                        }

                        // Success
                        send(context, MESSAGE_PREFIX + "stats.message", message -> plugin.getPlaceholderReplacer().replace(info, message));
                    }));
                }).build());
    }

}