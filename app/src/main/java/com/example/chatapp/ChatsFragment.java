package com.example.chatapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.chatapp.adapters.UsersAdapter;
import com.example.chatapp.models.Chat;
import com.example.chatapp.models.ChatList;
import com.example.chatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private UsersAdapter usersAdapter;
    private List<User> users;

    FirebaseUser currentUser;
    DatabaseReference reference;

    public static ProgressBar progressBar;

    LinearLayout noChats;


    private List<ChatList> chatList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.progressbarc);
        noChats = view.findViewById(R.id.nochats);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chat");

        chatList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ChatList chatList1 = ds.getValue(ChatList.class);
                    chatList.add(chatList1);
                }
                chatList(chatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }

    private void chatList(final List<ChatList> chatList) {
        users = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    for (ChatList list: ChatsFragment.this.chatList){
                        if(user.getUserid().equals(list.getId())){
                            users.add(user);
                        }
                    }

                    if(chatList.size()>0){
                    usersAdapter = new UsersAdapter(getContext(),users,true);
                    recyclerView.setAdapter(usersAdapter);
                    }else {
                        progressBar.setVisibility(View.GONE);
                        noChats.setVisibility(View.VISIBLE);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
