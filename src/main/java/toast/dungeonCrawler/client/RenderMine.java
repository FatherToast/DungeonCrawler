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
import toast.dungeonCrawler.EntityMine;
import toast.dungeonCrawler.ItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMine extends Render{

    public RenderMine() {}

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationItemsTexture;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        EntityMine mine = (EntityMine)entity;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y + 0.0625F, (float)z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        float color;
        if (mine.fuse >= 0 && mine.fuse - partialTick + 1.0F < 10.0F) {
            color = 1.0F - (mine.fuse - partialTick + 1.0F) / 10.0F;
            if (color < 0.0F) {
				color = 0.0F;
			}
            if (color > 1.0F) {
				color = 1.0F;
			}
            color *= color * color;
            float scale = color * 0.5F;
            GL11.glScalef(mine.width + scale, mine.height + scale, mine.width + scale);
        } else {
			GL11.glScalef(mine.width, mine.height, mine.width);
		}
        color = (1.0F - (mine.fuse - partialTick + 1.0F) / 100.0F) * 0.8F;

        if (DungeonCrawlerMod.DCItem != null) {
            this.bindEntityTexture(mine);
            IIcon icon = DungeonCrawlerMod.DCItem.getIconFromDamage(ItemHelper.MINE + mine.getType());
            if (icon != null) {
                float minU = icon.getMinU();
                float maxU = icon.getMaxU();
                float minV = icon.getMinV();
                float maxV = icon.getMaxV();
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
                tessellator.addVertexWithUV(-0.5, -0.5, 0.0, minU, maxV);
                tessellator.addVertexWithUV( 0.5, -0.5, 0.0, maxU, maxV);
                tessellator.addVertexWithUV( 0.5,  0.5, 0.0, maxU, minV);
                tessellator.addVertexWithUV(-0.5,  0.5, 0.0, minU, minV);
                tessellator.draw();
                if (mine.fuse >= 0 && mine.fuse / 5 % 2 == 0) {
                    GL11.glEnable(GL11.GL_CULL_FACE);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, color);
                    tessellator.startDrawingQuads();
                    tessellator.setNormal(0.0F, 1.0F, 0.0F);
                    tessellator.addVertexWithUV(-0.5, -0.5, 0.0, minU, maxV);
                    tessellator.addVertexWithUV( 0.5, -0.5, 0.0, maxU, maxV);
                    tessellator.addVertexWithUV( 0.5,  0.5, 0.0, maxU, minV);
                    tessellator.addVertexWithUV(-0.5,  0.5, 0.0, minU, minV);
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