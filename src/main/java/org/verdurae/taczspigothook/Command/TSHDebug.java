package org.verdurae.taczspigothook.Command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.verdurae.taczspigothook.TacZSpigotHook;

/**
 * @author Kaminy
 * @date 2026/6/8 19:02
 * @since 1.0.1
 */
public class TSHDebug implements Command<CommandSourceStack> {
    public static final TSHDebug INSTANCE = new TSHDebug();

    @Override
    public int run(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        TacZSpigotHook.debug = !TacZSpigotHook.debug;

        CommandSourceStack source = commandContext.getSource();
        Component message;

        if (TacZSpigotHook.debug) {
            message = Component.translatable("command.taczspigothook.debug.enabled");
        } else {
            message = Component.translatable("command.taczspigothook.debug.disabled");
        }

        source.sendSystemMessage(Component.literal(message.getString()));
        TacZSpigotHook.LOGGER.info(message.getString());
        return 1;
    }
}