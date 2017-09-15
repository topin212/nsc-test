package main;

import com.github.sshaddicts.neuralclient.data.ProcessedData;

import java.io.IOException;

public interface CustomView {
    void recieveData(ProcessedData data);
}
