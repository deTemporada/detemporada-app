package cat.detemporada.helpers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cat.detemporada.models.CategoriaProducte;
import cat.detemporada.models.CategoriaRecepta;
import cat.detemporada.models.Imatge;
import cat.detemporada.models.Ingredient;
import cat.detemporada.models.ItemLlistaCompra;
import cat.detemporada.models.ItemMenu;
import cat.detemporada.models.ItemRebost;
import cat.detemporada.models.PasRecepta;
import cat.detemporada.models.Producte;
import cat.detemporada.models.Recepta;
import cat.detemporada.models.Temporada;
import cat.detemporada.models.Unitat;

/*
 *  La classe Utils conté els mètodes genèrics que es fan servir al llarg de l'aplicació
 */


public class Utils {

    private final static String TAG = "Utils";

    // Definim dos SimpleDateFormat per a formatar dates segons per als ItemMenu i l'emmagatzemament d'imatges
    private static SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    private static SimpleDateFormat s2 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    /*
     *  Mètode que afegeix tots els ingredients d'una recepta de la llista de la compra
     */
    public static void afegeixIngredientsALlistaCompra(List<Ingredient> ingredients) {
        for(Ingredient ingredient: ingredients) {
            ItemLlistaCompra itemLlistaCompra = new ItemLlistaCompra(ingredient);
            itemLlistaCompra.save();
        }
    }

    /*
     *  Mètode que elimina tots els ingredients d'una recepta de la llista de la compra
     */
    public static void eliminaIngredientsDeLlistaCompra(Long receptaId) {
        ItemLlistaCompra.deleteAll(ItemLlistaCompra.class, "recepta = ?", receptaId.toString());
    }

    /*
     *  Mñetode que comprova si els ingredients d'una recepta es troben a la llista de la compra
     */
    public static boolean receptaInLlistaCompra(Long receptaId) {
        Long itemsCount = ItemLlistaCompra.count(ItemLlistaCompra.class, "recepta = ?", receptaId.toString());
        return itemsCount > 0;
    }

    /*
     *  Mètode que afegeix una recepta al menu d'una data concreta
     */
    public static void afegeixReceptaAMenu(Recepta recepta, Date date, boolean dinar) {
        ItemMenu.Apat apat = dinar ? ItemMenu.Apat.DINAR : ItemMenu.Apat.SOPAR;
        Iterator<ItemMenu> existingMenus = ItemMenu.findWithQueryAsIterator(ItemMenu.class, "select id from item_menu where data = ? and apat = ? collate nocase limit 1", s.format(date), apat.toString());
        ItemMenu itemMenu = new ItemMenu(recepta, date, apat);
        if(existingMenus.hasNext()) {
            ItemMenu menuExistent = existingMenus.next();
            itemMenu.setId(menuExistent.getId());
        }
        itemMenu.save();
    }

    /*
     *  Mètode que comprova si una recepta ha estat afegida al menu d'una data futura
     */
    public static boolean receptaInFutureMenu(Long receptaId, Date date) {
        Iterator<ItemMenu> existingMenu = ItemMenu.findWithQueryAsIterator(ItemMenu.class, "select id from item_menu where data >= ? and recepta = ? limit 1", s.format(date), receptaId.toString());
        return existingMenu.hasNext();
    }

    /*
     *  Mètode que crea un arxiu a la memòria externa del dispositiu (per emmagatzemar imatges de l'usuari)
    */
    public static File creaFileImatge() {
        String timeStamp = s2.format(new Date());
        String imageFileName = "deTemporada_" + timeStamp + ".jpeg";
        File imageFile = new File(new File(Environment.getExternalStorageDirectory(), "deTemporada"), imageFileName);
        boolean mkdirs = imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    /*
     *  Mètode que crea un arxiu a la memòria interna del dispositiu (per emmagatzemar imatges de productes i receptes al dispositiu)
     */
    private static File creaFileImatgeIntern(Context context, String nom) {
        String timeStamp = s2.format(new Date());
        String imageFileName = "deTemporada_" + timeStamp + nom +".jpeg";
        File imageFile = new File(new File(context.getFilesDir(), "imatges"), imageFileName);
        boolean mkdirs = imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    /*
     *  Mètode que carrega una Uri i retorna el Bitmap corresponent
     */
    private static Bitmap bitmapFromUri(Context context, Uri uri) {

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, null);
    }

    /*
     * Mètode que emmagatzema un File intern a partir d'una Uri
     */
    public static File salvaImatgeDeUri(Context context, Uri uri) {
        Bitmap bitmap = bitmapFromUri(context, uri);
        File file = creaFileImatgeIntern(context, uri.getLastPathSegment());
        return bitmapToFile(bitmap, file) ? file : null;
    }

    /*
     *  Mètode que emmagatzema un Bitmap en un arxiu.
     */
    private static boolean bitmapToFile(Bitmap bitmap, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     *  Mètode que redimensiona una imatge a partir d'un arxiu i l'emmagatzema cridant el mètode bitmapToFile.
     *  Es fa servir per reduir la mida de les imatges fetes amb el dispositiu abans d'enviar-les a la API de Clarifai
     *  i així reduir el temps de la petició POST i agilitzar la detecció d'imatges.
     */
    public static boolean redimensionaImatgeClarifai(File imatge, int width, int height) {
        BitmapFactory.Options opcions = new BitmapFactory.Options();

        // Aquesta opció de BitmapFactory ens permet accedir al bitmap sense haver-li d'assignar un lloc a memòria
        opcions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imatge.getPath(), opcions);
        int originalWidth = opcions.outWidth;
        int originalHeight = opcions.outHeight;

        int scaleFactor = 1;
        if ((width > 0) || (width > 0)) {
            scaleFactor = Math.min(originalWidth/width, originalHeight/height);
        }

        opcions.inJustDecodeBounds = false;
        opcions.inSampleSize = scaleFactor;

        // Deprecat a API 21 pel que s'ignora.
        // API < 21 permet al sistema eliminar el bitmap de la memòria si necessita espai.
        opcions.inPurgeable = true;

        Bitmap bitmap =  BitmapFactory.decodeFile(imatge.getPath(), opcions);
        return bitmapToFile(bitmap, imatge);
    }


    /*
     *  Mètode que retorna a partir d'una variable de tipus Date retorna la mateixa però establint els camps d'hora, minut,
     *  segon i milisegon a 0 per així poder comparar dies sense tenir en compte el factor temps
     */
    public static Date dataSenseTemps(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /*
     *  Mètode que genera nous ItemMenu si l'últim ItemMenu a la base de dades no és el de d'aqui una setmana
     */
    public static void generaNousMenus() {
        Iterator<ItemMenu> itemMenuIterator = ItemMenu.findWithQueryAsIterator(ItemMenu.class, "select id, data from item_menu order by data desc limit 1");
        Calendar inici = Calendar.getInstance();
        Date avui = new Date();
        if(itemMenuIterator.hasNext()){
            ItemMenu ultimItemMenu = itemMenuIterator.next();
            Date ultimaData = ultimItemMenu.getDataAsDate();
            inici.setTime(ultimaData.before(avui) ? avui : ultimaData);
            inici.add(Calendar.DATE, 1);
        } else {
            inici.setTime(avui);
        }
        Calendar fi = Calendar.getInstance();
        fi.setTime(avui);
        fi.add(Calendar.DATE, 7);
        Iterator<Recepta> receptaIterator = receptesSuggeridesIterator();
        for (Date data = inici.getTime(); inici.before(fi); inici.add(Calendar.DATE, 1), data = inici.getTime()) {
            if (receptaIterator.hasNext()) {
                Recepta recepta = receptaIterator.next();
                recepta.setValoracio(recepta.getValoracio() + 0.1f);
                recepta.save();
                afegeixReceptaAMenu(recepta, data, true);
            }
            if (receptaIterator.hasNext()) {
                Recepta recepta = receptaIterator.next();
                recepta.setValoracio(recepta.getValoracio() + 0.1f);
                recepta.save();
                afegeixReceptaAMenu(recepta, data, false);
            }
        }
    }

    /*
     *  Classe que exté AsyncTask per tal de calcular la temporalitat de les receptes de forma asíncrona.
     */
    public static class calculaTemporalitatAsincron extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Iterator<Recepta> receptaIterator = Recepta.findAll(Recepta.class);
            Date avui = new Date();
            while (receptaIterator.hasNext()) {
                Recepta recepta = receptaIterator.next();
                List<Ingredient> ingredients = recepta.loadIngredients();
                float totalIngredientsAmbProducte = 0;
                float totalIngredientsAmbProducteDeTemporada = 0;
                for(Ingredient ingredient: ingredients) {
                    Producte producte = ingredient.getProducte();
                    if (producte != null) {
                        totalIngredientsAmbProducte++;
                        List<Temporada> temporades = producte.loadTemporades();
                        for(Temporada temporada: temporades) {
                            if(temporada.esTemporada(avui)) {
                                totalIngredientsAmbProducteDeTemporada++;
                                break;
                            }
                        }
                    }
                }
                int indexTemporalitat = (int) (100f*(totalIngredientsAmbProducteDeTemporada / totalIngredientsAmbProducte));
                recepta.setIndexTemporalitat(indexTemporalitat);
                recepta.save();
            }
            return null;
        }
    }

    /*
     *  Mètode que retorna un Iterator de Receptes amb les receptes suggerides en base a la valoració de l'usuari i la temporalitat.
     *  Es fa servir per generar nous ItemMenu ja sigui perque manquen a la BBDD o perque l'usuari ha fet swipe en un ItemMenu que
     *  no li agrada.
     */
    public static Iterator<Recepta> receptesSuggeridesIterator() {
        String avui = s.format(new Date());
        return Recepta.findWithQueryAsIterator(Recepta.class, "select * from recepta r where r.esborrany = 0 and (r.categoria = 'PRIMER' or r.categoria = 'SEGON') and r.id not in (select recepta from item_menu where data >= ?) order by r.index_temporalitat desc, r.valoracio desc", avui);
    }

    /*
     *  Al no disposar d'un mètode genèric per fer un FLUSH de les taules de la BBDD cal cridar els mètodes deleteAll
     *  de SugarORM.
     */
    public static void flushBBDD() {
        Imatge.deleteAll(Imatge.class);
        Ingredient.deleteAll(Ingredient.class);
        ItemLlistaCompra.deleteAll(ItemLlistaCompra.class);
        ItemMenu.deleteAll(ItemMenu.class);
        ItemRebost.deleteAll(ItemRebost.class);
        PasRecepta.deleteAll(PasRecepta.class);
        Producte.deleteAll(Producte.class);
        Recepta.deleteAll(Recepta.class);
        Temporada.deleteAll(Temporada.class);
    }

    /*
     *  Aquest mètode s'encarrega de llegir un arxiu JSON dels resources i emmagatzemar totes les dades a la
     *  BBDD local SQLite. Això s'ha implementat per falta d'una API online.
     */
    public static void llegeixJSON(InputStream rawReceptes) throws IOException {
        JsonReader reader  = new JsonReader(new InputStreamReader(rawReceptes, "UTF-8"));
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "receptes":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        Recepta recepta = new Recepta();
                        List<PasRecepta> instruccions = new ArrayList<>();
                        List<Ingredient> ingredients = new ArrayList<>();
                        Imatge imatgePrincipal = new Imatge();
                        while (reader.hasNext()) {
                            String keyRecepta = reader.nextName();
                            if (keyRecepta.equalsIgnoreCase("nom")) {
                                String nomRecepta = reader.nextString();
                                recepta.setNom(nomRecepta);
                            } else if (keyRecepta.equalsIgnoreCase(("categoria"))) {
                                try {
                                    CategoriaRecepta categoria = CategoriaRecepta.fromString(reader.nextString());
                                    recepta.setCategoria(categoria);
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                    recepta.setCategoria(CategoriaRecepta.PRIMER);
                                }
                            } else if (keyRecepta.equalsIgnoreCase(("dificultat"))) {
                                try {
                                    Recepta.Dificultat grauDificultat = Recepta.Dificultat.fromString(reader.nextString());
                                    recepta.setDificultat(grauDificultat);
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                    recepta.setDificultat(Recepta.Dificultat.MITJANA);
                                }
                            } else if (keyRecepta.equalsIgnoreCase("descripcio")) {
                                recepta.setDescripcio(reader.nextString());
                            } else if (keyRecepta.equalsIgnoreCase("valoracio")) {
                                recepta.setValoracio((float) reader.nextDouble());
                            } else if (keyRecepta.equalsIgnoreCase("persones")) {
                                recepta.setPersones(Integer.valueOf(reader.nextString()));
                            } else if (keyRecepta.equalsIgnoreCase("temps")) {
                                recepta.setTemps(Integer.valueOf(reader.nextString()));
                            } else if (keyRecepta.equalsIgnoreCase(("imatge"))) {
                                imatgePrincipal.setOriginal(reader.nextString());
                                imatgePrincipal.setPrincipal(true);
                            } else if (keyRecepta.equalsIgnoreCase(("ingredients"))) {
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    Ingredient ingredient = new Ingredient();
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        String keyIngredient = reader.nextName();
                                        if (keyIngredient.equalsIgnoreCase("nom")) {
                                            ingredient.setNom(reader.nextString());
                                        } else if (keyIngredient.equalsIgnoreCase(("producte"))) {
                                            List<Producte> productes = Producte.find(Producte.class, "nom = ?", reader.nextString());
                                            if (!productes.isEmpty()) {
                                                ingredient.setProducte(productes.get(0));
                                            }
                                        } else if (keyIngredient.equalsIgnoreCase(("quantitat"))) {
                                            ingredient.setQuantitat(Float.valueOf(reader.nextString()));
                                        } else if (keyIngredient.equalsIgnoreCase(("unitat"))) {
                                            try {
                                                Unitat unitat = Unitat.fromString(reader.nextString());
                                                ingredient.setUnitat(unitat);
                                            } catch (IllegalArgumentException e) {
                                                Log.e(TAG, e.getLocalizedMessage());
                                                ingredient.setUnitat(Unitat.GRAMS);
                                            }
                                        }
                                    }
                                    ingredients.add(ingredient);
                                    reader.endObject();
                                }
                                reader.endArray();
                            } else if (keyRecepta.equalsIgnoreCase(("preparacio"))) {
                                int posicio = 1;
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    PasRecepta pasRecepta = new PasRecepta();
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        String keyIngredient = reader.nextName();
                                        if (keyIngredient.equalsIgnoreCase("pas")) {
                                            pasRecepta.setPas(reader.nextString());
                                        } else if (keyIngredient.equalsIgnoreCase(("imatge"))) {
                                            reader.skipValue();
                                        } else {
                                            reader.skipValue();
                                        }
                                    }
                                    reader.endObject();
                                    pasRecepta.setPosicio(posicio);
                                    posicio++;
                                    instruccions.add(pasRecepta);
                                }
                                reader.endArray();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        recepta.setEsborrany(false);
                        recepta.setUsuari(false);
                        recepta.save();

                        imatgePrincipal.setRecepta(recepta);
                        imatgePrincipal.save();

                        for (Ingredient ingredient : ingredients) {
                            ingredient.setRecepta(recepta);
                            ingredient.save();
                        }

                        for (PasRecepta pas : instruccions) {
                            pas.setRecepta(recepta);
                            pas.save();
                        }
                    }
                    reader.endArray();
                    break;
                case "productes":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        Producte producte = new Producte();
                        List<Temporada> temporades = new ArrayList<>();
                        Imatge imatgePrincipal = new Imatge();
                        while (reader.hasNext()) {
                            String keyProducte = reader.nextName();
                            if (keyProducte.equalsIgnoreCase("nom")) {
                                producte.setNom(reader.nextString());
                            } else if (keyProducte.equalsIgnoreCase("descripcio")) {
                                producte.setDescripcio(reader.nextString());
                            } else if (keyProducte.equalsIgnoreCase(("categoria"))) {
                                try {
                                    CategoriaProducte categoria = CategoriaProducte.fromString(reader.nextString());
                                    producte.setCategoria(categoria);
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                }
                            } else if (keyProducte.equalsIgnoreCase(("unitatDefecte"))) {
                                try {
                                    Unitat unitat = Unitat.fromString(reader.nextString());
                                    producte.setUnitatDefecte(unitat);
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                    producte.setUnitatDefecte(Unitat.GRAMS);
                                }
                            } else if (keyProducte.equalsIgnoreCase(("imatge"))) {
                                imatgePrincipal.setOriginal(reader.nextString());
                                imatgePrincipal.setPrincipal(true);
                            } else if (keyProducte.equalsIgnoreCase(("temporades"))) {
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    Temporada temporada = new Temporada();
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        String keyTemporada = reader.nextName();
                                        if (keyTemporada.equalsIgnoreCase("inici")) {
                                            temporada.setIniciString(reader.nextString());
                                        } else if (keyTemporada.equalsIgnoreCase(("fi"))) {
                                            temporada.setFiString(reader.nextString());
                                        }

                                    }
                                    temporades.add(temporada);
                                    reader.endObject();
                                }
                                reader.endArray();
                            } else if (keyProducte.equalsIgnoreCase("nomClarifai")) {
                                producte.setNomClarifai(reader.nextString());
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        producte.save();

                        imatgePrincipal.setProducte(producte);
                        imatgePrincipal.save();

                        for (Temporada temporada : temporades) {
                            try {
                                boolean atemporal = temporada.checkAtemporal();
                            } catch (IllegalArgumentException e) {
                                temporada.setAtemporal(false);
                            }
                            temporada.setProducte(producte);
                            temporada.save();
                        }
                    }
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }
}
