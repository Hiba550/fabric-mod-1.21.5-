package com.example.mixin.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.ExampleModClient;

@Mixin(ItemStack.class)
public class SmithingTemplateItemMixin {
    
    // Map to store clean display names for smithing templates
    private static final Map<String, String> CLEAN_NAMES = new HashMap<>();
    
    static {
        // Initialize clean display names for all smithing templates
        CLEAN_NAMES.put("bolt_armor_trim", "Bolt Armor Trim");
        CLEAN_NAMES.put("coast_armor_trim", "Coast Armor Trim");
        CLEAN_NAMES.put("dune_armor_trim", "Dune Armor Trim");
        CLEAN_NAMES.put("eye_armor_trim", "Eye Armor Trim");
        CLEAN_NAMES.put("flow_armor_trim", "Flow Armor Trim");
        CLEAN_NAMES.put("host_armor_trim", "Host Armor Trim");
        CLEAN_NAMES.put("raiser_armor_trim", "Raiser Armor Trim");
        CLEAN_NAMES.put("rib_armor_trim", "Rib Armor Trim");
        CLEAN_NAMES.put("sentry_armor_trim", "Sentry Armor Trim");
        CLEAN_NAMES.put("shaper_armor_trim", "Shaper Armor Trim");
        CLEAN_NAMES.put("silence_armor_trim", "Silence Armor Trim");
        CLEAN_NAMES.put("snout_armor_trim", "Snout Armor Trim");
        CLEAN_NAMES.put("spire_armor_trim", "Spire Armor Trim");
        CLEAN_NAMES.put("tide_armor_trim", "Tide Armor Trim");
        CLEAN_NAMES.put("vex_armor_trim", "Vex Armor Trim");
        CLEAN_NAMES.put("ward_armor_trim", "Ward Armor Trim");
        CLEAN_NAMES.put("wayfinder_armor_trim", "Wayfinder Armor Trim");
        CLEAN_NAMES.put("wild_armor_trim", "Wild Armor Trim");
        CLEAN_NAMES.put("netherite_upgrade", "Netherite Upgrade");
    }
    
    /**
     * Overrides getName() for smithing template items to ensure they display properly
     * This fixes the item name in the inventory view
     */
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    public void getName(CallbackInfoReturnable<Text> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        Item item = stack.getItem();
        
        // Only process smithing template items
        if (item instanceof SmithingTemplateItem) {
            String itemId = Registries.ITEM.getId(item).getPath();
            
            // Extract the template name without the _smithing_template suffix
            for (Map.Entry<String, String> entry : CLEAN_NAMES.entrySet()) {
                if (itemId.contains(entry.getKey())) {
                    // Return the clean name instead of the default one
                    cir.setReturnValue(Text.literal(entry.getValue()));
                    return;
                }
            }
        }
    }
}