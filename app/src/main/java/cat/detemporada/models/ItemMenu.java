package cat.detemporada.models;

import com.orm.SugarRecord;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.orm.annotation.Ignore;


public class ItemMenu extends SugarRecord {
    public enum Apat {
        DINAR("Dinar", 0),
        SOPAR("Sopar", 1);

        private String stringValue;
        int intValue;
        Apat(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    private Recepta recepta;
    private String data;
    private Apat apat;

    @Ignore
    private SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    public ItemMenu() {}

    public ItemMenu(Recepta recepta, Date data, Apat apat) {
        this.recepta = recepta;
        this.data = s.format(data);
        this.apat = apat;
    }

    public Recepta getRecepta() {
        return recepta;
    }

    public void setRecepta(Recepta recepta) {
        this.recepta = recepta;
    }

    public String getData() {
        return data;
    }

    public Date getDataAsDate() { return  s.parse(data,new ParsePosition(0));}

    public void setData(Date data) {
        this.data = s.format(data);
    }

    public Apat getApat() {
        return apat;
    }

    public void setApat(Apat apat) {
        this.apat = apat;
    }
}
