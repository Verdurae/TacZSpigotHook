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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.slf4j.Logger;
import org.verdurae.taczspigothook.Command.TSHDebug;
import org.verdurae.taczspigothook.Utils.NmsUtil;

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
        NmsUtil.init();
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
    public void onEntityHurtByGun(EntityHurtByGunEvent.Pre event) {
        double damage = event.getAmount();
        Entity NmsEntity = event.getHurtEntity();
        LivingEntity NmsAttacker = event.getAttacker();
        Object craftServer = Bukkit.getServer();
        Object craftEntity = NmsUtil.getCraftEntity(craftServer, NmsEntity);
        Object craftAttacker = NmsUtil.getCraftEntity(craftServer, NmsAttacker);
        if (craftEntity != null && craftAttacker != null &&
                NmsUtil.isCraftLivingEntity(craftEntity) &&
                NmsUtil.isCraftLivingEntity(craftAttacker)) {
            UUID uuid = UUID.randomUUID();
            if (debug) {
                LOGGER.warn("------------------------------------------------");
                LOGGER.warn(Component.literal("§c正在将TacZ事件链接到Spigot").getString());
                LOGGER.warn("HandleUUID:{}", uuid);
                LOGGER.warn("Damage:{}", damage);
                LOGGER.warn("Attacker:{}", craftAttacker);
                LOGGER.warn("Victim:{}", craftEntity);
                LOGGER.warn("headshot:{}", event.isHeadShot());
                LOGGER.warn("------------------------------------------------");
            }
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) craftEntity;
            org.bukkit.entity.LivingEntity livingAttacker = (org.bukkit.entity.LivingEntity) craftAttacker;
            EntityDamageByEntityEvent bukkitEvent = new EntityDamageByEntityEvent(livingAttacker, livingEntity, EntityDamageByEntityEvent.DamageCause.PROJECTILE, damage);
            if (debug) {
                LOGGER.warn("Handle {} " + Component.literal("§c抛出事件").getString(), uuid);
            }
            Bukkit.getPluginManager().callEvent(bukkitEvent);
            if (bukkitEvent.isCancelled()) {
                if (debug) {
                    LOGGER.warn("Handle {} " + Component.literal("§c被Spigot的插件取消").getString(), uuid);
                }
                event.setCanceled(true);
            } else {
                if (debug) {
                    LOGGER.warn("Handle {} " + Component.literal("§c成功抛出").getString(), uuid);
                    LOGGER.warn("HandledDamage:{}", bukkitEvent.getDamage());
                    LOGGER.warn("headshot:{}", event.isHeadShot());
                }
                event.setBaseAmount((float) bukkitEvent.getDamage());
            }
        }
    }
}

