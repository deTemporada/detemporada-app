package cat.detemporada.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import cat.detemporada.R;
import cat.detemporada.fragments.AfegirEditarIngredientFragment;
import cat.detemporada.fragments.LlistaProductesFragment;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.Producte;
import cat.detemporada.models.Unitat;

/*
 *  AfegirEditarIngredientActivity
 *
 *  Activitat que permet afegir o editar un ingredient a la base de dades
 */

public class AfegirEditarIngredientActivity extends AppCompatActivity implements LlistaProductesFragment.OnProducteSelectedListener, AfegirEditarIngredientFragment.OnIngredientAfegitOEditatListener {
    private boolean productSelected = false;
    private Toolbar toolbar;

    public final static String ITEM_ID = "itemId";
    public final static String POSITION = "position";

    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afegir_item_llista);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Mostrem la fletxa per tornar enrere
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        /*
         *  Recollim el Intent que ha llançat l'activitat i comprovem si volem afegir o editar un Ingredient
         *  En cas que l'acció sigui d'editar, ens assegurem que ens han passat l'ID de l'ingredient i la posició
         *  dins l'array d'ingredients de la recepta.
         *  En tots els casos contraris tanquem l'activitat.
         */
        Intent intentEntrant = getIntent();
        if (intentEntrant != null && intentEntrant.getAction() != null) {
            if (intentEntrant.getAction().equalsIgnoreCase(Intent.ACTION_EDIT)) {
                if (intentEntrant.hasExtra(ITEM_ID) && intentEntrant.hasExtra(POSITION)) {
                    Long itemId = intentEntrant.getLongExtra(ITEM_ID, 0);
                    position = intentEntrant.getIntExtra(POSITION, -1);
                    if (itemId > 0 && position != -1) {
                        editItem(itemId);
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            } else if (intentEntrant.getAction().equalsIgnoreCase(Intent.ACTION_INSERT)) {
                // Si volem afegir un ingredient el primer que cal fer és escollir un producte de la llista pel que carreguem el fragment
                loadProducteList();
            } else  {
                finish();
            }
        } else {
            finish();
        }

    }

    /*
     *  Implementació del mètode de l'interface LlistaProductesFragment.OnProducteSelectedListener, cridat pel fragment
     *  LlistaProductesFragment quan es selecciona un producte de la llista.
     *  Iniciem el fragement AfegirEditarIngredientFragment i li passem el ID del producte seleccionat.
     */
    public void onProducteSelected(Long producteId) {
        productSelected = true;
        getSupportActionBar().setTitle(R.string.afegir_element);
        Bundle arguments = new Bundle();
        arguments.putLong(AfegirEditarIngredientFragment.ARG_PRODUCTE_ID, producteId);
        AfegirEditarIngredientFragment fragment = new AfegirEditarIngredientFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_afegir, fragment)
                .commit();
    }

    /*
     *  Implementació del mètode de l'interface AfegirEditarIngredientFragment.OnIngredientAfegitOEditatListener, cridat pel fragment
     *  AfegirEditarIngredientFragment quan es pitja el botó R.id.boto_submit.
     *  S'encarrega d'emmagatzemar l'ingredient a la base de dades i finalitzar l'activitat retornant el id i la posició (si s'escau) de
     *  l'ingredient.
     */
    public void itemAfegitOEditat(Long ingredientId, Long producteId, float quantitat, Unitat unitat, String nom) {
        Ingredient ingredient;

        // Si l'id de l'ingredient és 0 vol dir que cal crear-ne un de nou (ACTION_INSERT)
        if (ingredientId == 0L) {
            ingredient = new Ingredient();
        } else {
            ingredient = Ingredient.findById(Ingredient.class, ingredientId);
        }

        // Establim els parámetres de l'ingredient
        if (producteId != null) {
            ingredient.setProducte(Producte.findById(Producte.class, producteId));
        }
        ingredient.setQuantitat(quantitat);
        ingredient.setUnitat(unitat);
        ingredient.setNom(nom);
        ingredient.save();

        // Creem un intent, afegim els paràmetres i acabem l'activitat
        Intent intent = new Intent();
        intent.putExtra("ingredientId", ingredient.getId());
        if (position != -1) {
            intent.putExtra("position", position);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    // Mètode que carrega el Fragment amb la llista de productes
    private void loadProducteList() {
        productSelected = false;
        toolbar.setTitle(R.string.tria_producte);
        Bundle arguments = new Bundle();
        arguments.putBoolean(LlistaProductesFragment.ARG_PARAM2, true);
        LlistaProductesFragment fragment = new LlistaProductesFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_afegir, fragment)
                .commit();
    }

    // Si estem editant un Ingredient, carreguem directament el AfegirEditarIngredientFragment passant-li el ID del producte
    // associat a l'ingredient que estem editant
    private void editItem(Long producteId) {
        getSupportActionBar().setTitle(R.string.editar_element);
        Bundle arguments = new Bundle();
        arguments.putLong(AfegirEditarIngredientFragment.ARG_ITEM_ID, producteId);
        AfegirEditarIngredientFragment fragment = new AfegirEditarIngredientFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_afegir, fragment)
                .commit();
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
    public void onBackPressed() {
        // Si ja hem seleccionat un producte i pitjem enrere tornem a mostrar el fragment per escollir producte
        if (productSelected) {
            loadProducteList();
        } else {
            super.onBackPressed();
        }
    }
}
