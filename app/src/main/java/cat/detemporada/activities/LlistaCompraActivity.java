package cat.detemporada.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;

import cat.detemporada.R;
import cat.detemporada.fragments.AfegirEditarItemLlistaFragment;
import cat.detemporada.fragments.LlistaCompraFragment;
import cat.detemporada.fragments.LlistaCompraReceptesFragment;
import cat.detemporada.helpers.MenuDrawer;

/*
 *  LlistaCompraActivity
 *
 *  Activitat que mostra els elements de la llista de la compra i les receptes associades
 *  a aquesta en dues tabs
 *
 */


public class LlistaCompraActivity extends AppCompatActivity implements LlistaCompraFragment.OnItemLlistaCompraSelectedListener {
    private Drawer drawer;
    private Context context;

    private static final int AFEGIR_ITEM_LLISTA_COMPRA_CODE = 0;
    private static final int EDITAR_ITEM_LLISTA_COMPRA_CODE = 1;

    private LlistaCompraFragment tab1;
    private LlistaCompraReceptesFragment tab2;
    private int tabSelectedPosition = 0;
    private boolean pausat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llista_compra);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this.getApplicationContext();

        // Inicialitzem el calaix mitjançant el helper MenuDrawer
        drawer = MenuDrawer.createMenu(this.getApplicationContext(), this, toolbar, 4, savedInstanceState);

        // Inicialitzem el nostre tabLayout amb les dues pestanyes: Llista i Receptes
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Llista"));
        tabLayout.addTab(tabLayout.newTab().setText("Receptes"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // El TabLayout mostra dos Fragments gestionats per un PagerAdapter
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Quan seleccionem una pestanya canviem a l'element corresponent del viewPager
                tabSelectedPosition = tab.getPosition();
                viewPager.setCurrentItem(tabSelectedPosition);
                // Quan seleccionem la pestanya Receptes, actualitzem la llista per a que mostri els elements adquirits correctament
                if(tabSelectedPosition == 1) {
                    tab2.actualitzaLlista();
                }
                // Com que a la pestanya Receptes no volem mostrar el botó + al Toolbar invalidem el menu
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    // Implementació del mètode de l'interface LlistaCompraFragment.OnItemLlistaCompraSelectedListener que inicia
    // l'activitat AfegirEditarItemLlistaActivity per editar l'element especificant que es tracta d'un ItemLlistaCompra
    public void onEditItemLlistaCompraSelected(Long itemId) {
        Intent intent = new Intent(context, AfegirEditarItemLlistaActivity.class);
        intent.putExtra(AfegirEditarItemLlistaActivity.ITEM_ID, itemId);
        intent.putExtra(AfegirEditarItemLlistaActivity.ITEM_TIPUS, AfegirEditarItemLlistaFragment.TIPUS_ITEM_LLISTA_COMPRA);
        intent.setAction(Intent.ACTION_EDIT);
        startActivityForResult(intent, EDITAR_ITEM_LLISTA_COMPRA_CODE);
    }


    // Si som a la pestanya "Receptes" no mostrém el botó + del menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.add);
        if(tabSelectedPosition == 0) {
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }
        return true;
    }

    //  Si l'usuari pitja el botó + del menu iniciem l'activitat AfegirEditarItemLlistaActivity especificant que es tracta
    //  d'un element de tipus ItemLlistaCompra
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(context, AfegirEditarItemLlistaActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(AfegirEditarItemLlistaActivity.ITEM_TIPUS, AfegirEditarItemLlistaFragment.TIPUS_ITEM_LLISTA_COMPRA);
                startActivityForResult(intent, AFEGIR_ITEM_LLISTA_COMPRA_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Si el Drawer està obert el tanquem i sino executem mètode original
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    //  Quan l'activitat AfegirEditarItemLlistaActivity que hem iniciat retorna un resultat positiu actualitzem
    //  les llistes cridant els mètodes corresponents.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AFEGIR_ITEM_LLISTA_COMPRA_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(context, "S'ha afegit un element a la llista", Toast.LENGTH_SHORT).show();
                tab1.actualitzaLlista();
                tab2.actualitzaLlista();
            }
        } else if (requestCode == EDITAR_ITEM_LLISTA_COMPRA_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(context, "S'ha editat l'element de la llista", Toast.LENGTH_SHORT).show();
                tab1.actualitzaLlista();
                tab2.actualitzaLlista();
            }
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    tab1 = new LlistaCompraFragment();
                    return tab1;
                case 1:
                    tab2 = new LlistaCompraReceptesFragment();
                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausat = true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(pausat) {
            tab1.actualitzaLlista();
            tab2.actualitzaLlista();
            pausat = false;
        }
    }
}
