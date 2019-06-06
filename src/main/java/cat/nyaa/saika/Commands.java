package cat.nyaa.saika;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.Pair;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.nyaacore.utils.LocaleUtils;
import cat.nyaa.saika.forge.*;
import cat.nyaa.saika.forge.EnchantSource.EnchantmentType;
import cat.nyaa.saika.forge.ForgeManager.NbtExistException;
import cat.nyaa.saika.forge.ui.EnchantUi;
import cat.nyaa.saika.forge.ui.ForgeUi;
import cat.nyaa.saika.forge.ui.RecycleUi;
import cat.nyaa.saika.forge.ui.RepulseUi;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length> 0) {
            List<String> strings = commandCompleter.onSubCommand(args[0], args);
            if (strings == null) {
                return super.onTabComplete(sender, command, alias, args);
            }
            return strings;
        } else {
            return super.onTabComplete(sender, command, alias, args);
        }
    }

    class SaikaCommandCompleter {
        List<String> open = Arrays.asList("forge", "enchant", "repulse", "recycle");
        List<String> add;
        List<String> define = Arrays.asList("level", "element", "enchant", "repulse", "recycle");
        List<String> list;

        List<String> onSubCommand(String subCommand, String[] arguments) {
            List<String> str = null;
            switch (subCommand) {
                case "open":
                    str = completeOpen(subCommand, arguments);
                    break;
                case "add":
                    str = completeAdd(subCommand, arguments);
                    break;
                case "define":
                    str = completeDefine(subCommand, arguments);
                    break;
                case "delete":
                    str = completeDelete(subCommand, arguments);
                    break;
                case "remove":
                    str = completeRemove(subCommand, arguments);
                    break;
                case "modify":
                    str = completeModify(subCommand, arguments);
                    break;
                case "bonus":
                    str = completeBonus(subCommand, arguments);
                    break;
                case "inspect":
                    str = completeInspect(subCommand, arguments);
                    break;
                case "list":
                    str = completeList(subCommand, arguments);
                    break;
                default:
                    break;
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
                    str.add("level");
                    break;
                case 3:
                    str.add("element");
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
            String top = arguments[1];
            if (top == null) {
                return define;
            }
            switch (top) {
                case "level":
                    if (arguments.length<3 || arguments[2] == null) {
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
            String top = arguments[1];
            if (top == null) {
                return define;
            }
            str.add("id");
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
                    str.add("id");
                    break;
                case 3:
                    str = define;
                    break;
                case 4:
                    str.add("value");
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
            switch (arguments.length){
                case 2:
                    str.add("level");
                    break;
                case 3:
                    str.add("element");
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

    @SubCommand("reload")
    public void onReload(CommandSender sender, Arguments arguments) {
        if (dontHavePermission(sender, PERMISSION_ADMIN, "")) {
            return;
        }
        plugin.reload();
        new Message(I18n.format("reload.success"))
                .send(sender);
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
            new Message(I18n.format("modify.error.no_item", id))
                    .send(sender);
            return;
        }
        switch (targetVal) {
            case "level":
                forgeableItem.setLevel(value);
                new Message("").append(I18n.format("modify.success.level", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "element":
                forgeableItem.setElement(value);
                new Message("").append(I18n.format("modify.success.element", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "cost":
                forgeableItem.setMinCost(Integer.parseInt(value));
                new Message("").append(I18n.format("modify.success.cost", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "weight":
                forgeableItem.setWeight(Integer.parseInt(value));
                new Message("").append(I18n.format("modify.success.weight", value), forgeableItem.getItemStack())
                        .send(sender);
                break;
            case "recycle":
                String action = arguments.nextString();
                ForgeableItem.RecycleInfo recycle = forgeableItem.getRecycle();
                switch (action){
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
                        if (bonus.equals("-1")){
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
                        forgeableItem.setRecycle(recycle.min, recycle.max, recycle.hard, bonusItem.toNbt(), recycle.bonus.chance);
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
            Message message = new Message("").append(I18n.format("inspect.info", id), forgeableItem.getItemStack());//&r{itemName}&r * {amount}
            sendItemInfo(sender, message, forgeManager, forgeableItem);
            message.send(sender);
        } else {
            new Message(I18n.format("inspect.error.no_item", id))
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
        List<ForgeableItem> s = forgeManager.listItem(level, element);
        s.sort(Comparator.comparingInt(ForgeableItem::getMinCost));
        if (!s.isEmpty()) {
            new Message(I18n.format("list.success"))
                    .send(sender);
            s.forEach(forgeableItem -> {
                Message message = new Message("").append(I18n.format("list.info", forgeableItem.getId()), forgeableItem.getItemStack());
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
            try {
                ItemStack itemStack = ItemStackUtils.itemFromBase64(forgeBonus.item);
                message.append(I18n.format("list.forge_bonus", forgeBonus.chance), itemStack);
            } catch (Exception e) {
            }
        }
        ForgeableItem.Bonus recycleBonus = forgeableItem.getRecycleBonus();
        if (!recycleBonus.item.equals("")) {
            try {
                ItemStack itemStack = ItemStackUtils.itemFromBase64(recycleBonus.item);
                message.append(I18n.format("list.recycle_bonus", recycleBonus.chance), itemStack);
            } catch (Exception e) {
            }
        }
    }

    Random random = new Random();

    @SubCommand("bonus")
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
                bonus.item = bonusItem.toNbt();
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

    @SubCommand("add")
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

    @SubCommand("remove")
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

    @SubCommand("open")
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
            if (nearbyBlock.parallelStream().anyMatch(loc -> loc.getBlock().getType() == forgeBlock)) match = true;
            else match = false;
            if (!match) {
                new Message("")
                        .append(I18n.format("open.error.no_required_block", forgeUiDistance), Collections.singletonMap("{block}", LocaleUtils.getNameComponent(new ItemStack(forgeBlock))))
                        .send(sender);
            }
        }
        return match;
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
