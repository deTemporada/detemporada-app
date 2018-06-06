package cat.detemporada.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.Producte;
import cat.detemporada.models.Recepta;

/**
 * Fragment que mostra els resultats de la cerca
 */
public class ResultatsCercaFragment extends Fragment {

    public static final String ARG_QUERY = "query";

    private boolean carregant;
    private int totalItemsAnterior = 0;

    private List<Recepta> resultats = new ArrayList<>();
    private List<Long> receptesId = new ArrayList<>();
    private SimpleItemRecyclerViewAdapter adapter;
    private TextView textViewNoResultats;
    private RecyclerView recyclerView;

    private List<Long> productesId = new ArrayList<>();

    private Iterator<Ingredient> ingredientIterator;

    private OnResultatsCercaSelectedListener mListener;

    public String queryFiltreIngredients = "select i.id, i.producte, i.recepta from ingredient i where (i.nom_normalitzat like ? or i.recepta in (select id from recepta where nom like ? or descripcio like ?)) order by i.nom";

    public ResultatsCercaFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     *  Aquest métode és el que es crida des de la MainActivity quan s'escriu alguna cosa a la searchView
     *  o s'apliquen els filtres i el que fa és buidar els resultats i executar la classe
     *  carregaResultatsAsincronament.
     */
    public void carregaResultats(String query) {
        String queryAmbModificadors = "%" + query.trim() + "%";
        totalItemsAnterior = 0;
        resultats = new ArrayList<>();
        resultats.add(null);
        adapter.setItems(resultats);
        receptesId = new ArrayList<>();
        carregaResultatsAsincronament carregador = new carregaResultatsAsincronament(queryFiltreIngredients, queryAmbModificadors, new carregaResultatsAsincronament.AsyncResponse() {
            @Override
            public void processFinish(ArrayList<Recepta> resultatsCerca, Iterator<Ingredient> iterador, List<Long> receptes) {
                receptesId = receptes;
                ingredientIterator = iterador;
                resultats = resultatsCerca;
                new carregaProductesPerFiltreAsincronament(receptesId, productesId, mListener).execute();
                preparaLlista();
            }
        });
        carregador.execute();
    }

    /*
     *  En aquesta classe es cerquen les receptes en base a la query i els filtres de manera asíncrona
     */
    public static class carregaResultatsAsincronament extends AsyncTask<Void, Void, ArrayList<Recepta>> {
        Iterator<Ingredient> ingredientIterator;
        List<Long> receptesId = new ArrayList<>();
        ArrayList<Recepta> resultats = new ArrayList<>();
        String queryFiltreIngredients;
        String queryAmbModificadors;

        public interface AsyncResponse {
            void processFinish(ArrayList<Recepta> resultats, Iterator<Ingredient> ingredientIterator, List<Long> receptesId);
        }

        AsyncResponse delegate = null;

        carregaResultatsAsincronament(String queryFiltreIngredients, String queryAmbModificadors,AsyncResponse delegate) {
            this.delegate = delegate;
            this.queryFiltreIngredients = queryFiltreIngredients;
            this.queryAmbModificadors = queryAmbModificadors;
        }
        carregaResultatsAsincronament(Iterator<Ingredient> ingredientIterator, List<Long> receptesId, AsyncResponse delegate) {
            this.ingredientIterator = ingredientIterator;
            this.receptesId = receptesId;
            this.delegate = delegate;
        }

        @Override
        protected ArrayList<Recepta> doInBackground(Void... strings) {
            if (ingredientIterator == null) {
                ingredientIterator = Ingredient.findWithQueryAsIterator(Ingredient.class, queryFiltreIngredients, queryAmbModificadors, queryAmbModificadors, queryAmbModificadors);
            }
            int limit = 0;
            while (ingredientIterator.hasNext() && limit < 20) {
                Ingredient ingredient = ingredientIterator.next();
                Recepta recepta = ingredient.getRecepta();
                if(!recepta.isEsborrany() && !receptesId.contains(recepta.getId())) {
                    receptesId.add(recepta.getId());
                    resultats.add(recepta);
                    limit++;
                }
            }
            return resultats;
        }

        @Override
        protected void onPostExecute(ArrayList<Recepta> resultats) {
            delegate.processFinish(resultats, ingredientIterator, receptesId);
        }
    }

    /*
     *  Després que carregaResultatsAsincronament retorna els resultats cal que actualitzem els filtres amb els nous productes
     *  provinents dels resultats. Com que això també pot alentir el UI Thread ho fem asíncronament
     */
    public static class carregaProductesPerFiltreAsincronament extends AsyncTask<Void, Void, List<Producte>> {
        Iterator<Ingredient> ingredientIterator;
        List<Long> receptesId = new ArrayList<>();
        List<Long> productesId = new ArrayList<>();
        OnResultatsCercaSelectedListener mListener;

        carregaProductesPerFiltreAsincronament(List<Long> receptesId, List<Long> productesId, OnResultatsCercaSelectedListener mListener) {
            this.receptesId = receptesId;
            this.productesId = productesId;
            this.mListener = mListener;
        }

        @Override
        protected List<Producte> doInBackground(Void... strings) {
            List<Producte> productes = new ArrayList<>();
            for (Long id: receptesId) {
                String query = "select id, producte from ingredient where recepta = ? and producte != 0";
                for(Long producteId: productesId) {
                    query = query.concat(String.format(Locale.getDefault(), " and producte != %d", producteId));
                }
                List<Ingredient> ingredients = Ingredient.findWithQuery(Ingredient.class, query, id.toString());
                for (Ingredient ingredient: ingredients) {
                    Producte producte = ingredient.getProducte();
                    if(producte != null) {
                        productesId.add(producte.getId());
                        productes.add(producte);
                    }
                }
            }
            return productes;
        }

        @Override
        protected void onPostExecute(List<Producte> productes) {
            mListener.carregaProductesFiltre(productes);
        }
    }

    private void preparaLlista() {
        if(!resultats.isEmpty()) {
            adapter.setItems(resultats);
            recyclerView.setVisibility(View.VISIBLE);
            textViewNoResultats.setVisibility(View.GONE);
        } else {
            textViewNoResultats.setText(R.string.no_resultat_recepta);
            textViewNoResultats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflem el layout per al fragment
        return inflater.inflate(R.layout.fragment_llista_base, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.llista_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                        .getLayoutManager();

                int totalItems = linearLayoutManager.getItemCount();
                int ultimItemVisible = linearLayoutManager.findLastVisibleItemPosition();

                if(!carregant && totalItems <= (ultimItemVisible+5) && totalItems > totalItemsAnterior) {

                    // Afegim un element null a la nostra llista de productes per a que es mostri la progressBar dins la RecyclerView
                    resultats.add(null);
                    recyclerView.post(new Runnable() {
                        public void run() {
                            adapter.notifyItemInserted(resultats.size() - 1);
                        }
                    });
                    carregant = true;

                    // Carreguem més resultats de forma asíncrona
                    carregaResultatsAsincronament carregador = new carregaResultatsAsincronament(ingredientIterator, receptesId, new carregaResultatsAsincronament.AsyncResponse() {
                        @Override
                        public void processFinish(ArrayList<Recepta> resultatsCerca, Iterator<Ingredient> iterador, List<Long> receptes) {
                            receptesId = receptes;
                            ingredientIterator = iterador;
                            resultats.remove(null);
                            resultats.addAll(resultatsCerca);
                            totalItemsAnterior = resultats.size();
                            new carregaProductesPerFiltreAsincronament(receptesId, productesId, mListener).execute();
                            preparaLlista();
                        }
                    });
                    carregador.execute();
                }

            }
        });
        adapter = new SimpleItemRecyclerViewAdapter(resultats);
        textViewNoResultats = view.findViewById(R.id.llista_no_resultats);
        recyclerView.setAdapter(adapter);

        if(resultats.isEmpty()) {
            textViewNoResultats.setText(R.string.hint_cerca_recepta);
            textViewNoResultats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultatsCercaSelectedListener) {
            mListener = (OnResultatsCercaSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar els mètodes de OnResultatsCercaSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Recepta> mValues;
        private static final int VIEW_ITEM = 0;
        private static final int VIEW_PROGRESS = 1;

        SimpleItemRecyclerViewAdapter(List<Recepta> items) {
            mValues = items;
        }

        public void setItems(List<Recepta> resultats) {
            mValues = resultats;
            notifyDataSetChanged();
        }

        @Override
        @NonNull
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType == VIEW_ITEM) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_llista_base_element, parent, false);
                return new ViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.progressbar, parent, false);
                return new ProgressViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ViewHolder) {
                ViewHolder vh = (ViewHolder) holder;
                vh.mItem = mValues.get(position);
                String nom;
                Imatge imatgePrincipal;
                nom = vh.mItem.getNom();
                imatgePrincipal = vh.mItem.loadImatgePrincipal();
                vh.mView.setTag(R.id.object, vh.mItem.getId());

                vh.mNomView.setText(nom);

                if (imatgePrincipal != null) {
                    Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(vh.mImatgeView);
                }

                vh.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onResultatSelected((Long) v.getTag(R.id.object));

                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mValues.get(position)!=null ? VIEW_ITEM : VIEW_PROGRESS;
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final ImageView mImatgeView;
            final TextView mNomView;
            final TextView mExtraView;
            Recepta mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImatgeView = view.findViewById(R.id.generic_list_element_imatge);
                mNomView = view.findViewById(R.id.generic_list_element_nom);
                mExtraView = view.findViewById(R.id.generic_list_element_extra);
                mExtraView.setVisibility(View.GONE);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNomView.getText() + "'";
            }
        }

        class ProgressViewHolder extends RecyclerView.ViewHolder{
            ProgressViewHolder(View itemView) {
                super(itemView);
            }
        }

    }

    public interface OnResultatsCercaSelectedListener {
        void onResultatSelected(Long id);
        void carregaProductesFiltre(List<Producte> productes);
    }

}
