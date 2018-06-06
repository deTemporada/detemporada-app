package cat.detemporada.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cat.detemporada.R;
import cat.detemporada.activities.AfegirEditarReceptaActivity;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.Producte;

/**
 * Fragment que mostra una llista d'ingredients
 */
public class LlistaIngredientsFragment extends Fragment {
    public static final String RECEPTA_ID = "receptaId";

    private List<Ingredient> ingredients = new ArrayList<>();

    private OnIngredientsSelectedListener mListener;

    private SimpleItemRecyclerViewAdapter adapter;
    private Context context;

    public LlistaIngredientsFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Long receptaId = getArguments().getLong(RECEPTA_ID);
            if (receptaId != 0) {
                ingredients = Ingredient.find(Ingredient.class, "recepta = ?", receptaId.toString());
            }
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
        context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.llista_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SimpleItemRecyclerViewAdapter(ingredients);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIngredientsSelectedListener) {
            mListener = (OnIngredientsSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnIngredientSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setIngredients(List<Ingredient> ingredients) {
            adapter.setItems(ingredients);
    }

    private class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<Ingredient> mValues;

        SimpleItemRecyclerViewAdapter(List<Ingredient> items) {
            mValues = items;
        }

        public void setItems(List<Ingredient> items) {
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
            holder.mNomView.setText(holder.mItem.getNom());
            holder.mExtraView.setText(holder.mItem.getQuantitatFormatted());

            Producte producte = holder.mItem.getProducte();
            if(producte != null) {
                Imatge imatgePrincipal = producte.loadImatgePrincipal();
                if (imatgePrincipal != null) {
                    Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(holder.mImatgeView);
                }
            } else {
                Picasso.get().load(getResources().getString(R.string.placeholder_url)).fit().into(holder.mImatgeView);
            }


            /*
             *  En cas que aquest Fragment hagi estat afegit a l'activity AfegirEditarReceptaActivity afegim un
             *  OnLongClickListener que mostrarà les opcions d'editar i eliminar l'element
             */
            if (context instanceof AfegirEditarReceptaActivity) {
                holder.mView.setTag(R.id.position,position);
                holder.mView.setTag(R.id.object, holder.mItem.getId());
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final Long itemId = (Long) v.getTag(R.id.object);
                        final int position = (int) v.getTag(R.id.position);

                        CharSequence opcions[] = new CharSequence[] {"Editar", "Eliminar"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setItems(opcions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mListener.onEditIngredientSelected(itemId, position);
                                        break;
                                    case 1:
                                        mListener.onDeleteIngredientSelected(position);
                                        break;
                                }
                            }
                        });
                        builder.show();
                        return true;
                    }
                });
            } else {
                // En cas contrari s'està mostrant el fragment a una ReceptaActivity pel que afegim el listeners corresponents
                if(producte != null) {
                    holder.mView.setTag(R.id.object, producte.getId());
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Long itemId = (Long) v.getTag(R.id.object);
                            mListener.onIngredientSelected(itemId);
                        }
                    });
                    holder.mView.setTag(R.id.ingredient, holder.mItem.getId());
                    holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final Long itemId = (Long) v.getTag(R.id.ingredient);

                            CharSequence opcions[] = new CharSequence[] {"Afegir a la llista de la compra"};

                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setItems(opcions, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            mListener.onAfegirALlistaCompraSelected(itemId);
                                            break;
                                    }
                                }
                            });
                            builder.show();
                            return true;
                        }
                    });
                }
            }

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final ImageView mImatgeView;
            final TextView mNomView;
            final TextView mExtraView;

            Ingredient mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImatgeView = view.findViewById(R.id.generic_list_element_imatge);
                mNomView = view.findViewById(R.id.generic_list_element_nom);
                mExtraView = view.findViewById(R.id.generic_list_element_extra);
            }
        }

    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public interface OnIngredientsSelectedListener {
        void onIngredientSelected(Long itemId);
        void onAfegirALlistaCompraSelected(Long itemId);
        void onEditIngredientSelected(Long itemId, int posicio);
        void onDeleteIngredientSelected(int posicio);
    }
}
