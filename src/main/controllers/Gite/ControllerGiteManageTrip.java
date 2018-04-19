package main.controllers.Gite;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import main.NormalClasses.Anagrafica.Child;
import main.NormalClasses.Gite.Bus;

import main.NormalClasses.Gite.BusAssociation;
import main.NormalClasses.Gite.Trip;
import main.StringPropertyClasses.Anagrafica.StringPropertyChild;
import main.StringPropertyClasses.Gite.StringPropertyBus;
import main.StringPropertyClasses.Gite.StringPropertyTrip;
import main.controllers.AbstractController;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerGiteManageTrip extends AbstractController implements Initializable {

    //Utilities
    private final String pattern = "dd/MM/yyyy";
    private final String inputPattern = "yyyy-MM-dd";

    @FXML private Button genericButton;
    @FXML private ImageView editBusIV;
    @FXML private ImageView  editTripIV;
    @FXML private ImageView editChildIV;

    //Tabelle
    private ObservableList<StringPropertyTrip> giteObservableList = FXCollections.observableArrayList();

    @FXML private TableView<StringPropertyTrip> giteTable;
    @FXML private TableColumn<StringPropertyTrip, String> nomeGitaColumn;
    @FXML private TableColumn<StringPropertyTrip, String> dataGitaColumn;

    private ObservableList<StringPropertyBus> autobusObservableList = FXCollections.observableArrayList();

    @FXML private TableView<StringPropertyBus> autobusTable;
    @FXML private TableColumn<StringPropertyBus, String> busesColumn;

    //Tabella Bambini
    private ObservableList<StringPropertyChild> bambiniObservableList = FXCollections.observableArrayList();

    @FXML private TableView<StringPropertyChild> iscrizioniTable;
    @FXML private TableColumn<StringPropertyChild, String> codiceBambinoColumn;
    @FXML private TableColumn<StringPropertyChild, String> nomeBambinoColumn;
    @FXML private TableColumn<StringPropertyChild, String> cognomeBambinoColumn;
    @FXML private TableColumn<StringPropertyChild, Boolean> presenzaBambinoColumn;

    List <BusAssociation> enrolled = new ArrayList<>();
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        AbstractController.setCurrentController(this);

        iscrizioniTable.setEditable(true);
        codiceBambinoColumn.setCellValueFactory(cellData -> cellData.getValue().codRProperty());
        nomeBambinoColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        cognomeBambinoColumn.setCellValueFactory(cellData -> cellData.getValue().cognomeProperty());
        presenzaBambinoColumn.setCellFactory( CheckBoxTableCell.forTableColumn(presenzaBambinoColumn) );
        presenzaBambinoColumn.setCellValueFactory(cellData -> cellData.getValue().booleanStatusProperty());
        //presenzaBambinoColumn.setCellValueFactory(p -> createEnrollment(p));

        dataGitaColumn.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        nomeGitaColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        giteTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showRelatedAutobusAndChildren(newValue));

        busesColumn.setCellValueFactory(cellData -> cellData.getValue().targaProperty());

        refreshTripTable();


    }

    private void refreshTripTable(){

        giteObservableList.clear();

        List<Trip> giteArrayList = CLIENT.clientExtractAllTripsFromDb();
        if(giteArrayList  != null){
            for(Trip t : giteArrayList){
                giteObservableList.add(new StringPropertyTrip(t));
            }
        }
        giteTable.setItems(giteObservableList);
    }

    private void refreshChildrenTable(String data){

        bambiniObservableList.clear();

        List<Child> childrenList = CLIENT.clientExtractEnrollableChildren(data);
        if(childrenList != null){
            for(Child c : childrenList){
                bambiniObservableList.add(new StringPropertyChild(c));
            }
        }
        iscrizioniTable.setItems(bambiniObservableList);
    }

    private void refreshBusTable(String nomeG, String dataG){

        autobusObservableList.clear();

        List<Bus> autobusArrayList = CLIENT.clientExtractAllBusesFromTrip(nomeG,dataG);
        if(autobusArrayList != null){
            for(Bus b : autobusArrayList){
                autobusObservableList.add(new StringPropertyBus(b));
            }
        }
        autobusTable.setItems(autobusObservableList);
    }

    private void showRelatedAutobusAndChildren(StringPropertyTrip selectedGita) {
        if(selectedGita!=null) {
            refreshBusTable(selectedGita.getNome(), selectedGita.getData());
            refreshChildrenTable(selectedGita.getData());
        }
    }

    @FXML private void addTrip() throws IOException {
        changeSceneInPopup(genericButton, "../../resources/fxml/gite_addTrip.fxml",450,450);
        refreshTripTable();
    }

    @FXML private void addBus() throws IOException {
        if (giteTable.getSelectionModel().selectedItemProperty().get() != null) {
            StringPropertyTrip selectedGita = giteTable.getSelectionModel().selectedItemProperty().get();
            ControllerGiteAddBus.setGita(selectedGita);
            changeSceneInPopup(genericButton, "../../resources/fxml/gite_addBus.fxml",800,450);
            refreshBusTable(selectedGita.getNome(), selectedGita.getData());
        }else {
                createErrorPopup("Errore", "Non hai selezionato una gita");
        }

    }

    @FXML private void removeTrip(){
        if (giteTable.getSelectionModel().selectedItemProperty().get() != null) {
            StringPropertyTrip selectedGita = giteTable.getSelectionModel().selectedItemProperty().get();
            boolean success = CLIENT.clientDeleteTripFromDb(selectedGita.getNome(),selectedGita.getData());
            if (success) {
                createSuccessPopup();
                refreshTripTable();
                refreshChildrenTable(selectedGita.getData());
            } else {
                createErrorPopup("Errore", "Non è stato possibile eliminare la gita, riprova più tardi");
            }
        }else {
            createErrorPopup("Errore", "Non hai selezionato una gita");
        }
    }

    @FXML private void removeBus(){
        if (giteTable.getSelectionModel().selectedItemProperty().get() != null &
                autobusTable.getSelectionModel().selectedItemProperty().get() != null) {
            StringPropertyBus selectedBus = autobusTable.getSelectionModel().selectedItemProperty().get();
            boolean success = CLIENT.clientDeleteBusFromDb(selectedBus.getNomeG(), selectedBus.getDataG(),selectedBus.getTarga());
            if (success) {
                createSuccessPopup();
                refreshBusTable(selectedBus.getNomeG(), selectedBus.getDataG());
                refreshChildrenTable(selectedBus.getDataG());
            } else {
                createErrorPopup("Errore", "Non è stato possibile eliminare l'autobus, riprova più tardi");
            }
        }else {
            createErrorPopup("Errore", "Non hai selezionato un autobus");
        }
    }


    @FXML private void addChildren(){
        enrolled.clear();
        if(bambiniObservableList.size() != 0 && giteTable.getSelectionModel().selectedItemProperty().get() != null &
                autobusTable.getSelectionModel().selectedItemProperty().get() != null){
            StringPropertyBus selectedBus = autobusTable.getSelectionModel().selectedItemProperty().get();
            for (StringPropertyChild c : bambiniObservableList){
                if (c.isBooleanStatus() )  enrolled.add(new BusAssociation(c.getCodiceFiscale(),selectedBus.getNomeG(), selectedBus.getDataG(),selectedBus.getTarga()));
            }
            boolean success = CLIENT.clientEnrollChildren(enrolled);
            if (success) {
                createSuccessPopup();
                refreshChildrenTable(selectedBus.getDataG());
            } else {
                createErrorPopup("Errore", "Non è stato possibile iscrivere i bambini, riprova più tardi");
            }
        }else {
            createErrorPopup("Errore", "Non hai selezionato una gita è un autobus");
        }

    }

    @FXML private void getInfo(){
        createInfoPopup("Seleziona una gita per cancellarla, idem per la cancellazione di autobus\n" +
                "Seleziona una gita per aggiungere un autobus\n" +
                "Seleziona un autobus e bambini per aggiungerli e poi fai click su aggiungi bambini\n"+
                "Nella tabella sono mostrati solo i bambini senza alcun impegno nella data selezionata");
    }

    @FXML private void goHome(){
        Stage stage = (Stage) genericButton.getScene().getWindow();
        stage.close();
    }

    @FXML public void editBus() throws IOException {
        if (giteTable.getSelectionModel().selectedItemProperty().get() != null &
                autobusTable.getSelectionModel().selectedItemProperty().get() != null) {
            StringPropertyBus selectedBus = autobusTable.getSelectionModel().selectedItemProperty().get();
            ControllerGiteEditBus.setBus(selectedBus);
            openPopOver("../../resources/fxml/gite_editBus.fxml", PopOver.ArrowLocation.LEFT_CENTER, editBusIV );
            //changeSceneInPopup(genericButton, "../../resources/fxml/gite_editBus.fxml",380,380);
            refreshBusTable(selectedBus.getNomeG(), selectedBus.getDataG());
        }else {
            createErrorPopup("Errore", "Non hai selezionato un autobus");
        }
    }

    @FXML public void editTrip() throws IOException {
        if (giteTable.getSelectionModel().selectedItemProperty().get() != null){
            StringPropertyTrip selectedTrip = giteTable.getSelectionModel().selectedItemProperty().get();
            ControllerGiteEditTrip.setTrip(selectedTrip);
            openPopOver("../../resources/fxml/gite_editTrip.fxml", PopOver.ArrowLocation.LEFT_TOP, editTripIV );
            //changeSceneInPopup(genericButton,"../../resources/fxml/gite_editTrip.fxml",380,380);
            refreshTripTable();
        }else {
            createErrorPopup("Errore", "Non hai selezionato una gita");
        }
    }

    @FXML public void removeChildren() throws IOException {
        StringPropertyBus selectedBus = autobusTable.getSelectionModel().selectedItemProperty().get();
        if (giteTable.getSelectionModel().selectedItemProperty().get() != null & selectedBus!= null){
            ControllerGiteRemoveChildren.setSelectedBus(selectedBus);
            openPopOver("../../resources/fxml/gite_editChild.fxml", PopOver.ArrowLocation.LEFT_BOTTOM, editChildIV);
            //changeSceneInPopup(genericButton,"../../resources/fxml/gite_editChild.fxml",380,380);
            refreshTripTable();
            refreshChildrenTable(selectedBus.getDataG());
        }else {
            createErrorPopup("Errore", "Non hai selezionato una gita e autobus");
        }
    }
}