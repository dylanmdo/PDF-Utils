package org.pdfutils.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class SplitAppController {

    File selectedFile;
    File droppedFile ;

    @FXML
    public Label FileSelectedSplit;

    @FXML
    public TextField startPageField;

    @FXML
    public TextField endPageField;


    @FXML
    public void initialize() {
    }

    //Sélectionner les fichiers à traiter
    @FXML
    public void handleSliptSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            FileSelectedSplit.setText(selectedFile.getName());
        }

    }


    @FXML
    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
    }

    //Selectionner des fichiers via Drag and Drop
    @FXML
    public void handleDragDropped(DragEvent event) {
        List<File> files  = event.getDragboard().getFiles();
        droppedFile = files.get(0);
        FileSelectedSplit.setText(droppedFile.getName());
    }




    // Méthode pour séparer les pdf 1 à 1
    public void documentSplit(File selectedFile, File selectedDirectory) throws IOException {
        try (PDDocument document = PDDocument.load(selectedFile)) {
            Splitter splitter = new Splitter();
            List<PDDocument> splitPages = splitter.split(document);

            int i = 1;

            for (PDDocument docpdf : splitPages) {

                String fileName = selectedFile.getName();
                System.out.println(selectedDirectory.getAbsolutePath());

                if (!fileName.contains(".")) {
                    docpdf.save(selectedDirectory.getAbsolutePath() + "/" + fileName + "_page" + i + ".pdf");
                } else {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    docpdf.save(selectedDirectory.getAbsolutePath() + File.separator + fileName + "_page" + i + ".pdf");
                }

                i++;
            }
        }



    }

    // Méthode pour séparer les pdf par interval
    public void documentSplitRange(File selectedFile, File selectedDirectory) throws IOException {
        PDDocument document = PDDocument.load(selectedFile);

        int splitStartPage = Integer.parseInt(startPageField.getText());
        int splitEndPage = Integer.parseInt(endPageField.getText());

        Splitter splitter = new Splitter();
        splitter.setStartPage(splitStartPage);
        splitter.setEndPage(splitEndPage);

        List<PDDocument> splitPages = splitter.split(document);

        PDDocument newDocument = new PDDocument();

        for (PDDocument mydoc : splitPages) {
            newDocument.addPage(mydoc.getPage(0));
        }

        String fileName = selectedFile.getName();

        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }

        System.out.println(selectedDirectory + File.separator + fileName + "_page" + splitStartPage + "à" + splitEndPage + ".pdf");
        newDocument.save(selectedDirectory + File.separator + fileName + "_page" + splitStartPage + "à" + splitEndPage + ".pdf");
        newDocument.close();
    }


    //methode de selection de dossier de sauvegarde et de traitement de PDF
    private void directoryChooserAndSplit(File selectedFile) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(selectedFile.getParentFile());
        File selectedDirectory = directoryChooser.showDialog(startPageField.getScene().getWindow());



        if (selectedDirectory != null) {
            if (startPageField.getText().trim().isEmpty() && endPageField.getText().trim().isEmpty()) {
                // Méthode pour séparer les pdf 1 à 1
                documentSplit(selectedFile, selectedDirectory);

            } else {
                // Méthode pour séparer les pdf 1 à 1
                documentSplitRange(selectedFile, selectedDirectory);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Enregistrement terminé \uD83D\uDC4C  ");
            alert.showAndWait();

        }
    }

    //Methode de sauvegarde, qui fait appel à la methode de selection de dossier de sauvegarde , puis realise le traitement du PDF
    public void handleSaveSplit() throws IOException {

        //Si les fichiers on été selectionnés via Drag and Drop
        if (droppedFile != null) {
            directoryChooserAndSplit(droppedFile);

            //Si les fichiers on été selectionnés via fileChooser
        }else {
            if (selectedFile == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Enregistrement échoué");
                alert.setContentText("Vous devez d'abord sélectionner un fichier");
                alert.showAndWait();
            } else {

                directoryChooserAndSplit(selectedFile);

            }
        }

    }

    @FXML
    public void returnBtnMain(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/pdfutils/MainAppView.fxml")));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1000, 600);
        JMetro jMetro = new JMetro();
        jMetro.setStyle(Style.LIGHT);
        jMetro.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }




}
