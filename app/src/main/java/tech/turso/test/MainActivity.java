package tech.turso.test;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import tech.turso.libsql.Connection;
import tech.turso.libsql.Database;
import tech.turso.libsql.Libsql;
import tech.turso.libsql.Rows;
import tech.turso.test.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;

        tv.setText("Loading...");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String dbFileBasePath = MainActivity.this.getFilesDir().getPath();
                String dbUrl = "<url returned from `turso db show --url db-name`>";
                String dbAuthToken = "<auth token returned from `turso db tokens create -e never db-name`";
                String count = "Not found";
                try (Database db = Libsql.openRemote(dbUrl, dbAuthToken)) { // Remote only
//                try (Database db = Libsql.openLocal(dbFileBasePath + "/test-local.db")) { // Local only
//                try (Database db = Libsql.openEmbeddedReplica(dbFileBasePath + "/test-embedded.db", dbUrl, dbAuthToken)) { // Remote with local embedded replica
                    try (Connection conn = db.connect()) {
                        conn.execute("CREATE TABLE IF NOT EXISTS test (a int)");
                        conn.execute("INSERT INTO test VALUES(1)");
                        try (Rows rows = conn.query("SELECT COUNT(*) FROM test")) {
                            Object[] row = null;
                            while ((row = rows.nextRow()) != null) {
                                Log.i("turso-example", "row " + Arrays.toString(row));
                                for (Object o : row) {
                                    Log.i("turso-example", "row column " + o);
                                    count = o.toString();
                                }
                            }
                            Log.i("turso-example", "done");
                        }
                    }
                } catch (Exception e) {
                    Log.e("turso-example", "Error in db operations", e);
                }
                String text = count;
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.i("turso-example", text);
                        tv.setText(text);
                    }
                });
            }
        });
    }
}