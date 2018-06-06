package cat.detemporada.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.fragments.LlistaIngredientsFragment;
import cat.detemporada.fragments.LlistaPasReceptaFragment;
import cat.detemporada.helpers.Utils;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.ItemLlistaCompra;
import cat.detemporada.models.Recepta;

/*
 *  ReceptaActivity
 *
 *  Activitat que mostra una Recepta
 *
 */

public class ReceptaActivity extends AppCompatActivity implements LlistaIngredientsFragment.OnIngredientsSelectedListener, View.OnClickListener, LlistaPasReceptaFragment.OnPasReceptaSelectedListener {
    public final static String ITEM_ID = "itemId";

    private Recepta recepta;
    private FrameLayout frameIngredients;
    private TextView viewIngredientsTitol;
    private Long itemId = 0L;

    private List<Ingredient> ingredients;
    private LlistaIngredientsFragment fragmentIngredients = new LlistaIngredientsFragment();

    private List<AppCompatImageButton> stars = new ArrayList<>();
    private AppCompatImageButton buttonPreferida;
    private AppCompatImageButton buttonLlistaCompra;
    private AppCompatImageButton buttonAddCalendar;

    private Calendar calendar = Calendar.getInstance();

    private Context context;

    private Date avui = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepta);
        Toolbar toolbar = findViewById(R.id.detall_toolbar);
        setSupportActionBar(toolbar);

        context = this.getApplicationContext();

        // Mostrem la fletxa per tornar enrere
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*
         *  Recollim el Intent que ha llançat l'activitat i ens assegurem que ens han passat l'ID de la recepta.
         *  En tots els casos contraris tanquem l'activitat.
         */
        Intent intentEntrant = getIntent();
        if (intentEntrant != null && intentEntrant.hasExtra(ITEM_ID)) {
            itemId = intentEntrant.getLongExtra(ITEM_ID, 0);
            if (itemId > 0) {
                recepta = Recepta.findById(Recepta.class, itemId);
                if(recepta != null) {
                    Imatge imatge = recepta.loadImatgePrincipal();
                    CollapsingToolbarLayout appBarLayout = this.findViewById(R.id.detall_toolbar_layout);
                    if (appBarLayout != null) {
                        appBarLayout.setTitle(recepta.getNom());
                    }
                    ImageView imageHeader = this.findViewById(R.id.detall_imatge);
                    if (imageHeader != null && imatge != null) {
                        Picasso.get().load(imatge.getPath()).into(imageHeader);
                    }
                   } else {
                       finish();
                   }
               } else {
                   finish();
               }
        } else {
            finish();
        }

        /*
         *  Inicialitzem les Views del layout i establim un OnClickListener al botons
         */

        frameIngredients = findViewById(R.id.frame_ingredients);
        viewIngredientsTitol = findViewById(R.id.recepta_ingredients_titol);
        viewIngredientsTitol.setOnClickListener(this);

        buttonPreferida = findViewById(R.id.recepta_icon_bookmark);
        buttonPreferida.setOnClickListener(this);

        buttonLlistaCompra = findViewById(R.id.recepta_icon_cart);
        buttonLlistaCompra.setOnClickListener(this);

        buttonAddCalendar = findViewById(R.id.recepta_icon_calendar);
        buttonAddCalendar.setOnClickListener(this);

        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star1));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star2));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star3));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star4));
        stars.add((AppCompatImageButton) findViewById(R.id.recepta_star5));
        for(AppCompatImageButton star: stars) {
            star.setOnClickListener(this);
        }

        actualitzaEstrelles(recepta.getValoracio());

        if(recepta.isPreferida()) {
            buttonPreferida.setImageResource(R.drawable.ic_bookmark_filled);
        }

        if(Utils.receptaInLlistaCompra(itemId)) {
            buttonLlistaCompra.setImageResource(R.drawable.ic_cart_enabled);
        }

        if(Utils.receptaInFutureMenu(itemId, avui)) {
            buttonAddCalendar.setImageResource(R.drawable.ic_calendari_enabled);
        }

        TextView viewDificultat = findViewById(R.id.recepta_dificultat);
        TextView viewTemps = findViewById(R.id.recepta_temps);
        TextView viewPersones = findViewById(R.id.recepta_persones);
        TextView viewCategoria = findViewById(R.id.recepta_categoria);
        TextView viewDescripcio = findViewById(R.id.recepta_descripcio);
        TextView viewTemporalitat = findViewById(R.id.recepta_temporalitat);

        Recepta.Dificultat dificultat = recepta.getDificultat();
        switch (dificultat) {
            case BAIXA:
                viewDificultat.setTextColor(getResources().getColor(R.color.dificultatBaixa));
            case MITJANA:
                viewDificultat.setTextColor(getResources().getColor(R.color.dificultatMitjana));
                break;
            case ALTA:
                viewDificultat.setTextColor(getResources().getColor(R.color.dificultatAlta));
                break;
        }
        viewDificultat.setText(dificultat.toString());

        viewCategoria.setText(recepta.getCategoria().toString());

        String descripcio = recepta.getDescripcio();
        if (descripcio.isEmpty()) {
            viewDescripcio.setVisibility(View.GONE);
        } else {
            viewDescripcio.setText(recepta.getDescripcio());
        }

        viewTemporalitat.setText(String.valueOf(recepta.getIndexTemporalitat()).concat("%"));

        viewTemps.setText(String.format(Locale.getDefault(),"%d'", recepta.getTemps()));
        viewPersones.setText(String.format(Locale.getDefault(),"%d", recepta.getPersones()));

        carregaIngredientsFragment(itemId);
        carregaInstruccionsFragment(itemId);

    }

    //  mètode que carrega el Fragment amb la llista de Ingredient per a una Recepta donada
    //  passant-li l'ID corresponent
    private void carregaIngredientsFragment(Long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(LlistaIngredientsFragment.RECEPTA_ID, itemId);
        fragmentIngredients.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_ingredients, fragmentIngredients)
                .commit();
    }

    //  Mètode que carrega el Fragment amb la llista de PasRecepta per a una Recepta donada
    //  passant-li l'ID corresponent
    private void carregaInstruccionsFragment(Long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(LlistaPasReceptaFragment.RECEPTA_ID, itemId);
        LlistaPasReceptaFragment fragment = new LlistaPasReceptaFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_instruccions, fragment)
                .commit();
    }

    // Implementació del mètode de l'interface LlistaIngredientsFragment.OnIngredientsSelectedListener que inicia
    // l'activitat ProducteActivity passant-li l'ID corresponent
    public void onIngredientSelected(Long itemId) {
        Intent intent = new Intent(this.getApplicationContext(), ProducteActivity.class);
        intent.putExtra(ProducteActivity.ITEM_ID, itemId);
        startActivity(intent);
    }

    public void onAfegirALlistaCompraSelected(Long itemId) {
        Ingredient ingredient = Ingredient.findById(Ingredient.class, itemId);
        if (ingredient != null) {
            ItemLlistaCompra itemLlistaCompra = new ItemLlistaCompra(ingredient);
            itemLlistaCompra.save();
            Toast.makeText(context, "S'han afegit l'ingredient a la llista de la compra", Toast.LENGTH_SHORT).show();
        }
    }

    // Aquest mètodes no els fem servir en aquesta activitat però cal implementar-los
    public void onEditIngredientSelected(Long itemId, int posicio) {}
    public void onDeleteIngredientSelected(int posicio) {}
    public void onEditPasReceptaSelected(Long itemId, int posicio){}
    public void onDeletePasReceptaSelected(int posicio){}

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

    // Mètode que marca o desmarca una recepta com a preferida actualitzant el drawable i emmagatzemant el valor a la base de dades
    private void toggleReceptaPreferida() {
        if(recepta.isPreferida()) {
            buttonPreferida.setImageResource(R.drawable.ic_bookmark_empty);
            Toast.makeText(context, "S'ha tret la recepta de la llista de preferides", Toast.LENGTH_SHORT).show();
        } else {
            buttonPreferida.setImageResource(R.drawable.ic_bookmark_filled);
            Toast.makeText(context, "S'ha afegit la recepta a la llista de preferides", Toast.LENGTH_SHORT).show();
        }
        recepta.setPreferida(!recepta.isPreferida());
        recepta.save();
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
        Toast.makeText(context, String.format(Locale.getDefault(),"Recepta valorada amb %d estrelles", Math.round(valoracio)), Toast.LENGTH_SHORT).show();
        recepta.setValoracio(valoracio);
        recepta.save();
        actualitzaEstrelles(valoracio);
    }

    // Mètode que emmagatzema els ingredients de la recepta a la llista de la compra o els elimina
    private void toggleIngredientsLlistaCompra() {
        if(ingredients == null) {
            ingredients = fragmentIngredients.getIngredients();
        }
        if (Utils.receptaInLlistaCompra(itemId)) {
            Utils.eliminaIngredientsDeLlistaCompra(itemId);
            buttonLlistaCompra.setImageResource(R.drawable.ic_cart_disabled);
            Toast.makeText(context, "S'han eliminat els ingredients de la llista de la compra", Toast.LENGTH_SHORT).show();
        } else {
            Utils.afegeixIngredientsALlistaCompra(ingredients);
            buttonLlistaCompra.setImageResource(R.drawable.ic_cart_enabled);
            Toast.makeText(context, "S'han afegit els ingredients a la llista de la compra", Toast.LENGTH_SHORT).show();
        }
    }

    /*  Mètode que crea un AlertDialog amb un layout personalitzat que inclou un calendari i permet a l'usuari seleccionar una data i
     *  establir la recepta com a àpat per aquell dia.
     */
    private void creaDialogCalendari() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_afegir_calendari, null);

        CalendarView calendarView = dialoglayout.findViewById(R.id.calendari);

        // No volem que l'usuari pugui seleccionar una data en el passat pel que establim que la data mínima sigui avui
        calendarView.setMinDate(calendar.getTimeInMillis());
        calendarView.setTag(calendar.getTime());

        //  Com que el mètode calendarView.getDate retorna la data actual cal que escoltem si l'usuari selecciona una
        //  nova data i l'emmagatzamem com a Tag a la vista per poder-hi accedir en clicar el botó "Afegir" de l'AlertDialog
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int day) {
                calendar.set(year, month, day);
                view.setTag(calendar.getTime());
            }
        });

        builder.setTitle("Al menu de quin dia vols afegir la recepta?");
        builder.setView(dialoglayout);
        builder.setNegativeButton("Cancelar", null);
        builder.setPositiveButton("Afegir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CalendarView calendarView = ((AlertDialog)dialog).findViewById(R.id.calendari);
                try {
                    Date data = (Date) calendarView.getTag();
                    RadioGroup apatButton = ((AlertDialog)dialog).findViewById(R.id.apat);
                    boolean dinar = apatButton.getCheckedRadioButtonId() == R.id.apat_dinar;
                    Utils.afegeixReceptaAMenu(recepta, data, dinar);
                    SimpleDateFormat s = new SimpleDateFormat("dd/MM/YY", Locale.getDefault());
                    buttonAddCalendar.setImageResource(R.drawable.ic_calendari_enabled);
                    Toast.makeText(context, String.format("S'ha afegit la recepta al menu del %s", s.format(data)), Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Log.e("ReceptaActivity", e.getLocalizedMessage());
                }
            }
        });

        builder.show();
    }

    /*
     *  Com que l'activitat implementa View.OnClickListener podem fer un Override del mètode onClick i gestionar tots els esdeveniments
     *  onClick des d'aqui
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.recepta_ingredients_titol:
                toggleIngredientsVisibility();
                break;
            case R.id.recepta_icon_bookmark:
                toggleReceptaPreferida();
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
            case R.id.recepta_icon_cart:
                toggleIngredientsLlistaCompra();
                break;
            case R.id.recepta_icon_calendar:
                creaDialogCalendari();
                break;
        }
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

}
