package buildcraft.api.enums;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import buildcraft.api.items.IBlueprintItem;

public enum EnumBlueprintType implements IStringSerializable {
    NONE,
    BLUEPRINT,
    TEMPLATE;

    public final String lowerCaseName = name().toLowerCase(Locale.ROOT);

    public static EnumBlueprintType valueOf(int index) {
        if (index <= 0 || index >= values().length) {
            return NONE;
        } else {
            return values()[index];
        }
    }

    public static EnumBlueprintType getType(ItemStack item) {
        if (item == null) {
            return NONE;
        } else if (item.getItem() instanceof IBlueprintItem) {
            return ((IBlueprintItem) item.getItem()).getType(item);
        } else {
            return NONE;
        }
    }

    @Override
    public String getName() {
        return lowerCaseName;
    }
}
