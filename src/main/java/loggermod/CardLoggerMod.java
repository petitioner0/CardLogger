package loggermod;

import basemod.*;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.awt.Desktop;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SpireInitializer
public class CardLoggerMod implements
        OnCardUseSubscriber,
        PostBattleSubscriber,
        OnStartBattleSubscriber,
        PostInitializeSubscriber,
        PostUpdateSubscriber {

    private static final int DEFAULT_KEY = Input.Keys.F8;
    private static final String LOG_DIR = "mods/CardLogger/logs/";

    private String fileName = null;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean lastTurnEnded = false;

    public CardLoggerMod() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new CardLoggerMod();
    }

    @Override
    public void receivePostInitialize() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom room) {
        String seed = Settings.seed.toString();
        int floor = AbstractDungeon.floorNum;

        // 创建以种子为名的文件夹
        String seedDir = LOG_DIR + seed + "/";
        File dir = new File(seedDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 设置日志文件路径
        fileName = seedDir + floor + ".txt";

        // 每次都写入新的 SL 记录（不再做文件内容判断）
        int slNum = countExistingSLs(fileName) + 1;
        String time = formatter.format(new Date());
        writeLine("\n第" + slNum + "次SL（" + time + "）：");
    }

    @Override
    public void receiveCardUsed(AbstractCard card) {
        if (fileName == null) return;

        String target = null;

        if (card.target == AbstractCard.CardTarget.ALL_ENEMY) {
            target = "全体";
        } else if (card.target == AbstractCard.CardTarget.SELF) {
            target = "玩家";
        } else {
            // 猜测目标：是否有怪物被 hover（仅在单体攻击有效）
            for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                if (!m.isDeadOrEscaped() && m.hb.hovered) {
                    target = m.name;
                    break;
                }
            }
        }

        // 默认目标处理
        if (target == null || target.trim().isEmpty()) {
            target = "未知目标";
        }

        writeLine("出牌：" + card.name + "（" + target + "）");
    }

    @Override
    public void receivePostBattle(AbstractRoom room) {
        if (fileName == null) return;

        String endDesc = "【战斗结束】";
        if (AbstractDungeon.player.isDead) {
            endDesc = "【玩家死亡】";
        } else if (AbstractDungeon.getCurrRoom().monsters != null &&
                AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
            endDesc = "【胜利】";
        } else if (AbstractDungeon.getCurrRoom().smoked) {
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

        // 检测回合结束
        if (fileName != null) {
            boolean currentTurnEnded = AbstractDungeon.actionManager.turnHasEnded;
            if (!lastTurnEnded && currentTurnEnded) {
                writeLine("-> 回合结束");
            }
            lastTurnEnded = currentTurnEnded;
        }
    }

    private void writeLine(String text) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(fileName, true), true)) {
            writer.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkSLWritten(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("第") && line.contains("次SL")) {
                    return true;
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private int countExistingSLs(String filePath) {
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