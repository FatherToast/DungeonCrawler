package toast.dungeonCrawler;

import java.util.HashMap;
import java.util.Random;
import net.minecraftforge.common.Configuration;

/**
    This helper class automatically creates, stores, and retrieves properties.
    Supported data types:
        String, boolean, int, double
    
    Any property can be retrieved as an Object or String.
    Any non-String property can also be retrieved as any other non-String property.
    Retrieving a number as a boolean will produce a randomized output depending on the value.
*/
public abstract class Properties
{
    /// Mapping of all properties in the mod to their values.
    private static final HashMap<String, Object> map = new HashMap();
    /// Common category names.
        public static final String GENERAL = "_general";
        public static final String ALT_RECIPES = "alternate_recipes";
        public static final String ITEMS = "_items";
        public static final String DCFOODS = "dc_foods";
        public static final String DCITEMS = "dc_items";
        
    /// Initializes these properties.
    public static void init(Configuration config) {
        config.load();
        
        add(config, GENERAL, "block_damage", true, "If this is false, explosions will not deal damage to blocks.");
        add(config, GENERAL, "bone_stick_recipes", true, "If this is false, bone-stick recipes will be disabled.");
        add(config, GENERAL, "override_mushroom_stew", true, "If this is true, mushroom stew will be overridden with a stackable version (ignore the id mismatch 282 warning).");
        add(config, GENERAL, "salvage_recipes", true, "If this is false, salvaging recipes will be disabled.");
        
        add(config, ALT_RECIPES, "bomb", false, "If this is true, bombs are crafted diagonally instead.");
        add(config, ALT_RECIPES, "bone_club", false, "If this is true, bone clubs are crafted diagonally instead.");
        add(config, ALT_RECIPES, "chain", false, "If this is true, chain links will be crafted with two iron ingots instead of one (also doubles output).");
        add(config, ALT_RECIPES, "knife", false, "If this is true, knives are crafted diagonally instead.");
        
        int id = 16000;
        add(config, ITEMS, "_easy_ids", true, "If this is true, all item ids will be determined by a single number. An id of 0 for an item will still disable that item.");
        add(config, ITEMS, "_starting_id", id, "The number this mod's item ids will start at if _easy_ids is enabled.");
        for (int i = 0; i < 4; i++)
            add(config, ITEMS, "armor_bone_" + ItemHelper.getArmorName(i).toLowerCase(), id++);
        add(config, ITEMS, "bone_club", id++);
        add(config, ITEMS, "dc_food", id++);
        add(config, ITEMS, "dc_item", id++);
        for (int i = 0; i < ItemHelper.materials.length; i++)
            add(config, ITEMS, "knife_" + ItemHelper.getMaterialName(i).toLowerCase(), id++);
        add(config, ITEMS, "rockshot", id++);
        add(config, ITEMS, "sharp_stick", id++);
        
        id = ItemHelper.FOOD_BASIC;
        addDCFood(config, id++, 2, 1.2F, true,  "Golden Cookie", "cookie_golden");
        
        id = ItemHelper.FOOD_BASIC + 32; /// Foiled food
        
        id = ItemHelper.FOOD_SMELT;
        addDCFood(config, id++, 2, 0.2F, false, "Undead Meat", "zombie_meat");
        addDCFood(config, id++, 4, 0.4F, false, "Dead Meat", "zombie_meat_cooked");
        
        id = ItemHelper.FOOD_CRAFT;
        addDCFood(config, id++, 2, 0.1F, false, "Eye of Spider", "spider_meat");
        addDCFood(config, id++, 2, 1.2F, true,  "Spider Soup", "spider_soup");
        addDCFood(config, id++, 2, 1.2F, true,  "Spider Soup Bucket", "spider_bucket");
        
        addDCFood(config, id++, 6, 0.6F, false, "Mushroom Stew Bucket", "mushroom_bucket");
        
        
        id = ItemHelper.BASIC;
        addDCItem(config, id++, "Pebble", "pebble");
        addDCItem(config, id++, "Chain Links", "chain");
        
        id = ItemHelper.BASIC + 32; /// Foiled items
        
        id = ItemHelper.BOMB;
        addDCItem(config, id++, "Small Imp Bomb", "bomb_imp_small");
        addDCItem(config, id++, "Imp Bomb", "bomb_imp");
        addDCItem(config, id++, "Large Imp Bomb", "bomb_imp_large");
        
        addDCItem(config, id++, "Small Fire Bomb", "bomb_fire_small");
        addDCItem(config, id++, "Fire Bomb", "bomb_fire");
        addDCItem(config, id++, "Large Fire Bomb", "bomb_fire_large");
        
        addDCItem(config, id++, "Small End Bomb", "bomb_end_small");
        addDCItem(config, id++, "End Bomb", "bomb_end");
        addDCItem(config, id++, "Large End Bomb", "bomb_end_large");
        
        id = ItemHelper.GRENADE;
        addDCItem(config, id++, "Small Grenade", "grenade_imp_small");
        addDCItem(config, id++, "Grenade", "grenade_imp");
        addDCItem(config, id++, "Large Grenade", "grenade_imp_large");
        
        addDCItem(config, id++, "Small Fire Grenade", "grenade_fire_small");
        addDCItem(config, id++, "Fire Grenade", "grenade_fire");
        addDCItem(config, id++, "Large Fire Grenade", "grenade_fire_large");
        
        addDCItem(config, id++, "Small End Grenade", "grenade_end_small");
        addDCItem(config, id++, "End Grenade", "grenade_end");
        addDCItem(config, id++, "Large End Grenade", "grenade_end_large");
        
        id = ItemHelper.MINE;
        addDCItem(config, id++, "Small Mine", "mine_imp_small");
        addDCItem(config, id++, "Mine", "mine_imp");
        addDCItem(config, id++, "Large Mine", "mine_imp_large");
        
        addDCItem(config, id++, "Small Fire Mine", "mine_fire_small");
        addDCItem(config, id++, "Fire Mine", "mine_fire");
        addDCItem(config, id++, "Large Fire Mine", "mine_fire_large");
        
        addDCItem(config, id++, "Small End Mine", "mine_end_small");
        addDCItem(config, id++, "End Mine", "mine_end");
        addDCItem(config, id++, "Large End Mine", "mine_end_large");
        
        addDCItem(config, id++, "Gas Mine", "mine_gas");
        
        id = ItemHelper.TRAP;
        addDCItem(config, id++, "Spike Trap", "trap_spike", "trap_spike_on", "trap_spike_off");
        addDCItem(config, id++, "Poison Trap", "trap_spike_poison", "trap_spike_poison_on", "trap_spike_poison_off");
        
        addDCItem(config, id++, "Fire Trap", "trap_fire", "trap_fire_on", "trap_fire_off");
        addDCItem(config, id++, "End Trap", "trap_end", "trap_end_on", "trap_end_off");
        addDCItem(config, id++, "Screamer Trap", "trap_screamer", "trap_screamer_on", "trap_screamer_off");
        
        addDCItem(config, id++, "Launcher Trap", "trap_launcher", "trap_launcher_base", "trap_launcher_head");
        addDCItem(config, id++, "Crippling Trap", "trap_crippling");
        
        config.addCustomCategoryComment(GENERAL, "General and/or miscellaneous options.");
        config.addCustomCategoryComment(ITEMS, "Options for dictating item ids. If you set an item's id to 0, it will be disabled.");
        config.addCustomCategoryComment(ALT_RECIPES, "Enables/disables alternate recipes.");
        config.addCustomCategoryComment(DCITEMS, "Enables/disables specific dc items. These items all share the same item id (dc_item).");
        config.save();
    }
    
    /// Loads the DCItem, if enabled.
    private static void addDCItem(Configuration config, int index, String... values) {
        if (config.get(DCITEMS, values[1], true).getBoolean(true))
            ItemHelper.DCItemNames[index] = values;
    }
    
    /// Loads the DCFood, if enabled.
    private static void addDCFood(Configuration config, int index, int stamina, float saturation, boolean alwaysEdible, String... values) {
        if (config.get(DCFOODS, values[1], true).getBoolean(true)) {
            ItemHelper.DCFoodNames[index] = values;
            ItemDCFood.healAmounts[index] = stamina;
            ItemDCFood.saturationModifiers[index] = saturation;
            ItemDCFood.alwaysEdible[index] = alwaysEdible;
        }
    }
    
    /// Passes to the mod.
    public static void debugException(String message) {
        _DungeonCrawler.debugException(message);
    }
    
    /// Loads the property as the specified value.
    public static void add(Configuration config, String category, String field, String defaultValue, String comment) {
        map.put(category + "@" + field, config.get(category, field, defaultValue, comment).getString());
    }
    public static void add(Configuration config, String category, String field, int defaultValue) {
        map.put(category + "@" + field, Integer.valueOf(config.get(category, field, defaultValue).getInt(defaultValue)));
    }
    public static void add(Configuration config, String category, String field, int defaultValue, String comment) {
        map.put(category + "@" + field, Integer.valueOf(config.get(category, field, defaultValue, comment).getInt(defaultValue)));
    }
    public static void add(Configuration config, String category, String field, boolean defaultValue, String comment) {
        map.put(category + "@" + field, Boolean.valueOf(config.get(category, field, defaultValue, comment).getBoolean(defaultValue)));
    }
    public static void add(Configuration config, String category, String field, double defaultValue, String comment) {
        map.put(category + "@" + field, Double.valueOf(config.get(category, field, defaultValue, comment).getDouble(defaultValue)));
    }
    
    /// Gets the Object property.
    public static Object getProperty(String category, String field) {
        return map.get(category + "@" + field);
    }
    
    /// Gets the value of the property (instead of an Object representing it).
    public static String getString(String category, String field) {
        return getProperty(category, field).toString();
    }
    public static boolean getBoolean(String category, String field) {
        Object property = getProperty(category, field);
        if (property instanceof Boolean)
            return ((Boolean)property).booleanValue();
        debugException("Tried to get boolean for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return false;
    }
    public static int getInt(String category, String field) {
        Object property = getProperty(category, field);
        if (property instanceof Number)
            return ((Number)property).intValue();
        if (property instanceof Boolean)
            return ((Boolean)property).booleanValue() ? 1 : 0;
        debugException("Tried to get int for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return 0;
    }
    public static double getDouble(String category, String field) {
        Object property = getProperty(category, field);
        if (property instanceof Number)
            return ((Number)property).doubleValue();
        if (property instanceof Boolean)
            return ((Boolean)property).booleanValue() ? 1.0 : 0.0;
        debugException("Tried to get double for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return 0.0;
    }
}