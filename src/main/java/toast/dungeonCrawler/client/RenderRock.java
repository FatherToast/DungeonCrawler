package toast.dungeonCrawler.client;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import toast.dungeonCrawler.EntityRock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRock extends Render {

    // Helps this class render blocks.
    private static RenderBlocks blockRenderer = new RenderBlocks();
    // The current texture to use for rendering.
    private static ResourceLocation currentTexture;

    public RenderRock() {
        this.shadowSize = 0.5F;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return RenderRock.currentTexture;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        ItemStack rockStack = ((EntityRock) entity).getItemStack();
        if (rockStack == null)
            return;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        if (rockStack.getItemSpriteNumber() == 0 && rockStack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(((ItemBlock) rockStack.getItem()).field_150939_a.getRenderType())) {
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            RenderRock.currentTexture = TextureMap.locationBlocksTexture;
            this.bindEntityTexture(entity);
            RenderRock.blockRenderer.renderBlockAsItem(((ItemBlock) rockStack.getItem()).field_150939_a /* the block */, rockStack.getItemDamage(), 1.0F);
        }
        else {
            IIcon icon = rockStack.getIconIndex();
            if (icon != null) {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                if (rockStack.getItemSpriteNumber() == 0) {
					RenderRock.currentTexture = TextureMap.locationBlocksTexture;
				} else {
					RenderRock.currentTexture = TextureMap.locationItemsTexture;
				}
                this.bindEntityTexture(entity);
                if (icon == ItemPotion.func_94589_d("potion") /* Gets the specific icon. */) {
                    int color = PotionHelper.func_77915_a(rockStack.getItemDamage(), false); // Gets the color int for the potion damage value.
                    float r = (color >> 16 & 255) / 255.0F;
                    float g = (color >> 8  & 255) / 255.0F;
                    float b = (color       & 255) / 255.0F;
                    GL11.glColor3f(r, g, b);
                    GL11.glPushMatrix();
                    this.doRender2D(ItemPotion.func_94589_d("potion_contents") /* Gets the specific icon. */);
                    GL11.glPopMatrix();
                    GL11.glColor3f(1.0F, 1.0F, 1.0F);
                }
                this.doRender2D(icon);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            }
        }
        GL11.glPopMatrix();
    }

    // Renders the item in 2D, facing the player.
    private void doRender2D(IIcon icon) {
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