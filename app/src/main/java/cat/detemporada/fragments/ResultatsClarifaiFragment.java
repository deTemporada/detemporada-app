package cat.detemporada.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cat.detemporada.BuildConfig;
import cat.detemporada.R;
import cat.detemporada.helpers.Utils;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Producte;
import cat.detemporada.models.ResultatClarifai;
import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

/**
 * Fragment que mostra una llista de productes
 */
public class ResultatsClarifaiFragment extends Fragment {

    public static final String IMATGE = "imatge";

    private List<ResultatClarifai> resultatsClarifai = new ArrayList<>();
    private ProgressBar progressBar;
    private Button botoCercar;
    private TextView textNoResultats;

    private OnResultatsClarifaiAcceptedListener mListener;

   SimpleItemRecyclerViewAdapter adapter;

    public ResultatsClarifaiFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String pathImatge = getArguments().getString(IMATGE);
            if (!pathImatge.isEmpty()) {
                if(Utils.redimensionaImatgeClarifai(new File(pathImatge), 512, 512)) {
                    new cridaAPIClarifaiAsincrona().execute(pathImatge);
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflem el layout per al fragment
        return inflater.inflate(R.layout.fragment_llista_clarifai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        botoCercar = view.findViewById(R.id.llista_cercar);
        botoCercar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnResultatsClarifaiAccepted(resultatsClarifai);
            }
        });
        textNoResultats = view.findViewById(R.id.llista_no_resultats);
        progressBar = view.findViewById(R.id.llista_progress);
        RecyclerView recyclerView = view.findViewById(R.id.llista_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SimpleItemRecyclerViewAdapter(resultatsClarifai);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultatsClarifaiAcceptedListener) {
            mListener = (OnResultatsClarifaiAcceptedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnResultatsClarifaiAcceptedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
     *  Aquesta classe crea un nou client Clarifai i envia l'imatge a la API. Com que la resposta pot trigar s'executa de manera
     *  asíncrona i es mostra un progressBar mentre s'espera resposta.
     */
    private class cridaAPIClarifaiAsincrona extends AsyncTask<String, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>> {
        @Override protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(String... params) {
            String pathImatge = params[0];
            final ClarifaiClient client = new ClarifaiBuilder(BuildConfig.APP_KEY_1).buildSync();
            Model<Concept> foodModel = client.getDefaultModels().foodModel();
            File imatge = new File(pathImatge);
            PredictRequest<Concept> request = foodModel.predict().withInputs(
                    ClarifaiInput.forImage(imatge)
            );
            boolean delete = imatge.delete();
            return request.executeSync();
        }

        @Override protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {
            if (!response.isSuccessful()) {
                textNoResultats.setVisibility(View.VISIBLE);
                textNoResultats.setText(String.format(getResources().getString(R.string.clarifai_error), response.responseCode()));
                botoCercar.setText(R.string.clarifai_nova_foto);
            } else {
                final List<ClarifaiOutput<Concept>> predictions = response.get();
                if (predictions.isEmpty()) {
                    textNoResultats.setVisibility(View.VISIBLE);
                    botoCercar.setText(R.string.clarifai_nova_foto);
                } else {
                    boolean producteTrobat = false;
                    for (Concept resultat : predictions.get(0).data()) {
                        List<Producte> productes = Producte.findWithQuery(Producte.class, "select id, nom from producte where nom_clarifai like ?", resultat.name());
                        if (productes.size() > 0) {
                            producteTrobat = true;
                            for (Producte producte: productes) {
                                resultatsClarifai.add(new ResultatClarifai(producte, resultat.value()));
                                adapter.setItems(resultatsClarifai);
                            }
                        }
                    }
                    if (!producteTrobat) {
                        textNoResultats.setVisibility(View.VISIBLE);
                        botoCercar.setText(R.string.clarifai_nova_foto);
                    }
                }
            }
            progressBar.setVisibility(View.GONE);
        }
    }


    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<ResultatClarifai> mValues;

        SimpleItemRecyclerViewAdapter(List<ResultatClarifai> items) {
            mValues = items;
        }

        private void setItems(List<ResultatClarifai> items) {
            mValues = items;
            notifyDataSetChanged();
        }


        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_llista_clarifai_element, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

            holder.mItem = mValues.get(position);
            Producte producte = holder.mItem.getProducte();

            holder.mCheckboxView.setTag(position);
            holder.mCheckboxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    toggleResultatSeleccionat((int) buttonView.getTag());
                }
            });

            holder.mNomView.setText(producte.getNom());

            float estimacio100 = holder.mItem.getEstimacio()*100;
            String percentatge = String.valueOf((int) estimacio100).concat(" %");
            holder.mExtraView.setText(percentatge);

            Imatge imatgePrincipal = producte.loadImatgePrincipal();
            if (imatgePrincipal != null) {
                Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(holder.mImatgeView);
            } else {
                Picasso.get().load(getResources().getString(R.string.placeholder_url)).fit().into(holder.mImatgeView);
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

            ResultatClarifai mItem;

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

    private void toggleResultatSeleccionat(int posicio) {
        ResultatClarifai item = resultatsClarifai.get(posicio);
        item.setSeleccionat(!item.isSeleccionat());
    }

    public interface OnResultatsClarifaiAcceptedListener {
        void OnResultatsClarifaiAccepted(List<ResultatClarifai> resultats);
    }
}
