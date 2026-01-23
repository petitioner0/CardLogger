package loggermod;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;

import java.util.ArrayList;

@SpirePatch(
        clz = HandCardSelectScreen.class,
        method = "update"
)
public class HandCardSelectConfirmPatch {

    private static boolean logged = false;

    @SpirePrefixPatch
    public static void Prefix(HandCardSelectScreen __instance) {

        // 还没确认，不做
        if (__instance.wereCardsRetrieved) {
            logged = false; // 重置，给下次用
            return;
        }

        // 已选，但还没记录过 → 只记录一次
        if (!logged && !__instance.selectedCards.group.isEmpty()) {

            ArrayList<AbstractCard> chosen =
                    new ArrayList<>(__instance.selectedCards.group);

            CardLoggerMod.logChoose(chosen);
            logged = true;
        }
    }
}