package cat.nyaa.saika;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.saika.forge.EnchantSource.EnchantmentType;
import cat.nyaa.saika.forge.*;
import cat.nyaa.saika.forge.ForgeManager.NbtExistException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class Commands extends CommandReceiver {
    private static final String PERMISSION_ADMIN = "saika.admin";
    private static final String PERMISSION_OPEN = "saika.open";
    private static final String PERMISSION_LIST = "saika.list";

    Saika plugin;

    public Commands(Saika plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand("reload")
    public void onReload(CommandSender sender, Arguments arguments) {
        plugin.reload();
    }

    @SubCommand("define")
    public void onDefine(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "define")) {
            return;
        }
        if (!(sender instanceof Player)) {
            new Message(I18n.format("error.not_player"))
                    .send(sender);
            return;
        }
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
        if (itemInMainHand.getType().equals(Material.AIR)) {
            new Message(I18n.format("define.error.no_item"))
                    .send(sender);
            return;
        }
        ItemStack is = itemInMainHand.clone();
        is.setAmount(1);
        String defineTarget = arguments.nextString();
        try {
            switch (defineTarget) {
                case "level":
                    String level = arguments.nextString();
                    int elementCost = arguments.nextInt();
                    ForgeIron forgeIron = forgeManager.defineForgeIron(is, level, elementCost);
                    new Message(I18n.format("define.success.iron", forgeIron.getId()))
                            .send(sender);
                    break;
                case "element":
                    String element = arguments.nextString();
                    ForgeElement forgeElement = forgeManager.defineElement(element, is);
                    new Message(I18n.format("define.success.element", forgeElement.getId()));
                    break;
                case "recycle":
                    ForgeRecycler recycler = forgeManager.defineRecycler(is);
                    new Message(I18n.format("define.success.recycler", recycler.getId()))
                            .send(sender);
                    break;
                case "repulse":
                    ForgeEnchantBook repulse = forgeManager.defineEnchant(is, EnchantmentType.REPULSE);
                    new Message(I18n.format("define.success.repulse", repulse.getId()))
                            .send(sender);
                    break;
                case "enchant":
                    ForgeEnchantBook enchant = forgeManager.defineEnchant(is, EnchantmentType.ENCHANT);
                    new Message(I18n.format("define.success.enchant", enchant.getId()))
                            .send(sender);
                    break;
                default:
                    new Message(I18n.format("define.error.unknown"))
                            .send(sender);
            }
        } catch (NbtExistException e) {
            new Message(I18n.format("define.error.nbt_exists"))
                    .send(sender);
        } catch (ForgeManager.InvalidEnchantSourceException e) {
            new Message(I18n.format("define.error.invalid_enchant_source"))
                    .send(sender);
        }
    }

    @SubCommand("delete")
    public void onDelete(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "delete")) {
            return;
        }
        String type = arguments.nextString();
        String id = arguments.nextString();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        switch (type) {
            case "level":
                if (forgeManager.deleteIron(id)) {
                    new Message(I18n.format("delete.success.level", id))
                            .send(sender);
                } else {
                    new Message(I18n.format("delete.error.level", id))
                            .send(sender);
                }
                break;
            case "element":
                if (forgeManager.deleteElement(id)) {
                    new Message(I18n.format("delete.success.element", id))
                            .send(sender);
                } else {
                    new Message(I18n.format("delete.error.element", id))
                            .send(sender);
                }
                break;
            case "enchant":
                if (forgeManager.deleteEnchant(id, EnchantmentType.ENCHANT)) {
                    new Message(I18n.format("delete.success.enchant", id))
                            .send(sender);
                } else {
                    new Message(I18n.format("delete.error.enchant", id))
                            .send(sender);
                }
                break;
            case "repulse":
                if (forgeManager.deleteEnchant(id, EnchantmentType.REPULSE)) {
                    new Message(I18n.format("delete.success.repulse", id))
                            .send(sender);
                } else {
                    new Message(I18n.format("delete.error.repulse", id))
                            .send(sender);
                }
                break;
            case "recycle":
                if (forgeManager.deleteRecycle(id)) {
                    new Message(I18n.format("delete.success.recycle", id))
                            .send(sender);
                } else {
                    new Message(I18n.format("delete.success.recycle", id))
                            .send(sender);
                }
                break;
            default:
                new Message(I18n.format("delete.error.unknown"))
                        .send(sender);
                break;
        }
    }

    @SubCommand("modify")
    public void onModify(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "modify")) {
            return;
        }
        String id = arguments.nextString();
        String targetVal = arguments.nextString();
        String value = arguments.nextString();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);
        if (forgeableItem == null) {
            new Message(I18n.format("modify.error.no_item"))
                    .send(sender);
            return;
        }
        switch (targetVal) {
            case "level":
                forgeableItem.setLevel(value);
                break;
            case "element":
                forgeableItem.setElement(value);
                break;
            case "cost":
                forgeableItem.setMinCost(Integer.parseInt(value));
                break;
            case "weight":
                forgeableItem.setWeight(Integer.parseInt(value));
                break;
            case "recycle":
                int min = arguments.nextInt();
                int max = arguments.nextInt();
                int hard = arguments.nextInt();
                String bonus = arguments.nextString();
                double chance = arguments.nextDouble();
                forgeableItem.setRecycle(min, max, hard, bonus, chance);
                break;
            default:
                new Message(I18n.format("modify.error.unknown"))
                        .send(sender);
                return;
        }
        forgeManager.saveItem(id);
    }

    @SubCommand("inspect")
    public void onInspect(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "inspect")) {
            return;
        }
        String id = arguments.nextString();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);
        if (forgeableItem != null) {
            YamlConfiguration sec = new YamlConfiguration();
            forgeableItem.serialize(sec);
            String s = sec.saveToString();
            new Message(I18n.format("inspect.info", id, s))
                    .send(sender);
        } else {
            new Message(I18n.format("inspect.error.no_item"))
                    .send(sender);
        }
    }

    @SubCommand("list")
    public void onList(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_LIST, null)) {
            return;
        }

        String level = arguments.nextString();
        String element = arguments.nextString();

        ForgeManager forgeManager = ForgeManager.getForgeManager();
        YamlConfiguration s = forgeManager.listItem(level, element);
        String str = s.saveToString();
        if (str.equals("")) {
            new Message(I18n.format("list.error.no_result"))
                    .send(sender);
        } else {
            new Message(I18n.format("list.success"))
                    .send(sender);
        }
    }

    @SubCommand("bonus")
    public void onBonus(CommandSender sender, Arguments arguments){
        if (dontHavePermission(sender, PERMISSION_ADMIN, "bonus")){
            return;
        }
        String action = arguments.nextString();
        switch (action){
            case "add":
                if (!(sender instanceof Player)){
                    new Message(I18n.format("error.not_player"))
                            .send(sender);
                    return;
                }
                ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)){
                    new Message(I18n.format("bonus.error.no_item"))
                            .send(sender);
                    return;
                }
                String s = ForgeManager.getForgeManager().addBonus(itemInMainHand);
                new Message(I18n.format("bonus.success",s))
                        .send(sender);
                break;
            case "set":
                break;
            default:
                new Message(I18n.format("bonus.error.unknown_action"))
                        .send(sender);
        }
    }

    @SubCommand("add")
    public void onAdd(CommandSender sender, Arguments arguments){
        if (dontHavePermission(sender, PERMISSION_ADMIN, "add")){
            return;
        }
        if (!(sender instanceof Player)) {
            new Message(I18n.format("error.not_player"))
                    .send(sender);
            return;
        }
        ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
        if (itemInMainHand.getType().equals(Material.AIR)){
            new Message(I18n.format("add.error.no_item"))
                    .send(sender);
            return;
        }
        String level = arguments.nextString();
        String element = arguments.nextString();
        int cost = arguments.nextInt();
        int weight = arguments.nextInt();

        ForgeManager forgeManager = ForgeManager.getForgeManager();
        try {
            forgeManager.addItem(itemInMainHand, level, element, cost, weight);
        } catch (NbtExistException e) {
            new Message(I18n.format("add.error.nbt_exists"))
                    .send(sender);
        }

    }

    @SubCommand("remove")
    public void onRemove(CommandSender sender, Arguments arguments){
        if (dontHavePermission(sender, PERMISSION_ADMIN, "remove")){
            return;
        }
        String id = arguments.nextString();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        if (forgeManager.hasItem(id)){
            forgeManager.removeForgeableItem(id);
            new Message(I18n.format("remove.success",id));
        }else {
            new Message(I18n.format("remove.error.no_item"))
                    .send(sender);
        }
    }

    private boolean dontHavePermission(CommandSender sender, String mainPermission, String subPermission) {
        if (sender.isOp()) {
            return false;
        }
        String permission = subPermission == null ? mainPermission : mainPermission + "." + subPermission;
        if (!sender.hasPermission(permission)) {
            new Message(I18n.format("error.permission", permission))
                    .send(sender);
            return true;
        }
        return false;
    }
}
