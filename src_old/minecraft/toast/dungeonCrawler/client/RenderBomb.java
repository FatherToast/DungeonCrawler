package toast.dungeonCrawler.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import toast.dungeonCrawler._DungeonCrawler;
import toast.dungeonCrawler.EntityBomb;
import toast.dungeonCrawler.ItemHelper;

@SideOnly(Side.CLIENT)
public class RenderBomb extends Render
{
    public RenderBomb() {
    }
    
    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationItemsTexture;
    }
    
    /// Renders the entity.
    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        EntityBomb bomb = (EntityBomb)entity;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.0625F, (float)z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        
        float color;
        if (bomb.fuse >= 0 && (float)bomb.fuse - partialTick + 1.0F < 10.0F) {
            color = 1.0F - ((float)bomb.fuse - partialTick + 1.0F) / 10.0F;
            if (color < 0.0F)
                color = 0.0F;
            if (color > 1.0F)
                color = 1.0F;
            color *= color * color;
            float scale = color * 0.5F;
            GL11.glScalef(bomb.width + scale, bomb.height + scale, bomb.width + scale);
        }
        else
            GL11.glScalef(bomb.width, bomb.height, bomb.width);
        color = (1.0F - ((float)bomb.fuse - partialTick + 1.0F) / 100.0F) * 0.8F;
        
        if (_DungeonCrawler.DCItem != null) {
            bindEntityTexture(bomb);
            Icon icon = _DungeonCrawler.DCItem.getIconFromDamage(ItemHelper.BOMB + bomb.getType());
            if (icon != null) {
                float minU = icon.getMinU();
                float maxU = icon.getMaxU();
                float minV = icon.getMinV();
                float maxV = icon.getMaxV();
                GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
                tessellator.addVertexWithUV(-0.5, -0.5, 0.0, (double)minU, (double)maxV);
                tessellator.addVertexWithUV( 0.5, -0.5, 0.0, (double)maxU, (double)maxV);
                tessellator.addVertexWithUV( 0.5,  0.5, 0.0, (double)maxU, (double)minV);
                tessellator.addVertexWithUV(-0.5,  0.5, 0.0, (double)minU, (double)minV);
                tessellator.draw();
                if (bomb.fuse >= 0 && bomb.fuse / 5 % 2 == 0) {
                    GL11.glEnable(GL11.GL_CULL_FACE);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, color);
                    tessellator.startDrawingQuads();
                    tessellator.setNormal(0.0F, 1.0F, 0.0F);
                    tessellator.addVertexWithUV(-0.5, -0.5, 0.0, (double)minU, (double)maxV);
                    tessellator.addVertexWithUV( 0.5, -0.5, 0.0, (double)maxU, (double)maxV);
                    tessellator.addVertexWithUV( 0.5,  0.5, 0.0, (double)maxU, (double)minV);
                    tessellator.addVertexWithUV(-0.5,  0.5, 0.0, (double)minU, (double)minV);
                    tessellator.draw();
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_CULL_FACE);
                }
            }
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}