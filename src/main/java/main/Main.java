package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sshaddicts.lucrecium.imageProcessing.ImageProcessor;
import com.github.sshaddicts.neuralclient.data.ProcessedData;
import org.opencv.core.Core;

import java.io.File;
import java.io.IOException;

public class Main implements CustomView{

    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        final String cred = "test";
        final Main main = new Main();

        final ImageProcessor processor = new ImageProcessor("numbers31.jpg");

        NeuralSwarmClient client = new NeuralSwarmClient("test", "test", main);

        client.authenticateClient();
        client.requestImageProcessing(ImageProcessor.toByteArray(processor.getImage()),
                processor.getImage().width(),
                processor.getImage().height());

        client.connect();

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
