/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import stamboom.controller.StamboomController;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;
import stamboom.util.StringUtilities;

/**
 *
 * @author frankpeeters
 */
public class StamboomFXController extends StamboomController implements Initializable {

    //MENUs en TABs
    @FXML MenuBar menuBar;
    @FXML MenuItem miNew;
    @FXML MenuItem miOpen;
    @FXML MenuItem miSave;
    @FXML CheckMenuItem cmDatabase;
    @FXML MenuItem miClose;
    @FXML Tab tabPersoon;
    @FXML Tab tabGezin;
    @FXML Tab tabPersoonInvoer;
    @FXML Tab tabGezinInvoer;

    //PERSOON
    @FXML ComboBox cbPersonen;
    @FXML TextField tfPersoonNr;
    @FXML TextField tfVoornamen;
    @FXML TextField tfTussenvoegsel;
    @FXML TextField tfAchternaam;
    @FXML TextField tfGeslacht;
    @FXML TextField tfGebDatum;
    @FXML TextField tfGebPlaats;
    @FXML ComboBox cbOuderlijkGezin;
    @FXML ListView lvAlsOuderBetrokkenBij;
    @FXML Button btStamboom;
    @FXML TextArea tfStamBoom;

    //INVOER GEZIN
    @FXML ComboBox cbOuder1Invoer;
    @FXML ComboBox cbOuder2Invoer;
    @FXML TextField tfHuwelijkInvoer;
    @FXML TextField tfScheidingInvoer;
    @FXML Button btOKGezinInvoer;
    @FXML Button btCancelGezinInvoer;
    @FXML ComboBox cbSelecteerGezin;

    //opgave 4
    private boolean withDatabase;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initComboboxes();
        withDatabase = false;
    }

    private void initComboboxes() {
        cbPersonen.setItems(super.getAdministratie().getPersonen());
        cbOuder1Invoer.setItems(super.getAdministratie().getPersonen());
        cbOuder2Invoer.setItems(super.getAdministratie().getPersonen());
        cbOuderlijkGezin.setItems(super.getAdministratie().getGezinnen());
        cbSelecteerGezin.setItems(super.getAdministratie().getGezinnen());

    }

    public void selectPersoon(Event evt) {
        Persoon persoon = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        showPersoon(persoon);
    }

    private void showPersoon(Persoon persoon) {
        if (persoon == null) {
            clearTabPersoon();
        } else {
            tfPersoonNr.setText(persoon.getNr() + "");
            tfVoornamen.setText(persoon.getVoornamen());
            tfTussenvoegsel.setText(persoon.getTussenvoegsel());
            tfAchternaam.setText(persoon.getAchternaam());
            tfGeslacht.setText(persoon.getGeslacht().toString());
            tfGebDatum.setText(StringUtilities.datumString(persoon.getGebDat()));
            tfGebPlaats.setText(persoon.getGebPlaats());
            if (persoon.getOuderlijkGezin() != null) {
                cbOuderlijkGezin.getSelectionModel().select(persoon.getOuderlijkGezin());
            } else {
                cbOuderlijkGezin.getSelectionModel().clearSelection();
            }

            lvAlsOuderBetrokkenBij.setItems(persoon.getAlsOuderBetrokkenIn());
        }
    }

    public void setOuders(Event evt) {
        if (tfPersoonNr.getText().isEmpty()) {
            return;
        }
        Gezin ouderlijkGezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();
        if (ouderlijkGezin == null) {
            return;
        }

        int nr = Integer.parseInt(tfPersoonNr.getText());
        Persoon p = getAdministratie().getPersoon(nr);
        if (getAdministratie().setOuders(p, ouderlijkGezin)) {
            showDialog("Success", ouderlijkGezin.toString()
                    + " is nu het ouderlijk gezin van " + p.getNaam());
        }

    }

    public void selectGezin(Event evt) {
        Gezin gezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();
        showGezin(gezin);
    }

    private void showGezin(Gezin gezin) {
        if (gezin == null) {
            clearTabGezin();
            return;
        } else {
            cbOuder1Invoer.getSelectionModel().select(gezin.getOuder1());
            cbOuder2Invoer.getSelectionModel().select(gezin.getOuder2());
            tfHuwelijkInvoer.setText(gezin.getHuwelijksdatum().toString());
            tfScheidingInvoer.setText(gezin.getScheidingsdatum().toString());
        }
    }

    public void setHuwdatum(Event evt) {
        Persoon p1 = (Persoon) cbOuder1Invoer.getSelectionModel().getSelectedItem();
        Persoon p2 = (Persoon) cbOuder2Invoer.getSelectionModel().getSelectedItem();
        Calendar datum = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            datum.setTime(sdf.parse(tfHuwelijkInvoer.getText()));
        } catch (ParseException ex) {
            showDialog("Warning", "datum moet in formaat dd-mm-jjjj");
            return;
        }
        if (super.getAdministratie().addHuwelijk(p1, p2, datum) == null) {
            showDialog("Warning", "huwelijk niet geaccepteerd!");
            return;
        }
        clearTabGezinInvoer();

    }

    public void setScheidingsdatum(Event evt) {
        Gezin gezin = (Gezin) cbSelecteerGezin.getSelectionModel().getSelectedItem();
        if (gezin == null) {
            showDialog("Warning", "Geen gezin geselecteerd!");
            return;
        } else {
            Calendar datum = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            try {
                datum.setTime(sdf.parse(tfScheidingInvoer.getText()));
            } catch (ParseException ex) {
                showDialog("Warning", "datum moet in formaat dd-mm-jjjj");
                return;
            }
            if (!super.getAdministratie().setScheiding(gezin, datum)) {
                showDialog("Warning", "Scheiding niet geaccepteerd!");
                return;
            }
        }
        clearTabGezinInvoer();
    }

    public void cancelPersoonInvoer(Event evt) {
        clearTabPersoonInvoer();

    }

    public void okPersoonInvoer(Event evt) {
        String voornamen = tfVoornamen.getText();
        String[] voornamenArray = voornamen.split(" ");
        String tussenvoegsel = tfTussenvoegsel.getText();
        String achternaam = tfAchternaam.getText();
        Geslacht geslacht;
        if (tfGeslacht.getText().trim().toLowerCase().equals("m")) {
            geslacht = Geslacht.MAN;
        } else if (tfGeslacht.getText().trim().toLowerCase().equals("v")) {
            geslacht = Geslacht.VROUW;
        } else {
            showDialog("Warning", "geslacht is fout ingevoerd!");
            return;
        }
        Calendar gebDatum = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            gebDatum.setTime(sdf.parse(tfGebDatum.getText()));
        } catch (ParseException ex) {
            showDialog("Warning", "datum moet in formaat dd-mm-jjjj");
            return;
        }
        String gebplaats = tfGebPlaats.getText();
        Gezin ouderlijkGezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();

        try {
            super.getAdministratie().addPersoon(geslacht, voornamenArray,
                    achternaam, tussenvoegsel,
                    gebDatum, gebplaats, ouderlijkGezin);
        } catch (IllegalArgumentException ex) {
            showDialog("Warning!", ex.getMessage());
        }
        clearTabPersoonInvoer();
    }

    public void okGezinInvoer(Event evt) {
        Persoon ouder1 = (Persoon) cbOuder1Invoer.getSelectionModel().getSelectedItem();
        if (ouder1 == null) {
            showDialog("Warning", "eerste ouder is niet ingevoerd");
            return;
        }
        Persoon ouder2 = (Persoon) cbOuder2Invoer.getSelectionModel().getSelectedItem();
        Calendar huwdatum;
        try {
            huwdatum = StringUtilities.datum(tfHuwelijkInvoer.getText());
        } catch (IllegalArgumentException exc) {
            showDialog("Warning", "huwelijksdatum :" + exc.getMessage());
            return;
        }
        Gezin g;
        if (huwdatum != null) {
            g = getAdministratie().addHuwelijk(ouder1, ouder2, huwdatum);
            if (g == null) {
                showDialog("Warning", "Invoer huwelijk is niet geaccepteerd");
            } else {
                Calendar scheidingsdatum;
                try {
                    scheidingsdatum = StringUtilities.datum(tfScheidingInvoer.getText());
                    if (scheidingsdatum != null) {
                        getAdministratie().setScheiding(g, scheidingsdatum);
                    }
                } catch (IllegalArgumentException exc) {
                    showDialog("Warning", "scheidingsdatum :" + exc.getMessage());
                }
            }
        } else {
            g = getAdministratie().addOngehuwdGezin(ouder1, ouder2);
            if (g == null) {
                showDialog("Warning", "Invoer ongehuwd gezin is niet geaccepteerd");
            }
        }

        clearTabGezinInvoer();
    }

    public void cancelGezinInvoer(Event evt) {
        clearTabGezinInvoer();
    }

    public void showStamboom(Event evt) {
        Persoon selected = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showDialog("Warning", "Geen persoon geselecteerd!");
        } else {
            tfStamBoom.setText(selected.stamboomAlsString());
        }

    }

    public void createEmptyStamboom(Event evt) {
        this.clearAdministratie();
        clearTabs();
        initComboboxes();
    }

    public void openStamboom(Event evt) {
        File file = new File("data.bin");
        try {
            super.deserialize(file);
        } catch (IOException ex) {
            showDialog("Warning", ex.getMessage());
        } catch (ClassNotFoundException ex) {
            showDialog("Warning", ex.getMessage());
        }
        clearTabs();
        initComboboxes();
    }

    public void saveStamboom(Event evt) {
        File file = new File("data.bin");
        try {
            super.serialize(file);
        } catch (IOException ex) {
            showDialog("Warning", ex.getMessage());
        }
    }

    public void closeApplication(Event evt) {
        saveStamboom(evt);
        getStage().close();
    }

    public void configureStorage(Event evt) {
        withDatabase = cmDatabase.isSelected();
    }

    public void selectTab(Event evt) {
        Object source = evt.getSource();
        if (source == tabPersoon) {
            clearTabPersoon();
        } else if (source == tabGezin) {
            clearTabGezin();
        } else if (source == tabPersoonInvoer) {
            clearTabPersoonInvoer();
        } else if (source == tabGezinInvoer) {
            clearTabGezinInvoer();
        }
    }

    private void clearTabs() {
        clearTabPersoon();
        clearTabPersoonInvoer();
        clearTabGezin();
        clearTabGezinInvoer();
    }

    private void clearTabPersoonInvoer() {
        clearTabPersoon();
    }

    private void clearTabGezinInvoer() {
        cbOuder1Invoer.getSelectionModel().clearSelection();
        cbOuder2Invoer.getSelectionModel().clearSelection();
        tfHuwelijkInvoer.clear();
        tfScheidingInvoer.clear();
        cbSelecteerGezin.getSelectionModel().clearSelection();
    }

    private void clearTabPersoon() {
        cbPersonen.getSelectionModel().clearSelection();
        tfPersoonNr.clear();
        tfVoornamen.clear();
        tfTussenvoegsel.clear();
        tfAchternaam.clear();
        tfGeslacht.clear();
        tfGebDatum.clear();
        tfGebPlaats.clear();
        cbOuderlijkGezin.getSelectionModel().clearSelection();
        lvAlsOuderBetrokkenBij.setItems(FXCollections.emptyObservableList());
    }

    private void clearTabGezin() {
        clearTabGezinInvoer();

    }

    private void showDialog(String type, String message) {
        Stage myDialog = new Dialog(getStage(), type, message);
        myDialog.show();
    }

    private Stage getStage() {
        return (Stage) menuBar.getScene().getWindow();
    }

}
