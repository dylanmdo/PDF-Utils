package org.pdfutils.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MergeAppController {

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private final ArrayList<File> selections = new ArrayList<>();


    @FXML
    public TableView<File> tableViewFile;

    @FXML
    public TableColumn<File, String> titleFileName;

    @FXML
    public TableColumn<File, String> titleFileSize;

    List<File> selectedFiles;
    List<File> droppedFiles;

    @FXML
    public TextField nameFileSaved;

    @FXML
    public void initialize() {
        tableViewFile.setPlaceholder(new Label(""));
    }

    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
    }

    //Selectionner des fichiers via Drag and Drop puis on colle le nom et la taille à la TableView
    @FXML
    public void handleDragDropped(DragEvent event) {
        droppedFiles  = event.getDragboard().getFiles();

        if (droppedFiles != null) {
            tableViewFile.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableViewFile.getItems().addAll(droppedFiles);
            titleFileName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
            titleFileSize.setCellValueFactory(c -> new SimpleStringProperty((c.getValue().length() / 1024)+1 + " Ko"));
        }

        tableReorder();

        System.out.println(tableViewFile.getItems());


    }


    //Modifier l'ordre des lignes de la TableView via Drag and Drop
    public void tableReorder(){

        //Mise en place du  Drag and Drop
        tableViewFile.setRowFactory(tv -> {
            TableRow<File> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (! row.isEmpty()) {
                    Integer index = row.getIndex();

                    selections.clear();//important...

                    ObservableList<File> items = tableViewFile.getSelectionModel().getSelectedItems();

                    selections.addAll(items);


                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != (Integer) db.getContent(SERIALIZED_MIME_TYPE)) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();

                if (db.hasContent(SERIALIZED_MIME_TYPE)) {

                    int dropIndex;
                    File dI=null;

                    if (row.isEmpty()) {
                        dropIndex = tableViewFile.getItems().size() ;
                    } else {
                        dropIndex = row.getIndex();
                        dI = tableViewFile.getItems().get(dropIndex);
                    }
                    int delta=0;
                    if(dI!=null)
                        while(selections.contains(dI)) {
                            delta=1;
                            --dropIndex;
                            if(dropIndex<0) {
                                dI=null;dropIndex=0;
                                break;
                            }
                            dI = tableViewFile.getItems().get(dropIndex);
                        }

                    for(File sI:selections) {
                        tableViewFile.getItems().remove(sI);
                    }

                    if(dI!=null)
                        dropIndex=tableViewFile.getItems().indexOf(dI)+delta;
                    else if(dropIndex!=0)
                        dropIndex=tableViewFile.getItems().size();



                    tableViewFile.getSelectionModel().clearSelection();

                    for(File sI:selections) {
                        //draggedIndex = selections.get(i);
                        tableViewFile.getItems().add(dropIndex, sI);
                        tableViewFile.getSelectionModel().select(dropIndex);
                        dropIndex++;

                    }

                    event.setDropCompleted(true);
                    selections.clear();
                    event.consume();
                }
            });

            //Réalisation du menu contextuel Supprimer ou ouvrir
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem("Supprimer");
            final MenuItem openMenuItem = new MenuItem("Ouvrir");
            removeMenuItem.setOnAction(event -> tableViewFile.getItems().remove(row.getItem()));
            openMenuItem.setOnAction(event ->
                    new Thread(() -> {
                        try {
                            File file = new File(String.valueOf(tableViewFile.getSelectionModel().getSelectedItem()));
                            Desktop.getDesktop().open((file) );
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }).start() );

            contextMenu.getItems().add(removeMenuItem);
            contextMenu.getItems().add(openMenuItem);
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );

            return row ;
        });

    }


    //Sélectionner les fichiers à traiter
    @FXML
    public void handleMergeSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        selectedFiles = fileChooser.showOpenMultipleDialog(null);

        //Coller au tableau le nom et la taille du fichier
        if (selectedFiles != null) {
            tableViewFile.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableViewFile.getItems().addAll(selectedFiles);
            titleFileName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
            titleFileSize.setCellValueFactory(c -> new SimpleStringProperty((c.getValue().length() / 1024)+1 + " Ko"));
        }

        //Appel de la methode de Drag and Drop
        tableReorder();


    }

    // Méthode pour fusionner les pdf
    public void documentMerge(List<File> selectedFiles, File selectedDirectory) throws IOException {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();

        String fileName = nameFileSaved.getText();

        if (nameFileSaved.getText().isEmpty()) {
            String FirstFileName = selectedFiles.get(0).getName();
            if (FirstFileName.contains(".")) {
                FirstFileName = FirstFileName.substring(0, FirstFileName.lastIndexOf("."));
                pdfMergerUtility.setDestinationFileName(selectedDirectory + "\\" + FirstFileName + "_merged.pdf");
            }

        } else {
            pdfMergerUtility.setDestinationFileName(selectedDirectory + "\\" + fileName + "_merged.pdf");
        }

        for (File file : selectedFiles) {
            pdfMergerUtility.addSource(file.getPath());

        }

        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

    }

    //Bouton retour vers le Main App
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

    //Methode de sauvegarde qui fait appel à la methode de traitement de PDF, puis demande un en placement de sauvegarde
    public void handleSaveMerge() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (selectedFiles == null && tableViewFile.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Enregistrement échoué");
            alert.setContentText("Aucun fichier n'a été sélectionné");
            alert.showAndWait();
        } else {
            int i = 0;
            for (File ignored : tableViewFile.getItems()) {
                i++;
            }
            if (i > 1) {
                directoryChooser.setInitialDirectory(tableViewFile.getItems().get(0).getParentFile());
                File selectedDirectory = directoryChooser.showDialog(null);
                if (selectedDirectory != null) {
                    documentMerge(tableViewFile.getItems(), selectedDirectory);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Enregistrement terminé \uD83D\uDC4C  ");
                    alert.showAndWait();

                }
            } else  {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Enregistrement échoué");
                alert.setContentText("Vous devez sélectionner au moins deux fichiers PDF");
                alert.showAndWait();
            }


        }

    }
}
