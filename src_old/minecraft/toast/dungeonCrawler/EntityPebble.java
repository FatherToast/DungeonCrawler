package toast.dungeonCrawler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityPebble extends EntityThrowable
{
    public EntityPebble(World world) {
        super(world);
    }
    
    public EntityPebble(World world, EntityLivingBase throwingEntity) {
        super(world, throwingEntity);
    }
    
    public EntityPebble(World world, double x, double y, double z) {
        super(world, x, y, z);
    }
    
    /// Called when this EntityThrowable hits a block or entity.
    @Override
    protected void onImpact(MovingObjectPosition object) {
        if (object.entityHit != null)
            object.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 1.0F);
        for (int i = 0; i < 3; i++)
            worldObj.spawnParticle("smoke", posX + (rand.nextDouble() - 0.5) / 3.0, posY + (rand.nextDouble() - 0.5) / 3.0, posZ + (rand.nextDouble() - 0.5) / 3.0, 0.0, 0.0, 0.0);
        if (!worldObj.isRemote)
            setDead();
    }
}