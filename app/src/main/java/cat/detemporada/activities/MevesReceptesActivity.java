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
import cat.detemporada.fragments.LlistaReceptesFragment;
import cat.detemporada.helpers.MenuDrawer;

public class MevesReceptesActivity extends AppCompatActivity implements LlistaReceptesFragment.OnReceptaSelectedListener {
    private Drawer drawer;
    Toolbar toolbar;
    Context context;
    TabLayout tabLayout;

    private static final int AFEGIR_RECEPTA_CODE = 0;
    private static final int EDITAR_RECEPTA_CODE = 1;

    private LlistaReceptesFragment tab1 = new LlistaReceptesFragment();
    private LlistaReceptesFragment tab2 = new LlistaReceptesFragment();

    private boolean pausat = false;
    private int tabSelectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llista_compra);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this.getApplicationContext();
        drawer = MenuDrawer.createMenu(this.getApplicationContext(), this, toolbar, 7, savedInstanceState);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Meves"));
        tabLayout.addTab(tabLayout.newTab().setText("Preferides"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSelectedPosition = tab.getPosition();
                viewPager.setCurrentItem(tabSelectedPosition);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(context, AfegirEditarReceptaActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                startActivityForResult(intent, AFEGIR_RECEPTA_CODE);
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

        if (requestCode == AFEGIR_RECEPTA_CODE) {
            if (resultCode == RESULT_OK) {
                tab1.actualitzaLlista();
                Toast.makeText(context, "S'ha afegit la recepta", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == EDITAR_RECEPTA_CODE) {
            if (resultCode == RESULT_OK) {
                tab1.actualitzaLlista();
                Toast.makeText(context, "S'ha editat la recepta", Toast.LENGTH_SHORT).show();

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
                    Bundle arguments = new Bundle();
                    arguments.putBoolean(LlistaReceptesFragment.USUARI_ARG, true);
                    tab1.setArguments(arguments);
                    return tab1;
                case 1:
                    Bundle arguments2 = new Bundle();
                    arguments2.putBoolean(LlistaReceptesFragment.PREFERIDA_ARG, true);
                    tab2.setArguments(arguments2);
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


    public void onReceptaSelected(Long itemId) {
        Intent intent = new Intent(this.getApplicationContext(), ReceptaActivity.class);
        intent.putExtra(ReceptaActivity.ITEM_ID, itemId);
        startActivity(intent);
    }


    public void onReceptaEsborranySelected(Long itemId) {
        Intent intent = new Intent(this.getApplicationContext(), AfegirEditarReceptaActivity.class);
        intent.putExtra(AfegirEditarReceptaActivity.ITEM_ID, itemId);
        intent.setAction(Intent.ACTION_EDIT);
        startActivity(intent);
    }

    public void onEditReceptaSelected(Long itemId) {
        onReceptaEsborranySelected(itemId);
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
