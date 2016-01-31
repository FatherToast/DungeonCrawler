package toast.dungeonCrawler;

public interface ITrap
{
    /// Called to detonate the trap.
    public void trigger();
    
    /// Called to disarm the trap and drop itself as a resource.
    public void dropAsItem();
}