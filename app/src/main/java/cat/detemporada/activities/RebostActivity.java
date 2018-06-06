package cat.detemporada.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;

import cat.detemporada.R;
import cat.detemporada.fragments.AfegirEditarItemLlistaFragment;
import cat.detemporada.fragments.LlistaRebostFragment;
import cat.detemporada.helpers.MenuDrawer;

public class RebostActivity extends AppCompatActivity implements LlistaRebostFragment.OnItemRebostSelectedListener {
    private Drawer drawer;
    private Context context;

    private static final int AFEGIR_ITEM_REBOST_CODE = 0;
    private static final int EDITAR_ITEM_REBOST_CODE = 1;

    private boolean pausat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_setmanal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this.getApplicationContext();
        drawer = MenuDrawer.createMenu(context, this, toolbar, 5, savedInstanceState);

        loadRebostFragment();

    }

    public void loadRebostFragment() {
        LlistaRebostFragment fragment = new LlistaRebostFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_items, fragment)
                .commit();
    }

    public void onRebostItemSelected(Long itemRebostId) {

    }

    public void onEditRebostItemSelected(Long itemRebostId) {

        Intent intent = new Intent(context, AfegirEditarItemLlistaActivity.class);
        intent.putExtra(AfegirEditarItemLlistaActivity.ITEM_ID, itemRebostId);
        intent.putExtra(AfegirEditarItemLlistaActivity.ITEM_TIPUS, AfegirEditarItemLlistaFragment.TIPUS_ITEM_REBOST);
        intent.setAction(Intent.ACTION_EDIT);
        startActivityForResult(intent, EDITAR_ITEM_REBOST_CODE);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(context, AfegirEditarItemLlistaActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(AfegirEditarItemLlistaActivity.ITEM_TIPUS, AfegirEditarItemLlistaFragment.TIPUS_ITEM_REBOST);
                startActivityForResult(intent, AFEGIR_ITEM_REBOST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AFEGIR_ITEM_REBOST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(context, "S'ha afegit un element al rebost", Toast.LENGTH_SHORT).show();
                loadRebostFragment();
            }
        } else if (requestCode == EDITAR_ITEM_REBOST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(context, "S'ha editat l'element del rebost", Toast.LENGTH_SHORT).show();
                loadRebostFragment();
            }
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
            loadRebostFragment();
            pausat = false;
        }
    }
}
