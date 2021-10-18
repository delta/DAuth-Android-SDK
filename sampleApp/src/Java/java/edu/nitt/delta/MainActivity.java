package edu.nitt.delta;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import edu.nitt.delta.deltaButton.DeltaButton;
import edu.nitt.delta.interfaces.ResultListener;
import edu.nitt.delta.models.AuthorizationRequest;
import edu.nitt.delta.models.GrantType;
import edu.nitt.delta.models.ResponseType;
import edu.nitt.delta.models.Scope;
import edu.nitt.delta.models.User;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DeltaButton signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Scope> scopes = new ArrayList<>();
                scopes.add(Scope.OpenID);
                scopes.add(Scope.User);
                scopes.add(Scope.Email);
                scopes.add(Scope.Profile);

                DAuth.INSTANCE.signIn(
                        MainActivity.this,
                        new AuthorizationRequest(
                                ResponseType.Code,
                                GrantType.AuthorizationCode,
                                "1ww12",
                                scopes,
                                "ncsasd"),
                        new ResultListener<User>() {
                            @Override
                            public void onSuccess(@NonNull User user) {
                                Log.d("DAuthSample","Success: " + user.toString());
                            }

                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });
            }
        });
    }
}
