package it.poliba.swing;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.Date;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Offerta extends RealmObject {
    @PrimaryKey
    private int codOfferta;
    @Required
    private RealmList<String> luogoPartenza;
    @Required
    private String luogoArrivo;
    @Required
    private Date data;


    private float prezzo;

    public Offerta() {
    }

    public Offerta(int codOfferta, RealmList<String> luogoPartenza, String luogoArrivo, Date data, float prezzo) {
        this.codOfferta = codOfferta;
        this.luogoPartenza = luogoPartenza;
        this.luogoArrivo = luogoArrivo;
        this.data = data;
        this.prezzo = prezzo;
    }

    public int getCodOfferta() {
        return codOfferta;
    }

    public RealmList<String> getLuogoPartenza() {
        return luogoPartenza;
    }

    public String getLuogoArrivo() {
        return luogoArrivo;
    }

    public Date getData() {
        return data;
    }



    public float getPrezzo() {
        return prezzo;
    }
}
