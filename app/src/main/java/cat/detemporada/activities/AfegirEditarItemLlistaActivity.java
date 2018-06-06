package cat.detemporada.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import cat.detemporada.R;
import cat.detemporada.fragments.AfegirEditarItemLlistaFragment;
import cat.detemporada.fragments.LlistaProductesFragment;

/*
 *  AfegirEditarItemLlistaActivity
 *
 *  Activitat que permet afegir o editar un element a la llista de la compra o al rebost
 */

public class AfegirEditarItemLlistaActivity extends AppCompatActivity implements LlistaProductesFragment.OnProducteSelectedListener, AfegirEditarItemLlistaFragment.OnItemRebostAfegitListener {
    private boolean productSelected = false;
    private Toolbar toolbar;

    public final static String ITEM_ID = "itemId";
    public final static String ITEM_TIPUS = "tipus";

    private String tipus;

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
         *  Recollim el Intent que ha llançat l'activitat i comprovem si volem afegir o editar un element i de quin tipus es tracta
         *  En cas que l'acció sigui d'editar, ens assegurem que ens han passat l'ID de l'element.
         *  En tots els casos contraris tanquem l'activitat.
         */

        Intent intentEntrant = getIntent();
        if (intentEntrant != null && intentEntrant.getAction() != null) {
            if (intentEntrant.hasExtra(ITEM_TIPUS)) {
                tipus = intentEntrant.getStringExtra(ITEM_TIPUS);
            } else {
                // Si no sabem que volem afegir o editar (ItemLlistaCompra o ItemRebost) tanquem l'activitat
                finish();
            }
            if (intentEntrant.getAction().equalsIgnoreCase(Intent.ACTION_EDIT)) {
                if (intentEntrant.hasExtra(ITEM_ID)) {
                    Long itemId = intentEntrant.getLongExtra(ITEM_ID, 0);
                    if (itemId > 0) {
                        editItem(itemId);
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            } else if (intentEntrant.getAction().equalsIgnoreCase(Intent.ACTION_INSERT)) {
                // Si volem afegir un element el primer que cal fer és escollir un producte de la llista pel que carreguem el fragment
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
    *  Iniciem el fragement AfegirEditarItemLlistaFragment i li passem el ID del producte seleccionat.
    */
    public void onProducteSelected(Long producteId) {
        productSelected = true;
        getSupportActionBar().setTitle(R.string.afegir_element);
        Bundle arguments = new Bundle();
        arguments.putLong(AfegirEditarItemLlistaFragment.ARG_PRODUCTE_ID, producteId);
        arguments.putString(AfegirEditarItemLlistaFragment.ARG_TIPUS, tipus);
        AfegirEditarItemLlistaFragment fragment = new AfegirEditarItemLlistaFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_afegir, fragment)
                .commit();
    }

    /*
     *  Implementació del mètode de l'interface AfegirEditarItemLlistaFragment.OnItemRebostAfegitListener que
     *  tanca l'activitat i retorna el resultat OK.
     */
    public void itemAfegitOEditat() {
        Intent intent = new Intent();
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

    // Si estem editant un element, carreguem directament el AfegirEditarItemLlistaFragment passant-li el ID del producte
    // associat a l'element que estem editant i de quin tipus d'element es tracta (ItemLlistaCompra o ItemRebost)
    private void editItem(Long itemId) {
        getSupportActionBar().setTitle(R.string.editar_element);
        Bundle arguments = new Bundle();
        arguments.putLong(AfegirEditarItemLlistaFragment.ARG_ITEM_ID, itemId);
        arguments.putString(AfegirEditarItemLlistaFragment.ARG_TIPUS, tipus);
        AfegirEditarItemLlistaFragment fragment = new AfegirEditarItemLlistaFragment();
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
