package toast.dungeonCrawler.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import toast.dungeonCrawler.EntityTrap;

@SideOnly(Side.CLIENT)
public class ModelBearTrap extends ModelBase
{
    /// The two different states (open, closed) this model can be in.
    public ModelRenderer[] states = new ModelRenderer[2];
    
    public ModelBearTrap() {
        textureWidth = 64;
        textureHeight = 64;
        
        /// Opened
        states[0] = new ModelRenderer(this, 0, 17).addBox(-7.0F, -5.0F, -8.0F, 14, 5, 16);
        states[0].rotateAngleX = (float)Math.PI;
        /// Closed
        states[1] = new ModelRenderer(this).addBox(-7.0F, -9.0F, -4.0F, 14, 9, 8);
        states[1].rotateAngleX = (float)Math.PI;
    }
    
    /// Sets the models various rotation angles then renders the model.
    @Override
    public void render(Entity entity, float time, float moveSpeed, float rotationFloat, float rotationYaw, float rotationPitch, float scale) {
        states[((EntityTrap)entity).getIsSet() ? 0 : 1].render(scale);
    }
}