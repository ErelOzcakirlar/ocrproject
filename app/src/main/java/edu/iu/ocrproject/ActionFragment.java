package edu.iu.ocrproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import edu.iu.ocrproject.data.User;


public class ActionFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private User user;

    public static ActionFragment current;

    public ActionFragment() {
        // Required empty public constructor
    }

    public static ActionFragment newInstance(User user) {
        ActionFragment fragment = new ActionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User)getArguments().getSerializable(ARG_PARAM1);
        }
    }

    RelativeLayout CameraButton,GalleryButton,ProfileButton,ExitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        current = this;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_action, container, false);

        CameraButton = (RelativeLayout)view.findViewById(R.id.button_camera);
        GalleryButton = (RelativeLayout)view.findViewById(R.id.button_gallery);
        ProfileButton = (RelativeLayout)view.findViewById(R.id.button_profile);
        ExitButton = (RelativeLayout)view.findViewById(R.id.button_exit);

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.current.makeCameraRequest();
            }
        });
        GalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.current.makeGalleryRequest();
            }
        });
        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.current.openFragment(ProfileFragment.newInstance(user),true);
            }
        });
        ExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.current.checkDataManager();
                        MainActivity.current.dataManager.logout();
                    }
                }).start();
                MainActivity.current.openFragment(FirstFragment.newInstance(),false);
            }
        });

        if(user.likes.isEmpty() && user.dislikes.isEmpty()){
            MainActivity.current.openFragment(ProfileFragment.newInstance(user),true);
        }
        return view;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
