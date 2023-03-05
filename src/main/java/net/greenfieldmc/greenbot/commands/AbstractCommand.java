package net.greenfieldmc.greenbot.commands;

public abstract class AbstractCommand implements SlashCommand {

    private final String commandName;
    private final String description;

    public AbstractCommand(String commandDesciption, String name) {
        this.commandName = name;
        this.description = commandDesciption;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
