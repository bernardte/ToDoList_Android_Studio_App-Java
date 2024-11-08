package student.inti.signuplogin.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.stream.Collectors;

import student.inti.signuplogin.DataClass;
import student.inti.signuplogin.R;

public class DisplayNote extends RecyclerView.Adapter<DisplayNote.MyViewHolder> {

    private ArrayList<DataClass> fullDataList;
    private ArrayList<DataClass> dataList;
    private Context context;

    public DisplayNote(ArrayList<DataClass> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
        this.fullDataList = new ArrayList<>(dataList); // Keep a copy of the original list
    }

    // Getter method for fullDataList
    public ArrayList<DataClass> getFullDataList() {
        return fullDataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataClass currentItem = dataList.get(position);

        // Load image using Glide
        Glide.with(context).load(currentItem.getImageURL()).into(holder.recyclerImage);
        holder.recyclerCaption.setText(currentItem.getCaption());

        // Set the delete button click listener
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get document ID and image URL
                String documentId = currentItem.getDocumentId();
                String imageUrl = currentItem.getImageURL();

                Log.d("DeleteOperation", "Document ID: " + documentId);
                Log.d("DeleteOperation", "Image URL: " + imageUrl);

                // Confirm deletion with the user
                new AlertDialog.Builder(context)
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // Proceed with deletion
                            deleteItem(documentId, imageUrl, holder);
                        })
                        .setNegativeButton(android.R.string.no, null) // Do nothing on "No"
                        .show();
            }
        });
    }

    // Method to delete the item from Firestore and Storage
    private void deleteItem(String documentId, String imageUrl, MyViewHolder holder) {
        FirebaseFirestore.getInstance().collection("Images").document(documentId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Document successfully deleted
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);

                        // Delete the image from Firebase Storage
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Successfully delete note.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("StorageError", "Error deleting image: ", e);
                                Toast.makeText(context, "Failed to delete note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirestoreError", "Error deleting document: ", e);
                        Toast.makeText(context, "Failed to delete document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //filter for searching caption
    public void filterImageNote(String query) {
        //if copy list is empty return null
        if (fullDataList == null) {
            // Handle case where fullDataList is not initialized
            return;
        }

        // Initialize filteredList based on query
        ArrayList<DataClass> filteredList;
        // Check if the user input is not empty
        if (!query.isEmpty()) {
            // Stream filter based on the caption, ignoring space
            filteredList = new ArrayList<>(fullDataList.stream()
                    .filter(item -> item.getCaption() != null && item.getCaption().toLowerCase().replaceAll("\\s+", "").contains(query.toLowerCase().replaceAll("\\s+", "")))//Checks if the caption contains the search query (case insensitive).
                    .collect(Collectors.toList()));//Collects the filtered results back into a list.
        } else {
            // If query is empty, return all items
            filteredList = new ArrayList<>(fullDataList);
        }

        // Clear and add filtered data to dataList
        dataList.clear();// clears any existing items in dataList, ensuring it starts fresh.
        dataList.addAll(filteredList);//Contains either the filtered results or all items if the query was empty

        // Notify the adapter of data changes
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView recyclerImage;
        TextView recyclerCaption;
        ImageView delete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerImage = itemView.findViewById(R.id.recyclerImage);
            recyclerCaption = itemView.findViewById(R.id.recyclerCaption);
            delete = itemView.findViewById(R.id.deleteIcon);
        }

    }
}