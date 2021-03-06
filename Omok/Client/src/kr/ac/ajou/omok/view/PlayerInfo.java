package kr.ac.ajou.omok.view;

import processing.core.PApplet;

import static kr.ac.ajou.omok.view.Constant.*;

public class PlayerInfo implements Gui {
    private final boolean isMe;
    private final int id;
    private final int x;
    private final int y;
    private final int w;
    private final int h;

    private boolean ready;
    private int stoneColor;
    private boolean turn;

    PlayerInfo(int id, boolean isMe) {
        this.isMe = isMe;
        this.id = id;
        x = BLOCK;
        y = BOARD + 2 * BLOCK + (BUTTON_H + GAP) * this.id;
        w = BUTTON_W;
        h = BUTTON_H;

        ready = false;
        stoneColor = NONE;
        turn = false;
    }

    void setStoneColor(int color) {
        if (color == BLACK || color == WHITE)
            stoneColor = color;
    }

    int getId() {
        return id;
    }

    void changeTurn(int id) {
        turn = (this.id == id);
    }

    void doReady() {
        this.ready = true;
    }

    @Override
    public void display(PApplet p) {
        drawBox(p);
        drawReady(p);
        drawName(p);
        if (turn) drawTurn(p);
        drawStoneColor(p);
    }

    private void drawBox(PApplet p) {
        fillWhite(p);
        p.rect(x, y, w, h);
    }

    private void drawReady(PApplet p) {
        if (ready) fillBlack(p);
        else fillGrey(p);

        p.textSize(TEXT_SIZE);
        p.textAlign(p.CENTER, p.CENTER);
        p.text("READY", x + w - 2 * BLOCK, y + BLOCK - 3);
    }

    private void drawName(PApplet p) {
        fillBlack(p);
        p.text("PLAYER " + id, x + BLOCK * 3, y + BLOCK - 3);

        if (isMe) {
            fillGreen(p);
            p.ellipse(x + BLOCK * 5, y + BLOCK, 5, 5);
        }
    }

    private void drawTurn(PApplet p) {
        fillRed(p);
        p.rect(x, y, BLOCK / 2, h);
    }

    private void drawStoneColor(PApplet p) {
        if (stoneColor == BLACK) {
            fillBlack(p);
            p.ellipse(x + BLOCK * 6, y + BLOCK, DIAMETER, DIAMETER);
        } else if (stoneColor == WHITE) {
            fillWhite(p);
            p.ellipse(x + BLOCK * 6, y + BLOCK, DIAMETER, DIAMETER);
        }
    }
}
