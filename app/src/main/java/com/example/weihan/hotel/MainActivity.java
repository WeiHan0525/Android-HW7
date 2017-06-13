package com.example.weihan.hotel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private PetArrayAdapter adapter = null;

    private static final int LIST_PETS = 1;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LIST_PETS: {
                    List<Hotel> pets = (List<Hotel>) msg.obj;
                    refreshPetList(pets);
                    break;
                }
            }
        }
    };

    private void refreshPetList(List<Hotel> pets) {
        adapter.clear();
        adapter.addAll(pets);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvPets = (ListView)findViewById(R.id.lsview_pet) ;

        adapter = new PetArrayAdapter(this, new ArrayList<Hotel>());
        lvPets.setAdapter(adapter);

        getPetsFormFirebase();
    }

    class FirebaseThread extends Thread {
        private DataSnapshot dataSnapshot;

        public FirebaseThread(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public void run() {
            List<Hotel> lsPets = new ArrayList<>();
            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                for(DataSnapshot ds1 : ds.getChildren()) {
                    for(DataSnapshot ds2 : ds1.getChildren()) {
                        DataSnapshot dsSName = ds2.child("Name");
                        DataSnapshot dsAKind = ds2.child("Add");

                        String shelterName = (String)dsSName.getValue();
                        String kind = (String)dsAKind.getValue();

                        DataSnapshot dsImg = ds2.child("Picture1");
                        String imgUrl = (String)dsImg.getValue();
                        Bitmap petImg = getImgBitmap(imgUrl);

                        Hotel aPet = new Hotel();
                        aPet.setShelter(shelterName);
                        aPet.setKind(kind);
                        aPet.setImgUrl(petImg);
                        lsPets.add(aPet);
                        Log.v("AdoptPet", shelterName + ":" + kind);

                        Message msg = new Message();
                        msg.what = LIST_PETS;
                        msg.obj = lsPets;
                        handler.sendMessage(msg);
                    }

                }
            }
        }
    }

    private void getPetsFormFirebase() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new FirebaseThread(dataSnapshot).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("AdoptPet", databaseError.getMessage());
            }
        });
    }

    private Bitmap getImgBitmap(String imgUrl) {
        try {
            URL url = new URL(imgUrl);
            Bitmap bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return bm;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    class PetArrayAdapter extends ArrayAdapter<Hotel> {
        Context context;

        public PetArrayAdapter(Context context, List<Hotel> items) {
            super(context, 0, items);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout itemlayout = null;
            if (convertView == null) {
                itemlayout = (LinearLayout) inflater.inflate(R.layout.pet_item, null);
            } else {
                itemlayout = (LinearLayout) convertView;
            }

            Hotel item = (Hotel) getItem(position);
            TextView tvShelter = (TextView) itemlayout.findViewById(R.id.tv_shelter);
            tvShelter.setText(item.getShelter());
            TextView tvKind = (TextView) itemlayout.findViewById(R.id.tv_kind);
            tvKind.setText(item.getKind());
            ImageView ivPet = (ImageView) itemlayout.findViewById(R.id.iv_pet);
            ivPet.setImageBitmap(item.getImgUrl());

            return itemlayout;
        }
    }
}
