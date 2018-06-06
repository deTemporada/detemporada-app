package cat.detemporada.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import cat.detemporada.R;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.Producte;
import cat.detemporada.models.Unitat;

/*
 *  Fragment que mostra la vista per inserir els camps nom, quantitat i unitat quan
 *  afegim o editem un ingredient
 */

public class AfegirEditarIngredientFragment extends Fragment {

    public static final String ARG_PRODUCTE_ID = "producteId";
    public static final String ARG_ITEM_ID = "itemtId";

    private boolean itemPersonalitzat = false;
    private Producte producte;
    private Long producteId;
    private Long itemId;
    private float quantitat;
    private Unitat unitat;
    private boolean isEdit = false;
    private EditText inputQuantitat;
    private Spinner llistaUnitats;
    private EditText nomIngredient;

    private OnIngredientAfegitOEditatListener mListener;

    public AfegirEditarIngredientFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
            if (itemId != 0L) {
                isEdit = true;
                    Ingredient ingredient = Ingredient.findById(Ingredient.class, itemId);
                    quantitat = ingredient.getQuantitat();
                    unitat = ingredient.getUnitat();
                    producte = ingredient.getProducte();
            } else {
                producteId = getArguments().getLong(ARG_PRODUCTE_ID);
                producte = Producte.findById(Producte.class, producteId);
            }
            itemPersonalitzat = producte == null;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflem el layout per al fragment
        return inflater.inflate(R.layout.fragment_afegir_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button submitButton = view.findViewById(R.id.afegir_item_submit);
        llistaUnitats = view.findViewById(R.id.afegir_item_form_unitat);
        inputQuantitat = view.findViewById(R.id.afegir_item_form_quantitat);
        nomIngredient = view.findViewById(R.id.afegir_item_card_nom);
        ImageView mImatgeView = view.findViewById(R.id.afegir_item_card_imatge);

        ArrayAdapter<Unitat> arrayUnitats = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, Unitat.values());
        llistaUnitats.setAdapter(arrayUnitats);

        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if(itemPersonalitzat) {
            Picasso.get().load(getResources().getString(R.string.placeholder_url)).fit().into(mImatgeView);
            nomIngredient.requestFocus();
            if (imm != null) {
                imm.showSoftInput(nomIngredient, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            nomIngredient.setText(producte.getNom());
            Imatge imatgePrincipal = producte.loadImatgePrincipal();
            if (imatgePrincipal != null) {
                Picasso.get().load(imatgePrincipal.getPath()).fit().centerCrop().into(mImatgeView);
            } else {
                Picasso.get().load(getResources().getString(R.string.placeholder_url)).fit().into(mImatgeView);
            }
            inputQuantitat.requestFocus();
            if (imm != null) {
                imm.showSoftInput(inputQuantitat, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        if (isEdit) {
            if(!unitat.equals(Unitat.QS)) {
                inputQuantitat.setText(String.format(Locale.getDefault(), "%.0f", quantitat));
            }
            llistaUnitats.setSelection(arrayUnitats.getPosition(unitat));
            submitButton.setText(R.string.desar);
        } else if (!itemPersonalitzat){
            llistaUnitats.setSelection(arrayUnitats.getPosition(producte.getUnitatDefecte()));
        }


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nomIngredient.getText().toString().equals("") && itemPersonalitzat) {
                    nomIngredient.setError("Introdueix un nom");
                } else if(inputQuantitat.getText().toString().equals("") && !(llistaUnitats.getSelectedItem()).equals(Unitat.QS)) {
                    inputQuantitat.setError("Introdueix una quantitat");
                } else {
                    String nom = nomIngredient.getText().toString();
                    if(nom.equals("")) {
                        nom = producte.getNom();
                    }
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                    float quantitat = inputQuantitat.getText().toString().equals("") ? 0 : Float.valueOf(inputQuantitat.getText().toString());
                    mListener.itemAfegitOEditat(itemId, producteId, quantitat, (Unitat) llistaUnitats.getSelectedItem(), nom);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIngredientAfegitOEditatListener) {
            mListener = (OnIngredientAfegitOEditatListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnIngredientAfegitOEditatListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnIngredientAfegitOEditatListener {
        void itemAfegitOEditat(Long ingredientId, Long producteId, float quantitat, Unitat unitat, String nom);
    }
}
