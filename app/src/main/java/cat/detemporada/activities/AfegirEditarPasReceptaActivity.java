package cat.detemporada.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.picasso.Picasso;

import java.io.File;

import cat.detemporada.R;
import cat.detemporada.helpers.Utils;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.PasRecepta;

/*
 *  AfegirEditarPasReceptaActivity
 *
 *  Activitat que permet afegir o editar un PasRecepta
 */

public class AfegirEditarPasReceptaActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String ITEM_ID = "itemId";
    public final static String POSITION = "position";

    private static final int OBRE_CAMERA_CODE = 0;
    private static final int OBRE_GALERIA_CODE = 1;
    private static final int PERMISSION_EXERNAL_STORAGE = 2;


    private Imatge imatge;
    private AppCompatImageButton imatgeView;
    private EditText viewPas;

    private PasRecepta pasRecepta;

    private Context context;
    private File fileImatge;

    private int position = -1;

    boolean permis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afegir_editar_pas_recepta);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this.getApplicationContext();

        // Mostrem la fletxa per tornar enrere
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*
         *  Inicialitzem les Views del layout i establim un OnClickListener al botó submit
         */
        imatgeView = findViewById(R.id.pas_imatge);
        imatgeView.setOnClickListener(this);
        viewPas = findViewById(R.id.pas_text);

        Button submit = findViewById(R.id.boto_submit);
        submit.setOnClickListener(this);

        /*
         *  Recollim el Intent que ha llançat l'activitat i comprovem si volem afegir o editar un PasRecepta
         *  En cas que l'acció sigui d'editar, ens assegurem que ens han passat l'ID de l'ingredient i la posició
         *  dins l'array d'instruccions de la recepta.
         *  En tots els casos contraris tanquem l'activitat.
         */

        Intent intentEntrant = getIntent();
        if (intentEntrant != null && intentEntrant.getAction() != null) {
            if (intentEntrant.hasExtra(ITEM_ID) && intentEntrant.getAction().equals(Intent.ACTION_EDIT)) {
                Long itemId = intentEntrant.getLongExtra(ITEM_ID, 0);
                position = intentEntrant.getIntExtra(POSITION, -1);
                if (itemId > 0 && position != -1) {
                    pasRecepta = PasRecepta.findById(PasRecepta.class, itemId);
                    if(pasRecepta != null) {
                        viewPas.setText(pasRecepta.getPas());
                        imatge = pasRecepta.getImatge();
                        if (imatge != null) {
                            Picasso.get().load(imatge.getPath()).fit().centerCrop().into(imatgeView);
                        }
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            } else {
                pasRecepta = new PasRecepta();
            }
        } else {
            finish();
        }

    }


    /*
     *  Com que l'activitat implementa View.OnClickListener podem fer un Override del mètode onClick i gestionar tots els esdeveniments
     *  onClick des d'aqui
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.pas_imatge:
                /*
                 *  Quan l'usuari clica sobre el botó que mostra l'imatge del pas s'obre un AlertDialog que permet escollir si fer
                 *  una foto amb la càmera o seleccionar una imatge de la galeria
                 */
                CharSequence opcions[] = new CharSequence[] {"Fes una foto", "Ves a la galeria"};

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setItems(opcions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                //  Les imatges capturades amb la càmera s'emmagatzemen a la memòria externa de l'aplicació
                                // ja que així l'usuari en pot tenir accès però per això cal que
                                //  comprovem si tenim permis per llegir i escriure aquesta memòria externa.
                                permis = comprovaPermisMemoriaExterna();
                                if(permis) {
                                    // Si tenim permis iniciem l'intent que llança la càmera
                                    iniciaCameraIntent();
                                }
                                break;
                            case 1:
                                //  Si l'usuari vol seleccionar una imatge de la galeria, creem un intent que crida el MediaStore amb l'acció
                                //  ACTION_PICK i ens permetrà fer servir la galeria del telèfon per seleccionar-ne una.
                                Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intentGaleria , OBRE_GALERIA_CODE);
                                break;
                        }
                    }
                });
                builder.show();
                break;
            case R.id.boto_submit:
                //  Si l'usuari pitja el botó per Desar el PasRecepta, comprovem que hagi escrit algun text (l'imatge és opcional) i en cas
                //  afirmatiu emmagatzamem el pas a la base de dades i tanquem l'activitat retornant l'ID del pas i si s'escau la posició que ens
                //  havien passat previament.
                String pas = viewPas.getText().toString();
                if(pas.equals("")) {
                    viewPas.setError("Introdueix una explicació del pas");
                } else {
                    pasRecepta.setPas(pas);
                    pasRecepta.save();
                    Intent intent = new Intent();
                    intent.putExtra("pasId", pasRecepta.getId());
                    if(position != -1) {
                        intent.putExtra("position", position);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                }
        }
    }

    private void iniciaCameraIntent(){
        /*
         *  Per tal de permetre l'usuari fer fotos i afegir-les a les receptes o els passos sense haver d'implementar una CameraView i gestionar
         *  tot plegat, podem cridar l'aplicació de Càmera preferida per l'usuari mitjançant un Intent amb l'acció ACTION_IMAGE_CAPTURE.
         *  Especificant una Uri amb el Extra MediaStore.EXTRA_OUTPUT fem que l'aplicació iniciada emmagatzemi el resultat en aquesta localització però
         *  per tal de que pugui accedir a l'arxiu li donem l'adreça que passa per el nostre FileProvider i el permís amb FLAG_GRANT_READ_URI_PERMISSION.
         */
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileImatge = Utils.creaFileImatge();
        Uri uriImatgePerExtern = FileProvider.getUriForFile(context, "cat.detemporada.fileprovider", fileImatge);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uriImatgePerExtern);
        intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intentCamera, OBRE_CAMERA_CODE);
    }

    // En clicar la fletxa per tornar enrere executem el mètode onBackPressed()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case OBRE_CAMERA_CODE:
                /*
                 *  Un cop retornats de l'aplicació de la Càmera, si el resultat és d'èxit, creem una nova imatge a partir de l'arxiu
                 *  mitjançant el constructor apropiat i l'emmagatzamem. No cal que fem cap acció sobre l'arxiu perque aquest ja és nostre
                 *  i tenim permis per llegir-lo i modificar-lo.
                 *  També establim la nova Imatge creada com a imatge del PasRecepta i actualitzem l'imatge mostrada per a que es
                 *  correspongui amb la nova.
                 */
                if(resultCode == RESULT_OK){
                    if(imatge != null) {
                        imatge.delete();
                    }
                    imatge = new Imatge(fileImatge);
                    imatge.save();
                    pasRecepta.setImatge(imatge);
                    Picasso.get().load(imatge.getPath()).fit().centerCrop().into(imatgeView);
                }

                break;
            case OBRE_GALERIA_CODE:
                /*
                 *  L'aplicació de la galeria ens retorna una Uri donada pel seu FileProvider que podem llegir però com que no sabem
                 *  quant de temps tindrem permís per accedir a aquesta imatge, la copiem a la nostra memòria interna mitjançant el mètode
                 *  salvaImatgedeUri.
                 *  Un cop tenim l'imatge emmagatzemada, creem un nou objecte Imatge a partir d'aquesta i l'emmagatzamem a la base de dades.
                 *  També establim la nova Imatge creada com a imatge del PasRecepta i actualitzem l'imatge mostrada per a que es
                 *  correspongui amb la nova.
                 */
                if(resultCode == RESULT_OK){
                    if(imatge != null) {
                        imatge.delete();
                    }
                    Uri imatgeSeleccionada = data.getData();
                    File arxiu = Utils.salvaImatgeDeUri(context, imatgeSeleccionada);
                    imatge = new Imatge(arxiu);
                    imatge.save();
                    pasRecepta.setImatge(imatge);
                    Picasso.get().load(imatge.getPath()).fit().centerCrop().into(imatgeView);
                }
                break;
        }
    }

    /*
     *  Mètode encarregat de comprovar si tenim permís per escriure a memòria externa. En cas contrari demanem aquest permís i el que
     *  ens permet llegir-la també.
     */
    private boolean comprovaPermisMemoriaExterna() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_EXERNAL_STORAGE);
            return false;
        } else {
            return true;
        }
    }

    /*
     *  Després del diàleg que permet a l'usuari concedir o dener un permís es crida aquest mètode de l'Activity, on comprovem
     *  si ens han estat concedits els permisos i en cas afirmatiu iniciem l'intent que llança l'aplicació de la Càmera.
     *  Si l'usuari ens ha denegat el permis no fem res, ja el tornarem a demanar quan l'usuari vulgui fer una foto de nou.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
                                           String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_EXERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permis = true;
                    iniciaCameraIntent();
                } else {
                    permis = false;
                }
            }
        }
    }

}
