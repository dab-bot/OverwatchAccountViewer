package controllers;


import application.Main;
import com.dlsc.gemsfx.DialogPane;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;


public class MainMenuController extends Controller {

    @FXML
    private BorderPane contentPane,topPane;
    @FXML
    private HBox controlBox;
    @FXML
    private Button maximize,minimize,close;
    @FXML
    private VBox accountRowContainer;
    @FXML
    private MFXScrollPane accountScroll;
    @FXML
    private ProgressIndicator progressIndicator;

    private Connection con = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    private Main main;
    private Controller currentPageController;
    private Dialog<Object> dialog;
    private ObservableList<HBox> rows= FXCollections.observableArrayList();
    private int threadCount,expectedThreadCount;
    private boolean loadingAccounts = false;

    public void setMain(Main main) {
        this.main = main;
    }

    public void setConnection(Connection c) {
        this.con = c;
    }

    public void fill() {
        this.main.getBs().setMoveControl(topPane);
        close.setOnAction(a -> this.main.getStg().close());
        minimize.setOnAction(a -> this.main.getStg().setIconified(true));
        maximize.setOnAction(a -> this.main.getBs().maximizeStage());

        accountRowContainer.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double deltaY = event.getDeltaY()*6; // *6 to make the scrolling a bit faster
                double width = accountScroll.getContent().getBoundsInLocal().getWidth();
                double vvalue = accountScroll.getVvalue();
                accountScroll.setVvalue(vvalue + -deltaY/width); // deltaY/width to make the scrolling equally fast regardless of the actual width of the component
            }
        });


        loadAccounts();
    }

    public void loadAccounts(){
        if(!loadingAccounts){
            ObservableList<String> battleTags = FXCollections.observableArrayList();
            ObservableList<String> emails = FXCollections.observableArrayList();
            String sql = null;
            sql = "SELECT * FROM accounts";
            try {
                preparedStatement = con.prepareStatement(sql);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    battleTags.add(resultSet.getString("BattleTag"));
                    if(resultSet.getString("Email")!=null){
                        emails.add(resultSet.getString("Email"));
                    }else{
                        emails.add("");
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            expectedThreadCount=battleTags.size();
            threadCount=0;
            loadingAccounts = true;
            rows.clear();
            for(int i = 0;i<battleTags.size();i++)
                addAccountRow(battleTags.get(i),emails.get(i));
        }
    }

    public void addAccount(){
        if(!loadingAccounts){
            TextInputDialog td = new TextInputDialog();
            td.setTitle("Add battletag");
            td.setContentText("Enter in the account battletag here");
            td.setHeaderText(null);
            Optional<String> result = td.showAndWait();
            String battleTag = td.getEditor().getText();
            if (result.isPresent()) {
                td = new TextInputDialog();
                td.setTitle("Add Email");
                td.setContentText("Enter in the email for the account: " + battleTag);
                td.setHeaderText(null);
                result = td.showAndWait();
                if(result.isPresent()){
                    String email = td.getEditor().getText();
                    String sql = "INSERT INTO accounts(BattleTag,Email) VALUES(?,?)";
                    try {
                        preparedStatement = con.prepareStatement(sql);
                        preparedStatement.setString(1, battleTag);
                        preparedStatement.setString(2, email);
                        preparedStatement.executeUpdate();
                    } catch (SQLException ex) {
                        System.err.println(ex.getMessage());
                    }
                    expectedThreadCount = 1;
                    threadCount = 0;
                    loadingAccounts =true;
                    addAccountRow(battleTag,email);
                }
            }
        }
        }

        public void addAccountRow(String battleTag,String email){
            progressIndicator.setVisible(true);
            MainMenuController me = this;
            Task<HBox> task = new Task<HBox>() {
                @Override
                protected HBox call() throws Exception {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXML/AccountRow.fxml"));
                        HBox accountRow = null;
                        try {
                            accountRow = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        AccountRowController sc = loader.getController();
                        sc.setMain(main);
                        sc.setConnection(con);
                        sc.setAccount(battleTag);
                        sc.setEmail(email);
                        sc.setParent(me);
                        sc.fill();
                        return accountRow;
                }
            };
            progressIndicator.progressProperty().bind(task.progressProperty());
            task.setOnSucceeded(evt -> {
                if(threadCount<expectedThreadCount-1){
                    rows.add(task.getValue());
                    threadCount++;
                }else{
                    rows.add(task.getValue());
                    HBoxComparator comparator = new HBoxComparator();
                    rows.sort(comparator);
                    accountRowContainer.getChildren().clear();
                    accountRowContainer.getChildren().setAll(rows);
                    loadingAccounts=false;
                    progressIndicator.setVisible(false);
                }
            });
            new Thread(task).start();
        }
}

class HBoxComparator implements Comparator<HBox> {

    @Override
    public int compare(HBox firstBox, HBox secondBox) {
        Label label1 = (Label) firstBox.getChildren().get(0);
        Label label2 = (Label) secondBox.getChildren().get(0);
        int level1 = Integer.parseInt(label1.getText());
        int level2 = Integer.parseInt(label2.getText());
        return Integer.compare(level2,level1);
    }

}

