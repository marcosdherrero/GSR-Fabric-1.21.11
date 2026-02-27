package net.berkle.groupspeedrun.gui.components;

import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * Supplies dropdown data and display from a screen model.
 * Generic so Run Manager, Run History, and other screens can use the same dropdown component.
 *
 * @param <M> Screen model type.
 */
public interface GSRMultiSelectDropdownBehavior<M> {

    /** Items shown in the dropdown list. */
    List<String> getItems(M model);

    /** Indices of selected items (multi-select: any subset; single-select: at most one). */
    Set<Integer> getSelectedIndices(M model);

    /** Text shown on the trigger button. */
    String getDisplayLabel(M model);

    /** When selection was last confirmed (ms), for ticker "active after select" window. */
    long getSelectionTimeMs(M model);

    /** Optional item icon for list row at index. Return empty stack for text-only row. */
    default ItemStack getItemIcon(M model, int index) {
        return ItemStack.EMPTY;
    }

    /** Optional ARGB tint overlay for item icon at index. When non-null, a color mask is drawn over the icon. */
    default Integer getItemIconTint(M model, int index) {
        return null;
    }

    /** Optional ARGB color for the trigger button label. When non-null, label is drawn in this color. */
    default Integer getDisplayLabelColor(M model) {
        return null;
    }
}
