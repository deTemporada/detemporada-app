package cat.detemporada.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cat.detemporada.R;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.Recepta;

/**
 * Fragment que mostra una llista de receptes
 */
public class LlistaReceptesFragment extends Fragment {
    private static final String TAG = "LlistaReceptesFragment";

    public static final String PRODUCTE_ID = "producteId";
    public static final String PREFERIDA_ARG = "preferida";
    public static final String USUARI_ARG = "deLUsuari";

    private List<Recepta> receptes = new ArrayList<>();

    private OnReceptaSelectedListener mListener;

    private Long producteId = 0L;
    private boolean preferida;
    private boolean usuari;

    private SimpleItemRecyclerViewAdapter adapter;
    private TextView textViewNoResultats;
    private RecyclerView recyclerView;

    public LlistaReceptesFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            producteId = getArguments().getLong(PRODUCTE_ID);
            preferida = getArguments().getBoolean(PREFERIDA_ARG);
            usuari = getArguments().getBoolean(USUARI_ARG);
            carregaLlista();
        }

    }

    private void carregaLlista() {
        if (producteId != 0L) {
            List<Ingredient> ingredients = Ingredient.findWithQuery(Ingredient.class, "select id, recepta from ingredient where producte = ?", producteId.toString());
            for(Ingredient ingredient:ingredients) {
                Recepta recepta = ingredient.getRecepta();
                if(!recepta.isEsborrany()) {
                    receptes.add(recepta);
                }
            }
        } else {
            if (preferida) {
                receptes = Recepta.findWithQuery(Recepta.class, "select id, nom from recepta where preferida = 1");
            } else {
                if (usuari) {
                    receptes = Recepta.findWithQuery(Recepta.class, "select id, nom, usuari, esborrany from recepta where usuari = 1");
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    public void actualitzaLlista() {
        carregaLlista();
        preparaLlista();
    }

    private void preparaLlista() {
        if(!receptes.isEmpty()) {
            adapter.setItems(receptes);
            recyclerView.setVisibility(View.VISIBLE);
            textViewNoResultats.setVisibility(View.GONE);
        } else {
            int textNoResultats = usuari ? R.string.no_receptes_usuari : preferida ? R.string.cap_recepta_preferida : R.string.no_resultat_recepta;
            textViewNoResultats.setText(textNoResultats);
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
        adapter = new SimpleItemRecyclerViewAdapter(receptes);
        recyclerView.setAdapter(adapter);
        textViewNoResultats = view.findViewById(R.id.llista_no_resultats);
        preparaLlista();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReceptaSelectedListener) {
            mListener = (OnReceptaSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnReceptaSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<Recepta> mValues;

        SimpleItemRecyclerViewAdapter(List<Recepta> items) {
            mValues = items;
        }

        public void setItems(List<Recepta> items) {
            mValues = items;
            notifyDataSetChanged();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_llista_base_element, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            String nomRecepta = holder.mItem.getNom();
            try {
                if (nomRecepta.equals("")) {
                    holder.mNomView.setText(R.string.recepta_sense_nom);
                } else {
                    holder.mNomView.setText(nomRecepta);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            Imatge imatgePrincipal = holder.mItem.loadImatgePrincipal();
            if (imatgePrincipal != null) {
                Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(holder.mImatgeView);
            } else {
                Picasso.get().load(getResources().getString(R.string.placeholder_url)).fit().into(holder.mImatgeView);
            }

            holder.mView.setTag(R.id.object,holder.mItem.getId());
            if(holder.mItem.isEsborrany()) {
                holder.mNomView.setTypeface(holder.mNomView.getTypeface(), Typeface.ITALIC);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long receptaId = (Long) v.getTag(R.id.object);
                        mListener.onReceptaEsborranySelected(receptaId);

                    }
                });
            } else {
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long receptaId = (Long) v.getTag(R.id.object);
                        mListener.onReceptaSelected(receptaId);

                    }
                });
            }

            if(holder.mItem.isUsuari()) {
                holder.mView.setTag(R.id.position,position);
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final Long itemId = (Long) v.getTag(R.id.object);
                        final int position = (int) v.getTag(R.id.position);

                        CharSequence opcions[] = new CharSequence[]{"Editar", "Eliminar"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setItems(opcions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mListener.onEditReceptaSelected(itemId);
                                        break;
                                    case 1:
                                        deleteItem(position);
                                        break;
                                }
                            }
                        });
                        builder.show();
                        return false;
                    }
                });
            }



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

    }

    public void deleteItem(int position) {
        Recepta recepta = receptes.get(position);
        recepta.delete();
        receptes.remove(position);
        Toast.makeText(this.getContext(), "S'ha esborrat la recepta", Toast.LENGTH_SHORT).show();
        preparaLlista();
    }

    public interface OnReceptaSelectedListener {
        void onReceptaSelected(Long itemId);
        void onReceptaEsborranySelected(Long itemId);
        void onEditReceptaSelected(Long itemId);
    }
}
