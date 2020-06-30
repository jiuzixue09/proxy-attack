package com.bigbigwork.controllers;

import com.bigbigwork.services.RecordService;
import com.bigbigwork.util.ObjectFileUtil;
import com.bigbigwork.vo.Command;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RecordController {
    private static final String RECORD_FILE = "record.bin";
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @FXML
    public GridPane gridPane;
    @FXML
    public TableView<Command> tableView;

    public void addContextMenu(){
        ContextMenu cm = new ContextMenu();
        MenuItem mi1 = new MenuItem("  删除  ");
        mi1.setOnAction(event -> handleDeletePerson());
        cm.getItems().add(mi1);
        MenuItem mi2 = new MenuItem("  上移  ");
        cm.getItems().add(mi2);

        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
            if(t.getButton() == MouseButton.SECONDARY) {
                cm.show(tableView, t.getScreenX(), t.getScreenY());
            }
        });
    }
    @FXML
    public void initialize() {
        TableColumn<Command,Integer> idColumn = new TableColumn<>("序号");
        idColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Command,String> typeColumn = new TableColumn<>("事件类型");
        typeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Command,String> operationColumn = new TableColumn<>("操作");
        operationColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.7));
        operationColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));

        tableView.getColumns().addAll(idColumn,typeColumn,operationColumn);
        tableView.setEditable(true);
        addContextMenu();

    }

    private void handleDeletePerson() {
        int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            tableView.getItems().remove(selectedIndex);
        } else {
            // Nothing selected.
            Alert alert = new Alert(Alert.AlertType.WARNING);
//            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Person Selected");
            alert.setContentText("Please select a row in the table.");

            alert.showAndWait();
        }
    }

    public RecordController(){
        Thread thread = new Thread(this::start);
        thread.setDaemon(true);
        thread.start();
    }

    public void start(){
        RecordService recordService = new RecordService(tableView);
        recordService.run();
    }

    public void cancel() {
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }

    public void save() {
        try {
            List<Command> collect = new ArrayList<>(tableView.getItems());
            ObjectFileUtil.write(new File(RECORD_FILE), collect);
        } catch (IOException e) {
            LOG.error("write error", e);
        }
    }
}
