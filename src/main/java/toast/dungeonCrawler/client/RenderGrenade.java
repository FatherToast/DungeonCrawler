package toast.dungeonCrawler.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import toast.dungeonCrawler.DungeonCrawlerMod;
import toast.dungeonCrawler.EntityGrenade;
import toast.dungeonCrawler.ItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGrenade extends Render {

    public RenderGrenade() {}

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationItemsTexture;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        EntityGrenade grenade = (EntityGrenade)entity;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        if (DungeonCrawlerMod.DCItem != null) {
            this.bindEntityTexture(entity);
            this.doRender2D(DungeonCrawlerMod.DCItem.getIconFromDamage(ItemHelper.GRENADE + grenade.getType()));
        }
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    // Renders the item in 2D, facing the player.
    private void doRender2D(IIcon icon) {
        if (icon == null)
            return;
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5, -0.25, 0.0, minU, maxV);
        tessellator.addVertexWithUV( 0.5, -0.25, 0.0, maxU, maxV);
        tessellator.addVertexWithUV( 0.5,  0.75, 0.0, maxU, minV);
        tessellator.addVertexWithUV(-0.5,  0.75, 0.0, minU, minV);
        tessellator.draw();
    }
}