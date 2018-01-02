package id.agristreet.agristreetapp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.agristreet.agristreetapp.R;
import id.agristreet.agristreetapp.data.model.Lamaran;
import id.agristreet.agristreetapp.data.model.Petani;
import id.agristreet.agristreetapp.util.CurrencyFormatter;
import id.agristreet.agristreetapp.util.Util;

public class DetailLamaranActivity extends AppCompatActivity {
    private static final String LAMARAN_KEY = "lamaran";
    private Lamaran lamaran;
    private Petani petani;

    @BindView(R.id.iv_photo)
    ImageView ivPhoto;
    @BindView(R.id.nama)
    TextView etNama;
    @BindView(R.id.sms)
    ImageView ivSms;
    @BindView(R.id.keterangaan)
    TextView etKeterangan;
    @BindView(R.id.harga_tawar)
    TextView etHarga;
    @BindView(R.id.bt_pilih)
    Button btPilih;

    public static Intent generateIntent(Context context, Lamaran lamaran) {
        Intent intent = new Intent(context, DetailLamaranActivity.class);
        intent.putExtra(LAMARAN_KEY, lamaran);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_lamaran);
        ButterKnife.bind(this);
        lamaran = getIntent().getParcelableExtra(LAMARAN_KEY);
        if (lamaran == null) {
            finish();
            return;
        }

        petani = lamaran.getCreator();
        showLamaran();
    }

    private void showLamaran() {
        ivPhoto.setBackgroundColor(Util.randomColor());
        Glide.with(this)
                .load(petani.getFoto())
                .into(ivPhoto);
        etNama.setText(petani.getNama());
        etKeterangan.setText(lamaran.getDescription());
        etHarga.setText(CurrencyFormatter.format(lamaran.getPrice()));
    }

    @OnClick(R.id.sms)
    public void sms() {
        Uri uri = Uri.parse(petani.getNoTelp());
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", "The SMS text");
        startActivity(intent);
    }

    @OnClick(R.id.bt_pilih)
    public void pilihPetani() {

    }
}