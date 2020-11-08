package com.example.chatapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chatapp.ChatsFragment;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.PeopleFragment;
import com.example.chatapp.R;
import com.example.chatapp.models.Chat;
import com.example.chatapp.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {



    public Context context;
    List<User> users;

    String lastMessage;

    boolean isChat;

    public UsersAdapter(Context context, List<User> user,boolean isChat) {
        this.context = context;
        this.users = user;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users_list,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final User user = users.get(position);





        if(isChat){
            holder.name.setVisibility(View.GONE);
            lastMessage(user.getUserid(),holder.lastMsg);
            holder.name1.setText(user.getName());

        }else{
            holder.chatsss.setVisibility(View.GONE);
            holder.name.setText(user.getName());
        }

//       Deal with image later on...

        try{
            Picasso.get().load(user.getImage()).into(holder.profile);
        }catch (Exception e){
            Picasso.get().load(R.drawable.people).into(holder.profile);
        }
//
        holder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Deal with context also
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId",user.getUserid());
                context.startActivity(intent);
                ((Activity)context).finish();

                    }
        });


        if(!isChat) {
            PeopleFragment.progressBar.setVisibility(View.GONE);
        }else{
            ChatsFragment.progressBar.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView profile;
        TextView name,lastMsg,name1;
        RelativeLayout userLayout;
        LinearLayout chatsss;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.username);
            name1 = itemView.findViewById(R.id.username1);
            userLayout = itemView.findViewById(R.id.userLayout);

            lastMsg = itemView.findViewById(R.id.msgLast);
            chatsss = itemView.findViewById(R.id.chatsss);
        }
    }

    //check for last message
    private void lastMessage(final String userId, final TextView lastMsg){
        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chat");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if(chat.getReciever().equals(firebaseUser.getUid())  && chat.getSender().equals(userId)
                            || chat.getReciever().equals(userId) && chat.getSender().equals(firebaseUser.getUid())){

                        if(chat.getType().equals("text")) {
                            lastMessage = chat.getMessage();
                        }else {
                            lastMessage = "Photo Message";
                        }

                        if (!chat.isIsseen() && firebaseUser.getUid().equals(chat.getReciever())){
                            lastMsg.setTypeface(null, Typeface.BOLD);
                            lastMsg.setTextColor(Color.parseColor("#000000"));
                        }else {
                            lastMsg.setTypeface(null,Typeface.NORMAL);
                        }
                    }

                }
                switch (lastMessage){
                    case "default":
                        lastMsg.setText("No Message");
                        break;

                    default:
                        lastMsg.setText(lastMessage);
                        break;
                }


                lastMessage = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}













//package com.example.chatapp.adapters;
//
//        import android.app.Activity;
//        import android.content.Intent;
//        import android.view.LayoutInflater;
//        import android.view.View;
//        import android.view.ViewGroup;
//        import android.widget.ImageView;
//        import android.widget.LinearLayout;
//        import android.widget.TextView;
//
//        import com.example.chatapp.MessageActivity;
//        import com.example.chatapp.R;
//        import com.example.chatapp.models.User;
//        import com.firebase.ui.database.FirebaseRecyclerAdapter;
//        import com.firebase.ui.database.FirebaseRecyclerOptions;
//
//        import java.util.ArrayList;
//
//        import androidx.annotation.NonNull;
//        import androidx.recyclerview.widget.RecyclerView;
//
//public class UsersAdapter extends FirebaseRecyclerAdapter<User, UsersAdapter.MyViewHolder> {
//
//
//
//    Activity context;
//
//    /**
//     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
//     * {@link FirebaseRecyclerOptions} for configuration options.
//     *
//     * @param options
//     */
//    public UsersAdapter(@NonNull FirebaseRecyclerOptions<User> options,Activity context) {
//        super(options);
//        this.context = context;
//    }
//
//    @Override
//    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull final User model) {
//        holder.name.setText(model.getName());
//
////        Deal with image later on...
//
//
//        holder.userLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Deal with context also
//                Intent intent = new Intent(context, MessageActivity.class);
//                intent.putExtra("userId",model.getUserid());
//                context.startActivity(intent);
//                context.finish();
//
//            }
//        });
//    }
//
//    @NonNull
//    @Override
//    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users_list,parent,false);
//
//        return new MyViewHolder(view);
//    }
//
//    public class MyViewHolder extends RecyclerView.ViewHolder{
//
//        ImageView profile;
//        TextView name;
//        LinearLayout userLayout;
//        public MyViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            profile = itemView.findViewById(R.id.profileImage);
//            name = itemView.findViewById(R.id.username);
//            userLayout = itemView.findViewById(R.id.userLayout);
//        }
//    }
//}

