package com.xaral.musicfission.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.SongAdapter;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.OpenFileDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsFragment  extends Fragment {
    private Spinner spinner;
    private TextView settingsText, languageText, textPath, path;
    private ImageView imageFolder;
    private LinearLayout folder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        /*try {
            AdView adView = root.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } catch (Exception ignored) {}*/

        settingsText = root.findViewById(R.id.settings);
        textPath = root.findViewById(R.id.textPath);
        path = root.findViewById(R.id.path);
        folder = root.findViewById(R.id.folder);
        languageText = root.findViewById(R.id.textLanguage);
        spinner = root.findViewById(R.id.chooseLanguage);
        imageFolder = root.findViewById(R.id.imageFolder);
        List<String> languages = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.languages)));
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(root.getContext(), R.layout.list_languages, languages);
        mArrayAdapter.setDropDownViewResource(R.layout.list_languages);

        spinner.setAdapter(mArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String language = languages.get(i);
                switch (language) {
                    case "English":
                        setLocale("en");
                        break;
                    case "Russian":
                        setLocale("ru");
                        break;
                    case "German":
                        setLocale("de");
                        break;
                    case "Spanish":
                        setLocale("es");
                        break;
                    case "French":
                        setLocale("fr");
                        break;
                    case "Italian":
                        setLocale("it");
                        break;
                    case "Ukrainian":
                        setLocale("uk");
                        break;
                }
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        int pos = languages.indexOf("English");
        String languageCode = FavoriteService.getLocale();
        switch (languageCode) {
            case "en":
                pos = languages.indexOf("English");
                break;
            case "ru":
                pos = languages.indexOf("Russian");
                break;
            case "de":
                pos = languages.indexOf("German");
                break;
            case "es":
                pos = languages.indexOf("Spanish");
                break;
            case "fr":
                pos = languages.indexOf("French");
                break;
            case "it":
                pos = languages.indexOf("Italian");
                break;
            case "uk":
                pos = languages.indexOf("Ukrainian");
                break;
        }
        spinner.setSelection(pos);
        String downloadFolder = FavoriteService.getFolder();
        path.setText(downloadFolder);
        if (!downloadFolder.equals(""))
            imageFolder.setImageResource(R.drawable.ic_folder_off_white_24dp);

        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenFileDialog openFileDialog = new OpenFileDialog(view.getContext()).setOnlyFoldersFilter();
                openFileDialog.setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                    @Override
                    public void OnSelectedFile(String fileName) {
                        File folder = new File(fileName);
                        if (!folder.exists() || !folder.canRead() || !folder.canWrite()) {
                            Toast.makeText(view.getContext(), view.getContext().getString(R.string.no_access_to_folder), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FavoriteService.setFolder(folder.getAbsolutePath());
                        path.setText(folder.getAbsolutePath());
                        imageFolder.setImageResource(R.drawable.ic_folder_off_white_24dp);
                    }
                });
                openFileDialog.show();
            }
        });

        imageFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String downloadFolder = FavoriteService.getFolder();
                if (!downloadFolder.equals("")) {
                    FavoriteService.setFolder("");
                    path.setText("");
                    imageFolder.setImageResource(R.drawable.ic_folder_white_24dp);
                    return;
                }
                OpenFileDialog openFileDialog = new OpenFileDialog(view.getContext()).setOnlyFoldersFilter();
                openFileDialog.setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                    @Override
                    public void OnSelectedFile(String fileName) {
                        File folder = new File(fileName);
                        if (!folder.exists() || !folder.canRead() || !folder.canWrite()) {
                            Toast.makeText(view.getContext(), view.getContext().getString(R.string.no_access_to_folder), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FavoriteService.setFolder(folder.getAbsolutePath());
                        path.setText(folder.getAbsolutePath());
                        imageFolder.setImageResource(R.drawable.ic_folder_off_white_24dp);
                    }
                });
                openFileDialog.show();
            }
        });

        return root;
    }

    private static void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        MainActivity.activity.getResources().updateConfiguration(config, MainActivity.activity.getBaseContext().getResources().getDisplayMetrics());
        FavoriteService.setLocale(languageCode);
    }

    private void updateUI() {
        settingsText.setText(getString(R.string.settings));
        languageText.setText(getString(R.string.language));
        textPath.setText(getString(R.string.download_folder));
    }
}