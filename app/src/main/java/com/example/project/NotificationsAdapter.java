package com.example.project;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.Holder> {

    ArrayList<NotificationItem> list;
    SupabaseClient sb;

    public NotificationsAdapter(ArrayList<NotificationItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        sb = new SupabaseClient(parent.getContext());
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {

        NotificationItem n = list.get(position);

        h.tvTitle.setText(n.title);
        h.tvMessage.setText(n.message);

        h.btnDismiss.setOnClickListener(v -> {

            int safePosition = h.getAdapterPosition();
            if (safePosition == RecyclerView.NO_POSITION) return;

            String email = SupabaseSession.sessionEmail;

            sb.dismissUserNotification(n.id, email, new okhttp3.Callback() {

                @Override public void onFailure(okhttp3.Call call, java.io.IOException e) {}

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {

                    Activity a = (Activity) h.itemView.getContext();
                    a.runOnUiThread(() -> {

                        if (safePosition >= list.size()) return;

                        list.remove(safePosition);
                        notifyItemRemoved(safePosition);

                        if (safePosition < list.size()) {
                            notifyItemRangeChanged(safePosition, list.size() - safePosition);
                        }
                    });
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;
        ImageView btnDismiss;

        Holder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvNotiTitle);
            tvMessage = v.findViewById(R.id.tvNotiMsg);
            btnDismiss = v.findViewById(R.id.btnDismiss);
        }
    }
}
