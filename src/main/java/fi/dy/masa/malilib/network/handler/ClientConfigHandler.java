package fi.dy.masa.malilib.network.handler;

import com.google.common.collect.ArrayListMultimap;
import fi.dy.masa.malilib.network.payload.MaLibByteBuf;
import fi.dy.masa.malilib.network.payload.PayloadType;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;

public class ClientConfigHandler<T extends CustomPayload> implements IClientConfigHandler
{
    private static final ClientConfigHandler<CustomPayload> INSTANCE = new ClientConfigHandler<>();
    private final ArrayListMultimap<PayloadType, IPluginConfigHandler<T>> handlers = ArrayListMultimap.create();
    public static IClientConfigHandler getInstance()
    {
        return INSTANCE;
    }

    private ClientConfigHandler() { }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends CustomPayload> void registerClientConfigHandler(IPluginConfigHandler<P> handler)
    {
        PayloadType type = handler.getPayloadType();

        if (type.exists(type))
        {
            if (!this.handlers.containsEntry(type, handler))
            {
                this.handlers.put(type, (IPluginConfigHandler<T>) handler);
                handler.registerConfigPayload(type);
                // Don't register Receivers until Server/World fully joined.
            }
        }
    }

    @Override
    public <P extends CustomPayload> void unregisterClientConfigHandler(IPluginConfigHandler<P> handler)
    {
        PayloadType type = handler.getPayloadType();

        if (type.exists(type))
        {
            if (this.handlers.remove(type, handler))
            {
                handler.unregisterConfigHandler(type);
            }
        }
    }

    /**
     * LOCAL API CALLS DO NOT USE ANYWHERE ELSE (DANGEROUS!)
     */
    public void reset(PayloadType type)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginConfigHandler<T> handler : this.handlers.get(type))
            {
                handler.reset(type);
            }
        }
    }
    public void registerConfigPayload(PayloadType type)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginConfigHandler<T> handler : this.handlers.get(type))
            {
                handler.registerConfigPayload(type);
            }
        }
    }
    public void registerConfigHandler(PayloadType type)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginConfigHandler<T> handler : this.handlers.get(type))
            {
                handler.registerConfigHandler(type);
            }
        }
    }
    public void unregisterConfigHandler(PayloadType type)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginConfigHandler<T> handler : this.handlers.get(type))
            {
                handler.unregisterConfigHandler(type);
            }
        }
    }
    public <P extends CustomPayload> void receiveS2CConfigPayload(PayloadType type, P payload, ClientConfigurationNetworking.Context ctx)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginConfigHandler<T> handler : this.handlers.get(type))
            {
                handler.receiveS2CConfigPayload(type, payload, ctx);
            }
        }
    }
    public <P extends CustomPayload> void receiveS2CConfigPayload(PayloadType type, P payload, ClientConfigurationNetworkHandler networkHandler)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginConfigHandler<T> handler : this.handlers.get(type))
            {
                handler.receiveS2CConfigPayload(type, payload, networkHandler);
            }
        }
    }
   public void decodeS2CNbtCompound(PayloadType type, NbtCompound data)
   {
       if (!this.handlers.isEmpty())
       {
           for (IPluginConfigHandler<T> handler : this.handlers.get(type))
           {
               handler.decodeS2CNbtCompound(type, data);
           }
       }
   }
    public void decodeS2CByteBuf(PayloadType type, MaLibByteBuf data)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginConfigHandler<T> handler : this.handlers.get(type))
            {
                handler.decodeS2CByteBuf(type, data);
            }
        }
    }
}
