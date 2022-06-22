package controllers;


import application.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class AccountRowController extends Controller {

    @FXML
    ImageView borderImage,starImage,portraitImage,tankRankImage,dpsRankImage,supportRankImage;
    @FXML
    Label levelTrackLabel,errorLabel,levelLabel,battleTagLabel,tankRankLabel,dpsRankLabel,supportRankLabel,tagCopyLabel,emailCopyLabel;

    private Connection con = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    private Main main;
    private Controller currentPageController;
    private String battleTag,savedTag;
    private MainMenuController parent;
    private int level;
    private String email;


    public void setMain(Main main) {
        this.main = main;
    }

    public void setConnection(Connection c) {
        this.con = c;
    }

    public void setParent(MainMenuController mmc) {this.parent = mmc;}

    public void fill() {
        HttpResponse<JsonNode> response = null;
//        HttpResponse<JsonNode> levelresponse = null;
        try {
            response = Unirest.get("https://owapi.io/profile/pc/us/"+battleTag).asJson();
//            levelresponse = Unirest.get("https://best-overwatch-api.herokuapp.com/player/pc/us/"+battleTag).asJson();
        } catch (UnirestException e) {
            System.out.println("Failure");
            e.printStackTrace();
        }
        if(response.getBody().getObject().optString("message")!=""){
            errorLabel.setVisible(true);
            errorLabel.setText(response.getBody().getObject().optString("message"));
            battleTagLabel.setText(savedTag);
            levelLabel.setText("");
            borderImage.setImage(null);
            starImage.setImage(null);
            portraitImage.setImage(null);
            tankRankImage.setImage(null);
            tankRankLabel.setText("");
            dpsRankImage.setImage(null);
            dpsRankLabel.setText("");
            supportRankImage.setImage(null);
            supportRankLabel.setText("");
        }else{
            errorLabel.setVisible(false);
            //Level Text
            if(response.getBody().getObject().optString("level")!=""){
                int level = response.getBody().getObject().optInt("level");
                levelLabel.setText(String.valueOf(level%100));
                levelTrackLabel.setText(String.valueOf(level));
            }else{
                levelLabel.setText("");
                levelTrackLabel.setText(String.valueOf(Integer.MAX_VALUE));
            }
            //Border Image
            if(response.getBody().getObject().get("levelFrame").toString()!=""){
                Image image = new Image(response.getBody().getObject().get("levelFrame").toString());
                borderImage.setImage(image);
            }else{
                borderImage.setImage(null);
            }
            //Star Image
            if(response.getBody().getObject().optString("star")!=""){
                Image image = new Image(response.getBody().getObject().optString("star"));
                starImage.setImage(image);
            }else{
                starImage.setImage(null);
            }
            //Portrait Image
            if(response.getBody().getObject().get("portrait").toString()!=""){
                Image image = new Image(response.getBody().getObject().get("portrait").toString());
                portraitImage.setImage(image);
            }else{
                portraitImage.setImage(null);
            }
            //BattleTag
            battleTagLabel.setText(battleTag.replace('-','#'));
            //Tank rank
            if(!(response.getBody().getObject().getJSONObject("competitive").getJSONObject("tank").optString("rank_img").isEmpty())){
                Image image = new Image(response.getBody().getObject().getJSONObject("competitive").getJSONObject("tank").optString("rank_img"));
                tankRankImage.setImage(image);
                tankRankLabel.setText(response.getBody().getObject().getJSONObject("competitive").getJSONObject("tank").optString("rank"));
            }else{
                tankRankImage.setImage(null);
                tankRankLabel.setText("");
            }
            //DPS rank
            if(!(response.getBody().getObject().getJSONObject("competitive").getJSONObject("damage").optString("rank_img").isEmpty())){
                Image image = new Image(response.getBody().getObject().getJSONObject("competitive").getJSONObject("damage").optString("rank_img"));
                dpsRankImage.setImage(image);
                dpsRankLabel.setText(response.getBody().getObject().getJSONObject("competitive").getJSONObject("damage").optString("rank"));
            }else{
                dpsRankImage.setImage(null);
                dpsRankLabel.setText("");
            }
            //Support rank
            if(!(response.getBody().getObject().getJSONObject("competitive").getJSONObject("support").optString("rank_img").isEmpty())){
                Image image = new Image(response.getBody().getObject().getJSONObject("competitive").getJSONObject("support").optString("rank_img"));
                supportRankImage.setImage(image);
                supportRankLabel.setText(response.getBody().getObject().getJSONObject("competitive").getJSONObject("support").optString("rank"));
            }else{
                supportRankImage.setImage(null);
                supportRankLabel.setText("");
            }
        }
    }

    public void setAccount(String battleTag){
        this.battleTag = battleTag.replace('#','-');
        this.savedTag = battleTag;
    }

    public void deleteAccount(){
        String sql = "DELETE FROM accounts WHERE battleTag = ?";
        try {
            preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, savedTag);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        parent.loadAccounts();
    }
    public void setEmail(String s){
        this.email = s;
    }

    public String getEmail(){
        return this.email;
    }

    public void copyEmail(){
        ClipboardContent content = new ClipboardContent();
        content.putString(this.email);
        Clipboard.getSystemClipboard().setContent(content);
        emailCopyLabel.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> emailCopyLabel.setVisible(false));
        pause.play();
    }

    public void copyTag(){
        ClipboardContent content = new ClipboardContent();
        content.putString(battleTag.replace('-','#'));
        Clipboard.getSystemClipboard().setContent(content);
        tagCopyLabel.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> tagCopyLabel.setVisible(false));
        pause.play();
    }
}

