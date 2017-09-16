package com.sshaddicts.example.main;

import com.github.sshaddicts.neuralclient.AuthenticatedClient;
import com.github.sshaddicts.neuralclient.Client;
import com.github.sshaddicts.neuralclient.ConnectedClient;
import com.github.sshaddicts.neuralclient.data.ProcessedData;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.impl.DeferredObject;
import rx.functions.Action1;

public class NeuralSwarmClient {
    private Client client;

    private String username;
    private String password;

    private final CustomView view;

    private Deferred<AuthenticatedClient, Void, Void> deferredClient = new DeferredObject<>();

    public NeuralSwarmClient(String username, String password, CustomView view) {
        this.username = username;
        this.password = password;
        this.view = view;
        client = new Client("ws://localhost:7778/", "api");
    }

    public void registerClient() {
        client.getConnected().subscribe(new Action1<ConnectedClient>() {
            @Override
            public void call(ConnectedClient connectedClient) {
                connectedClient.register(username, password).subscribe(new Action1<AuthenticatedClient>() {
                    @Override
                    public void call(AuthenticatedClient authenticatedClient) {
                        deferredClient.resolve(authenticatedClient);
                    }
                });
            }
        });
    }

    public void authenticateClient(){
        client.getConnected().subscribe(new Action1<ConnectedClient>() {
            @Override
            public void call(ConnectedClient connectedClient) {
                connectedClient.auth(username, password).subscribe(new Action1<AuthenticatedClient>() {
                    @Override
                    public void call(AuthenticatedClient authenticatedClient) {
                        deferredClient.resolve(authenticatedClient);
                    }
                });
            }
        });
    }

    public void requestImageProcessing(final byte[] data, final int width, final int height) {
        deferredClient.then(new DoneCallback<AuthenticatedClient>() {
            @Override
            public void onDone(AuthenticatedClient result) {
                result.processImage(data, width, height).subscribe(
                        new Action1<ProcessedData>() {
                            @Override
                            public void call(ProcessedData processedData) {
                                view.recieveData(processedData);
                            }
                        }
                );
            }
        });
    }

    public void connect(){
        client.connect();
    }
}
