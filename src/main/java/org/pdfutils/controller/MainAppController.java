package org.pdfutils.controller;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.util.Objects;


public class MainAppController extends Application {


    @Override
    public void start(Stage primaryStage) {
        try {
            BorderPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/pdfutils/MainAppView.fxml")));
            Scene scene = new Scene(root, 1000, 600);

            JMetro jMetro = new JMetro();
            jMetro.setStyle(Style.LIGHT);
            jMetro.setScene(scene);


            Image icon  = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/pdfutils/image/pdf_icon.png")));
            primaryStage.getIcons().add(icon);


            primaryStage.setTitle("PDFUtils");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void handleMainSplit(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/pdfutils/SplitView.fxml")));

        loadStage(event, root);

    }

    @FXML
    public void handleMainMerge(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/pdfutils/MergeView.fxml")));
        loadStage(event, root);

    }

    private void loadStage(ActionEvent event, Parent root) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        JMetro jMetro = new JMetro();
        jMetro.setStyle(Style.LIGHT);
        jMetro.setScene(scene);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}
