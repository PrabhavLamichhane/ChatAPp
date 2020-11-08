package com.example.chatapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chatapp.MessageActivity;
import com.example.chatapp.PicPreviewActivity;
import com.example.chatapp.R;
import com.example.chatapp.models.Chat;
import com.example.chatapp.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public Context context;

    List<Chat> chats;

    FirebaseUser currentUser;
    String imageUrl;


    public MessageAdapter(Context context, List<Chat> chats, String imageUrl) {
        this.context = context;
        this.chats = chats;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
           View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.MyViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.MyViewHolder(view);
        }



    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Chat chat = chats.get(position);


//       Deal with image later on
        Log.d("image", "onBindViewHolder: "+imageUrl);
        try{
            Picasso.get().load(imageUrl).into(holder.profile);
        }catch (Exception e){
//            Picasso.get().load(R.drawable.people).into(holder.profile);
        }

        if(chat.getType().equals("text")) {
            holder.showMsg.setText(chat.getMessage());
            holder.messageImage.setVisibility(View.GONE);
        }else{
            holder.showMsg.setVisibility(View.GONE);
            //load image
            Picasso.get().load(chat.getMessage()).placeholder(R.drawable.ic_image_black_24dp)
                    .into(holder.messageImage);

            holder.messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PicPreviewActivity.class);
                    intent.putExtra("image",chat.getMessage());
                    context.startActivity(intent);
                }
            });

        }



        if(position == chats.size()-1){
            if(chat.isIsseen()){
                holder.txtSeen.setText("Seen");
            }else {
                holder.txtSeen.setText("Delivered");
            }
        }else {
            holder.txtSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

        @Override
    public int getItemViewType(int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(chats.get(position).getSender().equals(currentUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView profile,messageImage;
        TextView showMsg,txtSeen;
        LinearLayout userLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profile_image);
            showMsg = itemView.findViewById(R.id.show_msg);
            txtSeen = itemView.findViewById(R.id.txtSeen);
            messageImage = itemView.findViewById(R.id.messageTv);

        }
    }
}













