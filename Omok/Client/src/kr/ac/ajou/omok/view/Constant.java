package kr.ac.ajou.omok.view;

class Constant {
    static final int BLACK = 1;
    static final int WHITE = -1;
    static final int NONE = 0;
    static final int BLOCK = 30;
    static final int POINT = 15;
    static final int DIAMETER = BLOCK / 5 * 4;
    static final int GAP = BLOCK / 2;
    static final int BOARD = BLOCK * (POINT + 1);
    static final int RANGE = BLOCK / 6;
    static final int BUTTON_W = BOARD;
    static final int BUTTON_H = BLOCK * 2;
    static final int WINDOW_W = BOARD + BLOCK * 2;
    static final int WINDOW_H = BOARD + BLOCK * 3 + BUTTON_H * 3 + GAP * 2;
    static final int TEXT_SIZE = 20;
    static final int WHITE_COLOR = 255;

    static final int ROOM_LIST_X = BLOCK;
    static final int ROOM_LIST_Y = BLOCK * 3;
    static final int ROOM_LIST_W = BOARD;
    static final int ROOM_LIST_H = BUTTON_H * 9;

    static final int LOBBY = 1010;
    static final int ROOM = 1011;
}
