package toast.dungeonCrawler.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import toast.dungeonCrawler.EntityRock;

@SideOnly(Side.CLIENT)
public class RenderRock extends Render
{
    /// Helps this class render blocks.
    private static RenderBlocks blockRenderer = new RenderBlocks();
    /// The current texture to use for rendering.
    private static ResourceLocation currentTexture;
    
    public RenderRock() {
        shadowSize = 0.5F;
    }
    
    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return currentTexture;
    }
    
    /// Renders the entity.
    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        ItemStack rockStack = ((EntityRock)entity).getItemStack();
        if (rockStack == null)
            return;
        Block block = null;
        if (rockStack.itemID < Block.blocksList.length)
            block = Block.blocksList[rockStack.itemID];
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        if (rockStack.getItemSpriteNumber() == 0 && block != null && RenderBlocks.renderItemIn3d(block.getRenderType())) {
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            currentTexture = TextureMap.locationBlocksTexture;
            bindEntityTexture(entity);
            blockRenderer.renderBlockAsItem(block, rockStack.getItemDamage(), 1.0F);
        }
        else {
            Icon icon = rockStack.getIconIndex();
            if (icon != null) {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                if (rockStack.getItemSpriteNumber() == 0)
                    currentTexture = TextureMap.locationBlocksTexture;
                else
                    currentTexture = TextureMap.locationItemsTexture;
                bindEntityTexture(entity);
                if (icon == ItemPotion.func_94589_d("potion") /** Gets the specific icon. */) {
                    int color = PotionHelper.func_77915_a(rockStack.getItemDamage(), false); /// Gets the color int for the potion damage value.
                    float r = (float)(color >> 16 & 255) / 255.0F;
                    float g = (float)(color >> 8  & 255) / 255.0F;
                    float b = (float)(color       & 255) / 255.0F;
                    GL11.glColor3f(r, g, b);
                    GL11.glPushMatrix();
                    doRender2D(ItemPotion.func_94589_d("potion_contents") /** Gets the specific icon. */);
                    GL11.glPopMatrix();
                    GL11.glColor3f(1.0F, 1.0F, 1.0F);
                }
                doRender2D(icon);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            }
        }
        GL11.glPopMatrix();
    }
    
    /// Renders the item in 2D, facing the player.
    private void doRender2D(Icon icon) {
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5, -0.25, 0.0, (double)minU, (double)maxV);
        tessellator.addVertexWithUV( 0.5, -0.25, 0.0, (double)maxU, (double)maxV);
        tessellator.addVertexWithUV( 0.5,  0.75, 0.0, (double)maxU, (double)minV);
        tessellator.addVertexWithUV(-0.5,  0.75, 0.0, (double)minU, (double)minV);
        tessellator.draw();
    }
}