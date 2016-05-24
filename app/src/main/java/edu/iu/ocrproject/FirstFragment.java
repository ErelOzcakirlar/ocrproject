package edu.iu.ocrproject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.devspark.appmsg.AppMsg;

import edu.iu.ocrproject.data.BaseManager;
import edu.iu.ocrproject.data.User;


public class FirstFragment extends Fragment implements BaseManager.IQuery {

    public FirstFragment() {
        // Required empty public constructor
    }

    public static FirstFragment newInstance() {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    EditText UsernameEdit,PasswordEdit;
    Button LoginButton,SignupButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        UsernameEdit = (EditText)view.findViewById(R.id.edit_username);
        PasswordEdit = (EditText)view.findViewById(R.id.edit_password);

        LoginButton = (Button)view.findViewById(R.id.button_login);
        SignupButton = (Button)view.findViewById(R.id.button_signup);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.current.checkDataManager();
                        MainActivity.current.dataManager.login(
                                FirstFragment.this,
                                UsernameEdit.getText().toString(),
                                PasswordEdit.getText().toString());
                    }
                }).start();
            }
        });

        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.current.openFragment(SignupFragment.newInstance(),true);
            }
        });

        return view;
    }

    @Override
    public void onResult(String func, Object... result) {
        if((boolean)result[0]){
            MainActivity.current.openFragment(ActionFragment.newInstance((User)result[1]),false);
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppMsg.Style style = new AppMsg.Style(AppMsg.LENGTH_SHORT, R.color.colorAccent);
                    AppMsg.makeText(getActivity(), getString(R.string.wrong_login), style).show();
                }
            });
        }
    }
}
