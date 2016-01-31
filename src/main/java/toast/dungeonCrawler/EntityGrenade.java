package toast.dungeonCrawler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityGrenade extends EntityThrowable {

    // Ticks until this grenade explodes.
    // Currently, just used to flag that it has been hit.
    public int fuse = -1;

    public EntityGrenade(World world) {
        super(world);
    }

    public EntityGrenade(World world, EntityLivingBase throwingEntity, int t) {
        super(world, throwingEntity);
		this.setType(t);
    }

    public EntityGrenade(World world, int t, double x, double y, double z) {
        super(world, x, y, z);
		this.setType(t);
    }

    // Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        super.entityInit();
        // type; The ID of this bomb.
        this.dataWatcher.addObject(31, 0);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.05F;
    }

    // Gets the initial throwing velocity (default = 1.5).
    @Override
    protected float func_70182_d() {
        return 0.6F;
    }

    @Override
    protected float func_70183_g() {
        return -20.0F;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (this.fuse < 0) {
            this.fuse = 40;
            this.trigger();
        }
        return damageSource.isProjectile();
    }

    @Override
    protected void onImpact(MovingObjectPosition object) {
        this.trigger();
    }

    // Called to detonate the grenade.
	public void trigger() {
		ItemHelper.explodeByType(this, this.getType());
		this.setDead();
	}

    // Get/set functions for type.
    public int getType() {
        return this.dataWatcher.getWatchableObjectInt(31);
    }
    public void setType(int type) {
        this.dataWatcher.updateObject(31, type);
    }
}