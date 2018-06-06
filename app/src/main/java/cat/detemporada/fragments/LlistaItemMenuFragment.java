package cat.detemporada.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.helpers.ItemTouchHelperCallback;
import cat.detemporada.helpers.Utils;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.ItemMenu;
import cat.detemporada.models.Recepta;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Fragment que mostra una llista de items del menu
 */
public class LlistaItemMenuFragment extends Fragment {
    public static final String ARG_PARAM1 = "dia";

    private String dia;
    private List<Object> items = new ArrayList<>();
    private Iterator<Recepta> novesReceptes = Utils.receptesSuggeridesIterator();

    int cornerSize;

    private OnItemMenuSelectedListener mListener;

    public LlistaItemMenuFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cornerSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,
                getResources().getDisplayMetrics()
        );

        if (getArguments() != null) {
            dia = getArguments().getString(ARG_PARAM1);
        }

        List<ItemMenu> itemsMenu;
        if(dia != null) {
            itemsMenu =  ItemMenu.findWithQuery(ItemMenu.class, "select * from item_menu where data = ? order by data, apat asc", dia);
        } else {
            SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            dia = s.format(new Date());
            itemsMenu = ItemMenu.findWithQuery(ItemMenu.class, "select * from item_menu where data >= ? order by data, apat asc", dia);
        }
        if(!itemsMenu.isEmpty()) {
            Date dataPrev = null;
            for (ItemMenu itemMenu : itemsMenu) {
                Date data = itemMenu.getDataAsDate();
                if (!data.equals(dataPrev)) {
                    items.add(data);
                    dataPrev = data;
                }
                items.add(itemMenu);
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

        RecyclerView recyclerView = view.findViewById(R.id.llista_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        SimpleItemRecyclerViewAdapter adapter = new SimpleItemRecyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemMenuSelectedListener) {
            mListener = (OnItemMenuSelectedListener) context;
        }/* else if (context instanceof OnProducteForRebostSelectedListener) {
            mListener2 = (OnProducteForRebostSelectedListener) context;
        }*/ else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnItemMenuSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
     * Implementant el nostre ItemTouchHelper propi podem permetre a l'usuari que faci swipe sobre un element de la RecyclerView
     */

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperCallback.ItemTouchHelperAdapter{

        private static final int VIEW_ITEM_MENU = 0;
        private static final int VIEW_ITEM_MENU_DATA = 1;

        private List<Object> mValues;
        private HashMap<ItemMenu, ItemMenu> mLlistaItems = new HashMap<>();

        SimpleItemRecyclerViewAdapter(List<Object> items) {
            mValues = items;
        }

        @Override
        public int getItemViewType(int position) {
            if(mValues.get(position) instanceof ItemMenu) {
                return VIEW_ITEM_MENU;
            } else {
                return VIEW_ITEM_MENU_DATA;
            }
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if(viewType == VIEW_ITEM_MENU) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_menu_item, parent, false);
                return new ViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_menu_item_data, parent, false);
                return new ViewHolderAmbData(view);
            }

        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            if(getItemViewType(position) == VIEW_ITEM_MENU_DATA) {
                String textData;
                Date data = Utils.dataSenseTemps((Date) mValues.get(position));
                Date avui = Utils.dataSenseTemps(new Date());
                if (data.equals(avui)) {
                    textData = "Menu d'avui:";
                } else {
                    SimpleDateFormat s = new SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.getDefault());
                    textData = s.format(data);
                }
                ((ViewHolderAmbData) holder).mDataView.setText(textData);
            } else {
                holder.mProgressBar.setVisibility(View.INVISIBLE);

                holder.mItem = (ItemMenu) mValues.get(position);

                Recepta recepta = holder.mItem.getRecepta();
                holder.mNomView.setText(recepta.getNom());

                ItemMenu.Apat apat = holder.mItem.getApat();
                holder.mApatView.setText(apat.toString());
                if (apat.equals(ItemMenu.Apat.DINAR)) {
                    holder.mApatView.setBackgroundColor(getResources().getColor(R.color.etiqueta_dinar));
                } else {
                    holder.mApatView.setBackgroundColor(getResources().getColor(R.color.etiqueta_sopar));
                }

                holder.mTempsView.setText(String.format(Locale.getDefault(), "%d'", recepta.getTemps()));

                Imatge imatgePrincipal = recepta.loadImatgePrincipal();
                if (imatgePrincipal != null) {
                    Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().transform(new RoundedCornersTransformation(cornerSize, 0, RoundedCornersTransformation.CornerType.TOP)).into(holder.mImatgeView);
                }
                holder.mView.setTag(recepta.getId());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long receptaId = (Long) v.getTag();
                        mListener.onItemMenuSelected(receptaId);

                    }
                });
                holder.mCardView.setPadding(0, 0, 0, 0);
                holder.mCardView.setPreventCornerOverlap(false);
                holder.mCardView.setUseCompatPadding(false);
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final ImageView mImatgeView;
            final TextView mNomView, mTempsView, mApatView;
            final CardView mCardView;
            public RelativeLayout mPrimerPla;
            public TextView mDesfesText, mNouSuggerimentText;
            public ImageView mDesfesIcona, mNouSuggerimentIcona;
            public ProgressBar mProgressBar;

            ItemMenu mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImatgeView = view.findViewById(R.id.recepta_imatge);
                mNomView = view.findViewById(R.id.recepta_titol);
                mTempsView = view.findViewById(R.id.recepta_temps);
                mApatView = view.findViewById(R.id.recepta_apat);
                mCardView = view.findViewById(R.id.recepta_card);
                mPrimerPla = view.findViewById(R.id.item_primerpla);
                mDesfesText = view.findViewById(R.id.menu_desfes_text);
                mDesfesIcona = view.findViewById(R.id.menu_desfes_icona);
                mNouSuggerimentText = view.findViewById(R.id.menu_nou_suggeriment_text);
                mNouSuggerimentIcona = view.findViewById(R.id.menu_nou_suggeriment_icona);
                mProgressBar = view.findViewById(R.id.menu_progressbar);
            }

            public boolean esUnItemNou() {
                return mLlistaItems.containsKey(mItem);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNomView.getText() + "'";
            }
        }

        public class ViewHolderAmbData extends ViewHolder {
            final View mView;
            final TextView mDataView;

            ViewHolderAmbData(View view) {
                super(view);
                mView = view;
                mDataView = view.findViewById(R.id.recepta_data);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNomView.getText() + "'";
            }
        }

        @Override
        public void nouMenu(int position) {
            ItemMenu itemNou;
            ItemMenu itemVell = (ItemMenu) mValues.get(position);
            ItemMenu.Apat apat = itemVell.getApat();
            Date data = itemVell.getDataAsDate();
            // Si l'iterator ha arribat al final en carreguem un de nou per a que l'usuari pugui fer swipe infinit
            if(!novesReceptes.hasNext()) {
                novesReceptes = Utils.receptesSuggeridesIterator();
            }
            if(novesReceptes.hasNext()) {
                itemNou = new ItemMenu(novesReceptes.next(), data, apat);
                Recepta receptaNova = itemNou.getRecepta();
                receptaNova.setValoracio(receptaNova.getValoracio() + 0.1f);
                receptaNova.save();
                itemNou.save();

                Recepta receptaVella = itemVell.getRecepta();
                receptaVella.setValoracio(receptaVella.getValoracio() - 0.1f);
                receptaVella.save();
                itemVell.delete();

                mLlistaItems.put(itemNou, itemVell);
                mValues.set(position, itemNou);
            }
            notifyDataSetChanged();
        }

        @Override
        public void desfesNouMenu(int position) {
            // Recuperem l'item que volem eliminar i l'esborrem de la base de dades
            ItemMenu itemADesfer = (ItemMenu) mValues.get(position);
            Recepta receptaADesfer = itemADesfer.getRecepta();
            receptaADesfer.setValoracio(receptaADesfer.getValoracio() - 0.1f);
            receptaADesfer.save();
            itemADesfer.delete();

            // Recuperem l'item que havia estat substituït pel que hem eliminat i l'emmagatzemem a la base de dades
            ItemMenu itemVell = mLlistaItems.get(itemADesfer);
            Recepta receptaVella = itemVell.getRecepta();
            receptaVella.setValoracio(receptaVella.getValoracio() + 0.1f);
            receptaVella.save();
            itemVell.save();

            // Posem l'item que havia estat substituït a la nostra llista i actualitzem la RecyclerView
            mValues.set(position, itemVell);
            notifyDataSetChanged();
        }
    }

    public interface OnItemMenuSelectedListener {
        void onItemMenuSelected(Long id);
    }

}
