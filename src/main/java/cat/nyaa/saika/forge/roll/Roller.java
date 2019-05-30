package cat.nyaa.saika.forge.roll;

import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ForgeableItem;

import java.util.*;

public class Roller {
    private ForgeManager forgeManager;
    Map<ForgeRecipe, List<ForgeableItem>> forgeCache = new WeakHashMap<>();

    public Roller(ForgeManager forgeManager) {
        this.forgeManager = forgeManager;
    }

    private Random random = new Random(System.currentTimeMillis());

    public ForgeableItem rollItem(ForgeRecipe recipe) {
        ForgeableItem item = null;
        //查缓存有没有对应配方的合成列表，没有就重查
        List<ForgeableItem> forgeableItems = forgeCache.get(recipe);
        if (forgeableItems == null) {
            forgeableItems = getForgePool(recipe);
        }
        //计算列表内所有weight的和
        forgeableItems.sort(Comparator.comparingInt(ForgeableItem::getWeight).reversed());
        ForgeableItem maxCostItem = forgeableItems.stream().max(Comparator.comparingInt(ForgeableItem::getMinCost)).orElse(null);
        int[] ints = forgeableItems.stream().mapToInt(ForgeableItem::getWeight).toArray();
        int newWeight = Math.toIntExact(
                Math.round(((double) maxCostItem.getWeight()) * (((double) recipe.getIronAmount()) / ((double) forgeableItems.get(0).getMinCost())))
        );
        int i = forgeableItems.indexOf(maxCostItem);
        ints[i] = newWeight;
        int totalWeight = Arrays.stream(ints).sum();
        //生成随机数，范围[0-totalWeight) int
        int randomResult = random.nextInt(totalWeight);
        PrimitiveIterator.OfInt iterator = Arrays.stream(ints).iterator();
        int count = 0;
        int selectedItem = 0;
        //顺序遍历列表
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            //count每一次迭代增加对应item的weight
            int nextCount = count + next;
            //如果落在item的weight区间则返回该物品
            if (count <= randomResult && nextCount > randomResult) {
                return forgeableItems.get(selectedItem);
            }
            count = nextCount;
            selectedItem++;
        }
        item = forgeableItems.get(forgeableItems.size() - 1);
        return item;
    }

    public boolean hasForgeableItem(ForgeRecipe recipe) {
        return !getForgePool(recipe).isEmpty();
    }

    private List<ForgeableItem> getForgePool(ForgeRecipe recipe) {
        List<ForgeableItem> itemByRecipie = forgeManager.getItemsByRecipie(recipe);
        forgeCache.put(recipe, itemByRecipie);
        return itemByRecipie;
    }

    public void onItemMapUpdate() {
        forgeCache.clear();
    }
}
