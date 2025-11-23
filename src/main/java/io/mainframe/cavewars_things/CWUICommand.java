package io.mainframe.cavewars_things;

import alepando.dev.dialogapi.body.types.PlainMessageDialogBody;
import alepando.dev.dialogapi.executor.PlayerOpener;
import alepando.dev.dialogapi.factory.Dialog;
import alepando.dev.dialogapi.factory.button.Button;
import alepando.dev.dialogapi.factory.button.data.ButtonDataBuilder;
import alepando.dev.dialogapi.factory.data.DialogData;
import alepando.dev.dialogapi.factory.data.DialogDataBuilder;
import alepando.dev.dialogapi.types.builders.MultiActionDialogBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CWUICommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args) {
        if (args.length != 0) {sender.sendMessage("Usage: /cwui"); return true;}
        DialogData dialogData = new DialogDataBuilder()
                .title(Component.text("OMGOMG"))
                .addBody(new PlainMessageDialogBody(
                        400, Component.text("я продал своего друга за 5000 фурри коннекторов ради додепа")
                )).build();
        Button button = new Button(
                new ButtonDataBuilder()
                        .label(Component.text("OK"))
                        .width(80)
                        .build(),
                Optional.empty() // No custom action for now
        );
        Dialog dialog = new MultiActionDialogBuilder()
                .data(dialogData)  // Pass your DialogData here
                .addButton(button)
                .columns(1)
                .build();
        PlayerOpener.INSTANCE.openDialog((Player) sender, dialog);
        return true;
    }
}