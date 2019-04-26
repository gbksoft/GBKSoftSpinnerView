package com.gbksoft.example;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gbksoft.spinnerview.SpinnerAdapter;
import com.gbksoft.spinnerview.SpinnerView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String[] ANDROID_VERSIONS = {
            "Cupcake",
            "Donut",
            "Eclair",
            "Froyo",
            "Gingerbread",
            "Honeycomb",
            "Ice Cream Sandwich",
            "Jelly Bean",
            "KitKat",
            "Lollipop",
            "Marshmallow",
            "Nougat",
            "Oreo",
            "Pie",
            "Q",
            "R"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SpinnerView spinner = findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new SpinnerView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                Snackbar.make(spinner, "Clicked " + ANDROID_VERSIONS[position], Snackbar.LENGTH_LONG).show();
            }
        });

        spinner.setOnNothingSelectedListener(new SpinnerView.OnNothingSelectedListener() {

            @Override
            public void onNothingSelected() {
                Snackbar.make(spinner, "Nothing selected", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private SpinnerAdapter spinnerAdapter = new SpinnerAdapter() {
        @Override
        public int getCount() {
            return ANDROID_VERSIONS.length;
        }

        @Override
        public Object getItem(int position) {
            return ANDROID_VERSIONS[position];
        }

        @Override
        public String getItemString(int position) {
            return ANDROID_VERSIONS[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView :
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            TextView tv = view.findViewById(R.id.tv);
            tv.setText(ANDROID_VERSIONS[position]);
            return view;
        }
    };
}
