package id.agristreet.agristreetapp.data.remote;

/**
 * Created by RyMey on 12/10/17.
 */


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import id.agristreet.agristreetapp.data.local.PengelolaDataLokal;
import id.agristreet.agristreetapp.data.model.Akun;
import id.agristreet.agristreetapp.data.model.Alamat;
import id.agristreet.agristreetapp.data.model.Kategori;
import id.agristreet.agristreetapp.data.model.Kerjasama;
import id.agristreet.agristreetapp.data.model.Lamaran;
import id.agristreet.agristreetapp.data.model.Lowongan;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public class RestApi {
    private static RestApi instance;
    private final Context context;
    private final OkHttpClient httpClient;
    private final Api api;
    private Gson gson;

    private RestApi(Context context) {
        this.context = context;

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addNetworkInterceptor(httpLoggingInterceptor)
                .build();

        api = new Retrofit.Builder()
                .baseUrl("http://128.199.215.222")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    public static RestApi getInstance(Context context) {
        if (instance == null) {
            instance = new RestApi(context);
        }
        return instance;
    }

    public Observable<String> verifyPhonePebisnis(String noTelp) {
        return api.verifyPhonePebisnis(noTelp)
                .map(json -> json.get("result").getAsString())
                .doOnNext(reqId -> {
                    PengelolaDataLokal.getInstance(context).simpanNoTelp(noTelp);
                    PengelolaDataLokal.getInstance(context).simpanRequestId(reqId);
                });
    }

    public Observable<String> verifyPhonePetani(String noTelp) {
        return api.verifyPhonePetani(noTelp)
                .map(json -> json.get("result").getAsString())
                .doOnNext(reqId -> {
                    PengelolaDataLokal.getInstance(context).simpanNoTelp(noTelp);
                    PengelolaDataLokal.getInstance(context).simpanRequestId(reqId);
                });
    }

    public Observable<Akun> authPebisnis(String code) {
        return api.authPebisnis(PengelolaDataLokal.getInstance(context).getNoTelp(),
                PengelolaDataLokal.getInstance(context).getReqId(),
                code)
                .map(json -> {
                    Akun akun = new Akun();
                    try {
                        JsonObject result = json.get("result").getAsJsonObject();
                        akun.setToken(result.get("token").getAsString());
                        akun.setUser(ModelParser.parsePebisnis(result));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return akun;
                })
                .doOnNext(akun -> PengelolaDataLokal.getInstance(context).simpanAkun(akun));
    }

    public Observable<Akun> authPetani(String code) {
        return api.authPetani(PengelolaDataLokal.getInstance(context).getNoTelp(),
                PengelolaDataLokal.getInstance(context).getReqId(),
                code)
                .map(json -> {
                    Akun akun = new Akun();
                    try {
                        JsonObject result = json.get("result").getAsJsonObject();
                        akun.setToken(result.get("token").getAsString());
                        akun.setUser(ModelParser.parsePetani(result));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return akun;
                })
                .doOnNext(akun -> PengelolaDataLokal.getInstance(context).simpanAkun(akun));
    }

    public Observable<Akun> updateProfilePebisnis(String nama, String foto) {
        return api.updateProfilePebisnis(PengelolaDataLokal.getInstance(context).getAkun().getToken(),
                nama, foto)
                .map(json -> {
                    Akun akun = PengelolaDataLokal.getInstance(context).getAkun();
                    try {
                        JsonObject result = json.get("result").getAsJsonObject();
                        akun.setUser(ModelParser.parsePebisnis(result));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return akun;
                })
                .doOnNext(akun -> PengelolaDataLokal.getInstance(context).simpanAkun(akun));
    }

    public Observable<Akun> updateProfilePetani(String nama, String foto) {
        return api.updateProfilePetani(PengelolaDataLokal.getInstance(context).getAkun().getToken(),
                nama, foto)
                .map(json -> {
                    Akun akun = PengelolaDataLokal.getInstance(context).getAkun();
                    try {
                        JsonObject result = json.get("result").getAsJsonObject();
                        akun.setUser(ModelParser.parsePetani(result));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return akun;
                })
                .doOnNext(akun -> PengelolaDataLokal.getInstance(context).simpanAkun(akun));
    }

    public Observable<List<Lowongan>> getLowongan() {
        return api.getLowongan(PengelolaDataLokal.getInstance(context).getAkun().getToken())
                .map(json -> {
                    JsonArray jsonArray = json.get("result").getAsJsonArray();
                    List<Lowongan> daftarLowongan = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        daftarLowongan.add(ModelParser.parseLowongan(jsonElement.getAsJsonObject()));
                    }
                    return daftarLowongan;
                });
    }

    public Observable<List<Lowongan>> getLowonganku() {
        return api.getLowonganku(PengelolaDataLokal.getInstance(context).getAkun().getToken())
                .map(json -> {
                    JsonArray jsonArray = json.get("result").getAsJsonArray();
                    List<Lowongan> daftarLowongan = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        daftarLowongan.add(ModelParser.parseLowongan(jsonElement.getAsJsonObject()));
                    }
                    return daftarLowongan;
                });
    }

    public Observable<List<Kerjasama>> getKerjasama() {
        return api.getKerjasama(PengelolaDataLokal.getInstance(context).getAkun().getToken())
                .map(json -> {
                    JsonArray jsonArray = json.get("result").getAsJsonArray();
                    List<Kerjasama> daftarKerjasama = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        daftarKerjasama.add(ModelParser.parseKerjasama(jsonElement.getAsJsonObject()));
                    }
                    return daftarKerjasama;
                });
    }

    public Observable<Void> makeBid(int idLowongan, String keterangan, long harga) {
        return api.makeBid(PengelolaDataLokal.getInstance(context).getAkun().getToken(),
                idLowongan, keterangan, harga)
                .map(jsonObject -> null);
    }

    public Observable<List<Kategori>> getKategori() {
        return api.getKategori(PengelolaDataLokal.getInstance(context).getAkun().getToken())
                .map(json -> {
                    JsonArray jsonArray = json.get("result").getAsJsonArray();
                    List<Kategori> daftarKategori = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        daftarKategori.add(ModelParser.parseKategori(jsonElement.getAsJsonObject()));
                    }
                    return daftarKategori;
                });
    }

    public Observable<List<Alamat>> getAlamat() {
        Akun akun = PengelolaDataLokal.getInstance(context).getAkun();
        return api.getAlamat(akun.getToken(), akun.getUser().getId())
                .map(json -> {
                    JsonArray jsonArray = json.get("result").getAsJsonArray();
                    List<Alamat> daftarAlamat = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        daftarAlamat.add(ModelParser.parseAlamat(jsonElement.getAsJsonObject()));
                    }
                    return daftarAlamat;
                });
    }

    public Observable<Lowongan> createLowongan(int idKatgori, int idAlamat, String title, String imgUrl,
                                               String description, String tglTutup, int jumlahKomoditas, long price) {
        return api.createLowongan(PengelolaDataLokal.getInstance(context).getAkun().getToken(),
                idKatgori, idAlamat, title, imgUrl, description, tglTutup, jumlahKomoditas, price)
                .map(json -> ModelParser.parseLowongan(json.get("result").getAsJsonObject()));
    }

    public Observable<List<Lamaran>> getLamaran(int idLowongan) {
        return api.getPelamar(PengelolaDataLokal.getInstance(context).getAkun().getToken(), idLowongan)
                .map(json -> {
                    JsonArray jsonArray = json.get("result").getAsJsonArray();
                    List<Lamaran> daftarLamaran = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        daftarLamaran.add(ModelParser.parseLamaran(jsonElement.getAsJsonObject()));
                    }
                    return daftarLamaran;
                });
    }

    public Observable<Kerjasama> makeKerjasama(int idLowongan, int idLamaran) {
        return api.makeKerjasama(PengelolaDataLokal.getInstance(context).getAkun().getToken(),
                idLowongan, idLamaran)
                .map(json -> ModelParser.parseKerjasama(json.get("result").getAsJsonObject()));
    }

    public Observable<Void> finishKerjasama(int idKerjasama) {
        return api.finishKerjasama(PengelolaDataLokal.getInstance(context).getAkun().getToken(), idKerjasama)
                .map(jsonObject -> null);
    }

    public Observable<Void> sendFeedback(String idPetani, int idKerjasama, String saran, int tipeIkon) {
        return api.sendFeedback(PengelolaDataLokal.getInstance(context).getAkun().getToken(),
                idPetani, idKerjasama, saran, tipeIkon)
                .map(jsonObject -> null);
    }

    private interface Api {

        @FormUrlEncoded
        @POST("/pebisnis/verify-phone")
        Observable<JsonObject> verifyPhonePebisnis(@Field("no_telp") String noTelp);

        @FormUrlEncoded
        @POST("/petani/verify-phone")
        Observable<JsonObject> verifyPhonePetani(@Field("no_telp") String noTelp);

        @FormUrlEncoded
        @POST("/pebisnis/auth")
        Observable<JsonObject> authPebisnis(@Field("no_telp") String noTelp,
                                            @Field("request_id") String reqId,
                                            @Field("code") String code);

        @FormUrlEncoded
        @POST("/petani/auth")
        Observable<JsonObject> authPetani(@Field("no_telp") String noTelp,
                                          @Field("request_id") String reqId,
                                          @Field("code") String code);

        @FormUrlEncoded
        @PUT("/pebisnis/update-profile")
        Observable<JsonObject> updateProfilePebisnis(@Header("token") String token,
                                                     @Field("nama_pebisnis") String nama,
                                                     @Field("foto") String foto);

        @FormUrlEncoded
        @PUT("/petani/update-profile")
        Observable<JsonObject> updateProfilePetani(@Header("token") String token,
                                                   @Field("nama_petani") String nama,
                                                   @Field("foto") String foto);

        @GET("/lowongan")
        Observable<JsonObject> getLowongan(@Header("token") String token);

        @GET("/lowongan/pebisnis")
        Observable<JsonObject> getLowonganku(@Header("token") String token);

        @GET("/kerjasama")
        Observable<JsonObject> getKerjasama(@Header("token") String token);

        @FormUrlEncoded
        @POST("/lamaran/make-lamaran-petani")
        Observable<JsonObject> makeBid(@Header("token") String token,
                                       @Field("id_lowongan") int idLowongan,
                                       @Field("deskripsi_lamaran") String keterangan,
                                       @Field("harga_tawar") long harga);

        @GET("/kategori")
        Observable<JsonObject> getKategori(@Header("token") String token);

        @GET("/alamat/pebisnis/{id}")
        Observable<JsonObject> getAlamat(@Header("token") String token,
                                         @Path("id") String id);

        @FormUrlEncoded
        @POST("/lowongan/make-lowongan")
        Observable<JsonObject> createLowongan(@Header("token") String token,
                                              @Field("id_kategori") int idKatgori,
                                              @Field("id_alamat_pengiriman") int idAlamat,
                                              @Field("judul_lowongan") String title,
                                              @Field("foto") String imgUrl,
                                              @Field("deskripsi_lowongan") String description,
                                              @Field("tgl_tutup") String tglTutup,
                                              @Field("jumlah_komoditas") int jumlahKomoditas,
                                              @Field("harga_awal") long price);

        @GET("/lamaran/lowongan/{id}")
        Observable<JsonObject> getPelamar(@Header("token") String token,
                                          @Path("id") int idLowongan);

        @FormUrlEncoded
        @POST("/kerjasama/make-kerjasama")
        Observable<JsonObject> makeKerjasama(@Header("token") String token,
                                             @Field("id_lowongan") int idLowongan,
                                             @Field("id_lamaran") int idLamaran);

        @FormUrlEncoded
        @POST("/feedback/make-feedback")
        Observable<JsonObject> sendFeedback(@Header("token") String token,
                                            @Field("id_penerima") String idPenerima,
                                            @Field("id_kerjasama") int idKerjasama,
                                            @Field("saran") String saran,
                                            @Field("tipe_ikon") int tipeIkon);

        @FormUrlEncoded
        @PUT("/kerjasama/finish-kerjasama")
        Observable<JsonObject> finishKerjasama(@Header("token") String token,
                                               @Field("id_kerjasama") int idKerjasama);
    }
}
