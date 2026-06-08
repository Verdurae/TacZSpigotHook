package org.verdurae.taczspigothook;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.slf4j.Logger;
import org.verdurae.taczspigothook.Command.TSHDebug;

import java.util.UUID;

@Mod(TacZSpigotHook.MODID)
public class TacZSpigotHook {
    public static final String MODID = "taczspigothook";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean debug = false;

    public TacZSpigotHook() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("tsh")
                        .then(
                                Commands.literal("debug")
                                        .requires((commandSource) -> commandSource.hasPermission(2))
                                        .executes(TSHDebug.INSTANCE)
                        )
        );
    }

    @SubscribeEvent
    public void onTacZShoot(EntityHurtByGunEvent.Pre event) {
        double damage = event.getAmount();
        Entity NmsEntity = event.getHurtEntity();
        LivingEntity NmsAttacker = event.getAttacker();
        CraftEntity craftEntity = CraftEntity.getEntity((CraftServer) Bukkit.getServer(), NmsEntity);
        CraftEntity craftAttacker = CraftEntity.getEntity((CraftServer) Bukkit.getServer(), NmsAttacker);
        if (craftEntity instanceof CraftLivingEntity craftLivingEntity && craftAttacker instanceof CraftLivingEntity craftLivingAttacker) {
            UUID uuid = UUID.randomUUID();
            Component message;
            if (TacZSpigotHook.debug) {
                LOGGER.warn("------------------------------------------------");
                message = Component.translatable("debug.taczspigothook.handle.init");
                LOGGER.warn(message.getString());
                LOGGER.warn("HandleUUID:{}", uuid);
                LOGGER.warn("Damage:{}", damage);
                LOGGER.warn("Attacker:{}", craftLivingAttacker);
                LOGGER.warn("Victim:{}", craftLivingEntity);
                LOGGER.warn("headshot:{}", event.isHeadShot());
                LOGGER.warn("------------------------------------------------");
            }
            org.bukkit.entity.LivingEntity livingEntity = craftLivingEntity;
            org.bukkit.entity.LivingEntity livingAttacker = craftLivingAttacker;
            EntityDamageByEntityEvent bukkitEvent = new EntityDamageByEntityEvent(livingAttacker, livingEntity, EntityDamageByEntityEvent.DamageCause.PROJECTILE, damage);
            if (TacZSpigotHook.debug) {
                message = Component.translatable("debug.taczspigothook.handle.call");
                LOGGER.warn("Handle{}" + message.getString(), uuid);
            }
            Bukkit.getPluginManager().callEvent(bukkitEvent);
            if (bukkitEvent.isCancelled()) {
                if (TacZSpigotHook.debug) {
                    message = Component.translatable("debug.taczspigothook.handle.cancel");
                    LOGGER.warn("Handle{}" + message.getString(), uuid);
                }
                event.setCanceled(true);
            } else {
                if (TacZSpigotHook.debug) {
                    message = Component.translatable("debug.taczspigothook.handle.success");
                    LOGGER.warn("Handle{}" + message.getString(), uuid);
                    LOGGER.warn("HandledDamage:{}", bukkitEvent.getDamage());
                    LOGGER.warn("headshot:{}", event.isHeadShot());
                }
                event.setBaseAmount((float) bukkitEvent.getDamage());
            }
        }
    }
}
