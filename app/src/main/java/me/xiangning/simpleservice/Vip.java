package me.xiangning.simpleservice;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import me.xiangning.annotation.Aidl;

/**
 * Created by xiangning on 2021/7/3.
 */
@Aidl
public class Vip implements Parcelable {

    private String name;

    protected Vip(Parcel in) {
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Vip> CREATOR = new Creator<Vip>() {
        @Override
        public Vip createFromParcel(Parcel in) {
            return new Vip(in);
        }

        @Override
        public Vip[] newArray(int size) {
            return new Vip[size];
        }
    };
}
