package me.xorgon.volleyball.util;

import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class ConfigUtil {

    public static Object serializeVector(Vector vector) {
        return vector.toString();
    }

    public static Vector deserializeVector(Object serialized) {
        String[] split = ((String) serialized).split(",");
        return new Vector(Double.valueOf(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]));
    }

    public static Object serializeLocation(Location location, boolean storeWorld) {
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch() +
                (storeWorld ? location.getWorld().getName() : "");
    }

    public static Location deserializeLocation(Object serialized, boolean storeWorld) {
        String[] split = ((String) serialized).split(",");
        double x = Double.valueOf(split[0]);
        double y = Double.valueOf(split[1]);
        double z = Double.valueOf(split[2]);
        float yaw = Float.parseFloat(split[3]);
        float pitch = Float.parseFloat(split[4]);
        if (storeWorld) {
            World world = Bukkit.getWorld(split[5]);
            Validate.notNull(world, "'" + split[5] + "' is not a valid world.");
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            return new Location(null, x, y, z, yaw, pitch);
        }
    }

    public static Object serializeMaterialData(MaterialData materialData) {
        return materialData.getItemType() + ":" + materialData.getData();
    }

    public static MaterialData deserializeMaterialData(Object serialized) {
        String[] split = ((String) serialized).split(":");
        return new MaterialData(Material.getMaterial(split[0]), Byte.valueOf(split[1]));
    }
}
