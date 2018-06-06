package cat.detemporada.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.helpers.MenuDrawer;
import cat.detemporada.helpers.Utils;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Recepta;

/*
 *  ExploraActivity
 *
 *  Activitat que mostra un llista de Receptes que es va carregant automàticament de la base de dades
 *  a mida que l'usuari va fullejant més Receptes gràcies a un Iterator.
 *  En clicar sobre una Recepta es llança l'activity ReceptaActivity
 *
 */

public class ExploraActivity extends AppCompatActivity {

    private Drawer drawer;
    private Iterator<Recepta> receptesIterator;
    private List<Recepta> receptes = new ArrayList<>();
    private CustomPagerAdapter adapter;
    private ViewPager viewPager;
    private boolean pausat;

    private Date avui = new Date();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explora);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicialitzem el calaix mitjançant el helper MenuDrawer
        drawer = MenuDrawer.createMenu(this.getApplicationContext(), this, toolbar, 6, savedInstanceState);

        // Seleccionem totes les receptes que no siguin esborranys i les ordenem aleatòriament
        receptesIterator = Recepta.findWithQueryAsIterator(Recepta.class, "select * from recepta where esborrany = 0 order by random()");

        // Carreguem les 10 primeres receptes que ens retorna l'Iterator.
        for (int i = 0; i < 10; i++) {
            if (receptesIterator.hasNext()) {
                receptes.add(receptesIterator.next());
            }
        }

        // Calcuem 16dp en píxels
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16,
                getResources().getDisplayMetrics()
        );

        //Inicialitzem el nostre ViewPager
        viewPager = findViewById(R.id.frame_explora);
        adapter = new CustomPagerAdapter(this, receptes);
        viewPager.setAdapter(adapter);
        viewPager.setClipToPadding(false);
        // Per a que es vegi una mica de la següent CardView fem que el marge de la pàgina del ViewPager sigui la meitat que el padding.
        viewPager.setPadding(margin, margin, margin, margin);
        viewPager.setPageMargin(margin/2);

        // Afegim un Listener al ViewPager que carrega més receptes de l'Iterator (mentre existeixin) quan quedin menys de 10 receptes per el final
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            public void onPageSelected(int position) {
                if(position + 10 > receptes.size()) {
                    if (receptesIterator.hasNext()) {
                        adapter.addRecepta(receptesIterator.next());
                    }
                }
            }
        });

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


    /*
     *  PagerAdapter personalitzat que mostra les dades de la recepta (excepte ingredients i instruccions)
     *  de manera anàloga a ReceptaActivity (Més endavant: convertir-ho en Fragment per no repetir codi)
     */
    public class CustomPagerAdapter extends PagerAdapter {

        private Context mContext;
        List<Recepta> receptes;

        private CustomPagerAdapter(Context context, List<Recepta> receptes) {
            mContext = context;
            this.receptes = receptes;
        }

        private void setItems(List<Recepta> receptes) {
            this.receptes = receptes;
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            Recepta recepta = receptes.get(position);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup receptaView = (ViewGroup) inflater.inflate(R.layout.cardview_activity_explora_recepta, collection, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.imatgeView = receptaView.findViewById(R.id.recepta_imatge);
            viewHolder.nomReceptaView = receptaView.findViewById(R.id.recepta_titol);
            viewHolder.tempsView = receptaView.findViewById(R.id.recepta_temps);
            viewHolder.personesView = receptaView.findViewById(R.id.recepta_persones);
            viewHolder.dificultatView = receptaView.findViewById(R.id.recepta_dificultat);
            viewHolder.categoriaView = receptaView.findViewById(R.id.recepta_categoria);
            viewHolder.descripcioView = receptaView.findViewById(R.id.recepta_descripcio);
            viewHolder.temporalitatView = receptaView.findViewById(R.id.recepta_temporalitat);
            viewHolder.stars.add((ImageView) receptaView.findViewById(R.id.recepta_star1));
            viewHolder.stars.add((ImageView) receptaView.findViewById(R.id.recepta_star2));
            viewHolder.stars.add((ImageView) receptaView.findViewById(R.id.recepta_star3));
            viewHolder.stars.add((ImageView) receptaView.findViewById(R.id.recepta_star4));
            viewHolder.stars.add((ImageView) receptaView.findViewById(R.id.recepta_star5));

            viewHolder.add_menu = receptaView.findViewById(R.id.recepta_icon_calendar);
            viewHolder.add_llista_compra = receptaView.findViewById(R.id.recepta_icon_cart);
            viewHolder.add_preferits = receptaView.findViewById(R.id.recepta_icon_bookmark);

            viewHolder.nomReceptaView.setText(recepta.getNom());

            actualitzaEstrelles(recepta.getValoracio(), viewHolder.stars);

            if(recepta.isPreferida()) {
                viewHolder.add_preferits.setImageResource(R.drawable.ic_bookmark_filled);
            }

            if(Utils.receptaInLlistaCompra(recepta.getId())) {
                viewHolder.add_llista_compra.setImageResource(R.drawable.ic_cart_enabled);
            }

            if(Utils.receptaInFutureMenu(recepta.getId(), avui)) {
                viewHolder.add_menu.setImageResource(R.drawable.ic_calendari_enabled);
            }

            Recepta.Dificultat dificultat = recepta.getDificultat();
            switch (dificultat) {
                case BAIXA:
                    viewHolder.dificultatView.setTextColor(getResources().getColor(R.color.dificultatBaixa));
                case MITJANA:
                    viewHolder.dificultatView.setTextColor(getResources().getColor(R.color.dificultatMitjana));
                    break;
                case ALTA:
                    viewHolder.dificultatView.setTextColor(getResources().getColor(R.color.dificultatAlta));
                    break;
            }

            viewHolder.dificultatView.setText(dificultat.toString());

            viewHolder.categoriaView.setText(recepta.getCategoria().toString());
            viewHolder.descripcioView.setText(recepta.getDescripcio());

            viewHolder.temporalitatView.setText(String.valueOf(recepta.getIndexTemporalitat()).concat("%"));

            viewHolder.tempsView.setText(String.format(Locale.getDefault(),"%d'", recepta.getTemps()));
            viewHolder.personesView.setText(String.format(Locale.getDefault(),"%d", recepta.getPersones()));

            Imatge imatge = recepta.loadImatgePrincipal();
            if(imatge != null) {
                Picasso.get().load(imatge.getPath()).into(viewHolder.imatgeView);
            }

            // Si l'usuari clica a la CardView de la recepta iniciem l'activitat que la mostra
            receptaView.setTag(R.id.position, position);
            receptaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startReceptaActivity((int) v.getTag(R.id.position));
                }
            });

            collection.addView(receptaView);
            return receptaView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return receptes.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Recepta recepta = receptes.get(position);
            return recepta.getNom();
        }

        void addRecepta(Recepta recepta) {
            receptes.add(recepta);
            notifyDataSetChanged();
        }

        private class ViewHolder {
            List<ImageView> stars = new ArrayList<>();
            ImageView imatgeView,add_menu, add_llista_compra, add_preferits;
            TextView nomReceptaView, tempsView, personesView, dificultatView, categoriaView, descripcioView, temporalitatView;
        }
    }


    // Mètode que inicia l'activitat ReceptaActivity amb l'ID de la recepta seleccionada
   public void startReceptaActivity(int position) {
       Intent intent = new Intent(this.getApplicationContext(), ReceptaActivity.class);
       intent.putExtra(ReceptaActivity.ITEM_ID, receptes.get(position).getId());
       intent.setAction(Intent.ACTION_EDIT);
       startActivity(intent);
   }

    // Mètode que modifica el drawable de les 5 estrelles (AppCompatImageButton) per a que siguin
    // plenes o buides segons la valoració de la recepta
    private void actualitzaEstrelles(float valoracio, List<ImageView> stars) {
        int valoracioEntera = Math.round(valoracio);
        for(int i = 0; i < 5; i++) {
            if (i<valoracioEntera) {
                stars.get(i).setImageResource(R.drawable.ic_star_filled);
            } else {
                stars.get(i).setImageResource(R.drawable.ic_star_empty);
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
            int receptaActualIndex = viewPager.getCurrentItem();
            Recepta receptaActual = receptes.get(receptaActualIndex);
            Recepta receptaActualitzada = Recepta.findById(Recepta.class, receptaActual.getId());
            receptes.set(receptaActualIndex, receptaActualitzada);
            adapter.setItems(receptes);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(receptaActualIndex);
            pausat = false;
        }
    }
}
