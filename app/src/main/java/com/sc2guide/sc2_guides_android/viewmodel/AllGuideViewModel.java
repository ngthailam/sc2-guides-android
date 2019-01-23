package com.sc2guide.sc2_guides_android.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sc2guide.sc2_guides_android.data.model.Guide;
import com.sc2guide.sc2_guides_android.data.model.GuideBodyItem;
import com.sc2guide.sc2_guides_android.error.NotPossibleException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AllGuideViewModel extends ViewModel {
    private MutableLiveData<List<Guide>> allGuides;
    private String reference = "guides";
    private int guideNumLimit = 7;
    private String lastItemKey = "";

    public void setLastItemKey(String lastItemKey) {
        this.lastItemKey = lastItemKey;
    }

    public LiveData<List<Guide>> getMoreGuides() {
        loadMoreGuides(); // TODO: change later

        return allGuides;
    }

    public LiveData<List<Guide>> getAllGuides(boolean forceUpdate) {
        if (forceUpdate) {
            loadAllGuides();
        }
        if (allGuides == null) {
            allGuides = new MutableLiveData<>();
            loadAllGuides();
        }
        return allGuides;
    }

    private void loadAllGuides() {
        // Do async operation to fetch guides
        FirebaseDatabase.getInstance()
                .getReference(reference)
                .orderByKey()
                .limitToLast(guideNumLimit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Guide> tempListGuide = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    tempListGuide.add(retrieveDataToModel(snapshot));
                                } catch (NotPossibleException e) {
                                    e.printStackTrace();
                                }
                            }
                            // reverse the temp list
                            handleLoadGuideLogic(tempListGuide);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        databaseError.getCode();
                    }
                });
    }

    private void loadMoreGuides() {
        //
        FirebaseDatabase.getInstance()
                .getReference(reference)
                .orderByKey()
                .limitToLast(guideNumLimit)
                .endAt(lastItemKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Guide> tempListGuide = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // add the snapshot to the temp list
                                try {
                                    tempListGuide.add(retrieveDataToModel(snapshot));
                                } catch (NotPossibleException e) {
                                    e.printStackTrace();
                                }
                            }
                            // set value to the mutable live data
                            handleLoadMoreLogic(tempListGuide);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        databaseError.getCode();
                    }
                });
    }

    /**
     * @effects: use the data from the snapshot to make a new Guide();
     * @param snapshot
     * @return a new Guide Object
     * @throws NotPossibleException
     */
    private Guide retrieveDataToModel(DataSnapshot snapshot) throws NotPossibleException {
        // get all the attributes
        String id = snapshot.getKey();
        String title = snapshot.child("title").getValue().toString();
        String my_race = snapshot.child("myRace").getValue().toString();
        String op_race = snapshot.child("opRace").getValue().toString();
        String author_id = snapshot.child("authorId").getValue().toString();
        String author_email = snapshot.child("authorName").getValue().toString();
        String current_time = snapshot.child("date").getValue().toString();
        // list to hold all the guide body items
        List<GuideBodyItem> mList = new ArrayList<>();
        // get guide body items
        for (DataSnapshot mySnap : snapshot.child("guideBodyItems").getChildren()) {
            GuideBodyItem item = new GuideBodyItem(mySnap.child("type").getValue().toString(), mySnap.child("body").getValue().toString());
            mList.add(item);
        }

        return new Guide(id, title, my_race, op_race, author_id, author_email, mList, current_time);
    }

    /**
     *
     * @param tempListGuide
     */
    private void handleLoadGuideLogic(List<Guide> tempListGuide) {
        Collections.reverse(tempListGuide);
        List<Guide> newList = new ArrayList<>();
        for (int i = 0; i < tempListGuide.size() - 1; i++) {
            newList.add(tempListGuide.get(i));
        }
        lastItemKey = tempListGuide.get(tempListGuide.size() - 1).getId();
        // set value to the mutable live data
        allGuides.setValue(newList);
    }


    /**
     * @effects:
     * @param originalList
     */
    private void handleLoadMoreLogic (List<Guide> originalList) {
        Collections.reverse(originalList);

        if (lastItemKey.equals("END")) { // if there are no more to load => return
            return;
        }

        if (originalList.size() < guideNumLimit) { // if the number of guides left are less than the minimum per load
            Objects.requireNonNull(allGuides.getValue()).addAll(originalList);
            lastItemKey = "END"; // set up key for no more guide
            return;
        }

        List<Guide> newList = new ArrayList<>();
        for (int i = 0; i < originalList.size() - 1; i++) {
            newList.add(originalList.get(i));
        } // clone the result from query ( except the last element ) to a new list to add to the current list
        lastItemKey = originalList.get(originalList.size() - 1).getId(); // the last element => key for load more
        Objects.requireNonNull(allGuides.getValue()).addAll(newList); // add the newly acquired guides to the list
    }
}
