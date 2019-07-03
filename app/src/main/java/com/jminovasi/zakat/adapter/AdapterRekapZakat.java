package com.jminovasi.zakat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jminovasi.zakat.R;
import com.jminovasi.zakat.model.ModelRekapZakat;

import java.util.List;

public class AdapterRekapZakat extends RecyclerView.Adapter<AdapterRekapZakat.ViewHolder> {

    private Context mContext;
    private List<ModelRekapZakat> listZakat;

    public AdapterRekapZakat(Context mContext, List<ModelRekapZakat> listZakat) {
        this.mContext = mContext;
        this.listZakat = listZakat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_zakat, parent,false);

        ViewHolder vHolder = new ViewHolder(v);

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelRekapZakat modelRekapZakat = listZakat.get(position);

        String tipe = modelRekapZakat.getTipe();

        holder.tvItemAmilZ.setText(modelRekapZakat.getAmil());
        holder.tvItemAlamatZ.setText(modelRekapZakat.getAlamat());

        if(tipe.equals("beras")){

            holder.ivKategoriZ.setImageResource(R.drawable.ic_sack);
            holder.tvItemTanggalZ.setText(modelRekapZakat.getTanggal());
            holder.tvItemNamaZ.setText(modelRekapZakat.getMuzakki());
            holder.tvItemNominalZ.setText("Beras "+modelRekapZakat.getJumlah()+" Kg");

        }else if(tipe.equals("uang")){
            holder.ivKategoriZ.setImageResource(R.drawable.ic_bank);
            holder.tvItemTanggalZ.setText(modelRekapZakat.getTanggal());
            holder.tvItemNamaZ.setText(modelRekapZakat.getMuzakki());
            holder.tvItemNominalZ.setText("Rp "+modelRekapZakat.getJumlah());
        }
    }

    @Override
    public int getItemCount() {
        return listZakat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView ivKategoriZ;
        TextView tvItemNamaZ, tvItemNominalZ, tvItemTanggalZ, tvItemAmilZ, tvItemAlamatZ;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            ivKategoriZ = mView.findViewById(R.id.ivItemZakat);
            tvItemNamaZ = mView.findViewById(R.id.tvItemNamaZ);
            tvItemNominalZ = mView.findViewById(R.id.tvItemNominalZ);
            tvItemTanggalZ = mView.findViewById(R.id.tvItemTanggalZ);
            tvItemAmilZ = mView.findViewById(R.id.tvItemAmilZ);
            tvItemAlamatZ = mView.findViewById(R.id.tvItemAlamatZ);
        }
    }

}
