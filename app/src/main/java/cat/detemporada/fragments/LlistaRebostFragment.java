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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cat.detemporada.R;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.ItemRebost;
import cat.detemporada.models.Producte;

/**
 * Fragment que mostra una llista d'elements del rebost
 */
public class LlistaRebostFragment extends Fragment {

    private List<ItemRebost> rebost = new ArrayList<>();

    private OnItemRebostSelectedListener mListener;

    private RecyclerView recyclerView;
    private SimpleItemRecyclerViewAdapter adapter;
    private TextView textViewNoResultats;

    public LlistaRebostFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rebost = ItemRebost.findWithQuery(ItemRebost.class, "select * from item_rebost order by nom collate nocase asc");
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
        adapter = new SimpleItemRecyclerViewAdapter(rebost);
        recyclerView.setAdapter(adapter);
        textViewNoResultats = view.findViewById(R.id.llista_no_resultats);
        preparaLlista();
    }

    private void preparaLlista() {
        if(!rebost.isEmpty()) {
            adapter.setItems(rebost);
            recyclerView.setVisibility(View.VISIBLE);
            textViewNoResultats.setVisibility(View.GONE);
        } else {
            textViewNoResultats.setText(R.string.no_element_rebost);
            textViewNoResultats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemRebostSelectedListener) {
            mListener = (OnItemRebostSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnItemRebostSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<ItemRebost> mValues;

        SimpleItemRecyclerViewAdapter(List<ItemRebost> items) {
            mValues = items;
        }

        public void setItems(List<ItemRebost> items) {
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


            holder.mView.setTag(position);

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int currentPos = (int) v.getTag();

                    CharSequence opcions[] = new CharSequence[] {"Editar", "Eliminar"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setItems(opcions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    mListener.onEditRebostItemSelected(rebost.get(currentPos).getId());
                                    break;
                                case 1:
                                    deleteItem(currentPos);
                                    break;
                            }
                        }
                    });
                    builder.show();
                    return true;
                }
            });
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

            ItemRebost mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImatgeView = view.findViewById(R.id.generic_list_element_imatge);
                mNomView = view.findViewById(R.id.generic_list_element_nom);
                mExtraView = view.findViewById(R.id.generic_list_element_extra);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNomView.getText() + "'";
            }
        }

    }

    /*
     *  Mètode que elimina de la base de dades l'element que es troba a la posició 'position' de l'array rebost
     */
    public void deleteItem(int position) {
        ItemRebost item = rebost.get(position);
        item.delete();
        rebost.remove(position);
        Toast.makeText(this.getContext(), "S'ha esborrat un element del rebost", Toast.LENGTH_SHORT).show();
        preparaLlista();
    }

    public interface OnItemRebostSelectedListener {
        void onRebostItemSelected(Long itemRebostId);
        void onEditRebostItemSelected(Long itemRebostId);
    }
}
