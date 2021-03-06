package it.polimi.ingsw.view.graphical_view;

import it.polimi.ingsw.ViewClient;
import it.polimi.ingsw.connection.ConnectionTech;
import it.polimi.ingsw.connection.client.RmiClient;
import it.polimi.ingsw.connection.client.SClient;
import it.polimi.ingsw.connection.client.SClientCommManager;
import it.polimi.ingsw.utils.FileLoader;
import it.polimi.ingsw.view.virtual_model.*;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import static it.polimi.ingsw.view.command_line_view.CliMessages.CONNECTION_ERR;
import static it.polimi.ingsw.view.graphical_view.GUIFiles.*;
import static java.lang.Thread.sleep;


/**
 * @author Carlo Bellacoscia
 */
public class LobbyJavaFX extends Application {

    public static final int THREAD_PRIORITY=3;

    private double widthScreen;
    private double widthLateral;
    private double widthCenter;
    private double heightScreen;
    private double heightRows;
    private double heightTitle;
    private double heightMaps;
    private double heightPlayers;

    private boolean reconnected;
    private Thread t;

    private boolean terminator;
    private boolean frenzy;
    private int map;
    private int skull;
    private ConnectionTech conn;
    private VirtualPlayer owner;
    private FileLoader fileLoader = new FileLoader();
    private VirtualModel vmodel;
    private GameJavaFX game;

    private boolean start;

    Font font = new Font("TimesRoman", 20);
    Font titleFont = new Font("TimesRoman", 36);

    private VirtualLobby lobby;
    private ArrayList<VirtualPlayer> joinedPlayers = new ArrayList<>();

    private Stage primaryStage;

    /**
     *
     * @param conn the connection used
     * @param owner the owner player
     * @param reconnected if is a reconnected player
     */
    public LobbyJavaFX(ConnectionTech conn, VirtualPlayer owner,boolean reconnected) {

        vmodel = new VirtualModel(owner);

        this.reconnected = reconnected;
        this.conn = conn;
        this.owner = owner;
        this.lobby = new VirtualLobby(conn, owner);
        game = new GameJavaFX(vmodel);
        terminator = false;
        frenzy = false;
        map = 0;
        skull = 0;
        start = false;

        widthScreen = Screen.getPrimary().getBounds().getWidth();
        widthLateral = widthScreen/6;
        widthCenter = widthLateral * 4;

        heightScreen = Screen.getPrimary().getBounds().getHeight();
        heightRows = heightScreen / 20;
        heightTitle = heightRows * 2;
        heightMaps = heightRows * 3;
        heightPlayers = heightRows * 4;
    }

    public VirtualPlayer getOwner() {
        return owner;
    }

    /**
     *
     * @param primaryStage the stage
     * @throws Exception
     */
    public void start(Stage primaryStage) throws Exception {

        t = new Thread(() -> {
            try {
                while(true) {
                    lobby.pingRmi();
                    Thread.sleep(500);
                }
            } catch (IOException | InterruptedException ignored) {
            }
        });

        if(conn.isRmi()&&!reconnected) {
            t.start();
        }



        this.primaryStage = primaryStage;


        primaryStage.setTitle("Lobby");

        Group root = new Group();
        Scene theScene = new Scene(root);
        primaryStage.setScene(theScene);

        GridPane lobbyGrid = new GridPane();
        lobbyGrid.setAlignment(Pos.CENTER);
        lobbyGrid.setHgap(0);
        lobbyGrid.setVgap(0);
        lobbyGrid.setPadding(new Insets(50, 50, 50, 50));

        Scene scene = new Scene(lobbyGrid, widthScreen, heightScreen);
        primaryStage.setScene(scene);

        RowConstraints r1 = new RowConstraints(heightRows);
        RowConstraints r2 = new RowConstraints(heightTitle);
        RowConstraints r3 = new RowConstraints(heightRows);
        RowConstraints r4 = new RowConstraints(heightRows);
        RowConstraints r5 = new RowConstraints(heightRows);
        RowConstraints r6 = new RowConstraints(heightRows);
        RowConstraints r7 = new RowConstraints(heightMaps);
        RowConstraints r8 = new RowConstraints(heightRows);
        RowConstraints r9 = new RowConstraints(heightPlayers);
        RowConstraints r10 = new RowConstraints(heightRows);
        RowConstraints r11 = new RowConstraints(heightRows);
        RowConstraints r12 = new RowConstraints(heightRows);
        RowConstraints r13 = new RowConstraints(heightRows);


        ColumnConstraints c1 = new ColumnConstraints(widthLateral);
        ColumnConstraints c2 = new ColumnConstraints(widthCenter);
        ColumnConstraints c3 = new ColumnConstraints(widthLateral);

        lobbyGrid.getRowConstraints().addAll(r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,r13);
        lobbyGrid.getColumnConstraints().addAll(c1,c2,c3);

        Text title = new Text("Lobby");
        title.setFont(titleFont);

        //set background
        Image back = new Image(fileLoader.getResource(PATH_BACK), widthScreen * 2, heightScreen * 2, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(back, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        lobbyGrid.setBackground(new Background(backgroundImage));

        // skulls choise
        javafx.scene.control.Label lblSkulls = new javafx.scene.control.Label("Numero di teschi:");
        lblSkulls.setFont(font);
        ObservableList<String> skuItems = FXCollections.observableArrayList("5", "6", "7", "8");
        ComboBox skuBox = new ComboBox(skuItems);
        HBox skuH = new HBox(20);
        skuH.getChildren().add(lblSkulls);
        skuH.getChildren().add(skuBox);

        //terminator choise
       javafx.scene.control.Label lblTerminator = new javafx.scene.control.Label("Aggiungere Terminator?");
        lblTerminator.setFont(font);
        ObservableList<String> terItems = FXCollections.observableArrayList("SI", "NO");
        ComboBox terBox = new ComboBox(terItems);
        HBox terH = new HBox(20);
        terH.getChildren().add(lblTerminator);
        terH.getChildren().add(terBox);

        //frenzy choise
        javafx.scene.control.Label lblFrenzy = new javafx.scene.control.Label("Aggiungere Frenesia Finale?");
        lblFrenzy.setFont(font);
        ObservableList<String> freItems = FXCollections.observableArrayList("SI", "NO");
        ComboBox freBox = new ComboBox(freItems);
        HBox freH = new HBox(20);
        freH.getChildren().add(lblFrenzy);
        freH.getChildren().add(freBox);

        //map Buttons
        Label lblMap = new Label("Scegli una mappa:");
        lblMap.setFont(font);
        Button map1 = new Button();
        javafx.scene.image.Image img1 = new Image(fileLoader.getResource(PATH_MEDIUM1_MAP), 150, 150, true, true);
        map1.setGraphic(new ImageView(img1));
        Button map2 = new Button();
        javafx.scene.image.Image img2 = new Image(fileLoader.getResource(PATH_SMALL_MAP), 150, 150, true, true);
        map2.setGraphic(new ImageView(img2));
        Button map3 = new Button();
        javafx.scene.image.Image img3 = new Image(fileLoader.getResource(PATH_LARGE_MAP), 150, 150, true, true);
        map3.setGraphic(new ImageView(img3));
        Button map4 = new Button();
        javafx.scene.image.Image img4 = new Image(fileLoader.getResource(PATH_MEDIUM2_MAP), 150, 150, true, true);
        map4.setGraphic(new ImageView(img4));
        HBox mapBox = new HBox(50);
        mapBox.setAlignment(Pos.CENTER);
        mapBox.getChildren().add(map1);
        mapBox.getChildren().add(map2);
        mapBox.getChildren().add(map3);
        mapBox.getChildren().add(map4);

        Label lblPlayer = new Label();
        HBox playerBox = new HBox();




        Button btnStart = new Button("PRONTO");
        btnStart.setAlignment(Pos.CENTER);
        btnStart.setFont(font);



        //merge layout
        lobbyGrid.add(title, 1, 1);
        lobbyGrid.add(skuH, 1, 2);
        lobbyGrid.add(terH, 1, 3);
        lobbyGrid.add(freH, 1, 4);
        lobbyGrid.add(lblMap, 1, 5);
        lobbyGrid.add(mapBox, 1, 6);

        lobbyGrid.add(lblPlayer, 1, 7);
        lobbyGrid.add(playerBox, 1, 8);

        lobbyGrid.add(btnStart, 1, 9);

        ImageView load = null;

            load = new ImageView(new Image(fileLoader.getResource(PATH_LOADING), heightRows, heightRows, true, true));

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(3000), load);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setCycleCount(Timeline.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        ImageView finalLoad = load;
        lobbyGrid.add(finalLoad, 2, 10);


        //buttons action
         skuBox.setOnAction(e -> {
            skull = Integer.parseInt(skuBox.getValue().toString());
        });


        terBox.setOnAction(e -> {
            if (terBox.getValue().toString().equals("SI")) {
                terminator = true;
            } else {
                terminator = false;
            }
        });

        freBox.setOnAction(e -> {
            if (freBox.getValue().toString().equals("SI")) {
                frenzy = true;
            } else {
                frenzy = false;
            }
        });

        map1.setOnAction(e -> map = 1);
        map2.setOnAction(e -> map = 2);
        map3.setOnAction(e -> map = 3);
        map4.setOnAction(e -> map = 4);


        Thread thread = new Thread(()->{
            System.out.println("thread");

            if(t.isAlive())
                t.interrupt();

            while(!lobby.isGameStarted()){
                try {
                        lobby.waitUpdate();


                    for (VirtualPlayer p : lobby.getNewPlayersList()) {
                        if(!p.isPrinted()) {
                            Platform.runLater(() -> {

                                playerBox.getChildren().add(setJoinedPlayers(p));
                            });
                            p.setPrinted(true);
                        }
                    }

                } catch (IOException ex) {
                    System.err.println(CONNECTION_ERR);
                    System.exit(0);
                }
            }
            System.out.println("game started");

            Platform.runLater(()->gameStart());

        });

        btnStart.setOnAction(e -> {

            lobby.setKillPref(skull);
            lobby.setTerminatorPref(terminator);
            lobby.setFinalFrenzyPref(frenzy);
            lobby.setMapPref(map);


            try {
                lobby.sendPref();

                btnStart.setVisible(false);
                rotateTransition.play();
                thread.setPriority(THREAD_PRIORITY);
                thread.start();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            lblPlayer.setText("Player connessi:");
            lblPlayer.setFont(font);


        });

        primaryStage.setMaximized(true);

        //RECONNECTION
        if(reconnected){
            gameStart();
        }
        primaryStage.show();

    }

    /**
     * starts the game
     */
    public void gameStart(){



        if(!conn.isRmi()){
            ((SClient)conn).setCommManager(new SClientCommManager(((SClient)conn),game));
            ((SClient)conn).getCommManager().setPriority(THREAD_PRIORITY);
            ((SClient)conn).getCommManager().start();
        }else{
            try {
                ((RmiClient)conn).getClientHandler().setView((ViewClient) UnicastRemoteObject.exportObject(game, 0));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
         }


        try {
        while(!vmodel.isUpdated()){
                sleep(500);
        }

            sleep(1000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            game.setStart(true);

        try {
            game.start(primaryStage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * add a player in the list of players connected
     *
     * @param p player connected
     * @return
     */
    public VBox setJoinedPlayers(VirtualPlayer p){

            VBox v = new VBox();


                switch (p.getCharacter()) {
                    case "yellow": {
                        ImageView yellowV = new ImageView(new Image(fileLoader.getResource(PATH_YELLOW_CHARACTER), widthCenter, heightPlayers, true, true));
                        v.getChildren().add(yellowV);
                        break;
                    }
                    case "red": {
                        ImageView redV = new ImageView(new Image(fileLoader.getResource(PATH_RED_CHARACTER), widthCenter, heightPlayers, true, true));
                        v.getChildren().add(redV);
                        break;
                    }
                    case "blue": {
                        ImageView blueV = new ImageView(new Image(fileLoader.getResource(PATH_BLUE_CHARACTER), widthCenter, heightPlayers, true, true));
                        v.getChildren().add(blueV);
                        break;
                    }
                    case "green": {
                        ImageView greenV = new ImageView(new Image(fileLoader.getResource(PATH_GREEN_CHARACTER), widthCenter, heightPlayers, true, true));
                        v.getChildren().add(greenV);
                        break;
                    }
                    case "grey": {
                        ImageView greyV = new ImageView(new Image(fileLoader.getResource(PATH_GREY_CHARACTER), widthCenter, heightPlayers, true, true));
                        v.getChildren().add(greyV);
                        break;
                    }
                }


            TextField txtUsername = new TextField(p.getUsername());
            txtUsername.setPrefSize(widthCenter/5, heightRows);
            txtUsername.setFont(font);
            txtUsername.setEditable(false);
            v.getChildren().add(txtUsername);


        return v;
    }
}