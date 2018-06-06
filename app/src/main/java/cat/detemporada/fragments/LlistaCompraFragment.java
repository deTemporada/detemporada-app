package cat.detemporada.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.ItemLlistaCompra;
import cat.detemporada.models.ItemRebost;
import cat.detemporada.models.Producte;
import jp.wasabeef.picasso.transformations.GrayscaleTransformation;

/**
 * Fragment que mostra la llista de la compra
 */
public class LlistaCompraFragment extends Fragment {

    public static final String RECEPTA_ID = "receptaId";
    private Long receptaId;

    private List<ItemLlistaCompra> llistaCompra = new ArrayList<>();

    private String avui;
    private int ultimaPosicioNoChecked;
    private int marginTop;

    private OnItemLlistaCompraSelectedListener mListener;
    private RecyclerView recyclerView;
    private SimpleItemRecyclerViewAdapter adapter;
    private TextView textViewNoResultats;

    public LlistaCompraFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        avui = s.format(new Date());

        marginTop = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,
                getResources().getDisplayMetrics()
        );

        if (getArguments() != null) {
            receptaId = getArguments().getLong(RECEPTA_ID);
            if (receptaId != 0) {
                carregaLlista(receptaId);
            }
        } else {
            carregaLlista(0L);
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
        adapter = new SimpleItemRecyclerViewAdapter(llistaCompra);
        recyclerView.setAdapter(adapter);
        textViewNoResultats = view.findViewById(R.id.llista_no_resultats);
        preparaLlista();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemLlistaCompraSelectedListener) {
            mListener = (OnItemLlistaCompraSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnItemLlistaCompraSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void carregaLlista(Long receptaId) {
        if(receptaId == 0L) {
            llistaCompra = ItemLlistaCompra.findWithQuery(ItemLlistaCompra.class, "Select * from item_llista_compra where data_adquisicio is null or data_adquisicio = ? order by data_adquisicio, nom", avui);
        } else {
            llistaCompra = ItemLlistaCompra.findWithQuery(ItemLlistaCompra.class, "Select * from item_llista_compra where (data_adquisicio is null or data_adquisicio = ?) and recepta = ? order by data_adquisicio, nom", avui, receptaId.toString());
        }
    }

    public void actualitzaLlista() {
        carregaLlista(0L);
        preparaLlista();
    }

    // Métode que prepara la llista si hi ha elements o mostra un text quan és buida
    private void preparaLlista() {
        if(!llistaCompra.isEmpty()) {
            adapter.setItems(llistaCompra);
            recyclerView.setVisibility(View.VISIBLE);
            textViewNoResultats.setVisibility(View.GONE);
        } else {
            textViewNoResultats.setText(R.string.no_element_llista_compra);
            textViewNoResultats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<ItemLlistaCompra> mValues;

        SimpleItemRecyclerViewAdapter(List<ItemLlistaCompra> items) {
            mValues = items;
            getUltimaPosicioNoChecked();
        }

        private void setItems(List<ItemLlistaCompra> items) {
            mValues = items;
            getUltimaPosicioNoChecked();
            notifyDataSetChanged();
        }

        private void getUltimaPosicioNoChecked() {
            ultimaPosicioNoChecked = -2;
            for(int i = 0; i < mValues.size(); i++) {
                ultimaPosicioNoChecked = mValues.get(i).getDataAdquisicio() == null ? i : ultimaPosicioNoChecked;
            }
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

            if (receptaId == null) {
                holder.mCheckboxView.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mExtraView.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_PARENT_END);
                holder.mExtraView.setLayoutParams(params);
            }

            holder.mItem = mValues.get(position);
            String dataAdquisicio = holder.mItem.getDataAdquisicio();
            holder.mCheckboxView.setOnCheckedChangeListener(null);
            if (dataAdquisicio != null) {
                holder.mCheckboxView.setChecked(true);
                holder.mNomView.setPaintFlags(holder.mNomView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.mNomView.setTypeface(null, Typeface.ITALIC);
                holder.mExtraView.setPaintFlags(holder.mExtraView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.mExtraView.setTypeface(null, Typeface.ITALIC);
                holder.mView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundGray));
            } else {
                holder.mCheckboxView.setChecked(false);
                holder.mNomView.setPaintFlags(holder.mNomView.getPaintFlags() &(~ Paint.STRIKE_THRU_TEXT_FLAG));
                holder.mNomView.setTypeface(null, Typeface.NORMAL);
                holder.mExtraView.setPaintFlags(holder.mExtraView.getPaintFlags() &(~ Paint.STRIKE_THRU_TEXT_FLAG));
                holder.mExtraView.setTypeface(null, Typeface.NORMAL);
                holder.mView.setBackgroundColor(Color.WHITE);
            }

            if (position == ultimaPosicioNoChecked + 1) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) holder.mView.getLayoutParams();
                marginParams.setMargins(marginParams.leftMargin, marginTop, marginParams.rightMargin, marginParams.bottomMargin);
                holder.mView.setLayoutParams(marginParams);
            } else {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) holder.mView.getLayoutParams();
                marginParams.setMargins(marginParams.leftMargin, 0, marginParams.rightMargin, marginParams.bottomMargin);
                holder.mView.setLayoutParams(marginParams);
            }

            holder.mNomView.setText(holder.mItem.getNom());
            holder.mExtraView.setText(holder.mItem.getQuantitatFormatted());

            Producte producte = holder.mItem.getProducte();
            if(producte != null) {
                Imatge imatgePrincipal = producte.loadImatgePrincipal();
                if (imatgePrincipal != null) {
                    if(holder.mCheckboxView.isChecked()) {
                        Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().transform(new GrayscaleTransformation()).into(holder.mImatgeView);
                    } else {
                        Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(holder.mImatgeView);
                    }
                }
            } else {
                if(holder.mCheckboxView.isChecked()) {
                    Picasso.get().load(getResources().getString(R.string.placeholder_url)).fit().centerCrop().transform(new GrayscaleTransformation()).into(holder.mImatgeView);
                } else {
                    Picasso.get().load(getResources().getString(R.string.placeholder_url)).fit().into(holder.mImatgeView);
                }
            }

            if (receptaId == null) {
                holder.mCheckboxView.setTag(R.id.position, position);
                holder.mCheckboxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                        int position = (int) v.getTag(R.id.position);
                        actualitzaItemLlistaComprat(position, isChecked);
                    }
                });

                holder.mView.setTag(position);
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final int currentPos = (int) v.getTag();

                        CharSequence opcions[] = new CharSequence[]{"Editar", "Eliminar"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setItems(opcions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mListener.onEditItemLlistaCompraSelected(mValues.get(currentPos).getId());
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
            final AppCompatCheckBox mCheckboxView;

            ItemLlistaCompra mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImatgeView = view.findViewById(R.id.generic_list_element_imatge);
                mNomView = view.findViewById(R.id.generic_list_element_nom);
                mExtraView = view.findViewById(R.id.generic_list_element_extra);
                mCheckboxView = view.findViewById(R.id.generic_list_element_checkbox);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNomView.getText() + "'";
            }
        }

    }

    /*
     * Cada cop que marquem la checkbox d'un element de la llista de la compra es crida aquest métode que mou
     * l'element dins l'Arraylist i emmagatzema a la base de dades quan ha estat adquirit. També es crea o s'elimina
     * un ItemRebost segons si marquem o desmarquem la casella.
     */
    public void actualitzaItemLlistaComprat(int position, boolean isChecked) {
        ItemLlistaCompra item = llistaCompra.get(position);

        int destinacio;
        if (isChecked) {
            item.setDataAdquisicio(new Date());
            ItemRebost nouItemRebost = new ItemRebost(item);
            nouItemRebost.save();
            destinacio = ultimaPosicioNoChecked;
        } else {
            destinacio = ultimaPosicioNoChecked == -2 ? 0 : ultimaPosicioNoChecked + 1;
            Iterator<ItemRebost> itemRebostIterator = ItemRebost.findWithQueryAsIterator(ItemRebost.class, "select id from item_rebost where item_llista_compra = ?", item.getId().toString());
            if (itemRebostIterator.hasNext()) {
                ItemRebost itemRebost = itemRebostIterator.next();
                itemRebost.delete();
            }
            item.deleteDataAdquisicio();
        }

        int delta = position < destinacio ? 1 : -1;
        for (int i = position; i != destinacio; i += delta) {
            llistaCompra.set(i, llistaCompra.get(i + delta));
        }
        llistaCompra.set(destinacio, item);
        item.save();

        adapter.setItems(llistaCompra);
    }

    public void deleteItem(int position) {
        ItemLlistaCompra item = llistaCompra.get(position);
        item.delete();
        llistaCompra.remove(position);
        Toast.makeText(this.getContext(), "S'ha esborrat l'element de la llista", Toast.LENGTH_SHORT).show();
        preparaLlista();
    }

    public interface OnItemLlistaCompraSelectedListener {
        void onEditItemLlistaCompraSelected(Long itemId);
    }
}
