package cat.nyaa.saika;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Commands extends CommandReceiver {
    private static String PERMISSION_OP = "saika.permission";

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
    public void onReload(CommandSender sender, Arguments arguments){
        plugin.reload();
    }

    @SubCommand("define")
    public void onDefine(CommandSender sender, Arguments arguments){
        if (sender.hasPermission(PERMISSION_OP)){
            if (!(sender instanceof Player)){
                new Message(I18n.format("error.not_player"))
                        .send(sender);
                return;
            }

        }else {
            new Message(I18n.format("error.permission"))
                    .send(sender);
        }
    }
}
