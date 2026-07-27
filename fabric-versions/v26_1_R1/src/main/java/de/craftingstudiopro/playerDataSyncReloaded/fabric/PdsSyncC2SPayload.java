package de.craftingstudiopro.playerDataSyncReloaded.fabric;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Client-to-server sync control (Fabric 1.21+ {@link CustomPayload} API).
 */
public record PdsSyncC2SPayload(String message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PdsSyncC2SPayload> PACKET_ID = createPacketId();
    
    @SuppressWarnings("unchecked")
    private static CustomPacketPayload.Type<PdsSyncC2SPayload> createPacketId() {
        try {
            Class<?> idClass;
            try {
                idClass = Class.forName("net.minecraft.resources.Identifier");
            } catch (ClassNotFoundException e) {
                idClass = Class.forName("net.minecraft.resources.ResourceLocation");
            }
            Method fromNamespaceAndPath = idClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            Object id = fromNamespaceAndPath.invoke(null, "pds", "sync");
            Constructor<CustomPacketPayload.Type> constructor = CustomPacketPayload.Type.class.getConstructor(idClass);
            return constructor.newInstance(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create packet ID", e);
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PdsSyncC2SPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PdsSyncC2SPayload::message, PdsSyncC2SPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
