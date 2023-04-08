//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.envyful.reforged.bingo.forge.player;

import com.envyful.reforged.bingo.forge.ReforgedBingo;
import com.envyful.reforged.bingo.forge.event.BingoSlotCompleteEvent;
import com.envyful.reforged.bingo.forge.shade.envy.api.forge.chat.UtilChatColour;
import com.envyful.reforged.bingo.forge.shade.envy.api.forge.config.UtilConfigItem;
import com.envyful.reforged.bingo.forge.shade.envy.api.forge.items.ItemBuilder;
import com.envyful.reforged.bingo.forge.shade.envy.api.forge.player.ForgeEnvyPlayer;
import com.envyful.reforged.bingo.forge.shade.envy.api.forge.player.attribute.AbstractForgeAttribute;
import com.envyful.reforged.bingo.forge.shade.envy.api.gui.factory.GuiFactory;
import com.envyful.reforged.bingo.forge.shade.envy.api.gui.item.Displayable;
import com.envyful.reforged.bingo.forge.shade.envy.api.gui.pane.Pane;
import com.envyful.reforged.bingo.forge.shade.envy.api.json.UtilGson;
import com.envyful.reforged.bingo.forge.shade.envy.api.player.EnvyPlayer;
import com.envyful.reforged.bingo.forge.shade.envy.api.reforged.pixelmon.sprite.UtilSprite;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BingoAttribute extends AbstractForgeAttribute<ReforgedBingo> {
    private long started;
    private CardSlot[][] bingoCard;

    public BingoAttribute(ReforgedBingo manager, EnvyPlayer<?> parent) {
        super(manager, (ForgeEnvyPlayer)parent);
    }

    public BingoAttribute(UUID uuid) {
        super(uuid);
    }

    public void load() {
        try {
            Connection connection = this.manager.getDatabase().getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT card, timeStarted FROM `reforged_bingo_cards` WHERE uuid = ?;");
                Throwable var4 = null;

                try {
                    preparedStatement.setString(1, this.parent.getUuid().toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        this.started = resultSet.getLong("timeStarted");
                        this.bingoCard = UtilGson.GSON.fromJson(resultSet.getString("card"), CardSlot[][].class);
                        return;
                    }

                    this.generateNewCard();
                } catch (Throwable var33) {
                    var4 = var33;
                    throw var33;
                } finally {
                    if (preparedStatement != null) {
                        if (var4 != null) {
                            try {
                                preparedStatement.close();
                            } catch (Throwable var32) {
                                var4.addSuppressed(var32);
                            }
                        } else {
                            preparedStatement.close();
                        }
                    }

                }
            } catch (Throwable var35) {
                var2 = var35;
                throw var35;
            } finally {
                if (connection != null) {
                    if (var2 != null) {
                        try {
                            connection.close();
                        } catch (Throwable var31) {
                            var2.addSuppressed(var31);
                        }
                    } else {
                        connection.close();
                    }
                }

            }

        } catch (SQLException var37) {
            var37.printStackTrace();
        }
    }

    public void save() {
        try {
            Connection connection = this.manager.getDatabase().getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `reforged_bingo_cards`(uuid, card, timeStarted) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE card = VALUES(`card`), timeStarted = VALUES(`timeStarted`);");
                Throwable var4 = null;

                try {
                    preparedStatement.setString(1, this.parent.getUuid().toString());
                    preparedStatement.setString(2, UtilGson.GSON.toJson(this.bingoCard));
                    preparedStatement.setLong(3, this.started);
                    preparedStatement.executeUpdate();
                } catch (Throwable var29) {
                    var4 = var29;
                    throw var29;
                } finally {
                    if (preparedStatement != null) {
                        if (var4 != null) {
                            try {
                                preparedStatement.close();
                            } catch (Throwable var28) {
                                var4.addSuppressed(var28);
                            }
                        } else {
                            preparedStatement.close();
                        }
                    }

                }
            } catch (Throwable var31) {
                var2 = var31;
                throw var31;
            } finally {
                if (connection != null) {
                    if (var2 != null) {
                        try {
                            connection.close();
                        } catch (Throwable var27) {
                            var2.addSuppressed(var27);
                        }
                    } else {
                        connection.close();
                    }
                }

            }
        } catch (SQLException var33) {
            var33.printStackTrace();
        }

    }

    public boolean checkCardExpiry() {
        return System.currentTimeMillis() - this.started > TimeUnit.SECONDS.toMillis(ReforgedBingo.getInstance().getConfig().getCardDurationSeconds());
    }

    public void generateNewCard() {
        this.bingoCard = new CardSlot[4][7];

        for(int y = 0; y < 4; ++y) {
            CardSlot[] currentLine = this.bingoCard[y];

            for(int x = 0; x < 7; ++x) {
                Species species;
                do {
                    species = PixelmonSpecies.getRandomSpecies();
                } while (!this.canPickPokemon(species));
                currentLine[x] = new CardSlot(species, false);
            }
        }

        this.started = System.currentTimeMillis();
        this.parent.message(UtilChatColour.translateColourCodes('&', ((ReforgedBingo)this.manager).getLocale().getCardReset()));
    }

    private boolean canPickPokemon(Species value) {
        if (value.getDefaultForm().getPreEvolutions().size() >= this.manager.getConfig().getMaximumEvolution()) {
            return false;
        }

        if (this.manager.isBlacklisted(value)) {
            return false;
        }

        if (this.manager.getConfig().isDisableLegendaryPokemonGenerate()) {
            return !value.getFirstForm().getTags().isLegendary();
        }

        return true;
    }

    public void catchPokemon(Species species) {
        CardSlot[][] var2 = this.bingoCard;

        for (CardSlot[] cardSlots : var2) {

            for (CardSlot cardSlot : cardSlots) {
                if (cardSlot.getSpecies() == species && !cardSlot.isComplete()) {
                    cardSlot.setComplete(true);
                    boolean rowComplete = this.checkRowCompletion(cardSlots);
                    boolean cardComplete = this.checkCardCompletion();
                    BingoSlotCompleteEvent completeEvent = new BingoSlotCompleteEvent(this.parent, this, rowComplete, cardComplete);
                    MinecraftForge.EVENT_BUS.post(completeEvent);
                    return;
                }
            }
        }

    }

    private boolean checkRowCompletion(CardSlot[] complete) {
        for (CardSlot cardSlot : complete) {
            if (!cardSlot.isComplete()) {
                return false;
            }
        }

        return true;
    }

    private boolean checkCardCompletion() {
        CardSlot[][] var1 = this.bingoCard;

        for (CardSlot[] cardSlots : var1) {

            for (CardSlot cardSlot : cardSlots) {
                if (!cardSlot.isComplete()) {
                    return false;
                }
            }
        }

        return true;
    }

    public long getTimeRemaining() {
        return TimeUnit.SECONDS.toHours(ReforgedBingo.getInstance().getConfig().getCardDurationSeconds()) - TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - this.started);
    }

    public void display(Pane pane) {
        Displayable complete = GuiFactory.displayableBuilder(ItemStack.class).itemStack(UtilConfigItem.fromConfigItem(ReforgedBingo.getInstance().getConfig().getCompleteItem())).build();

        for(int y = 0; y < 4; ++y) {
            for(int x = 0; x < 7; ++x) {
                if (this.bingoCard[y][x].isComplete()) {
                    pane.set(1 + x, 1 + y, complete);
                } else {
                    CardSlot cardSlot = this.bingoCard[y][x];
                    List<String> lore = Lists.newArrayList();

                    for (String s : ReforgedBingo.getInstance().getLocale().getCardSlotLore()) {
                        lore.add(UtilChatColour.translateColourCodes('&', s));
                    }

                    pane.set(1 + x, 1 + y, GuiFactory.displayableBuilder(ItemStack.class).itemStack((new ItemBuilder(UtilSprite.getPixelmonSprite(cardSlot.getSpecies()))).addLore((String[])lore.toArray(new String[0])).build()).clickHandler((envyPlayer, clickType) -> {

                        for (String cardSlotCommand : ReforgedBingo.getInstance().getConfig().getCardSlotCommands()) {
                            envyPlayer.executeCommand(cardSlotCommand.replace("%pokemon%", cardSlot.getSpecies().getLocalizedName()));
                        }

                    }).build());
                }
            }
        }

    }
}