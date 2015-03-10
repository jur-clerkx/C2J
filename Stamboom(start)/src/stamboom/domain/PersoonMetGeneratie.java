package stamboom.domain;

import java.io.Serializable;

public class PersoonMetGeneratie implements Serializable {
    // *********datavelden**************************************************
    private final String persoonsgegevens;
    private final int generatie;
    
    //***********************constructoren**********************************
    public PersoonMetGeneratie(String tekst, int generatie) {
        this.persoonsgegevens = tekst;
        this.generatie = generatie;
    }
    
    //**********************methoden****************************************
    public int getGeneratie() {
        return generatie;
    }

    public String getPersoonsgegevens() {
        return persoonsgegevens;
    }
}
