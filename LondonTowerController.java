/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package education;

import static education.Education.myRestObj;
import static education.Education.myStatObj;
import static education.Education.root_game;
import static education.Education.scene_game;
import static education.Education.stage_game;
import static education.FXMLMainController.validID;
import helper.Coin;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Sherif_11211
 */
public class LondonTowerController implements Initializable {

    @FXML
    private Button left;
    @FXML
    private Button right;
    @FXML
    private Button leftD;
    @FXML
    private Button rightD;
    @FXML
    private AnchorPane anchor;
    @FXML
    private Label Level;
    @FXML
    private Label Category;
    @FXML
    private Label movement;
    @FXML
    private Label myCount;
    @FXML
    private ImageView result;
    @FXML
    private ImageView imageViewPlay;
    @FXML
    private HBox HBoxPlay;
    @FXML
    private HBox NotTrialLabel1;
    @FXML
    private HBox NotTrialLabel2;
    @FXML
    private AnchorPane TrialLabel;

    Coin[] coins = new Coin[6];
    Coin selectedCoin;
    int CX, CY, DX, DY;
    int goingToPosition = 0;

    int currentLevel = 0;
    int currentGroup = 0;
    int currentCategory = 0;

    int score = 0;
    boolean isTrial = false;
    int movements = 0;
    int seconds = 0;
    private Timer myTimer;
    private TimerTask task;

    private int minimize;
    private int preSeconds;

    boolean[] disableButtons = new boolean[4];

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        TrialLabel.setVisible(false);
        NotTrialLabel1.setVisible(false);
        NotTrialLabel2.setVisible(false);

        imageViewPlay.setOnMouseClicked((event) -> {
            if (Education.withTime) {
                myCount.setText(5 + "");
            } else {
                myCount.setText("0");
            }
            imageViewPlay.setVisible(false);
            result.setImage(null);
            setCounter();
            newLevel();
        });
        hideAll();
        Level.setVisible(false);
        Category.setVisible(false);

        // === ...
        stage_game.setOnHiding((event) -> {
            if (myTimer != null) {
                myTimer.cancel();
            }
        });
    }

    public void newLevel() {
        for (int i = 0; i < coins.length; i++) {
            if (coins[i] != null) {
                anchor.getChildren().remove(coins[i]);
            }
        }
        HBoxPlay.setVisible(false);
        movements = 0;
        int R, Y, B;
        try {
            myRestObj = myStatObj.executeQuery("SELECT * FROM SHERIF.STUDENTS WHERE ID = " + validID);
            while (myRestObj.next()) {
                currentLevel = myRestObj.getInt("TOWERLEVEL");
                score = myRestObj.getInt("TOWERSCORE");
                preSeconds = myRestObj.getInt("TOWERTIME");
            }
            if (currentLevel >= 13) {
                // finish
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("انتهت");
                alert.setHeaderText(null);
                alert.setContentText("لقد انتهت هذه اللعبة");
                alert.showAndWait();
                try {
                    root_game = FXMLLoader.load(getClass().getResource("FinishTest.fxml"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                scene_game = new Scene(root_game);
                stage_game.setScene(scene_game);
                stage_game.setTitle("education");
                stage_game.setResizable(false);
                stage_game.show();
            } else if (currentLevel == 0) {
                //
                TrialLabel.setVisible(true);
                NotTrialLabel1.setVisible(false);
                NotTrialLabel2.setVisible(false);

                mapToGroupNCat();
                isTrial = true;
                myRestObj = myStatObj.executeQuery("SELECT * FROM SHERIF.LONDONINFO WHERE ID = 0" /*+ currentLevel*/);
            } else {
                //
                TrialLabel.setVisible(false);
                NotTrialLabel1.setVisible(true);
                NotTrialLabel2.setVisible(true);
                
                myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TRIAL" + currentLevel + " = " + 0 + " WHERE ID = " + validID);

                mapToGroupNCat();
                isTrial = false;
                myRestObj = myStatObj.executeQuery("SELECT * FROM SHERIF.LONDONINFO WHERE ID = " + currentLevel);
            }
            while (myRestObj.next()) {
                R = myRestObj.getInt("RED");
                Y = myRestObj.getInt("YELLOW");
                B = myRestObj.getInt("BLUE");
                coins[0] = buildCoin(R, Color.RED);
                coins[3] = buildDesired(R, Color.RED);
                coins[1] = buildCoin(Y, Color.YELLOW);
                coins[4] = buildDesired(Y, Color.YELLOW);
                coins[2] = buildCoin(B, Color.BLUE);
                coins[5] = buildDesired(B, Color.BLUE);

                coins[0].setOnMouseClicked(event -> {
                    select(coins[0]);
                });
                coins[1].setOnMouseClicked(event -> {
                    select(coins[1]);
                });
                coins[2].setOnMouseClicked(event -> {
                    select(coins[2]);
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        minimize = currentGroup / 3 + 1;
        movement.setText(movements + "/" + ((currentGroup + 1) * 2 - minimize));
        //
        Level.setVisible(true);
        Category.setVisible(true);
        Level.setText(currentGroup + "");
        Category.setText(currentCategory + "");
        //Score.setText(score + "");
    }

    @FXML
    public void right(ActionEvent event) {
        //
        updateTrial(currentLevel);
        //
        if (disableButtons[0]) {
            vibrateCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY());
            return;
        }
        ArrayList<Integer> aval = new ArrayList<>();
        aval.add(coins[0].getPosition());
        aval.add(coins[1].getPosition());
        aval.add(coins[2].getPosition());
        switch (selectedCoin.getPosition()) {
            case 1:
            case 2:
            case 3:
                goingToPosition = 4;
                if (aval.contains(4)) {
                    goingToPosition = 5;
                }
                break;
            case 4:
            case 5:
                goingToPosition = 6;
                break;
        }
        moveCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY(), 1);
    }

    @FXML
    public void left(ActionEvent event) {
        //
        updateTrial(currentLevel);
        //
        if (disableButtons[1]) {
            vibrateCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY());
            return;
        }
        ArrayList<Integer> aval = new ArrayList<>();
        aval.add(coins[0].getPosition());
        aval.add(coins[1].getPosition());
        aval.add(coins[2].getPosition());
        switch (selectedCoin.getPosition()) {
            case 4:
            case 5:
                goingToPosition = 1;
                if (aval.contains(2)) {
                    goingToPosition = 3;
                } else if (aval.contains(1)) {
                    goingToPosition = 2;
                }
                break;
            case 6:
                goingToPosition = 4;
                if (aval.contains(4)) {
                    goingToPosition = 5;
                }
                break;
        }
        moveCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY(), 0);
    }

    @FXML
    public void doubleRight(ActionEvent event) {
        //
        updateTrial(currentLevel);
        //
        if (disableButtons[2]) {
            vibrateCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY());
            return;
        }
        goingToPosition = 6;
        moveCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY(), 3);
    }

    @FXML
    public void doubleLeft(ActionEvent event) {
        //
        updateTrial(currentLevel);
        //
        if (disableButtons[3]) {
            vibrateCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY());
            return;
        }
        ArrayList<Integer> aval = new ArrayList<>();
        aval.add(coins[0].getPosition());
        aval.add(coins[1].getPosition());
        aval.add(coins[2].getPosition());
        goingToPosition = 1;
        if (aval.contains(2)) {
            goingToPosition = 3;
        } else if (aval.contains(1)) {
            goingToPosition = 2;
        }
        moveCoin(selectedCoin.getCurrentX(), selectedCoin.getCurrentY(), 2);
    }

    public void moveCoin(int currentX, int currentY, int directionIndex) {
        hideAll();
        setCXCY(goingToPosition);
        Double GoToX;
        boolean SWEEP = false;
        int radiusY = 30;
        switch (directionIndex) {
            case 0:
                GoToX = currentX - 100.0;
                break;
            case 1:
                GoToX = currentX + 100.0;
                SWEEP = true;
                break;
            case 2:
                GoToX = currentX - 200.0;
                radiusY = 15;
                break;
            case 3:
                GoToX = currentX + 200.0;
                radiusY = 15;
                SWEEP = true;
                break;
            default:
                return;
        }
        ArcTo arcTo = new ArcTo();
        arcTo.setSweepFlag(SWEEP);
        arcTo.setX(GoToX);
        arcTo.setY(150);
        arcTo.setRadiusX(30.0);
        arcTo.setRadiusY(radiusY);

        Path pth = new Path();
        pth.getElements().add(new MoveTo(currentX, currentY));
        pth.getElements().add(new LineTo(currentX, 150));       //   fixed
        pth.getElements().add(arcTo);
        pth.getElements().add(new LineTo(GoToX, CY));

        PathTransition transition = new PathTransition();
        transition.setNode(selectedCoin);
        transition.setDuration(Duration.millis(1000));
        transition.setPath(pth);
        transition.setCycleCount(1);
        //
        selectedCoin.setPosition(goingToPosition);
        //set location
        selectedCoin.setCurrentX(CX);
        selectedCoin.setCurrentY(CY);
        selectedCoin.setStroke(null);
        //
        transition.play();
        transition.setOnFinished((event) -> {
            movements++;
            movement.setText(movements + "/" + ((currentGroup + 1) * 2 - minimize));
            if (coins[0].getPosition() == coins[3].getPosition() && coins[1].getPosition() == coins[4].getPosition() && coins[2].getPosition() == coins[5].getPosition()) {
                //System.out.println("................................................ successfully passed Red ...");
                if (Education.withTime) {
                    //
                } else {
                    saveTheTime();
                }
                whenRight();
            } else if (movements >= (currentGroup + 1) * 2 - minimize) {
                if (Education.withTime) {
                    //
                } else {
                    saveTheTime();
                }
                whenWrong();
            }
        });
    }

    public void vibrateCoin(int currentX, int currentY) {

        hideAll();

        Path pth = new Path();
        pth.getElements().add(new MoveTo(currentX, currentY));
        pth.getElements().add(new LineTo(currentX - 20, currentY));
        pth.getElements().add(new LineTo(currentX + 20, currentY));
        pth.getElements().add(new LineTo(currentX, currentY));

        PathTransition transition = new PathTransition();
        transition.setNode(selectedCoin);
        transition.setDuration(Duration.millis(180));
        transition.setPath(pth);
        transition.setCycleCount(2);

        selectedCoin.setStroke(null);
        //
        transition.play();
        transition.setOnFinished((event) -> {
            movements++;
            movement.setText(movements + "/" + ((currentGroup + 1) * 2 - minimize));
            if (movements >= (currentGroup + 1) * 2 - minimize) {
                if (Education.withTime) {
                    //
                } else {
                    saveTheTime();
                }
                whenWrong();
            }
        });
    }

    public void setCXCY(int pos) {
        switch (pos) {
            case 1:
                CX = 110;
                CY = 280;
                break;
            case 2:
                CX = 110;
                CY = 240;
                break;
            case 3:
                CX = 110;
                CY = 200;
                break;
            case 4:
                CX = 210;
                CY = 280;
                break;
            case 5:
                CX = 210;
                CY = 240;
                break;
            case 6:
                CX = 310;
                CY = 280;
                break;
            default:
                CX = 110;
                CY = 280;
                break;
        }
    }

    public void setDXDY(int pos) {
        switch (pos) {
            case 1:
                DX = 490;
                DY = 280;
                break;
            case 2:
                DX = 490;
                DY = 240;
                break;
            case 3:
                DX = 490;
                DY = 200;
                break;
            case 4:
                DX = 590;
                DY = 280;
                break;
            case 5:
                DX = 590;
                DY = 240;
                break;
            case 6:
                DX = 690;
                DY = 280;
                break;
            default:
                DX = 490;
                DY = 280;
                break;
        }
    }

    public Coin buildCoin(int colorIndex, Color color) {
        Coin coin;
        setCXCY(colorIndex % 10);
        setDXDY(colorIndex / 10);
        coin = new Coin(CX, CY, colorIndex % 10, color, anchor);
        return coin;
    }

    public Coin buildDesired(int colorIndex, Color color) {
        Coin coin;
        setDXDY(colorIndex / 10);
        coin = new Coin(DX, DY, colorIndex / 10, color, anchor);
        return coin;
    }

    public void hideAll() {
        left.setDisable(true);
        right.setDisable(true);
        leftD.setDisable(true);
        rightD.setDisable(true);
    }

    public void showValid(boolean x, boolean c, boolean v, boolean b) {
        left.setDisable(x);
        right.setDisable(c);
        leftD.setDisable(v);
        rightD.setDisable(b);
    }

    public void select(Coin coin) {
        boolean isblow = false;
        hideAll();
        coins[0].setStroke(null);
        coins[1].setStroke(null);
        coins[2].setStroke(null);
        switch (coin.getPosition()) {
            case 1:
                if (coins[0].getPosition() == 2 || coins[1].getPosition() == 2 || coins[2].getPosition() == 2) {
                    isblow = true;
                }
                break;
            case 2:
                if (coins[0].getPosition() == 3 || coins[1].getPosition() == 3 || coins[2].getPosition() == 3) {
                    isblow = true;
                }
                break;
            case 4:
                if (coins[0].getPosition() == 5 || coins[1].getPosition() == 5 || coins[2].getPosition() == 5) {
                    isblow = true;
                }
                break;
            default:
                isblow = false;
                break;
        }
        if (!isblow) {
            coin.setStrokeWidth(2);
            coin.setStroke(Color.GREEN);
            selectedCoin = coin;
            switch (coin.getPosition()) {
                case 1:
                case 2:
                case 3:
                    //goingToPosition = 
                    showValid(true, false, true, false);
                    break;
                case 4:
                case 5:
                    showValid(false, false, true, true);
                    break;
                case 6:
                    showValid(false, true, false, true);
                    break;
            }
            ArrayList<Integer> aval = new ArrayList<>();
            aval.add(coins[0].getPosition());
            aval.add(coins[1].getPosition());
            aval.add(coins[2].getPosition());
            switch (coin.getPosition()) {
                case 1:
                    if (aval.contains(6)) {
                        rightD.setDisable(true);
                    } else if ((aval.contains(5) && aval.contains(4))) {
                        right.setDisable(true);
                    }
                    break;
                case 2:
                    if (aval.contains(6)) {
                        rightD.setDisable(true);
                    }
                    break;
                case 4:
                    if (aval.contains(6)) {
                        right.setDisable(true);
                    }
                    break;
                case 5:
                    if (aval.contains(6)) {
                        right.setDisable(true);
                    }
                    break;
                case 6:
                    if ((aval.contains(5) && aval.contains(4))) {
                        left.setDisable(true);
                    }
                    break;
            }

            // for enable all buttons ...
            if (right.isDisabled()) {
                disableButtons[0] = true;
                right.setDisable(false);
            } else {
                disableButtons[0] = false;
            }
            if (left.isDisabled()) {
                disableButtons[1] = true;
                left.setDisable(false);
            } else {
                disableButtons[1] = false;
            }
            if (rightD.isDisabled()) {
                disableButtons[2] = true;
                rightD.setDisable(false);
            } else {
                disableButtons[2] = false;
            }
            if (leftD.isDisabled()) {
                disableButtons[3] = true;
                leftD.setDisable(false);
            } else {
                disableButtons[3] = false;
            }
            right.setFocusTraversable(false);
            left.setFocusTraversable(false);
            rightD.setFocusTraversable(false);
            leftD.setFocusTraversable(false);
        }

    }

    public void whenRight() {
        if (isTrial) {
            try {
                myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TOWERLEVEL = " + 1 + " WHERE ID = " + validID);
                //     message ...
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("جيد");
                alert.setHeaderText(null);
                alert.setContentText("لقد انهيت المرحلة التجريبية بنجاح سوف تنتقل الان للعبة" + ".");
                alert.show();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TOWERLEVEL = " + (currentLevel + 1) + " WHERE ID = " + validID);
                myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TOWERSCORE = " + (this.score + 1) + " WHERE ID = " + validID);
                if (currentLevel + 1 >= 13) {
                    finishTest();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            //Score.setText(score + "");
        }
        result.setImage(new Image("/icons/right.png"));
        //newCard();
        imageViewPlay.setVisible(true);
        HBoxPlay.setVisible(true);
    }

    public void whenWrong() {
        if (isTrial) {
            // no thing to do ...
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("سيئ");
            alert.setHeaderText(null);
            alert.setContentText("اجابة خاطئة حاول مرة اخرى مازلت ف المرحلة التجريبية" + ".");
            alert.show();
        } else {
            int errorIndex = 0;
            try {
                myRestObj = myStatObj.executeQuery("SELECT * FROM SHERIF.STUDENTS WHERE ID = " + validID);
                while (myRestObj.next()) {
                    errorIndex = myRestObj.getInt("TOWERERROR");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            if (errorIndex == currentGroup && errorIndex != 0) {
                finishTest();
            } else if (errorIndex != currentGroup) {
                try {
                    myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TOWERERROR = " + currentGroup + " WHERE ID = " + validID);
                    myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TOWERLEVEL = " + (currentLevel + 1) + " WHERE ID = " + validID);
                } catch (SQLException ex) {
                    Logger.getLogger(SoundsController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }
        result.setImage(new Image("/icons/wrong.png"));
        HBoxPlay.setVisible(true);
        imageViewPlay.setVisible(true);
    }

    public void setCounter() {
        myTimer = null;
        task = null;
        myCount.setVisible(true);
        myTimer = new Timer();
        seconds = 1;
        task = new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    if (Education.withTime) {

                    } else {
                        setScore(seconds);
                        check_8_minite(seconds);
                    }
                    seconds++;
                });
            }
        };
        myTimer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public void setScore(int x) {
        myCount.setText((x) + "");
    }

    private void saveTheTime() {
        myTimer.cancel();

        if (!isTrial) {
            int previousTime = 0;
            try {
                myRestObj = myStatObj.executeQuery("SELECT * FROM SHERIF.STUDENTS WHERE ID = " + validID);
                while (myRestObj.next()) {
                    previousTime = myRestObj.getInt("TOWERTIME");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            try {
                myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TOWERTIME = " + (previousTime + seconds - 1) + " WHERE ID = " + validID);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void mapToGroupNCat() {
        switch (currentLevel) {
            case 0:
                currentGroup = 1;
                currentCategory = 2;
                break;
            case 1:
                currentGroup = 1;
                currentCategory = 1;
                break;
            case 2:
                currentGroup = 1;
                currentCategory = 2;
                break;
            case 3:
                currentGroup = 2;
                currentCategory = 1;
                break;
            case 4:
                currentGroup = 2;
                currentCategory = 2;
                break;
            case 5:
                currentGroup = 3;
                currentCategory = 1;
                break;
            case 6:
                currentGroup = 3;
                currentCategory = 2;
                break;
            case 7:
                currentGroup = 3;
                currentCategory = 3;
                break;
            case 8:
                currentGroup = 3;
                currentCategory = 4;
                break;
            case 9:
                currentGroup = 4;
                currentCategory = 1;
                break;
            case 10:
                currentGroup = 4;
                currentCategory = 2;
                break;
            case 11:
                currentGroup = 4;
                currentCategory = 3;
                break;
            case 12:
                currentGroup = 4;
                currentCategory = 4;
        }
    }

    private void finishTest() {
        // finish
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("انتهت");
        alert.setHeaderText(null);
        alert.setContentText("لقد انتهت هذه اللعبة");
        alert.show();

        try {
            root_game = FXMLLoader.load(getClass().getResource("FinishTest.fxml"));
            myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TOWERLEVEL = " + (currentLevel + 100) + " WHERE ID = " + validID);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(SoundsController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        scene_game = new Scene(root_game);
        stage_game.setScene(scene_game);
        stage_game.setTitle("education");
        stage_game.setResizable(false);
        stage_game.show();
    }

    public void updateTrial(int level) {
        if (level == 0)
            return;
        
        int numberOfMoves = 0;
        try {
            myRestObj = myStatObj.executeQuery("SELECT * FROM SHERIF.STUDENTS WHERE ID = " + validID);
            while (myRestObj.next()) {
                numberOfMoves = myRestObj.getInt("TRIAL" + level);
                numberOfMoves++;
            }
            myStatObj.executeUpdate("UPDATE SHERIF.STUDENTS SET TRIAL" + level + " = " + numberOfMoves + " WHERE ID = " + validID);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void check_8_minite(int seconds) {
        if (seconds + preSeconds >= 480) {
            finishTest();
            task.cancel();
        }
    }

}
