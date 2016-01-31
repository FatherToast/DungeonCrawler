package toast.dungeonCrawler.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import toast.dungeonCrawler._DungeonCrawler;

@SideOnly(Side.CLIENT)
public class RenderBearTrap extends Render
{
    /// The instance of this renderer. Used to actually do the rendering.
    private static final RenderBearTrap INSTANCE = new RenderBearTrap();
    /// The texture for this renderer.
    public static final ResourceLocation BEAR_TRAP_IMAGE = new ResourceLocation("textures/entity/cripplingTrap.png");
    
    /// The model this will render.
    public ModelBase model;
    
    public RenderBearTrap() {
        shadowSize = 0.5F;
        model = new ModelBearTrap();
        setRenderManager(RenderManager.instance);
    }
    
    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return BEAR_TRAP_IMAGE;
    }
    
    /// Renders the entity.
    public static void render(Entity entity, double x, double y, double z, float pitch, float yaw) {
        INSTANCE.doRender(entity, x, y, z, pitch, yaw);
    }
    
    /// Renders the entity.
    @Override
    public void doRender(Entity entity, double x, double y, double z, float pitch, float yaw) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glTranslatef((float)x, (float)y + 0.0625F, (float)z);
        bindEntityTexture(entity);
        model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }
}