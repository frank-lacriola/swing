package it.poliba.swing;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;


public class FragmentRichiesta extends DialogFragment implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {
    EditText et_data;
    EditText et_ora;
    EditText et_posti;
    Switch abbonamento;
    EditText et_LPartenza;
    EditText et_LArrivo;
    boolean controllo_posti = false;



    RealmList<Offerta_Periodica> resMatchPeriodico = new RealmList<>();
    RealmList<Offerta> resMatchSemplice = new RealmList<>();
    RealmList<String> giorniSel = new RealmList<>();
    final Richiesta r = new Richiesta();
    final Richiesta_Periodica rp= new Richiesta_Periodica();
    Boolean caricamento = new Boolean(false);






    String[] day; //contiene tutti gli elementi
    String giorni_selezionati; //conterrĂ  la stringa composta da tutti gli elementi selezionati
    boolean[] checkedItems;
    ArrayList<Integer> UserItem = new ArrayList<>(); //contiene le posizioni all'interno della dialog degli elementi selezionati
    TextView giorni;
    int trovati=0;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        String syncServerURL = "https://swingdatabase.de1a.cloud.realm.io/SWING_DB";
        final SyncConfiguration config = new SyncConfiguration.Builder(SyncUser.current(), syncServerURL).build();
        final Realm realm = Realm.getInstance(config);

        final String mailUtente = ((Home_activity) getActivity()).utente.getEmail();
        final Utente utente = ((Home_activity) getActivity()).utente;

        final View view = inflater.inflate(R.layout.fragment_richiesta, container, false);
        Button invia = view.findViewById(R.id.InviaR);

        final Intent intentMatchList = new Intent(getContext(), MatchList_Activity.class);


        et_LPartenza = view.findViewById(R.id.etLuogoPartenzaF);
        et_LArrivo = view.findViewById(R.id.etLuogoArrivoF);

        et_data = view.findViewById(R.id.etDataRichiesta);
        et_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        et_ora = view.findViewById(R.id.etOraRichiesta);
        et_ora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        et_posti = view.findViewById(R.id.etPostiRichiesta);

        abbonamento = view.findViewById(R.id.switch1);
        day = getResources().getStringArray(R.array.numbers);
        checkedItems = new boolean[day.length];
        giorni = view.findViewById(R.id.giorni);
        abbonamento.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    giorni.setEnabled(true);
                    giorni.setAlpha(1f);

                    Toast.makeText(getContext(), "ABBONAMENTO", Toast.LENGTH_SHORT).show();

                    final AlertDialog.Builder scelta = new AlertDialog.Builder(getContext());
                    scelta.setTitle("Seleziona i giorni desiderati");

                    //Aggiungo gli elementi selezionati ad un vettore
                    scelta.setMultiChoiceItems(day, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                            if (isChecked) {
                                if (!UserItem.contains(position)) {
                                    UserItem.add(position);
                                }

                            } else {
                                for (int i = 0; i < UserItem.size(); i++) {
                                    if (UserItem.get(i) == position) {
                                        UserItem.remove(i);
                                    }
                                }
                            }
                        }

                    });


                    //Comportamento in seguito all'azione di Conferma
                    scelta.setCancelable(false);
                    scelta.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!UserItem.isEmpty()) {
                                giorni_selezionati = "";
                                for (int i = 0; i < UserItem.size(); i++) {
                                    giorni_selezionati = giorni_selezionati + day[UserItem.get(i)];
                                    giorniSel.add(day[UserItem.get(i)]);
                                    if (i != UserItem.size() - 1) {
                                        giorni_selezionati = giorni_selezionati + "-";
                                    }
                                }
                                giorni.setText(giorni_selezionati);
                            } else giorni.setText("Giorni");
                        }
                    });

                    //Comportamento in seguito all'azione di Annulla
                    scelta.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


                    giorni.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog giorni = scelta.create();
                            giorni.show();
                        }
                    });

                } else {
                    giorni.setEnabled(false);
                    giorni.setAlpha(0);
                }
            }
        });


        //caricamento dati sul DataBase

        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String numero = et_posti.getText().toString();
                int intero = Integer.parseInt(numero);
                double numerocodrich = Math.random() * 99999999;


                if (giorniSel.isEmpty()) {


                    r.setCodRichiesta((int) numerocodrich);
                    r.setDataPartenza(et_data.getText().toString());
                    r.setLuogoPartenza(et_LPartenza.getText().toString());
                    r.setLuogoArrivo(et_LArrivo.getText().toString());
                    r.setNumPosti(intero);
                    r.setOra(et_ora.getText().toString());
                    r.setMailUtente(mailUtente);

                    if (et_data.getText().toString() == "" || et_LPartenza.getText().toString() == "" || et_LArrivo.getText().toString() == ""
                            || et_posti.getText().toString() == "" || et_ora.getText().toString() == "") {
                        Toast.makeText(getContext(),"Compila tutti i campi necessri prima",Toast.LENGTH_LONG).show(); }

                    else {


                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                                realm.copyToRealm(r);
                            }
                        });
                    }

                    //controllo se la transazione Ă¨ andata a buon fine
                    if (realm.where(Richiesta.class).equalTo("codRichiesta", (int) numerocodrich).count() != 0) {
                        Toast.makeText(getContext(), "Richiesta correttamente pubblicata", Toast.LENGTH_LONG).show();
                        caricamento = true;
                    } else {
                        Toast.makeText(getContext(), "La richiesta non Ă¨ andata a buon fine ", Toast.LENGTH_LONG).show();
                    }

                } else {

                    rp.setCodRichiesta((int) numerocodrich);
                    rp.setDataPartenza(et_data.getText().toString());
                    rp.setLuogoPartenza(et_LPartenza.getText().toString());
                    rp.setLuogoArrivo(et_LArrivo.getText().toString());
                    rp.setNumPosti(intero);
                    rp.setGiorni(giorniSel);
                    rp.setMailUtente(mailUtente);

                    if (et_data.getText().toString() == "" || et_LPartenza.getText().toString() == "" || et_LArrivo.getText().toString() == ""
                            || et_posti.getText().toString() == "" || giorniSel.isEmpty()) {
                        Toast.makeText(getContext(),"Compila tutti i campi necessri prima",Toast.LENGTH_LONG).show(); }
                    else {

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealm(rp);
                            }
                        });
                    }

                    //controllo se la transazione Ă¨ andata a buon fine
                    if (realm.where(Richiesta_Periodica.class).equalTo("codRichiesta", (int) numerocodrich).count() != 0) {
                        Toast.makeText(getContext(), "Richiesta periodica correttamente pubblicata", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "La richiesta non Ă¨ andata a buon fine ", Toast.LENGTH_LONG).show();
                    }

                }


                // settore del matching
                if (giorniSel.isEmpty() && caricamento == true) {

                    //Controllo se c'Ă¨ giĂ  un' offerta pubblicata dall'Utente richiedente con gli stessi parametri della richiesta

                    if (realm.where(Offerta.class)
                            .equalTo("luogoPartenza", r.getLuogoPartenza())
                            .equalTo("luogoArrivo", r.getLuogoArrivo()).equalTo("emailUtente", r.getMailUtente())
                            .equalTo("data", r.getDataPartenza()).count() == 0) {

                        RealmResults<Offerta> queryRes = realm.where(Offerta.class)
                                .equalTo("luogoPartenza", r.getLuogoPartenza())
                                .equalTo("luogoArrivo", r.getLuogoArrivo()).equalTo("data", r.getDataPartenza()).findAll();

                    if (!queryRes.isEmpty()) {

                        for (int i = 0; i < queryRes.size(); i++) {
                            if (queryRes.get(i).getNumPostiDisponibili() >= r.getNumPosti()) {
                                resMatchSemplice.add(queryRes.get(i));
                                Toast.makeText(getContext(), "Match Semplice riuscito ", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else{ Toast.makeText(getContext(),"Non c'Ă¨ nessuna offerta nella data selezionata",Toast.LENGTH_LONG).show(); }

                    } else {
                        Toast.makeText(getContext(), "C'Ă¨ un offerta da te formulata " +
                                "con gli stessi parametri richiesti in questa data !! "
                                , Toast.LENGTH_LONG).show();   }
                } else {





                    //MATCH PER OFF-RICH PERIODICHE


                    System.err.println("LISTA GIORNI SELEZIONATI");
                    for (int count=0;count < giorniSel.size(); count++){
                        System.out.println( giorniSel.get(count) ); }


                    if (realm.where(Offerta_Periodica.class)
                            .equalTo("luogoPartenza", et_LPartenza.getText().toString())
                            .equalTo("luogoArrivo", et_LArrivo.getText().toString()).equalTo("emailUtente", rp.getMailUtente()).count() == 0) {
                    if (realm.where(Offerta_Periodica.class)
                            .equalTo("luogoPartenza", et_LPartenza.getText().toString())
                            .equalTo("luogoArrivo", et_LArrivo.getText().toString()).count() != 0) {

                        RealmResults<Offerta_Periodica> queryResultsP = realm.where(Offerta_Periodica.class)
                                .equalTo("luogoPartenza", et_LPartenza.getText().toString())
                                .equalTo("luogoArrivo", et_LArrivo.getText().toString()).findAll();

                        for (int i = 0; i < queryResultsP.size(); i++) {
                            trovati = 0;

                            System.err.println("LISTA GIORNI DELL'' OFFERTA "+i);
                            for (int count1=0; count1<queryResultsP.get(i).getGiorni().size(); count1++){
                               System.out.println(queryResultsP.get(i).getGiorni().get(count1));
                            }

                            for (int k = 0; k < queryResultsP.get(i).getGiorni().size(); k++) {
                                if (trovati == giorniSel.size()) {
                                    resMatchPeriodico.add(queryResultsP.get(i));
                                    break;
                                }

                                for (int j = 0; j < giorniSel.size(); j++) {
                                    if (giorniSel.get(j) == queryResultsP.get(i).getGiorni().get(k))
                                        trovati++;


                                }
                            }
                        }
                    }
                }
                }


                if (!resMatchPeriodico.isEmpty()) {
                    Toast.makeText(getContext(), "Match periodico riuscito", Toast.LENGTH_LONG).show();
                } else if (!resMatchSemplice.isEmpty()) {
                    Toast.makeText(getContext(), "Match Semplice riuscito ", Toast.LENGTH_LONG).show();
                    Bundle b1 = new Bundle();
                    b1.putParcelable("object_key", utente);
                    intentMatchList.putExtras(b1);
                    startActivity(intentMatchList);
                }





            }


        });


        return view;
    }

    final int StartYear = 2019;
    final int StartMonth = 12;
    final int StartDay = 31;


    private void showDatePickerDialog() {
        DatePickerDialog dpd = new DatePickerDialog(
                getContext(),
                this,
                StartYear,
                StartMonth,
                StartDay);
        dpd.show();
    }

    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month = month + 1;
        String date = "" + dayOfMonth + "/" + month;
        et_data.setText(date);
    }


    NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            et_posti.setText(" " + picker.getValue());
        }
    };

    private void showTimePickerDialog() {
        TimePickerDialog tpd = new TimePickerDialog(
                getContext(),
                this,
                Calendar.HOUR_OF_DAY,
                Calendar.MINUTE,
                true);
        tpd.show();
    }

    public void onTimeSet(TimePicker t, int hour, int minute) {
        String time = "" + hour + ":" + minute + "";
        et_ora.setText(time);
    }


}

