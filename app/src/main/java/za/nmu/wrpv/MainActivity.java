package za.nmu.wrpv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.barteksc.pdfviewer.PDFView;
import com.otaliastudios.zoom.ZoomLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.nmu.wrpv.client.Player;
import za.nmu.wrpv.client.PlayerData;
import za.nmu.wrpv.client.Receiver;
import za.nmu.wrpv.messages.Package;
import za.nmu.wrpv.qwirkle.Position;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.Tile;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout parent, gameOverLayout;
    private int minRow, maxRow;
    private int minCol, maxCol;

    private int numRow, numCol;

    ZoomLayout zoomLayout;

    ImageView zoomIn, zoomOut;

    AppCompatImageButton sound, trade, pass;
    AppCompatImageButton finalDone;

    AlertDialog quitAlert;

    LinearLayout grid;

    Player player;

    TextView numBag, timer, popup_mgs, points;

    MediaPlayer mediaPlayer;

    Tile selectedTile = null;
    ImageView selectedTileImg = null;
    ImageView tile_1, tile_2, tile_3, tile_4, tile_5, tile_6;

    ImageView info, gameOverLogo;

    LinearLayout score_board_1, score_board_2, score_board_3, score_board_4;
    LinearLayout finalScore;

    List<Tile> onBoardTiles = null;

    boolean soundOn = true;
    boolean isTrade = false;
    boolean isPlaced = false;


    PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minRow = 1;
        maxRow = 6;
        minCol = 1;
        maxCol = 6;
        numCol = 6;
        numRow = 6;


        Intent intent = getIntent();
        if (intent != null) {
            String name = (String) intent.getExtras().get("name");
            String ip = (String) intent.getExtras().get("ip");


            Receiver receiver = p -> {
                runOnUiThread(
                        () -> receive(p)
                );
            };

            player = new Player(name, receiver);
            player.connectToServer(ip.trim());

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            send(new Package("player-name", data));

            iniViews();
            zoom();
            bottomButtons();
        }


    }

    private void iniViews() {

        // Parent
        parent = findViewById(R.id.main_parent);

        // PDF View
        pdfView = findViewById(R.id.pdfView);
        setUpPdf();

        // Info
        info = findViewById(R.id.info_btn);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pdfView.getVisibility() == View.VISIBLE) {
                    YoYo.with(Techniques.SlideOutRight).duration(700)
                            .playOn(pdfView);
                    pdfView.setVisibility(View.GONE);
                } else {
                    pdfView.setVisibility(View.VISIBLE);

                    YoYo.with(Techniques.SlideInLeft).duration(700)
                            .playOn(pdfView);
                    setUpPdf();
                }

            }
        });

        // Time
        timer = findViewById(R.id.timer);

        points = findViewById(R.id.points);

        popup_mgs = findViewById(R.id.pop_msg);

        // Scores
        score_board_1 = findViewById(R.id.player_1_score);
        score_board_2 = findViewById(R.id.player_2_score);
        score_board_3 = findViewById(R.id.player_3_score);
        score_board_4 = findViewById(R.id.player_4_score);

        // Zoom
        zoomLayout = findViewById(R.id.z_l);
        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_out);

        // Bags
        numBag = findViewById(R.id.bag_num);

        // Bottom buttons
        sound = findViewById(R.id.menu_button);
        trade = findViewById(R.id.trade);
        pass = findViewById(R.id.place);

        // Grid
        grid = findViewById(R.id.grid);
        createGrid();

        // Hand tiles
        handTiles();
        setHandTiles();
        lock();

        // Quit
        quitAlert();

        // Game Over

        gameOverLayout = findViewById(R.id.game_over_layout);
        gameOverLogo = findViewById(R.id.game_over_logo);
        finalScore = findViewById(R.id.final_score);
    /*    finalDone = findViewById(R.id.done_final);

        finalDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Back to initial screen
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);
                finish();
            }
        });*/
    }

    /**
     * Sends package to the server
     *
     * @param p package
     */
    private void send(Package p) {
        player.sendPackage(p);
    }

    /**
     * Receive package from server
     *
     * @param p package
     */
    private void receive(Package p) {
        switch (p.command) {
            case "player-data": {
                player.hand = (List<Tile>) p.data.get("hand");
                player.number = (int) p.data.get("player-number");
                setHandTiles();
                lock();
                break;
            }
            case "player-score": {

                List<Tile> tiles = (List<Tile>) p.data.get("placing-list");

                place(tiles);

                String score = "" + (int) p.data.get("player-score");
                String gain = "+" + (int) p.data.get("gain");

                // Show gained points
                points.setVisibility(View.VISIBLE);
                points.setText(gain);
                YoYo.with(Techniques.ZoomOut).delay(700)
                        .duration(2000)
                        .playOn(points);

                int num = (int) p.data.get("player-number");

                switch (num) {
                    case 1: {

                        ((TextView) findViewById(R.id.score_1))
                                .setText(score);


                        break;
                    }
                    case 2: {

                        ((TextView) findViewById(R.id.score_2))
                                .setText(score);
                        break;
                    }
                    case 3: {

                        ((TextView) findViewById(R.id.score_3))
                                .setText(score);
                        break;
                    }
                    case 4: {

                        ((TextView) findViewById(R.id.score_4))
                                .setText(score);
                        break;
                    }
                }

                break;
            }
            case "hand-update": {

                int index = 0;
                int handIndex = 0;

                List<Tile> tiles = (List<Tile>) p.data.get("new-tiles");

                if (!tiles.isEmpty()) {
                    for (Tile t : player.hand) {

                        if (t == null) {
                            player.hand.set(handIndex, tiles.get(index));

                            index++;
                            if (index == tiles.size())
                                break;
                        }

                        handIndex++;
                    }
                    setHandTiles();
                }
                break;
            }
            case "player-joined": {

                int num = (Integer) p.data.get("player-number");
                String name = (String) p.data.get("name");

                switch (num) {
                    case 1: {

                        LinearLayout board = findViewById(R.id.player_1_score);
                        ((TextView) board.findViewById(R.id.name_1))
                                .setText(name);
                        board.setVisibility(View.VISIBLE);

                        break;
                    }
                    case 2: {
                        LinearLayout board = findViewById(R.id.player_2_score);
                        ((TextView) board.findViewById(R.id.name_2))
                                .setText(name);
                        board.setVisibility(View.VISIBLE);

                        break;
                    }
                    case 3: {

                        LinearLayout board = findViewById(R.id.player_3_score);
                        ((TextView) board.findViewById(R.id.name_3))
                                .setText(name);
                        board.setVisibility(View.VISIBLE);

                        break;
                    }
                    case 4: {

                        LinearLayout board = findViewById(R.id.player_4_score);
                        ((TextView) board.findViewById(R.id.name_4))
                                .setText(name);
                        board.setVisibility(View.VISIBLE);

                        break;
                    }
                }

                break;
            }
            case "valid-positions": {

                if (isTrade) {
                    displayMsg("Pass or Trade tiles");
                } else {
                    List<Position> positions = (List<Position>) p.data.get("valid-positions");

                    if (positions == null) {
                        makeAllValid();
                    } else if (positions.isEmpty()) {
                        updateBoard();
                    } else {

                        for (Position position : positions) {

                            ImageView tile = findViewById(resourceID(position.row, position.column));
                            tile.setBackgroundColor(getColor(R.color.white));
                            tile.setClickable(true);
                        }

                    }
                }
                break;
            }
            case "on-board": {

                Tile tile = (Tile) p.data.get("tile");

                List<Tile> board = (List<Tile>) p.data.get("board");

                onBoardTiles = board;


                reUpdateBoard(tile.row, tile.column, board);
                ImageView img = findViewById(resourceID(tile.row, tile.column));
                updateShapeOnImg(img, tile.color, tile.shape);
                img.setBackgroundColor(getColor(R.color.white));

                YoYo.with(Techniques.ZoomIn).duration(600).playOn(img);
                onBoardTiles = (List<Tile>) p.data.get("board");
                updateBoard();

                break;
            }
            case "trade-new": {

                List<Tile> newTiles = (List<Tile>) p.data.get("new");

                if (newTiles.isEmpty())
                    displayMsg("The bag is empty");
                else {
                    if (newTiles.size() == 6) {
                        player.hand = newTiles;
                        setHandTiles();
                        isTrade = true;
                    } else {

                        int index = player.hand.indexOf(selectedTile);
                        Tile tile = newTiles.get(0);
                        player.hand.set(index, tile);
                        isTrade = true;
                        switch (index) {
                            case 0: {
                                updateShapeOnImg(tile_1, tile.color, tile.shape);
                                tile_1.setBackgroundColor(getColor(R.color.black));
                                break;
                            }
                            case 1: {
                                updateShapeOnImg(tile_2, tile.color, tile.shape);
                                tile_2.setBackgroundColor(getColor(R.color.black));
                                break;
                            }
                            case 2: {
                                updateShapeOnImg(tile_3, tile.color, tile.shape);
                                tile_3.setBackgroundColor(getColor(R.color.black));
                                break;
                            }
                            case 3: {
                                updateShapeOnImg(tile_4, tile.color, tile.shape);
                                tile_4.setBackgroundColor(getColor(R.color.black));
                                break;
                            }
                            case 4: {
                                updateShapeOnImg(tile_5, tile.color, tile.shape);
                                tile_5.setBackgroundColor(getColor(R.color.black));
                                break;
                            }
                            case 5: {
                                updateShapeOnImg(tile_6, tile.color, tile.shape);
                                tile_6.setBackgroundColor(getColor(R.color.black));
                                break;
                            }
                        }

                    }
                    displayMsg("Successfully traded");
                }

                selectedTileImg = null;
                selectedTile = null;
                break;
            }
            case "turn": {
                int turn = (int) p.data.get("turn");

                if (turn == player.number)
                    unlock();
                else lock();

                updateTurn(turn);

                break;
            }
            case "game-over": {

                List<PlayerData> playerData = (List<PlayerData>) p.data.get("players");

                if (playerData != null) {
                    playerData.sort(Comparator.comparing(playerData1 -> playerData1.score));
                }

                for (int x = 1; x <= playerData.size(); x++) {

                    switch (x) {
                        case 4: {

                            PlayerData ply = playerData.get(x - 1);

                            ((TextView) findViewById(R.id.final_4))
                                    .setText("" + ply.name + " " + ply.score);
                            break;
                        }
                        case 3: {

                            PlayerData ply = playerData.get(x - 1);

                            ((TextView) findViewById(R.id.final_3))
                                    .setText("" + ply.name + " " + ply.score);
                            break;
                        }
                        case 2: {

                            PlayerData ply = playerData.get(x - 1);

                            ((TextView) findViewById(R.id.final_2))
                                    .setText("" + ply.name + " " + ply.score);
                            break;
                        }
                        case 1: {

                            PlayerData ply = playerData.get(x - 1);

                            ((TextView) findViewById(R.id.final_1))
                                    .setText("" + ply.name + " " + ply.score);
                            break;
                        }
                    }
                }

                ((TextView) findViewById(R.id.final_winner))
                        .setText("Winner! " + playerData.get(playerData.size() - 1).name);

                gameOverLayout.setVisibility(View.VISIBLE);

                YoYo.with(Techniques.Wobble)
                        .duration(1000)
                        .playOn(gameOverLayout);

                YoYo.with(Techniques.SlideInRight)
                        .delay(1000)
                        .duration(1000)
                        .playOn(gameOverLogo);

                YoYo.with(Techniques.FlipInY)
                        .delay(2000)
                        .duration(1500)
                        .playOn(finalScore);

                break;
            }
            case "time": {

                int time = (int) p.data.get("time");

                timer.setText("" + (30 - time));
                playMediaPlayer(R.raw.count_beep);
                if (time == 30) {
                    timer.setVisibility(View.GONE);
                    unlock();
                    displayMsg("START PLAYING, ENJOY!");
                    playMediaPlayer(R.raw.start_playing);
                }

                break;
            }
            case "number-bag": {
                int bag = (int) p.data.get("number-bag");
                numBag.setText("" + bag);
            }

        }
    }

    /**
     * Generate a new grid
     */
    private void createGrid() {

        grid.removeAllViews();

        for (int row = minRow; row <= maxRow; row++) {

            LayoutInflater rowInflater = LayoutInflater
                    .from(grid.getContext());

            LinearLayout rowLayout = (LinearLayout) rowInflater
                    .inflate(R.layout.row_layout, grid,
                            false);
            for (int col = minCol; col <= maxCol; col++) {
                LayoutInflater inflater = LayoutInflater
                        .from(rowLayout.getContext());

                ImageView tile = (ImageView) inflater
                        .inflate(R.layout.tile_layout, rowLayout,
                                false);
                tile.setClickable(false);

                int id = resourceID(row, col);
                tile.setId(id);

                rowLayout.addView(tile);

            }

            grid.addView(rowLayout);

        }

    }

    /**
     * Displays messages on the screen
     *
     * @param msg message
     */
    private void displayMsg(String msg) {

        popup_mgs.setText(msg);

        popup_mgs.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideOutRight).duration(2000).delay(1000)
                .playOn(popup_mgs);

    }

    /**
     * Grid tile onclick method
     *
     * @param view tile
     */
    public void onTileClicked(View view) {

        ImageView img = (ImageView) view;


        if (selectedTile != null) {

            Tile t = new Tile(selectedTile.color, selectedTile.shape);
            t.row = getRow(img);
            t.column = getCol(img);

            Map<String, Object> data = new HashMap<>();
            data.put("selected-tile", t);
            isPlaced = true;
            send(new Package("placing-tile", data));

            player.hand.set(player.hand.indexOf(selectedTile), null);

            selectedTileImg.setVisibility(View.INVISIBLE);
            selectedTile = null;
            selectedTileImg = null;


        } else Toast.makeText(this, "Select a tile from hand", Toast.LENGTH_SHORT).show();
    }

    /**
     * Add shape of given color and shape on an ImageView
     *
     * @param img   ImageView
     * @param color color number
     * @param shape color number
     */
    private void updateShapeOnImg(ImageView img, int color, int shape) {
        int colorShape = Integer.parseInt("" + color + shape);

        switch (colorShape) {
            case 11: {
                img.setImageResource(R.drawable.s11);
                break;
            }
            case 12: {
                img.setImageResource(R.drawable.s12);
                break;
            }
            case 13: {
                img.setImageResource(R.drawable.s13);
                break;
            }
            case 14: {
                img.setImageResource(R.drawable.s14);
                break;
            }
            case 15: {
                img.setImageResource(R.drawable.s15);
                break;
            }
            case 16: {
                img.setImageResource(R.drawable.s16);
                break;
            }

            case 21: {
                img.setImageResource(R.drawable.s21);
                break;
            }
            case 22: {
                img.setImageResource(R.drawable.s22);
                break;
            }
            case 23: {
                img.setImageResource(R.drawable.s23);
                break;
            }
            case 24: {
                img.setImageResource(R.drawable.s24);
                break;
            }
            case 25: {
                img.setImageResource(R.drawable.s25);
                break;
            }
            case 26: {
                img.setImageResource(R.drawable.s26);
                break;
            }

            case 31: {
                img.setImageResource(R.drawable.s31);
                break;
            }
            case 32: {
                img.setImageResource(R.drawable.s32);
                break;
            }
            case 33: {
                img.setImageResource(R.drawable.s33);
                break;
            }
            case 34: {
                img.setImageResource(R.drawable.s34);
                break;
            }
            case 35: {
                img.setImageResource(R.drawable.s35);
                break;
            }
            case 36: {
                img.setImageResource(R.drawable.s36);
                break;
            }

            case 41: {
                img.setImageResource(R.drawable.s41);
                break;
            }
            case 42: {
                img.setImageResource(R.drawable.s42);
                break;
            }
            case 43: {
                img.setImageResource(R.drawable.s43);
                break;
            }
            case 44: {
                img.setImageResource(R.drawable.s44);
                break;
            }
            case 45: {
                img.setImageResource(R.drawable.s45);
                break;
            }
            case 46: {
                img.setImageResource(R.drawable.s46);
                break;
            }

            case 51: {
                img.setImageResource(R.drawable.s51);
                break;
            }
            case 52: {
                img.setImageResource(R.drawable.s52);
                break;
            }
            case 53: {
                img.setImageResource(R.drawable.s53);
                break;
            }
            case 54: {
                img.setImageResource(R.drawable.s54);
                break;
            }
            case 55: {
                img.setImageResource(R.drawable.s55);
                break;
            }
            case 56: {
                img.setImageResource(R.drawable.s56);
                break;
            }

            case 61: {
                img.setImageResource(R.drawable.s61);
                break;
            }
            case 62: {
                img.setImageResource(R.drawable.s62);
                break;
            }
            case 63: {
                img.setImageResource(R.drawable.s63);
                break;
            }
            case 64: {
                img.setImageResource(R.drawable.s64);
                break;
            }
            case 65: {
                img.setImageResource(R.drawable.s65);
                break;
            }
            case 66: {
                img.setImageResource(R.drawable.s66);
                break;
            }

        }

        if (img.getVisibility() == View.INVISIBLE)
            img.setVisibility(View.VISIBLE);

    }

    /**
     * Update who is turn is to play on the screen
     *
     * @param turn player number
     */
    private void updateTurn(int turn) {
        score_board_1.setBackgroundColor(getColor(R.color.white));

        score_board_2.setBackgroundColor(getColor(R.color.white));

        score_board_3.setBackgroundColor(getColor(R.color.white));

        score_board_4.setBackgroundColor(getColor(R.color.white));

        switch (turn) {
            case 1: {
                score_board_1.setBackgroundColor(getColor(R.color.turn_color));

                break;
            }
            case 2: {
                score_board_2.setBackgroundColor(getColor(R.color.turn_color));


                break;
            }
            case 3: {
                score_board_3.setBackgroundColor(getColor(R.color.turn_color));

                break;
            }
            case 4: {
                score_board_4.setBackgroundColor(getColor(R.color.turn_color));

                break;
            }

        }

    }

    private void place(List<Tile> list) {
        for (Tile tile : list) {
            ImageView img = findViewById(resourceID(tile.row, tile.column));
            img.setBackgroundColor(getColor(R.color.black));
            img.setClickable(false);

            YoYo.with(Techniques.SlideInDown).duration(500).playOn(img);
        }
    }

    private void playMediaPlayer(int uri) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(
                    new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stopMediaPlayer();
                        }
                    }
            );
        }
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void updateBoard() {

        if (onBoardTiles != null) {
            for (int row = minRow; row <= maxRow; row++)
                for (int column = minCol; column <= maxCol; column++) {

                    int finalColumn = column;
                    int finalRow = row;

                    long count = 0;
                    if (onBoardTiles != null)
                        count = onBoardTiles.stream()
                                .filter(tile ->
                                        tile.row == finalRow
                                                && tile.column == finalColumn)
                                .count();
                    if (count == 0) {

                        ImageView tile = findViewById(resourceID(row, column));
                        tile.setBackgroundColor(getColor(R.color.my_pink));
                        tile.setClickable(false);
                    }
                }
        }
    }

    private void reattachImages(List<Tile> list) {
        for (Tile tile : list) {

            ImageView img = findViewById(resourceID(tile.row, tile.column));

            if (tile.placement == 4)
                img.setBackgroundColor(getColor(R.color.black));
            else
                img.setBackgroundColor(getColor(R.color.white));


            updateShapeOnImg(img, tile.color, tile.shape);


        }
    }

    private void reUpdateBoard(int row, int col, List<Tile> list) {
        if (row == minRow) {
            minRow -= 1;
            numRow += 1;

            createGrid();
            reattachImages(list);

        } else if (row == maxRow) {
            maxRow += 1;
            numRow += 1;

            createGrid();
            reattachImages(list);
        }

        // Column
        if (col == minCol) {
            minCol -= 1;
            numCol += 1;

            createGrid();
            reattachImages(list);

        } else if (col == maxCol) {

            maxCol += 1;
            numCol += 1;
            createGrid();
            reattachImages(list);
        }

        // updateBoard();

    }

    private void makeAllValid() {
        for (int row = minRow; row <= maxRow; row++)
            for (int column = minCol; column <= maxCol; column++) {

                ImageView tile = findViewById(resourceID(row, column));
                tile.setBackgroundColor(getColor(R.color.white));
                tile.setClickable(true);
            }
    }

    private void setUpPdf() {

        pdfView.fromAsset("qwirkle_rules.pdf")
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .load();
    }

    private void setHandTiles() {

        Tile t1 = player.hand.get(0);
        Tile t2 = player.hand.get(1);
        Tile t3 = player.hand.get(2);
        Tile t4 = player.hand.get(3);
        Tile t5 = player.hand.get(4);
        Tile t6 = player.hand.get(5);

        if (t1 != null) {
            updateShapeOnImg(tile_1, t1.color, t1.shape);
            YoYo.with(Techniques.SlideInLeft).duration(700).playOn(tile_1);
            tile_1.setBackgroundColor(getColor(R.color.dark_blue));
        }

        if (t2 != null) {
            updateShapeOnImg(tile_2, t2.color, t2.shape);
            YoYo.with(Techniques.SlideInLeft).duration(700).playOn(tile_2);
            tile_2.setBackgroundColor(getColor(R.color.dark_blue));
        }

        if (t3 != null) {
            updateShapeOnImg(tile_3, t3.color, t3.shape);
            YoYo.with(Techniques.SlideInLeft).duration(700).playOn(tile_3);
            tile_3.setBackgroundColor(getColor(R.color.dark_blue));
        }

        if (t4 != null) {
            updateShapeOnImg(tile_4, t4.color, t4.shape);
            YoYo.with(Techniques.SlideInLeft).duration(700).playOn(tile_4);
            tile_4.setBackgroundColor(getColor(R.color.dark_blue));
        }

        if (t5 != null) {
            updateShapeOnImg(tile_5, t5.color, t5.shape);
            YoYo.with(Techniques.SlideInLeft).duration(700).playOn(tile_5);
            tile_5.setBackgroundColor(getColor(R.color.dark_blue));
        }

        if (t6 != null) {
            updateShapeOnImg(tile_6, t6.color, t6.shape);
            YoYo.with(Techniques.SlideInLeft).duration(700).playOn(tile_6);
            tile_6.setBackgroundColor(getColor(R.color.dark_blue));
        }

    }

    private void handTiles() {

        tile_1 = findViewById(R.id.hand_tile_1);
        tile_2 = findViewById(R.id.hand_tile_2);
        tile_3 = findViewById(R.id.hand_tile_3);
        tile_4 = findViewById(R.id.hand_tile_4);
        tile_5 = findViewById(R.id.hand_tile_5);
        tile_6 = findViewById(R.id.hand_tile_6);

        tile_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTileImg != null && selectedTileImg.isClickable())
                    selectedTileImg.setBackgroundColor(getColor(R.color.dark_blue));
                if (selectedTileImg != null)
                    updateBoard();


                tile_1.setBackgroundColor(getColor(R.color.white));
                YoYo.with(Techniques.FlipInY)
                        .duration(600)
                        .playOn(tile_1);

                selectedTile = player.hand.get(0);
                selectedTileImg = tile_1;

                Map<String, Object> data = new HashMap<>();
                data.put("selected-tile", selectedTile);
                player.sendPackage(new Package("selected-tile", data));

            }
        });
        tile_2.setOnClickListener(view -> {

            if (selectedTileImg != null && selectedTileImg.isClickable())
                selectedTileImg.setBackgroundColor(getColor(R.color.dark_blue));

            if (selectedTileImg != null)
                updateBoard();

            tile_2.setBackgroundColor(getColor(R.color.white));
            YoYo.with(Techniques.FlipInY)
                    .duration(600)
                    .playOn(tile_2);


            selectedTile = player.hand.get(1);
            selectedTileImg = tile_2;

            Map<String, Object> data = new HashMap<>();
            data.put("selected-tile", selectedTile);
            player.sendPackage(new Package("selected-tile", data));


        });
        tile_3.setOnClickListener(view -> {
            if (selectedTileImg != null && selectedTileImg.isClickable())
                selectedTileImg.setBackgroundColor(getColor(R.color.dark_blue));
            if (selectedTileImg != null)
                updateBoard();
            tile_3.setBackgroundColor(getColor(R.color.white));
            YoYo.with(Techniques.FlipInY)
                    .duration(600)
                    .playOn(tile_3);


            selectedTile = player.hand.get(2);
            selectedTileImg = tile_3;

            Map<String, Object> data = new HashMap<>();
            data.put("selected-tile", selectedTile);
            player.sendPackage(new Package("selected-tile", data));

        });
        tile_4.setOnClickListener(view -> {
            if (selectedTileImg != null && selectedTileImg.isClickable())
                selectedTileImg.setBackgroundColor(getColor(R.color.dark_blue));
            if (selectedTileImg != null)
                updateBoard();
            tile_4.setBackgroundColor(getColor(R.color.white));
            YoYo.with(Techniques.FlipInY)
                    .duration(600)
                    .playOn(tile_4);


            selectedTile = player.hand.get(3);
            selectedTileImg = tile_4;

            Map<String, Object> data = new HashMap<>();
            data.put("selected-tile", selectedTile);
            player.sendPackage(new Package("selected-tile", data));

        });
        tile_5.setOnClickListener(view -> {

            if (selectedTileImg != null && selectedTileImg.isClickable())
                selectedTileImg.setBackgroundColor(getColor(R.color.dark_blue));
            if (selectedTileImg != null)
                updateBoard();
            tile_5.setBackgroundColor(getColor(R.color.white));
            YoYo.with(Techniques.FlipInY)
                    .duration(600)
                    .playOn(tile_5);


            selectedTile = player.hand.get(4);
            selectedTileImg = tile_5;

            Map<String, Object> data = new HashMap<>();
            data.put("selected-tile", selectedTile);
            player.sendPackage(new Package("selected-tile", data));

        });
        tile_6.setOnClickListener(view -> {

            if (selectedTileImg != null && selectedTileImg.isClickable())
                selectedTileImg.setBackgroundColor(getColor(R.color.dark_blue));
            if (selectedTileImg != null)
                updateBoard();
            tile_6.setBackgroundColor(getColor(R.color.white));
            YoYo.with(Techniques.FlipInY)
                    .duration(600)
                    .playOn(tile_6);


            selectedTile = player.hand.get(5);
            selectedTileImg = tile_6;

            Map<String, Object> data = new HashMap<>();
            data.put("selected-tile", selectedTile);
            player.sendPackage(new Package("selected-tile", data));

        });


    }

    private void quitAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setTitle(getString(R.string.alert_title_quit));
        builder.setMessage(getString(R.string.alert_msg_quit));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                // Disconnect from server
                disconnectServer();

                // Back to initial screen
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        quitAlert = builder.create();
    }

    private void disconnectServer() {
        // Code that disconnect from server
        // sendPackage(new Package("quit",null));
    }

    private void bottomButtons() {

        //   AudioManager myAudioManager;
        //   myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);


        pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                int count = (int) player.hand.stream()
                        .filter(tile -> tile == null)
                        .count();
                Map<String, Object> data = new HashMap<>();
                data.put("num-new", count);

                send(new Package("place-tiles", data));
                isTrade = false;
                isPlaced = false;

            }
        });

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (soundOn) {
                    sound.setImageResource(R.drawable.my_no_sound);
                    soundOn = false;

                    displayMsg("SOUND OFF");
                    // myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else {
                    sound.setImageResource(R.drawable.my_sound);
                    soundOn = true;
                    displayMsg("SOUND ON");
                    //  myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }

            }
        });

        trade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaced) {

                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.trade_items, popupMenu.getMenu());


                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            switch (menuItem.getItemId()) {
                                case R.id.all: {

                                    long count = player.hand
                                            .stream().filter(tile -> tile == null).count();

                                    if (count == 0) {

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("tiles", player.hand);

                                        send(new Package("trade-tiles", data));
                                    } else displayMsg("You have less than 6, trade singles");
                                    break;
                                }
                                case R.id.selected: {

                                    if (selectedTile != null) {

                                        List<Tile> select = new ArrayList<>();
                                        select.add(selectedTile);

                                        Map<String, Object> data = new HashMap<>();
                                        // data.put("index",player.hand.indexOf(selectedTile));
                                        data.put("tiles", select);

                                        send(new Package("trade-tiles", data));
                                    }
                                    break;
                                }
                            }

                            return true;
                        }
                    });
                    // Showing the popup menu

                    popupMenu.show();

                } else displayMsg("Can only trade before placing");


            }
        });

    }

    private void zoom() {

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomLayout.zoomIn();
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomLayout.zoomOut();
            }
        });

    }

    private void lock() {

        tile_1.setClickable(false);
        tile_2.setClickable(false);
        tile_3.setClickable(false);
        tile_4.setClickable(false);
        tile_5.setClickable(false);
        tile_6.setClickable(false);

        trade.setClickable(false);
        pass.setClickable(false);

    }

    private void unlock() {
        tile_1.setClickable(true);
        tile_2.setClickable(true);
        tile_3.setClickable(true);
        tile_4.setClickable(true);
        tile_5.setClickable(true);
        tile_6.setClickable(true);

        trade.setClickable(true);
        pass.setClickable(true);

    }

    private int getRow(ImageView img) {
        String id = "" + img.getId();

        String col = "" + id.charAt(0)
                + id.charAt(1)
                + id.charAt(2)
                + id.charAt(3);

        if (col.startsWith("1"))
            col = replaceFirst("-", col);
        else if (col.startsWith("2"))
            col = replaceFirst("", col);
        else
            col = "0";

        return Integer.parseInt(col);
    }

    private int getCol(ImageView img) {
        String id = "" + img.getId();

        String col = "" + id.charAt(5)
                + id.charAt(6)
                + id.charAt(7)
                + id.charAt(8);

        if (col.startsWith("1"))
            col = replaceFirst("-", col);
        else if (col.startsWith("2"))
            col = replaceFirst("", col);
        else
            col = "0";

        return Integer.parseInt(col);
    }

    private String replaceFirst(String with, String str) {
        String text = with;

        for (int x = 1; x < str.length(); x++)
            text += str.charAt(x);

        return text;
    }

    private int resourceID(int row, int col) {


        String str = genFourNum(row) + "0" + genFourNum(col);
        return Integer.parseInt(str);
    }

    private String genFourNum(int num) {
        String strNum = "" + num;

        String sign = "";


        if (strNum.startsWith("-")) {
            strNum = replaceFirst("", strNum);
            sign = "1";
        } else if (strNum.startsWith("0"))
            sign = "3";
        else sign = "2";


        if (strNum.length() == 1)
            strNum = "00" + strNum;
        else if (strNum.length() == 2)
            strNum = "0" + strNum;

        return sign + strNum;

    }

    @Override
    public void onBackPressed() {

        quitAlert.show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMediaPlayer();
    }

}