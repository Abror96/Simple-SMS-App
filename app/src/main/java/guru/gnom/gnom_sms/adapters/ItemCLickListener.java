package guru.gnom.gnom_sms.adapters;

/**
 * Created by R Ankit on 25-12-2016.
 */
public interface ItemCLickListener {
    void itemClicked(int color, String contact, long id, String read, String name, String thread_id);
    //void itemLongClicked(int position,String contact,long id);
}
