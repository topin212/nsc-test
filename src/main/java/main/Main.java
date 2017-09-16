package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sshaddicts.lucrecium.imageProcessing.ImageProcessor;
import com.github.sshaddicts.neuralclient.AuthenticatedClient;
import com.github.sshaddicts.neuralclient.Client;
import com.github.sshaddicts.neuralclient.ConnectedClient;
import com.github.sshaddicts.neuralclient.data.ProcessedData;
import org.opencv.core.Core;
import rx.functions.Action1;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main implements CustomView{

    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        final String cred = "test";
        final Main main = new Main();

        final ImageProcessor processor = new ImageProcessor("file.png");

        Client testClient = new Client("ws://localhost:7778", "api");

        testClient.getConnected().subscribe(new Action1<ConnectedClient>() {
            @Override
            public void call(ConnectedClient connectedClient) {
                connectedClient.auth(cred, cred).subscribe(new Action1<AuthenticatedClient>() {
                    @Override
                    public void call(AuthenticatedClient authenticatedClient) {
                        authenticatedClient.processImage(ImageProcessor.toByteArray(processor.getImage()),
                                processor.getImage().width(),
                                processor.getImage().height()).subscribe(new Action1<ProcessedData>() {
                            @Override
                            public void call(ProcessedData processedData) {
                                main.recieveData(processedData);
                            }
                        },
                                (Throwable e) -> e.printStackTrace());
                    }
                },
                        (Throwable e) -> e.printStackTrace());
            }
        }, (Throwable e) -> e.printStackTrace());

        testClient.connect();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recieveData(ProcessedData data) {
        System.out.println("data recieved");
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(new File("test.log"), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
