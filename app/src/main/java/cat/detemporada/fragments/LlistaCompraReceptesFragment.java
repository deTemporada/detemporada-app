package cat.detemporada.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.ItemLlistaCompra;
import cat.detemporada.models.Recepta;

/**
 * Fragment que mostra una llista de receptes corresponents als elements de la llista de la compra
 */
public class LlistaCompraReceptesFragment extends Fragment {

    private List<Recepta> receptes = new ArrayList<>();

    private RecyclerView recyclerView;
    private SimpleItemRecyclerViewAdapter adapter;
    private TextView textViewNoResultats;
    private RecyclerView.LayoutManager layoutManager;
    private String avui;
    private FragmentManager fragmentManager;

    public LlistaCompraReceptesFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        avui = s.format(new Date());

        carregaLlista();
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
        Context context = view.getContext();
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        FragmentManager fragmentManager = getChildFragmentManager();
        adapter = new SimpleItemRecyclerViewAdapter(receptes, context);
        recyclerView.setAdapter(adapter);
        textViewNoResultats = view.findViewById(R.id.llista_no_resultats);
        preparaLlista();
    }

    private void carregaLlista() {
        receptes = new ArrayList<>();
        List<ItemLlistaCompra> items = ItemLlistaCompra.findWithQuery(ItemLlistaCompra.class, "Select id, recepta from item_llista_compra where data_adquisicio is null or data_adquisicio = ? order by recepta", avui);
        Long receptaId = 0L;
        for(ItemLlistaCompra item: items) {
            Recepta recepta = item.getRecepta();
            if(recepta != null) {
                if (!recepta.getId().equals(receptaId)) {
                    receptaId = recepta.getId();
                    receptes.add(recepta);
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
            textViewNoResultats.setText(R.string.no_receptes_llista_compra);
            textViewNoResultats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<Recepta> mValues;
        Context mContext;

        SimpleItemRecyclerViewAdapter(List<Recepta> items, Context context) {
            this.mValues = items;
            this.mContext = context;
        }

        public void setItems(List<Recepta> items) {
            mValues = items;
            notifyDataSetChanged();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_llista_base_element_frame, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mNomView.setText(holder.mItem.getNom());

            Imatge imatgePrincipal = holder.mItem.loadImatgePrincipal();
            if (imatgePrincipal != null) {
                Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(holder.mImatgeView);
            }

            LlistaCompraFragment oldFragment = (LlistaCompraFragment) fragmentManager.findFragmentById(holder.mFrameView.getId());
            if(oldFragment != null) {
                fragmentManager.beginTransaction().remove(oldFragment).commit();
            }

            int newViewId = (int) (SystemClock.currentThreadTimeMillis() * Math.random());
            holder.mView.setTag(R.id.viewid, newViewId);
            holder.mFrameView.setId(newViewId);
            holder.mView.setTag(R.id.viewid, holder.mFrameView.getId());


            if ((boolean) holder.mView.getTag(R.id.open)) {
                    holder.mFrameView.setVisibility(View.VISIBLE);
                    holder.mView.setTag(R.id.open, true);
            } else {
                    holder.mFrameView.setVisibility(View.GONE);
                    holder.mView.setTag(R.id.open, false);
            }

            /*
             *  Quan pitjem sobre la recepta carreguem un nou fragment del tipus LlistaCompraFragment que mostra
             *  els elements de la llista corresponents a aquesta recepta.
             */
            holder.mView.setTag(R.id.position, position);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int viewId = (int) v.getTag(R.id.viewid);
                    if((boolean) v.getTag(R.id.open)) {
                        v.setTag(R.id.open, false);
                        v.findViewById(viewId).setVisibility(View.GONE);
                    } else {
                        LlistaCompraFragment fragmentExistent = (LlistaCompraFragment) fragmentManager.findFragmentById(viewId);
                        if(fragmentExistent == null) {
                            Bundle arguments = new Bundle();
                            arguments.putLong(LlistaCompraFragment.RECEPTA_ID, holder.mItem.getId());
                            LlistaCompraFragment fragment = new LlistaCompraFragment();
                            fragment.setArguments(arguments);
                            fragmentManager.beginTransaction()
                                    .replace(viewId, fragment)
                                    .commit();
                        }
                        v.setTag(R.id.open, true);
                        v.findViewById(viewId).setVisibility(View.VISIBLE);
                        layoutManager.scrollToPosition((int)v.getTag(R.id.position));
                    }

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
            final FrameLayout mFrameView;

            Recepta mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mView.setTag(R.id.open, false);
                mImatgeView = view.findViewById(R.id.generic_list_element_imatge);
                mNomView = view.findViewById(R.id.generic_list_element_nom);
                mExtraView = view.findViewById(R.id.generic_list_element_extra);
                mExtraView.setVisibility(View.GONE);
                Object frameViewId = view.getTag(R.id.viewid);
                if (frameViewId == null) {
                    frameViewId = R.id.generic_list_element_frame;
                }
                mFrameView = view.findViewById((int) frameViewId);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNomView.getText() + "'";
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentManager = getChildFragmentManager();
    }
}
