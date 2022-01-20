package com.onfido.qa.webdriver.listener;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onfido.qa.webdriver.WebTestBase;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import static java.util.concurrent.TimeUnit.SECONDS;

public class BrowserStackListener extends TestListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(BrowserStackListener.class);

    private final Api client;

    private enum Status {
        @JsonProperty("passed")
        PASSED,

        @JsonProperty("failed")
        FAILED
    }

    private static class TestResult {

        @JsonProperty
        private final Status status;

        @JsonProperty
        private final String reason;

        private TestResult(Status status, String reason) {
            this.status = status;
            this.reason = reason;
        }
    }

    private interface Api {
        @PUT("/automate/sessions/{sessionId}.json")
        Call<ResponseBody> publishTestResult(@Path("sessionId") String sessionId, @Body TestResult testResult);
    }

    public BrowserStackListener() {

        var username = System.getProperty("browserstack.username", System.getenv("BROWSERSTACK_USERNAME"));
        var accessKey = System.getProperty("browserstack.accessKey", System.getenv("BROWSERSTACK_ACCESS_KEY"));

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(accessKey)) {

            var httpClient = new OkHttpClient.Builder()
                    .authenticator((route, response) -> {
                        return response.request().newBuilder().header("Authorization", Credentials.basic(username, accessKey)).build();
                    })
                    .connectTimeout(5, SECONDS)
                    .writeTimeout(10, SECONDS)
                    .readTimeout(10, SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.browserstack.com/")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .client(httpClient)
                    .build();

            client = retrofit.create(Api.class);
        } else {
            client = null;
        }

    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);

        report(Status.PASSED);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        super.onTestFailure(tr);

        var throwable = tr.getThrowable();
        var out = new StringWriter();
        throwable.printStackTrace(new PrintWriter(out));

        //noinspection HardcodedLineSeparator
        report(Status.FAILED, String.format("%s\n\n%s", throwable.getMessage(), out));
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped(tr);

        report(Status.FAILED, "SKIPPED");
    }

    private void report(Status status) {
        report(status, null);
    }

    private void report(Status status, String reason) {
        try {
            _report(status, reason);
        } catch (IOException e) {
            log.error("Got error, while calling browserstack api to publish result", e);
        }
    }

    private void _report(Status status, String reason) throws IOException {

        if (client == null) {
            return;
        }

        var sessionId = Objects.requireNonNull(WebTestBase.d()).getSessionId();
        if (sessionId == null) {
            return;
        }

        var response = client.publishTestResult(sessionId.toString(), new TestResult(status, reason)).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Got non successful response from browserstack api");
        }


    }
}
