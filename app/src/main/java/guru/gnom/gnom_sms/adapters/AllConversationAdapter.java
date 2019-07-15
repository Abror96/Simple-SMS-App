package guru.gnom.gnom_sms.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.amulyakhare.textdrawable.TextDrawable;

import java.util.List;

import guru.gnom.gnom_sms.R;
import guru.gnom.gnom_sms.SMS;
import guru.gnom.gnom_sms.utils.ColorGeneratorModified;
import guru.gnom.gnom_sms.utils.Helpers;

/**
 * Created by R Ankit on 25-12-2016.
 */

public class AllConversationAdapter extends RecyclerView.Adapter<AllConversationAdapter.MyHolder> {

    private Context context;
    private List<SMS> data;
    private ItemCLickListener itemClickListener;
    ColorGeneratorModified generator = ColorGeneratorModified.MATERIAL;


    public AllConversationAdapter(Context context, List<SMS> data) {
        this.context = context;
        this.data = data;

    }

    @Override
    public AllConversationAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.single_sms_small_layout, parent, false);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(AllConversationAdapter.MyHolder holder, int position) {

        SMS SMS = data.get(position);

        holder.senderContact.setText(SMS.getName());
        holder.message.setText(SMS.getMsg());

        int color = generator.getColor(SMS.getAddress());
        String firstChar = String.valueOf(SMS.getAddress().charAt(0));
        TextDrawable drawable = TextDrawable.builder().buildRound(firstChar, color);
        holder.senderImage.setImageDrawable(drawable);

        SMS.setColor(color);


        if (SMS.getReadState().equals("0")) {
            holder.isRead.setVisibility(View.VISIBLE);
        } else {
            holder.isRead.setVisibility(View.INVISIBLE);

        }

        holder.time.setText(Helpers.getDate(SMS.getTime()));

    }



    @Override
    public int getItemCount() {
        return (data == null) ? 0 : data.size();
    }


    public void setItemClickListener(ItemCLickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView senderImage;
        private ImageView isRead;
        private TextView senderContact;
        private TextView message;
        private TextView time;
        private LinearLayout mainLayout;

        public MyHolder(View itemView) {
            super(itemView);
            senderImage = (ImageView) itemView.findViewById(R.id.smsImage);
            isRead = itemView.findViewById(R.id.isRead);
            senderContact = (TextView) itemView.findViewById(R.id.smsSender);
            message = (TextView) itemView.findViewById(R.id.smsContent);
            time = (TextView) itemView.findViewById(R.id.time);
            mainLayout = (LinearLayout) itemView.findViewById(R.id.small_layout_main);

            mainLayout.setOnClickListener(this);
            mainLayout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {

                data.get(getAdapterPosition()).setReadState("1");
                notifyItemChanged(getAdapterPosition());

                itemClickListener.itemClicked(data.get(getAdapterPosition()).getColor(),
                        data.get(getAdapterPosition()).getAddress(),
                        data.get(getAdapterPosition()).getId(),
                        data.get(getAdapterPosition()).getReadState(),
                        senderContact.getText().toString(),
                        data.get(getAdapterPosition()).getThread_id());
            }

        }

        @Override
        public boolean onLongClick(View view) {

            String[] items = {"Delete"};

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context
                    , android.R.layout.simple_list_item_1, android.R.id.text1, items);

            new AlertDialog.Builder(context)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            deleteDialog();
                        }
                    })
                    .show();

            return true;
        }

        private void deleteDialog() {

            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setMessage("Are you sure you want to delete this message?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    deleteSMS(data.get(getAdapterPosition()).getId(), getAdapterPosition());

                }

            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
            alert.create();
            alert.show();
        }
    }

    public void deleteSMS(long messageId, int position) {

        long affected = context.getContentResolver().delete(
                Uri.parse("content://sms/" + messageId), null, null);

        if (affected != 0) {

            data.remove(position);
            notifyItemRemoved(position);

        }

    }
}
