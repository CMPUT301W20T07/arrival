package com.example.android.arrival.Util;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.math.BigDecimal;
import java.sql.RowId;
import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {


    private Context context;
    private ArrayList<Request> requests;
    private static final String TAG = "Request Adapter";
    private AccountManager accountManager = AccountManager.getInstance();
    private RequestViewHolder rVHolder;
    private String type;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public RequestAdapter(Context context, ArrayList<Request> requests, String type) {
        this.context = context;
        this.requests = requests;
        this.type = type;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }


    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(context);
        View view =inflater.inflate(R.layout.ride_history_content, null);
        return new RequestViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requests.get(position);
        rVHolder = holder;
        Log.d(TAG, "onBindViewHolder: " + rVHolder);
        String start = request.getStartLocation().getAddress();
        String end = request.getEndLocation().getAddress();

        Float amount = request.getFare();
        NumberFormat defaultFormat = NumberFormat.getCurrencyInstance();

        String fare = defaultFormat.format(amount);
        holder.startLoc.setText(start);
        holder.endLoc.setText(end);
        holder.fare.setText(fare);


        if (type.equals(AccountManager.RIDER_TYPE_STRING)) {
            String uid = request.getDriver();
            getPhoto(uid, holder, position);
            // get name
            DocumentReference reference = db.collection("drivers").document(uid);
            reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String nameStr = documentSnapshot.toObject(Driver.class).getName();
                    holder.name.setText(nameStr);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Could not fetch name for ride " + position, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: -Get name " + e.toString());
                }
            });

        }
        else if (type.equals(AccountManager.DRIVER_TYPE_STRING)) {
            String uid = request.getRider();

            getPhoto(uid, holder, position);
            // get name
            DocumentReference reference = db.collection("riders").document(uid);
            reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String nameStr = documentSnapshot.toObject(Rider.class).getName();
                    holder.name.setText(nameStr);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Could not fetch name for ride " + position, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: -Get name " + e.toString());
                }
            });
        }


    }
    private void getPhoto(String uid, RequestViewHolder holder, int position) {
        //get photo
        StorageReference storageReference = storage.getReference().child("images/" + uid);
        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    Glide.with(context).load(uri).into(holder.pp);
                }
                else {
                    Toast.makeText(context, "Could not fetch photo for ride " + position, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: -Get Photo" + task.getException().toString());
                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return requests.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView name, startLoc, endLoc, fare;
        CircularImageView pp;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ride_partner_name);
            startLoc = itemView.findViewById(R.id.history_start_loc);
            endLoc = itemView.findViewById(R.id.history_end_loc);
            fare = itemView.findViewById(R.id.ride_fare);
            pp = itemView.findViewById(R.id.user_profile_pic_ride_history);
        }
    }
}
