package me.xiangning.simpleservice.demo.music;

/**
 * Created by xiangning on 2021/7/4.
 */
//public class MusicServiceLocal implements MusicService {
//
//    private MusicServiceBinder remote;
//
//    public MusicServiceLocal(IBinder binder) {
//        this.remote = MusicServiceBinder.Stub.asInterface(binder);
//    }
//
//    public MusicServiceLocal(MusicServiceBinder binder) {
//        this.remote = binder;
//    }
//
//    @Override
//    public boolean play(List<String> names) {
//        try {
//            return remote.play(names);
//        } catch (RemoteException e) {
//            onRemoteException(e);
//        }
//        return false;
//    }
//
//    @Override
//    public void download(String name, OnDownloadListener listener) {
//        try {
//            remote.download(name, new DownloadRemoteWrapper(listener));
//        } catch (RemoteException e) {
//            onRemoteException(e);
//        }
//    }
//
//    public void onRemoteException(Throwable exception) {
//
//    }
//
//}