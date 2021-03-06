package kr.ac.ajou.omok.view;

import processing.core.PApplet;

import static kr.ac.ajou.omok.view.Constant.*;

public class MsgBox implements Gui {
    private static final int BOX_X = BLOCK;
    private static final int BOX_Y = BLOCK * 5;
    private static final int BOX_W = BOARD;
    private static final int BOX_H = BLOCK * 8;

    private String msg;

    MsgBox(String msg) {
        this.msg = msg;
    }

    @Override
    public void display(PApplet p) {
        fillTransparent(p);
        p.rect(BOX_X, BOX_Y, BOX_W, BOX_H);
        fillBlack(p);
        p.textSize(TEXT_SIZE);
        p.textAlign(p.CENTER, p.CENTER);
        p.text(msg, BLOCK + BOARD / 2, BLOCK + BOARD / 2 - 7);
    }
}
