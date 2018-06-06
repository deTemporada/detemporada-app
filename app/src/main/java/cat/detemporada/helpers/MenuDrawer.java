package cat.detemporada.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.Toolbar;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import cat.detemporada.R;
import cat.detemporada.activities.ExploraActivity;
import cat.detemporada.activities.LlistaCompraActivity;
import cat.detemporada.activities.MainActivity;
import cat.detemporada.activities.MenuSetmanalActivity;
import cat.detemporada.activities.MevesReceptesActivity;
import cat.detemporada.activities.RebostActivity;

/*
 * Aquesta classe ens serveix per crear el calaix a cada activity
 */

public class MenuDrawer {

    private static PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.inici).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_home);
    private static PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.cerca).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_search);
    private static PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.menu_setmanal).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_restaurant);
    private static PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.llista_compra).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_shopping_cart);
    private static PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.rebost).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_shopping_basket);
    private static PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(6).withName(R.string.explora).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_explore);
    private static PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(7).withName(R.string.meves_receptes).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_bookmark_border);

    public static Drawer createMenu(Context context, final Activity activity, Toolbar toolbar, int selected, Bundle savedInstance) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withHeader(inflater.inflate(R.layout.drawer_header, null))
                .withCloseOnClick(true)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        item4,
                        item5,
                        item6,
                        item7
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Context context = view.getContext();
                        Intent intent = new Intent();
                        switch (position) {
                            case 1:
                                intent.setClass(context, MainActivity.class);
                                context.startActivity(intent);
                                break;
                            case 2:
                                if(context instanceof MainActivity) {
                                    ((MainActivity) context).toggleCerca(true);
                                } else {
                                    intent.setClass(context, MainActivity.class);
                                    intent.setAction(Intent.ACTION_SEARCH);
                                    context.startActivity(intent);
                                }
                                break;
                            case 3:
                                intent.setClass(context, MenuSetmanalActivity.class);
                                context.startActivity(intent);
                                break;
                            case 4:
                                intent.setClass(context, LlistaCompraActivity.class);
                                context.startActivity(intent);
                                break;
                            case 5:
                                intent.setClass(context, RebostActivity.class);
                                context.startActivity(intent);
                                break;
                            case 6:
                                intent.setClass(context, ExploraActivity.class);
                                context.startActivity(intent);
                                break;
                            case 7:
                                intent.setClass(context, MevesReceptesActivity.class);
                                context.startActivity(intent);
                                break;
                        }
                        return false;
                    }
                })
                .withSelectedItem(selected)
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        activity.onBackPressed();
                        return true;
                    }
                })
                .withSavedInstance(savedInstance)
                .build();
    }
}
