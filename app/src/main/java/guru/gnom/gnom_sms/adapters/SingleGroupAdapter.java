package guru.gnom.gnom_sms.adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.amulyakhare.textdrawable.TextDrawable;

import java.util.Arrays;

import guru.gnom.gnom_sms.R;
import guru.gnom.gnom_sms.utils.ColorGeneratorModified;
import guru.gnom.gnom_sms.utils.Helpers;

import static guru.gnom.gnom_sms.utils.Helpers.getDate;

/**
 * Created by R Ankit on 25-12-2016.
 */

public class SingleGroupAdapter extends RecyclerView.Adapter<SingleGroupAdapter.MyViewHolder> {

    private ColorGeneratorModified generator;
    private Context context;
    private Cursor dataCursor;
    private int color;
    private long old_date = 0;

    public SingleGroupAdapter(Context context, Cursor dataCursor, int color) {

        this.context = context;
        this.dataCursor = dataCursor;
        this.color = color;

        if (color == 0)
            generator = ColorGeneratorModified.MATERIAL;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.single_sms_detailed, parent, false);
        SingleGroupAdapter.MyViewHolder myHolder = new SingleGroupAdapter.MyViewHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        dataCursor.moveToPosition(position);
        holder.message.setText(dataCursor.getString(dataCursor.getColumnIndexOrThrow("body")));

        long time = dataCursor.getLong(dataCursor.getColumnIndexOrThrow("date"));

        if (position+1 < dataCursor.getCount()) {
            dataCursor.moveToPosition(position+1);
            if (!getDate(dataCursor.getLong(dataCursor.getColumnIndexOrThrow("date"))).equals(getDate(time))) {
                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(getDate(time));
            } else {
                holder.time.setVisibility(View.GONE);
            }
            dataCursor.moveToPosition(position);
        } else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(getDate(time));
        }

        String name = dataCursor.getString(dataCursor.getColumnIndexOrThrow("address"));

        int type = dataCursor.getInt(dataCursor.getColumnIndexOrThrow("type"));
        if (type == 1) {
            holder.message.setBackgroundResource(R.drawable.rounded_background);
            holder.message.setTextColor(Color.parseColor("#212121"));
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.START;
            holder.message.setLayoutParams(layoutParams);
        } else {
            holder.message.setBackgroundResource(R.drawable.rounded_background_my);
            holder.message.setTextColor(Color.parseColor("#ffffff"));
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.END;
            holder.message.setLayoutParams(layoutParams);
        }
        if (type == 5) {
            holder.error.setVisibility(View.VISIBLE);
        } else {
            holder.error.setVisibility(View.GONE);
        }
        holder.message.setPadding((int)context.getResources().getDimension(R.dimen.activity_horizontal_margin), (int)context.getResources().getDimension(R.dimen.activity_horizontal_margin),(int)context.getResources().getDimension(R.dimen.activity_horizontal_margin),(int)context.getResources().getDimension(R.dimen.activity_horizontal_margin));

        if (color == 0){
            if (generator!=null)
                color = generator.getColor(name);
        }

//        old_date = dataCursor.getLong(dataCursor.getColumnIndexOrThrow("date"));

//        holder.frameLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (type == 5) {
//                    Toast.makeText(context, "resend", Toast.LENGTH_LONG).show();
//                }
//            }
//        });

    }

    public Cursor swapCursor(Cursor cursor) {
        if (dataCursor == cursor) {
            return null;
        }
        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView message;
        private TextView time;
        private FrameLayout frameLayout;
        ImageView error;

        public MyViewHolder(View itemView) {
            super(itemView);

            message = (TextView) itemView.findViewById(R.id.message);
            time = (TextView) itemView.findViewById(R.id.time);
            frameLayout = itemView.findViewById(R.id.frame);
            error = itemView.findViewById(R.id.error);

        }

    }
}
