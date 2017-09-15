package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.sshaddicts.lucrecium.imageProcessing.ImageProcessor;
import com.github.sshaddicts.neuralclient.AuthenticatedClient;
import com.github.sshaddicts.neuralclient.Client;
import com.github.sshaddicts.neuralclient.ConnectedClient;
import com.github.sshaddicts.neuralclient.data.History;
import com.github.sshaddicts.neuralclient.data.ProcessedData;
import org.opencv.core.Core;
import rx.functions.Action1;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main implements CustomView{

    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final String cred = "test";
        final Main main = new Main();

        final ImageProcessor processor = new ImageProcessor("file.png");

        CompletableFuture<History> done = new CompletableFuture<>();

        Client testClient = new Client("ws://192.168.0.111:7778", "api");

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


        done.get(5, TimeUnit.SECONDS).getItems().forEach((ProcessedData hist) -> {
            System.out.println("---------");
            hist.getItems().forEach((ObjectNode data) ->  {
                System.out.println("[" +  data.get("name").textValue() + "]" +
                        " [" + data.get("price").asDouble() + "]");
            });

        });


        testClient.connect();
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
