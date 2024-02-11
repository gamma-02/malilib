package fi.dy.masa.malilib.network.payload;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.network.handler.ClientConfigHandler;
import fi.dy.masa.malilib.network.handler.ClientPlayHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This is made to "manage" the payload types and do the actual channel registrations via the Fabric Network API (4.0.0+)
 * From here, we Map the payload CODEC and TYPE into a HashMap; for our own reference by the Payloads based on their PacketType.
 * This was done in an attempt to make the remaining functions more abstract.
 */
public class PayloadTypeRegister
{
    public static final PayloadTypeRegister INSTANCE = new PayloadTypeRegister();
    public static PayloadTypeRegister getInstance() { return INSTANCE; }
    private final Map<PayloadType, PayloadCodec> TYPES = new HashMap<>();

    public PayloadTypeRegister()
    {
        initPayloads();
    }
    @Nullable
    public PayloadCodec register(PayloadType type, String key, String namespace, String path)
    {
        if (!TYPES.containsKey(type))
        {
            PayloadCodec codec = new PayloadCodec(type, key, namespace, path);
            TYPES.put(type, codec);
            MaLiLib.printDebug("PayloadTypeRegister#register(): registering a new PayloadCodec id: {} // {}:{}", codec.getId().hashCode(), codec.getId().getNamespace(), codec.getId().getPath());

            MaLiLib.printDebug("PayloadTypeRegister#register(): VERIFY containsKey(): {}", TYPES.containsKey(type));
            return codec;
        }
        else
        {
            MaLiLib.printDebug("PayloadTypeRegister#register(): type {} already exists.", type.toString());
            return null;
        }
    }
    @Nullable
    public PayloadCodec register(IPayloadType payloadType)
    {
        // Maybe this works as long as the "PayloadType" exists?
        PayloadType type = payloadType.getType();

        // Basic check for sanity
        if (!type.exists(type))
        {
            MaLiLib.printDebug("PayloadTypeRegister#register(): unhandled type {} given.", type.toString());
            return null;
        }
        if (!TYPES.containsKey(type))
        {
            PayloadCodec codec = new PayloadCodec(type, payloadType.getKey(), payloadType.getNamespace(), payloadType.getPath());
            TYPES.put(type, codec);
            MaLiLib.printDebug("PayloadTypeRegister#register(): registered a new PayloadCodec id: {} // {}:{}", codec.getId().hashCode(), codec.getId().getNamespace(), codec.getId().getPath());

            return codec;
        }
        else
        {
            MaLiLib.printDebug("PayloadTypeRegister#register(): type {} already exists.", type.toString());
            return null;
        }
    }

    public <T extends CustomPayload> void registerPlayChannel(PayloadType type, CustomPayload.Id<T> id, PacketCodec<PacketByteBuf, T> packetCodec)
    {
        PayloadCodec codec = getPayloadCodec(type);

        // Never Attempt to "re-register" a channel.  Bad things will happen.
        if (codec == null || codec.isPlayRegistered())
            return;
        codec.registerPlayCodec();

        MaLiLib.printDebug("PayloadTypeRegister#registerPlayChannel(): registering Play C2S Channel: {}", id.id().toString());
        PayloadTypeRegistry.playC2S().register(id, packetCodec);
        PayloadTypeRegistry.playS2C().register(id, packetCodec);
        // TODO Do we need to register the S2C channel also or only on the server?
    }

    public <T extends CustomPayload> void registerConfigChannel(PayloadType type, CustomPayload.Id<T> id, PacketCodec<PacketByteBuf, T> packetCodec)
    {
        PayloadCodec codec = getPayloadCodec(type);

        // Never Attempt to "re-register" a channel.  Bad things will happen.
        if (codec == null || codec.isConfigRegistered())
            return;
        codec.registerConfigCodec();

        MaLiLib.printDebug("PayloadTypeRegister#registerConfigChannel(): registering Configuration C2S Channel: {}", id.id().toString());
        PayloadTypeRegistry.configurationC2S().register(id, packetCodec);
        PayloadTypeRegistry.configurationS2C().register(id, packetCodec);
        // TODO Do we need to register the S2C channel also or only on the server?
    }
    @Nullable
    public PayloadCodec getPayloadCodec(PayloadType type)
    {
        //MaLiLib.printDebug("PayloadTypeRegister#getPayloadCodec(): type: {}", type.toString());
        return TYPES.getOrDefault(type, null);
    }
    @Nullable
    public Identifier getIdentifier(PayloadType type)
    {
        //MaLiLib.printDebug("PayloadTypeRegister#getIdentifier(): type: {}", type.toString());
        return TYPES.getOrDefault(type, null).getId();
    }

    /**
     * The Payload "KEY" field is simply for declaring any special "default" key Values for data if none are known
     */
    @Nullable
    public String getKey(PayloadType type)
    {
        //MaLiLib.printDebug("PayloadTypeRegister#getKey(): type: {}", type.toString());
        return TYPES.getOrDefault(type, null).getKey();
    }
    public void initPayloads()
    {
        MaLiLib.printDebug("PayloadTypeRegister#initPayloads(): invoked.");

        // TODO Register the play/config channel codec for every existing PayLoad or CRASH :)
        register(PayloadType.CARPET_HELLO,      "carpet_hello",             "carpet",   "hello");
        register(PayloadType.MALILIB_BYTE_BUF,  "malilib_bytebuf",          "malilib",  "bytebuf");
        register(PayloadType.SERVUX_LITEMATICS, "litematic_shared_storage", "servux",   "litematics");
        register(PayloadType.SERVUX_METADATA,   "metadata_service",         "servux",   "metadata");
        register(PayloadType.SERVUX_STRUCTURES, "structure_bounding_boxes", "servux",   "structures");

        listTypes();
    }
    public void resetPayloads()
    {
        MaLiLib.printDebug("PayloadTypeRegister#resetPayloads(): sending reset() to all registered Payload types.");
        for (PayloadType type : TYPES.keySet())
        {
            ((ClientPlayHandler<?>) ClientPlayHandler.getInstance()).reset(type);
        }
    }
    public void registerAllHandlers()
    {
        MaLiLib.printDebug("PayloadTypeRegister#registerAllHandlers(): sending registerHandlers() to all registered Payload types.");
        for (PayloadType type : TYPES.keySet())
        {
            if (TYPES.get(type).isPlayRegistered())
                ((ClientPlayHandler<?>) ClientPlayHandler.getInstance()).registerPlayHandler(type);
            if (TYPES.get(type).isConfigRegistered())
                ((ClientConfigHandler<?>) ClientPlayHandler.getInstance()).registerConfigHandler(type);
        }
    }
    // For Debugging only
    public void listTypes()
    {
        for (PayloadCodec codec : TYPES.values())
        {
            MaLiLib.printDebug("listTypes(): type {} // {}", codec.getType().toString(), codec.getId().toString());
        }
    }
}
