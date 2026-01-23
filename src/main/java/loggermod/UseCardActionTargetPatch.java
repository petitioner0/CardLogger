package loggermod;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

@SpirePatch(
        clz = UseCardAction.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = {AbstractCard.class, AbstractCreature.class}
)
public class UseCardActionTargetPatch {

    public static void Postfix(
            UseCardAction __instance,
            AbstractCard card,
            AbstractCreature target
    ) {
        String targetDesc;

        if (target instanceof AbstractMonster) {
            targetDesc = ((AbstractMonster) target).name;

        } else if (target instanceof AbstractPlayer) {
            targetDesc = "玩家";

        } else {
            // target == null
            if (card.type == AbstractCard.CardType.ATTACK) {
                targetDesc = "全体敌人";
            } else {
                targetDesc = "无目标";
            }
        }

        CardLoggerMod.logCardUse(card, targetDesc);
    }
}