package org.verdurae.taczspigothook.Utils;

import java.lang.reflect.Method;

/**
 * @author Kaminy
 * @date 2026/6/8 21:30
 * @since 1.0.2
 */
public class NmsUtil {
    public static Class<?> CraftEntity;
    public static Class<?> CraftLivingEntity;
    public static Class<?> CraftServer;

    private static Method getEntityMethod;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        try {
            CraftEntity = Class.forName("org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity");
            CraftLivingEntity = Class.forName("org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity");
            CraftServer = Class.forName("org.bukkit.craftbukkit.v1_20_R1.CraftServer");

            getEntityMethod = CraftEntity.getMethod("getEntity",
                    CraftServer,
                    net.minecraft.world.entity.Entity.class);

            initialized = true;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find CraftBukkit classes. Make sure you're running on a Paper/Spigot server.", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find getEntity method in CraftEntity", e);
        }
    }

    public static Object getCraftEntity(Object craftServer, net.minecraft.world.entity.Entity nmsEntity) {
        if (!initialized) {
            init();
        }

        try {
            return getEntityMethod.invoke(null, craftServer, nmsEntity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get CraftEntity from NMS Entity", e);
        }
    }

    public static boolean isCraftLivingEntity(Object entity) {
        if (!initialized) {
            init();
        }
        return CraftLivingEntity.isInstance(entity);
    }
}