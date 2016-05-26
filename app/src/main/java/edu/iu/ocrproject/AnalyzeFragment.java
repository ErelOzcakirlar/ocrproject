package edu.iu.ocrproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;

import edu.iu.ocrproject.data.BaseManager;
import edu.iu.ocrproject.data.Ingredient;
import edu.iu.ocrproject.data.User;


public class AnalyzeFragment extends Fragment implements BaseManager.IQuery {

    private static final String ARG_PARAM1 = "param1";

    private User user;
    private Bitmap bitmap;

    public AnalyzeFragment() {
        // Required empty public constructor
    }

    public static AnalyzeFragment newInstance(User user) {
        AnalyzeFragment fragment = new AnalyzeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    FrameLayout Switcher;
    RelativeLayout RealContent;
    ImageView BarcodeImage;
    TextView ProductText, MessageText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Switcher = (FrameLayout) inflater.inflate(R.layout.fragment_analyze, container, false);

        RealContent = (RelativeLayout) Switcher.findViewById(R.id.content_real);
        BarcodeImage = (ImageView) Switcher.findViewById(R.id.image_barcode);
        ProductText = (TextView) Switcher.findViewById(R.id.text_product_name);
        MessageText = (TextView) Switcher.findViewById(R.id.text_message);

        return Switcher;
    }

    @Override
    public void onResult(String func, final Object... result) {
        if ((boolean) result[0]) {
            final String name = (String) result[1];
            List<Ingredient> likes = (List<Ingredient>) result[2];
            List<Ingredient> dislikes = (List<Ingredient>) result[3];
            List<Ingredient> warnings = (List<Ingredient>) result[4];
            String to_message = "Sayın " + user.name + " " + user.surname + "\n";
            if (likes.size() > 0) {
                if (dislikes.size() > 0) {
                    String message = "Ürünün içerisinde istediğiniz maddelerden ";
                    for (int i = 0; i < likes.size(); i++) {
                        message += likes.get(i).name;
                        if(i != likes.size() - 1){
                            message += ", ";
                        }
                    }
                    message += " ve istemediğiniz maddelerden ";
                    for (int i = 0; i < dislikes.size(); i++) {
                        message += dislikes.get(i).name;
                        if(i != dislikes.size() - 1){
                            message += ", ";
                        }
                    }
                    message += " bulunmaktadır.";
                    to_message += message;
                } else {
                    String message = "Ürünün içerisinde istediğiniz maddelerden ";
                    for (int i = 0; i < likes.size(); i++) {
                        message += likes.get(i).name;
                        if(i != likes.size() - 1){
                            message += ", ";
                        }
                    }
                    message += " bulunmaktadır.";
                    to_message += message;
                }
            } else {
                if (dislikes.size() > 0) {
                    String message = "Ürünün içerisinde istemediğiniz maddelerden ";
                    for (int i = 0; i < dislikes.size(); i++) {
                        message += dislikes.get(i).name;
                        if(i != dislikes.size() - 1){
                            message += ", ";
                        }
                    }
                    message += " bulunmaktadır.";
                    to_message += message;
                } else {
                    to_message += "Ürünün içerisinde istediğiniz veya istemediğiniz herhangi bir madde bulunmamaktadır.";
                }
            }

            if(warnings.size() > 0){

                to_message += "\n\nÜrün içerisinde bulunan sağlığa zararlı olabilecek maddeler :";

                for (int i = 0; i < warnings.size(); i++) {
                    to_message += "\n\n"+warnings.get(i).message;
                }
            }

            final String message = to_message;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switcher.bringChildToFront(RealContent);
                    BarcodeImage.setImageBitmap(bitmap);
                    ProductText.setText("Ürün : " + name);
                    MessageText.setText(message);
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switcher.bringChildToFront(RealContent);
                    ProductText.setText((String) result[1]);
                }
            });
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
