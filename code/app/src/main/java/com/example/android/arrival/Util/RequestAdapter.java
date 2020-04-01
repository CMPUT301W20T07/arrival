package com.example.android.arrival.Util;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.sql.RowId;
import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> implements AccountCallbackListener {


    private Context context;
    private ArrayList<Request> requests;
    private AccountManager accountManager = AccountManager.getInstance();
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    private RequestViewHolder rVHolder;

    private String type;

    public RequestAdapter(Context context, ArrayList<Request> requests, String type) {
        this.context = context;
        this.requests = requests;
        this.type = type;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(context);
        View view =inflater.inflate(R.layout.ride_history_content, null);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requests.get(position);
        rVHolder = holder;

        holder.startLoc.setText(request.getStartLocation().toString());
        holder.endLoc.setText(request.getEndLocation().toString());
        holder.fare.setText(String.valueOf(request.getFare()));

        if (type.equals(RIDER_TYPE_STRING)) {
            String uid = request.getDriver();

            accountManager.getRequestDriverData(uid, this);
            accountManager.getProfilePhoto(this, uid);
        }
        else if (type.equals(DRIVER_TYPE_STRING)) {
            String uid = request.getRider();
            accountManager.getRequestRider(uid, this);
            accountManager.getProfilePhoto(this, uid);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    @Override
    public void onAccountTypeRetrieved(String userType) {

    }

    @Override
    public void onAccountTypeRetrieveFailure(String e) {

    }

    @Override
    public void onAccountCreated(String accountType) {

    }

    @Override
    public void onAccountCreationFailure(String e) {

    }

    @Override
    public void onRiderDataRetrieved(Rider rider) {
        rVHolder.name.setText(rider.getName());
    }

    @Override
    public void onDriverDataRetrieved(Driver driver) {
        rVHolder.name.setText(driver.getName());
    }

    @Override
    public void onDataRetrieveFail(String e) {
        Toast.makeText(context, "Failed to retrieve some data", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccountDeleted() {

    }

    @Override
    public void onAccountDeleteFailure(String e) {

    }

    @Override
    public void onImageUpload() {

    }

    @Override
    public void onImageUploadFailure(String e) {

    }

    @Override
    public void onPhotoReceived(Uri uri) {
        Glide.with(context).load(uri).into(rVHolder.pp);
    }

    @Override
    public void onPhotoReceiveFailure(String e) {
        Toast.makeText(context, "Failed to retrieve some data", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccountUpdated() {

    }

    @Override
    public void onAccountUpdateFailure(String e) {

    }

    public class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView name, startLoc, endLoc, fare;
        CircularImageView pp;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ride_partner_name);
            startLoc = itemView.findViewById(R.id.history_start_loc);
            endLoc = itemView.findViewById(R.id.history_end_loc);
            fare = itemView.findViewById(R.id.ride_fare);
            pp = itemView.findViewById(R.id.user_profile_pic_ride_history);
        }
    }
}
