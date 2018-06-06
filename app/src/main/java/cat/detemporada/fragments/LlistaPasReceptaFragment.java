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
import cat.detemporada.models.PasRecepta;

/**
 * Fragment que mostra una llista de passos de preparació de recepta
 */
public class LlistaPasReceptaFragment extends Fragment {
    public static final String RECEPTA_ID = "receptaId";

    private List<PasRecepta> passos = new ArrayList<>();

    private OnPasReceptaSelectedListener mListener;

    private SimpleItemRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private Context context;

    public LlistaPasReceptaFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Long receptaId = getArguments().getLong(RECEPTA_ID);
            if (receptaId != 0) {
                passos = PasRecepta.findWithQuery(PasRecepta.class, "select * from pas_recepta where recepta = ? order by posicio asc", receptaId.toString());
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
        recyclerView = view.findViewById(R.id.llista_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SimpleItemRecyclerViewAdapter(passos);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPasReceptaSelectedListener) {
            mListener = (OnPasReceptaSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnPasReceptaSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setPassos(List<PasRecepta> passos) {
        if (adapter == null) {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new SimpleItemRecyclerViewAdapter(passos);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(passos);
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<PasRecepta> mValues;

        SimpleItemRecyclerViewAdapter(List<PasRecepta> items) {
            mValues = items;
        }

        public void setItems(List<PasRecepta> items) {
            mValues = items;
            notifyDataSetChanged();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_pas_recepta, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mPasView.setText(holder.mItem.getPas());

            Imatge imatge = holder.mItem.getImatge();
                if (imatge != null) {
                    Picasso.get().load(imatge.getPath()).fit().centerCrop().into(holder.mImatgeView);
                } else {
                    holder.mImatgeView.setVisibility(View.GONE);
                }

            if (context instanceof AfegirEditarReceptaActivity) {
                holder.mView.setTag(R.id.position, position);
                holder.mView.setTag(R.id.object, holder.mItem.getId());
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
                                        mListener.onEditPasReceptaSelected(itemId, position);
                                        break;
                                    case 1:
                                        mListener.onDeletePasReceptaSelected(position);
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
            final TextView mPasView;

            PasRecepta mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImatgeView = view.findViewById(R.id.pas_recepta_imatge);
                mPasView = view.findViewById(R.id.pas_recepta);
            }
        }

    }

    public interface OnPasReceptaSelectedListener {
        void onEditPasReceptaSelected(Long itemId, int posicio);
        void onDeletePasReceptaSelected(int posicio);

    }
}
