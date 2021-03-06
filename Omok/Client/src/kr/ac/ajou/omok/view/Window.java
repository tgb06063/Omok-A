package kr.ac.ajou.omok.view;

import static kr.ac.ajou.omok.protocol.Protobuf.GameStatusData.Status.*;
import static kr.ac.ajou.omok.protocol.Protobuf.LobbyData.Header.*;
import static kr.ac.ajou.omok.view.Constant.*;
import static kr.ac.ajou.omok.protocol.Protobuf.*;

import com.google.protobuf.InvalidProtocolBufferException;
import kr.ac.ajou.omok.communicate.ReceiveThread;
import processing.core.PApplet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Window extends PApplet implements Gui {
    private OutputStream os;
    private DataOutputStream dos;
    private Queue<Protocol> queue;
    private int position;

    private int numOfPlayer;
    private Button createRoomButton;
    private List<RoomInfo> rooms;
    private RoomInfo roomTemp;

    private int id;
    private GameStatusData.Status gameStatus;
    private boolean myTurn;
    private int color;
    private Board board;
    private Button readyButton;
    private List<PlayerInfo> players;
    private List<Stone> stones;
    private List<MsgBox> msgBoxes;

    @Override
    public void setup() {
        connect();
    }

    @Override
    public void settings() {
        queue = new ConcurrentLinkedQueue<>();

        rooms = new ArrayList<>();
        createRoomButton = new BigButton("CREATE ROOM");
        createRoomButton.activate();

        board = Board.getInstance();
        readyButton = new BigButton("READY");
        players = new CopyOnWriteArrayList<>();
        stones = new CopyOnWriteArrayList<>();
        msgBoxes = new CopyOnWriteArrayList<>();
        myTurn = false;

        size(WINDOW_W, WINDOW_H);
    }

    @Override
    public void draw() {
        while (!queue.isEmpty()) {
            Protocol protocol = queue.poll();
            String type = protocol.getType();
            try {
                switch (type) {
                    case "LobbyData":
                        LobbyData lobbyData =
                                LobbyData.parseFrom(protocol.getData());
                        analysisLobbyData(lobbyData);
                        break;
                    case "RoomInfoData":
                        RoomInfoData roomInfoData =
                                RoomInfoData.parseFrom(protocol.getData());
                        analysisRoomInfoData(roomInfoData);
                        break;
                    case "GameStatusData":
                        GameStatusData gameStatusData =
                                GameStatusData.parseFrom(protocol.getData());
                        analysisGameStatusData(gameStatusData);
                        break;
                    case "IdData":
                        IdData idData =
                                IdData.parseFrom(protocol.getData());
                        analysisIdData(idData);
                        break;
                    case "PlayerData":
                        PlayerData playerData =
                                PlayerData.parseFrom(protocol.getData());
                        analysisPlayerData(playerData);
                        break;
                    case "ReadyData":
                        ReadyData readyData =
                                ReadyData.parseFrom(protocol.getData());
                        analysisReadyData(readyData);
                        break;
                    case "MsgData":
                        MsgData msgData =
                                MsgData.parseFrom(protocol.getData());
                        analysisMsgData(msgData);
                    case "ColorData":
                        ColorData colorData =
                                ColorData.parseFrom(protocol.getData());
                        analysisColorData(colorData);
                        break;
                    case "TurnData":
                        TurnData turnData =
                                TurnData.parseFrom(protocol.getData());
                        analysisTurnData(turnData);
                        break;
                    case "StoneData":
                        StoneData stoneData =
                                StoneData.parseFrom(protocol.getData());
                        analysisStoneData(stoneData);
                        break;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        this.display(this);

        if (position == LOBBY) {
            displayLobby();
        } else if (position == ROOM) {
            displayRoom();
        }
    }

    private void analysisLobbyData(LobbyData lobbyData) {
        LobbyData.Header header = lobbyData.getHeader();
        if (header == NUM_OF_PLAYER) {
            position = LOBBY;
            numOfPlayer = lobbyData.getNumOfPlayer();
        } else if (header == ENTER_ROOM) {
            position = ROOM;
        }
    }

    private void analysisRoomInfoData(RoomInfoData roomInfoData) {
        int roomNumber = roomInfoData.getRoomNumber();

        if (position == LOBBY && rooms.size() > roomNumber) {
            RoomInfo roomInfo = rooms.get(roomInfoData.getRoomNumber());
            roomInfo.setNumOfPlayer(roomInfoData.getNumOfPlayer());
        } else {
            if (position == LOBBY) {
                rooms.size();
                RoomInfo roomInfo = new RoomInfo(roomInfoData.getRoomNumber());
                roomInfo.setNumOfPlayer(roomInfoData.getNumOfPlayer());
                rooms.add(roomInfo);
                if (rooms.size() >= 9) createRoomButton.deactivate();
            }
        }
    }

    private void analysisGameStatusData(GameStatusData gameStatusData) {
        gameStatus = gameStatusData.getGameStatus();
        if (gameStatus == ALL_ENTER)
            readyButton.activate();
        else if (gameStatus == RUNNING)
            msgBoxes = new CopyOnWriteArrayList<>();
        else if (gameStatus == RESET)
            resetGame();
    }

    private void analysisIdData(IdData idData) {
        id = idData.getId();
    }

    private void analysisPlayerData(PlayerData playerData) {
        System.out.println("PlayerData");
        int idTemp = playerData.getId();
        if (idTemp == 0) {
            players.add(new PlayerInfo(0, id == 0));
        } else if (idTemp == 1) {
            players.add(new PlayerInfo(0, id == 0));
            players.add(new PlayerInfo(1, id == 1));
        }
    }

    private void analysisReadyData(ReadyData readyData) {
        int idTemp = readyData.getId();
        for (PlayerInfo p : players) {
            if (p.getId() == idTemp) p.doReady();
        }
    }

    private void analysisMsgData(MsgData msgData) {
        String msg = msgData.getMsg();
        if (msg.equals("Empty")) {
            msgBoxes = new CopyOnWriteArrayList<>();
        } else {
            msgBoxes.add(new MsgBox(msg));
        }
    }

    private void analysisColorData(ColorData colorData) {
        int colorOfPlayer0 = colorData.getColorOfPlayer0();
        int colorOfPlayer1 = colorData.getColorOfPlayer1();

        for (PlayerInfo p : players) {
            if (p.getId() == 0)
                p.setStoneColor(colorOfPlayer0);
            else if (p.getId() == 1)
                p.setStoneColor(colorOfPlayer1);
        }

        if (this.id == 0) color = colorOfPlayer0;
        else if (this.id == 1) color = colorOfPlayer1;
        else color = NONE;
    }

    private void analysisTurnData(TurnData turnData) {
        int turn = turnData.getTurn();
        myTurn = (id == turnData.getTurn());

        for (PlayerInfo p : players) {
            p.changeTurn(turn);
        }
    }

    private void analysisStoneData(StoneData stoneData) {
        int i = stoneData.getI();
        int j = stoneData.getJ();
        int color = stoneData.getColor();
        stones.add(new Stone(i, j, color));
    }

    private void mouseEvent() {
        if (position == LOBBY) {
            if (createRoomButton.isMouseOver(this)) cursor(HAND);
            else if (onEnterButton()) cursor(HAND);
            else cursor(ARROW);

        } else if (position == ROOM) {
            switch (gameStatus) {
                case DEFAULT:
                    cursor(ARROW);
                    break;
                case ALL_ENTER:
                    if (readyButton.isMouseOver(this)) cursor(HAND);
                    else cursor(ARROW);
                    break;
                case RUNNING:
                    if (checkPutStone(  )) cursor(HAND);
                    else cursor(ARROW);
                    break;
            }
        }
    }

    @Override
    public void mousePressed() {
        if (mouseButton == LEFT && position == LOBBY &&
                createRoomButton.isMouseOver(this)) {
            createRoomButton.click();
        }

        if (mouseButton == LEFT && position == LOBBY) {
            for (RoomInfo roomInfo : rooms) {
                if (roomInfo.onEnterButton(this)) {
                    roomTemp = roomInfo;
                    roomTemp.clickEnterButton();
                    break;
                }
            }
        }

        if (mouseButton == LEFT && position == ROOM &&
                readyButton.isMouseOver(this)) {
            readyButton.click();
        }

        if (mouseButton == LEFT && position == ROOM &&
                gameStatus == RUNNING && checkPutStone()) {
            int i = convertToIndex(mouseY);
            int j = convertToIndex(mouseX);
            sendStoneData(i, j);
        }
    }

    @Override
    public void mouseReleased() {
        if (mouseButton == LEFT && position == LOBBY &&
                createRoomButton.isMouseOver(this)) {
            createRoomButton.release();
            sendCreateRoom();
        }

        if (mouseButton == LEFT && position == LOBBY &&
                onEnterButton()) {
            roomTemp.releaseEnterButton();
            sendEnterRoom(roomTemp.getRoomNumber());
        }

        if (mouseButton == LEFT && position == ROOM &&
                readyButton.isMouseOver(this)) {
            readyButton.release();
            readyButton.deactivate();
            sendReadyData();
        }
    }

    private boolean onEnterButton() {
        for (RoomInfo roomInfo : rooms) {
            if (roomInfo.onEnterButton(this)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPutStone() {
        int i = convertToIndex(mouseY);
        int j = convertToIndex(mouseX);

        return myTurn && checkRange() && isVacant(i, j);
    }

    private int convertToIndex(int mouse) {
        return (mouse - RANGE * 2) / BLOCK - 1;
    }

    private boolean checkRange() {
        for (int i = 0; i < 15; ++i) {
            for (int j = 0; j < 15; ++j) {
                if (((BLOCK * 2 - RANGE + (BLOCK * i)) < mouseX) &&
                        ((BLOCK * 2 + RANGE + (BLOCK * i)) > mouseX) &&
                        ((BLOCK * 2 - RANGE + (BLOCK * j)) < mouseY) &&
                        ((BLOCK * 2 + RANGE + (BLOCK * j)) > mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isVacant(int i, int j) {
        for (Stone s : stones) {
            if (s.checkStone(i, j)) return false;
        }
        return true;
    }

    private void resetGame() {
        players = new CopyOnWriteArrayList<>();
        stones = new CopyOnWriteArrayList<>();
        msgBoxes = new CopyOnWriteArrayList<>();
        myTurn = false;
        readyButton.deactivate();
    }

    private void connect() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("192.168.11.27", 5000));
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            System.out.println("연결 성공\n");
            ReceiveThread thread = new ReceiveThread(socket, this);
            thread.start();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void addQueue(Protocol protocol) {
        queue.add(protocol);
    }

    private void sendData(Protocol protocol) {
        try {
            byte[] data = protocol.toByteArray();
            int len = data.length;
            dos.writeInt(len);
            os.write(data, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCreateRoom() {
        LobbyData lobbyData = LobbyData
                .newBuilder()
                .setHeader(CREATE_ROOM)
                .build();
        Protocol protocol = Protocol
                .newBuilder()
                .setType("LobbyData")
                .setData(lobbyData.toByteString())
                .build();
        sendData(protocol);
    }

    private void sendEnterRoom(int roomNumber) {
        LobbyData lobbyData = LobbyData
                .newBuilder()
                .setHeader(ENTER_ROOM)
                .setRoomNumber(roomNumber)
                .build();
        Protocol protocol = Protocol
                .newBuilder()
                .setType("LobbyData")
                .setData(lobbyData.toByteString())
                .build();
        sendData(protocol);
    }

    private void sendStoneData(int i, int j) {
        StoneData stoneData = StoneData
                .newBuilder()
                .setI(i)
                .setJ(j)
                .setColor(color)
                .build();
        Protocol protocol = Protocol
                .newBuilder()
                .setType("StoneData")
                .setData(stoneData.toByteString())
                .build();
        sendData(protocol);
    }

    private void sendReadyData() {
        ReadyData readyData = ReadyData
                .newBuilder()
                .setId(id)
                .build();
        Protocol protocol = Protocol
                .newBuilder()
                .setType("ReadyData")
                .setData(readyData.toByteString())
                .build();
        sendData(protocol);
    }

    @Override
    public void display(PApplet p) {
        background(WHITE_COLOR);
        mouseEvent();
    }

    private void displayLobby() {
        fillBlack(this);
        textSize(TEXT_SIZE);
        textAlign(CENTER, CENTER);
        text("ROOM LIST", ROOM_LIST_X + ROOM_LIST_W / 2, ROOM_LIST_Y - BLOCK + 3);

        fillWhite(this);
        rect(ROOM_LIST_X, ROOM_LIST_Y, ROOM_LIST_W, ROOM_LIST_H);
        for (RoomInfo roomInfo : rooms) {
            roomInfo.display(this);
        }

        fillBlack(this);
        textSize(TEXT_SIZE);
        textAlign(CENTER, CENTER);
        String text = "Players [" + numOfPlayer + "]";
        text(text, WINDOW_W - BLOCK * 3, WINDOW_H - BUTTON_H - BLOCK * 3 + 5);

        createRoomButton.display(this);
    }

    private void displayRoom() {
        board.display(this);
        readyButton.display(this);
        for (MsgBox b : msgBoxes) {
            if (msgBoxes.size() > 1) {
                msgBoxes.remove(b);
                continue;
            }
            b.display(this);
        }
        for (PlayerInfo p : players) p.display(this);
        for (Stone s : stones) s.display(this);
    }

    public static void main(String[] args) {
        PApplet.main(Window.class);
    }
}