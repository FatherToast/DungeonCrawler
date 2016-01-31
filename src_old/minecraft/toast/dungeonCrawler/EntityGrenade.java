package toast.dungeonCrawler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityGrenade extends EntityThrowable
{
    /// Ticks until this grenade explodes.
    /// Currently, just used to flag that it has been hit.
    public int fuse = -1;
    
    public EntityGrenade(World world) {
        super(world);
    }
    
    public EntityGrenade(World world, EntityLivingBase throwingEntity, int t) {
        super(world, throwingEntity);
		setType(t);
    }
    
    public EntityGrenade(World world, int t, double x, double y, double z) {
        super(world, x, y, z);
		setType(t);
    }
    
    /// Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        super.entityInit();
        /// type; The ID of this bomb.
        dataWatcher.addObject(31, 0);
    }
    
    /// Gets the amount of gravity to apply to the thrown entity with each tick (default = 0.3).
    @Override
    protected float getGravityVelocity() {
        return 0.05F;
    }
    
    /// Gets the initial throwing velocity (default = 1.5).
    @Override
    protected float func_70182_d() {
        return 0.6F;
    }
    
    /// Vertical angle shift (negative increases angle, default = 0).
    @Override
    protected float func_70183_g() {
        return -20.0F;
    }
    
    /// Called to deal damage to this entity. Returns true if damage is dealt.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (fuse < 0) {
            fuse = 40;
            trigger();
        }
        return damageSource.isProjectile();
    }
    
    /// Called when this EntityThrowable hits a block or entity.
    @Override
    protected void onImpact(MovingObjectPosition object) {
        trigger();
    }
    
    /// Called to detonate the grenade.
	public void trigger() {
		ItemHelper.explodeByType(this, getType());
		setDead();
	}
    
    /// Get/set functions for type.
    public int getType() {
        return dataWatcher.getWatchableObjectInt(31);
    }
    public void setType(int type) {
        dataWatcher.updateObject(31, type);
    }
}