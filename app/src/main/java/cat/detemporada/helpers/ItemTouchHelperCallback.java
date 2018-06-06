package cat.detemporada.helpers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cat.detemporada.fragments.LlistaItemMenuFragment;


/*
 * El ItemTouchHelper és la classe que s'encarrega de detectar els gestos sobre una vista. Implementant
 * el nostre propi Callback podem modificar l'UI cada cop que ItemTouchHelper detecta un gest (ja que aquest crida el nostre callback)
 */

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private final Paint mPaint = new Paint();

    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int swipeFlags = viewHolder instanceof LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolderAmbData ? 0 : ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).esUnItemNou() ? ItemTouchHelper.START | ItemTouchHelper.END : ItemTouchHelper.START;
        return makeMovementFlags(0, swipeFlags);
    }


    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View primerPla = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mPrimerPla;
        getDefaultUIUtil().clearView(primerPla);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View primerPla = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mPrimerPla;
        getDefaultUIUtil().onDraw(c, recyclerView, primerPla, dX, dY, actionState, isCurrentlyActive);

        TextView textViewDesfes = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mDesfesText;
        ImageView imageViewDesfes = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mDesfesIcona;

        TextView textViewNou = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mNouSuggerimentText;
        ImageView imageViewNou = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mNouSuggerimentIcona;

        if(dX > 0) {
            textViewDesfes.setVisibility(View.VISIBLE);
            imageViewDesfes.setVisibility(View.VISIBLE);
            textViewNou.setVisibility(View.INVISIBLE);
            imageViewNou.setVisibility(View.INVISIBLE);
        } else {
            textViewDesfes.setVisibility(View.INVISIBLE);
            imageViewDesfes.setVisibility(View.INVISIBLE);
            textViewNou.setVisibility(View.VISIBLE);
            imageViewNou.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View primerPla = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mPrimerPla;
        getDefaultUIUtil().onDraw(c, recyclerView, primerPla, dX, dY, actionState, isCurrentlyActive);

        TextView textViewDesfes = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mDesfesText;
        ImageView imageViewDesfes = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mDesfesIcona;

        TextView textViewNou = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mNouSuggerimentText;
        ImageView imageViewNou = ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mNouSuggerimentIcona;

        if(dX > 0) {
            textViewDesfes.setVisibility(View.VISIBLE);
            imageViewDesfes.setVisibility(View.VISIBLE);
            textViewNou.setVisibility(View.INVISIBLE);
            imageViewNou.setVisibility(View.INVISIBLE);
        } else {
            textViewDesfes.setVisibility(View.INVISIBLE);
            imageViewDesfes.setVisibility(View.INVISIBLE);
            textViewNou.setVisibility(View.VISIBLE);
            imageViewNou.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mDesfesText.setVisibility(View.INVISIBLE);
        ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mDesfesIcona.setVisibility(View.INVISIBLE);
        ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mNouSuggerimentText.setVisibility(View.INVISIBLE);
        ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mNouSuggerimentIcona.setVisibility(View.INVISIBLE);
        ((LlistaItemMenuFragment.SimpleItemRecyclerViewAdapter.ViewHolder) viewHolder).mProgressBar.setVisibility(View.VISIBLE);

        if(direction == ItemTouchHelper.START) {
            mAdapter.nouMenu(viewHolder.getAdapterPosition());
        } else if (direction == ItemTouchHelper.END) {
            mAdapter.desfesNouMenu(viewHolder.getAdapterPosition());
        }
    }

    public interface ItemTouchHelperAdapter {
        void nouMenu(int position);
        void desfesNouMenu(int position);
    }
}
