package cat.detemporada.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
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

import cat.detemporada.R;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Producte;
import cat.detemporada.models.Temporada;

/**
 * Fragment que mostra una llista de productes
 */
public class LlistaProductesFragment extends Fragment {
    public static final String ARG_PARAM1 = "dia";
    public static final String ARG_PARAM2 = "afegirItem";

    private String dia;
    private boolean afegirItem = false;
    private List<Producte> productes = new ArrayList<>();

    private Iterator<Temporada> temporadaIterator;
    private Iterator<Producte> producteIterator;
    private int limit = 0;
    private boolean carregant = false;
    private int totalItemsAnterior = 0;

    private SimpleItemRecyclerViewAdapter adapter;

    private OnProducteSelectedListener mListener;

    public LlistaProductesFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dia = getArguments().getString(ARG_PARAM1);
            afegirItem = getArguments().getBoolean(ARG_PARAM2);
        }

        if(dia != null) {
            temporadaIterator = Temporada.findWithQueryAsIterator(Temporada.class, "select id, producte from temporada where inici <= ? and fi >= ? and atemporal = 0", dia, dia);
            while (temporadaIterator.hasNext() && limit < 20) {
                Temporada temporada = temporadaIterator.next();
                Producte producte = temporada.getProducte();
                if (producte != null) {
                    productes.add(producte);
                    limit++;
                }
            }
        } else {
            producteIterator = Producte.findWithQueryAsIterator(Producte.class, "select nom, id from producte order by nom");
            while (producteIterator.hasNext() && limit < 20) {
                productes.add(producteIterator.next());
                limit++;
            }
        }

    }

    private void carregaMesResultats() {
        if (productes.size() > 0){
            if(productes.get(productes.size() - 1) == null)
                productes.remove(productes.size() - 1);
        }
        limit = 0;
        if(dia != null) {
            while (temporadaIterator.hasNext() && limit < 10) {
                Temporada temporada = temporadaIterator.next();
                Producte producte = temporada.getProducte();
                if (producte != null) {
                    productes.add(producte);
                    limit++;
                }
            }
        } else {
            while (producteIterator.hasNext() && limit < 10) {
                productes.add(producteIterator.next());
                limit++;
            }
        }
        adapter.setItems(productes);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflem el layout per al fragment
        return inflater.inflate(R.layout.fragment_llista_afegir_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(afegirItem) {
            CardView viewAfegirPersonalitzat = view.findViewById(R.id.afegir_element_personalitzat);
            viewAfegirPersonalitzat.setVisibility(View.VISIBLE);
            viewAfegirPersonalitzat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onProducteSelected(0L);

                }
            });
        }
        RecyclerView recyclerView = view.findViewById(R.id.llista_recyclerview);
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
                    productes.add(null);
                    recyclerView.post(new Runnable() {
                        public void run() {
                            adapter.notifyItemInserted(productes.size() - 1);
                        }
                    });
                    carregant = true;
                    totalItemsAnterior = productes.size();

                    // Carreguem més resultats en un altre Thread
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            carregaMesResultats();
                            carregant = false;
                        }
                    });
                }

            }
        });
        adapter = new SimpleItemRecyclerViewAdapter(productes);
        recyclerView.setAdapter(adapter);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProducteSelectedListener) {
            mListener = (OnProducteSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnProducteSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Producte> mValues;
        private static final int VIEW_ITEM = 0;
        private static final int VIEW_PROGRESS = 1;

        SimpleItemRecyclerViewAdapter(List<Producte> items) {
            mValues = items;
        }

        public void setItems(List<Producte> productes) {
            //progressBar.setVisibility(View.GONE);
            mValues = productes;
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
            if(holder instanceof ViewHolder) {
                ViewHolder vh = (ViewHolder) holder;
                vh.mItem = mValues.get(position);
                vh.mNomView.setText(vh.mItem.getNom());

                Imatge imatgePrincipal = vh.mItem.loadImatgePrincipal();
                if (imatgePrincipal != null) {
                    Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(vh.mImatgeView);
                }

                vh.mView.setTag(vh.mItem.getId());
                vh.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long producteId = (Long) v.getTag();
                        mListener.onProducteSelected(producteId);

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
            Producte mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImatgeView = view.findViewById(R.id.generic_list_element_imatge);
                mNomView = view.findViewById(R.id.generic_list_element_nom);
                mExtraView = view.findViewById(R.id.generic_list_element_extra);
                mExtraView.setVisibility(View.GONE);
            }
        }

        class ProgressViewHolder extends RecyclerView.ViewHolder{
            ProgressViewHolder(View itemView) {
                super(itemView);
            }
        }

    }

    public interface OnProducteSelectedListener {
        void onProducteSelected(Long id);
    }

}
