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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.fragments.LlistaIngredientsFragment;
import cat.detemporada.fragments.LlistaPasReceptaFragment;
import cat.detemporada.helpers.Utils;
import cat.detemporada.models.CategoriaRecepta;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.PasRecepta;
import cat.detemporada.models.Recepta;

/*
 *  AfegirEditarReceptaActivity
 *
 *  Activitat que permet afegir o editar una Recepta
 */

public class AfegirEditarReceptaActivity extends AppCompatActivity implements LlistaIngredientsFragment.OnIngredientsSelectedListener, View.OnClickListener, LlistaPasReceptaFragment.OnPasReceptaSelectedListener {
    public final static String ITEM_ID = "itemId";

    private static final int OBRE_CAMERA_CODE = 0;
    private static final int OBRE_GALERIA_CODE = 1;
    private static final int AFEGIR_INGREDIENT_CODE = 2;
    private static final int EDITAR_INGREDIENT_CODE = 3;
    private static final int AFEGIR_PAS_RECEPTA_CODE = 4;
    private static final int EDITAR_PAS_RECEPTA_CODE = 5;
    private static final int PERMISSION_EXTERNAL_STORAGE = 6;

    private Recepta recepta;
    private FrameLayout frameIngredients;
    private TextView viewIngredientsTitol;
    private Long itemId = 0L;

    private Imatge imatge;
    private List<Ingredient> ingredients = new ArrayList<>();
    private LlistaIngredientsFragment fragmentIngredients = new LlistaIngredientsFragment();

    private List<PasRecepta> instruccions = new ArrayList<>();
    private LlistaPasReceptaFragment fragmentInstruccions = new LlistaPasReceptaFragment();

    private List<AppCompatImageButton> stars = new ArrayList<>();
    private AppCompatImageButton imatgeView;
    private EditText viewNom, viewDescripcio, viewTemps, viewPersones;
    private Spinner viewDificultat, viewCategoria;

    private Context context;
    private File fileImatge;

    private boolean permis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afegir_editar_recepta);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Emmagatzemem el context per iniciar els intents
        context = this.getApplicationContext();

        // Mostrem la fletxa per tornar enrere
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*
         *  Inicialitzem les Views del layout i establim un OnClickListener al botons
         */
        imatgeView = findViewById(R.id.recepta_imatge);
        imatgeView.setOnClickListener(this);

        frameIngredients = findViewById(R.id.frame_ingredients);
        viewIngredientsTitol = findViewById(R.id.recepta_ingredients_titol);
        viewIngredientsTitol.setOnClickListener(this);

        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star1));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star2));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star3));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star4));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star5));
        for(AppCompatImageButton star: stars) {
            star.setOnClickListener(this);
        }

        viewNom = findViewById(R.id.recepta_nom);
        viewDescripcio = findViewById(R.id.recepta_descripcio);
        viewDificultat = findViewById(R.id.recepta_dificultat);
        viewCategoria = findViewById(R.id.recepta_categoria);
        viewTemps = findViewById(R.id.recepta_temps);
        viewPersones = findViewById(R.id.recepta_persones);

        Button afegirIngredient = findViewById(R.id.boto_afegir_ingredients);
        afegirIngredient.setOnClickListener(this);

        Button afegirPasRecepta = findViewById(R.id.boto_afegir_pas);
        afegirPasRecepta.setOnClickListener(this);

        Button submit = findViewById(R.id.boto_submit);
        submit.setOnClickListener(this);

        ArrayAdapter<Recepta.Dificultat> arrayDificultat = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, Recepta.Dificultat.values());
        viewDificultat.setAdapter(arrayDificultat);

        ArrayAdapter<CategoriaRecepta> arrayCategoria = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, CategoriaRecepta.values());
        viewCategoria.setAdapter(arrayCategoria);

        /*
         *  Recollim el Intent que ha llançat l'activitat i comprovem si volem afegir o editar una Recepta
         *  En cas que l'acció sigui d'editar, ens assegurem que ens han passat l'ID de la recepta i establim
         *  els valors de les View de manera corresponent.
         *  En tots els casos contraris tanquem l'activitat.
         */

        Intent intentEntrant = getIntent();
        if (intentEntrant != null && intentEntrant.getAction() != null) {
            if (intentEntrant.hasExtra(ITEM_ID) && intentEntrant.getAction().equals(Intent.ACTION_EDIT)) {
                itemId = intentEntrant.getLongExtra(ITEM_ID, 0);
                if (itemId > 0) {
                    recepta = Recepta.findById(Recepta.class, itemId);
                    if(recepta != null) {
                        ingredients = recepta.loadIngredients();
                        instruccions = recepta.loadInstruccions();

                        viewNom.setText(recepta.getNom());
                        viewDescripcio.setText(recepta.getDescripcio());

                        imatge = recepta.loadImatgePrincipal();
                        if (imatge != null) {
                            Picasso.get().load(imatge.getPath()).fit().centerCrop().into(imatgeView);
                        }

                        actualitzaEstrelles(recepta.getValoracio());

                        viewDificultat.setSelection(arrayDificultat.getPosition(recepta.getDificultat()));
                        viewCategoria.setSelection(arrayCategoria.getPosition(recepta.getCategoria()));

                        viewTemps.setText(String.format(Locale.getDefault(), "%d", recepta.getTemps()));
                        viewPersones.setText(String.format(Locale.getDefault(), "%d", recepta.getPersones()));
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            } else {
                recepta = new Recepta(true, true);
                recepta.save();
            }
            carregaIngredientsFragment();
            carregaInstruccionsFragment();
        } else {
            finish();
        }

    }

    //  Mètode que carrega el Fragment amb la llista de Ingredient per a una Recepta donada
    //  passant-li l'ID corresponent
    private void carregaIngredientsFragment() {
        Bundle arguments = new Bundle();
        arguments.putLong(LlistaIngredientsFragment.RECEPTA_ID, itemId);
        fragmentIngredients.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_ingredients, fragmentIngredients)
                .commit();
    }

    //  Mètode que carrega el Fragment amb la llista de PasRecepta per a una Recepta donada
    //  passant-li l'ID corresponent
    private void carregaInstruccionsFragment() {
        Bundle arguments = new Bundle();
        arguments.putLong(LlistaPasReceptaFragment.RECEPTA_ID, itemId);
        fragmentInstruccions = new LlistaPasReceptaFragment();
        fragmentInstruccions.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_instruccions, fragmentInstruccions)
                .commit();
    }

    // Aquest mètodes no els fem servir en aquesta activitat però cal implementar-los
    public void onIngredientSelected(Long itemId) {}
    public void onAfegirALlistaCompraSelected(Long itemId) {}

    // Implementació del mètode de l'interface LlistaIngredientsFragment.OnIngredientsSelectedListener que inicia
    // l'activitat AfegirEditarPasReceptaActivity per editar el Ingredient
    public void onEditIngredientSelected(Long itemId, int posicio) {
        Intent intent = new Intent(context, AfegirEditarIngredientActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(AfegirEditarIngredientActivity.ITEM_ID, itemId);
        intent.putExtra(AfegirEditarIngredientActivity.POSITION, posicio);
        startActivityForResult(intent, EDITAR_INGREDIENT_CODE);
    }

    // Implementació del mètode de l'interface LlistaIngredientsFragment.OnIngredientsSelectedListener que esborra un Ingredient
    public void onDeleteIngredientSelected(int posicio) {
        Ingredient ingredient = ingredients.get(posicio);
        ingredient.delete();
        ingredients.remove(posicio);
        fragmentIngredients.setIngredients(ingredients);
    }

    // Implementació del mètode de l'interface LlistaPasReceptaFragment.OnPasReceptaSelectedListener que inicia
    // l'activitat AfegirEditarPasReceptaActivity per editar el PasRecepta
    public void onEditPasReceptaSelected(Long itemId, int posicio) {
        Intent intent = new Intent(context, AfegirEditarPasReceptaActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(AfegirEditarPasReceptaActivity.ITEM_ID, itemId);
        intent.putExtra(AfegirEditarPasReceptaActivity.POSITION, posicio);
        startActivityForResult(intent, EDITAR_PAS_RECEPTA_CODE);
    }

    // Implementació del mètode de l'interface LlistaPasReceptaFragment.OnPasReceptaSelectedListener que esborra un PasRecepta
    public void onDeletePasReceptaSelected(int posicio) {
        PasRecepta pasRecepta = instruccions.get(posicio);
        pasRecepta.delete();
        instruccions.remove(posicio);
        fragmentInstruccions.setPassos(instruccions);
    }

    // Amaguem o mostrem al frame amb la llista d'ingredients
    private void toggleIngredientsVisibility(){
        if (frameIngredients.getVisibility() == View.GONE) {
            frameIngredients.setVisibility(View.VISIBLE);
            viewIngredientsTitol.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fletxa_obert,0,0,0);
        } else {
            frameIngredients.setVisibility(View.GONE);
            viewIngredientsTitol.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fletxa_tancat,0,0,0);
        }
    }

    // Mètode que modifica el drawable de les 5 estrelles (AppCompatImageButton) per a que siguin
    // plenes o buides segons la valoració de la recepta
    private void actualitzaEstrelles(float valoracio) {
        int valoracioEntera = Math.round(valoracio);
        for(int i = 0; i < 5; i++) {
            if (i<valoracioEntera) {
                stars.get(i).setImageResource(R.drawable.ic_star_filled);
            } else {
                stars.get(i).setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    // Establim la valoració de la recepta i actualitzem les estrelles.
    private void setValoracio(float valoracio) {
        recepta.setValoracio(valoracio);
        actualitzaEstrelles(valoracio);
    }

    /*
     *  Mètode que inicialitza o actualitza els parámetres de l'objecte Recepta a partir de l'input de l'usuari i l'emmagatzema, juntament amb els
     *  ingredients, passos i imatges corresponents a la base de dades. Perque una recepta es consideri completa ha de tenir un nom, un temps, unes persones
     *  i com a mínim un ingredient i un pas. Si no es considera completa s'emmagatzema com a esborrany.
     */
    private void salvaRecepta() {
        String nom = viewNom.getText().toString();
        String descripcio = viewDescripcio.getText().toString();
        String stringTemps = viewTemps.getText().toString();
        String stringPersones = viewPersones.getText().toString();
        recepta.setNom(nom);
        recepta.setDescripcio(descripcio);
        recepta.setDificultat((Recepta.Dificultat)viewDificultat.getSelectedItem());
        recepta.setCategoria((CategoriaRecepta)viewCategoria.getSelectedItem());

        if (!stringTemps.isEmpty()) {
            recepta.setTemps(Integer.valueOf(stringTemps));
        }

        if (!stringPersones.isEmpty()) {
            recepta.setPersones(Integer.valueOf(stringPersones));
        }
        if (!nom.isEmpty() && !stringPersones.isEmpty() && !stringTemps.isEmpty() && !stringPersones.equals("0") && !stringTemps.equals("0") && ingredients.size() > 0 && instruccions.size() > 0) {
            recepta.setEsborrany(false);
            Toast.makeText(context, "S'ha desat la recepta", Toast.LENGTH_SHORT).show();
        } else {
            recepta.setEsborrany(true);
            Toast.makeText(context, "S'ha desat la recepta com a esborrany", Toast.LENGTH_SHORT).show();
        }
        recepta.save();
        if (imatge != null) {
            imatge.setRecepta(recepta);
            imatge.setPrincipal(true);
            imatge.save();
        }
        for(Ingredient ingredient: ingredients) {
            ingredient.setRecepta(recepta);
            ingredient.save();
        }
        for(PasRecepta pas: instruccions) {
            pas.setRecepta(recepta);
            pas.save();
        }

    }

    /*
     *  Com que l'activitat implementa View.OnClickListener podem fer un Override del mètode onClick i gestionar tots els esdeveniments
     *  onClick des d'aqui
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.recepta_ingredients_titol:
                // Amaguem o mostrem la llista d'ingredients
                toggleIngredientsVisibility();
                break;
            case R.id.recepta_star1:
                setValoracio(1f);
                break;
            case R.id.recepta_star2:
                setValoracio(2f);
                break;
            case R.id.recepta_star3:
                setValoracio(3f);
                break;
            case R.id.recepta_star4:
                setValoracio(4f);
                break;
            case R.id.recepta_star5:
                setValoracio(5f);
                break;
            case R.id.recepta_imatge:
                /*
                 *  Quan l'usuari clica sobre el botó que mostra l'imatge del pas s'obre un AlertDialog que permet escollir si fer
                 *  una foto amb la càmera o seleccionar una imatge de la galeria
                 */
                CharSequence opcions[] = new CharSequence[]{"Fes una foto", "Ves a la galeria"};

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setItems(opcions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                //  Les imatges capturades amb la càmera s'emmagatzemen a la memòria externa de l'aplicació
                                // ja que així l'usuari en pot tenir accès però per això cal que
                                //  comprovem si tenim permis per llegir i escriure aquesta memòria externa.
                                permis = comprovaPermisEscriptura();
                                if(permis) {
                                    // Si tenim permis iniciem l'intent que llança la càmera
                                    iniciaCameraIntent();
                                }
                                break;
                            case 1:
                                //  Si l'usuari vol seleccionar una imatge de la galeria, creem un intent que crida el MediaStore amb l'acció
                                //  ACTION_PICK i ens permetrà fer servir la galeria del telèfon per seleccionar-ne una.
                                Intent intentGaleria = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intentGaleria, OBRE_GALERIA_CODE);
                                break;
                        }
                    }
                });
                builder.show();
                break;
            case R.id.boto_afegir_ingredients:
                // Iniciem l'activitat AfegirEditarIngredientActivity amb l'acció per inserir un Ingredient nou
                Intent intent = new Intent(context, AfegirEditarIngredientActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                startActivityForResult(intent, AFEGIR_INGREDIENT_CODE);
                break;
            case R.id.boto_afegir_pas:
                // Iniciem l'activitat AfegirEditarPasReceptaActivity amb l'acció per inserir un PasRecepta nou
                Intent intent2 = new Intent(context, AfegirEditarPasReceptaActivity.class);
                intent2.setAction(Intent.ACTION_INSERT);
                startActivityForResult(intent2, AFEGIR_PAS_RECEPTA_CODE);
                break;
            case R.id.boto_submit:
                //  Emmagatzamem la recepta i tanquem l'activitat
                salvaRecepta();
                finish();
                break;
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
                 *  També establim la nova Imatge com a principal i actualitzem l'imatge mostrada per a que es correspongui amb la nova.
                 */
                if(resultCode == RESULT_OK){
                    if(imatge != null) {
                        imatge.delete();
                    }
                    imatge = new Imatge(fileImatge);
                    imatge.setPrincipal(true);
                    Picasso.get().load(imatge.getPath()).fit().centerCrop().into(imatgeView);
                }

                break;
            case OBRE_GALERIA_CODE:
                /*
                 *  L'aplicació de la galeria ens retorna una Uri donada pel seu FileProvider que podem llegir però com que no sabem
                 *  quant de temps tindrem permís per accedir a aquesta imatge, la copiem a la nostra memòria interna mitjançant el mètode
                 *  salvaImatgedeUri.
                 *  Un cop tenim l'imatge emmagatzemada, creem un nou objecte Imatge a partir d'aquesta i l'emmagatzamem a la base de dades.
                 *  També establim la nova Imatge com a principal i actualitzem l'imatge mostrada per a que es correspongui amb la nova.
                 */
                if(resultCode == RESULT_OK){
                    if(imatge != null) {
                        imatge.delete();
                    }
                    Uri imatgeSeleccionada = data.getData();
                    File arxiu = Utils.salvaImatgeDeUri(context, imatgeSeleccionada);
                    imatge = new Imatge(arxiu);
                    imatge.setPrincipal(true);
                    Picasso.get().load(imatge.getPath()).fit().centerCrop().into(imatgeView);
                }
                break;
            case AFEGIR_INGREDIENT_CODE:
                /*
                 *  Si s'ha afegit correctament un Ingredient, el carreguem de la base de dades mitjançant el ID que ens han retornat, l'afegim
                 *  a l'array ingredients i actualitzem la llista d'ingredients passant-li al fragment.
                 */
                if(resultCode == RESULT_OK){
                    Ingredient ingredient = Ingredient.findById(Ingredient.class, data.getLongExtra("ingredientId", 0));
                    ingredients.add(ingredient);
                    fragmentIngredients.setIngredients(ingredients);
                }
                break;
            case EDITAR_INGREDIENT_CODE:
                /*
                 *  Si s'ha editat correctament un Ingredient i ens han retornat la posició dins l'array original d'ingrdients,
                 *  el carreguem de la base de dades mitjançant el ID que ens han retornat, l'afegim a l'array ingredients
                 *  substituïnt l'existent i actualitzem la llista d'ingredients passant-li al fragment.
                 */
                if(resultCode == RESULT_OK){
                    int position = data.getIntExtra("position", -1);
                    if (position != -1) {
                        Ingredient ingredient = Ingredient.findById(Ingredient.class, data.getLongExtra("ingredientId", 0));
                        ingredients.set(position, ingredient);
                        fragmentIngredients.setIngredients(ingredients);
                    }
                }
                break;
            case AFEGIR_PAS_RECEPTA_CODE:
                /*
                 *  Si s'ha afegit correctament un PasRecepta, el carreguem de la base de dades mitjançant el ID que ens han retornat, l'afegim
                 *  a l'array instruccions i actualitzem la llista de passos passant-li al fragment.
                 */
                if(resultCode == RESULT_OK){
                    PasRecepta pasRecepta = PasRecepta.findById(PasRecepta.class, data.getLongExtra("pasId", 0));
                    instruccions.add(pasRecepta);
                    fragmentInstruccions.setPassos(instruccions);
                }
                break;
            case EDITAR_PAS_RECEPTA_CODE:
                /*
                 *  Si s'ha editat correctament un PasRecepta i ens han retornat la posició dins l'array original d'instruccions,
                 *  el carreguem de la base de dades mitjançant el ID que ens han retornat, l'afegim a l'array instruccions
                 *  substituïnt l'existent i actualitzem la llista de passos passant-li al fragment.
                 */
                if(resultCode == RESULT_OK){
                    int position = data.getIntExtra("position", -1);
                    if (position != -1) {
                        PasRecepta pasRecepta = PasRecepta.findById(PasRecepta.class, data.getLongExtra("pasId", 0));
                        instruccions.set(position, pasRecepta);
                        fragmentInstruccions.setPassos(instruccions);
                    }
                }
                break;
        }
    }

    // Si l'usuari torna enrere emmagatzamem l'estat actual de la recepta (No hi ha l'opció de cancelar els canvis)
    @Override
    public void onBackPressed() {
        salvaRecepta();
        super.onBackPressed();
    }

    /*
     *  Mètode encarregat de comprovar si tenim permís per escriure a memòria externa. En cas contrari demanem aquest permís i el que
     *  ens permet llegir-la també.
     */
    private boolean comprovaPermisEscriptura() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_EXTERNAL_STORAGE);
            return false;
        } else {
            return true;
        }
    }

    /*
     *  Després del diàleg que permet a l'usuari concedir o denegar un permís es crida aquest mètode de l'Activity, on comprovem
     *  si ens han estat concedits els permisos i en cas afirmatiu iniciem l'intent que llança l'aplicació de la Càmera.
     *  Si l'usuari ens ha denegat el permis no fem res, ja el tornarem a demanar quan l'usuari vulgui fer una foto de nou.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_EXTERNAL_STORAGE: {
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
