package cat.detemporada.activities;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cat.detemporada.R;
import cat.detemporada.fragments.LlistaReceptesFragment;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Producte;
import cat.detemporada.models.Temporada;

import static cat.detemporada.R.drawable.mes_en_temporada;

public class ProducteActivity extends AppCompatActivity implements LlistaReceptesFragment.OnReceptaSelectedListener {
    public final static String ITEM_ID = "itemId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producte);
        Toolbar toolbar = findViewById(R.id.detall_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intentEntrant = getIntent();
        if (intentEntrant != null) {
           if (intentEntrant.hasExtra(ITEM_ID)) {
               Long itemId = intentEntrant.getLongExtra(ITEM_ID, 0);
               if (itemId > 0) {
                   Producte producte = Producte.findById(Producte.class, itemId);
                   if(producte != null) {
                       Imatge imatge = producte.loadImatgePrincipal();
                       CollapsingToolbarLayout appBarLayout = this.findViewById(R.id.detall_toolbar_layout);
                       if (appBarLayout != null) {
                           appBarLayout.setTitle(producte.getNom());
                       }
                       ImageView imageHeader = this.findViewById(R.id.detall_imatge);
                       if (imageHeader != null) {
                           Picasso.get().load(imatge.getPath()).into(imageHeader);
                       }

                       List<Temporada> temporades = producte.loadTemporades();

                       TextView descripcio = findViewById(R.id.producte_descripcio);
                       descripcio.setText(producte.getDescripcio());

                       TableLayout taulaTemporades = findViewById(R.id.producte_temporada);
                       TextView temporadaTotLany = findViewById(R.id.producte_temporada_tot_lany);
                       TableRow fila = (TableRow) taulaTemporades.getChildAt(0);
                       for (int j = 0; j < 12; j++) {
                           for(Temporada temporada: temporades) {
                               if(temporada.isAtemporal()) {
                                    temporadaTotLany.setVisibility(View.VISIBLE);
                                    taulaTemporades.setVisibility(View.GONE);
                               } else {
                                   if (temporada.getInici() / 100 <= j + 1 && j + 1 <= temporada.getFi() / 100) {
                                       TextView mes = (TextView) fila.getChildAt(j);
                                       mes.setBackgroundResource(mes_en_temporada);
                                       break;
                                   }
                               }
                           }

                       }

                       carregaReceptesFragment(producte.getId());
                   }
               }
           }
        }
    }

    private void carregaReceptesFragment(Long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(LlistaReceptesFragment.PRODUCTE_ID, itemId);
        LlistaReceptesFragment fragment = new LlistaReceptesFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_receptes, fragment)
                .commit();
    }

    public void onReceptaSelected(Long itemId) {
        Intent intent = new Intent(this.getApplicationContext(), ReceptaActivity.class);
        intent.putExtra(ReceptaActivity.ITEM_ID, itemId);
        startActivity(intent);
    }
    public void onReceptaEsborranySelected(Long itemId) {}
    public void onEditReceptaSelected(Long itemId) {}


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
}
