package stamboom.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Administratie implements Serializable{

    //************************datavelden*************************************
    private int nextGezinsNr;
    private int nextPersNr;
    private List<Persoon> personen;
    private List<Gezin> gezinnen;
    private transient ObservableList<Persoon> observablePersonen;
    private transient ObservableList<Gezin> observableGezinnen;

    //***********************constructoren***********************************
    /**
     * er wordt een lege administratie aangemaakt. observablePersonen en observableGezinnen die in
 de toekomst zullen worden gecreeerd, worden (apart) opvolgend genummerd
 vanaf 1
     */
    public Administratie() {
        nextGezinsNr = 1;
        nextPersNr = 1;
        personen = new ArrayList();
        gezinnen = new ArrayList();
        this.observablePersonen = FXCollections.observableList(personen);
        this.observableGezinnen = FXCollections.observableList(gezinnen);
    }

    //**********************methoden****************************************
    /**
     * er wordt een persoon met de gegeven parameters aangemaakt; de persoon
     * krijgt een uniek nummer toegewezen, en de persoon is voortaan ook bij het
     * (eventuele) ouderlijk gezin bekend. Voor de voornamen, achternaam en
     * gebplaats geldt dat de eerste letter naar een hoofdletter en de
     * resterende letters naar kleine letters zijn geconverteerd; het
     * tussenvoegsel is in zijn geheel geconverteerd naar kleine letters;
     * overbodige spaties zijn verwijderd
     *
     * @param geslacht
     * @param vnamen vnamen.length>0; alle strings zijn niet leeg
     * @param anaam niet leeg
     * @param tvoegsel mag leeg zijn
     * @param gebdat
     * @param gebplaats niet leeg
     * @param ouderlijkGezin mag de waarde null (=onbekend) hebben
     *
     * @return de nieuwe persoon. Als de persoon al bekend was (op basis van
     * combinatie van getNaam(), geboorteplaats en geboortedatum), wordt er null
     * geretourneerd.
     */
    public Persoon addPersoon(Geslacht geslacht, String[] vnamen, String anaam,
            String tvoegsel, Calendar gebdat,
            String gebplaats, Gezin ouderlijkGezin) {

        if (vnamen.length == 0) {
            throw new IllegalArgumentException("ten minste 1 voornaam");
        }
        for (String voornaam : vnamen) {
            if (voornaam.trim().isEmpty()) {
                throw new IllegalArgumentException("lege voornaam is niet toegestaan");
            }
        }

        if (anaam.trim().isEmpty()) {
            throw new IllegalArgumentException("lege achternaam is niet toegestaan");
        }

        if (gebplaats.trim().isEmpty()) {
            throw new IllegalArgumentException("lege geboorteplaats is niet toegestaan");
        }
        //vnamen omzetten naar goede vorm voor controle
        String voornamen = "";
        String[] vnamenNew = new String[vnamen.length];
        int i = 0;
        for (String s : vnamen) {
            s = s.trim();
            voornamen += s.substring(0, 1).toUpperCase() + ".";
            vnamenNew[i] = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            i++;
        }
        voornamen = voornamen.trim();
        //Controleren of persoon al bestaat
        boolean bestaat = false;
        for (Persoon p : this.getPersonen()) {
            if (p.getAchternaam().equals(anaam.substring(0,1).toUpperCase() + anaam.substring(1).toLowerCase())
                    && p.getTussenvoegsel().equals(tvoegsel.toLowerCase())
                    && p.getGebDat().equals(gebdat)
                    && p.getGebPlaats().equals(gebplaats.substring(0,1).toUpperCase() + gebplaats.substring(1).toLowerCase())
                    && p.getInitialen().equals(voornamen)) {
                return null;
            }
        }
        Persoon p = new Persoon(getNextPersNr(), vnamenNew, anaam, tvoegsel, gebdat, gebplaats, geslacht, ouderlijkGezin);
        this.observablePersonen.add(p);
        if (ouderlijkGezin != null) {
            ouderlijkGezin.breidUitMet(p);
        }
        return p;
    }

    /**
     * er wordt, zo mogelijk (zie return) een (kinderloos) ongehuwd gezin met
     * ouder1 en ouder2 als ouders gecreeerd; de huwelijks- en scheidingsdatum
     * zijn onbekend (null); het gezin krijgt een uniek nummer toegewezen; dit
     * gezin wordt ook bij de afzonderlijke ouders geregistreerd;
     *
     * @param ouder1
     * @param ouder2 mag null zijn
     *
     * @return het nieuwe gezin. null als ouder1 = ouder2 of als een van de
     * volgende voorwaarden wordt overtreden: 1) een van de ouders is op dit
     * moment getrouwd 2) het koppel vormt al een ander gezin
     */
    public Gezin addOngehuwdGezin(Persoon ouder1, Persoon ouder2) {
        if (ouder1 == ouder2) {
            return null;
        }

        if (ouder1.getGebDat().compareTo(Calendar.getInstance()) > 0) {
            return null;
        }
        if (ouder2 != null && ouder2.getGebDat().compareTo(Calendar.getInstance()) > 0) {
            return null;
        }

        Calendar nu = Calendar.getInstance();
        if (ouder1.isGetrouwdOp(nu) || (ouder2 != null
                && ouder2.isGetrouwdOp(nu))
                || ongehuwdGezinBestaat(ouder1, ouder2)) {
            return null;
        }

        Gezin gezin = new Gezin(nextGezinsNr, ouder1, ouder2);
        nextGezinsNr++;
        observableGezinnen.add(gezin);

        ouder1.wordtOuderIn(gezin);
        if (ouder2 != null) {
            ouder2.wordtOuderIn(gezin);
        }

        return gezin;
    }

    /**
     * Als het ouderlijk gezin van persoon nog onbekend is dan wordt persoon een
     * kind van ouderlijkGezin, en tevens wordt persoon als kind in dat gezin
     * geregistreerd. Als de ouders bij aanroep al bekend zijn, verandert er
     * niets
     *
     * @param persoon
     * @param ouderlijkGezin
     * @return of ouderlijk gezin kon worden toegevoegd.
     */
    public boolean setOuders(Persoon persoon, Gezin ouderlijkGezin) {
        return persoon.setOuders(ouderlijkGezin);
    }

    /**
     * als de ouders van dit gezin gehuwd zijn en nog niet gescheiden en datum
     * na de huwelijksdatum ligt, wordt dit de scheidingsdatum. Anders gebeurt
     * er niets.
     *
     * @param gezin
     * @param datum
     * @return true als scheiding geaccepteerd, anders false
     */
    public boolean setScheiding(Gezin gezin, Calendar datum) {
        return gezin.setScheiding(datum);
    }

    /**
     * registreert het huwelijk, mits gezin nog geen huwelijk is en beide ouders
     * op deze datum mogen trouwen (pas op: het is niet toegestaan dat een ouder
     * met een toekomstige (andere) trouwdatum trouwt.)
     *
     * @param gezin
     * @param datum de huwelijksdatum
     * @return false als huwelijk niet mocht worden voltrokken, anders true
     */
    public boolean setHuwelijk(Gezin gezin, Calendar datum) {
        return gezin.setHuwelijk(datum);
    }

    /**
     *
     * @param ouder1
     * @param ouder2
     * @return true als dit koppel (ouder1,ouder2) al een ongehuwd gezin vormt
     */
    boolean ongehuwdGezinBestaat(Persoon ouder1, Persoon ouder2) {
        return ouder1.heeftOngehuwdGezinMet(ouder2) != null;
    }

    /**
     * als er al een ongehuwd gezin voor dit koppel bestaat, wordt het huwelijk
     * voltrokken, anders wordt er zo mogelijk (zie return) een (kinderloos)
     * gehuwd gezin met ouder1 en ouder2 als ouders gecreeerd; de
     * scheidingsdatum is onbekend (null); het gezin krijgt een uniek nummer
     * toegewezen; dit gezin wordt ook bij de afzonderlijke ouders
     * geregistreerd;
     *
     * @param ouder1
     * @param ouder2
     * @param huwdatum
     * @return null als ouder1 = ouder2 of als een van de ouders getrouwd is
     * anders het gehuwde gezin
     */
    public Gezin addHuwelijk(Persoon ouder1, Persoon ouder2, Calendar huwdatum) {
        //Controleer of huwelijk aan voorwaarden voldoet
        if (ouder1 == ouder2) {
            return null;
        }
        if (!ouder1.kanTrouwenOp(huwdatum) || !ouder2.kanTrouwenOp(huwdatum)) {
            return null;
        }
        //Controleer of gezin al bestaat, zo ja vul hier dan huwelijksdatum in
        for (Gezin gezin : ouder1.getAlsOuderBetrokkenIn()) {
            if ((gezin.getOuder1() == ouder2 || gezin.getOuder2() == ouder2) && gezin.isOngehuwd()) {
                if (gezin.setHuwelijk(huwdatum)) {
                    return gezin;
                } else {
                    return null;
                }
            }
        }
        //Zo niet, maak een nieuw huwelijk aan.
        Gezin gezin = addOngehuwdGezin(ouder1, ouder2);
        gezin.setHuwelijk(huwdatum);
        return gezin;
    }

    /**
     *
     * @return het aantal geregistreerde observablePersonen
     */
    public int aantalGeregistreerdePersonen() {
        return nextPersNr - 1;
    }

    /**
     *
     * @return het aantal geregistreerde observableGezinnen
     */
    public int aantalGeregistreerdeGezinnen() {
        return nextGezinsNr - 1;
    }

    /**
     *
     * @param nr
     * @return de persoon met nummer nr, als die niet bekend is wordt er null
     * geretourneerd
     */
    public Persoon getPersoon(int nr) {
        for (Persoon persoon : getPersonen()) {
            if (persoon.getNr() == nr) {
                return persoon;
            }
        }
        return null;
    }

    /**
     * @param achternaam
     * @return alle observablePersonen met een achternaam gelijk aan de meegegeven
 achternaam (ongeacht hoofd- en kleine letters)
     */
    public ArrayList<Persoon> getPersonenMetAchternaam(String achternaam) {
        ArrayList<Persoon> personen = new ArrayList<Persoon>();
        for (Persoon persoon : getPersonen()) {
            if (persoon.getAchternaam().toLowerCase().equals(achternaam.toLowerCase())) {
                personen.add(persoon);
            }
        }
        return personen;
    }

    /**
     *
     * @return de geregistreerde observablePersonen
     */
    public ObservableList<Persoon> getPersonen() {
        return (ObservableList<Persoon>) FXCollections.unmodifiableObservableList(observablePersonen);
    }

    /**
     *
     * @param vnamen
     * @param anaam
     * @param tvoegsel
     * @param gebdat
     * @param gebplaats
     * @return de persoon met dezelfde initialen, tussenvoegsel, achternaam,
     * geboortedatum en -plaats mits bekend (ongeacht hoofd- en kleine letters),
     * anders null
     */
    public Persoon getPersoon(String[] vnamen, String anaam, String tvoegsel,
            Calendar gebdat, String gebplaats) {
        //Maak controleerbare lijst met voornamen
        String voornamen = "";
        for (String s : vnamen) {
            s = s.trim();
            voornamen += s.substring(0, 1).toLowerCase() + ".";
        }
        voornamen = voornamen.trim();
        //Controleer of persoon bestaat.
        for (Persoon p : getPersonen()) {
            if (p.getInitialen().toLowerCase().equals(voornamen.toLowerCase()) && p.getAchternaam().toLowerCase().equals(anaam.toLowerCase())
                    && p.getTussenvoegsel().toLowerCase().equals(tvoegsel.toLowerCase())
                    && p.getGebDat().equals(gebdat)
                    && p.getGebPlaats().toLowerCase().equals(gebplaats.toLowerCase())) {
                return p;
            }
        }
        return null;
    }

    /**
     *
     * @return de geregistreerde observableGezinnen
     */
    public ObservableList<Gezin> getGezinnen() {
        return FXCollections.unmodifiableObservableList(this.observableGezinnen);
    }

    /**
     *
     * @param gezinsNr
     * @return het gezin met nummer nr. Als dat niet bekend is wordt er null
     * geretourneerd
     */
    public Gezin getGezin(int gezinsNr) {
        // aanname: er worden geen observableGezinnen verwijderd
        if (observableGezinnen != null && 1 <= gezinsNr && gezinsNr <= observableGezinnen.size()) {
            return observableGezinnen.get(gezinsNr - 1);
        }
        return null;
    }

    /**
     * Geeft het volgende gezinsnummer en verhoogt het volgende gezinsnummer
     * direct
     *
     * @return Het nieuwe gezinsnummer
     */
    public int getNextGezinNr() {
        int result = this.nextGezinsNr;
        nextGezinsNr++;
        return result;
    }

    /**
     * Geeft het volgende persoonsnummer en verhoogt het volgende persoonsnummer
     * direct
     *
     * @return Het nieuwe persoonsnummer
     */
    public int getNextPersNr() {
        int result = this.nextPersNr;
        this.nextPersNr++;
        return result;
    }
    
    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        observablePersonen = FXCollections.observableArrayList(personen);
        observableGezinnen = FXCollections.observableArrayList(gezinnen);
    }
}
