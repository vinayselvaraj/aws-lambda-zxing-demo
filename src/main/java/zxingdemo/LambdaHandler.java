package zxingdemo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class LambdaHandler implements RequestHandler<Map<String,String>, String> {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String handleRequest(Map<String,String> event, Context context) {

        // Detect file type (PNG, JPEG, TIFF, PDF, etc..)

        return null;
    }
}