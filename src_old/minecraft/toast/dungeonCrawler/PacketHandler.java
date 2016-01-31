package toast.dungeonCrawler;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import net.minecraft.entity.Entity;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;

public class PacketHandler implements IPacketHandler
{
    /// Called when a packet is recieved.
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        if (packet.channel.equals("DC|TPEntity"))
            handleTeleportPacket(packet, ((Entity)player).worldObj);
    }
    
    /// Handles a teleport packet.
    private void handleTeleportPacket(Packet250CustomPayload packet, World world) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
        double[] dim = new double[2];
        double[] pos = new double[6];
        try {
            for (int i = 0; i < 2; i++)
                dim[i] = (double)in.readFloat();
            for (int i = 0; i < 6; i++)
                pos[i] = (double)in.readFloat();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        for (int i = 0; i < 128; i++) {
            double posRelative = (double)i / 127D;
            float vX = (_DungeonCrawler.random.nextFloat() - 0.5F) * 0.2F;
            float vY = (_DungeonCrawler.random.nextFloat() - 0.5F) * 0.2F;
            float vZ = (_DungeonCrawler.random.nextFloat() - 0.5F) * 0.2F;
            double dX = pos[0] + (pos[3] - pos[0]) * posRelative + (_DungeonCrawler.random.nextDouble() - 0.5D) * dim[0] * 2D;
            double dY = pos[1] + (pos[4] - pos[1]) * posRelative + _DungeonCrawler.random.nextDouble() * dim[1];
            double dZ = pos[2] + (pos[5] - pos[2]) * posRelative + (_DungeonCrawler.random.nextDouble() - 0.5D) * dim[0] * 2D;
            world.spawnParticle("portal", dX, dY, dZ, (double)vX, (double)vY, (double)vZ);
        }
        world.playSoundEffect(pos[0], pos[1], pos[2], "mob.endermen.portal", 1F, 1F);
        world.playSoundEffect(pos[3], pos[4], pos[5], "mob.endermen.portal", 1F, 1F);
    }
    
    /// Sends a teleport packet for the entity.
    public static void sendTeleportPacket(Entity entity, double... pos) {
        if (pos.length != 6)
            return;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(32);
        DataOutputStream out = new DataOutputStream(byteStream);
        try {
            out.writeFloat(entity.width);
            out.writeFloat(entity.height);
            for (int i = 0; i < 6; i++)
                out.writeFloat((float)pos[i]);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        PacketDispatcher.sendPacketToAllInDimension(new Packet250CustomPayload("DC|TPEntity", byteStream.toByteArray()), entity.worldObj.provider.dimensionId);
    }
}