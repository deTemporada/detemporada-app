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
import cat.detemporada.models.ItemLlistaCompra;
import cat.detemporada.models.ItemRebost;
import cat.detemporada.models.Producte;
import cat.detemporada.models.Unitat;

/*
 *  Fragment que mostra la vista per inserir els camps nom, quantitat i unitat quan
 *  afegim o editem un ingredient
 */

public class AfegirEditarItemLlistaFragment extends Fragment {

    public static final String ARG_PRODUCTE_ID = "producteId";
    public static final String ARG_ITEM_ID = "itemtId";
    public static final String ARG_TIPUS = "tipus";
    public static final String TIPUS_ITEM_REBOST = "ItemRebost";
    public static final String TIPUS_ITEM_LLISTA_COMPRA = "ItemLlistaCompra";

    private boolean itemPersonalitzat = false;
    private Producte producte;
    private ItemRebost itemRebost;
    private ItemLlistaCompra itemLlistaCompra;
    private float quantitat;
    private Unitat unitat;
    private boolean isEdit = false;
    private boolean isItemRebost = false;
    private EditText inputQuantitat;
    private Spinner llistaUnitats;
    private EditText nomIngredient;

    private OnItemRebostAfegitListener mListener;

    public AfegirEditarItemLlistaFragment() {
        // Constructor buit requerit per a que el Fragment Manager pugui instanciar
        // el fragment en cas de reorientació de la pantalla per exemple
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Long itemId = getArguments().getLong(ARG_ITEM_ID);
            String tipus = getArguments().getString(ARG_TIPUS);
            if(tipus != null) {
                if (tipus.equals(TIPUS_ITEM_REBOST)) {
                    isItemRebost = true;
                }
                if (itemId != 0) {
                    isEdit = true;
                    if (tipus.equals(TIPUS_ITEM_REBOST)) {
                        itemRebost = ItemRebost.findById(ItemRebost.class, itemId);
                        quantitat = itemRebost.getQuantitat();
                        unitat = itemRebost.getUnitat();
                        producte = itemRebost.getProducte();
                    } else if (tipus.equals(TIPUS_ITEM_LLISTA_COMPRA)) {
                        itemLlistaCompra = ItemLlistaCompra.findById(ItemLlistaCompra.class, itemId);
                        quantitat = itemLlistaCompra.getQuantitat();
                        unitat = itemLlistaCompra.getUnitat();
                        producte = itemLlistaCompra.getProducte();
                    }
                } else {
                    Long producteId = getArguments().getLong(ARG_PRODUCTE_ID);
                    producte = Producte.findById(Producte.class, producteId);
                }
                itemPersonalitzat = producte == null;
            }
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

        if(isEdit){
            if(isItemRebost) {
                nomIngredient.setText(itemRebost.getNom());
            } else {
                nomIngredient.setText(itemLlistaCompra.getNom());
            }
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
                    afegeixOEditaItem();
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    mListener.itemAfegitOEditat();
                }
            }
        });
    }

    private void afegeixOEditaItem() {
        float quantitat = inputQuantitat.getText().toString().equals("") ? 0 : Float.valueOf(inputQuantitat.getText().toString());
        String nom = nomIngredient.getText().toString();
        if(nom.equals("") && !itemPersonalitzat) {
            nom = producte.getNom();
        }
        if (isEdit) {
            if (isItemRebost) {
                itemRebost.setNom(nom);
                itemRebost.setQuantitat(quantitat);
                itemRebost.setUnitat((Unitat) llistaUnitats.getSelectedItem());
                itemRebost.save();
            } else {
                itemLlistaCompra.setNom(nom);
                itemLlistaCompra.setQuantitat(quantitat);
                itemLlistaCompra.setUnitat((Unitat) llistaUnitats.getSelectedItem());
                itemLlistaCompra.save();
            }
        } else {
            if (isItemRebost) {
                itemRebost = new ItemRebost(nom, producte, quantitat, (Unitat) llistaUnitats.getSelectedItem());
                itemRebost.save();
            } else {
                itemLlistaCompra = new ItemLlistaCompra(nom, producte, quantitat, (Unitat) llistaUnitats.getSelectedItem(), true);
                itemLlistaCompra.save();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemRebostAfegitListener) {
            mListener = (OnItemRebostAfegitListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " ha d'implementar OnItemRebostAfegitListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnItemRebostAfegitListener {
        void itemAfegitOEditat();
    }
}
