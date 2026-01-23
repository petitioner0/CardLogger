package loggermod;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.unique.DiscoveryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

@SpirePatch(
        clz = DiscoveryAction.class,
        method = "update"
)
public class DiscoveryConfirmPatch {

    private static AbstractCard lastSeen = null;

    @SpirePrefixPatch
    public static void Prefix(DiscoveryAction __instance) {

        AbstractCard current =
                AbstractDungeon.cardRewardScreen.discoveryCard;

        // 玩家刚刚点下确认（null → 非 null）
        if (lastSeen == null && current != null) {
            CardLoggerMod.logChoose(current);
        }

        // 同一帧后半段 / 下一帧会被清空，这里同步状态
        lastSeen = current;
    }
}