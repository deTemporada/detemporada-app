package cat.detemporada.models;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;

import com.mikepenz.materialdrawer.model.BaseDescribeableDrawerItem;
import com.mikepenz.materialdrawer.model.BaseViewHolder;

import java.util.List;

import cat.detemporada.R;

/*
 *  Implementació pròpia d'un DrawerItem per al MaterialDrawer que ens permet afegir els botons
 *  per incloure/excloure un ingredient i escoltar els events associats.
 */

public class FilterDrawerItem extends BaseDescribeableDrawerItem<FilterDrawerItem, FilterDrawerItem.ViewHolder> {

    private boolean inclos = false;
    private boolean exclos = false;
    private View.OnClickListener onClickListener = null;

    public FilterDrawerItem withInclos(boolean inclos) {
        this.inclos = inclos;
        return this;
    }

    public FilterDrawerItem withExclos(boolean exclos) {
        this.exclos = exclos;
        return this;
    }

    public FilterDrawerItem withOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    private View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.filter_drawer_item;
    }

    @Override
    public void bindView(final ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        //bind the basic view parts
        bindViewHelper(viewHolder);

        // Controlem els botons per incloure/excloure l'item
        if(inclos) {
            viewHolder.botoInclou.setImageResource(R.drawable.ic_filtre_inclos_enabled);
            viewHolder.botoExclou.setImageResource(R.drawable.ic_filtre_exclos_disabled);
        } else if (exclos){
            viewHolder.botoInclou.setImageResource(R.drawable.ic_filtre_inclos_disabled);
            viewHolder.botoExclou.setImageResource(R.drawable.ic_filtre_exclos_enabled);
        } else {
            viewHolder.botoInclou.setImageResource(R.drawable.ic_filtre_inclos_disabled);
            viewHolder.botoExclou.setImageResource(R.drawable.ic_filtre_exclos_disabled);
        }
        viewHolder.botoInclou.setTag(this.getTag());
        viewHolder.botoInclou.setTag(R.id.inclos, this.inclos);
        viewHolder.botoInclou.setTag(R.id.exclos, this.exclos);
        viewHolder.botoInclou.setOnClickListener(getOnClickListener());

        viewHolder.botoExclou.setTag(this.getTag());
        viewHolder.botoExclou.setTag(R.id.inclos, this.inclos);
        viewHolder.botoExclou.setTag(R.id.exclos, this.exclos);
        viewHolder.botoExclou.setOnClickListener(getOnClickListener());

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, viewHolder.itemView);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    public static class ViewHolder extends BaseViewHolder {
        private AppCompatImageButton botoInclou;
        private AppCompatImageButton botoExclou;


        private ViewHolder(View view) {
            super(view);
            this.botoInclou = view.findViewById(R.id.filter_drawer_boto_inclou);
            this.botoExclou = view.findViewById(R.id.filter_drawer_boto_exclou);
        }
    }

    private View.OnClickListener checkedChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getOnClickListener() != null) {
                getOnClickListener().onClick(view);
            }
        }
    };
}