package loggermod;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import javassist.CtBehavior;

@SpirePatch(
        clz = DrawCardAction.class,
        method = "update"
)
public class DrawCardActionPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void Insert(DrawCardAction __instance) {
        if (!DrawCardAction.drawnCards.isEmpty()) {
            AbstractCard c =
                    DrawCardAction.drawnCards.get(
                            DrawCardAction.drawnCards.size() - 1
                    );
            CardLoggerMod.logDrawCard(c);
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(
                    com.megacrit.cardcrawl.characters.AbstractPlayer.class,
                    "draw"
            );
            return LineFinder.findInOrder(ctBehavior, matcher);
        }
    }
}