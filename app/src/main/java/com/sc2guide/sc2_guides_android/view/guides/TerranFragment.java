package com.sc2guide.sc2_guides_android.view.guides;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sc2guide.sc2_guides_android.MainActivity;
import com.sc2guide.sc2_guides_android.R;
import com.sc2guide.sc2_guides_android.adapter.GuideAdapter;
import com.sc2guide.sc2_guides_android.data.model.Guide;
import com.sc2guide.sc2_guides_android.viewmodel.AllGuideViewModel;
import com.sc2guide.sc2_guides_android.viewmodel.RaceGuideViewModel;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TerranFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TerranFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TerranFragment extends Fragment {
    private RaceGuideViewModel mViewModel;
    private GuideAdapter adapter;

    private OnFragmentInteractionListener mListener;

    public TerranFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TerranFragment.
     */
    public static TerranFragment newInstance(String param1, String param2) {
        TerranFragment fragment = new TerranFragment();
        Bundle args = new Bundle();
        // put param
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(RaceGuideViewModel.class);
        mViewModel.getRaceGuides("Terran").observe(this, guide -> {
            updateUI(guide);
        });
    }

    private void updateUI(List<Guide> guide) {
        adapter.setGuides(guide);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //
        changeUIColors();
        //
        RecyclerView recyclerView = getView().findViewById(R.id.all_guides_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        // adapter
        adapter = new GuideAdapter( new GuideAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Guide guide) {
                ((MainActivity) getActivity()).makeTransaction(new GuideDetailFragment(),"DETAIL_FRAG","GUIDE_DETAIL", guide);
            }
        });
        recyclerView.setAdapter(adapter);
        // get data from view model
        mViewModel = ViewModelProviders.of(this).get(RaceGuideViewModel.class);
        mViewModel.getRaceGuides("Terran").observe(this, guide -> {
            updateUI(guide);
        });
    }

    private void changeUIColors() {
        ((MainActivity) getActivity()).getAb().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.terranRed)));
        ((MainActivity) getActivity()).gethView().setBackgroundResource(R.drawable.terran_gradient);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarInfo("Terran Guides","Flying buildings");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
