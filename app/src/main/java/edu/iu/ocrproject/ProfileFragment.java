package edu.iu.ocrproject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.iu.ocrproject.data.BaseManager;
import edu.iu.ocrproject.data.Ingredient;
import edu.iu.ocrproject.data.User;


public class ProfileFragment extends Fragment implements BaseManager.IQuery {

    private static final String ARG_PARAM1 = "param1";

    private User user;

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
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

    LinearLayout BirthSpinner, SexSpinner, LikesSpinner, DislikesSpinner;
    TextView BirthText, SexText, LikesText, DislikesText;
    Button SaveButton;

    String[] BirthNameSource;
    int[] BirthIdSource;

    List<Ingredient> Ingredients = new ArrayList<>();

    String[] LikeNameSource = new String[0];
    int[] LikeIdSource = new int[0];

    String[] DislikeNameSource = new String[0];
    int[] DislikeIdSource = new int[0];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        MainActivity.current.checkDataManager();
        MainActivity.current.dataManager.getIngredients(this);

        BirthSpinner = (LinearLayout) view.findViewById(R.id.spinner_birth);
        SexSpinner = (LinearLayout) view.findViewById(R.id.spinner_sex);
        LikesSpinner = (LinearLayout) view.findViewById(R.id.spinner_likes);
        DislikesSpinner = (LinearLayout) view.findViewById(R.id.spinner_dislikes);

        BirthText = (TextView) view.findViewById(R.id.text_birth);
        SexText = (TextView) view.findViewById(R.id.text_sex);
        LikesText = (TextView) view.findViewById(R.id.text_likes);
        DislikesText = (TextView) view.findViewById(R.id.text_dislikes);

        SaveButton = (Button) view.findViewById(R.id.button_save);

        createBirthSource();

        if (user.age != 0) {
            BirthText.setText(String.valueOf(user.age));
        }
        SexText.setText(user.sex == 0 ? "K" : "E");

        BirthSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getContext())
                        .items(BirthNameSource)
                        .itemsIds(BirthIdSource)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                BirthText.setText(text);
                                user.age = itemView.getId();
                            }
                        }).show();
            }
        });

        SexSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getContext())
                        .items(new String[]{"K", "E"})
                        .itemsIds(new int[]{0, 1})
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                SexText.setText(text);
                                user.sex = itemView.getId();
                            }
                        }).show();
            }
        });

        LikesSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getContext())
                        .title(R.string.likes)
                        .items(LikeNameSource)
                        .itemsIds(LikeIdSource)
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, final Integer[] which, CharSequence[] text) {
                                /**
                                 * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected check box to actually be selected.
                                 * See the limited multi choice dialog example in the sample project for details.
                                 **/
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        user.likes = new HashSet<>();
                                        for(int index:which){
                                            user.likes.add(LikeIdSource[index]);
                                        }
                                        setDataSources();
                                    }
                                }).start();
                                String to_show = "";
                                for (int i = 0; i < text.length; i++) {
                                    to_show += text[i];
                                    if(i != text.length - 1){
                                        to_show += ", ";
                                    }
                                }
                                LikesText.setText(to_show);

                                return true;
                            }
                        })
                        .positiveText(R.string.choose)
                        .show();
            }
        });

        DislikesSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getContext())
                        .title(R.string.dislikes)
                        .items(DislikeNameSource)
                        .itemsIds(DislikeIdSource)
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, final Integer[] which, CharSequence[] text) {
                                /**
                                 * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected check box to actually be selected.
                                 * See the limited multi choice dialog example in the sample project for details.
                                 **/
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        user.dislikes = new HashSet<>();
                                        for(int index:which){
                                            user.dislikes.add(DislikeIdSource[index]);
                                        }
                                        setDataSources();
                                    }
                                }).start();
                                String to_show = "";
                                for (int i = 0; i < text.length; i++) {
                                    to_show += text[i];
                                    if(i != text.length - 1){
                                        to_show += ", ";
                                    }
                                }
                                DislikesText.setText(to_show);

                                return true;
                            }
                        })
                        .positiveText(R.string.choose)
                        .show();
            }
        });

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.current.checkDataManager();
                MainActivity.current.dataManager.updateUser(user);
                ActionFragment.current.setUser(user);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void createBirthSource() {

        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);

        BirthIdSource = new int[year - 1929];
        BirthNameSource = new String[year - 1929];

        int k = 0;
        for (int i = year - 7; i > 1922; i--) {
            BirthNameSource[k] = String.valueOf(i);
            BirthIdSource[k] = i;
            k++;
        }
    }

    @Override
    public void onResult(String func, Object... result) {
        Ingredients = (List<Ingredient>) result[0];
        setDataSources();
    }

    private void setDataSources() {
        List<Ingredient> LikeSource = new ArrayList<>();
        List<Ingredient> DislikeSource = new ArrayList<>();
        for (Ingredient item : Ingredients) {
            if (!user.dislikes.contains(item.id)) {
                LikeSource.add(item);
            }
            if (!user.likes.contains(item.id)) {
                DislikeSource.add(item);
            }
        }

        LikeNameSource = new String[LikeSource.size()];
        LikeIdSource = new int[LikeSource.size()];
        int i = 0;
        for (Ingredient item : LikeSource) {
            LikeNameSource[i] = item.name;
            LikeIdSource[i] = item.id;
            i++;
        }

        DislikeNameSource = new String[DislikeSource.size()];
        DislikeIdSource = new int[DislikeSource.size()];
        int k = 0;
        for (Ingredient item : DislikeSource) {
            DislikeNameSource[k] = item.name;
            DislikeIdSource[k] = item.id;
            k++;
        }
    }
}
