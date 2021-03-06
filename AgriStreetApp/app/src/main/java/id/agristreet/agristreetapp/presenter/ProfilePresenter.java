package id.agristreet.agristreetapp.presenter;

import android.content.Context;
import android.util.Log;

import java.io.File;

import id.agristreet.agristreetapp.data.local.PengelolaDataLokal;
import id.agristreet.agristreetapp.data.model.Akun;
import id.agristreet.agristreetapp.data.model.User;
import id.agristreet.agristreetapp.data.remote.ImageUploader;
import id.agristreet.agristreetapp.data.remote.RestApi;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by RyMey on 12/13/17.
 */

public class ProfilePresenter extends BasePresenter<ProfilePresenter.View> {

    private final Context context;

    public ProfilePresenter(Context context, View view) {
        super(view);
        this.context = context;
    }

    public void saveProfile(String name) {
        if (PengelolaDataLokal.getInstance(context).getUserType() == PengelolaDataLokal.UserType.PEBISNIS) {
            updateProfilePebisnis(name);
        } else {
            updateProfilePetani(name);
        }
    }

    private void updateProfilePebisnis(String name) {
        view.showLoading();
        User user = PengelolaDataLokal.getInstance(context).getAkun().getUser();
        RestApi.getInstance(context)
                .updateProfilePebisnis(name, user.getFoto())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(akun -> {
                    view.onSavedProfile();
                    view.dismissLoading();
                }, throwable -> {
                    view.showError(throwable.getMessage());
                    view.dismissLoading();
                });
    }

    private void updateProfilePetani(String name) {
        view.showLoading();
        User user = PengelolaDataLokal.getInstance(context).getAkun().getUser();
        RestApi.getInstance(context)
                .updateProfilePetani(name, user.getFoto())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(akun -> {
                    view.onSavedProfile();
                    view.dismissLoading();
                }, throwable -> {
                    view.showError(throwable.getMessage());
                    view.dismissLoading();
                });
    }

    public void uploadAvatar(File imageFile) {
        view.showLoading();
        User user = PengelolaDataLokal.getInstance(context).getAkun().getUser();
        Observable<String> uploadImageTask = ImageUploader.getInstance(context).upload(imageFile);
        Observable<Akun> updateProfileTask;
        if (PengelolaDataLokal.getInstance(context).getUserType() == PengelolaDataLokal.UserType.PEBISNIS) {
            updateProfileTask = uploadImageTask.flatMap(imgUrl ->
                    RestApi.getInstance(context).updateProfilePebisnis(user.getNama(), imgUrl));
        } else {
            updateProfileTask = uploadImageTask.flatMap(imgUrl ->
                    RestApi.getInstance(context).updateProfilePetani(user.getNama(), imgUrl));
        }

        updateProfileTask.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(akun -> {
                    view.onAvatarUploaded();
                    view.dismissLoading();
                }, throwable -> {
                    view.showError(throwable.getMessage());
                    view.dismissLoading();
                });
    }

    public interface View extends BasePresenter.View {
        void onSavedProfile();

        void onAvatarUploaded();
    }
}
