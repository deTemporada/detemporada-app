package cat.detemporada.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;


import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.fragments.LlistaItemMenuFragment;
import cat.detemporada.fragments.LlistaProductesFragment;
import cat.detemporada.fragments.ResultatsCercaFragment;
import cat.detemporada.fragments.ResultatsClarifaiFragment;
import cat.detemporada.helpers.MenuDrawer;
import cat.detemporada.helpers.Utils;
import cat.detemporada.models.CategoriaRecepta;
import cat.detemporada.models.FilterDrawerItem;
import cat.detemporada.models.Producte;
import cat.detemporada.models.ResultatClarifai;

/*
 *  MainActivity
 *
 *  Activitat principal que mostra el menu del dia i els productes de temporada actualment
 *  a més d'integrara la funció de cerca
 *
 */

public class MainActivity extends AppCompatActivity implements LlistaProductesFragment.OnProducteSelectedListener, LlistaItemMenuFragment.OnItemMenuSelectedListener, ResultatsCercaFragment.OnResultatsCercaSelectedListener, ResultatsClarifaiFragment.OnResultatsClarifaiAcceptedListener {
    private Drawer drawer;
    private Drawer drawerFiltres;
    private String TAG = "MainActivity";
    private static final int OBRE_CAMERA_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE = 1;
    private boolean permis = false;

    private List<Producte> productesFiltreInclosos = new ArrayList<>();
    private List<Producte> productesFiltreExclosos = new ArrayList<>();
    private List<Producte> productesFiltreResta = new ArrayList<>();
    private List<CategoriaRecepta> categoriesFiltre = new ArrayList<>();

    private boolean cercant = false;
    private boolean cercantClarifai = false;
    private ResultatsCercaFragment resultatsCercaFragment;
    private InputMethodManager imm;
    private SearchView searchView;
    private MenuItem filtreMenuItem;
    private String queryAnterior = "";

    private String TAG_PRODUCTES_EXCLOSOS = "Exclosos";
    private String TAG_PRODUCTES_INCLOSOS = "Inclosos";
    private String TAG_PRODUCTES_RESTA = "Resta";

    private Context context;
    private File fileImatge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Emmagatzemem el context per iniciar els intents
        context = this.getApplicationContext();

        // Inicialitzem el nostre InputMethodManager per controlar quan es mostra/oculta el teclat virtual
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Inicialitzem el calaix mitjançant el helper MenuDrawer
        drawer = MenuDrawer.createMenu(this.getApplicationContext(), this, toolbar, 1, savedInstanceState);

        // Creem un altre calaix per els filtres de cerca que es mostra a la dreta de la pantalla però que no es pot obrir
        // si no s'ha clicat sobre la lent d'engradiment (i per tant s'inicia una cerca)
        drawerFiltres = new DrawerBuilder()
                .withActivity(this)
                .withSavedInstance(savedInstanceState)
                .withDrawerGravity(GravityCompat.END)
                .withStickyFooterDivider(false)
                .withStickyFooterShadow(false)
                .withFooterClickable(true)
                .withStickyFooter(R.layout.filter_drawer_footer)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        /*
                         *  Consumim el clic de l'element del calaix per a que aquest no es tanqui quan
                         *  l'usuari clica sobre un filtre.
                         */
                        return true;
                    }
                })
                .append(drawer);
        drawerFiltres.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        afegeixItemsDrawerFiltre();

        Button botoAplica = findViewById(R.id.filter_drawer_boto_aplica);
        botoAplica.setOnClickListener(clickListener);
        Button botoEsborra = findViewById(R.id.filter_drawer_boto_esborra);
        botoEsborra.setOnClickListener(clickListener);

        //  Comprovem si s'ha iniciat l'activitat a partir d'un intent amb l'acció ACTION_SEARCH i en cas
        //  afirmatiu iniciem la cerca
        Intent intentEntrant = getIntent();
        if (intentEntrant != null && intentEntrant.getAction() != null) {
            if (intentEntrant.getAction().equalsIgnoreCase(Intent.ACTION_SEARCH)) {
                toggleCerca(true);
            }
        }

        // Generem els nous menus per als propers dies si cal
        Utils.generaNousMenus();

        // Carreguem els fragments de la vista dins els corresponents FrameLayout
        carregaFrameItemMenu();
        carregaFrameTemporades();
        carregaFrameCerca();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawer.saveInstanceState(outState);
        //add the values which need to be saved from the apppended drawer to the bundle
        outState = drawerFiltres.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    // Mètode que inicia un LlistaItemMenuFragment passant-li la data d'avui com a paràmetre
    private void carregaFrameItemMenu() {
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Bundle arguments = new Bundle();
        arguments.putString(LlistaItemMenuFragment.ARG_PARAM1, s.format(new Date()));
        LlistaItemMenuFragment fragment = new LlistaItemMenuFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_menu_dia, fragment)
                .commit();
    }

    // Mètode que inicia un LlistaProductesFragment passant-li la data d'avui com a paràmetre
    private void carregaFrameTemporades() {
        SimpleDateFormat s = new SimpleDateFormat("MMdd", Locale.getDefault());
        Bundle arguments = new Bundle();
        arguments.putString(LlistaProductesFragment.ARG_PARAM1, s.format(new Date()));
        LlistaProductesFragment fragment = new LlistaProductesFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_productes_temporada, fragment)
                .commit();
    }

    //  Mètode que inicia el Fragment amb els resultats de la cerca per a que estigui llest per
    //  quan l'usuari vulgui fer una cerca.
    private void carregaFrameCerca() {
        resultatsCercaFragment = new ResultatsCercaFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.resultats_cerca, resultatsCercaFragment)
                .commit();
    }

    // Implementació del mètode de l'interface LlistaProductesFragment.OnProducteSelectedListener que inicia
    // l'activitat ProducteActivity que mostra informació sobre el producte corresponent
    public void onProducteSelected(Long id) {
        Intent intent = new Intent(this.getApplicationContext(), ProducteActivity.class);
        intent.putExtra(ProducteActivity.ITEM_ID, id);
        startActivity(intent);

    }

    // Implementació del mètode de l'interface LlistaItemMenuFragment.OnItemMenuSelectedListener que inicia
    // l'activitat ReceptaActivity que mostra la recepta corresponent al ItemMenu
    public void onItemMenuSelected(Long id) {
        Intent intent = new Intent(this.getApplicationContext(), ReceptaActivity.class);
        intent.putExtra(ReceptaActivity.ITEM_ID, id);
        startActivity(intent);
    }

    // Implementació del mètode de l'interface ResultatsCercaFragment.OnResultatsCercaSelectedListener que inicia
    // l'activitat ReceptaActivity per mostrar la recepta seleccionada (cridem ItemMenuSelected perque
    // ja s'encarrega d'iniciar una ReceptaActivity i així ens estalviem codi)
    public void onResultatSelected(Long id) {
        onItemMenuSelected(id);
    }

    public void carregaProductesFiltre(List<Producte> productes) {
        for(Producte producte: productes) {
            if(!productesFiltreInclosos.contains(producte)  && !productesFiltreExclosos.contains(producte) && !productesFiltreResta.contains(producte)) {
                productesFiltreResta.add(producte);
            }
        }
        Collections.sort(productesFiltreResta);
        afegeixItemsDrawerFiltre();
    }

    /*
     *  Aquest mètode és l'encarregat d'afegir els items al calaix dels filtres
     */
    private void afegeixItemsDrawerFiltre() {
        drawerFiltres.removeAllItems();
        SectionDrawerItem sectionHeaderCategories = new SectionDrawerItem().withDivider(false).withName("Filtra les receptes per categoria:");
        drawerFiltres.addItem(sectionHeaderCategories);
        for(CategoriaRecepta categoria: CategoriaRecepta.values()) {
            SwitchDrawerItem item = new SwitchDrawerItem().withName(categoria.toString()).withTag(categoria).withChecked(categoriesFiltre.contains(categoria)).withSelectable(false).withOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        categoriesFiltre.add((CategoriaRecepta) drawerItem.getTag());
                    } else {
                        categoriesFiltre.remove(drawerItem.getTag());
                    }
                }
            });
            drawerFiltres.addItem(item);
        }

        if(!productesFiltreInclosos.isEmpty() || !productesFiltreExclosos.isEmpty() || !productesFiltreResta.isEmpty()) {
            SectionDrawerItem sectionHeaderIngredients = new SectionDrawerItem().withDivider(true).withName("Filtra les receptes per ingredient:");
            drawerFiltres.addItem(sectionHeaderIngredients);

            SectionDrawerItem sectionInclosos = new SectionDrawerItem().withDivider(true).withName("Inclosos").withTag(TAG_PRODUCTES_INCLOSOS);
            drawerFiltres.addItem(sectionInclosos);
            for (Producte producte : productesFiltreInclosos) {
                FilterDrawerItem item = new FilterDrawerItem().withName(producte.getNom()).withTag(producte).withSelectable(false).withOnClickListener(clickListener).withInclos(true).withEnabled(false).withDisabledTextColor(getResources().getColor(R.color.negre));
                drawerFiltres.addItem(item);
            }

            SectionDrawerItem sectionExclosos = new SectionDrawerItem().withDivider(true).withName("Exclosos").withTag(TAG_PRODUCTES_EXCLOSOS);
            drawerFiltres.addItem(sectionExclosos);
            for (Producte producte : productesFiltreExclosos) {
                FilterDrawerItem item = new FilterDrawerItem().withName(producte.getNom()).withTag(producte).withSelectable(false).withOnClickListener(clickListener).withExclos(true).withEnabled(false).withDisabledTextColor(getResources().getColor(R.color.negre));
                drawerFiltres.addItem(item);
            }

            SectionDrawerItem sectionResta = new SectionDrawerItem().withDivider(true).withName("Inclou/Exclou ingredients:").withTag(TAG_PRODUCTES_RESTA);
            drawerFiltres.addItem(sectionResta);
            for (Producte producte : productesFiltreResta) {
                FilterDrawerItem item = new FilterDrawerItem().withName(producte.getNom()).withTag(producte).withSelectable(false).withOnClickListener(clickListener).withEnabled(false).withDisabledTextColor(getResources().getColor(R.color.negre));
                drawerFiltres.addItem(item);
            }
        }
    }

    // Implementació del OnClickListener per escoltar els clics als botons del calaix de filtres
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            Producte producte;
            boolean inclos;
            boolean exclos;
            FilterDrawerItem filterDrawerItem;
            SectionDrawerItem sectionPrecedent = (SectionDrawerItem) drawerFiltres.getDrawerItem(TAG_PRODUCTES_RESTA);

            switch (id) {
                case R.id.filter_drawer_boto_inclou:
                    producte = (Producte) view.getTag();

                    filterDrawerItem = (FilterDrawerItem) drawerFiltres.getDrawerItem(producte);
                    drawerFiltres.removeItem(filterDrawerItem.getIdentifier());

                    inclos = (boolean) view.getTag(R.id.inclos);
                    exclos = (boolean) view.getTag(R.id.exclos);
                    if(!inclos && !exclos) {
                        int index = productesFiltreResta.indexOf(producte);
                        if(index != -1) {
                            productesFiltreInclosos.add(productesFiltreResta.get(index));
                            productesFiltreResta.remove(index);
                            filterDrawerItem.withInclos(true);
                            sectionPrecedent = (SectionDrawerItem) drawerFiltres.getDrawerItem(TAG_PRODUCTES_INCLOSOS);
                        }
                    } else if(!inclos) {
                        int index = productesFiltreExclosos.indexOf(producte);
                        if(index != -1) {
                            productesFiltreInclosos.add(productesFiltreExclosos.get(index));
                            productesFiltreExclosos.remove(index);
                            filterDrawerItem.withInclos(true).withExclos(false);
                            sectionPrecedent = (SectionDrawerItem) drawerFiltres.getDrawerItem(TAG_PRODUCTES_INCLOSOS);
                        }
                    } else if(!exclos) {
                        int index = productesFiltreInclosos.indexOf(producte);
                        if(index != -1) {
                            productesFiltreResta.add(productesFiltreInclosos.get(index));
                            productesFiltreInclosos.remove(index);
                            filterDrawerItem.withInclos(false);
                        }
                    }

                    drawerFiltres.addItemAtPosition(filterDrawerItem, drawerFiltres.getPosition(sectionPrecedent)+1);
                    break;
                case R.id.filter_drawer_boto_exclou:
                    producte = (Producte) view.getTag();

                    filterDrawerItem = (FilterDrawerItem) drawerFiltres.getDrawerItem(producte);
                    drawerFiltres.removeItem(filterDrawerItem.getIdentifier());

                    inclos = (boolean) view.getTag(R.id.inclos);
                    exclos = (boolean) view.getTag(R.id.exclos);
                    if(!inclos && !exclos) {
                        int index = productesFiltreResta.indexOf(producte);
                        if(index != -1) {
                            productesFiltreExclosos.add(productesFiltreResta.get(index));
                            productesFiltreResta.remove(index);
                            filterDrawerItem.withExclos(true);
                            sectionPrecedent = (SectionDrawerItem) drawerFiltres.getDrawerItem(TAG_PRODUCTES_EXCLOSOS);
                        }
                    } else if(!inclos) {
                        int index = productesFiltreExclosos.indexOf(producte);
                        if(index != -1) {
                            productesFiltreResta.add(productesFiltreExclosos.get(index));
                            productesFiltreExclosos.remove(index);
                            filterDrawerItem.withExclos(false);
                        }
                    } else if(!exclos) {
                        int index = productesFiltreInclosos.indexOf(producte);
                        if(index != -1) {
                            productesFiltreExclosos.add(productesFiltreInclosos.get(index));
                            productesFiltreInclosos.remove(index);
                            filterDrawerItem.withExclos(true).withInclos(false);
                            sectionPrecedent = (SectionDrawerItem) drawerFiltres.getDrawerItem(TAG_PRODUCTES_EXCLOSOS);
                        }
                    }

                    drawerFiltres.addItemAtPosition(filterDrawerItem, drawerFiltres.getPosition(sectionPrecedent)+1);
                    break;
                case R.id.filter_drawer_boto_aplica:
                    drawerFiltres.closeDrawer();
                    actualitzaResultatsAmbFiltre();
                    break;
                case R.id.filter_drawer_boto_esborra:
                    drawerFiltres.closeDrawer();
                    esborraFiltres();
                    actualitzaResultatsAmbFiltre();
                    afegeixItemsDrawerFiltre();
                    break;
            }
        }
    };

    private void esborraFiltres() {
        categoriesFiltre = new ArrayList<>();
        productesFiltreInclosos = new ArrayList<>();
        productesFiltreExclosos = new ArrayList<>();
    }

    /*
     *  Aquest és el métode que s'encarrega de crear la query SQL per a la cerca i cridar el métode carregaResultats del
     *  fragment ResultatsCercaFragment. A la query mateixa es tenen en compte tots els filtres per tal que retorni directament
     *  els resultats que volem sense haver-los de processar després.
     */
    private void actualitzaResultatsAmbFiltre() {
        if (productesFiltreInclosos.isEmpty() && productesFiltreExclosos.isEmpty() && categoriesFiltre.isEmpty()) {
            filtreMenuItem.setIcon(R.drawable.ic_filter_empty);
        } else {
            filtreMenuItem.setIcon(R.drawable.ic_filter_filled);
        }

        String query = searchView.getQuery().toString();
        resultatsCercaFragment.queryFiltreIngredients = "select i.id, i.producte, i.recepta from ingredient i";
        int indexInclosos = 1;

        for (Producte producte: productesFiltreInclosos) {
            resultatsCercaFragment.queryFiltreIngredients = resultatsCercaFragment.queryFiltreIngredients.concat(String.format(Locale.getDefault()," join ingredient i%d on i.recepta = i%d.recepta and i%d.producte = %d", indexInclosos, indexInclosos, indexInclosos, producte.getId().intValue()));
            indexInclosos++;
        }

        resultatsCercaFragment.queryFiltreIngredients = resultatsCercaFragment.queryFiltreIngredients.concat(" where");
        boolean calAnd = false;
        if(!productesFiltreExclosos.isEmpty()) {
            String llistaProductes = "(".concat(TextUtils.join(", ", productesFiltreExclosos)).concat(")");
            resultatsCercaFragment.queryFiltreIngredients = resultatsCercaFragment.queryFiltreIngredients.concat(String.format(Locale.getDefault(), " i.recepta not in (select recepta from ingredient where producte in %s)", llistaProductes));
            calAnd = true;
        }

        if(!categoriesFiltre.isEmpty()) {
            if (calAnd) resultatsCercaFragment.queryFiltreIngredients = resultatsCercaFragment.queryFiltreIngredients.concat(" and");
            Iterator<CategoriaRecepta> categoriaReceptaIterator = categoriesFiltre.iterator();
            String llistaCategories = "(\"".concat(categoriaReceptaIterator.next().toString().toUpperCase());
            while (categoriaReceptaIterator.hasNext()) {
                llistaCategories = llistaCategories.concat("\", \"");
                llistaCategories = llistaCategories.concat(categoriaReceptaIterator.next().toString().toUpperCase());
            }
            llistaCategories = llistaCategories.concat("\")");
            resultatsCercaFragment.queryFiltreIngredients = resultatsCercaFragment.queryFiltreIngredients.concat(String.format(Locale.getDefault(), " i.recepta in (select id from recepta where categoria in %s collate nocase)", llistaCategories));
            calAnd = true;
        }
        if (calAnd) resultatsCercaFragment.queryFiltreIngredients = resultatsCercaFragment.queryFiltreIngredients.concat(" and");
        resultatsCercaFragment.queryFiltreIngredients = resultatsCercaFragment.queryFiltreIngredients.concat(" (i.nom_normalitzat like ? or i.recepta in (select id from recepta where nom like ? or descripcio like ?)) order by i.nom");
        resultatsCercaFragment.carregaResultats(query);
    }

    // Mètode que mostra el Fragment amb els resultats de la cerca i amaga la resta d'elements del Layout (i a l'inrevès)
    public void toggleCerca(boolean cercant) {
        this.cercant = cercant;
        if (cercant) {
            findViewById(R.id.frame_menu_dia).setVisibility(View.GONE);
            findViewById(R.id.text_productes_temporada).setVisibility(View.GONE);
            findViewById(R.id.frame_productes_temporada).setVisibility(View.GONE);
            findViewById(R.id.resultats_cerca).setVisibility(View.VISIBLE);
            drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            findViewById(R.id.frame_menu_dia).setVisibility(View.VISIBLE);
            findViewById(R.id.text_productes_temporada).setVisibility(View.VISIBLE);
            findViewById(R.id.frame_productes_temporada).setVisibility(View.VISIBLE);
            findViewById(R.id.resultats_cerca).setVisibility(View.GONE);
            drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        }
        invalidateOptionsMenu();
    }

    // Mètode que mostra el Fragment amb els resultats de la API de clarifai i amaga la resta d'elements del Layout (i a l'inrevès)
    public void toggleClarifai(boolean cercantClarifai) {
        this.cercantClarifai = cercantClarifai;
        invalidateOptionsMenu();
        ActionBar actionBar = getSupportActionBar();
        if (cercantClarifai) {
            findViewById(R.id.resultats_cerca).setVisibility(View.GONE);
            findViewById(R.id.resultats_clarifai).setVisibility(View.VISIBLE);
            if (actionBar != null) {
                // Modifiquem el títol de l'action bar
                actionBar.setTitle("Productes detectats");
                // Mostrem la fletxa per tornar enrere
                drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } else {
            findViewById(R.id.resultats_cerca).setVisibility(View.VISIBLE);
            findViewById(R.id.resultats_clarifai).setVisibility(View.GONE);
            if (actionBar != null) {
                // Reestablim el títol
                actionBar.setTitle(R.string.app_name);
                // Amaguem la fletxa per tornar enrera per a que es mostri el hamburguer icon del calaix
                actionBar.setDisplayHomeAsUpEnabled(false);
                drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflem el menu per a que es mostri a la ActionBar
        getMenuInflater().inflate(R.menu.menu_cerca, menu);

        MenuItem cercaMenuItem = menu.findItem(R.id.cerca);
        cercaMenuItem.setVisible(!cercantClarifai);


        // Quan l'usuari clica l'icona de la lupa obrim una vista de tipus SearchView a la toolbar i escoltem
        // els canvis.
        cercaMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if(cercant && !cercantClarifai) {
            /*
             *  Per evitar que el calaix es quedi obert en col·lapsar la searchView ho comprovem i tanquem si escau.
             */
                    if(drawerFiltres.isDrawerOpen()) {
                        drawerFiltres.closeDrawer();
                    } else if (drawer.isDrawerOpen()) {
                        drawer.closeDrawer();
                    }
                    toggleCerca(false);
                }
                return true;
            }
        });

        searchView = (SearchView) cercaMenuItem.getActionView();
        searchView.setQueryHint("Recepta, Producte...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.equals(queryAnterior) && query.length() > 2) {
                    queryAnterior = query;
                    resultatsCercaFragment.carregaResultats(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(!query.equals(queryAnterior) && query.length() > 2) {
                    queryAnterior = query;
                    resultatsCercaFragment.carregaResultats(query);
                }
                return true;
            }
        });
        if(cercant && !cercantClarifai) {
            cercaMenuItem.expandActionView();
        } else {
            cercaMenuItem.collapseActionView();
        }

        // L'icona de la càmera només es mostra quan estem cercant
        MenuItem cercaFoto = menu.findItem(R.id.camera);
        cercaFoto.setVisible(cercant && !cercantClarifai);

        // L'icona dels filtres només es mostra quan estem cercant
        filtreMenuItem = menu.findItem(R.id.filtres);
        filtreMenuItem.setVisible(cercant && !cercantClarifai);
        if (productesFiltreInclosos.isEmpty() && productesFiltreExclosos.isEmpty() && categoriesFiltre.isEmpty()) {
            filtreMenuItem.setIcon(R.drawable.ic_filter_empty);
        } else {
            filtreMenuItem.setIcon(R.drawable.ic_filter_filled);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cerca:
                toggleCerca(true);
                return true;
            case R.id.filtres:
                //if(!productesFiltreInclosos.isEmpty() || !productesFiltreExclosos.isEmpty() || !productesFiltreResta.isEmpty()) {
                    drawerFiltres.openDrawer();
                    if (imm != null && this.getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                //}
                return true;
            case R.id.camera:
                iniciaCameraIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if(drawerFiltres != null && drawerFiltres.isDrawerOpen()) {
            drawerFiltres.closeDrawer();
        } else if(cercantClarifai) {
            toggleClarifai(false);
        } else {
            toggleCerca(false);
            moveTaskToBack(true);
        }
    }

    // Quan tornem a mostrar l'activitat després d'haver saltat a una altra activitat recarreguem els elements del menu
    @Override
    protected void onResume() {
        super.onResume();
        carregaFrameItemMenu();
    }

    private void iniciaCameraIntent(){
        /*
         *  Per tal de permetre l'usuari fer fotos sense haver d'implementar una CameraView i gestionar
         *  tot plegat, podem cridar l'aplicació de Càmera preferida per l'usuari mitjançant un Intent amb l'acció ACTION_IMAGE_CAPTURE.
         *  Especificant una Uri amb el Extra MediaStore.EXTRA_OUTPUT fem que l'aplicació iniciada emmagatzemi el resultat en aquesta localització però
         *  per tal de que pugui accedir a l'arxiu li donem l'adreça que passa per el nostre FileProvider i el permís amb FLAG_GRANT_READ_URI_PERMISSION.
         */

        //  Les imatges capturades amb la càmera s'emmagatzemen a la memòria externa de l'aplicació
        // ja que així l'usuari en pot tenir accès però per això cal que
        //  comprovem si tenim permis per llegir i escriure aquesta memòria externa.
        permis = comprovaPermisEscriptura();
        // Si tenim permis iniciem l'intent que llança la càmera

        if(permis) {
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileImatge = Utils.creaFileImatge();
            Uri uriImatgePerExtern = FileProvider.getUriForFile(context, "cat.detemporada.fileprovider", fileImatge);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uriImatgePerExtern);
            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intentCamera, OBRE_CAMERA_CODE);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case OBRE_CAMERA_CODE:
                /*
                 *  Un cop retornats de l'aplicació de la Càmera, si el resultat és d'èxit, carreguem el Fragment que farà la crida
                 *  a la API de Clarifai i mostrarà els resultats de la predicció d'imatge.
                 */
                if(resultCode == RESULT_OK){
                    toggleClarifai(true);

                    // Iniciem el fragment que crida l'API de Clarifai i mostra els resultats passant-li el path
                    // a l'imatge obtinguda

                    ResultatsClarifaiFragment resultatsClarifaiFragment = new ResultatsClarifaiFragment();
                    Bundle arguments = new Bundle();
                    arguments.putString(ResultatsClarifaiFragment.IMATGE, fileImatge.getPath());
                    resultatsClarifaiFragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.resultats_clarifai, resultatsClarifaiFragment)
                            .commit();

                }

                break;
        }
    }

    /*
     *  Quan l'usuari ha seleccionat productes dels resultats de clarifai els afegim als filtres i actualitzem els resultats.
     */
    public void OnResultatsClarifaiAccepted(List<ResultatClarifai> resultats) {
        if (resultats.isEmpty()) {
            iniciaCameraIntent();
        } else {
            for (ResultatClarifai resultat : resultats) {
                if (resultat.isSeleccionat()) {
                    Producte producte = resultat.getProducte();
                    if (!productesFiltreInclosos.contains(producte)) {
                        productesFiltreInclosos.add(producte);
                    }
                    if (productesFiltreExclosos.contains(producte)) {
                        productesFiltreExclosos.remove(producte);
                    }
                    if (productesFiltreResta.contains(producte)) {
                        productesFiltreResta.remove(producte);
                    }
                }
            }

            toggleClarifai(false);
            afegeixItemsDrawerFiltre();
            actualitzaResultatsAmbFiltre();
        }
    }
}
