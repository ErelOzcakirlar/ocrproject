package edu.iu.ocrproject;

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


public class SignupFragment extends Fragment implements BaseManager.IQuery{

    public SignupFragment() {
        // Required empty public constructor
    }

    public static SignupFragment newInstance() {
        SignupFragment fragment = new SignupFragment();
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

    EditText NameEdit,SurnameEdit,UsernameEdit,PasswordEdit,PasswordAgainEdit;
    Button SignupButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        NameEdit = (EditText)view.findViewById(R.id.edit_name);
        SurnameEdit = (EditText)view.findViewById(R.id.edit_surname);
        UsernameEdit = (EditText)view.findViewById(R.id.edit_username);
        PasswordEdit = (EditText)view.findViewById(R.id.edit_password);
        PasswordAgainEdit = (EditText)view.findViewById(R.id.edit_password_again);

        SignupButton = (Button)view.findViewById(R.id.button_signup);
        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean validation = true;
                        String message = "";

                        String name = NameEdit.getText().toString();
                        if(name.length() < 2){
                            message = "Ad yeterli uzunlukta değil";
                            validation = false;
                        }

                        String surname = SurnameEdit.getText().toString();
                        if(surname.length() < 2){
                            message = validation ? "Soyad yeterli uzunlukta değil" : message;
                            validation = false;
                        }

                        String username = UsernameEdit.getText().toString();
                        if(username.length() < 6){
                            message = validation ? "Kullanıcı adı yeterli uzunlukta değil" : message;
                            validation = false;
                        }

                        String password = PasswordEdit.getText().toString();
                        if(password.length() < 6){
                            message = validation ? "Şifre yeterli uzunlukta değil" : message;
                            validation = false;
                        }

                        String password_again = PasswordAgainEdit.getText().toString();
                        if(!password.contentEquals(password_again)){
                            message = validation ? "Şifre tekrarı yanlış" : message;
                            validation = false;
                        }

                        if(validation){
                            MainActivity.current.checkDataManager();
                            MainActivity.current.dataManager.signup(
                                    SignupFragment.this,
                                    username,
                                    name,
                                    surname,
                                    password);
                        }else{
                            final String final_message = message;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AppMsg.Style style = new AppMsg.Style(AppMsg.LENGTH_SHORT, R.color.colorAccent);
                                    AppMsg.makeText(getActivity(), final_message, style).show();
                                }
                            });

                        }
                    }
                }).start();


            }
        });

        return view;
    }

    @Override
    public void onResult(String tag, final Object... result) {
        if((boolean)result[0]){
            MainActivity.current.openFragment(ActionFragment.newInstance((User)result[1]),false);
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppMsg.Style style = new AppMsg.Style(AppMsg.LENGTH_SHORT, R.color.colorAccent);
                    AppMsg.makeText(getActivity(), (String)result[1], style).show();
                }
            });

        }
    }
}
