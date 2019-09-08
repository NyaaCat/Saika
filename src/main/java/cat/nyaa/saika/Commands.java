package cat.nyaa.saika;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.Pair;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.nyaacore.utils.LocaleUtils;
import cat.nyaa.saika.forge.*;
import cat.nyaa.saika.forge.EnchantSource.EnchantmentType;
import cat.nyaa.saika.forge.ForgeManager.NbtExistException;
import cat.nyaa.saika.forge.roll.ForgeRecipe;
import cat.nyaa.saika.forge.ui.EnchantUi;
import cat.nyaa.saika.forge.ui.ForgeUi;
import cat.nyaa.saika.forge.ui.RecycleUi;
import cat.nyaa.saika.forge.ui.RepulseUi;
import cat.nyaa.saika.log.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Commands extends CommandReceiver {
    private static final String PERMISSION_ADMIN = "saika.admin";
    private static final String PERMISSION_OPEN = "saika.open";
    private static final String PERMISSION_LIST = "saika.list";

    Saika plugin;
    SaikaCommandCompleter commandCompleter;

    class SaikaCommandCompleter {

        private List<String> comleteRoll(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            String cmd = arguments[arguments.length - 1];
            switch (arguments.length) {
                case 2:
                    str.addAll(getElements());
                    break;
                case 3:
                    str.add("elementAmount");
                    break;
                case 4:
                    str.addAll(getLevels());
                    break;
                case 5:
                    str.add("levelAmount");
                    break;
                case 6:
                    str.add("true");
                    str.add("false");
                    break;
                default:
                    break;
            }
            List<String> collect = str.stream().filter(s -> s.startsWith(cmd)).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                str = collect;
            }
            return str;
        }

        private List<String> comleteAutoRoll(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            String cmd = arguments[arguments.length - 1];
            switch (arguments.length) {
                case 2:
                    str.addAll(getLevels());
                    break;
                case 3:
                    str.add("elementAmount");
                    break;
                case 4:
                    str.addAll(getElements());
                    break;
                case 5:
                    str.add("levelAmount");
                    break;
                case 6:
                    str.add("rollTimes");
                    break;
                default:
                    break;
            }
            List<String> collect = str.stream().filter(s -> s.startsWith(cmd)).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                str = collect;
            }
            return str;
        }

        List<String> completeOpen(String subCommand, String[] arguments) {
            if (arguments.length > 2) {
                return new ArrayList<>();
            }
            return open;
        }

        List<String> completeAdd(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            switch (arguments.length) {
                case 2:
                    str = getLevels();
                    break;
                case 3:
                    str = getElements();
                    break;
                case 4:
                    str.add("consumption");
                    break;
                case 5:
                    str.add("weight");
                    break;
            }
            return str;
        }

        List<String> completeDefine(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            if (arguments.length < 2) {
                return define;
            }
            String top = arguments[1];
            if (top == null || top.equals("")) {
                return define;
            }
            switch (top) {
                case "level":
                    if (arguments.length < 3 || arguments[2].equals("")) {
                        str.add("level");
                    } else {
                        str.add("element-consumption");
                    }
                    break;
                case "element":
                    str.add("element");
                    break;
                case "enchant":
                    break;
                case "repulse":
                    break;
                default:
                    break;
            }
            return str;
        }

        List<String> completeDelete(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            if (arguments.length == 2) {
                return define;
            } else if (arguments.length == 3) {
                str.add("id");
            }
            return str;
        }

        List<String> completeRemove(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            str.add("id");
            return str;
        }

        List<String> completeModify(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            switch (arguments.length) {
                case 2:
                    ForgeManager forgeManager = ForgeManager.getForgeManager();
                    List<String> itemList = forgeManager.getItemList();
                    if (arguments[1] != null) {
                        itemList = itemList.stream().filter(s -> s.startsWith(arguments[1])).collect(Collectors.toList());
                    }
                    str.addAll(itemList);
                    break;
                case 3:
                    str = new ArrayList<>(define);
                    str.add("item");
                    break;
                case 4:
                    if (arguments[2] == null || !arguments[2].equalsIgnoreCase("item")) {
                        str.add("value");
                    }
                    break;
                default:
                    break;
            }
            return str;
        }

        List<String> completeBonus(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            switch (arguments.length) {
                case 2:
                    str.add("add");
                    str.add("set");
                    break;
                case 3:
                    str.add("forge");
                    str.add("recycle");
                    break;
                case 4:
                    str.add("forge id");
                    break;
                case 5:
                    str.add("bonus id");
                    break;
                case 6:
                    str.add("probability");
                    break;
                default:
                    break;
            }
            return str;
        }

        List<String> completeInspect(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            ForgeManager forgeManager = ForgeManager.getForgeManager();
            if (arguments.length == 2) {
                str = forgeManager.getItemList();
            }
            return str;
        }

        List<String> completeList(String subCommand, String[] arguments) {
            List<String> str = new ArrayList<>();
            switch (arguments.length) {
                case 2:
                    str = getLevels();
                    break;
                case 3:
                    str = getElements();
                    break;
            }
            return str;
        }
    }

    public Commands(Saika plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
        commandCompleter = new SaikaCommandCompleter();
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "reload")
    public void onReload(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "")) {
            return;
        }
        plugin.reload();
        new Message(I18n.format("reload.success"))
                .send(sender);
    }

    @SubCommand(value = "define", tabCompleter = "defineCompleter")
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
                            .append(" {itemName}", forgeIron.getItemStack())
                            .send(sender);
                    break;
                case "element":
                    String element = arguments.nextString();
                    ForgeElement forgeElement = forgeManager.defineElement(element, is);
                    new Message(I18n.format("define.success.element", forgeElement.getId()))
                            .append(" {itemName}", forgeElement.getItemStack())
                            .send(sender);
                    break;
                case "recycle":
                    ForgeRecycler recycler = forgeManager.defineRecycler(is);
                    new Message(I18n.format("define.success.recycler", recycler.getId()))
                            .append(" {itemName}", recycler.getItemStack())
                            .send(sender);
                    break;
                case "repulse":
                    ForgeRepulse repulse = forgeManager.defineRepulse(is);
                    new Message(I18n.format("define.success.repulse", repulse.getId()))
                            .append(" {itemName}", repulse.getItemStack())
                            .send(sender);
                    break;
                case "enchant":
                    ForgeEnchantBook enchant = forgeManager.defineEnchant(is, EnchantmentType.ENCHANT);
                    new Message(I18n.format("define.success.enchant", enchant.getId()))
                            .append(" {itemName}", enchant.getItemStack())
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

    @SubCommand(value = "delete", tabCompleter = "deleteCompleter")
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
                    new Message(I18n.format("delete.error.recycle", id))
                            .send(sender);
                }
                break;
            default:
                new Message(I18n.format("delete.error.unknown"))
                        .send(sender);
                break;
        }
    }

    @SubCommand(value = "modify", tabCompleter = "modifyCompleter")
    public void onModify(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "modify")) {
            return;
        }
        String id = arguments.nextString();
        String targetVal = arguments.nextString();
        String value = arguments.top();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);
        if (forgeableItem == null) {
            new Message(I18n.format("modify.error.no_item", id))
                    .send(sender);
            return;
        }
        switch (targetVal) {
            case "level":
                Objects.requireNonNull(value);
                forgeableItem.setLevel(value);
                new Message("").append(I18n.format("modify.success.level", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "element":
                Objects.requireNonNull(value);
                forgeableItem.setElement(value);
                new Message("").append(I18n.format("modify.success.element", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "cost":
                Objects.requireNonNull(value);
                forgeableItem.setMinCost(Integer.parseInt(value));
                new Message("").append(I18n.format("modify.success.cost", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "weight":
                Objects.requireNonNull(value);
                forgeableItem.setWeight(Integer.parseInt(value));
                new Message("").append(I18n.format("modify.success.weight", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "recycle":
                Objects.requireNonNull(value);
                String action = arguments.nextString();
                ForgeableItem.RecycleInfo recycle = forgeableItem.getRecycle();
                switch (action) {
                    case "min":
                        int min = Integer.parseInt(value);
                        forgeableItem.setRecycle(min, recycle.max, recycle.hard, recycle.bonus.item, recycle.bonus.chance);
                        new Message("").append(I18n.format("modify.success.recycle.min", min), forgeableItem.getItemStack())
                                .send(sender);
                        break;
                    case "max":
                        int max = Integer.parseInt(value);
                        forgeableItem.setRecycle(recycle.min, max, recycle.hard, recycle.bonus.item, recycle.bonus.chance);
                        new Message("").append(I18n.format("modify.success.recycle.max", max), forgeableItem.getItemStack())
                                .send(sender);
                        break;
                    case "hard":
                        int hard = Integer.parseInt(value);
                        forgeableItem.setRecycle(recycle.min, recycle.max, hard, recycle.bonus.item, recycle.bonus.chance);
                        new Message("").append(I18n.format("modify.success.recycle.hard", hard), forgeableItem.getItemStack())
                                .send(sender);
                        break;
                    case "bonus":
                        String bonus = arguments.nextString();
                        if (bonus.equals("-1")) {
                            forgeableItem.setRecycle(recycle.min, recycle.max, recycle.hard, "", recycle.bonus.chance);
                            new Message("").append(I18n.format("modify.success.recycle.no_bonus"), forgeableItem.getItemStack())
                                    .send(sender);
                            return;
                        }
                        BonusItem bonusItem = forgeManager.getBonus(bonus);
                        if (bonusItem == null) {
                            new Message(I18n.format("modify.error.recycle", bonus))
                                    .send(sender);
                            return;
                        }
                        forgeableItem.setRecycle(recycle.min, recycle.max, recycle.hard, bonus, recycle.bonus.chance);
                        new Message("").append(I18n.format("modify.success.recycle.bonus", bonus), forgeableItem.getItemStack())
                                .send(sender);
                        break;
                    case "chance":
                        double chance = arguments.nextDouble();
                        forgeableItem.setRecycle(recycle.min, recycle.max, recycle.hard, recycle.bonus.item, chance);
                        new Message("").append(I18n.format("modify.success.recycle.chance", chance), forgeableItem.getItemStack())
                                .send(sender);
                        break;
                }
                break;
            case "item":
                if (!(sender instanceof Player)) {
                    new Message(I18n.format("error.not_player"))
                            .send(sender);
                    return;
                }
                ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)) {
                    new Message(I18n.format("modify.error.no_item"))
                            .send(sender);
                    return;
                }
                forgeableItem.setItem(itemInMainHand);
                new Message("").append(I18n.format("modify.success.item", id), itemInMainHand)
                        .send(sender);
                break;
            default:
                new Message(I18n.format("modify.error.unknown"))
                        .send(sender);
                return;
        }
        forgeManager.saveItem(id);
    }

    @SubCommand(value = "inspect", tabCompleter = "inspectCompleter")
    public void onInspect(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "inspect")) {
            return;
        }
        String id = arguments.nextString();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);
        if (forgeableItem != null) {
            Message message = new Message("").append(I18n.format("inspect.info", id, forgeableItem.getWeight()), forgeableItem.getItemStack());//&r{itemName}&r * {amount}
            sendItemInfo(sender, message, forgeManager, forgeableItem);
            message.send(sender);
        } else {
            new Message(I18n.format("inspect.error.no_item", id))
                    .send(sender);
        }
    }

    @SubCommand(value = "list", tabCompleter = "listCompleter")
    public void onList(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_LIST, null)) {
            return;
        }

        String level = arguments.nextString();
        String element = arguments.nextString();

        ForgeManager forgeManager = ForgeManager.getForgeManager();
        List<ForgeableItem> s = forgeManager.listItem(level, element);
        s.sort(Comparator.comparingInt(ForgeableItem::getMinCost));
        if (!s.isEmpty()) {
            new Message(I18n.format("list.success"))
                    .send(sender);
            s.forEach(forgeableItem -> {
                Message message = new Message("")
                        .append(I18n.format("list.info", forgeableItem.getId(), forgeableItem.getWeight()), forgeableItem.getItemStack());

                sendItemInfo(sender, message, forgeManager, forgeableItem);
                message.send(sender);
            });
        } else {
            new Message(I18n.format("list.error.no_result"))
                    .send(sender);
        }
    }

    private void sendItemInfo(CommandSender sender, Message message, ForgeManager forgeManager, ForgeableItem forgeableItem) {
        ForgeIron iron = forgeManager.getIron(forgeableItem.getLevel());
        ItemStack ironItem;
        if (iron == null) {
            ironItem = new ItemStack(Material.AIR);
        } else {
            ironItem = iron.getItemStack();
            ironItem.setAmount(forgeableItem.getMinCost());
        }
        message.append(I18n.format("list.iron"), ironItem);
        ForgeElement ele = forgeManager.getElement(forgeableItem.getElement());
        ItemStack elementItem;
        if (ele == null) {
            elementItem = new ItemStack(Material.AIR);
        } else {
            elementItem = ele.getItemStack();
        }
        message.append(I18n.format("list.element"), elementItem);
        ForgeableItem.Bonus forgeBonus = forgeableItem.getForgeBonus();
        if (!forgeBonus.item.equals("")) {
            BonusItem bonus = forgeManager.getBonus(forgeBonus.item);
            if (bonus != null) {
                ItemStack itemStack = ItemStackUtils.itemFromBase64(bonus.toNbt());
                message.append("\n").append(I18n.format("list.forge_bonus", forgeBonus.chance), itemStack);
            }
        }
        ForgeableItem.Bonus recycleBonus = forgeableItem.getRecycleBonus();
        if (!recycleBonus.item.equals("")) {
            try {
                BonusItem bonus = forgeManager.getBonus(recycleBonus.item);
                ItemStack itemStack = ItemStackUtils.itemFromBase64(bonus.toNbt());
                message.append("\n").append(I18n.format("list.recycle_bonus", recycleBonus.chance), itemStack);
            } catch (Exception e) {
            }
        }
    }

    Random random = new Random();

    @SubCommand(value = "bonus", tabCompleter = "bonusCompleter")
    public void onBonus(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "bonus")) {
            return;
        }
        String action = arguments.nextString();
        switch (action) {
            case "add":
                if (!(sender instanceof Player)) {
                    new Message(I18n.format("error.not_player"))
                            .send(sender);
                    return;
                }
                ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)) {
                    new Message(I18n.format("bonus.error.no_item"))
                            .send(sender);
                    return;
                }
                String s = ForgeManager.getForgeManager().addBonus(itemInMainHand);
                new Message(I18n.format("bonus.success", s))
                        .send(sender);
                break;
            case "set":
                String type = arguments.nextString();
                String id = arguments.nextString();
                String bonusId = arguments.nextString();
                int probability = arguments.nextInt();
                ForgeManager manager = ForgeManager.getForgeManager();
                if (!(sender instanceof Player)) {
                    new Message(I18n.format("error.not_player"))
                            .send(sender);
                    return;
                }
                BonusItem bonusItem = manager.getBonus(bonusId);
                if (bonusItem == null) {
                    new Message(I18n.format("bonus.set.no_item", bonusId))
                            .send(sender);
                    return;
                }
                ForgeableItem.Bonus bonus = new ForgeableItem.Bonus();
                bonus.item = bonusItem.getId();
                bonus.chance = probability;
                ForgeableItem forgeableItem = manager.getForgeableItem(id);
                switch (type) {
                    case "forge":
                        forgeableItem.setForgeBonus(bonus);
                        manager.saveItem(id);
                        new Message(I18n.format("bonus.set.forge", bonusId, id))
                                .send(sender);
                        break;
                    case "recycle":
                        forgeableItem.setRecycleBonus(bonus);
                        manager.saveItem(id);
                        new Message(I18n.format("bonus.set.recycle", bonusId, id))
                                .send(sender);
                        break;
                    default:
                        new Message("bonus.set.failed")
                                .send(sender);
                        return;
                }
                break;
            default:
                new Message(I18n.format("bonus.error.unknown_action", action))
                        .send(sender);
        }
    }

    @SubCommand(value = "add", tabCompleter = "addCompleter")
    public void onAdd(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "add")) {
            return;
        }
        if (!(sender instanceof Player)) {
            new Message(I18n.format("error.not_player"))
                    .send(sender);
            return;
        }
        ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
        if (itemInMainHand.getType().equals(Material.AIR)) {
            new Message(I18n.format("add.error.no_item"))
                    .send(sender);
            return;
        }
        String level = arguments.nextString();
        String element = arguments.nextString();
        int cost = arguments.nextInt();
        int weight = arguments.nextInt();

        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeableItem item = forgeManager.addItem(itemInMainHand, level, element, cost, weight);
        ((Player) sender).getInventory().setItemInMainHand(item.getItemStack());
        new Message("").append(I18n.format("add.success", item.getId()), item.getItemStack())
                .send(sender);
    }

    @SubCommand(value = "remove", tabCompleter = "removeCompleter")
    public void onRemove(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "remove")) {
            return;
        }
        String id = arguments.nextString();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        if (forgeManager.hasItem(id)) {
            ForgeableItem item = forgeManager.removeForgeableItem(id);
            if (item == null) {
                new Message(I18n.format("remove.error.no_item", id))
                        .send(sender);
                return;
            }
            new Message(I18n.format("remove.success", id))
                    .append("", item.getItemStack())
                    .send(sender);
        } else {
            new Message(I18n.format("remove.error.no_item", id))
                    .send(sender);
        }
    }

    @SubCommand(value = "open", tabCompleter = "openCompleter")
    public boolean onOpen(CommandSender sender, Arguments arguments) {
        String action = arguments.nextString();
        if (!action.equals("forge") && !action.equals("enchant") && !action.equals("repulse") && !action.equals("recycle")) {
            return false;
        }
        if (dontHavePermission(sender, PERMISSION_OPEN, action)) {
            return false;
        }
        if (!(sender instanceof Player)) {
            new Message(I18n.format("error.not_player"))
                    .send(sender);
            return false;
        }
        if (!checkRequiredBlock((Player) sender, action)) {
            return false;
        }
        switch (action) {
            case "forge":
                ForgeUi forgeUi = new ForgeUi();
                forgeUi.openInventory((Player) sender);
                break;
            case "enchant":
                EnchantUi enchantUi = new EnchantUi();
                enchantUi.openInventory((Player) sender);
                break;
            case "repulse":
                RepulseUi repulseUi = new RepulseUi();
                repulseUi.openInventory((Player) sender);
                break;
            case "recycle":
                RecycleUi recycleUi = new RecycleUi();
                recycleUi.openInventory((Player) sender);
                break;
            default:
                break;
        }
        return true;
    }

    @SubCommand(value = "consoleRoll", tabCompleter = "consoleRollCompleter")
    public void onAutoRoll(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "")) {
            return;
        }
        String element = arguments.nextString();
        int elementAmount = arguments.nextInt();
        String iron = arguments.nextString();
        int ironAmount = arguments.nextInt();
        int rollTimes = arguments.nextInt();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeIron forgeIron = forgeManager.getIron(iron);
        ForgeElement forgeElement = forgeManager.getElement(element);
        ForgeRecipe recipe = new ForgeRecipe(forgeElement, elementAmount, forgeIron, ironAmount);
        boolean b = forgeManager.hasItemOfRecipe(recipe);
        for (int i = 0; i < rollTimes; i++) {
            ForgeableItem forgeableItem = forgeManager.forgeItem(recipe);
            if (forgeableItem != null) {
                ItemStack itemStack = forgeableItem.getItemStack().clone();
                String playerName = sender.getName();
                Logger.logForge(forgeableItem, playerName, recipe);
            }
        }
    }

    @SubCommand(value = "roll", permission = "saika.roll", tabCompleter = "rollCompleter")
    public void onRoll(CommandSender sender, Arguments arguments) {
        String element = arguments.nextString();
        int elementNum = arguments.nextInt();
        String iron = arguments.nextString();
        int ironNum = arguments.nextInt();

        if (!(sender instanceof Player)) {
            new Message(I18n.format("error.not_player"))
                    .send(sender);
            return;
        }

        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeIron forgeIron = forgeManager.getIron(iron);
        ForgeElement forgeElement = forgeManager.getElement(element);
        if (forgeIron == null) {
            new Message("").append(I18n.format("roll.error.no_iron"))
                    .send(sender);
            return;
        }
        if (forgeElement == null) {
            new Message("").append(I18n.format("roll.error.no_element"))
                    .send(sender);
            return;
        }
        ForgeRecipe forgeRecipe = new ForgeRecipe(forgeElement, elementNum, forgeIron, ironNum);
        String canRecycle = arguments.top();
        int forgeTimes = 1;
        try {
            arguments.next();
            String top = arguments.top();
            if (top != null && !"".equals(top)) {
                forgeTimes = Integer.parseInt(top);
            }
        } catch (NumberFormatException e) {}
        if (!forgeManager.hasItemOfRecipe(forgeRecipe)) {
            new Message("").append(I18n.format("roll.error.no_recipe"))
                    .send(sender);
            return;
        }
        for (int i = 0; i < forgeTimes; i++) {
            ForgeableItem forgeableItem = forgeManager.forgeItem(forgeRecipe);
            ItemStack itemStack = forgeableItem.getItemStack();
            if (canRecycle != null && canRecycle.equals("true")) {
                ForgeableItem.addItemTag(itemStack, forgeableItem.getId());
                ForgeableItem.addCostTagTo(itemStack, ironNum);
            }
            if (!InventoryUtils.addItem(((Player) sender), itemStack)) {
                ((Player) sender).getWorld().dropItem(((Player) sender).getLocation(), forgeableItem.getItemStack());
            }
            if (sender.isOp()) {
                Message mess = new Message("").append(I18n.format("roll.success"), forgeableItem.getItemStack());
                mess.send(sender);
                Logger.logForge(forgeableItem, sender.getName(), forgeRecipe);
            }
        }
    }

    private boolean checkRequiredBlock(Player sender, String action) {
        Material forgeBlock;
        int forgeUiDistance;
        switch (action) {
            case "forge":
                forgeBlock = plugin.getConfigure().getForgeBlock();
                forgeUiDistance = plugin.getConfigure().getForgeUiDistance();
                break;
            case "enchant":
                forgeBlock = plugin.getConfigure().getEnchantBlock();
                forgeUiDistance = plugin.getConfigure().getEnchantUiDistance();
                break;
            case "repulse":
                forgeBlock = plugin.getConfigure().getEnchantBlock();
                forgeUiDistance = plugin.getConfigure().getEnchantUiDistance();
                break;
            case "recycle":
                forgeBlock = plugin.getConfigure().getForgeBlock();
                forgeUiDistance = plugin.getConfigure().getEnchantUiDistance();
                break;
            default:
                return false;
        }
        boolean match = true;
        if (forgeBlock != Material.AIR) {
            Location location = sender.getLocation();
            List<Location> nearbyBlock = IntStream.rangeClosed(-forgeUiDistance, forgeUiDistance)
                    .parallel()
                    .boxed()
                    .flatMap(x ->
                            IntStream.rangeClosed(-forgeUiDistance, forgeUiDistance)
                                    .parallel()
                                    .boxed()
                                    .map(y -> Pair.of(x, y))
                    )
                    .flatMap(p ->
                            IntStream.rangeClosed(-forgeUiDistance, forgeUiDistance)
                                    .parallel()
                                    .boxed()
                                    .map(z -> location.clone().add(p.getKey(), p.getValue(), z))
                    ).collect(Collectors.toList());
            if (nearbyBlock.stream().anyMatch(loc -> loc.getBlock().getType().equals(forgeBlock))) match = true;
            else match = false;
            if (!match) {
                new Message("")
                        .append(I18n.format("open.error.no_required_block", forgeUiDistance), Collections.singletonMap("{block}", LocaleUtils.getNameComponent(new ItemStack(forgeBlock))))
                        .send(sender);
            }
        }
        return match;
    }

    private List<String> open = Arrays.asList("forge", "enchant", "repulse", "recycle");
    private List<String> define = Arrays.asList("level", "element", "enchant", "repulse", "recycle");

    public List<String> defineCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        String target = arguments.nextString();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(define);
                break;
            case 3:
                if ("level".equals(target) || "element".equals(target)) {
                    completeStr.add("id");
                }
                break;
            case 4:
                if (target.equals("level")) {
                    completeStr.add("element cost");
                }
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> deleteCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(define);
                break;
            case 3:
                String target = arguments.nextString();
                ForgeManager forgeManager = ForgeManager.getForgeManager();
                switch (target) {
                    case "level":
                        completeStr.addAll(forgeManager.getIronList());
                        break;
                    case "element":
                        completeStr.addAll(forgeManager.getElementList());
                        break;
                    case "enchant":
                        completeStr.addAll(forgeManager.getEnchantBookIds());
                        break;
                    case "repulse":
                        completeStr.addAll(forgeManager.getRepulseIds());
                        break;
                    case "recycle":
                        completeStr.addAll(forgeManager.getRecycleIds());
                        break;
                    default:
                        break;
                }
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> modifyCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(forgeManager.getForgeableItemList());
                break;
            case 3:
                completeStr.add("level");
                completeStr.add("element");
                completeStr.add("cost");
                completeStr.add("weight");
                completeStr.add("recycle");
                completeStr.add("item");
                break;
            case 4:
                arguments.next();
                String target = arguments.next();
                switch (target) {
                    case "level":
                        completeStr.addAll(getLevels());
                        break;
                    case "element":
                        completeStr.addAll(getElements());
                        break;
                    case "recycle":
                        completeStr.addAll(getRecycles());
                        break;
                    default:
                        break;
                }
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> inspectCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                ForgeManager forgeManager = ForgeManager.getForgeManager();
                completeStr.addAll(forgeManager.getForgeableItemList());
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> listCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(getLevels());
                break;
            case 3:
                completeStr.addAll(getElements());
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> bonusCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                completeStr.add("add");
                completeStr.add("set");
                break;
            case 3:
                String target = arguments.nextString();
                if ("set".equals(target)) {
                    completeStr.add("forge");
                    completeStr.add("recycle");
                }
                break;
            case 4:
                String target1 = arguments.nextString();
                if ("set".equals(target1)) {
                    ForgeManager forgeManager = ForgeManager.getForgeManager();
                    completeStr.addAll(forgeManager.getForgeableItemList());
                }
                break;
            case 5:
                String target2 = arguments.nextString();
                if ("set".equals(target2)) {
                    ForgeManager forgeManager = ForgeManager.getForgeManager();
                    completeStr.addAll(forgeManager.getBonusIds());
                }
                break;
            case 6:
                String target3 = arguments.nextString();
                if ("set".equals(target3)) {
                completeStr.add("probability");
            }
            break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> addCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(getLevels());
                break;
            case 3:
                completeStr.addAll(getElements());
                break;
            case 4:
                completeStr.add("cost");
                break;
            case 5:
                completeStr.add("weight");
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                ForgeManager forgeManager = ForgeManager.getForgeManager();
                completeStr.addAll(forgeManager.getForgeableItemList());
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> openCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(open);
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> rollCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(getElements());
                break;
            case 3:
                completeStr.add("element amount");
                break;
            case 4:
                completeStr.addAll(getLevels());
                break;
            case 5:
                completeStr.add("material amount");
                break;
            case 6:
                completeStr.add("can recycle");
                completeStr.add("true");
                completeStr.add("false");
                break;
            case 7:
                completeStr.add("roll times");
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> consoleRollCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                completeStr.addAll(getElements());
                break;
            case 3:
                completeStr.add("element amount");
                break;
            case 4:
                completeStr.addAll(getLevels());
                break;
            case 5:
                completeStr.add("material amount");
                break;
            case 6:
                completeStr.add("roll times");
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> sampleCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.length()) {
            case 2:
                break;
        }
        return filtered(arguments, completeStr);
    }

    private List<String> filtered(Arguments arguments, List<String> completeStr) {
        String next = "";
        int remains = arguments.remains();
        for (int i = 0; i < remains; i++) {
            String next1 = arguments.next();
            next = next1 == null ? next : next1;
        }
        String finalNext = next;
        return completeStr.stream().filter(s -> s.startsWith(finalNext)).collect(Collectors.toList());
    }

    private List<String> getElements() {
        ArrayList<String> str = new ArrayList<>();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        str.addAll(forgeManager.getElementList());
        return str;
    }

    private List<String> getLevels() {
        ArrayList<String> str = new ArrayList<>();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        str.addAll(forgeManager.getIronList());
        return str;
    }

    private Collection<? extends String> getRecycles() {
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        return forgeManager.getRecycleIds();
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
