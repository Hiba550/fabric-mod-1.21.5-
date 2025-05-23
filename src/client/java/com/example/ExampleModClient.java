package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class ExampleModClient implements ClientModInitializer {
    
    // Thread-safe map to prevent concurrent modification crashes
    private static final Map<Item, SmithingTemplateData> SMITHING_TEMPLATE_TOOLTIPS = new ConcurrentHashMap<>();
    
    // Configuration options
    private static boolean enableEnhancedTooltips = true;
    
    /**
     * Simplified data class for smithing template information
     */
    private static class SmithingTemplateData {
        public final String title;
        public final String location;
        public final String duplication;
        public final String usage;
        public final String lore;
        public final String rarity;
        public final Formatting rarityColor;
        
        public SmithingTemplateData(String title, String location, String duplication, 
                                   String usage, String lore, String rarity, Formatting rarityColor) {
            this.title = title != null ? title : "Unknown Template";
            this.location = location != null ? location : "Unknown Location";
            this.duplication = duplication != null ? duplication : "Unknown Recipe";
            this.usage = usage != null ? usage : "Unknown Usage";
            this.lore = lore != null ? lore : "No lore available";
            this.rarity = rarity != null ? rarity : "Common";
            this.rarityColor = rarityColor != null ? rarityColor : Formatting.WHITE;
        }
    }
    
    @Override
    public void onInitializeClient() {
        try {
            // Initialize tooltip data with crash protection
            initializeTooltipData();
            
            // Register the enhanced tooltip callback
            ItemTooltipCallback.EVENT.register((stack, context, tooltipType, lines) -> {
                handleTooltipSafely(stack, context, tooltipType, lines);
            });
            
            ExampleMod.LOGGER.info("SmithingTemplateTooltips initialized successfully!");
            
        } catch (Exception e) {
            ExampleMod.LOGGER.error("Critical error during SmithingTemplateTooltips initialization", e);
            enableEnhancedTooltips = false;
        }
    }
    
    /**
     * Safe tooltip handler
     */
    private void handleTooltipSafely(ItemStack stack, Object contextObj, 
                                   Object tooltipType, List<Text> lines) {
        if (!enableEnhancedTooltips) return;
        
        try {
            if (stack == null || stack.isEmpty() || lines == null) return;
            
            Item item = stack.getItem();
            if (item == null) return;
            
            if (!(item instanceof SmithingTemplateItem)) return;
            
            SmithingTemplateData data = SMITHING_TEMPLATE_TOOLTIPS.get(item);
            if (data == null) return;
            
            synchronized (lines) {
                enhanceTooltipSafely(lines, data, stack, contextObj);
            }
            
        } catch (Exception e) {
            ExampleMod.LOGGER.warn("Error enhancing tooltip: {}", e.getMessage());
        }
    }
    
    /**
     * Enhanced tooltip modification with safety checks
     */
    private void enhanceTooltipSafely(List<Text> lines, SmithingTemplateData data, 
                                    ItemStack stack, Object contextObj) {
        try {
            // Find safe insertion point (after item name)
            int insertionIndex = findSafeInsertionPoint(lines);
            if (insertionIndex < 0) return;
            
            // Create simplified tooltip content
            List<Text> enhancedContent = createSimplifiedTooltipContent(data);
            
            // Safely insert content
            for (int i = 0; i < enhancedContent.size(); i++) {
                if (insertionIndex + i <= lines.size()) {
                    lines.add(insertionIndex + i, enhancedContent.get(i));
                }
            }
            
        } catch (Exception e) {
            ExampleMod.LOGGER.debug("Error in tooltip enhancement: {}", e.getMessage());
        }
    }
    
    /**
     * Find a safe place to insert tooltip content
     */
    private int findSafeInsertionPoint(List<Text> lines) {
        if (lines.isEmpty()) return 0;
        
        // Insert after the item name (usually first line)
        for (int i = 1; i < lines.size(); i++) {
            String lineText = lines.get(i).getString().toLowerCase();
            if (lineText.contains("applies to") || lineText.contains("ingredients") || 
                lineText.contains("smithing template") || lineText.trim().isEmpty()) {
                return i;
            }
        }
        
        return lines.size();
    }
    
    /**
     * Create simplified tooltip content with only the requested information
     */
    private List<Text> createSimplifiedTooltipContent(SmithingTemplateData data) {
        List<Text> content = new ArrayList<>();
        
        try {
            // Add separation
            content.add(Text.empty());
            
            // Add rarity information
            content.add(Text.literal("✦ Rarity: ")
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                .append(Text.literal(data.rarity)
                    .setStyle(Style.EMPTY.withColor(data.rarityColor).withBold(true))));
            
            // Location information
            content.add(Text.literal("📍 Found: ")
                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                .append(Text.literal(data.location)
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE))));
            
            // Duplication recipe
            content.add(Text.literal("🔄 Duplicate: ")
                .setStyle(Style.EMPTY.withColor(Formatting.AQUA))
                .append(Text.literal(data.duplication)
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE))));
            
            // Usage information
            content.add(Text.literal("⚡ Use: ")
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN))
                .append(Text.literal(data.usage)
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE))));
            
            // Lore text
            content.add(Text.literal("📜 Lore: ")
                .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE))
                .append(Text.literal("\"" + data.lore + "\"")
                    .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(true))));
            
        } catch (Exception e) {
            ExampleMod.LOGGER.warn("Error creating tooltip content: {}", e.getMessage());
            content.clear();
            content.add(Text.literal("Enhanced Smithing Template")
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
        }
        
        return content;
    }
    
    /**
     * Initialize tooltip data for all 19 smithing templates
     */
    private void initializeTooltipData() {
        try {
            // NETHERITE UPGRADE
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Netherite Upgrade Template",
                    "Bastion Remnant treasure chests (1.7% chance)",
                    "7 Diamonds + 1 Netherrack",
                    "Upgrade Diamond gear with Netherite Ingot to create the ultimate equipment",
                    "Forged in the eternal flames of the Nether, this template channels primordial power that predates the Overworld itself.",
                    "Legendary",
                    Formatting.GOLD
                ));
            
            // ARMOR TRIMS - ALL 18
            // 1. COAST ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Coast Armor Trim",
                    "Shipwreck treasure chests (16.7% chance)",
                    "7 Diamonds + 1 Cobblestone",
                    "Apply with any material for nautical barnacle-like patterns",
                    "Weathered by endless tides, this pattern holds the memory of brave sailors who faced the ocean's fury.",
                    "Common",
                    Formatting.WHITE
                ));
            
            // 2. DUNE ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Dune Armor Trim",
                    "Desert Temple treasure chamber (12.9% chance)",
                    "7 Diamonds + 1 Sandstone",
                    "Apply with any material for desert wave-like patterns",
                    "Carved by ancient desert winds, these rippling patterns capture the eternal dance of sand dunes.",
                    "Common",
                    Formatting.WHITE
                ));
            
            // 3. EYE ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Eye Armor Trim",
                    "Stronghold library chests (23.3% chance)",
                    "7 Diamonds + 1 End Stone",
                    "Apply with any material for mystical watching eye patterns",
                    "The all-seeing patterns seem to shift when observed, as if the void itself peers back through the fabric of reality.",
                    "Rare",
                    Formatting.BLUE
                ));
            
            // 4. HOST ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Host Armor Trim",
                    "Trail Ruins suspicious gravel (8.3% chance)",
                    "7 Diamonds + 1 Terracotta",
                    "Apply with any material for noble hospitality-themed accents",
                    "Echoes of grand banquets and warm hearths, when strangers were welcomed as family and hospitality was sacred.",
                    "Uncommon",
                    Formatting.GREEN
                ));
            
            // 5. RAISER ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Raiser Armor Trim",
                    "Pillager Outpost chest (16.7% chance)",
                    "7 Diamonds + 1 Stone Bricks",
                    "Apply with any material for pillar-inspired patterns",
                    "These patterns mimic the rising towers and structures of those who seek to dominate the overworld.",
                    "Common",
                    Formatting.WHITE
                ));
            
            // 6. RIB ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Rib Armor Trim",
                    "Ancient City ice box (14.3% chance)",
                    "7 Diamonds + 1 Bone Block",
                    "Apply with any material for skeletal, rib-like patterns",
                    "These macabre patterns echo the final moments of ancient beings, preserving their form in eternity.",
                    "Rare",
                    Formatting.BLUE
                ));
            
            // 7. SENTRY ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Sentry Armor Trim",
                    "Elder Guardian drop (100% chance)",
                    "7 Diamonds + 1 Prismarine",
                    "Apply with any material for guardian-inspired eye patterns",
                    "The watchful eyes of the deep sea guardians, eternally vigilant even when forged into armor.",
                    "Rare",
                    Formatting.BLUE
                ));
            
            // 8. SHAPER ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Shaper Armor Trim",
                    "Woodland Mansion chests (14.3% chance)",
                    "7 Diamonds + 1 Cobblestone",
                    "Apply with any material for architect-inspired patterns",
                    "The essence of creation itself is captured in these geometric patterns, favored by master builders of old.",
                    "Rare",
                    Formatting.BLUE
                ));
            
            // 9. SILENCE ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Silence Armor Trim",
                    "Ancient City chest (11.1% chance)",
                    "7 Diamonds + 1 Sculk Shrieker",
                    "Apply with any material for wave dampening patterns",
                    "The Deep Dark hungers for sound, and these patterns seem to absorb light and noise like the Sculk itself.",
                    "Epic",
                    Formatting.LIGHT_PURPLE
                ));
            
            // 10. SNOUT ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Snout Armor Trim",
                    "Bastion Remnant chest (10% chance)",
                    "7 Diamonds + 1 Gold Block",
                    "Apply with any material for piglin-inspired snout patterns",
                    "The distinctive markings of piglins' most revered ancestors, a symbol of status in their society.",
                    "Epic",
                    Formatting.LIGHT_PURPLE
                ));
            
            // 11. SPIRE ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Spire Armor Trim",
                    "End City chest (14.3% chance)",
                    "7 Diamonds + 1 Purpur Block",
                    "Apply with any material for end-inspired spike patterns",
                    "The twisted spires of End Cities defy gravity and reason, just as these patterns defy conventional geometry.",
                    "Epic",
                    Formatting.LIGHT_PURPLE
                ));
            
            // 12. TIDE ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Tide Armor Trim",
                    "Ocean Monument elder guardian chamber (100% chance)",
                    "7 Diamonds + 1 Prismarine Brick",
                    "Apply with any material for ocean monument-inspired patterns",
                    "The ancient ocean monuments hold power over water that flows through these intricate carvings.",
                    "Epic",
                    Formatting.LIGHT_PURPLE
                ));
            
            // 13. VEX ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Vex Armor Trim",
                    "Woodland Mansion secret room (16.7% chance)",
                    "7 Diamonds + 1 Cobblestone",
                    "Apply with any material for wing-inspired patterns",
                    "The mischievous spirits of vexes leave their ethereal signature in these light, wing-like patterns.",
                    "Rare",
                    Formatting.BLUE
                ));
            
            // 14. WARD ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Ward Armor Trim",
                    "Ancient City chest (11.1% chance)",
                    "7 Diamonds + 1 Cobbled Deepslate",
                    "Apply with any material for warden-inspired patterns",
                    "These patterns contain ancient protective magic, wards against the heartbeat of the Deep Dark.",
                    "Epic",
                    Formatting.LIGHT_PURPLE
                ));
            
            // 15. WAYFINDER ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Wayfinder Armor Trim",
                    "Trail Ruins suspicious gravel (8.3% chance)",
                    "7 Diamonds + 1 Cobblestone",
                    "Apply with any material for compass-inspired directional patterns",
                    "Ancient travelers marked their paths with these symbols, guiding those who followed in their footsteps.",
                    "Uncommon",
                    Formatting.GREEN
                ));
            
            // 16. WILD ARMOR TRIM
            SMITHING_TEMPLATE_TOOLTIPS.put(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 
                new SmithingTemplateData(
                    "Wild Armor Trim",
                    "Jungle Temple chest (50% chance)",
                    "7 Diamonds + 1 Mossy Cobblestone",
                    "Apply with any material for jungle-inspired wild patterns",
                    "The untamed wilderness and chaotic growth of the jungle is captured in these entangled patterns.",
                    "Uncommon",
                    Formatting.GREEN
                ));
                
            // 17. BOLT ARMOR TRIM (1.21+)
            try {
                SMITHING_TEMPLATE_TOOLTIPS.put(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, 
                    new SmithingTemplateData(
                        "Bolt Armor Trim",
                        "Trial Chamber reward vaults (requires Trial Key)",
                        "7 Diamonds + 1 Copper Block",
                        "Apply with any material for electrifying circuitry patterns",
                        "Lightning crackles along these patterns, channeling the ancient electrical power hidden in Trial Chambers.",
                        "Epic",
                        Formatting.LIGHT_PURPLE
                    ));
            } catch (Exception e) {
                // Silently ignore if this item doesn't exist (older MC versions)
            }
            
            // 18. FLOW ARMOR TRIM (1.21+)
            try {
                SMITHING_TEMPLATE_TOOLTIPS.put(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, 
                    new SmithingTemplateData(
                        "Flow Armor Trim",
                        "Trial Chamber reward vaults",
                        "7 Diamonds + 1 Terracotta",
                        "Apply with any material for elegant flowing patterns",
                        "Time itself flows through these patterns like a river, preserving moments in eternal motion.",
                        "Uncommon",
                        Formatting.GREEN
                    ));
            } catch (Exception e) {
                // Silently ignore if this item doesn't exist (older MC versions)
            }
            
        } catch (Exception e) {
            ExampleMod.LOGGER.error("Error initializing smithing template data", e);
        }
    }
    
    /**
     * Get color based on rarity
     */
    private Formatting getRarityColor(String rarity) {
        return switch (rarity.toLowerCase()) {
            case "common" -> Formatting.WHITE;
            case "uncommon" -> Formatting.GREEN;
            case "rare" -> Formatting.BLUE;
            case "epic" -> Formatting.LIGHT_PURPLE;
            case "legendary" -> Formatting.GOLD;
            default -> Formatting.GRAY;
        };
    }
}