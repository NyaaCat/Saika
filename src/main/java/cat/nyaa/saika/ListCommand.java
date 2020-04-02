package cat.nyaa.saika;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.saika.forge.ForgeElement;
import cat.nyaa.saika.forge.ForgeIron;
import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ForgeableItem;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cat.nyaa.saika.Commands.getItemInfo;

public class ListCommand extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public ListCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
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

    public List<String> listCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.addAll(getLevels());
                break;
            case 2:
                completeStr.addAll(getElements());
                break;
            case 3:
                completeStr.add("<amount>");
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

    @Override
    public List<String> acceptTabComplete(CommandSender sender, Arguments args) {
        return listCompleter(sender, args);
    }

    @SubCommand(isDefaultCommand = true, tabCompleter = "listCompleter")
    public void onList(CommandSender sender, Arguments arguments) {
        String level = arguments.nextString();
        String element = arguments.nextString();
        int ironAmount = 64;
        if (arguments.top()!=null){
            ironAmount = arguments.nextInt();
        }

        ForgeManager forgeManager = ForgeManager.getForgeManager();
        List<ForgeableItem> s = forgeManager.listItem(level, element,ironAmount);
        ForgeIron iron = forgeManager.getIron(level);
        ForgeElement element1 = forgeManager.getElement(element);

        s.sort(Comparator.comparingInt(ForgeableItem::getWeight));
        double weightSum = s.stream()
                .mapToInt(ForgeableItem::getWeight)
                .sum();
        if (!s.isEmpty()) {
            ItemStack clone = iron.getItemStack().clone();
            clone.setAmount(ironAmount);
            new Message("").append(I18n.format("list.success"), element1.getItemStack(), clone)
                    .send(sender);
            s.forEach(forgeableItem -> {
                Message message = new Message("")
                        .append(" {itemName} * {amount}", forgeableItem.getItemStack())
                        .append(I18n.format("list.possibility", (((double) forgeableItem.getWeight()) / weightSum) * 100));
                message.send(sender);
            });
        } else {
            new Message(I18n.format("list.error.no_result"))
                    .send(sender);
        }
    }

    @Override
    public String getHelpPrefix() {
        return null;
    }
}
