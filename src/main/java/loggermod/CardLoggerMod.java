package loggermod;

import basemod.*;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;

import java.awt.Desktop;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SpireInitializer
public class CardLoggerMod implements
        PostBattleSubscriber,
        OnStartBattleSubscriber,
        PostInitializeSubscriber,
        PostUpdateSubscriber {

    private static final int DEFAULT_KEY = Input.Keys.F8;
    private static final String LOG_DIR = "mods/CardLogger/logs/";
    private static final SimpleDateFormat formatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 当前战斗日志文件 */
    private static String fileName = null;

    /** 回合结束检测 */
    private boolean lastTurnEnded = false;

    public CardLoggerMod() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new CardLoggerMod();
    }

    /* ======================= 日志接口 ======================= */

    public static void logDrawCard(AbstractCard card) {
        if (fileName == null) return;
        writeLine("抽牌：" + card.name);
    }

    public static void logCardUse(AbstractCard card, String m) {
        if (fileName == null) return;
        writeLine("出牌：" + card.name + "（" + m + "）");
    }

    public static void logChoose(AbstractCard card) {
        if (fileName == null) return;
        writeLine("选择：" + card.name);
    }

    public static void logChoose(List<AbstractCard> cards) {
        if (fileName == null || cards == null || cards.isEmpty()) return;

        StringBuilder sb = new StringBuilder("选择：");
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).name);
            if (i < cards.size() - 1) {
                sb.append(",");
            }
        }

        writeLine(sb.toString());
    }

    /* ======================= BaseMod 回调 ======================= */

    @Override
    public void receivePostInitialize() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom room) {
        lastTurnEnded = false;

        String seed = Settings.seed.toString();
        int floor = AbstractDungeon.floorNum;

        String seedDir = LOG_DIR + seed + "/";
        File dir = new File(seedDir);
        if (!dir.exists()) dir.mkdirs();

        fileName = seedDir + floor + ".txt";

        int slNum = countExistingSLs(fileName) + 1;
        String time = formatter.format(new Date());
        writeLine("\n第" + slNum + "次SL（" + time + "）：");
    }

    @Override
    public void receivePostBattle(AbstractRoom room) {
        if (fileName == null) return;

        String endDesc = "【战斗结束】";
        if (AbstractDungeon.player.isDead) {
            endDesc = "【玩家死亡】";
        } else if (room.monsters != null && room.monsters.areMonstersBasicallyDead()) {
            endDesc = "【胜利】";
        } else if (room.smoked) {
            endDesc = "【逃跑】";
        }

        writeLine(endDesc);
        fileName = null;
    }

    @Override
    public void receivePostUpdate() {
        if (fileName != null && Gdx.input.isKeyJustPressed(DEFAULT_KEY)) {
            File file = new File(fileName);
            if (file.exists()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (fileName != null) {
            boolean currentTurnEnded = AbstractDungeon.actionManager.turnHasEnded;
            if (!lastTurnEnded && currentTurnEnded) {
                writeLine("-> 回合结束");
            }
            lastTurnEnded = currentTurnEnded;
        }
    }

    /* ======================= 工具方法 ======================= */

    private static void writeLine(String text) {
        if (fileName == null) return;
        try (PrintWriter writer =
                     new PrintWriter(new FileOutputStream(fileName, true), true)) {
            writer.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int countExistingSLs(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return 0;

        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("第") && line.contains("次SL")) {
                    count++;
                }
            }
        } catch (IOException ignored) {
        }
        return count;
    }
}
