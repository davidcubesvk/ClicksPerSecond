package dev.dejvokep.clickspersecond.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import dev.dejvokep.clickspersecond.ClicksPerSecond;
import dev.dejvokep.clickspersecond.UUIDFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class DeleteCommand extends PluginCommand {

    public DeleteCommand(ClicksPerSecond plugin, CommandManager<CommandSender> manager) {
        super(plugin);

        // Register
        manager.command(manager.commandBuilder("cps", "clickspersecond").literal("delete").permission("cps.delete")
                .argument(StringArgument.single("target")).handler(context -> {
            // The target
            String target = context.get("target");

            // Delete all
            if (target.equals("*") || target.equals("all")) {
                send(context, MESSAGE_REQUEST_SENT);
                plugin.getDataStorage().deleteAll().whenComplete((result, exception) -> handleResult(result, context));
                return;
            }

            // Parse UUID
            UUID uuid = UUIDFactory.fromArgument(target);
            if (uuid == null) {
                send(context, MESSAGE_INVALID_NAME);
                return;
            }

            // Delete single
            plugin.getDataStorage().delete(uuid).whenComplete((result, exception) -> handleResult(result, context));
        }).build());
    }

    private void handleResult(boolean result, CommandContext<CommandSender> context) {
        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            // If an error
            if (!result) {
                send(context, MESSAGE_REQUEST_ERROR);
                return;
            }

            // Success
            send(context, MESSAGE_PREFIX + "delete");
        });
    }

}