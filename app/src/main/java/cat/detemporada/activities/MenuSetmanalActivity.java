package cat.detemporada.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mikepenz.materialdrawer.Drawer;

import cat.detemporada.R;
import cat.detemporada.fragments.LlistaItemMenuFragment;
import cat.detemporada.helpers.MenuDrawer;

/*
 *  MenuSetmanalActivity
 *
 *  Activitat que mostra els elements del menu per a tota la setmana
 *
 */

public class MenuSetmanalActivity extends AppCompatActivity implements LlistaItemMenuFragment.OnItemMenuSelectedListener {
    private Drawer drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_setmanal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context context = this.getApplicationContext();

        // Inicialitzem el calaix mitjançant el helper MenuDrawer
        drawer = MenuDrawer.createMenu(context, this, toolbar, 3, savedInstanceState);

        loadMenuSetmanal();

    }

    // Carreguem el LlistaItemMenuFragment, que és el veritable encarregat de mostrar els elements
    public void loadMenuSetmanal() {
        LlistaItemMenuFragment fragment = new LlistaItemMenuFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_items, fragment)
                .commit();
    }

    // Implementació del mètode de l'interface LlistaItemMenuFragment.OnItemMenuSelectedListener que inicia
    // l'activitat ReceptaActivity que mostra la recepta corresponent al ItemMenu
    public void onItemMenuSelected(Long id) {
        Intent intent = new Intent(this.getApplicationContext(), ReceptaActivity.class);
        intent.putExtra(ReceptaActivity.ITEM_ID, id);
        startActivity(intent);
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
}
