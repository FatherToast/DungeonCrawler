package toast.dungeonCrawler.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import toast.dungeonCrawler.EntityTrap;
import toast.dungeonCrawler.ItemDCItem;
import toast.dungeonCrawler.ItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTrap extends Render {

    public RenderTrap() {}

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationItemsTexture;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        EntityTrap trap = (EntityTrap)entity;
        int type = ItemHelper.TRAP + trap.getType();
        if (type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_launcher")) {
			this.doRenderLauncherTrap(trap, x, y, z, yaw, partialTick);
		} else if (type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_crippling")) {
			this.doRenderBearTrap(trap, x, y, z, yaw, partialTick);
		} else if (type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_spike") || type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_spike_poison")) {
			this.doRenderSpikeTrap(trap, x, y, z, yaw, partialTick);
		} else {
			this.doRenderFlatTrap(trap, x, y, z, yaw, partialTick);
		}
    }

    // Renders the standard, flat trap.
    public void doRenderFlatTrap(EntityTrap trap, double x, double y, double z, float yaw, float partialTick) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.0625F, (float)z);
        GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        this.bindEntityTexture(trap);

        try {
            this.doRender2D(ItemDCItem.icons[ItemHelper.TRAP + trap.getType()][trap.getIsSet() ? 1 : 2]);
        }
        catch (Exception ex) {
        	// Do nothing
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    // Renders a spike trap.
    public void doRenderSpikeTrap(EntityTrap trap, double x, double y, double z, float yaw, float partialTick) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.5F, (float)z);
        GL11.glEnable(GL11.GL_CULL_FACE);
        this.bindEntityTexture(trap);

        IIcon icon = null;
        try {
            icon = ItemDCItem.icons[ItemHelper.TRAP + trap.getType()][trap.getIsSet() ? 1 : 2];
        }
        catch (Exception ex) {
        	// Do nothing
        }
        if (icon == null)
            return;
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.25,  0.5, -0.5,  minU, minV);
        tessellator.addVertexWithUV(-0.25, -0.5, -0.5,  minU, maxV);
        tessellator.addVertexWithUV(-0.25, -0.5,  0.5,  maxU, maxV);
        tessellator.addVertexWithUV(-0.25,  0.5,  0.5,  maxU, minV);
        tessellator.addVertexWithUV(-0.25,  0.5,  0.5,  minU, minV);
        tessellator.addVertexWithUV(-0.25, -0.5,  0.5,  minU, maxV);
        tessellator.addVertexWithUV(-0.25, -0.5, -0.5,  maxU, maxV);
        tessellator.addVertexWithUV(-0.25,  0.5, -0.5,  maxU, minV);
        tessellator.addVertexWithUV( 0.25,  0.5,  0.5,  minU, minV);
        tessellator.addVertexWithUV( 0.25, -0.5,  0.5,  minU, maxV);
        tessellator.addVertexWithUV( 0.25, -0.5, -0.5,  maxU, maxV);
        tessellator.addVertexWithUV( 0.25,  0.5, -0.5,  maxU, minV);
        tessellator.addVertexWithUV( 0.25,  0.5, -0.5,  minU, minV);
        tessellator.addVertexWithUV( 0.25, -0.5, -0.5,  minU, maxV);
        tessellator.addVertexWithUV( 0.25, -0.5,  0.5,  maxU, maxV);
        tessellator.addVertexWithUV( 0.25,  0.5,  0.5,  maxU, minV);
        tessellator.addVertexWithUV(-0.5,   0.5, -0.25, minU, minV);
        tessellator.addVertexWithUV(-0.5,  -0.5, -0.25, minU, maxV);
        tessellator.addVertexWithUV( 0.5,  -0.5, -0.25, maxU, maxV);
        tessellator.addVertexWithUV( 0.5,   0.5, -0.25, maxU, minV);
        tessellator.addVertexWithUV( 0.5,   0.5, -0.25, minU, minV);
        tessellator.addVertexWithUV( 0.5,  -0.5, -0.25, minU, maxV);
        tessellator.addVertexWithUV(-0.5,  -0.5, -0.25, maxU, maxV);
        tessellator.addVertexWithUV(-0.5,   0.5, -0.25, maxU, minV);
        tessellator.addVertexWithUV( 0.5,   0.5,  0.25, minU, minV);
        tessellator.addVertexWithUV( 0.5,  -0.5,  0.25, minU, maxV);
        tessellator.addVertexWithUV(-0.5,  -0.5,  0.25, maxU, maxV);
        tessellator.addVertexWithUV(-0.5,   0.5,  0.25, maxU, minV);
        tessellator.addVertexWithUV(-0.5,   0.5,  0.25, minU, minV);
        tessellator.addVertexWithUV(-0.5,  -0.5,  0.25, minU, maxV);
        tessellator.addVertexWithUV( 0.5,  -0.5,  0.25, maxU, maxV);
        tessellator.addVertexWithUV( 0.5,   0.5,  0.25, maxU, minV);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    // Renders a piston trap.
    public void doRenderLauncherTrap(EntityTrap trap, double x, double y, double z, float yaw, float partialTick) {
        boolean set = trap.getIsSet();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.0625F, (float)z);
        GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        this.bindEntityTexture(trap);

        // body
        try {
            this.doRender2D(ItemDCItem.icons[ItemHelper.TRAP + trap.getType()][1]);
        }
        catch (NullPointerException ex) {
        	// Do nothing
        }

        // head
        GL11.glTranslatef(0.0F, 0.0F, set ? 0.0625F : 0.375F);
        IIcon icon = null;
        try {
            icon = ItemDCItem.icons[ItemHelper.TRAP + trap.getType()][2];
        }
        catch (NullPointerException ex) {
        	// Do nothing
        }
        if (icon == null)
            return;
        this.doRender2D(icon);

        // arm
        float minU = icon.getInterpolatedU(6.0);
        float maxU = icon.getInterpolatedU(10.0);
        float minV = icon.getInterpolatedV(4.0);
        float maxV = icon.getInterpolatedV(set ? 5.0 : 10.0);
        double off = set ? -0.0625 : -0.375;
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(0.1767766953F, 1.0F, 0.1767766953F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.45, 0.0, -0.45, minU, minV);
        tessellator.addVertexWithUV(-0.45, off, -0.45, minU, maxV);
        tessellator.addVertexWithUV( 0.45, off,  0.45, maxU, maxV);
        tessellator.addVertexWithUV( 0.45, 0.0,  0.45, maxU, minV);
        tessellator.addVertexWithUV( 0.45, 0.0,  0.45, minU, minV);
        tessellator.addVertexWithUV( 0.45, off,  0.45, minU, maxV);
        tessellator.addVertexWithUV(-0.45, off, -0.45, maxU, maxV);
        tessellator.addVertexWithUV(-0.45, 0.0, -0.45, maxU, minV);
        tessellator.addVertexWithUV(-0.45, 0.0,  0.45, minU, minV);
        tessellator.addVertexWithUV(-0.45, off,  0.45, minU, maxV);
        tessellator.addVertexWithUV( 0.45, off, -0.45, maxU, maxV);
        tessellator.addVertexWithUV( 0.45, 0.0, -0.45, maxU, minV);
        tessellator.addVertexWithUV( 0.45, 0.0, -0.45, minU, minV);
        tessellator.addVertexWithUV( 0.45, off, -0.45, minU, maxV);
        tessellator.addVertexWithUV(-0.45, off,  0.45, maxU, maxV);
        tessellator.addVertexWithUV(-0.45, 0.0,  0.45, maxU, minV);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    // Renders a bear trap.
    public void doRenderBearTrap(EntityTrap trap, double x, double y, double z, float yaw, float partialTick) {
        RenderBearTrap.render(trap, x, y, z, yaw, partialTick);
    }

    // Renders a standard 2D trap.
    private void doRender2D(IIcon icon) {
        if (icon == null)
            return;
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5, -0.5, 0.0, minU, maxV);
        tessellator.addVertexWithUV( 0.5, -0.5, 0.0, maxU, maxV);
        tessellator.addVertexWithUV( 0.5,  0.5, 0.0, maxU, minV);
        tessellator.addVertexWithUV(-0.5,  0.5, 0.0, minU, minV);
        tessellator.draw();
    }
}