package com.yasingok.dinosaurbook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yasingok.dinosaurbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class DinoAdapter extends RecyclerView.Adapter<DinoAdapter.DinoHolder> {

    ArrayList<Dinosaur> dinoArrayList;
    public DinoAdapter(ArrayList<Dinosaur> dinoArrayList){
        this.dinoArrayList = dinoArrayList;
    }

    @NonNull
    @Override
    public DinoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false);
        return new DinoHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DinoHolder holder, int position) {
        holder.binding.recycler.setText(String.valueOf(position+1) + ") " + dinoArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), Dino.class);
                intent.putExtra("info", "old");     // Eski veri olduğunu gösteren ilkel yol
                intent.putExtra("dinoId", dinoArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dinoArrayList.size();
    }

    public class DinoHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public DinoHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
