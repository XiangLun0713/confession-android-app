package me.xianglun.confession_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.model.AdminModel;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    private final Context context;
    private final ArrayList<AdminModel> adminList;

    public AdminAdapter(Context context, ArrayList<AdminModel> adminList) {
        this.context = context;
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the view holder
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.template_admin, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        AdminModel admin = adminList.get(position);
        holder.textView.setText(admin.getUsername().concat(" - ").concat(admin.getEmail()));
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }


    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.admin_username);
        }
    }
}
