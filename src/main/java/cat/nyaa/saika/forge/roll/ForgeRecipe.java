package cat.nyaa.saika.forge.roll;

import cat.nyaa.saika.forge.ForgeElement;
import cat.nyaa.saika.forge.ForgeIron;

import java.util.Objects;

public class ForgeRecipe {
    public static ForgeRecipe INVALID = new ForgeRecipe(null, Integer.MIN_VALUE, null, Integer.MIN_VALUE);
    ForgeElement element;
    int elementAmount;
    ForgeIron ironLevel;
    int ironAmount;

    public ForgeRecipe(ForgeElement element, int elementAmount, ForgeIron ironLevel, int levelAmount){
        this.element = element;
        this.elementAmount = elementAmount;
        this.ironLevel = ironLevel;
        this.ironAmount = levelAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForgeRecipe that = (ForgeRecipe) o;
        return elementAmount == that.elementAmount &&
                ironAmount == that.ironAmount &&
                element.equals(that.element) &&
                ironLevel.equals(that.ironLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, elementAmount, ironLevel, ironAmount);
    }

    public ForgeElement getElement() {
        return element;
    }

    public int getElementAmount() {
        return elementAmount;
    }

    public ForgeIron getIronLevel() {
        return ironLevel;
    }

    public int getIronAmount() {
        return ironAmount;
    }
}
